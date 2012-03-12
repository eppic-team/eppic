package crk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import crk.predictors.CombinedPredictor;
import crk.predictors.EvolInterfZPredictor;
import crk.predictors.EvolRimCorePredictor;
import crk.predictors.GeometryPredictor;

import model.InterfaceItemDB;
import model.InterfaceResidueItemDB;
import model.InterfaceScoreItemDB;
import model.HomologsInfoItemDB;
import model.PDBScoreItemDB;
import model.QueryWarningItemDB;
import model.RunParametersItemDB;
import model.WarningItemDB;

import owl.core.runners.PymolRunner;
import owl.core.structure.AaResidue;
import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.InterfaceRimCore;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbChain;
import owl.core.structure.Residue;
import owl.core.structure.SpaceGroup;
import owl.core.util.Goodies;


public class WebUIDataAdaptor {

	private static final int FIRST = 0;
	private static final int SECOND = 1;
	
	private PDBScoreItemDB pdbScoreItem;
	
	private CRKParams params;
	
	private RunParametersItemDB runParametersItem;
	
	public WebUIDataAdaptor() {
		pdbScoreItem = new PDBScoreItemDB();
	}
	
	public void setParams(CRKParams params) {
		this.params = params;
		pdbScoreItem.setPdbName(params.getJobName());
		runParametersItem = new RunParametersItemDB();
		runParametersItem.setHomologsCutoff(params.getMinNumSeqs());
		runParametersItem.setHomSoftIdCutoff(params.getHomSoftIdCutoff());
		runParametersItem.setHomHardIdCutoff(params.getHomHardIdCutoff());
		runParametersItem.setQueryCovCutoff(params.getQueryCoverageCutoff());
		runParametersItem.setMaxNumSeqsCutoff(params.getMaxNumSeqs());
		runParametersItem.setReducedAlphabet(params.getReducedAlphabet());
		runParametersItem.setCaCutoffForGeom(params.getCAcutoffForGeom());
		runParametersItem.setCaCutoffForRimCore(params.getCAcutoffForRimCore());
		runParametersItem.setCaCutoffForZscore(params.getCAcutoffForZscore());
		runParametersItem.setEntrCallCutoff(params.getEntrCallCutoff());
		runParametersItem.setzScoreCutoff(params.getZscoreCutoff());
		runParametersItem.setMinCoreSizeForBio(params.getMinCoreSizeForBio());
		runParametersItem.setPdbScoreItem(pdbScoreItem);
		runParametersItem.setCrkVersion(CRKParams.PROGRAM_VERSION);
		pdbScoreItem.setRunParameters(runParametersItem);
	}
	
	public void setPdbMetadata(PdbAsymUnit pdb) {
		pdbScoreItem.setTitle(pdb.getTitle());
		SpaceGroup sg = pdb.getSpaceGroup();
		pdbScoreItem.setSpaceGroup(sg==null?null:sg.getShortSymbol());
		pdbScoreItem.setResolution(pdb.getResolution());
		pdbScoreItem.setExpMethod(pdb.getExpMethod());
		
	}
	
