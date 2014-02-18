package eppic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import eppic.model.HomologDB;
import eppic.model.ChainClusterDB;
import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceClusterScoreDB;
import eppic.model.InterfaceDB;
import eppic.model.ResidueDB;
import eppic.model.InterfaceScoreDB;
import eppic.model.PdbInfoDB;
import eppic.model.AssemblyDB;
import eppic.model.ScoringMethod;
import eppic.model.UniProtRefWarningDB;
import eppic.model.RunParametersDB;
import eppic.model.InterfaceWarningDB;
import eppic.predictors.CombinedPredictor;
import eppic.predictors.EvolCoreSurfacePredictor;
import eppic.predictors.EvolCoreRimPredictor;
import eppic.predictors.GeometryPredictor;
import owl.core.runners.PymolRunner;
import owl.core.sequence.Homolog;
import owl.core.structure.ChainCluster;
import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.InterfaceCluster;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbBioUnit;
import owl.core.structure.PdbBioUnitList;
import owl.core.structure.PdbChain;
import owl.core.structure.Residue;
import owl.core.structure.SpaceGroup;
import owl.core.util.Goodies;


public class DataModelAdaptor {

	private static final int FIRST = 0;
	private static final int SECOND = 1;
	
	private static final double CONFIDENCE_NOT_AVAILABLE = -1.0;
	private static final double SCORE_NOT_AVAILABLE = -1.0;
	
	private PdbInfoDB pdbInfo;
	
	private EppicParams params;
	
	private RunParametersDB runParameters;
	
	// a temp map to hold the warnings per interface, used in order to eliminate duplicate warnings
	private HashMap<Integer,HashSet<String>> interfId2Warnings;
	
	public DataModelAdaptor() {
		pdbInfo = new PdbInfoDB();
		interfId2Warnings = new HashMap<Integer, HashSet<String>>();
	}
	
	public void setParams(EppicParams params) {
		this.params = params;
		pdbInfo.setPdbCode(params.getPdbCode());
		runParameters = new RunParametersDB();
		runParameters.setMinNumSeqsCutoff(params.getMinNumSeqs());
		runParameters.setHomSoftIdCutoff(params.getHomSoftIdCutoff());
		runParameters.setHomHardIdCutoff(params.getHomHardIdCutoff());
		runParameters.setQueryCovCutoff(params.getQueryCoverageCutoff());
		runParameters.setMaxNumSeqsCutoff(params.getMaxNumSeqs());
		runParameters.setReducedAlphabet(params.getReducedAlphabet());
		runParameters.setCaCutoffForGeom(params.getCAcutoffForGeom());
		runParameters.setCaCutoffForCoreRim(params.getCAcutoffForRimCore());
		runParameters.setCaCutoffForCoreSurface(params.getCAcutoffForZscore());
		runParameters.setCrCallCutoff(params.getCoreRimScoreCutoff());
		runParameters.setCsCallCutoff(params.getCoreSurfScoreCutoff());
		runParameters.setGeomCallCutoff(params.getMinCoreSizeForBio());
		runParameters.setPdbInfo(pdbInfo);
		runParameters.setEppicVersion(EppicParams.PROGRAM_VERSION);
		runParameters.setSearchMode(params.getHomologsSearchMode().getName());
		pdbInfo.setRunParameters(runParameters);
	}
	
	public void setPdbMetadata(PdbAsymUnit pdb) {
		pdbInfo.setTitle(pdb.getTitle());
		pdbInfo.setReleaseDate(pdb.getReleaseDate());
		SpaceGroup sg = pdb.getSpaceGroup();
		pdbInfo.setSpaceGroup(sg==null?null:sg.getShortSymbol());
		pdbInfo.setResolution(pdb.getResolution());
		pdbInfo.setRfreeValue(pdb.getRfree());
		pdbInfo.setExpMethod(pdb.getExpMethod());
		
	}
	
