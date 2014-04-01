package eppic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import edu.uci.ics.jung.graph.util.Pair;
import eppic.model.ContactDB;
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
import eppic.predictors.CombinedClusterPredictor;
import eppic.predictors.CombinedPredictor;
import eppic.predictors.EvolCoreRimClusterPredictor;
import eppic.predictors.EvolCoreSurfaceClusterPredictor;
import eppic.predictors.EvolCoreSurfacePredictor;
import eppic.predictors.EvolCoreRimPredictor;
import eppic.predictors.GeometryClusterPredictor;
import eppic.predictors.GeometryPredictor;
import owl.core.sequence.Homolog;
import owl.core.structure.ChainCluster;
import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.CrystalCell;
import owl.core.structure.InterfaceCluster;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbBioUnit;
import owl.core.structure.PdbBioUnitList;
import owl.core.structure.PdbChain;
import owl.core.structure.Residue;
import owl.core.structure.SpaceGroup;
import owl.core.structure.graphs.RICGEdge;
import owl.core.structure.graphs.RICGNode;
import owl.core.structure.graphs.RICGraph;
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
	
	public PdbInfoDB getPdbInfo() {
		return pdbInfo;
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
		
		CrystalCell cc = pdb.getCrystalCell();
		if (cc!=null) {
			pdbInfo.setCellA(cc.getA());
			pdbInfo.setCellB(cc.getB());
			pdbInfo.setCellC(cc.getC());
			pdbInfo.setCellAlpha(cc.getAlpha());
			pdbInfo.setCellBeta(cc.getBeta());
			pdbInfo.setCellGamma(cc.getGamma());			
		}
	}
	
	public void setInterfaces(ChainInterfaceList interfaces, PdbBioUnitList bioUnitList) {

		
		List<InterfaceCluster> interfaceClusters = interfaces.getClusters();
		List<InterfaceClusterDB> icDBs = new ArrayList<InterfaceClusterDB>();
		for (InterfaceCluster ic:interfaceClusters) {
			InterfaceClusterDB icDB = new InterfaceClusterDB();
			icDB.setClusterId(ic.getId());			
			icDB.setPdbCode(pdbInfo.getPdbCode());
			icDB.setAvgArea(ic.getMeanArea());
			icDB.setNumMembers(ic.getMembers().size());
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
				
				
				// the contacts table
				List<ContactDB> contacts = new ArrayList<ContactDB>();
				
				interfaceDB.setContacts(contacts);
				
				RICGraph graph = new RICGraph(interf.getAICGraph());
				
				for (RICGEdge edge:graph.getEdges()) {
					
					Pair<RICGNode> pair = graph.getEndpoints(edge);
					
					ContactDB contact = new ContactDB();
					contact.setFirstResNumber(pair.getFirst().getResidueSerial());
					contact.setSecondResNumber(pair.getSecond().getResidueSerial());
					contact.setFirstResType(pair.getFirst().getResidueType());
					contact.setSecondResType(pair.getSecond().getResidueType());
					Residue iRes = interf.getFirstMolecule().getResidue(pair.getFirst().getResidueSerial());
					Residue jRes = interf.getSecondMolecule().getResidue(pair.getSecond().getResidueSerial());
					contact.setFirstBurial(iRes.getBsaToAsaRatio());
					contact.setSecondBurial(jRes.getBsaToAsaRatio());
					
					contact.setMinDistance(edge.getMinDistance());
					contact.setClash(edge.isClash());
					contact.setDisulfide(edge.isDisulfide());
					contact.setNumAtoms(edge.getnAtoms());
					contact.setNumHBonds(edge.getnHBonds()); 
					
					contact.setInterfaceId(interf.getId()); 
					contact.setPdbCode(pdbInfo.getPdbCode());
					
					contacts.add(contact);
					
					// parent/child
					contact.setInterfaceItem(interfaceDB);
					
				}
				
				// sorting so that at least in text files we'll get a nice sorting
				Collections.sort(contacts, new Comparator<ContactDB>() {

					@Override
					public int compare(ContactDB first, ContactDB second) {
						int iFirst = first.getFirstResNumber();
						int jFirst = first.getSecondResNumber();
						int iSecond = second.getFirstResNumber();
						int jSecond = second.getSecondResNumber();
						
						if (iFirst>iSecond) return 1;
						if (iFirst<iSecond) return -1;
						
						if (jFirst>jSecond) return 1;
						if (jFirst<jSecond) return -1;
						
						return 0;
					}
					
				}); 
				
				
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
				icsDB.setScore1(SCORE_NOT_AVAILABLE);
				icsDB.setScore2(SCORE_NOT_AVAILABLE);
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
	
	public void setGeometryScores(List<GeometryPredictor> gps, List<GeometryClusterPredictor> gcps) {
		
		// geometry scores per interface
		for (int i=0;i<gps.size();i++) {
			InterfaceDB ii = pdbInfo.getInterface(i+1);
			InterfaceScoreDB is = new InterfaceScoreDB();
			ii.addInterfaceScore(is);
			is.setInterfaceItem(ii);
			is.setInterfaceId(ii.getInterfaceId());
			CallType call = gps.get(i).getCall();
			is.setCallName(call.getName());
			is.setCallReason(gps.get(i).getCallReason());
			is.setMethod(ScoringMethod.EPPIC_GEOMETRY);
			is.setPdbCode(ii.getPdbCode());
			is.setConfidence(CONFIDENCE_NOT_AVAILABLE);
			is.setScore(gps.get(i).getScore());
			is.setScore1(gps.get(i).getScore1());
			is.setScore2(gps.get(i).getScore2());
			
			if(gps.get(i).getWarnings() != null) {
				
				List<String> warnings = gps.get(i).getWarnings();
				for(String warning: warnings) {
					
					// we first add warning to the temp HashSets in order to eliminate duplicates, 
					// in the end we fill the InterfaceItemDBs by calling addInterfaceWarnings
					interfId2Warnings.get(ii.getInterfaceId()).add(warning);
				}
			}

		}

		// geometry scores per interface cluster
		for (int i=0;i<gcps.size();i++) {

			InterfaceClusterDB ic = pdbInfo.getInterfaceClusters().get(i);
			GeometryClusterPredictor gcp = gcps.get(i);
			
			// method eppic-gm
			InterfaceClusterScoreDB ics = new InterfaceClusterScoreDB();
			ics.setMethod(ScoringMethod.EPPIC_GEOMETRY);
			ics.setCallName(gcp.getCall().getName());
			ics.setCallReason(gcp.getCallReason());
			ics.setScore(gcp.getScore());
			ics.setScore1(gcp.getScore1());
			ics.setScore2(gcp.getScore2());
			ics.setConfidence(CONFIDENCE_NOT_AVAILABLE);
			ics.setPdbCode(pdbInfo.getPdbCode());
			ics.setClusterId(ic.getClusterId());

			// setting relations child/parent
			ics.setInterfaceCluster(ic); 
			ic.addInterfaceClusterScore(ics);
		}

	}

	public void setEvolScores(InterfaceEvolContextList iecl) {
		
		List<ChainClusterDB> chainClusterDBs = new ArrayList<ChainClusterDB>();
		
		ChainEvolContextList cecl = iecl.getChainEvolContextList();
		for (ChainEvolContext cec:cecl.getAllChainEvolContext()) {
			ChainClusterDB chainClusterDB = new ChainClusterDB();
			ChainCluster cc = cecl.getPdb().getProtChainCluster(cec.getRepresentativeChainCode());
			chainClusterDB.setRepChain(cc.getRepresentative().getPdbChainCode());
			chainClusterDB.setMemberChains(cc.getCommaSepMemberPdbChainCodes());
			chainClusterDB.setHasUniProtRef(cec.hasQueryMatch());
			
			List<UniProtRefWarningDB> queryWarningItemDBs = new ArrayList<UniProtRefWarningDB>();
			for(String queryWarning : cec.getQueryWarnings()) {
				
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
				
				chainClusterDB.setPdbStart(cec.getPDBPosForQueryUniprotPos(cec.getQueryInterval().beg-1));
				chainClusterDB.setPdbEnd(cec.getPDBPosForQueryUniprotPos(cec.getQueryInterval().end-1));
				
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
					homologDB.setSubjectStart(hom.getBlastHsp().getSubjectStart());
					homologDB.setSubjectEnd(hom.getBlastHsp().getSubjectEnd());
					homologDB.setAlignedSeq(cec.getAlignment().getAlignedSequence(hom.getIdentifier()));
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
			EvolCoreSurfacePredictor ecsp = iec.getEvolCoreSurfacePredictor();
			InterfaceScoreDB isCS = new InterfaceScoreDB();
			ii.addInterfaceScore(isCS);
			isCS.setInterfaceItem(ii);
			isCS.setInterfaceId(iec.getInterface().getId());
			isCS.setMethod(ScoringMethod.EPPIC_CORESURFACE);

			CallType call = ecsp.getCall();	
			isCS.setCallName(call.getName());
			isCS.setCallReason(ecsp.getCallReason());
			
			if(ecsp.getWarnings() != null) {
				List<String> warnings = ecsp.getWarnings();
				for(String warning: warnings) {
					// we first add warning to the temp HashSets in order to eliminate duplicates, 
					// in the end we fill the InterfaceItemDBs by calling addInterfaceWarnings
					interfId2Warnings.get(ii.getInterfaceId()).add(warning);
				}
			}

			isCS.setScore1(ecsp.getScore1());
			isCS.setScore2(ecsp.getScore2());
			isCS.setScore(ecsp.getScore());	
			
			isCS.setConfidence(CONFIDENCE_NOT_AVAILABLE);
			isCS.setPdbCode(ii.getPdbCode());
			
			// 3) core-rim scores
			EvolCoreRimPredictor ecrp = iec.getEvolCoreRimPredictor();

			InterfaceScoreDB isCR = new InterfaceScoreDB();
			isCR.setInterfaceItem(ii);
			ii.addInterfaceScore(isCR);
			isCR.setInterfaceId(iec.getInterface().getId());
			isCR.setMethod(ScoringMethod.EPPIC_CORERIM);

			call = ecrp.getCall();	
			isCR.setCallName(call.getName());
			isCR.setCallReason(ecrp.getCallReason());
			
			if(ecrp.getWarnings() != null) {
				List<String> warnings = ecrp.getWarnings();
				for(String warning: warnings) {
					// we first add warning to the temp HashSets in order to eliminate duplicates, 
					// in the end we fill the InterfaceItemDBs by calling addInterfaceWarnings
					interfId2Warnings.get(ii.getInterfaceId()).add(warning);
				}
			}

			isCR.setScore1(ecrp.getScore1());
			isCR.setScore2(ecrp.getScore2());
			isCR.setScore(ecrp.getScore());				

			isCR.setConfidence(CONFIDENCE_NOT_AVAILABLE);
			isCR.setPdbCode(ii.getPdbCode());

			
			
		}

		// 4) interface cluster scores
		for (InterfaceClusterDB ic:pdbInfo.getInterfaceClusters()) {

			// method eppic-cr
			EvolCoreRimClusterPredictor ecrcp = iecl.getEvolCoreRimClusterPredictor(ic.getClusterId());
			InterfaceClusterScoreDB ics = new InterfaceClusterScoreDB();
			ics.setMethod(ScoringMethod.EPPIC_CORERIM);
			ics.setCallName(ecrcp.getCall().getName());
			ics.setCallReason(ecrcp.getCallReason());
			ics.setScore(ecrcp.getScore());
			ics.setScore1(ecrcp.getScore1());
			ics.setScore2(ecrcp.getScore2());
			ics.setConfidence(CONFIDENCE_NOT_AVAILABLE);
			ics.setPdbCode(pdbInfo.getPdbCode());
			ics.setClusterId(ic.getClusterId());

			// setting relations child/parent
			ics.setInterfaceCluster(ic); 
			ic.addInterfaceClusterScore(ics);
			
			// method eppic-cs
			EvolCoreSurfaceClusterPredictor ecscp = iecl.getEvolCoreSurfaceClusterPredictor(ic.getClusterId());
			ics = new InterfaceClusterScoreDB();
			ics.setMethod(ScoringMethod.EPPIC_CORESURFACE);			
			ics.setCallName(ecscp.getCall().getName());
			ics.setCallReason(ecscp.getCallReason());
			ics.setScore(ecscp.getScore());
			ics.setScore1(ecscp.getScore1());
			ics.setScore2(ecscp.getScore2());
			ics.setConfidence(CONFIDENCE_NOT_AVAILABLE);
			ics.setPdbCode(pdbInfo.getPdbCode());
			ics.setClusterId(ic.getClusterId());

			// setting relations child/parent
			ics.setInterfaceCluster(ic); 
			ic.addInterfaceClusterScore(ics);
		}
		

	}
	
	public void setCombinedPredictors(List<CombinedPredictor> cps, List<CombinedClusterPredictor> ccps) {

		// per interface combined scores
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
			is.setScore(cps.get(i).getScore());
			is.setScore1(SCORE_NOT_AVAILABLE);
			is.setScore2(SCORE_NOT_AVAILABLE);
			
			if(cps.get(i).getWarnings() != null) {
				
				List<String> warnings = cps.get(i).getWarnings();
				for(String warning: warnings) {
					
					// we first add warning to the temp HashSets in order to eliminate duplicates, 
					// in the end we fill the InterfaceItemDBs by calling addInterfaceWarnings
					interfId2Warnings.get(ii.getInterfaceId()).add(warning);
				}
			}
		}
		
		// per cluster combined scores
		for (int i=0;i<ccps.size();i++) {
			
			InterfaceClusterDB ic = pdbInfo.getInterfaceClusters().get(i);
			
			InterfaceClusterScoreDB ics = new InterfaceClusterScoreDB();
			
			ics.setMethod(ScoringMethod.EPPIC_FINAL);
			ics.setCallName(ccps.get(i).getCall().getName());
			ics.setCallReason(ccps.get(i).getCallReason());
			ics.setScore(ccps.get(i).getScore());
			ics.setScore1(SCORE_NOT_AVAILABLE);
			ics.setScore2(SCORE_NOT_AVAILABLE);
			ics.setConfidence(CONFIDENCE_NOT_AVAILABLE);
			ics.setPdbCode(pdbInfo.getPdbCode());
			ics.setClusterId(ic.getClusterId());

			// setting relations child/parent
			ics.setInterfaceCluster(ic); 
			ic.addInterfaceClusterScore(ics);
		}
	}
	
	public void writeSerializedModelFile(File file) throws EppicException {
		try {
			Goodies.serialize(file,pdbInfo);
		} catch (IOException e) {
			throw new EppicException(e, e.getMessage(), true);
		}
	}
	
	public void setResidueDetails(ChainInterfaceList interfaces) {
		for (ChainInterface interf:interfaces) {
			
			InterfaceDB ii = pdbInfo.getInterface(interf.getId());

			// we add the residue details
			addResidueDetails(ii, interf, params.isDoEvolScoring());
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
				
				double asa = residue.getAsa();
				double bsa = residue.getBsa();
				
				// NOTE the regions are mutually exclusive (one and only one assignment per region)

				// For the case of CORE_EVOL/CORE_GEOM we are assuming that CORE_EVOL is a superset of CORE_GEOM 
				// (i.e. that caCutoffForRimCore<caCutoffForGeom)
				// thus, as groups are exclusive, to get the actual full subset of CORE_EVOL one needs to get
				// the union of CORE_EVOL and CORE_GEOM
				
				// this should match the condition in owl.core.structure.PdbChain.getRimAndCore()
				if (residue.getAsa()>params.getMinAsaForSurface() && residue.getBsa()>0) {
					// NOTE: we use here caCutoffForRimCore as the one and only for both evol methods
					// NOTE2: we are assuming that caCutoffForRimCore<caCutoffForGeom, if that doesn't hold this won't work!
					if (residue.getBsaToAsaRatio()<params.getCAcutoffForRimCore()) {
						assignment = ResidueDB.RIM_EVOLUTIONARY;
					} else if (residue.getBsaToAsaRatio()<params.getCAcutoffForGeom()){
						assignment = ResidueDB.CORE_EVOLUTIONARY; 
					} else {
						assignment = ResidueDB.CORE_GEOMETRY;
					} 
					
				// residues not in interface but still with more ASA than minimum required are called surface
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
					entropies = cec.getConservationScores();
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
	
	public void setUniProtVersion(String uniProtVersion) {
		this.runParameters.setUniProtVersion(uniProtVersion);
	}
	
	/**
	 * Add to the pdbInfo member the cached warnings interfId2Warnings, compiled in
	 * {@link #setGeometryScores(List)}, {@link #setCombinedPredictors(List)} and {@link #setEvolScores(InterfaceEvolContextList)} 
	 */
	public void setInterfaceWarnings() {
		
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