	public void setInterfaces(ChainInterfaceList interfaces) {
		//this.interfaces = interfaces;
		for (ChainInterface interf:interfaces) {
			InterfaceItemDB ii = new InterfaceItemDB();
			ii.setId(interf.getId());
			ii.setArea(interf.getInterfaceArea());
			ii.setName(interf.getName());
			ii.setOperator(SpaceGroup.getAlgebraicFromMatrix(interf.getSecondTransf()));
			ii.setSize1(interf.getFirstRimCore().getCoreSize());
			ii.setSize2(interf.getSecondRimCore().getCoreSize());
			ii.setWarnings(new ArrayList<WarningItemDB>()); // we then need to add warnings from each method as we add the scores from each method
			
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
	
	public void setJmolScripts(ChainInterfaceList interfaces, double caCutoff, PymolRunner pr) {
		for (int i=0;i<interfaces.size();i++) {
			pdbScoreItem.getInterfaceItem(i).setJmolScript(createJmolScript(interfaces.get(i+1), caCutoff, pr));
		}
	}
	
	private String createJmolScript(ChainInterface interf, double caCutoff, PymolRunner pr) {
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
		interf.calcRimAndCore(caCutoff);
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
		sb.append("select interface"+chain1+";wireframe 0.3;");
		sb.append("select interface"+chain2+";wireframe 0.3;");
		sb.append("select core"+chain1+";"+"color "+colorInterf1+";wireframe 0.3;");
		sb.append("select core"+chain2+";"+"color "+colorInterf2+";wireframe 0.3;");		
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
			InterfaceItemDB ii = pdbScoreItem.getInterfaceItem(i);
			InterfaceScoreItemDB isi = new InterfaceScoreItemDB();
			ii.addInterfaceScore(isi);
			isi.setInterfaceItem(ii);
			isi.setId(gps.get(i).getInterface().getId());
			CallType call = gps.get(i).getCall();
			isi.setCallName(call.getName());
			isi.setCallReason(gps.get(i).getCallReason());
			isi.setMethod("Geometry");
			
			if(gps.get(i).getWarnings() != null)
			{
				List<String> warnings = gps.get(i).getWarnings();
				for(String warning: warnings)
				{
					WarningItemDB warningItem = new WarningItemDB();
					warningItem.setText(warning);
					warningItem.setInterfaceItem(ii);
					ii.getWarnings().add(warningItem);
				}
			}

		}
	}
	
	public void add(InterfaceEvolContextList iecl) {
		
		List<HomologsInfoItemDB> homInfos = new ArrayList<HomologsInfoItemDB>();
		
		ChainEvolContextList cecl = iecl.getChainEvolContextList();
		for (ChainEvolContext cec:cecl.getAllChainEvolContext()) {
			HomologsInfoItemDB homInfo = new HomologsInfoItemDB();
			homInfo.setChains(cec.getSeqIndenticalChainStr());
			homInfo.setHasQueryMatch(cec.hasQueryMatch());
			
			List<QueryWarningItemDB> queryWarningItemDBs = new ArrayList<QueryWarningItemDB>();
			for(String queryWarning : cec.getQueryWarnings())
			{
				QueryWarningItemDB queryWarningItemDB = new QueryWarningItemDB();
				queryWarningItemDB.setHomologsInfoItem(homInfo);
				queryWarningItemDB.setText(queryWarning);
				queryWarningItemDBs.add(queryWarningItemDB);
			}
			
			homInfo.setQueryWarnings(queryWarningItemDBs);
			
			if (cec.hasQueryMatch()) { //all other fields remain null otherwise
				
				homInfo.setNumHomologs(cec.getNumHomologs());
				homInfo.setUniprotId(cec.getQuery().getUniId()); 
				if (!cec.isSearchWithFullUniprot()) { 
					homInfo.setSubInterval(cec.getQueryInterval().beg+"-"+cec.getQueryInterval().end);
				}
				homInfo.setAlignedSeq1(cec.getPdb2uniprotAln().getAlignedSequences()[0]);
				homInfo.setMarkupLine(String.valueOf(cec.getPdb2uniprotAln().getMarkupLine()));
				homInfo.setAlignedSeq2(cec.getPdb2uniprotAln().getAlignedSequences()[1]);
				homInfo.setIdCutoffUsed(cec.getIdCutoff());
			} 

			homInfo.setPdbScoreItem(pdbScoreItem);
			homInfos.add(homInfo);	
		}
		
		pdbScoreItem.setHomologsInfoItems(homInfos);
		

		for (int i=0;i<iecl.size();i++) {
			
			InterfaceEvolContext iec = iecl.get(i);
			InterfaceItemDB ii = pdbScoreItem.getInterfaceItem(i);
			
			// 1) we add entropy values to the residue details
			addEntropyToResidueDetails(ii.getInterfaceResidues(), iec);
			
			
			// 2) z-scores
			EvolInterfZPredictor ezp = iecl.getEvolInterfZPredictor(i);
			InterfaceScoreItemDB isiZ = new InterfaceScoreItemDB();
			ii.addInterfaceScore(isiZ);
			isiZ.setInterfaceItem(ii);
			isiZ.setId(iec.getInterface().getId());
			isiZ.setMethod("Z-scores");

			CallType call = ezp.getCall();	
			isiZ.setCallName(call.getName());
			isiZ.setCallReason(ezp.getCallReason());
			
			if(ezp.getWarnings() != null) {
				List<String> warnings = ezp.getWarnings();
				for(String warning: warnings) {
					WarningItemDB warningItem = new WarningItemDB();
					warningItem.setText(warning);
					warningItem.setInterfaceItem(ii);
					ii.getWarnings().add(warningItem);
				}
			}

			isiZ.setUnweightedCore1Scores(ezp.getMember1Predictor().getCoreScore());
			isiZ.setUnweightedCore2Scores(ezp.getMember2Predictor().getCoreScore());
			isiZ.setUnweightedRatio1Scores(ezp.getMember1Predictor().getScore());
			isiZ.setUnweightedRatio2Scores(ezp.getMember2Predictor().getScore());
			isiZ.setUnweightedFinalScores(ezp.getScore());				

			
			// 3) rim-core entropies
			EvolRimCorePredictor ercp = iecl.getEvolRimCorePredictor(i);

			InterfaceScoreItemDB isiRC = new InterfaceScoreItemDB();
			isiRC.setInterfaceItem(ii);
			ii.addInterfaceScore(isiRC);
			isiRC.setId(iec.getInterface().getId());
			isiRC.setMethod("Entropy");

			call = ercp.getCall();	
			isiRC.setCallName(call.getName());
			isiRC.setCallReason(ercp.getCallReason());

			if(ercp.getWarnings() != null) {
				List<String> warnings = ercp.getWarnings();
				for(String warning: warnings) {
					WarningItemDB warningItem = new WarningItemDB();
					warningItem.setText(warning);
					warningItem.setInterfaceItem(ii);
					ii.getWarnings().add(warningItem);
				}
			}

			isiRC.setUnweightedCore1Scores(ercp.getMember1Predictor().getCoreScore());
			isiRC.setUnweightedCore2Scores(ercp.getMember2Predictor().getCoreScore());
			isiRC.setUnweightedRim1Scores(ercp.getMember1Predictor().getRimScore());
			isiRC.setUnweightedRim2Scores(ercp.getMember2Predictor().getRimScore());
			isiRC.setUnweightedRatio1Scores(ercp.getMember1Predictor().getScore());
			isiRC.setUnweightedRatio2Scores(ercp.getMember2Predictor().getScore());
			isiRC.setUnweightedFinalScores(ercp.getScore());				

		}
		

	}
	
	public void setCombinedPredictors(List<CombinedPredictor> cps) {
		for (int i=0;i<cps.size();i++) {
			InterfaceItemDB ii = pdbScoreItem.getInterfaceItem(i);
			ii.setFinalCallName(cps.get(i).getCall().getName());		
			ii.setFinalCallReason(cps.get(i).getCallReason());
			if(cps.get(i).getWarnings() != null)
			{
				List<String> warnings = cps.get(i).getWarnings();
				for(String warning: warnings)
				{
					WarningItemDB warningItem = new WarningItemDB();
					warningItem.setText(warning);
					warningItem.setInterfaceItem(ii);
					ii.getWarnings().add(warningItem);
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
	
	public void addResidueDetails(ChainInterfaceList interfaces, double caCutoff) {
		for (int i=0;i<interfaces.size();i++) {

			ChainInterface interf = interfaces.get(i+1);
			InterfaceItemDB ii = pdbScoreItem.getInterfaceItem(i);

			// we add the residue details
			addResidueDetails(ii, interf, caCutoff, params.isDoScoreEntropies());
		}
	}
	
	private void addResidueDetails(InterfaceItemDB ii, ChainInterface interf, double caCutoff, boolean includeEntropy) {
		
		List<InterfaceResidueItemDB> iril = new ArrayList<InterfaceResidueItemDB>();
		ii.setInterfaceResidues(iril);
		
		addResidueDetailsOfPartner(iril, interf, caCutoff, 0);
		addResidueDetailsOfPartner(iril, interf, caCutoff, 1);

		for(InterfaceResidueItemDB iri : iril)
		{
			iri.setInterfaceItem(ii);
		}
	}
	
	private void addResidueDetailsOfPartner(List<InterfaceResidueItemDB> iril, ChainInterface interf, double caCutoff, int molecId) {
		if (interf.isProtein()) {
			interf.calcRimAndCore(caCutoff);
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
			 
			for (Residue residue:mol) {
				String resType = residue.getLongCode();
				int assignment = -1;
				float asa = (float) residue.getAsa();
				float bsa = (float) residue.getBsa();
				if (rimCore.getRimResidues().contains(residue)) assignment = InterfaceResidueItemDB.RIM;
				else if (rimCore.getCoreResidues().contains(residue)) assignment = InterfaceResidueItemDB.CORE;

				if (assignment==-1 && asa>0) assignment = InterfaceResidueItemDB.SURFACE;

				InterfaceResidueItemDB iri = new InterfaceResidueItemDB(residue.getSerial(),residue.getPdbSerial(),resType,asa,bsa,bsa/asa,assignment,null);
				iri.setStructure(molecId+1); // structure ids are 1 and 2 while molecId are 0 and 1

				iril.add(iri);
			}
		}
	}

	private void addEntropyToResidueDetails(List<InterfaceResidueItemDB> iril, InterfaceEvolContext iec) {
		ChainInterface interf = iec.getInterface();
		
		
		int[] molecIds = new int[2];
		molecIds[0] = 0;
		molecIds[1] = 1;

		// beware the counter is global for both molecule 1 and 2 (as the List<InterfaceResidueItemDB> contains both, identified by a structure id 1 or 2)
		int i = 0;  

		for (int molecId:molecIds) { 
			ChainEvolContext cec = iec.getChainEvolContext(molecId);
			PdbChain mol = null;
			if (molecId==FIRST) {
				mol = interf.getFirstMolecule();
			}
			else if (molecId==SECOND) {
				mol = interf.getSecondMolecule();
			}

			if (interf.isProtein()) {
				 
				List<Double> entropies = null;
				if (cec.hasQueryMatch()) 
					entropies = cec.getConservationScores(ScoringType.ENTROPY);
				for (Residue residue:mol) {

	 				InterfaceResidueItemDB iri = iril.get(i);
					
					int queryUniprotPos = -1;
					if (!mol.isNonPolyChain() && mol.getSequence().isProtein() && cec.hasQueryMatch()) 
						queryUniprotPos = cec.getQueryUniprotPosForPDBPos(residue.getSerial());

					float entropy = -1;
					if (entropies!=null && residue instanceof AaResidue) {	
						if (queryUniprotPos!=-1) entropy = (float) entropies.get(queryUniprotPos).doubleValue();
					}

					iri.setEntropyScore(entropy); 
					i++;
				}
			}
		}
		
		
	}

	public RunParametersItemDB getRunParametersItem() {
		return runParametersItem;
	}
	
}