	public void setInterfaces(ChainInterfaceList interfaces, PdbBioUnitList bioUnitList) {

		
		List<InterfaceCluster> interfaceClusters = interfaces.getClusters();
		List<InterfaceClusterDB> icDBs = new ArrayList<InterfaceClusterDB>();
		for (InterfaceCluster ic:interfaceClusters) {
			InterfaceClusterDB icDB = new InterfaceClusterDB();
			icDB.setClusterId(ic.getId());			
			icDB.setPdbCode(pdbInfo.getPdbCode());
			icDB.setPdbInfo(pdbInfo);
			
			List<InterfaceDB> iDBs = new ArrayList<InterfaceDB>();
			
			// setting relations parent/child
			icDBs.add(icDB);
			icDB.setInterfaces(iDBs);
			
			
			for (ChainInterface interf:ic.getMembers()) {
				InterfaceDB interfaceDB = new InterfaceDB();
				interfaceDB.setInterfaceId(interf.getId());
				interfaceDB.setClusterId(interfaces.getCluster(interf.getId()).getId());
				interfaceDB.setArea(interf.getInterfaceArea());
				
				interfaceDB.setChain1(interf.getFirstMolecule().getPdbChainCode());
				interfaceDB.setChain2(interf.getSecondMolecule().getPdbChainCode());
				
				interfaceDB.setOperator(SpaceGroup.getAlgebraicFromMatrix(interf.getSecondTransf().getMatTransform()));
				interfaceDB.setOperatorType(interf.getSecondTransf().getTransformType().getShortName());
				interfaceDB.setIsInfinite(interf.isInfinite());
				
				interfaceDB.setPdbCode(pdbInfo.getPdbCode());
				
				// setting relations parent/child
				iDBs.add(interfaceDB);				
				interfaceDB.setInterfaceCluster(icDB);
				
				interfId2Warnings.put(interf.getId(),new HashSet<String>());
			}
		}
		pdbInfo.setInterfaceClusters(icDBs);
		
		
		// assemblies (biounits) parsed from PDB
		
		// NOTE that getInterfaceClusterMatches removes duplicate assignments (when several biounits refer to same cluster)
		TreeMap<Integer, List<Integer>> matchIds = bioUnitList.getInterfaceClusterMatches(interfaces);
		for(int bioUnitId:matchIds.keySet()){
			PdbBioUnit unit = bioUnitList.get(bioUnitId);
			
			AssemblyDB assembly = new AssemblyDB();			
			assembly.setMethod(unit.getType().getType());
			assembly.setMmSize(unit.getSize());
			assembly.setPdbCode(pdbInfo.getPdbCode());			
			assembly.setConfidence(CONFIDENCE_NOT_AVAILABLE);
			
			// setting relations parent/child
			assembly.setPdbInfo(pdbInfo);
			pdbInfo.addAssembly(assembly);

			List<Integer> memberClusterIds = matchIds.get(bioUnitId);
			
			List<InterfaceClusterDB> memberClustersDB = new ArrayList<InterfaceClusterDB>();
			assembly.setInterfaceClusters(memberClustersDB);
			
			for (int clusterId:memberClusterIds) {
				InterfaceClusterDB icDB = pdbInfo.getInterfaceCluster(clusterId);
				memberClustersDB.add(icDB);				
				
				InterfaceClusterScoreDB icsDB = new InterfaceClusterScoreDB();
				icsDB.setScore(SCORE_NOT_AVAILABLE);
				icsDB.setCallName(CallType.BIO.getName());
				icsDB.setConfidence(CONFIDENCE_NOT_AVAILABLE);
				icsDB.setMethod(unit.getType().getType());				
				icsDB.setClusterId(clusterId);
				icsDB.setPdbCode(pdbInfo.getPdbCode());
				
				// setting relations parent/child
				icsDB.setInterfaceCluster(icDB);
				icDB.addInterfaceClusterScore(icsDB);
				
				icDB.setAssembly(assembly);
			}
		}

	}
	
