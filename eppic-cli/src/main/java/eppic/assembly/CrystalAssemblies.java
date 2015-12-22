package eppic.assembly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Compound;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.CallType;
import eppic.EppicParams;
import eppic.InterfaceEvolContextList;

/**
 * A representation of all valid assemblies in a crystal structure.
 * 
 * @author duarte_j
 *
 */
public class CrystalAssemblies implements Iterable<Assembly> {
	
	
	private static final Logger logger = LoggerFactory.getLogger(CrystalAssemblies.class);

	private LatticeGraph<ChainVertex,InterfaceEdge> latticeGraph;
	private Structure structure;
	private List<StructureInterfaceCluster> interfaceClusters;

	/**
	 * The set of all valid assemblies in the crystal.
	 */
	private Set<Assembly> all;	
	
	/**
	 * Each of the clusters of equivalent assemblies.
	 * The representative is the first member and the maximal Assembly in terms of number of engaged interface clusters.
	 */
	private List<AssemblyGroup> clusters;
	
	private Map<Integer,AssemblyGroup> groups;
	
	private InterfaceEvolContextList interfEvolContextList;
	
	// the entity/chain maps: to map between indices used in Stoichiometry arrays and entity ids/chain ids
	private Map<Integer,Integer> entityId2Idx;
	private Map<Integer,Integer> idx2EntityId;
	
	private Map<String,Integer> chainIds2Idx;
	private Map<Integer,String> idx2ChainIds;
	
	
	
	/**
	 * 
	 * @param structure
	 * @param interfaces
	 * @throws StructureException
	 */
	public CrystalAssemblies(Structure structure, StructureInterfaceList interfaces) throws StructureException {
		
		this.structure = structure;
		this.latticeGraph = new LatticeGraph<ChainVertex,InterfaceEdge>(structure, interfaces,ChainVertex.class,InterfaceEdge.class);
		this.interfaceClusters = interfaces.getClusters(EppicParams.CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF);
		
		initEntityMaps();
		
		findValidAssemblies();
		
		initGroups();
		
		initClusters();
		
		
	}
	
	public int size() {
		return clusters.size();
	}
	
	/**
	 * Returns all topologically valid assemblies present in the crystal.
	 * The method traverses the tree of all possible combinations of n interface
	 * clusters (2^n in total), e.g. for a structure with 3 clusters {0,1,2} the tree looks like:
	 * <pre>
	 *         {}
	 *       /  |  \
	 *    {0}  {1}  {2}
	 *    |  X     X   |
	 *  {0,1} {0,2} {1,2}
	 *      \   |   /
	 *       {0,1,2}   
	 * </pre>
	 * As the tree is traversed, if a node is found to be an invalid assembly, then all of its children
	 * are pruned off and not tried. Thus the number of combinations reduces very quickly with a few
	 * pruned top nodes.
	 * @return
	 */
	private void findValidAssemblies() {
		
		latticeGraph.removeDuplicateEdges();
		
		Set<Assembly> validAssemblies = new HashSet<Assembly>();;
		
		// the list of nodes in the tree found to be invalid: all of their children will also be invalid
		List<Assembly> invalidNodes = new ArrayList<Assembly>();		
		
		Assembly emptyAssembly = new Assembly(this, new PowerSet(interfaceClusters.size()));
		
		validAssemblies.add(emptyAssembly); // the empty assembly (no engaged interfaces) is always a valid assembly
		
		Set<Assembly> prevLevel = new HashSet<Assembly>();
		prevLevel.add(emptyAssembly);
		Set<Assembly> nextLevel = null;
		
		for (int k = 1; k<=interfaceClusters.size();k++) {
			
			logger.debug("Traversing level {} of tree: {} parent nodes",k,prevLevel.size());
			
			nextLevel = new HashSet<Assembly>();
					
			for (Assembly p:prevLevel) {
				List<Assembly> children = p.getChildren(invalidNodes);
				
				for (Assembly c:children) {
					
					if (!c.isValid()) {
						logger.debug("Node {} is invalid, will prune off all of its children",c.toString());
						invalidNodes.add(c);
					} else {
						// we only add a child for next level if we know it's valid, if it wasn't valid 
						// then it's not added and thus the whole branch is pruned
						nextLevel.add(c);
						// add assembly as valid
						validAssemblies.add(c);
					}
				}
			}
			prevLevel = new HashSet<Assembly>(nextLevel); 
			
		}
		
		this.all = validAssemblies;
		

	}
	
