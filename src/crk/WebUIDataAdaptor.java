package crk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import crk.predictors.EvolRimCorePredictor;
import crk.predictors.GeometryPredictor;

import model.InterfaceItem;
import model.InterfaceResidueItem;
import model.InterfaceResidueMethodItem;
import model.InterfaceScoreItem;
import model.PDBScoreItem;

import owl.core.structure.AaResidue;
import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.InterfaceRimCore;
import owl.core.structure.PdbChain;
import owl.core.structure.Residue;
import owl.core.structure.SpaceGroup;
import owl.core.util.Goodies;


public class WebUIDataAdaptor {

	private PDBScoreItem pdbScoreItem;
	
	private ChainInterfaceList interfaces;
	private InterfaceEvolContextList iecl;
	private CRKParams params;
	
	
	
	public WebUIDataAdaptor() {
		pdbScoreItem = new PDBScoreItem();
	}
	
	public void setParams(CRKParams params) {
		this.params = params;
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
	
	public void setSpaceGroup(SpaceGroup sg) {
		pdbScoreItem.setSpaceGroup(sg.getShortSymbol());
	}
	
	public void setInterfaces(ChainInterfaceList interfaces) {
		this.interfaces = interfaces;
		for (ChainInterface interf:interfaces) {
			InterfaceItem ii = new InterfaceItem();
			ii.setId(interf.getId());
			ii.setArea(interf.getInterfaceArea());
			ii.setName(interf.getName());
			ii.setOperator(SpaceGroup.getAlgebraicFromMatrix(interf.getSecondTransf()));
			ii.setSize1(interf.getFirstRimCore().getCoreSize());
			ii.setSize2(interf.getSecondRimCore().getCoreSize());
			ii.setWarnings(new ArrayList<String>()); // we then need to add warnings from each method as we add the scores from each method
			
			ii.setAsaC1(interf.getFirstRimCore().getAsaCore());
			ii.setAsaR1(interf.getFirstRimCore().getAsaRim());
			ii.setBsaC1(interf.getFirstRimCore().getBsaCore());
			ii.setBsaR1(interf.getFirstRimCore().getBsaRim());
			ii.setAsaC2(interf.getSecondRimCore().getAsaCore());
			ii.setAsaR2(interf.getSecondRimCore().getAsaRim());
			ii.setBsaC2(interf.getSecondRimCore().getBsaCore());
			ii.setBsaR2(interf.getSecondRimCore().getBsaRim());
			
			pdbScoreItem.addInterfaceItem(ii);
		}

	}
	
	public void setGeometryScores(List<GeometryPredictor> gps) {
		for (int i=0;i<gps.size();i++) {
			InterfaceItem ii = pdbScoreItem.getInterfaceItem(i);
			InterfaceScoreItem isi = new InterfaceScoreItem();
			ii.addInterfaceScore(isi);
			isi.setId(gps.get(i).getInterface().getId());
			CallType call = gps.get(i).getCall();
			isi.setCall(call.getName());
			isi.setCallReason(gps.get(i).getCallReason());
			isi.setMethod("Geometry");
			ii.getWarnings().addAll(gps.get(i).getWarnings());

		}
	}
	
	public void add(InterfaceEvolContextList iecl) {
		
		this.iecl = iecl; // we cache the last one added
		pdbScoreItem.setNumHomologsStrings(iecl.getNumHomologsStrings());
		String method = null;
		if (iecl.getScoringType()==ScoringType.ENTROPY) {
			method = "Entropy";
		} else if (iecl.getScoringType()==ScoringType.KAKS) {
			method = "Kaks";
		}

		for (int i=0;i<iecl.size();i++) {
			InterfaceEvolContext iec = iecl.get(i);
			EvolRimCorePredictor ercp = iecl.getEvolRimCorePredictor(i);
			
			InterfaceItem ii = pdbScoreItem.getInterfaceItem(i);


			boolean append = false;
			InterfaceScoreItem isi = null;
			for (InterfaceScoreItem existing: ii.getInterfaceScores()){
				if (existing.getMethod().equals(method)) { //if we already have the method in, then this is simply the second part of it (weighted scores)
					append = true;
					isi = existing;
				}
			}
			
			if (!append) {
				isi = new InterfaceScoreItem();
				ii.addInterfaceScore(isi);
				isi.setId(iec.getInterface().getId());
				isi.setMethod(method);
				
				// TODO notice here we are only getting call, callReason and warnings from first of the 2 added (usually unweighted) 
				// TODO we still have to decide how to get a call from both weighted/unweighted scores
				CallType call = ercp.getCall();	
				isi.setCall(call.getName());
				isi.setCallReason(ercp.getCallReason());
				ii.getWarnings().addAll(ercp.getWarnings());

			} 
			

			double rat1Sc = ercp.getCoreScore(InterfaceEvolContext.FIRST)/ercp.getRimScore(InterfaceEvolContext.FIRST);
			double rat2Sc = ercp.getCoreScore(InterfaceEvolContext.SECOND)/ercp.getRimScore(InterfaceEvolContext.SECOND);

			if (iecl.isScoreWeighted()) {
				isi.setWeightedCore1Scores(ercp.getCoreScore(InterfaceEvolContext.FIRST));
				isi.setWeightedCore2Scores(ercp.getCoreScore(InterfaceEvolContext.SECOND));
				isi.setWeightedRim1Scores(ercp.getRimScore(InterfaceEvolContext.FIRST));
				isi.setWeightedRim2Scores(ercp.getRimScore(InterfaceEvolContext.SECOND));
				isi.setWeightedRatio1Scores(rat1Sc);
				isi.setWeightedRatio2Scores(rat2Sc);
				isi.setWeightedFinalScores(ercp.getFinalScore());
			} else {
				isi.setUnweightedCore1Scores(ercp.getCoreScore(InterfaceEvolContext.FIRST));
				isi.setUnweightedCore2Scores(ercp.getCoreScore(InterfaceEvolContext.SECOND));
				isi.setUnweightedRim1Scores(ercp.getRimScore(InterfaceEvolContext.FIRST));
				isi.setUnweightedRim2Scores(ercp.getRimScore(InterfaceEvolContext.SECOND));
				isi.setUnweightedRatio1Scores(rat1Sc);
				isi.setUnweightedRatio2Scores(rat2Sc);
				isi.setUnweightedFinalScores(ercp.getFinalScore());				
			}
		}
	}
	
	public void writePdbScoreItemFile(File file) throws CRKException {
		try {
			Goodies.serialize(file,pdbScoreItem);
		} catch (IOException e) {
			throw new CRKException(e, e.getMessage(), true);
		}
	}
	
	public void writeResidueDetailsFiles(boolean includeEntropy, boolean includeKaks, String suffix) throws CRKException {
		try {
			for (int i=0;i<interfaces.size();i++) {
				ChainInterface interf = interfaces.get(i+1);
				File file = params.getOutputFile("."+interf.getId()+"."+suffix);
				if (!includeEntropy && !includeKaks) {
					writeResidueDetailsFile(i,file);
				} else {
					writeResidueDetailsFile(i,file, includeKaks);
				}
				
			}
		} catch (IOException e) {
			throw new CRKException(e,e.getMessage(),true);
		}
	}
	
	
	private void writeResidueDetailsFile(int i,File file, boolean includeKaks) throws IOException {
		InterfaceEvolContext iec = iecl.get(i);
		ChainInterface interf = iec.getInterface();
		ChainEvolContext firstCec = iec.getFirstChainEvolContext();
		ChainEvolContext secondCec = iec.getSecondChainEvolContext();
		List<InterfaceResidueItem> partner1 = new ArrayList<InterfaceResidueItem>();
		List<InterfaceResidueItem> partner2 = new ArrayList<InterfaceResidueItem>();
		HashMap<Integer, List<InterfaceResidueItem>>  resDetailsMap = new HashMap<Integer, List<InterfaceResidueItem>>();
		resDetailsMap.put(1, partner1);
		resDetailsMap.put(2, partner2);
		if (interf.isFirstProtein() && interf.isSecondProtein()) {
			PdbChain firstMol = interf.getFirstMolecule();
			InterfaceRimCore rimCore = interf.getFirstRimCore(); 
			List<Double> entropies = firstCec.getConservationScores(ScoringType.ENTROPY);
			List<Double> kaksRatios = null;
			if (includeKaks && iec.canDoCRK())
				kaksRatios = firstCec.getConservationScores(ScoringType.KAKS);
			for (Residue residue:firstMol) {
				String resType = residue.getLongCode();
				int assignment = -1;
				float asa = (float) residue.getAsa();
				float bsa = (float) residue.getBsa();
				if (rimCore.getRimResidues().contains(residue)) assignment = InterfaceResidueItem.RIM;
				else if (rimCore.getCoreResidues().contains(residue)) assignment = InterfaceResidueItem.CORE;

				if (assignment==-1 && asa>0) assignment = InterfaceResidueItem.SURFACE;

				int queryUniprotPos = -1;
				if (!firstMol.isNonPolyChain() && firstMol.getSequence().isProtein()) 
					queryUniprotPos = firstCec.getQueryUniprotPosForPDBPos(residue.getSerial());

				float entropy = -1;
				if (residue instanceof AaResidue) {	
					if (queryUniprotPos!=-1) entropy = (float) entropies.get(queryUniprotPos).doubleValue();
				}
				float kaks = -1;
				if (includeKaks && iec.canDoCRK() && (residue instanceof AaResidue) && queryUniprotPos!=-1)
					kaks = (float)kaksRatios.get(queryUniprotPos).doubleValue();
				InterfaceResidueItem iri = new InterfaceResidueItem(residue.getSerial(),resType,asa,bsa,bsa/asa,assignment);

				List<InterfaceResidueMethodItem> scores = new ArrayList<InterfaceResidueMethodItem>();
				scores.add(new InterfaceResidueMethodItem(entropy, "entropy"));
				if (includeKaks && iec.canDoCRK()) scores.add(new InterfaceResidueMethodItem(kaks, "kaks"));
				iri.setInterfaceResidueMethodItems(scores);
				partner1.add(iri);
			}
			PdbChain secondMol = interf.getSecondMolecule();
			rimCore = interf.getSecondRimCore();
			entropies = secondCec.getConservationScores(ScoringType.ENTROPY);
			if (includeKaks && iec.canDoCRK()) 
				kaksRatios = secondCec.getConservationScores(ScoringType.KAKS);
			for (Residue residue:secondMol) {
				String resType = residue.getLongCode();
				int assignment = -1;
				float asa = (float) residue.getAsa();
				float bsa = (float) residue.getBsa();
				if (rimCore.getRimResidues().contains(residue)) assignment = InterfaceResidueItem.RIM;
				else if (rimCore.getCoreResidues().contains(residue)) assignment = InterfaceResidueItem.CORE;

				if (assignment==-1 && asa>0) assignment = InterfaceResidueItem.SURFACE;

				int queryUniprotPos = -1;
				if (!secondMol.isNonPolyChain() && secondMol.getSequence().isProtein()) 
					queryUniprotPos = secondCec.getQueryUniprotPosForPDBPos(residue.getSerial());

				float entropy = -1;
				if (residue instanceof AaResidue) {
					if (queryUniprotPos!=-1) entropy = (float) entropies.get(queryUniprotPos).doubleValue();
				}
				float kaks = -1;
				if (includeKaks && iec.canDoCRK() && (residue instanceof AaResidue) && queryUniprotPos!=-1)
					kaks = (float) kaksRatios.get(queryUniprotPos).doubleValue();
				InterfaceResidueItem iri = new InterfaceResidueItem(residue.getSerial(),resType,asa,bsa,bsa/asa,assignment);
				List<InterfaceResidueMethodItem> scores = new ArrayList<InterfaceResidueMethodItem>();
				scores.add(new InterfaceResidueMethodItem(entropy, "entropy"));
				if (includeKaks && iec.canDoCRK())
					scores.add(new InterfaceResidueMethodItem(kaks, "kaks"));
				iri.setInterfaceResidueMethodItems(scores);
				partner2.add(iri);
			}
		}
		Goodies.serialize(file, resDetailsMap);
	}
	
	private void writeResidueDetailsFile(int i,File file) throws IOException {
		ChainInterface interf = interfaces.get(i+1);

		List<InterfaceResidueItem> partner1 = new ArrayList<InterfaceResidueItem>();
		List<InterfaceResidueItem> partner2 = new ArrayList<InterfaceResidueItem>();
		HashMap<Integer, List<InterfaceResidueItem>>  resDetailsMap = new HashMap<Integer, List<InterfaceResidueItem>>();
		resDetailsMap.put(1, partner1);
		resDetailsMap.put(2, partner2);
		if (interf.isFirstProtein() && interf.isSecondProtein()) {
			PdbChain firstMol = interf.getFirstMolecule();
			InterfaceRimCore rimCore = interf.getFirstRimCore(); 
			for (Residue residue:firstMol) {
				String resType = residue.getLongCode();
				int assignment = -1;
				float asa = (float) residue.getAsa();
				float bsa = (float) residue.getBsa();
				if (rimCore.getRimResidues().contains(residue)) assignment = InterfaceResidueItem.RIM;
				else if (rimCore.getCoreResidues().contains(residue)) assignment = InterfaceResidueItem.CORE;

				if (assignment==-1 && asa>0) assignment = InterfaceResidueItem.SURFACE;

				InterfaceResidueItem iri = new InterfaceResidueItem(residue.getSerial(),resType,asa,bsa,bsa/asa,assignment);

				List<InterfaceResidueMethodItem> scores = new ArrayList<InterfaceResidueMethodItem>();
				scores.add(new InterfaceResidueMethodItem(0, "geometry"));
				iri.setInterfaceResidueMethodItems(scores);
				partner1.add(iri);
			}
			PdbChain secondMol = interf.getSecondMolecule();
			rimCore = interf.getSecondRimCore();
			for (Residue residue:secondMol) {
				String resType = residue.getLongCode();
				int assignment = -1;
				float asa = (float) residue.getAsa();
				float bsa = (float) residue.getBsa();
				if (rimCore.getRimResidues().contains(residue)) assignment = InterfaceResidueItem.RIM;
				else if (rimCore.getCoreResidues().contains(residue)) assignment = InterfaceResidueItem.CORE;

				if (assignment==-1 && asa>0) assignment = InterfaceResidueItem.SURFACE;

				InterfaceResidueItem iri = new InterfaceResidueItem(residue.getSerial(),resType,asa,bsa,bsa/asa,assignment);
				List<InterfaceResidueMethodItem> scores = new ArrayList<InterfaceResidueMethodItem>();
				scores.add(new InterfaceResidueMethodItem(0, "geometry"));
				iri.setInterfaceResidueMethodItems(scores);
				partner2.add(iri);
			}
		}
		Goodies.serialize(file, resDetailsMap);
	}
}