	public void writeJmolScriptFile(ChainInterface interf, double caCutoff, double minAsaForSurface, PymolRunner pr, File dir, String prefix, boolean usePdbResSer) 
			throws FileNotFoundException {
		 
			File file = new File(dir,prefix+"."+interf.getId()+".jmol");
			PrintStream ps = new PrintStream(file);
			ps.print(createJmolScript(interf, caCutoff, minAsaForSurface, pr, usePdbResSer));
			ps.close();

	}
	
	private String createJmolScript(ChainInterface interf, double caCutoff, double minAsaForSurface, PymolRunner pr, boolean usePdbResSer) {
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
		sb.append("cartoon on; wireframe off; spacefill off; set solvent off;\n");
		sb.append("select :"+chain1+"; color "+color1+";\n");
		sb.append("select :"+chain2+"; color "+color2+";\n");
		interf.calcRimAndCore(caCutoff, minAsaForSurface);
		sb.append(getSelString("core", chain1, interf.getFirstRimCore().getCoreResidues(), usePdbResSer)+";\n");
		sb.append(getSelString("core", chain2, interf.getSecondRimCore().getCoreResidues(), usePdbResSer)+";\n");
		sb.append(getSelString("rim", chain1, interf.getFirstRimCore().getRimResidues(), usePdbResSer)+";\n");
		sb.append(getSelString("rim", chain2, interf.getSecondRimCore().getRimResidues(), usePdbResSer)+";\n");
		sb.append("define interface"+chain1+" core"+chain1+" or rim"+chain1+";\n");
		sb.append("define interface"+chain2+" core"+chain2+" or rim"+chain2+";\n");
		sb.append("define bothinterf interface"+chain1+" or interface"+chain2+";\n");
		// surfaces are cool but in jmol they don't display as good as in pymol, especially the transparency effect is quite bad
		//sb.append("select :"+chain1+"; isosurface surf"+chain1+" solvent;color isosurface gray;color isosurface translucent;\n");
		//sb.append("select :"+chain2+"; isosurface surf"+chain2+" solvent;color isosurface gray;color isosurface translucent;\n");
		sb.append("select interface"+chain1+";wireframe 0.3;\n");
		sb.append("select interface"+chain2+";wireframe 0.3;\n");
		sb.append("select core"+chain1+";"+"color "+colorInterf1+";wireframe 0.3;\n");
		sb.append("select core"+chain2+";"+"color "+colorInterf2+";wireframe 0.3;\n");
		if (interf.hasCofactors()) {
			sb.append("select ligand;wireframe 0.3;\n");
		}
		return sb.toString();
	}
	
	private String getResiSelString(List<Residue> list, char chainName, boolean usePdbResSer) {
		// residue 0 or negatives can exist (e.g. 1epr). In order to have an empty selection we 
		// simply use a very low negative numbe which is unlikely to exist in PDB
		if (list.isEmpty()) return "-10000:"+chainName;
		StringBuffer sb = new StringBuffer();
		for (int i=0;i<list.size();i++) {
			if (usePdbResSer) {
				// jmol uses a special syntax for residue serials with insertion 
				// codes, e.g. 23A from chain A would be "23^A:A" and not "23A:A"
				// A PDB with this problem is 1yg9, it would show a blank jmol screen before this fix
				String pdbSerial = list.get(i).getPdbSerial();
				char lastChar = pdbSerial.charAt(pdbSerial.length()-1);
				if (!Character.isDigit(lastChar)) {
					pdbSerial = pdbSerial.replace(Character.toString(lastChar), "^"+lastChar);
				}
				sb.append(pdbSerial+":"+chainName);
			} else {
				sb.append(list.get(i).getSerial()+":"+chainName);
			}
			if (i!=list.size()-1) sb.append(",");
		}
		return sb.toString();
	}