	private void initGroups() {
		this.groups = new TreeMap<Integer, AssemblyGroup>();

		for (Assembly assembly:all) {

			// we classify into groups those that are fully covering

			if (assembly.getAssemblyGraph().isFullyCovering()) {
				// we assume they are valid, which implies even stoichiometry (thus the size of first will give the size for all)			
				int size = assembly.getAssemblyGraph().getSubAssemblies().get(0).getStoichiometry().getCountForIndex(0);

				if (!groups.containsKey(size)) {
					AssemblyGroup group = new AssemblyGroup();
					groups.put(size, group);
					group.add(assembly);
				} else {
					groups.get(size).add(assembly);
				}
			} else {
				logger.info("Assembly {} will not be clustered since it doesn't cover all entities",assembly.toString());
				if (!groups.containsKey(-1)) {
					AssemblyGroup group = new AssemblyGroup();
					group.add(assembly);
					groups.put(-1, group);
				} else {
					groups.get(-1).add(assembly);
				}
			}
		}
	}
	
	private void initClusters() {
		
		logger.info("Number of assemblies before clustering: {}", all.size());
		logger.info("Number of assembly groups: {}", groups.size());
		
		clusters = new ArrayList<AssemblyGroup>();
		
		for (int size:groups.keySet()) {
			AssemblyGroup ag = groups.get(size);

			if (size>0) {
				List<AssemblyGroup> clustersForGroup = ag.sortIntoClusters();

				logger.debug("{} assemblies with size {} group into {} clusters",ag.size(),size,clustersForGroup.size());


				for (int i=0;i<clustersForGroup.size();i++) {

					if (clustersForGroup.get(i).size()>1) 
						logger.info("Using assembly {} as representative for assembly cluster {}",clustersForGroup.get(i).get(0),clustersForGroup.get(i));

					this.clusters.add(clustersForGroup.get(i)); 
				}
			} else {
				// for those in the "-1" group we just add each assembly as a single-member cluster
				for (Assembly assembly:ag) {
					AssemblyGroup g = new AssemblyGroup();
					g.add(assembly);
					clusters.add(g);
				}
			}
		}
		
		logger.info("Number of assemblies after clustering: {}", clusters.size());
	}
	
	private void initEntityMaps() {
		// since the entityIds are not guaranteed to be 1 to n, we need to map them to indices
		entityId2Idx = new HashMap<Integer,Integer>();
		idx2EntityId = new HashMap<Integer,Integer>();
		chainIds2Idx = new HashMap<String, Integer>();
		idx2ChainIds = new HashMap<Integer, String>();

		int i = 0;
		for (Compound c:structure.getCompounds()) {
			entityId2Idx.put(c.getMolId(),i);
			idx2EntityId.put(i,c.getMolId());
			i++;
		}

		i = 0;
		for (Chain c:structure.getChains()) {
			chainIds2Idx.put(c.getChainID(),i);
			idx2ChainIds.put(i,c.getChainID());
			i++;
		}
	}
	
	/**
	 * Given an entity id returns the entity index as used by Stoichiometry arrays
	 * @param entityId
	 * @return
	 */
	public int getEntityIndex(int entityId) {
		return entityId2Idx.get(entityId);
	}
	
	/**
	 * Given and entity index as used in Stoichiometry arrays, returns the entity id
	 * @param index
	 * @return
	 */
	public int getEntityId(int index) {
		return idx2EntityId.get(index);
	}
	
	/**
	 * Given a chain id returns a chain index as used in Stoichiometry arrays
	 * @param chainId
	 * @return
	 */
	public int getChainIndex(String chainId) {
		return chainIds2Idx.get(chainId);
	}
	
	/**
	 * Given a chain index as used in Stoichiometry arrays, returns a chain id
	 * @param chainIdx
	 * @return
	 */
	public String getChainId(int chainIdx) {
		return idx2ChainIds.get(chainIdx);
	}
	
	/**
	 * Given an entity index as used in Stoichiometry arrays, returns the representative chain id for that entity
	 * @param index
	 * @return
	 */
	public String getRepresentativeChainIdForEntityIndex(int index) {
		return structure.getCompoundById(getEntityId(index)).getRepresentative().getChainID();
	}
	
	/**
	 * Get the number of chains in the Structure
	 * @return
	 */
	public int getNumChainsInStructure() {
		return structure.getChains().size();
	}
	
	/**
	 * Get the number of entities in the Structure
	 * @return
	 */
	public int getNumEntitiesInStructure() {
		
		int size = 0;
		// in mmCIF files some sugars are annotated as compounds with no chains linked to them, e.g. 3s26, 4uo5
		// we need to skip those to count the entities here
		for (Compound comp: structure.getCompounds()) {
			if (comp.getChains().isEmpty()) continue;
			size++;
		}
					
		return size;
	}


	@Override
	public Iterator<Assembly> iterator() {
		
		return getUniqueAssemblies().iterator();
	}
	
	/**
	 * Get all valid assemblies in the crystal (unclustered)
	 * @return
	 * @see #getUniqueAssemblies()
	 */
	public Set<Assembly> getAllAssemblies() {
		return all;
	}
	
