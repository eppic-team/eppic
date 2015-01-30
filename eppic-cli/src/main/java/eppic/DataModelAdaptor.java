package eppic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Compound;
import org.biojava.nbio.structure.ExperimentalTechnique;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.PDBCrystallographicInfo;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.asa.GroupAsa;
import org.biojava.nbio.structure.contact.AtomContact;
import org.biojava.nbio.structure.contact.GroupContact;
import org.biojava.nbio.structure.contact.GroupContactSet;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.quaternary.BioAssemblyInfo;
import org.biojava.nbio.structure.xtal.CrystalCell;
import org.biojava.nbio.structure.xtal.SpaceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.analysis.compare.InterfaceMatcher;
import eppic.analysis.compare.SimpleInterface;
import eppic.commons.sequence.Homolog;
import eppic.commons.util.Goodies;
import eppic.model.AssemblyDB;
import eppic.model.ChainClusterDB;
import eppic.model.ContactDB;
import eppic.model.HomologDB;
import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceClusterScoreDB;
import eppic.model.InterfaceDB;
import eppic.model.InterfaceScoreDB;
import eppic.model.InterfaceWarningDB;
import eppic.model.PdbInfoDB;
import eppic.model.ResidueDB;
import eppic.model.RunParametersDB;
import eppic.model.ScoringMethod;
import eppic.model.UniProtRefWarningDB;
import eppic.predictors.CombinedClusterPredictor;
import eppic.predictors.CombinedPredictor;
import eppic.predictors.EvolCoreRimClusterPredictor;
import eppic.predictors.EvolCoreRimPredictor;
import eppic.predictors.EvolCoreSurfaceClusterPredictor;
import eppic.predictors.EvolCoreSurfacePredictor;
import eppic.predictors.GeometryClusterPredictor;
import eppic.predictors.GeometryPredictor;