	private String getSelString(String namePrefix, char chainName, List<Residue> list, boolean usePdbResSer) {
		return "define "+namePrefix+chainName+" "+getResiSelString(list,chainName, usePdbResSer);
	}
	
	public void setGeometryScores(List<GeometryPredictor> gps) {
		for (int i=0;i<gps.size();i++) {
			InterfaceDB ii = pdbInfo.getInterface(i+1);
			InterfaceScoreDB is = new InterfaceScoreDB();
			ii.addInterfaceScore(is);
			is.setInterfaceItem(ii);
			is.setInterfaceId(gps.get(i).getInterface().getId());
			CallType call = gps.get(i).getCall();
			is.setCallName(call.getName());
			is.setCallReason(gps.get(i).getCallReason());
			is.setMethod(ScoringMethod.EPPIC_GEOMETRY);
			is.setPdbCode(ii.getPdbCode());
			is.setConfidence(CONFIDENCE_NOT_AVAILABLE);
			is.setScore(gps.get(i).getScore());
			// TODO score1 and score2 not available since GeometryPredictor doesn't have both sides, what should we do?
			
			if(gps.get(i).getWarnings() != null) {
				
				List<String> warnings = gps.get(i).getWarnings();
				for(String warning: warnings) {
					
					// we first add warning to the temp HashSets in order to eliminate duplicates, 
					// in the end we fill the InterfaceItemDBs by calling addInterfaceWarnings
					interfId2Warnings.get(ii.getInterfaceId()).add(warning);
				}
			}

		}
	}
	
