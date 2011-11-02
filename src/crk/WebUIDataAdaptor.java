package crk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import crk.predictors.EvolInterfZPredictor;
import crk.predictors.EvolRimCorePredictor;
import crk.predictors.GeometryPredictor;

import model.InterfaceItem;
import model.InterfaceResidueItem;
import model.InterfaceResidueMethodItem;
import model.InterfaceScoreItem;
import model.PDBScoreItem;
import model.RunParametersItem;

import owl.core.runners.PymolRunner;
import owl.core.structure.AaResidue;
import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.InterfaceRimCore;
import owl.core.structure.PdbChain;
import owl.core.structure.Residue;
import owl.core.structure.SpaceGroup;
import owl.core.util.Goodies;


public class WebUIDataAdaptor {

	private static final int FIRST = 0;
	private static final int SECOND = 1;
	
	private PDBScoreItem pdbScoreItem;
	
//	private ChainInterfaceList interfaces;
//	private InterfaceEvolContextList iecl;
	private CRKParams params;
	
	private boolean resDetailsAdded;
	
	public WebUIDataAdaptor() {
		pdbScoreItem = new PDBScoreItem();
		resDetailsAdded = false;
	}
	
	public void setParams(CRKParams params) {
		this.params = params;
		pdbScoreItem.setPdbName(params.getJobName());
		RunParametersItem runParametersItem = new RunParametersItem();
		runParametersItem.setHomologsCutoff(params.getMinHomologsCutoff());
		runParametersItem.setIdCutoff(params.getIdCutoff());
		runParametersItem.setQueryCovCutoff(params.getQueryCoverageCutoff());
		runParametersItem.setMaxNumSeqsCutoff(params.getMaxNumSeqs());
		runParametersItem.setReducedAlphabet(params.getReducedAlphabet());
		runParametersItem.setCaCutoffForGeom(params.getCAcutoffForGeom());
		runParametersItem.setCaCutoffForRimCore(params.getCAcutoffForRimCore());
		runParametersItem.setCaCutoffForZscore(params.getCAcutoffForZscore());
		runParametersItem.setEntrCallCutoff(params.getEntrCallCutoff());
		runParametersItem.setKaksCallCutoff(params.getKaksCallCutoff());
		runParametersItem.setzScoreCutoff(params.getZscoreCutoff());
		runParametersItem.setMinCoreSizeForBio(params.getMinCoreSizeForBio());
		runParametersItem.setPdbScoreItem(pdbScoreItem);
		pdbScoreItem.setRunParameters(runParametersItem);
	}
	
	public void setTitle(String title) {
		pdbScoreItem.setTitle(title);
	}
	
	public void setSpaceGroup(SpaceGroup sg) {
		pdbScoreItem.setSpaceGroup(sg==null?"No space group info":sg.getShortSymbol());
	}
	
