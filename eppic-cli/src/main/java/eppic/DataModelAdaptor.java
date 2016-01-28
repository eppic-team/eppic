package eppic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.biojava.nbio.structure.StructureTools;
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
import eppic.assembly.Assembly;
import eppic.assembly.AssemblyDescription;
import eppic.assembly.CrystalAssemblies;
import eppic.commons.sequence.Homolog;
import eppic.commons.util.Goodies;
import eppic.model.AssemblyContentDB;
import eppic.model.AssemblyDB;
import eppic.model.AssemblyScoreDB;
import eppic.model.ChainClusterDB;
import eppic.model.ContactDB;
import eppic.model.HomologDB;
import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceClusterScoreDB;
import eppic.model.InterfaceDB;
import eppic.model.InterfaceScoreDB;
import eppic.model.InterfaceWarningDB;
import eppic.model.PdbInfoDB;
import eppic.model.ResidueBurialDB;
import eppic.model.ResidueInfoDB;
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
	
	private static final int UNKNOWN_RESIDUE_INDEX = -1;
	
	public static final String PDB_BIOUNIT_METHOD = "pdb1";
	
	public static final int INVALID_ASSEMBLY_ID = 0;
	
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
		runParameters.setAlphabet(params.getAlphabet().toString());
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
		
		List<ChainClusterDB> chainClusterDBs = new ArrayList<ChainClusterDB>();

		for (Compound compound:pdb.getCompounds()) {
			
			// in mmCIF files some sugars are annotated as compounds with no chains linked to them, e.g. 3s26
			if (compound.getChains().isEmpty()) continue;

			chainClusterDBs.add(createChainCluster(compound));
		}
		pdbInfo.setNumChainClusters(chainClusterDBs.size());
		pdbInfo.setChainClusters(chainClusterDBs);
	}
	
	private ChainClusterDB createChainCluster(Compound compound) {
		ChainClusterDB chainClusterDB = new ChainClusterDB();
		
		chainClusterDB.setPdbCode(pdbInfo.getPdbCode());
		
		chainClusterDB.setRepChain(compound.getRepresentative().getChainID());
		chainClusterDB.setMemberChains(getMemberChainsString(compound));
		chainClusterDB.setNumMembers(compound.getChainIds().size());
		chainClusterDB.setProtein(StructureTools.isProtein(compound.getRepresentative()));
		
		chainClusterDB.setPdbInfo(pdbInfo);
		
		List<ResidueInfoDB> residueInfoDBs = new ArrayList<ResidueInfoDB>();
		
		chainClusterDB.setResidueInfos(residueInfoDBs);
		
		List<Group> groups = getGroups(compound); 
		
		for (int i=0;i<groups.size();i++) {
			
			Group g = groups.get(i);
			
			ResidueInfoDB residueInfoDB = new ResidueInfoDB();
			
			residueInfoDBs.add(residueInfoDB);
			
			residueInfoDB.setChainCluster(chainClusterDB);			
			
			residueInfoDB.setPdbCode(pdbInfo.getPdbCode());
			residueInfoDB.setRepChain(compound.getRepresentative().getChainID());
			
			// NOTE, here there can be 2 behaviours:
			// 1) there is a SEQRES and getAlignedResIndex gives the actual SEQRES indices
			// 2) there is no SEQRES and getAlignedResIndex gives the PDB residue number without the insertion codes:
			//    thus it can be wrong or misaligned.
			// Another possibility for 2) is that we used the UniProt reference alignment to give proper 
			// indices (based on the UniProt alignment). Of course they wouldn't coincide necessarily
			// with what the authors had in mind for the SEQRES construct. 
			// However we can't do that here, because at this time we don't have the alignment yet, 
			// only later we can do it when we add the evol details
			//
			// E.g. in 1n7y, chain A:
			//                                             1       10        20        30
			// SEQRES  ------------------------------------AEAGITGTWYEQLGSTFIVTAGADGALTGTYESAVGNAESRYVL
			//                                                1       10        20
			// ATOM    ---------------------------------------GITGTWYEQLGSTFIVTAGADGALTGTY-------ESRYVL
			//                                                |||||||.||||||||||||||||||||       ||||||
			// UNIPROT MRKIVVAAIAVSLTTVSITASASADPSKDSKAQVSAAEAGITGTWYNQLGSTFIVTAGADGALTGTYESAVGNAESRYVL
			//         1       10        20        30        40        50        60        70
			// For the different cases, the 3 first observed residues would get:
			// 1)  4 G,  5 I,  6 T
			// 2) 16 G, 17 I, 18 T
			// And with UniProt numbering:
			//    40 G, 41 I, 42 T

			// TODO here there are unresolved problems with no-SEQRES files (e.g. 3ddo with no SEQRES), 
			//      see also the todo in addResidueBurialDetailsOfPartner()
			
			residueInfoDB.setResidueNumber(compound.getAlignedResIndex(g, g.getChain()));
			
			// for seqres residues the pdb res number is unavailable in biojava
			residueInfoDB.setPdbResidueNumber( (g.getResidueNumber()==null?null:g.getResidueNumber().toString()) );
			
			residueInfoDB.setResidueType(g.getPDBName());
			
		}
		
		
		return chainClusterDB;
	}
	
	private List<Group> getGroups(Compound compound) {
		
		List<Group> groups = new ArrayList<Group>();
		
		List<Group> gs = compound.getRepresentative().getSeqResGroups();
		
		if (gs==null || gs.isEmpty()) {
			gs = compound.getRepresentative().getAtomGroups();
		}
		
		for (Group g : gs) {
			
			if (g.isWater()) continue;
			
			groups.add(g);
		}	
		
		return groups;
	}
	
	public void setInterfaces(StructureInterfaceList interfaces) {

		
		List<StructureInterfaceCluster> interfaceClusters = interfaces.getClusters(EppicParams.CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF);
		List<InterfaceClusterDB> icDBs = new ArrayList<InterfaceClusterDB>();
		for (StructureInterfaceCluster ic:interfaceClusters) {
			InterfaceClusterDB icDB = new InterfaceClusterDB();
			icDB.setClusterId(ic.getId());			
			icDB.setPdbCode(pdbInfo.getPdbCode());
			icDB.setAvgArea(ic.getTotalArea());
			icDB.setAvgContactOverlapScore(ic.getAverageScore());
			icDB.setNumMembers(ic.getMembers().size());
			icDB.setPdbInfo(pdbInfo);
			
			List<InterfaceDB> iDBs = new ArrayList<InterfaceDB>();
			
			// setting relations parent/child
			icDBs.add(icDB);
			icDB.setInterfaces(iDBs);
			
			boolean isClusterInfinite = false;
			int isologousCount = 0;
			
			for (StructureInterface interf:ic.getMembers()) {
				
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
				
				// infinite/isologous for cluster
				if (interfaceDB.isInfinite()) isClusterInfinite = true; // a single infinite sets the whole cluster as infinite
				if (interfaceDB.isIsologous()) isologousCount++;
				
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
					if (firstGroup.getChain().getCompound()==null)
						contact.setFirstResNumber(UNKNOWN_RESIDUE_INDEX);
					else 
						contact.setFirstResNumber(firstGroup.getChain().getCompound().getAlignedResIndex(firstGroup, firstGroup.getChain()) );
					if (secondGroup.getChain().getCompound()==null)
						contact.setSecondResNumber(UNKNOWN_RESIDUE_INDEX);
					else
						contact.setSecondResNumber(secondGroup.getChain().getCompound().getAlignedResIndex(secondGroup, secondGroup.getChain()) );
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
			
			// setting isologous and infinite for cluster
			icDB.setInfinite(isClusterInfinite);

			// at least half of them isologous and we set the whole cluster
			if (isologousCount >= ((double)icDB.getNumMembers()/2.0)) {
				icDB.setIsologous(true);
			} else {
				icDB.setIsologous(false);
			}
			
		}
		
		
		pdbInfo.setInterfaceClusters(icDBs);
		
	}
	
	public void setAssemblies(CrystalAssemblies validAssemblies) {
		
		for (Assembly validAssembly:validAssemblies) {
			AssemblyDB assembly = new AssemblyDB();
			
			assembly.setId(validAssembly.getId());
			
			// all assemblies that we pass are topologically valid, only externally calculated assemblies can be invalid (PDB, PISA)
			// and would need to be added explicitly when adding external assembly predictions
			assembly.setTopologicallyValid(true);
			
			// relations
			assembly.setPdbInfo(pdbInfo);
			pdbInfo.addAssembly(assembly);
			
			Set<InterfaceClusterDB> interfaceClusters = new HashSet<InterfaceClusterDB>();
			for (StructureInterfaceCluster ic:validAssembly.getEngagedInterfaceClusters()) {
				InterfaceClusterDB icDB = pdbInfo.getInterfaceCluster(ic.getId());
				interfaceClusters.add(icDB);
				icDB.addAssembly(assembly);
			}
			assembly.setInterfaceClusters(interfaceClusters);
						
			// other data
			assembly.setPdbCode(pdbInfo.getPdbCode());			
			
			assembly.setInterfaceClusterIds(validAssembly.toString());
			
			List<AssemblyDescription> description = validAssembly.getDescription();
			List<AssemblyContentDB> acDBs = new ArrayList<AssemblyContentDB>();
			for (AssemblyDescription d:description) {
				AssemblyContentDB acDB = new AssemblyContentDB();
				acDB.setPdbCode(pdbInfo.getPdbCode());
				acDB.setAssembly(assembly);
				acDB.setMmSize(d.getSize());
				acDB.setSymmetry(d.getSymmetry());
				acDB.setStoichiometry(d.getStoichiometry());
				acDB.setComposition(d.getComposition());
				acDB.setChainIds(d.getChainIds()); 
				acDBs.add(acDB);
			}
			assembly.setAssemblyContents(acDBs);			
						
			AssemblyScoreDB as = new AssemblyScoreDB();
			as.setMethod(ScoringMethod.EPPIC_FINAL);
			if (validAssembly.getCall()==null) 
				LOGGER.warn("Call is null for assembly {}", validAssembly.getId());
			else 
				as.setCallName(validAssembly.getCall().getName());			
			as.setCallReason(""); // what do we put in here?
			as.setScore(SCORE_NOT_AVAILABLE);
			as.setConfidence(CONFIDENCE_NOT_AVAILABLE);
			as.setPdbCode(pdbInfo.getPdbCode());
			as.setAssembly(assembly);
			assembly.addAssemblyScore(as);
			
			
		}
	}
	
	public void setPdbBioUnits(BioAssemblyInfo bioAssembly, String[] symmetries,
			CrystalAssemblies validAssemblies) {

		if (bioAssembly == null) {
			LOGGER.info("No bio assembly annotation present, will not add bio assembly info to data model");
			return;
		}
		
		CrystalCell cell = null;
		if (validAssemblies.getStructure().getCrystallographicInfo()!=null && validAssemblies.getStructure().isCrystallographic()) {
			cell = validAssemblies.getStructure().getCrystallographicInfo().getCrystalCell();
		}

		
		// since the move to Biojava, we have decided to take the first PDB-annotated biounit ONLY whatever its type

		Set<Integer> matchingClusterIds = matchToInterfaceClusters(bioAssembly, cell);	
		int[] matchingClusterIdsArray = new int[matchingClusterIds.size()];
		Iterator<Integer> it = matchingClusterIds.iterator();
		for (int i=0;i<matchingClusterIds.size();i++) matchingClusterIdsArray[i] = it.next(); 
		
		Assembly pdbAssembly = validAssemblies.generateAssembly(matchingClusterIdsArray);
		
		Assembly matchingAssembly = getMatchingAssembly(pdbAssembly, validAssemblies);
		AssemblyDB matchingAssemblyDB = null;
		if (matchingAssembly!=null) {
			for (AssemblyDB a:pdbInfo.getAssemblies()) {
				if (a.getId() == matchingAssembly.getId()) {
					matchingAssemblyDB = a;
				}
			}
		}
		
		AssemblyScoreDB as = new AssemblyScoreDB();
		as.setMethod(PDB_BIOUNIT_METHOD);
		as.setCallName(CallType.BIO.getName());
		as.setCallReason(""); // empty for the moment, perhaps we could use it for authors/pisa
		as.setScore(SCORE_NOT_AVAILABLE);
		as.setConfidence(CONFIDENCE_NOT_AVAILABLE);
		as.setPdbCode(pdbInfo.getPdbCode());
		
		if (matchingAssemblyDB!=null) {

			as.setAssembly(matchingAssemblyDB);
			
			if (!getSymmetryString(matchingAssemblyDB.getAssemblyContents()).equals(symmetries[0])) {
				LOGGER.warn("Symmetry calculated from graph is {} whilst detected from biounit is {}",
						getSymmetryString(matchingAssemblyDB.getAssemblyContents()),symmetries[0]);
			}
			
			if (!getStoichiometryString(matchingAssemblyDB.getAssemblyContents()).equals(symmetries[1])) {
				LOGGER.warn("Stoichiometry calculated from graph is {} whilst detected from biounit is {}",
						getStoichiometryString(matchingAssemblyDB.getAssemblyContents()),symmetries[1]);
			}
			matchingAssemblyDB.addAssemblyScore(as);
			
		} else {
			LOGGER.warn("PDB given assembly {} does not match any of the topologically valid assemblies.",
					pdbAssembly.toString());

			// the assembly is not one of our valid assemblies, we'll have to insert an invalid assembly to the list
			
			
			AssemblyDB assembly = new AssemblyDB();
			
			assembly.setId(INVALID_ASSEMBLY_ID);
			
			assembly.setTopologicallyValid(false);
			
			// relations
			assembly.setPdbInfo(pdbInfo);
			pdbInfo.addAssembly(assembly);
						
			Set<InterfaceClusterDB> interfaceClustersDB = new HashSet<InterfaceClusterDB>();
			for (int interfClusterId:matchingClusterIds) {
				InterfaceClusterDB icDB = pdbInfo.getInterfaceCluster(interfClusterId);
				interfaceClustersDB.add(icDB);
				icDB.addAssembly(assembly);
			}
			assembly.setInterfaceClusters(interfaceClustersDB);
			
			// other data
			assembly.setPdbCode(pdbInfo.getPdbCode());			

			assembly.setInterfaceClusterIds(pdbAssembly.toString());
						
			// TODO fill the AssemblyContents
			//assembly.setMmSize(invalidAssembly.getSize());
			//assembly.setComposition(composition);			
			//assembly.setSymmetry(sym);
			//assembly.setStoichiometry(stoic);
			
			// this is how to get the sym info from biojava qs detected ones
			//assembly.setMmSize(bioAssembly.getMacromolecularSize());
			//assembly.setSymmetry(symmetries[0]);
			//assembly.setStoichiometry(symmetries[1]);
			//assembly.setPseudoSymmetry(symmetries[2]);
			//assembly.setPseudoStoichiometry(symmetries[3]);
			//assembly.setPdbCode(pdbInfo.getPdbCode());			

			
			
			assembly.addAssemblyScore(as);
			
			as.setAssembly(assembly);
		}

		
		// fill all the other assemblies with XTAL AssemblyScores
		
		for (AssemblyDB assembly: pdbInfo.getAssemblies()) {
			if (matchingAssemblyDB!=null && assembly == matchingAssemblyDB) continue;
			
			AssemblyScoreDB asxtal = new AssemblyScoreDB();
			asxtal.setMethod(PDB_BIOUNIT_METHOD);
			asxtal.setCallName(CallType.CRYSTAL.getName());
			asxtal.setCallReason(""); // empty for the moment, perhaps we could use it for authors/pisa
			asxtal.setScore(SCORE_NOT_AVAILABLE);
			asxtal.setConfidence(CONFIDENCE_NOT_AVAILABLE);
			asxtal.setPdbCode(pdbInfo.getPdbCode());
			asxtal.setAssembly(assembly);
			
			assembly.addAssemblyScore(asxtal);
		}
	}
	
	private static Assembly getMatchingAssembly(Assembly pdbAssembly, CrystalAssemblies validAssemblies) {
		
		for (Assembly a:validAssemblies) {
			if (a.equals(pdbAssembly)) return a;
		}
		
		// if nothing returns, we still have to try whether any of our assemblies is a parent of pdbAssembly
		// We need this kind of matching for cases like 3cfh (see issue https://github.com/eppic-team/eppic/issues/47):
		//   in 3cfh the list of engaged interfaces that we detect from the PDB contains 
		//   a tiny interface cluster (12) that is thrown away by our validity detector (the
		//   edge that would make the graph isomorphic is missing because it falls below the 35A2 area cutoff)
		//   Thus none of our valid assemblies match the PDB one strictly and we need to do the trick below
		
		Assembly matching = null;
		
		for (Assembly a:validAssemblies) {
			if (pdbAssembly.isChild(a) && 
					pdbAssembly.getAssemblyGraph().getSubAssemblies().get(0).getStoichiometry().getCountForIndex(0) ==
					a.getAssemblyGraph().getSubAssemblies().get(0).getStoichiometry().getCountForIndex(0)) {
				
				if (matching!=null) 
					LOGGER.warn("More than 1 assembly in list of valid assemblies matches the PDB annotated bio unit assembly. Only last one {} will be considered",a.toString());
				
				matching = a;
			}
		}
		
		// if no match it will be null
		return matching;
		
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
	
	protected static String getSymmetryString(List<AssemblyContentDB> acDBs) {
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<acDBs.size();i++	) {
			sb.append(acDBs.get(i).getSymmetry());
			if (i!=acDBs.size()-1) sb.append(",");
		}
		return sb.toString();
	}
	
	protected static String getStoichiometryString(List<AssemblyContentDB> acDBs) {
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<acDBs.size();i++	) {
			sb.append(acDBs.get(i).getStoichiometry());
			if (i!=acDBs.size()-1) sb.append(",");
		}
		return sb.toString();
	}
	
	protected static String getMmSizeString(List<AssemblyContentDB> acDBs) {
		StringBuilder sb = new StringBuilder();
		for (int i=0;i<acDBs.size();i++	) {
			sb.append(acDBs.get(i).getMmSize());
			if (i!=acDBs.size()-1) sb.append(",");
		}
		return sb.toString();
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
		
		
		ChainEvolContextList cecl = iecl.getChainEvolContextList();
		
		for (ChainEvolContext cec:cecl.getAllChainEvolContext()) {
			
			// we first retrieved the corresponding chainClusterDB added in setPdbMetaData
			ChainClusterDB chainClusterDB = pdbInfo.getChainCluster(cec.getRepresentativeChainCode());
			
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
				
				chainClusterDB.setRefUniProtStart(cec.getPdbToUniProtMapper().getMatchingIntervalUniProtCoords().beg);
				chainClusterDB.setRefUniProtEnd(cec.getPdbToUniProtMapper().getMatchingIntervalUniProtCoords().end);
				
				chainClusterDB.setPdbStart(cec.getPdbToUniProtMapper().getMatchingIntervalPdbCoords().beg);
				chainClusterDB.setPdbEnd(cec.getPdbToUniProtMapper().getMatchingIntervalPdbCoords().end);
				
				chainClusterDB.setPdbAlignedSeq(cec.getPdbToUniProtMapper().getAlignment().getAlignedSequence(1).getSequenceAsString());
				chainClusterDB.setRefAlignedSeq(cec.getPdbToUniProtMapper().getAlignment().getAlignedSequence(2).getSequenceAsString());
				
				chainClusterDB.setSeqIdCutoff(cec.getIdCutoff());
				chainClusterDB.setClusteringSeqId(cec.getUsedClusteringPercentId()/100.0);
				
				// homologs
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
				
				
				// evol data adding to ResidueInfo
				if ( cec.isProtein() ) {

					List<Double> entropies = cec.getConservationScores();

					
					// following the same procedure as when we added the ResidueInfoDBs in createChainCluster
					// thus the indices will coincide

					int i = 0;
					for (Group g : getGroups(cec.getCompound())) {

						ResidueInfoDB residueInfo = chainClusterDB.getResidueInfos().get(i);
						
						int	queryUniprotPos = cec.getPdbToUniProtMapper().getUniProtIndexForPdbGroup(g, !cec.isSearchWithFullUniprot());
						int queryUniprotPosAbsolute = cec.getPdbToUniProtMapper().getUniProtIndexForPdbGroup(g, false);

						float entropy = -1;
						// we used to have here: "&& residue instanceof AaResidue" but that was preventing entropy values of mismatch-to-ref-uniprot-residues to be passed
						// for het residues we do have entropy values too as the entropy values are calculated on the reference uniprot sequence (where no het residues are present)
						if (entropies!=null && queryUniprotPos!=-1) {
							entropy = (float) entropies.get(queryUniprotPos-1).doubleValue();
						}

						if (queryUniprotPos==-1) 
							residueInfo.setMismatchToRef(true);
						else 
							residueInfo.setMismatchToRef(false);						
						
						residueInfo.setUniProtNumber(queryUniprotPosAbsolute);
						residueInfo.setEntropyScore(entropy);
						
						// TODO revise the no-SEQRES case (see other related TODOs in this file)
						// Do we want something like this here ? : 
						// (the idea is to assign uniprot numbers as residue serials so that we can reliably map
						//  between different chains when there's no SEQRES in the file)
						//if (cec.getPdbToUniProtMapper().isSequenceFromAtom()) {
						//	iri.setResidueNumber(cec.getPdbToUniProtMapper().getUniProtIndexForPdbGroup(residue, false));
						//}
						

						
						i++;
					}
				}
			} 

		}
				

		for (int i=0;i<iecl.size();i++) {
			
			InterfaceEvolContext iec = iecl.get(i);
			InterfaceDB ii = pdbInfo.getInterface(i+1);
			
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
	
	public void setResidueBurialDetails(StructureInterfaceList interfaces) {
		
		boolean noseqres = false;
		if (interfaces.size()>0) {
			Chain chain = interfaces.iterator().next().getParentChains().getFirst();
			if (chain.getSeqResGroups()==null || chain.getSeqResGroups().isEmpty()) {
				noseqres = true;
			}

			if (noseqres) 
				LOGGER.warn("There are no SEQRES groups available. "
						+ "There can be problems in residue mapping if the residue numbering is inconsistent "
						+ "across different chains of the same entity.", 
						chain.getChainID());
		}
		
		for (StructureInterface interf:interfaces) {
			
			InterfaceDB ii = pdbInfo.getInterface(interf.getId());

			// we add the residue details
			
			List<ResidueBurialDB> iril = new ArrayList<ResidueBurialDB>();
			ii.setResidueBurials(iril);

			addResidueBurialDetailsOfPartner(iril, interf, InterfaceEvolContext.FIRST, noseqres);
			addResidueBurialDetailsOfPartner(iril, interf, InterfaceEvolContext.SECOND, noseqres);

			for(ResidueBurialDB iri : iril) {
				iri.setInterfaceItem(ii);
			}

		}
	}
	
	private void addResidueBurialDetailsOfPartner(List<ResidueBurialDB> iril, StructureInterface interf, int molecId, boolean noseqres) {

		Chain chain = null;
		if (molecId == InterfaceEvolContext.FIRST) 
			chain =	interf.getParentChains().getFirst();
		else if (molecId == InterfaceEvolContext.SECOND) 
			chain =	interf.getParentChains().getSecond();
		
		String repChainId = chain.getCompound().getRepresentative().getChainID();
		ChainClusterDB chainCluster = pdbInfo.getChainCluster(repChainId);
		
		
		for (Group group:chain.getAtomGroups()) {

			if (group.isWater()) continue;

			GroupAsa groupAsa = null;
			if (molecId==InterfaceEvolContext.FIRST) 
				groupAsa = interf.getFirstGroupAsa(group.getResidueNumber());
			else if (molecId==InterfaceEvolContext.SECOND) 
				groupAsa = interf.getSecondGroupAsa(group.getResidueNumber());

			// if we have no groupAsa that means that this is a Residue for which we don't calculate ASA (most likely HETATM)
			if (groupAsa==null) continue;

			short assignment = ResidueBurialDB.OTHER;

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
					assignment = ResidueBurialDB.RIM_EVOLUTIONARY;
				} else if (groupAsa.getBsaToAsaRatio()<params.getCAcutoffForGeom()){
					assignment = ResidueBurialDB.CORE_EVOLUTIONARY; 
				} else {
					assignment = ResidueBurialDB.CORE_GEOMETRY;
				} 

				// residues not in interface but still with more ASA than minimum required are called surface
			} else if (groupAsa.getAsaU()>params.getMinAsaForSurface()) {
				assignment = ResidueBurialDB.SURFACE;
			}


			ResidueBurialDB iri = new ResidueBurialDB();

			iril.add(iri);
			
			iri.setAsa(asa);
			iri.setBsa(bsa);
			iri.setRegion(assignment);

			// side: false(0) for FIRST, true(1) for SECOND (used to be 1,2)
			boolean side = false;
			if (molecId==InterfaceEvolContext.SECOND) 
				side = true;
			
			iri.setSide(side);

			// relations
			iri.setInterfaceItem(pdbInfo.getInterface(interf.getId()));
			
			// this is a difficult operation: we are in a single chain and we connect to the equivalent
			// residue in the representative chain (the one we store in the residueInfos in chainCluster).
			// Thus the issues with residue serials in SEQRES/no SEQRES case will hit here!
			// See the comment in createChainCluster
			int resser = chain.getCompound().getAlignedResIndex(group, chain);
			if (resser==-1) {
				if (noseqres) 
					LOGGER.warn("Could not get a residue serial for group '{}' to connect ResidueBurial to ResidueInfo", group.toString());
				else 
					LOGGER.info("Could not get a residue serial for group '{}' to connect ResidueBurial to ResidueInfo", group.toString());
			} else {
				
				// Here getResidue(resser) matches the residue serials via the residue serials we added earlier 
				// to ResidueInfo. Those were coming from getAlignedResIndex, thus they should match correctly

				ResidueInfoDB residueInfo = chainCluster.getResidue(resser);

				// TODO in no-SEQRES case if the residues are not consistently named across different chains of 
				//      same entity, this will fail and return a null! e.g. 3ddo without seqres
				//      The ideal solution to this would be to go through our UniProt alignments and get a proper 
				//      mapping from there, but at this point we don't have an alignment yet... so it is complicated
				
				if (residueInfo==null && !noseqres) {
					// we only warn if we have seqres and find a null, case noseqres==true emits only 1 warning above
					LOGGER.warn("Could not find the ResidueInfo corresponding to ResidueBurial of group '{}', the mapped residue serial is {}.",
							group.toString(), resser);
				}
						
				
				iri.setResidueInfo(residueInfo);
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
		
		List<String> uniqChainIds = compound.getChainIds();

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
		List<String> uniqChainIds = compound.getChainIds();
		
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String chainId:uniqChainIds) {
			sb.append(chainId);
			if (i!=uniqChainIds.size()-1) sb.append(",");
			i++;
		}
		return sb.toString();
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