	public Structure getStructure() {
		return structure;
	}
	
	public LatticeGraph<ChainVertex, InterfaceEdge> getLatticeGraph() {
		return latticeGraph;
	}
	
	public List<StructureInterfaceCluster> getInterfaceClusters() {
		return interfaceClusters;
	}
	
	/**
	 * Returns the list of unique valid assemblies in the crystal, that is the representatives 
	 * of each of the assembly clusters. The representatives are chosen to be those assemblies that
	 * have maximal number of engaged interface clusters out of the group of equivalent assemblies 
	 * in the assembly cluster.
	 * The output list is sorted ascending on size of assemblies, with {@link Assembly#getId()} assigned
	 * according to that sorting.
	 * @return
	 */
	public List<Assembly> getUniqueAssemblies() {
		List<Assembly> representatives = new ArrayList<Assembly>();
		
		
		for (AssemblyGroup cluster:clusters) {
			// we use the first member of each cluster (which is the maximal group, see 
			// AssemblyGroup.sortIntoClusters() ) as the representative
			representatives.add(cluster.get(0));
		}
		
		Collections.sort(representatives, new Comparator<Assembly>() {

			@Override
			public int compare(Assembly arg0, Assembly arg1) {
				int firstSize = arg0.getAssemblyGraph().getSubAssemblies().get(0).getStoichiometry().getTotalSize();
				int secondSize = arg1.getAssemblyGraph().getSubAssemblies().get(0).getStoichiometry().getTotalSize();
				
				if (firstSize != secondSize) {
					return Integer.compare(firstSize, secondSize);
				} 
				
				// if both of same size, we sort based on engaged interface cluster ids
				List<StructureInterfaceCluster> interfClusters0 = arg0.getEngagedInterfaceClusters();
				List<StructureInterfaceCluster> interfClusters1 = arg1.getEngagedInterfaceClusters();
				if (interfClusters0.size()!=interfClusters1.size()) {
					// if different number of interface clusters we put the one with the most clusters first
					return Integer.compare(interfClusters1.size(),interfClusters0.size());
				}

				for (int i=0;i<interfClusters0.size();i++) {
					int id0 = interfClusters0.get(i).getId();
					int id1 = interfClusters1.get(i).getId();
					
					if (id0==id1) continue;
					
					return Integer.compare(id0,id1);
				}
				
				// this would happen if both are size 0
				return 0;
			}
			
		});

		int id = 1;
		for (Assembly a:representatives) {
			a.setId(id);
			id++;
		}
		
		return representatives;
	}
	
	public List<AssemblyGroup> getClusters() {
		return clusters;
	}
	
	public Assembly generateAssembly(int interfaceClusterId) {
		int[] interfaceClusterIds = new int[] {interfaceClusterId};
		return generateAssembly(interfaceClusterIds);
	}
	
	public Assembly generateAssembly(int[] interfaceClusterIds) {
		
		PowerSet engagedSet = new PowerSet(interfaceClusters.size());
		
		for (int clusterId:interfaceClusterIds) {
			engagedSet.switchOn(clusterId-1);
		}
		
		Assembly a = new Assembly(this, engagedSet);
		
		return a;
	}
	
	public InterfaceEvolContextList getInterfaceEvolContextList() {
		return interfEvolContextList;		
	}
	
	public void setInterfaceEvolContextList(InterfaceEvolContextList interfEvolContextList) {
		this.interfEvolContextList = interfEvolContextList;
	}
	
	public void score() {

		// this gets each of the unique assembly clusters, represented by the maximal member
		List<Assembly> uniques = getUniqueAssemblies();

		// 1 Do individual assemblies scoring
		for (Assembly a:uniques) {			
			a.score();							
		}
		
		// 2 Look at all calls and keep only the largest bio assembly. If no bios at all then assign bio to monomers
		Assembly maxSizeBioAssembly = null;
		int maxSize = 0;
		for (Assembly a:uniques) {
						
			// TODO this ignores other non-overlapping stoichiometries, must take care of that
			Stoichiometry sto = a.getAssemblyGraph().getSubAssemblies().get(0).getStoichiometry();
			int size = sto.getTotalSize();
			
			if (a.getCall() == CallType.BIO && maxSize<size) {
				maxSizeBioAssembly = a;
				maxSize = size;
			}
		}
		
		if (maxSizeBioAssembly == null) {
			// no assemblies were BIO
			
			for (Assembly a:uniques) {
				
				// TODO check how this would work with heteromers
				
				if (a.getNumEngagedInterfaceClusters()==0) {
					a.setCall(CallType.BIO);
				}
			}
			
		} else {			
			
			for (Assembly a:uniques) {			
				if (a == maxSizeBioAssembly) continue;
				
				a.setCall(CallType.CRYSTAL);
				
			}
		}
	}
	
}