	public void setInterfaces(ChainInterfaceList interfaces) {
		//this.interfaces = interfaces;
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
			ii.setPdbScoreItem(pdbScoreItem);
			
			pdbScoreItem.addInterfaceItem(ii);
		}

	}
	
	public void setJmolScripts(ChainInterfaceList interfaces, PymolRunner pr) {
		for (int i=0;i<interfaces.size();i++) {
			pdbScoreItem.getInterfaceItem(i).setJmolScript(createJmolScript(interfaces.get(i+1), pr));
		}
	}
	
	private String createJmolScript(ChainInterface interf, PymolRunner pr) {
		char chain1 = interf.getFirstMolecule().getPdbChainCode().charAt(0);
		char chain2 = interf.getSecondPdbChainCodeForOutput().charAt(0);
		
		String color1 = pr.getHexColorCode(pr.getChainColor(chain1, 0, interf.isSymRelated()));
		String color2 = pr.getHexColorCode(pr.getChainColor(chain2, 1, interf.isSymRelated()));
		color1 = "[x"+color1.substring(1, color1.length())+"]"; // converting to jmol format
		color2 = "[x"+color2.substring(1, color2.length())+"]";
		String colorInterf1 = pr.getHexColorCode(pr.getInterf1Color());
		String colorInterf2 = pr.getHexColorCode(pr.getInterf2Color());
		colorInterf1 = "[x"+colorInterf1.substring(1, colorInterf1.length())+"]";
		colorInterf2 = "[x"+colorInterf2.substring(1, colorInterf2.length())+"]";
		
		StringBuffer sb = new StringBuffer();
		sb.append("cartoon on; wireframe off; spacefill off; set solvent off;");
		sb.append("select :"+chain1+"; color "+color1+";");
		sb.append("select :"+chain2+"; color "+color2+";");
		sb.append(getSelString("core", chain1, interf.getFirstRimCore().getCoreResidues())+";");
		sb.append(getSelString("core", chain2, interf.getSecondRimCore().getCoreResidues())+";");
		sb.append(getSelString("rim", chain1, interf.getFirstRimCore().getRimResidues())+";");
		sb.append(getSelString("rim", chain2, interf.getSecondRimCore().getRimResidues())+";");
		sb.append("define interface"+chain1+" core"+chain1+" or rim"+chain1+";");
		sb.append("define interface"+chain2+" core"+chain2+" or rim"+chain2+";");
		sb.append("define bothinterf interface"+chain1+" or interface"+chain2+";");
		// surfaces are cool but in jmol they don't display as good as in pymol, especially the transparency effect is quite bad
		//sb.append("select :"+chain1+"; isosurface surf"+chain1+" solvent;color isosurface gray;color isosurface translucent;");
		//sb.append("select :"+chain2+"; isosurface surf"+chain2+" solvent;color isosurface gray;color isosurface translucent;");
		sb.append("select interface"+chain1+";"+"color "+colorInterf1+";wireframe 0.3;");
		sb.append("select interface"+chain2+";"+"color "+colorInterf2+";wireframe 0.3;");
		return sb.toString();
	}
	
	private String getResiSelString(List<Residue> list, char chainName) {
		if (list.isEmpty()) return "0:"+chainName;
		StringBuffer sb = new StringBuffer();
		for (int i=0;i<list.size();i++) {
			sb.append(list.get(i).getSerial()+":"+chainName);
			if (i!=list.size()-1) sb.append(",");
		}
		return sb.toString();
	}

	private String getSelString(String namePrefix, char chainName, List<Residue> list) {
		return "define "+namePrefix+chainName+" "+getResiSelString(list,chainName);
	}
	
	public void setGeometryScores(List<GeometryPredictor> gps) {
		for (int i=0;i<gps.size();i++) {
			InterfaceItem ii = pdbScoreItem.getInterfaceItem(i);
			InterfaceScoreItem isi = new InterfaceScoreItem();
			ii.addInterfaceScore(isi);
			isi.setInterfaceItem(ii);
			isi.setId(gps.get(i).getInterface().getId());
			CallType call = gps.get(i).getCall();
			isi.setCall(call.getName());
			isi.setCallReason(gps.get(i).getCallReason());
			isi.setMethod("Geometry");
			ii.getWarnings().addAll(gps.get(i).getWarnings());

		}
	}
	
	public void add(InterfaceEvolContextList iecl) {
		
		//this.iecl = iecl; // we cache the last one added
		pdbScoreItem.setNumHomologsStrings(iecl.getNumHomologsStrings());
		
		// first we add the residue details only once
		if (!resDetailsAdded) {
			for (int i=0;i<iecl.size();i++) {
				InterfaceEvolContext iec = iecl.get(i);
				InterfaceItem ii = pdbScoreItem.getInterfaceItem(i);
				addResidueDetails(ii, iec, params.isDoScoreEntropies(), params.isDoScoreKaks());
			}
			resDetailsAdded = true;
		}
		
		String method = null;
		if (iecl.getScoringType()==ScoringType.ENTROPY) {
			method = "Entropy";
		} else if (iecl.getScoringType()==ScoringType.KAKS) {
			method = "Kaks";
		} else if (iecl.getScoringType()==ScoringType.ZSCORE) {
			method = "Z-scores";
		}

		if (iecl.getScoringType()==ScoringType.ZSCORE) {
			for (int i=0;i<iecl.size();i++) {
				InterfaceEvolContext iec = iecl.get(i);
				EvolInterfZPredictor ezp = iecl.getEvolInterfZPredictor(i);

				InterfaceItem ii = pdbScoreItem.getInterfaceItem(i);

				InterfaceScoreItem isi = new InterfaceScoreItem();
				ii.addInterfaceScore(isi);
				isi.setInterfaceItem(ii);
				isi.setId(iec.getInterface().getId());
				isi.setMethod(method);

				CallType call = ezp.getCall();	
				isi.setCall(call.getName());
				isi.setCallReason(ezp.getCallReason());
				ii.getWarnings().addAll(ezp.getWarnings());
 

				isi.setUnweightedCore1Scores(ezp.getMember1Predictor().getCoreScore());
				isi.setUnweightedCore2Scores(ezp.getMember2Predictor().getCoreScore());
				//isi.setUnweightedRim1Scores(ezp.getMember1Predictor().getRimScore());
				//isi.setUnweightedRim2Scores(ezp.getMember2Predictor().getRimScore());
				isi.setUnweightedRatio1Scores(ezp.getMember1Predictor().getScore());
				isi.setUnweightedRatio2Scores(ezp.getMember2Predictor().getScore());
				isi.setUnweightedFinalScores(ezp.getScore());				

			}

		} else {
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
					isi.setInterfaceItem(ii);
					ii.addInterfaceScore(isi);
					isi.setId(iec.getInterface().getId());
					isi.setMethod(method);

					// NOTE: here we are only getting call, callReason and warnings from first of the 2 added (unweighted) 
					// thus we are ignoring the weighted calls in the web ui
					CallType call = ercp.getCall();	
					isi.setCall(call.getName());
					isi.setCallReason(ercp.getCallReason());
					ii.getWarnings().addAll(ercp.getWarnings());

				} 


				if (iecl.isScoreWeighted()) {
					isi.setWeightedCore1Scores(ercp.getMember1Predictor().getCoreScore());
					isi.setWeightedCore2Scores(ercp.getMember2Predictor().getCoreScore());
					isi.setWeightedRim1Scores(ercp.getMember1Predictor().getRimScore());
					isi.setWeightedRim2Scores(ercp.getMember2Predictor().getRimScore());
					isi.setWeightedRatio1Scores(ercp.getMember1Predictor().getScore());
					isi.setWeightedRatio2Scores(ercp.getMember2Predictor().getScore());
					isi.setWeightedFinalScores(ercp.getScore());
				} else {
					isi.setUnweightedCore1Scores(ercp.getMember1Predictor().getCoreScore());
					isi.setUnweightedCore2Scores(ercp.getMember2Predictor().getCoreScore());
					isi.setUnweightedRim1Scores(ercp.getMember1Predictor().getRimScore());
					isi.setUnweightedRim2Scores(ercp.getMember2Predictor().getRimScore());
					isi.setUnweightedRatio1Scores(ercp.getMember1Predictor().getScore());
					isi.setUnweightedRatio2Scores(ercp.getMember2Predictor().getScore());
					isi.setUnweightedFinalScores(ercp.getScore());				
				}
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
	
	private void addResidueDetails(InterfaceItem ii, InterfaceEvolContext iec, boolean includeEntropy, boolean includeKaks) {
		
		List<InterfaceResidueItem> iril = new ArrayList<InterfaceResidueItem>();
		ii.setInterfaceResidues(iril);
		
		addResidueDetailsOfPartner(iril, iec, includeEntropy, includeKaks, 0);
		addResidueDetailsOfPartner(iril, iec, includeEntropy, includeKaks, 1);

	}
	
	private void addResidueDetailsOfPartner(List<InterfaceResidueItem> iril, InterfaceEvolContext iec, boolean includeEntropy, boolean includeKaks, int molecId) {
		ChainInterface interf = iec.getInterface();
		ChainEvolContext cec = iec.getChainEvolContext(molecId);
		
		if (interf.isProtein()) {
			PdbChain mol = null;
			InterfaceRimCore rimCore = null;
			if (molecId==FIRST) {
				mol = interf.getFirstMolecule();
				rimCore = interf.getFirstRimCore();
			}
			else if (molecId==SECOND) {
				mol = interf.getSecondMolecule();
				rimCore = interf.getSecondRimCore();
			}
			 
			List<Double> entropies = null;
			if (cec.hasQueryMatch()) 
				entropies = cec.getConservationScores(ScoringType.ENTROPY);
			List<Double> kaksRatios = null;
			if (includeKaks && iec.canDoKaks())
				kaksRatios = cec.getConservationScores(ScoringType.KAKS);
			for (Residue residue:mol) {
				String resType = residue.getLongCode();
				int assignment = -1;
				float asa = (float) residue.getAsa();
				float bsa = (float) residue.getBsa();
				if (rimCore.getRimResidues().contains(residue)) assignment = InterfaceResidueItem.RIM;
				else if (rimCore.getCoreResidues().contains(residue)) assignment = InterfaceResidueItem.CORE;

				if (assignment==-1 && asa>0) assignment = InterfaceResidueItem.SURFACE;

				int queryUniprotPos = -1;
				if (!mol.isNonPolyChain() && mol.getSequence().isProtein() && cec.hasQueryMatch()) 
					queryUniprotPos = cec.getQueryUniprotPosForPDBPos(residue.getSerial());

				float entropy = -1;
				if (entropies!=null && residue instanceof AaResidue) {	
					if (queryUniprotPos!=-1) entropy = (float) entropies.get(queryUniprotPos).doubleValue();
				}
				float kaks = -1;
				if (includeKaks && iec.canDoKaks() && (residue instanceof AaResidue) && queryUniprotPos!=-1)
					kaks = (float)kaksRatios.get(queryUniprotPos).doubleValue();
				InterfaceResidueItem iri = new InterfaceResidueItem(residue.getSerial(),resType,asa,bsa,bsa/asa,assignment);
				iri.setStructure(molecId+1); // structure ids are 1 and 2 while molecId are 0 and 1

				List<InterfaceResidueMethodItem> scores = new ArrayList<InterfaceResidueMethodItem>();
				scores.add(new InterfaceResidueMethodItem(0, "geometry"));
				if (includeEntropy) scores.add(new InterfaceResidueMethodItem(entropy, "entropy"));
				if (includeKaks && iec.canDoKaks()) scores.add(new InterfaceResidueMethodItem(kaks, "kaks"));
				iri.setInterfaceResidueMethodItems(scores);
				iril.add(iri);
			}
		}
	}
	
}