public class DataModelAdaptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataModelAdaptor.class);
	
	private static final double CONFIDENCE_NOT_AVAILABLE = -1.0;
	private static final double SCORE_NOT_AVAILABLE = -1.0;
	
	private static final double NO_BURIAL_AVAILABLE = -1.0;
	
	public static final String PDB_BIOUNIT_METHOD = "pdb1";
	
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
	
	public void setPdbMetadata(Structure pdb) {
		pdbInfo.setTitle(pdb.getPDBHeader().getTitle());
		// TODO here we used to have the release date but it doesn't seem to be in biojava, do we want it?
		pdbInfo.setReleaseDate(pdb.getPDBHeader().getDepDate());
		PDBCrystallographicInfo pdbXtallographicInfo = pdb.getCrystallographicInfo();
		SpaceGroup sg = (pdbXtallographicInfo==null?null:pdbXtallographicInfo.getSpaceGroup());
		pdbInfo.setSpaceGroup(sg==null?null:sg.getShortSymbol());
		pdbInfo.setResolution(pdb.getPDBHeader().getResolution());
		pdbInfo.setRfreeValue(pdb.getPDBHeader().getRfree());
		Set<ExperimentalTechnique> expTecs = pdb.getPDBHeader().getExperimentalTechniques();
		// if there's nothing in header we 
		String exp = null;
		if (expTecs!=null) {
			exp = expTecs.iterator().next().getName();			
		} else {
			if (pdb.isCrystallographic()) {
				exp = ExperimentalTechnique.XRAY_DIFFRACTION.getName();
			} else {
				exp = "UNKNOWN";
			}
		}
		pdbInfo.setExpMethod(exp);
		
		pdbInfo.setNcsOpsPresent(pdb.getCrystallographicInfo().getNcsOperators()!=null);
		
		CrystalCell cc = (pdbXtallographicInfo==null?null:pdbXtallographicInfo.getCrystalCell());
		if (cc!=null) {
			pdbInfo.setCellA(cc.getA());
			pdbInfo.setCellB(cc.getB());
			pdbInfo.setCellC(cc.getC());
			pdbInfo.setCellAlpha(cc.getAlpha());
			pdbInfo.setCellBeta(cc.getBeta());
			pdbInfo.setCellGamma(cc.getGamma());			
		}
	}
	
	public void setInterfaces(StructureInterfaceList interfaces) {

		
		List<StructureInterfaceCluster> interfaceClusters = interfaces.getClusters();
		List<InterfaceClusterDB> icDBs = new ArrayList<InterfaceClusterDB>();
		for (StructureInterfaceCluster ic:interfaceClusters) {
			InterfaceClusterDB icDB = new InterfaceClusterDB();
			icDB.setClusterId(ic.getId());			
			icDB.setPdbCode(pdbInfo.getPdbCode());
			icDB.setAvgArea(ic.getTotalArea());
			icDB.setNumMembers(ic.getMembers().size());
			icDB.setPdbInfo(pdbInfo);
			
			List<InterfaceDB> iDBs = new ArrayList<InterfaceDB>();
			
			// setting relations parent/child
			icDBs.add(icDB);
			icDB.setInterfaces(iDBs);
			
			for (StructureInterface interf:ic.getMembers()) {
				//System.out.println("Interface " + interf.getId());
				
				InterfaceDB interfaceDB = new InterfaceDB();
				interfaceDB.setInterfaceId(interf.getId());
				interfaceDB.setClusterId(interf.getCluster().getId());
				interfaceDB.setArea(interf.getTotalArea());
				
				interfaceDB.setChain1(interf.getMoleculeIds().getFirst());
				interfaceDB.setChain2(interf.getMoleculeIds().getSecond());
				
				interfaceDB.setOperator(SpaceGroup.getAlgebraicFromMatrix(interf.getTransforms().getSecond().getMatTransform()));
				interfaceDB.setOperatorType(interf.getTransforms().getSecond().getTransformType().getShortName());
				interfaceDB.setInfinite(interf.isInfinite());
				interfaceDB.setOperatorId(interf.getTransforms().getSecond().getTransformId());
				interfaceDB.setXtalTrans_x(interf.getTransforms().getSecond().getCrystalTranslation().x);
				interfaceDB.setXtalTrans_y(interf.getTransforms().getSecond().getCrystalTranslation().y);
				interfaceDB.setXtalTrans_z(interf.getTransforms().getSecond().getCrystalTranslation().z);
				interfaceDB.setIsologous(interf.isIsologous());
				
				interfaceDB.setProt1(InterfaceEvolContext.isProtein(interf, InterfaceEvolContext.FIRST));
				interfaceDB.setProt2(InterfaceEvolContext.isProtein(interf, InterfaceEvolContext.SECOND));
				
				interfaceDB.setPdbCode(pdbInfo.getPdbCode());
				
				// setting relations parent/child
				iDBs.add(interfaceDB);				
				interfaceDB.setInterfaceCluster(icDB);
				
				interfId2Warnings.put(interf.getId(),new HashSet<String>());
				
				
				// the contacts table
				List<ContactDB> contacts = new ArrayList<ContactDB>();
				
				interfaceDB.setContacts(contacts);
							
				GroupContactSet groupContacts = new GroupContactSet(interf.getContacts());
				
				for (GroupContact groupContact:groupContacts) {
										
					ContactDB contact = new ContactDB();
					Group firstGroup = groupContact.getPair().getFirst();
					Group secondGroup = groupContact.getPair().getSecond();
					contact.setFirstResNumber(getSeqresSerial(firstGroup, firstGroup.getChain()) );
					contact.setSecondResNumber(getSeqresSerial(secondGroup, secondGroup.getChain()) );
					contact.setFirstResType(firstGroup.getPDBName());
					contact.setSecondResType(secondGroup.getPDBName());
					GroupAsa firstGroupAsa = interf.getFirstGroupAsa(firstGroup.getResidueNumber());
					GroupAsa secondGroupAsa = interf.getSecondGroupAsa(secondGroup.getResidueNumber());
					if (firstGroupAsa!=null) contact.setFirstBurial(firstGroupAsa.getBsaToAsaRatio());
					else contact.setFirstBurial(NO_BURIAL_AVAILABLE);
					if (secondGroupAsa!=null) contact.setSecondBurial(secondGroupAsa.getBsaToAsaRatio());
					else contact.setSecondBurial(NO_BURIAL_AVAILABLE);
					
					contact.setMinDistance(groupContact.getMinDistance());					
					contact.setClash(groupContact.getContactsWithinDistance(EppicParams.CLASH_DISTANCE).size()>0);
					contact.setDisulfide(isDisulfideInteraction(groupContact));
					contact.setNumAtoms(groupContact.getNumAtomContacts());
					// TODO transfer h-bonds stuff to Biojava and fill this field
					//contact.setNumHBonds(edge.getnHBonds()); 
					
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
		
	}
	
	public void setPdbBioUnits(BioAssemblyInfo bioAssembly, CrystalCell cell) {

		if (bioAssembly == null) {
			LOGGER.info("No bio assembly annotation present, will not add bio assembly info to data model");
			return;
		}
		
		// since the move to Biojava, we have decided to take the first PDB-annotated biounit ONLY whatever its type

		Set<Integer> matchingClusterIds = matchToInterfaceClusters(bioAssembly, cell);		
		

		AssemblyDB assembly = new AssemblyDB();			
		assembly.setMethod(PDB_BIOUNIT_METHOD);
		assembly.setMmSize(bioAssembly.getMacromolecularSize());
		assembly.setPdbCode(pdbInfo.getPdbCode());			
		assembly.setConfidence(CONFIDENCE_NOT_AVAILABLE);

		// setting relations parent/child
		assembly.setPdbInfo(pdbInfo);
		pdbInfo.addAssembly(assembly);

		List<InterfaceClusterScoreDB> memberClusterScoresDB = new ArrayList<InterfaceClusterScoreDB>();
		assembly.setInterfaceClusterScores(memberClusterScoresDB);
		for (InterfaceClusterDB icDB:pdbInfo.getInterfaceClusters()) {

			if (matchingClusterIds.contains(icDB.getClusterId())) {
				// all member interface clusters are assigned bio

				InterfaceClusterScoreDB icsDB = new InterfaceClusterScoreDB();
				memberClusterScoresDB.add(icsDB);				

				icsDB.setScore(SCORE_NOT_AVAILABLE);
				icsDB.setScore1(SCORE_NOT_AVAILABLE);
				icsDB.setScore2(SCORE_NOT_AVAILABLE);
				icsDB.setCallName(CallType.BIO.getName());
				icsDB.setConfidence(CONFIDENCE_NOT_AVAILABLE);
				icsDB.setMethod(PDB_BIOUNIT_METHOD);				
				icsDB.setClusterId(icDB.getClusterId());
				icsDB.setPdbCode(pdbInfo.getPdbCode());

				// setting relations parent/child
				icsDB.setInterfaceCluster(icDB);
				icDB.addInterfaceClusterScore(icsDB);

				// only the bio interfaces are part of the assembly
				icsDB.setAssembly(assembly);

			} else {
				// The rest (not members) are assigned xtal
				// We need to do this otherwise there's no distinction between 
				// missing annotations and real xtal annotations
				InterfaceClusterScoreDB icsDB = new InterfaceClusterScoreDB();
				icsDB.setScore(SCORE_NOT_AVAILABLE);
				icsDB.setScore1(SCORE_NOT_AVAILABLE);
				icsDB.setScore2(SCORE_NOT_AVAILABLE);
				icsDB.setCallName(CallType.CRYSTAL.getName());
				icsDB.setConfidence(CONFIDENCE_NOT_AVAILABLE);
				icsDB.setMethod(PDB_BIOUNIT_METHOD);				
				icsDB.setClusterId(icDB.getClusterId());
				icsDB.setPdbCode(pdbInfo.getPdbCode());

				// setting relations parent/child
				icsDB.setInterfaceCluster(icDB);
				icDB.addInterfaceClusterScore(icsDB);

			}
		}



	}
	
	/**
	 * For the given PDB bio unit (first in PDB annotation), map the PDB-annotated interfaces 
	 * to our interface cluster ids
	 * @param bioUnit
	 * @return the list of matching cluster ids
	 */
	private Set<Integer> matchToInterfaceClusters(BioAssemblyInfo bioUnit, CrystalCell cell) {

		// the Set will eliminate duplicates if any found, I'm not sure if duplicates are even possible really...
		Set<Integer> matchingClusterIds = new TreeSet<Integer>();

		List<SimpleInterface> bioUnitInterfaces = SimpleInterface.createSimpleInterfaceListFromPdbBioUnit(bioUnit, cell);
		InterfaceMatcher im = new InterfaceMatcher(pdbInfo.getInterfaceClusters(),bioUnitInterfaces);
		for (InterfaceClusterDB ic:pdbInfo.getInterfaceClusters()) {
			for (InterfaceDB i:ic.getInterfaces()) {
				if (im.oursMatch(i.getInterfaceId())) {
					matchingClusterIds.add(ic.getClusterId()); 							 
				} 
			}
		}

		if (!im.checkTheirsMatch()) {
			String msg = "";
			for (SimpleInterface theirI:im.getTheirsNotMatching()) {
				msg += theirI.toString()+"\t";
			}

			// This actually happens even if the mapping is fine. That's because we enumerate the biounit 
			// interfaces exhaustively, and thus sometimes an interface might not happen in reality because 
			// 2 molecules don't make a contact. 
			LOGGER.info("Some interfaces of PDB bio unit "+EppicParams.PDB_BIOUNIT_TO_USE+
					" do not match any of the EPPIC interfaces."+ 
					" Non-matching interfaces are: "+msg);

		}

		if (!im.checkOneToOneMapping()) {
			// This is not really a mapping problem, that's why it is only logged INFO
			// It will happen in many bona-fide proper mappings:
			// e.g. 2a7n or 1ae9 (the bio interfaces are in 4-fold or 3-fold xtal axes and thus 
			//      the operators given in bio-unit are repeated, for instance for 3-fold the operator 
			//      appears twice to construct the 2 symmetry partners, while in eppic it appears only once)
			LOGGER.info("Multiple match for an interface of PDB bio unit "+EppicParams.PDB_BIOUNIT_TO_USE);
		}

		
		
		return matchingClusterIds;
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
			is.setConfidence(gps.get(i).getConfidence());
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
			ics.setConfidence(gcp.getConfidence());
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
		pdbInfo.setNumChainClusters(cecl.size());
		
		for (ChainEvolContext cec:cecl.getAllChainEvolContext()) {
			ChainClusterDB chainClusterDB = new ChainClusterDB();
			Compound cc = null;
			for (Compound compound:cecl.getPdb().getCompounds()) {
				// in mmCIF files some sugars are annotated as compounds with no chains linked to them, e.g. 3s26
				if (compound.getChains().isEmpty()) continue;

				if (compound.getRepresentative().getChainID().equals(cec.getRepresentativeChainCode())) {
					cc = compound;
				}
			}
			chainClusterDB.setRepChain(cc.getRepresentative().getChainID());
			chainClusterDB.setMemberChains(getMemberChainsString(cc));
			chainClusterDB.setNumMembers(getUniqueChainIds(cc).size());
			chainClusterDB.setProtein(cec.isProtein());
			chainClusterDB.setHasUniProtRef(cec.hasQueryMatch());
			
			List<UniProtRefWarningDB> queryWarningItemDBs = new ArrayList<UniProtRefWarningDB>();
			for(String queryWarning : cec.getQueryWarnings()) {
				
				UniProtRefWarningDB queryWarningItemDB = new UniProtRefWarningDB();
				queryWarningItemDB.setChainCluster(chainClusterDB);
				queryWarningItemDB.setText(queryWarning);
				queryWarningItemDBs.add(queryWarningItemDB);
			}
			
			chainClusterDB.setUniProtRefWarnings(queryWarningItemDBs);
			chainClusterDB.setPdbCode(pdbInfo.getPdbCode());
			
			if (cec.hasQueryMatch()) { //all other fields remain null otherwise
				
				chainClusterDB.setNumHomologs(cec.getNumHomologs());
				chainClusterDB.setRefUniProtId(cec.getQuery().getUniId()); 
				chainClusterDB.setFirstTaxon(cec.getQuery().getFirstTaxon());
				chainClusterDB.setLastTaxon(cec.getQuery().getLastTaxon());
				
				chainClusterDB.setMsaAlignedSeq(cec.getAlignment().getAlignedSequence(cec.getQuery().getUniId()));
				 
				chainClusterDB.setRefUniProtStart(cec.getQueryInterval().beg);
				chainClusterDB.setRefUniProtEnd(cec.getQueryInterval().end);
				
				chainClusterDB.setPdbStart(cec.getPDBPosForQueryUniprotPos(cec.getQueryInterval().beg));
				chainClusterDB.setPdbEnd(cec.getPDBPosForQueryUniprotPos(cec.getQueryInterval().end));
				
				chainClusterDB.setPdbAlignedSeq(cec.getPdb2uniprotAln().getAlignedSequence(1).getSequenceAsString());
				chainClusterDB.setRefAlignedSeq(cec.getPdb2uniprotAln().getAlignedSequence(2).getSequenceAsString());
				chainClusterDB.setSeqIdCutoff(cec.getIdCutoff());
				chainClusterDB.setClusteringSeqId(cec.getUsedClusteringPercentId()/100.0);
				
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
			
			isCS.setConfidence(ecsp.getConfidence());
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

			isCR.setConfidence(ecrp.getConfidence());
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
			ics.setConfidence(ecrcp.getConfidence());
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
			ics.setConfidence(ecscp.getConfidence());
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
			is.setConfidence(cps.get(i).getConfidence());
			is.setInterfaceItem(ii);
			is.setInterfaceId(ii.getInterfaceId());
			is.setPdbCode(ii.getPdbCode());
			is.setScore(cps.get(i).getScore());
			is.setScore1(cps.get(i).getScore1());
			is.setScore2(cps.get(i).getScore2());
			
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
			ics.setScore1(ccps.get(i).getScore1());
			ics.setScore2(ccps.get(i).getScore2());
			ics.setConfidence(ccps.get(i).getConfidence());
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
	
	public void setResidueDetails(StructureInterfaceList interfaces) {
		for (StructureInterface interf:interfaces) {
			
			InterfaceDB ii = pdbInfo.getInterface(interf.getId());

			// we add the residue details
			addResidueDetails(ii, interf, params.isDoEvolScoring());
		}
	}
	
	private void addResidueDetails(InterfaceDB ii, StructureInterface interf, boolean includeEntropy) {
		
		List<ResidueDB> iril = new ArrayList<ResidueDB>();
		ii.setResidues(iril);

		addResidueDetailsOfPartner(iril, interf, InterfaceEvolContext.FIRST);
		addResidueDetailsOfPartner(iril, interf, InterfaceEvolContext.SECOND);

		for(ResidueDB iri : iril) {
			iri.setInterfaceItem(ii);
		}
	}
	
	private void addResidueDetailsOfPartner(List<ResidueDB> iril, StructureInterface interf, int molecId) {

		Chain chain = null;
		if (molecId == InterfaceEvolContext.FIRST) chain =  interf.getMolecules().getFirst()[0].getGroup().getChain();
		else if (molecId == InterfaceEvolContext.SECOND) chain = interf.getMolecules().getSecond()[0].getGroup().getChain();
		
		for (Group group:chain.getAtomGroups()) {

			if (group.isWater()) continue;

			GroupAsa groupAsa = null;
			if (molecId==InterfaceEvolContext.FIRST) groupAsa = interf.getFirstGroupAsa(group.getResidueNumber());
			else if (molecId==InterfaceEvolContext.SECOND) groupAsa = interf.getSecondGroupAsa(group.getResidueNumber());

			// if we have no groupAsa that means that this is a Residue for which we don't calculate ASA (most likely HETATM)
			if (groupAsa==null) continue;

			String resType = group.getPDBName();
			int assignment = ResidueDB.OTHER;

			double asa = groupAsa.getAsaU();
			double bsa = groupAsa.getBsa();

			// NOTE the regions are mutually exclusive (one and only one assignment per region)

			// For the case of CORE_EVOL/CORE_GEOM we are assuming that CORE_EVOL is a superset of CORE_GEOM 
			// (i.e. that caCutoffForRimCore<caCutoffForGeom)
			// thus, as groups are exclusive, to get the actual full subset of CORE_EVOL one needs to get
			// the union of CORE_EVOL and CORE_GEOM

			// this should match the condition in owl.core.structure.PdbChain.getRimAndCore()
			if (groupAsa.getAsaU()>params.getMinAsaForSurface() && groupAsa.getBsa()>0) {
				// NOTE: we use here caCutoffForRimCore as the one and only for both evol methods
				// NOTE2: we are assuming that caCutoffForRimCore<caCutoffForGeom, if that doesn't hold this won't work!
				if (groupAsa.getBsaToAsaRatio()<params.getCAcutoffForRimCore()) {
					assignment = ResidueDB.RIM_EVOLUTIONARY;
				} else if (groupAsa.getBsaToAsaRatio()<params.getCAcutoffForGeom()){
					assignment = ResidueDB.CORE_EVOLUTIONARY; 
				} else {
					assignment = ResidueDB.CORE_GEOMETRY;
				} 

				// residues not in interface but still with more ASA than minimum required are called surface
			} else if (groupAsa.getAsaU()>params.getMinAsaForSurface()) {
				assignment = ResidueDB.SURFACE;
			}


			ResidueDB iri = new ResidueDB();
			iri.setResidueNumber(getSeqresSerial(group, chain));
			iri.setPdbResidueNumber(group.getResidueNumber().toString());
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

	private void addEntropyToResidueDetails(List<ResidueDB> iril, InterfaceEvolContext iec) {
		StructureInterface interf = iec.getInterface();
		
		
		int[] molecIds = new int[2];
		molecIds[0] = InterfaceEvolContext.FIRST;
		molecIds[1] = InterfaceEvolContext.SECOND;

		// beware the counter is global for both molecule 1 and 2 (as the List<ResidueDB> contains both, identified by a structure id 1 or 2)
		int i = 0;  

		for (int molecId:molecIds) { 
			ChainEvolContext cec = iec.getChainEvolContext(molecId);
			Chain mol = null;
			if (molecId==InterfaceEvolContext.FIRST) {
				mol = interf.getMolecules().getFirst()[0].getGroup().getChain();
			}
			else if (molecId==InterfaceEvolContext.SECOND) {
				mol = interf.getMolecules().getSecond()[0].getGroup().getChain();
			}

			if ( cec!=null && cec.isProtein() ) {
				 
				List<Double> entropies = null;
				if (cec.hasQueryMatch()) 
					entropies = cec.getConservationScores();
				for (Group residue:mol.getAtomGroups()) {
					
					// skipping of residues: we follow exact same procedure as in addResidueDetailsOfPartner(), otherwise indices won't match
					if (residue.isWater()) continue;
					GroupAsa groupAsa = null;
					if (molecId==InterfaceEvolContext.FIRST) groupAsa = interf.getFirstGroupAsa(residue.getResidueNumber());
					else if (molecId==InterfaceEvolContext.SECOND) groupAsa = interf.getSecondGroupAsa(residue.getResidueNumber());
					
					if (groupAsa==null) continue;

	 				ResidueDB iri = iril.get(i);
					
					int queryUniprotPos = -1;
					// TODO before Biojava move we used to have here !mol.isNonPolyChain() as first condition, how do we do that now?
					if (cec.hasQueryMatch()) {
						int resser = cec.getSeqresSerial(residue);
						if (resser!=-1) 
							queryUniprotPos = cec.getQueryUniprotPosForPDBPos(resser);
					}

					float entropy = -1;
					// we used to have here: "&& residue instanceof AaResidue" but that was preventing entropy values of mismatch-to-ref-uniprot-residues to be passed
					// for het residues we do have entropy values too as the entropy values are calculated on the reference uniprot sequence (where no het residues are present)
					if (entropies!=null) {	
						if (queryUniprotPos!=-1) entropy = (float) entropies.get(queryUniprotPos-1).doubleValue();
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
	
	public static String getChainClusterString(Compound compound) {

		StringBuilder sb = new StringBuilder();

		sb.append(compound.getRepresentative().getChainID());
		
		Set<String> uniqChainIds = getUniqueChainIds(compound);

		if (uniqChainIds.size()>1) {

			sb.append(" (");
			for (String chainId:uniqChainIds) {
				if (chainId.equals(compound.getRepresentative().getChainID())) {
					continue;
				}

				sb.append(chainId+",");

			}

			sb.deleteCharAt(sb.length()-1);
			sb.append(")");
		}

		return sb.toString();
	}

	public static String getMemberChainsString(Compound compound) {
		Set<String> uniqChainIds = getUniqueChainIds(compound);
		
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String chainId:uniqChainIds) {
			sb.append(chainId);
			if (i!=uniqChainIds.size()-1) sb.append(",");
			i++;
		}
		return sb.toString();
	}
	
	private static Set<String> getUniqueChainIds(Compound compound) {
		Set<String> uniqChainIds = new TreeSet<String>();
		for (int i=0;i<compound.getChains().size();i++) {
			uniqChainIds.add(compound.getChains().get(i).getChainID());
		}
		return uniqChainIds;
	}
	
	/**
	 * Returns the SEQRES serial of the Group g in the Chain c
	 * @param g
	 * @param c
	 * @return the SEQRES serial (1 to n) or -1 if not found
	 */
	public static int getSeqresSerial(Group g, Chain c) {
		
		// this is not very efficient, it would be nice to store the mapping in a cached map somewhere 
		List<Group> seqresGroups = c.getSeqResGroups();
		for (int i=0;i<seqresGroups.size();i++) {
			if (seqresGroups.get(i) == g) 
				return i+1;
		}
		// not found, return -1
		return -1;
	}
	
	public static boolean isDisulfideInteraction(AtomContact contact) {
		Atom atomi = contact.getPair().getFirst();
		Atom atomj = contact.getPair().getSecond();
		if (atomi.getGroup().getPDBName().equals("CYS") &&
			atomj.getGroup().getPDBName().equals("CYS") &&
			atomi.getName().equals("SG") &&
			atomj.getName().equals("SG") &&
			contact.getDistance()<(EppicParams.DISULFIDE_BRIDGE_DIST+EppicParams.DISULFIDE_BRIDGE_DIST_SIGMA) && 
			contact.getDistance()>(EppicParams.DISULFIDE_BRIDGE_DIST-EppicParams.DISULFIDE_BRIDGE_DIST_SIGMA)) {
				return true;
		}
		return false;
	}
	
	public static boolean isDisulfideInteraction(GroupContact groupContact) {
		for (AtomContact contact:groupContact.getAtomContacts()) {
			if (isDisulfideInteraction(contact)) return true;
		}
		return false;
	}
}
