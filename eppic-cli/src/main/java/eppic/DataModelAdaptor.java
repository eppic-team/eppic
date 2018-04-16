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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.biojava.nbio.structure.*;
import org.biojava.nbio.structure.asa.GroupAsa;
import org.biojava.nbio.structure.cluster.SubunitClustererParameters;
import org.biojava.nbio.structure.contact.AtomContact;
import org.biojava.nbio.structure.contact.GroupContact;
import org.biojava.nbio.structure.contact.GroupContactSet;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.quaternary.BioAssemblyInfo;
import org.biojava.nbio.structure.quaternary.BiologicalAssemblyBuilder;
import org.biojava.nbio.structure.quaternary.BiologicalAssemblyTransformation;
import org.biojava.nbio.structure.symmetry.core.QuatSymmetryDetector;
import org.biojava.nbio.structure.symmetry.core.QuatSymmetryParameters;
import org.biojava.nbio.structure.symmetry.core.QuatSymmetryResults;
import org.biojava.nbio.structure.xtal.CrystalCell;
import org.biojava.nbio.structure.xtal.SpaceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.analysis.compare.InterfaceMatcher;
import eppic.analysis.compare.SimpleInterface;
import eppic.assembly.Assembly;
import eppic.assembly.AssemblyDescription;
import eppic.assembly.CrystalAssemblies;
import eppic.assembly.GraphUtils;
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
	
	/**
	 * The method name for PDB biounit annotations, suffixed with the biounit number, e.g. pdb1, pdb2, ...
	 */
	public static final String PDB_BIOUNIT_METHOD_PREFIX = "pdb";	
	
	public static final int INVALID_ASSEMBLY_ID = 0;
	
	private PdbInfoDB pdbInfo;

	private EppicParams params;
	
	private RunParametersDB runParameters;
	
	// a temp map to hold the warnings per interface, used in order to eliminate duplicate warnings
	private HashMap<Integer,HashSet<String>> interfId2Warnings;
	
	// a map to convert between asym ids and chain ids so that we can match PDB biounits correctly
	private HashMap<String,String> asymIds2chainIds;
	
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
		runParameters.setEppicBuild(EppicParams.BUILD_GIT_SHA);
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
		
		if (pdbXtallographicInfo!=null) {
			pdbInfo.setNcsOpsPresent(pdbXtallographicInfo.getNcsOperators()!=null);

			pdbInfo.setNonStandardSg(pdbXtallographicInfo.isNonStandardSg());
			pdbInfo.setNonStandardCoordFrameConvention(pdbXtallographicInfo.isNonStandardCoordFrameConvention());
		}
		
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

	public void setChainClustersData(Structure pdb, Map<String,String> chainOrigNames) {
		List<ChainClusterDB> chainClusterDBs = new ArrayList<ChainClusterDB>();

		for (EntityInfo compound:pdb.getEntityInfos()) {

			if (compound.getType() == EntityType.POLYMER) {
				// in mmCIF files some sugars are annotated as compounds with no chains linked to them, e.g. 3s26
				if (compound.getChains().isEmpty()) continue;

				chainClusterDBs.add(createChainCluster(compound, chainOrigNames));
			}
		}
		pdbInfo.setNumChainClusters(chainClusterDBs.size());
		pdbInfo.setChainClusters(chainClusterDBs);

		initAsymIds2chainIdsMap(pdb);
	}
	
	/**
	 * Initialize the map of asym ids to chain ids, this is a hack needed to work around the
	 * limitations of the data structure in Biojava 4.2. The map is used in the PDB biounit to our
	 * own interfaces matching.
	 * <p/>
	 * Note that the map should work in most cases, but it's not guaranteed because there is a one-to-many
	 * relationship between author chain ids and asym ids (internal ids). This is the best we can do 
	 * with the data available from Biojava 4.2
	 * TODO check if we still need with BioJava 5
	 * @param pdb the structure
	 */
	private void initAsymIds2chainIdsMap(Structure pdb) {
		asymIds2chainIds = new HashMap<>();
		
		for (Chain c : pdb.getChains()) {
			asymIds2chainIds.put(c.getId(), c.getName());
		}
	}

	private ChainClusterDB createChainCluster(EntityInfo compound, Map<String,String> chainOrigNames) {
		ChainClusterDB chainClusterDB = new ChainClusterDB();
		
		chainClusterDB.setPdbCode(pdbInfo.getPdbCode());
		
		chainClusterDB.setRepChain(compound.getRepresentative().getName());
		chainClusterDB.setMemberChains(getMemberChainsString(compound, chainOrigNames));
		chainClusterDB.setNumMembers(getUniqueChainNames(compound, chainOrigNames).size());
		chainClusterDB.setProtein(compound.getRepresentative().isProtein());
		
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
			residueInfoDB.setRepChain(compound.getRepresentative().getName());
			
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
	
	private List<Group> getGroups(EntityInfo compound) {
		
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
		
		List<StructureInterfaceCluster> interfaceClusters = reduceToNcsUnique(interfaces);

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
				
				interfaceDB.setSelfContactOverlapScore(interf.getContactOverlapScore(interf, true));
				
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
					if (firstGroup.getChain().getEntityInfo()==null)
						contact.setFirstResNumber(UNKNOWN_RESIDUE_INDEX);
					else 
						contact.setFirstResNumber(firstGroup.getChain().getEntityInfo().getAlignedResIndex(firstGroup, firstGroup.getChain()) );
					if (secondGroup.getChain().getEntityInfo()==null)
						contact.setSecondResNumber(UNKNOWN_RESIDUE_INDEX);
					else
						contact.setSecondResNumber(secondGroup.getChain().getEntityInfo().getAlignedResIndex(secondGroup, secondGroup.getChain()) );
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
		
		// the max number of clashes, used for general warnings on top (there's additional warnings per interface)
		List<Integer> numClashesPerInterface = new ArrayList<>(interfaces.size());
		for (StructureInterface interf:interfaces) {
			numClashesPerInterface.add(interf.getContacts().getContactsWithinDistance(EppicParams.CLASH_DISTANCE).size());
		}

		pdbInfo.setMaxNumClashesAnyInterface(Collections.max(numClashesPerInterface));
		
	}

	private List<StructureInterfaceCluster> reduceToNcsUnique(StructureInterfaceList interfaces) {
		List<StructureInterfaceCluster> clusters = interfaces.getClusters(EppicParams.CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF);

		if (!pdbInfo.isNcsOpsPresent()) {
			// no NCS case (normal case), return clusters as is
			return clusters;
		}

		// NCS case. We need to reduce to the unique-to-NCS set
		List<StructureInterfaceCluster> interfaceClustersNcs = interfaces.getClustersNcs();

		List<StructureInterfaceCluster> reduced = new ArrayList<>();
		for (StructureInterfaceCluster cluster : clusters) {
			Set<Integer> indices = new TreeSet<>();
			for (StructureInterface interf : cluster.getMembers()) {
				indices.add(getCorrespondingClustersIndex(interf, interfaceClustersNcs));
			}

			StructureInterfaceCluster reducedCluster = new StructureInterfaceCluster();
			reducedCluster.setId(cluster.getId());
			reducedCluster.setAverageScore(cluster.getAverageScore());
			for (int i : indices) {
				// we add one interface per NCS interface cluster
				StructureInterface interf = interfaceClustersNcs.get(i).getMembers().get(0);
				if (interf.getCluster().getId() != reducedCluster.getId()) {
					LOGGER.warn("Interface {} belongs to cluster {}. It should not be added to cluster id {}",
							interf.getId(), interf.getCluster().getId(), reducedCluster.getId());
				}
				reducedCluster.addMember(interf);
				// we add also the new back-reference to the parent
				interf.setCluster(reducedCluster);
			}

			reduced.add(reducedCluster);
		}

		return reduced;
	}

	private static int getCorrespondingClustersIndex(StructureInterface interf, List<StructureInterfaceCluster> interfaceClustersNcs) {
		for (int i = 0; i< interfaceClustersNcs.size(); i++) {
			for (StructureInterface s : interfaceClustersNcs.get(i).getMembers()) {
				if (s.getId() == interf.getId()) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public void setAssemblies(CrystalAssemblies validAssemblies) {
		
		pdbInfo.setExhaustiveAssemblyEnumeration(validAssemblies.isExhaustiveEnumeration());
		
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
			for (int id: GraphUtils.getDistinctInterfaceClusters(validAssembly.getAssemblyGraph().getSubgraph())) {
				InterfaceClusterDB icDB = pdbInfo.getInterfaceCluster(id);
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
				acDB.setComposition(d.getCompositionChainIds());
				acDB.setCompositionRepChainIds(d.getCompositionRepChainIds());
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
			as.setCallReason(validAssembly.getCallReason());
			as.setScore(validAssembly.getScore());
			as.setConfidence(validAssembly.getConfidence());
			as.setPdbCode(pdbInfo.getPdbCode());
			as.setAssembly(assembly);
			assembly.addAssemblyScore(as);
			
			
		}
	}
	
	/**
	 * Populate the data model with all the PDB biounit annotations.
	 * @param bioAssemblies
	 * @param validAssemblies
	 * @param pdb
	 */
	public void setPdbBioUnits(Map<Integer, BioAssemblyInfo> bioAssemblies, CrystalAssemblies validAssemblies, Structure pdb) {
		
		// see https://github.com/eppic-team/eppic/issues/139
		
		if (bioAssemblies == null) {
			LOGGER.info("No bio assembly annotations present, will not add bio assemblies info to data model");
			return;
		}
		
		for (Entry<Integer, BioAssemblyInfo> entry : bioAssemblies.entrySet()) {
			int bioAssemblyNumber = entry.getKey();
			BioAssemblyInfo bioAssembly = entry.getValue();
			
			setPdbBioUnit(bioAssemblyNumber, bioAssembly, validAssemblies, pdb);
		}
		
	}
	
	/**
	 * Populate the data model with 1 PDB biounit annotation.
	 * @param bioAssemblyNumber
	 * @param bioAssembly
	 * @param validAssemblies
	 * @param pdb
	 */
	private void setPdbBioUnit(int bioAssemblyNumber, BioAssemblyInfo bioAssembly, CrystalAssemblies validAssemblies, Structure pdb) {

		if (bioAssembly == null) {
			LOGGER.info("No bio assembly annotation present, will not add bio assembly info to data model");
			return;
		}
		
		CrystalCell cell = null;
		if (validAssemblies.getStructure().getCrystallographicInfo()!=null && validAssemblies.getStructure().isCrystallographic()) {
			cell = validAssemblies.getStructure().getCrystallographicInfo().getCrystalCell();
		}

		
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
		as.setMethod(PDB_BIOUNIT_METHOD_PREFIX + bioAssemblyNumber);
		as.setCallName(CallType.BIO.getName());
		as.setCallReason(""); // empty for the moment, perhaps we could use it for authors/pisa
		as.setScore(SCORE_NOT_AVAILABLE);
		as.setConfidence(CONFIDENCE_NOT_AVAILABLE);
		as.setPdbCode(pdbInfo.getPdbCode());
		
		if (matchingAssemblyDB!=null) {

			as.setAssembly(matchingAssemblyDB);
			
			// we use the symmetry detected with biojava's quat symmetry detection just to double check that we get it right in eppic
			String[] symmetries = getSymmetry(pdb, bioAssemblyNumber);
			
			if (!getSymmetryString(matchingAssemblyDB.getAssemblyContents()).equals(symmetries[0])) {
				LOGGER.warn("Symmetry calculated from graph is {} whilst detected from biounit is {}",
						getSymmetryString(matchingAssemblyDB.getAssemblyContents()),symmetries[0]);
			}
			
			// we represent the stoichiometries in db with parenthesis, e.g. A(2), we need to strip them before comparing 
			String stoFromEppic = getStoichiometryString(matchingAssemblyDB.getAssemblyContents());
			stoFromEppic = stoFromEppic.replaceAll("[()]", "");
			
			if (!stoFromEppic.equals(symmetries[1])) {
				LOGGER.warn("Stoichiometry calculated from graph is {} whilst detected from biounit is {}",
						stoFromEppic,symmetries[1]);
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
			asxtal.setMethod(PDB_BIOUNIT_METHOD_PREFIX + bioAssemblyNumber);
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
	 * @param cell
	 * @return the list of matching cluster ids
	 */
	private Set<Integer> matchToInterfaceClusters(BioAssemblyInfo bioUnit, CrystalCell cell) {

		// the Set will eliminate duplicates if any found, I'm not sure if duplicates are even possible really...
		Set<Integer> matchingClusterIds = new TreeSet<Integer>();

		List<SimpleInterface> bioUnitInterfaces = SimpleInterface.createSimpleInterfaceListFromPdbBioUnit(bioUnit, cell, asymIds2chainIds);
		InterfaceMatcher im = new InterfaceMatcher(pdbInfo.getInterfaceClusters(),bioUnitInterfaces);
		for (InterfaceClusterDB ic:pdbInfo.getInterfaceClusters()) {
			for (InterfaceDB i:ic.getInterfaces()) {
				if (im.oursMatch(i.getInterfaceId())) {
					matchingClusterIds.add(ic.getClusterId()); 							 
				} 
			}
		}

		if (!im.checkTheirsMatch()) {
			StringBuilder msg = new StringBuilder();
			for (SimpleInterface theirI:im.getTheirsNotMatching()) {
				msg.append(theirI.toString()).append("\t");
			}

			// This actually happens even if the mapping is fine. That's because we enumerate the biounit 
			// interfaces exhaustively, and thus sometimes an interface might not happen in reality because 
			// 2 molecules don't make a contact. 
			LOGGER.info("Some interfaces of PDB bio unit "+EppicParams.PDB_BIOUNIT_TO_USE+
					" do not match any of the EPPIC interfaces."+ 
					" Non-matching interfaces are: "+msg.toString());

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
			if (pdbInfo.isNcsOpsPresent() && ii==null) {
				LOGGER.info("Not storing geometry scores for redundant NCS interface {}", i+1);
				continue;
			}
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
				LOGGER.warn("There are no SEQRES groups available for chain with name {}. "
						+ "There can be problems in residue mapping if the residue numbering is inconsistent "
						+ "across different chains of the same entity.", 
						chain.getName());
		}
		
		for (StructureInterface interf:interfaces) {
			
			InterfaceDB ii = pdbInfo.getInterface(interf.getId());

			if (pdbInfo.isNcsOpsPresent() && ii==null) {
				LOGGER.info("Not storing residue burials info for redundant NCS interface {}", interf.getId());
				continue;
			}
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
		
		String repChainId = chain.getEntityInfo().getRepresentative().getName();
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
			int resser = chain.getEntityInfo().getAlignedResIndex(group, chain);
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
	
	public static String getChainClusterString(EntityInfo compound) {

		StringBuilder sb = new StringBuilder();

		sb.append(compound.getRepresentative().getName());
		
		List<String> uniqChainIds = compound.getChainIds();

		if (uniqChainIds.size()>1) {

			sb.append(" (");
			for (String chainId:uniqChainIds) {
				if (chainId.equals(compound.getRepresentative().getName())) {
					continue;
				}

				sb.append(chainId+",");

			}

			sb.deleteCharAt(sb.length()-1);
			sb.append(")");
		}

		return sb.toString();
	}

	private Set<String> getUniqueChainNames(EntityInfo compound, Map<String, String> chainOrigNames) {
		List<Chain> chains = compound.getChains();
		Set<String> uniqChainNames = new TreeSet<>();
		for (Chain c : chains) {
			String chainName;
			if(chainOrigNames!=null) { // will only be not null in cases with NCS ops
				chainName = chainOrigNames.get(c.getName());
			} else {
				chainName = c.getName();
			}
			uniqChainNames.add(chainName);
		}
		return uniqChainNames;
	}

	private String getMemberChainsString(EntityInfo compound, Map<String, String> chainOrigNames) {

		Set<String> uniqChainNames = getUniqueChainNames(compound, chainOrigNames);

		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String chainId:uniqChainNames) {
			sb.append(chainId);
			if (i!=uniqChainNames.size()-1) sb.append(",");
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
	
	/**
	 * Finds the symmetry of the biounit with the biojava quat symmetry algorithms
	 * @param pdb the au of the structure
	 * @param bioUnitNumber
	 * @return an array of size 2 with members: symmetry, stoichiometry
	 */
	private static String[] getSymmetry(Structure pdb, int bioUnitNumber) {
		
		
		if (pdb.getPDBHeader().getBioAssemblies().get(bioUnitNumber)==null || 
			pdb.getPDBHeader().getBioAssemblies().get(bioUnitNumber).getTransforms() == null || 
			pdb.getPDBHeader().getBioAssemblies().get(bioUnitNumber).getTransforms().size() == 0){
			
			LOGGER.warn("Could not load transformations for PDB biounit {}. Will not assign a symmetry value to it.", bioUnitNumber);
			return new String[]{null,null};
		}
		
		List<BiologicalAssemblyTransformation> transformations = 
				pdb.getPDBHeader().getBioAssemblies().get(bioUnitNumber).getTransforms();

		
		BiologicalAssemblyBuilder builder = new BiologicalAssemblyBuilder();

		Structure bioAssembly = builder.rebuildQuaternaryStructure(pdb, transformations, true, false);

		QuatSymmetryParameters parameters = new QuatSymmetryParameters();
        parameters.setOnTheFly(true);
        SubunitClustererParameters clusterParams = new SubunitClustererParameters();

        // TODO not sure if this is still possible in biojava 5
		//if (!detector.hasProteinSubunits()) {	
		//	LOGGER.info("No protein chains in biounit {}, can't calculate symmetry. Will not assign a symmetry value to it.", bioUnitNumber);
		//	return new String[]{null,null};
		//}		

		QuatSymmetryResults globalResults = QuatSymmetryDetector.calcGlobalSymmetry(bioAssembly, parameters, clusterParams);
		
		if (globalResults == null) {
			LOGGER.warn("No global symmetry found for biounit {}. Will not assign a symmetry value to it.",  bioUnitNumber);
			return new String[]{null, null};
		}
		
		String symmetry = globalResults.getSymmetry();
		
		String stoichiometry = globalResults.getStoichiometry().toString();
		LOGGER.info("Symmetry {} (stoichiometry {}) found in biounit {}", 
				symmetry, stoichiometry, bioUnitNumber);
		
		return new String[]{symmetry, stoichiometry};
		
	}
}
