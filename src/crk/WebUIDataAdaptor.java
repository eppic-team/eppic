package crk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.SpaceGroup;
import owl.core.util.Goodies;

import model.InterfaceItem;
import model.InterfaceScoreItem;
import model.PDBScoreItem;

public class WebUIDataAdaptor {

	private PDBScoreItem pdbScoreItem;
	
	public WebUIDataAdaptor() {
		pdbScoreItem = new PDBScoreItem();
	}
	
	public void setParams(CRKParams params) {
		pdbScoreItem.setPdbName(params.getJobName());
		pdbScoreItem.setHomologsCutoff(params.getMinHomologsCutoff());
		pdbScoreItem.setIdCutoff(params.getIdCutoff());
		pdbScoreItem.setQueryCovCutoff(params.getQueryCoverageCutoff());
		pdbScoreItem.setMaxNumSeqsCutoff(params.getMaxNumSeqsSelecton());
		pdbScoreItem.setBioCutoff(params.getEntrCallCutoff()-params.getGrayZoneWidth());
		pdbScoreItem.setXtalCutoff(params.getEntrCallCutoff()+params.getGrayZoneWidth());
		pdbScoreItem.setBsaToAsaCutoff(params.getCutoffCA());
		pdbScoreItem.setBsaToAsaSoftCutoff(params.getBsaToAsaSoftCutoff());
		pdbScoreItem.setBsaToAsaRelaxStep(params.getRelaxationStep());
		pdbScoreItem.setZoomUsed(params.isZooming());
	}

	public void setTitle(String title) {
		pdbScoreItem.setTitle(title);
	}
	
	public void setInterfaces(ChainInterfaceList interfaces) {
		for (ChainInterface interf:interfaces) {
			InterfaceItem ii = new InterfaceItem();
			ii.setId(interf.getId());
			ii.setArea(interf.getInterfaceArea());
			ii.setName(interf.getName());
			ii.setOperator(SpaceGroup.getAlgebraicFromMatrix(interf.getSecondTransf()));
			ii.setSize1(interf.getFirstRimCore().getCoreSize());
			ii.setSize2(interf.getSecondRimCore().getCoreSize());
			ii.setWarnings(new ArrayList<String>()); // we then need to add warnings from each method as we add the scores from each method
			pdbScoreItem.addInterfaceItem(ii);
		}

	}
	
	public void setGeometryScores(List<GeometryPredictor> gps) {
		for (int i=0;i<gps.size();i++) {
			InterfaceItem ii = pdbScoreItem.getInterfaceItem(i);
			InterfaceScoreItem isi = new InterfaceScoreItem();
			ii.addInterfaceScore(isi);
			CallType call = gps.get(i).getCall();
			isi.setCall(call.getName());
			isi.setCallReason(gps.get(i).getCallReason());
			isi.setMethod("Geometry");
			ii.getWarnings().addAll(gps.get(i).getWarnings());

		}
	}
	
	public void add(InterfaceEvolContextList iecl) {
		pdbScoreItem.setNumHomologsStrings(iecl.getNumHomologsStrings());
		for (int i=0;i<iecl.size();i++) {
			InterfaceEvolContext iec = iecl.get(i);
			InterfaceItem ii = pdbScoreItem.getInterfaceItem(i);
			InterfaceScoreItem isi = new InterfaceScoreItem();
			ii.addInterfaceScore(isi);
			isi.setId(iec.getInterface().getId());
			if (iec.getScoringType().getName().equals("entropy")) {
				isi.setMethod("Entropy");
			} else if (iec.getScoringType().getName().equals("KaKs ratio")) {
				isi.setMethod("Kaks");
			}
			
			CallType call = iec.getCall();
			
			isi.setCall(call.getName());
			isi.setCallReason(iec.getCallReason());
			ii.getWarnings().addAll(iec.getWarnings());

			double rat1Sc = iec.getCoreScore(InterfaceEvolContext.FIRST)/iec.getRimScore(InterfaceEvolContext.FIRST);
			double rat2Sc = iec.getCoreScore(InterfaceEvolContext.SECOND)/iec.getRimScore(InterfaceEvolContext.SECOND);

			if (iec.isScoreWeighted()) {
				isi.setWeightedCore1Scores(iec.getCoreScore(InterfaceEvolContext.FIRST));
				isi.setWeightedCore2Scores(iec.getCoreScore(InterfaceEvolContext.SECOND));
				isi.setWeightedRim1Scores(iec.getRimScore(InterfaceEvolContext.FIRST));
				isi.setWeightedRim2Scores(iec.getRimScore(InterfaceEvolContext.SECOND));
				isi.setWeightedRatio1Scores(rat1Sc);
				isi.setWeightedRatio2Scores(rat2Sc);
				isi.setWeightedFinalScores(iec.getFinalScore());
			} else {
				isi.setUnweightedCore1Scores(iec.getCoreScore(InterfaceEvolContext.FIRST));
				isi.setUnweightedCore2Scores(iec.getCoreScore(InterfaceEvolContext.SECOND));
				isi.setUnweightedRim1Scores(iec.getRimScore(InterfaceEvolContext.FIRST));
				isi.setUnweightedRim2Scores(iec.getRimScore(InterfaceEvolContext.SECOND));
				isi.setUnweightedRatio1Scores(rat1Sc);
				isi.setUnweightedRatio2Scores(rat2Sc);
				isi.setUnweightedFinalScores(iec.getFinalScore());				
			}
		}
	}
	
	public void writeToFile(File file) throws CRKException {
		try {
			Goodies.serialize(file,pdbScoreItem);
		} catch (IOException e) {
			throw new CRKException(e, e.getMessage(), true);
		}
	}
}