	public void add(InterfaceEvolContextList iecl) {
		
		List<ChainClusterDB> chainClusterDBs = new ArrayList<ChainClusterDB>();
		
		ChainEvolContextList cecl = iecl.getChainEvolContextList();
		for (ChainEvolContext cec:cecl.getAllChainEvolContext()) {
			ChainClusterDB chainClusterDB = new ChainClusterDB();
			ChainCluster cc = cecl.getPdb().getProtChainCluster(cec.getRepresentativeChainCode());
			chainClusterDB.setRepChain(cc.getRepresentative().getPdbChainCode());
			chainClusterDB.setMemberChains(cc.getCommaSepMemberPdbChainCodes());
			chainClusterDB.setHasUniProtRef(cec.hasQueryMatch());
			
			List<UniProtRefWarningDB> queryWarningItemDBs = new ArrayList<UniProtRefWarningDB>();
			for(String queryWarning : cec.getQueryWarnings())
			{
				UniProtRefWarningDB queryWarningItemDB = new UniProtRefWarningDB();
				queryWarningItemDB.setChainCluster(chainClusterDB);
				queryWarningItemDB.setText(queryWarning);
				queryWarningItemDBs.add(queryWarningItemDB);
			}
			
			chainClusterDB.setUniProtRefWarnings(queryWarningItemDBs);
			
			if (cec.hasQueryMatch()) { //all other fields remain null otherwise
				
				chainClusterDB.setNumHomologs(cec.getNumHomologs());
				chainClusterDB.setRefUniProtId(cec.getQuery().getUniId()); 
				chainClusterDB.setFirstTaxon(cec.getQuery().getFirstTaxon());
				chainClusterDB.setLastTaxon(cec.getQuery().getLastTaxon());
				
				chainClusterDB.setMsaAlignedSeq(cec.getAlignment().getAlignedSequence(cec.getQuery().getUniId()));
				 
				chainClusterDB.setRefUniProtStart(cec.getQueryInterval().beg);
				chainClusterDB.setRefUniProtEnd(cec.getQueryInterval().end);
				
				chainClusterDB.setPdbAlignedSeq(cec.getPdb2uniprotAln().getAlignedSequences()[0]);
				chainClusterDB.setAliMarkupLine(String.valueOf(cec.getPdb2uniprotAln().getMarkupLine()));
				chainClusterDB.setRefAlignedSeq(cec.getPdb2uniprotAln().getAlignedSequences()[1]);
				chainClusterDB.setSeqIdCutoff(cec.getIdCutoff());
				chainClusterDB.setClusteringSeqId(cec.getUsedClusteringPercentId()/100.0);
				chainClusterDB.setPdbCode(pdbInfo.getPdbCode());
				
				List<HomologDB> homologDBs = new ArrayList<HomologDB>();
				for (Homolog hom:cec.getHomologs().getFilteredSubset()) {
					HomologDB homologDB = new HomologDB();
					homologDB.setUniProtId(hom.getUniId());
					homologDB.setQueryStart(hom.getBlastHsp().getQueryStart());
					homologDB.setQueryEnd(hom.getBlastHsp().getQueryEnd());
					homologDB.setAlignedSeq(cec.getHomologs().getAlignment().getAlignedSequence(hom.getUniId()));
					if (hom.getUnirefEntry().hasTaxons()) {
						homologDB.setFirstTaxon(hom.getUnirefEntry().getFirstTaxon());
						homologDB.setLastTaxon(hom.getUnirefEntry().getLastTaxon());
					}
					homologDB.setSeqId(hom.getPercentIdentity()/100.0);
					homologDB.setQueryCoverage(hom.getQueryCoverage());					
					
					homologDBs.add(homologDB);
					
					homologDB.setChainCluster(chainClusterDB);
					
				}
				
				chainClusterDB.setHomologs(homologDBs);
			} 

			chainClusterDB.setPdbInfo(pdbInfo);
			chainClusterDBs.add(chainClusterDB);	
		}
		
		pdbInfo.setChainClusters(chainClusterDBs);
		

		for (int i=0;i<iecl.size();i++) {
			
			InterfaceEvolContext iec = iecl.get(i);
			InterfaceDB ii = pdbInfo.getInterface(i+1);
			
			// 1) we add entropy values to the residue details
			addEntropyToResidueDetails(ii.getResidues(), iec);
			
			
			// 2) core-surface scores
			EvolCoreSurfacePredictor ezp = iecl.getEvolInterfZPredictor(i);
			InterfaceScoreDB isCS = new InterfaceScoreDB();
			ii.addInterfaceScore(isCS);
			isCS.setInterfaceItem(ii);
			isCS.setInterfaceId(iec.getInterface().getId());
			isCS.setMethod(ScoringMethod.EPPIC_CORESURFACE);

			CallType call = ezp.getCall();	
			isCS.setCallName(call.getName());
			isCS.setCallReason(ezp.getCallReason());
			
			if(ezp.getWarnings() != null) {
				List<String> warnings = ezp.getWarnings();
				for(String warning: warnings) {
					// we first add warning to the temp HashSets in order to eliminate duplicates, 
					// in the end we fill the InterfaceItemDBs by calling addInterfaceWarnings
					interfId2Warnings.get(ii.getInterfaceId()).add(warning);
				}
			}

			isCS.setScore1(ezp.getMember1Predictor().getScore());
			isCS.setScore2(ezp.getMember2Predictor().getScore());
			isCS.setScore(ezp.getScore());	
			
			isCS.setConfidence(CONFIDENCE_NOT_AVAILABLE);
			isCS.setPdbCode(ii.getPdbCode());
			
			// 3) core-rim scores
			EvolCoreRimPredictor ercp = iecl.getEvolRimCorePredictor(i);

			InterfaceScoreDB isCR = new InterfaceScoreDB();
			isCR.setInterfaceItem(ii);
			ii.addInterfaceScore(isCR);
			isCR.setInterfaceId(iec.getInterface().getId());
			isCR.setMethod(ScoringMethod.EPPIC_CORERIM);

			call = ercp.getCall();	
			isCR.setCallName(call.getName());
			isCR.setCallReason(ercp.getCallReason());
			
			if(ercp.getWarnings() != null) {
				List<String> warnings = ercp.getWarnings();
				for(String warning: warnings) {
					// we first add warning to the temp HashSets in order to eliminate duplicates, 
					// in the end we fill the InterfaceItemDBs by calling addInterfaceWarnings
					interfId2Warnings.get(ii.getInterfaceId()).add(warning);
				}
			}

			isCR.setScore1(ercp.getMember1Predictor().getScore());
			isCR.setScore2(ercp.getMember2Predictor().getScore());
			isCR.setScore(ercp.getScore());				

			isCR.setConfidence(CONFIDENCE_NOT_AVAILABLE);
			isCR.setPdbCode(ii.getPdbCode());

			
			
		}

		// 4) interface cluster scores
		for (InterfaceClusterDB ic:pdbInfo.getInterfaceClusters()) {

			// TODO the cluster score is empty right now: we need to fill it!
			InterfaceClusterScoreDB ics = new InterfaceClusterScoreDB();
			ics.setMethod(ScoringMethod.EPPIC_FINAL);
			ics.setCallName(CallType.NO_PREDICTION.getName());
			ics.setScore(SCORE_NOT_AVAILABLE);
			ics.setConfidence(CONFIDENCE_NOT_AVAILABLE);
			ics.setPdbCode(pdbInfo.getPdbCode());
			ics.setClusterId(ic.getClusterId());

			// setting relations child/parent
			ics.setInterfaceCluster(ic); 
			ic.addInterfaceClusterScore(ics);
		}
		

	}
	
	public void setCombinedPredictors(List<CombinedPredictor> cps) {
		for (int i=0;i<cps.size();i++) {
			InterfaceDB ii = pdbInfo.getInterface(i+1);
			InterfaceScoreDB is = new InterfaceScoreDB();
			ii.addInterfaceScore(is);
			is.setMethod(ScoringMethod.EPPIC_FINAL);
			is.setCallName(cps.get(i).getCall().getName());
			is.setCallReason(cps.get(i).getCallReason());
			is.setConfidence(CONFIDENCE_NOT_AVAILABLE);
			is.setInterfaceItem(ii);
			is.setInterfaceId(ii.getInterfaceId());
			is.setPdbCode(ii.getPdbCode());			
			
			if(cps.get(i).getWarnings() != null)
			{
				List<String> warnings = cps.get(i).getWarnings();
				for(String warning: warnings)
				{
					// we first add warning to the temp HashSets in order to eliminate duplicates, 
					// in the end we fill the InterfaceItemDBs by calling addInterfaceWarnings
					interfId2Warnings.get(ii.getInterfaceId()).add(warning);
				}
			}
		}
	}
	
	public void writeSerializedModelFile(File file) throws EppicException {
		try {
			Goodies.serialize(file,pdbInfo);
		} catch (IOException e) {
			throw new EppicException(e, e.getMessage(), true);
		}
	}
	
	public void addResidueDetails(ChainInterfaceList interfaces) {
		for (ChainInterface interf:interfaces) {
			
			InterfaceDB ii = pdbInfo.getInterface(interf.getId());

			// we add the residue details
			addResidueDetails(ii, interf, params.isDoScoreEntropies());
		}
	}
	
	private void addResidueDetails(InterfaceDB ii, ChainInterface interf, boolean includeEntropy) {
		
		List<ResidueDB> iril = new ArrayList<ResidueDB>();
		ii.setResidues(iril);

		addResidueDetailsOfPartner(iril, interf, 0);
		addResidueDetailsOfPartner(iril, interf, 1);

		for(ResidueDB iri : iril) {
			iri.setInterfaceItem(ii);
		}
	}
	
	private void addResidueDetailsOfPartner(List<ResidueDB> iril, ChainInterface interf, int molecId) {
		if (interf.isProtein()) {
			PdbChain mol = null;
			if (molecId==FIRST) {
				mol = interf.getFirstMolecule();
			}
			else if (molecId==SECOND) {
				mol = interf.getSecondMolecule();
			}
			
			for (Residue residue:mol) {
				String resType = residue.getLongCode();
				int assignment = ResidueDB.OTHER;
				
				float asa = (float) residue.getAsa();
				float bsa = (float) residue.getBsa();
				
				if (residue.getAsa()>params.getMinAsaForSurface() && residue.getBsa()>0) {
					// NOTE: we use here caCutoffForRimCore as the one and only for both evol methods
					// NOTE2: we are assuming that caCutoffForRimCore<caCutoffForGeom, if that doesn't hold this won't work!
					if (residue.getBsaToAsaRatio()<params.getCAcutoffForRimCore()) {
						assignment = ResidueDB.RIM;
					} else if (residue.getBsaToAsaRatio()<params.getCAcutoffForGeom()){
						assignment = ResidueDB.CORE_EVOLUTIONARY; 
					} else {
						assignment = ResidueDB.CORE_GEOMETRY;
					}
				} else if (residue.getAsa()>params.getMinAsaForSurface()) {
					assignment = ResidueDB.SURFACE;
				}
				
				ResidueDB iri = new ResidueDB();
				iri.setResidueNumber(residue.getSerial());
				iri.setPdbResidueNumber(residue.getPdbSerial());
				iri.setResidueType(resType);
				iri.setAsa(asa);
				iri.setBsa(bsa);
				iri.setRegion(assignment);
				iri.setEntropyScore(-1.0);
				iri.setSide(molecId+1); // structure ids are 1 and 2 while molecId are 0 and 1

				iri.setInterfaceItem(pdbInfo.getInterface(interf.getId()));
				iril.add(iri);
			}
		}
	}

	private void addEntropyToResidueDetails(List<ResidueDB> iril, InterfaceEvolContext iec) {
		ChainInterface interf = iec.getInterface();
		
		
		int[] molecIds = new int[2];
		molecIds[0] = 0;
		molecIds[1] = 1;

		// beware the counter is global for both molecule 1 and 2 (as the List<ResidueDB> contains both, identified by a structure id 1 or 2)
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

	 				ResidueDB iri = iril.get(i);
					
					int queryUniprotPos = -1;
					if (!mol.isNonPolyChain() && mol.getSequence().isProtein() && cec.hasQueryMatch()) 
						queryUniprotPos = cec.getQueryUniprotPosForPDBPos(residue.getSerial());

					float entropy = -1;
					// we used to have here: "&& residue instanceof AaResidue" but that was preventing entropy values of mismatch-to-ref-uniprot-residues to be passed
					// for het residues we do have entropy values too as the entropy values are calculated on the reference uniprot sequence (where no het residues are present)
					if (entropies!=null) {	
						if (queryUniprotPos!=-1) entropy = (float) entropies.get(queryUniprotPos).doubleValue();
					}

					iri.setEntropyScore(entropy); 
					i++;
				}
			}
		}
		
		
	}

	public RunParametersDB getRunParametersItem() {
		return runParameters;
	}
	
	/**
	 * Add to the pdbInfo member the cached warnings interfId2Warnings, compiled in
	 * {@link #setGeometryScores(List)}, {@link #setCombinedPredictors(List)} and {@link #add(InterfaceEvolContextList)} 
	 */
	public void addInterfaceWarnings() {
		
		for (InterfaceClusterDB ic:pdbInfo.getInterfaceClusters()) {
			for (InterfaceDB ii:ic.getInterfaces()) {
				for (String warning : interfId2Warnings.get(ii.getInterfaceId())) {
					InterfaceWarningDB warningItem = new InterfaceWarningDB();
					warningItem.setText(warning);
					warningItem.setInterfaceItem(ii);
					ii.getInterfaceWarnings().add(warningItem);
				}
			}
		}
	}
	
}
