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
import java.util.TreeSet;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.EntityInfo;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.CallType;
import eppic.EppicParams;
import eppic.InterfaceEvolContextList;

/**
 * A representation of all valid assemblies in a crystal structure.
 * 
 * @author Jose Duarte
 *
 */
public class CrystalAssemblies implements Iterable<Assembly> {
	
	
	private static final Logger logger = LoggerFactory.getLogger(CrystalAssemblies.class);

	/**
	 * If this max number of assemblies is exceeded the assembly enumeration will happen on
	 * the heteromeric-contracted graph instead of on the full graph.
	 */
	private static final int MAX_ALLOWED_ASSEMBLIES = 1000;
	
	private LatticeGraph<ChainVertex,InterfaceEdge> latticeGraph;
	private Structure structure;

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
		
	private boolean largeNumAssemblies;
	
	/**
	 * Whether the assembly enumeration was exhaustive or via 
	 * heuristically contracting heteromeric edges.
	 */
	private boolean exhaustiveEnumeration;
		
	/**
	 * 
	 * @param structure
	 * @param interfaces
	 * @throws StructureException
	 */
	public CrystalAssemblies(Structure structure, StructureInterfaceList interfaces) throws StructureException {
		init(structure, interfaces, false);
	}
	
	/**
	 * 
	 * @param structure
	 * @param interfaces
	 * @param forceContracted
	 * @throws StructureException
	 */
	public CrystalAssemblies(Structure structure, StructureInterfaceList interfaces, boolean forceContracted) throws StructureException {
		init(structure, interfaces, forceContracted);
	}
	
	private void init(Structure structure, StructureInterfaceList interfaces, boolean forceContracted) throws StructureException {
		this.largeNumAssemblies = false;
		
		// for most cases we'll do the exhaustive enumeration, below we set to false when not
		this.exhaustiveEnumeration = true;
		
		this.structure = structure;
		this.latticeGraph = new LatticeGraph<ChainVertex,InterfaceEdge>(structure, interfaces,ChainVertex.class,InterfaceEdge.class);
				
		initEntityMaps();
		
		latticeGraph.removeDuplicateEdges();
		
		GraphContractor<ChainVertex, InterfaceEdge> graphContractor = null;

		if (forceContracted) {
			logger.info("Doing assemblies enumeration with graph contraction, since forceContracted is true");
			graphContractor = latticeGraph.contractGraph(InterfaceEdge.class);
			exhaustiveEnumeration = false;
		} 

		findValidAssemblies();

		if (!forceContracted && largeNumAssemblies) {

			logger.info("Structure has more than {} assemblies in full enumeration, will contract heteromeric interfaces to enumerate assemblies.", MAX_ALLOWED_ASSEMBLIES);

			graphContractor = latticeGraph.contractGraph(InterfaceEdge.class);
			
			findValidAssemblies();
			
			exhaustiveEnumeration = false;
		}
		
		if (graphContractor!=null) { // i.e. if we did contracted enumeration above
			
			// after the enumeration with the simplified contracted graph, we need to go back and 
			// express everything in terms of the full graph
			convertToFullGraph(graphContractor);

		}

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
				
		Set<Assembly> validAssemblies = new HashSet<Assembly>();
		
		// in contracted case this will find the distinct interfaces for the contracted graph
		int numInterfaceClusters = GraphUtils.getNumDistinctInterfaces(latticeGraph.getGraph());
		
		// the list of nodes in the tree found to be invalid: all of their children will also be invalid
		List<Assembly> invalidNodes = new ArrayList<Assembly>();		
		
		Assembly emptyAssembly = new Assembly(this, new PowerSet(numInterfaceClusters));
		
		validAssemblies.add(emptyAssembly); // the empty assembly (no engaged interfaces) is always a valid assembly
		
		Set<Assembly> prevLevel = new HashSet<Assembly>();
		prevLevel.add(emptyAssembly);
		Set<Assembly> nextLevel = null;

		for (int k = 1; k<=numInterfaceClusters; k++) {

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

						if (validAssemblies.size() > MAX_ALLOWED_ASSEMBLIES) {
							logger.warn("Exceeded the default max number of allowed assemblies ({}). Will do assembly enumeration from heteromeric-contracted graph", MAX_ALLOWED_ASSEMBLIES);
							largeNumAssemblies = true;
							all = new HashSet<Assembly>();
							return;
						}
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
		for (EntityInfo c:structure.getEntityInfos()) {
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
	 * Converts the assemblies generated through contracted enumeration (referring to the contracted graph) 
	 * to assemblies that refer to the full graph.
	 * @param graphContractor
	 */
	private void convertToFullGraph(GraphContractor<ChainVertex, InterfaceEdge> graphContractor) {
		// after the enumeration with the simplified contracted graph, we need to go back and 
		// express everything in terms of the full graph

		// this sets the exposed graph in lattice graph back to the full graph
		latticeGraph.filterEngagedClusters(null); 
		
		// let's get the number of interfaces in the full graph
		int numInterfaceClusters = GraphUtils.getNumDistinctInterfaces(latticeGraph.getGraph());
		
		
		Set<Assembly> allFromContracted = new HashSet<>(all);
		all = new HashSet<>();
		// the contracted graph enumeration will never count the trivial assembly of no interfaces engaged,
		// we need to add it now
		all.add(new Assembly(this, new PowerSet(numInterfaceClusters)));
		
		for (Assembly a : allFromContracted) {
			
			// 0. Initialise a list of interface cluster ids that we are going to engage in the full graph
			Set<Integer> interfClusterIdsToEngage = new HashSet<>();
			
			// 1. Add the edges in this assembly's contracted graph. Those are also valid edges in the full graph, we can simply add them directly			
			for (InterfaceEdge e : a.getAssemblyGraph().getSubgraph().edgeSet()) {
				interfClusterIdsToEngage.add(e.getClusterId());
			}
			
			// 2. Now simply add the contracted edges
			Set<Integer> contractedInterfClusterIds = graphContractor.getContractedInterfClusterIds();
			interfClusterIdsToEngage.addAll(contractedInterfClusterIds);
			
			// 3. Create the new assembly with interfClusterIdsToEngage
			Assembly aInFull = generateAssembly(interfClusterIdsToEngage);
			
			// 4. Finally we check if there's any induced interfaces that should be added, 
			// this is because GraphContractor.getContractedInterfClusterIds() does not keep track of interfaces that disappear indirectly during the contraction.
			// It only tracks the main heteromeric interfaces that we contract.
			PowerSet ps = aInFull.getEngagedSet();
			List<PowerSet> children = ps.getChildren(null);
			Set<Integer> inducedInterfClusterIds = new TreeSet<>();
			for (PowerSet child : children) {
				// if engaging an interface doesn't change the stoichiometry, then the interface is induced							
				Assembly childAssembly = new Assembly(this, child);
				
				// note: here we use the same check as in Assembly.calcScore() TODO is this the best way to do it? or should we check the stoichiometry?
				if ( (childAssembly.getAssemblyGraph().getSubAssemblies().size() == 
						aInFull.getAssemblyGraph().getSubAssemblies().size()) &&
						childAssembly.isValid() ) { // it also needs to be a valid assembly
					
					// alright, it looks like the newly engaged interface doesn't change the assembly, i.e. it is induced
					int index = ps.getDiff(child);
					if (index==-1) {
						logger.error("Something wrong when trying to find induced interfaces of assembly {} (after contraction. This must be a bug)", aInFull);
						throw new RuntimeException("Something wrong when trying to find induced interfaces after contraction. This must be a bug");
					}
					int interfClusterId = index +1;
					
					inducedInterfClusterIds.add(interfClusterId); 
				}
			}
			if (inducedInterfClusterIds.size()>0) {
				interfClusterIdsToEngage.addAll(inducedInterfClusterIds);
				aInFull = generateAssembly(interfClusterIdsToEngage);
			}
			
			all.add(aInFull);
			
			
			// TODO in some cases we might lose some induced interfaces in making the new assemblies here,
			//      e.g. in 4nwp the tetrahedral assembly is {1,2,3,8} but we actually get {1,2,3} from this procedure, losing 
			//      induced interface 8 (see test case in TestContractedAssemblyEnumeration)
			
			// another example: 5j11, after contraction we should get {1,2,3} but we only get {1,2}, interface 3 is induced
			// see test case in TestContractedAssemblyEnumeration
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
		return structure.getEntityById(getEntityId(index)).getRepresentative().getChainID();
	}
	
	/**
	 * Get the number of chains in the Structure
	 * @return
	 */
	public int getNumChainsInStructure() {
		return structure.getChains().size();
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
	
	public boolean isExhaustiveEnumeration() {
		return exhaustiveEnumeration;
	}
	
	public LatticeGraph<ChainVertex, InterfaceEdge> getLatticeGraph() {
		return latticeGraph;
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
				List<Integer> interfClusterIds0 = new ArrayList<>(GraphUtils.getDistinctInterfaceClusters(arg0.getAssemblyGraph().getSubgraph()));
				List<Integer> interfClusterIds1 = new ArrayList<>(GraphUtils.getDistinctInterfaceClusters(arg1.getAssemblyGraph().getSubgraph()));
				if (interfClusterIds0.size()!=interfClusterIds1.size()) {
					// if different number of interface clusters we put the one with the most clusters first
					return Integer.compare(interfClusterIds1.size(),interfClusterIds0.size());
				}

				for (int i=0;i<interfClusterIds0.size();i++) {
					int id0 = interfClusterIds0.get(i);
					int id1 = interfClusterIds1.get(i);
					
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
		
		PowerSet engagedSet = new PowerSet(GraphUtils.getNumDistinctInterfaces(latticeGraph.getGraph()));
		
		for (int clusterId:interfaceClusterIds) {
			engagedSet.switchOn(clusterId-1);
		}
		
		Assembly a = new Assembly(this, engagedSet);
		
		return a;
	}
	
	public Assembly generateAssembly(Set<Integer> interfaceClusterIds) {
		int[] icIds = new int[interfaceClusterIds.size()];
		int i = 0;
		for (int icId : interfaceClusterIds) {
			icIds[i] = icId;
			i++;
		}
		return generateAssembly(icIds);
	}
	
	public InterfaceEvolContextList getInterfaceEvolContextList() {
		return interfEvolContextList;		
	}
	
	public void setInterfaceEvolContextList(InterfaceEvolContextList interfEvolContextList) {
		this.interfEvolContextList = interfEvolContextList;
	}
	
	/**
	 * Calculates the probabilistic scores for each valid assembly in the crystal,
	 * normalizes the assembly probabilities to account for the 0 probability of 
	 * impossible assemblies and assigns the BIO call to the most probable one.
	 */
	public void score() {

		// this gets each of the unique assembly clusters, represented by the maximal member
		List<Assembly> uniques = getUniqueAssemblies();

		// 1 Do individual assemblies scoring
		for (Assembly a:uniques) {
			a.calcScore();
			a.setCall(CallType.CRYSTAL);
		}
		
		// 2 Compute the sum of probabilities
		double sumProbs = 0;
		double maxScore = 0;
		int maxIndx = 0;
		int indx = 0;
		for (Assembly a:uniques) {
			sumProbs += a.getScore();
			if (maxScore < a.getScore()) {
				maxIndx = indx;
				maxScore = a.getScore();
			}
			indx++;
		}
		
		// This is done when multiple assemblies are high scoring
		indx = 0;
		List<Integer> indices = new ArrayList<Integer>();
		for (Assembly a:uniques) {
			if (a.getScore() == maxScore){
				indices.add(indx);
			}
			indx++;
		}
		
		// Warn if low probability density of valid assemblies
		if (sumProbs < EppicParams.MIN_TOTAL_ASSEMBLY_SCORE) {
			logger.warn("The total probability of valid assemblies is only {}. "
					+ "Assembly ennumeration may be incomplete.", String.format("%.2f", sumProbs));
		} else {
			// Normalize the scores so that they sum up to the total of 1
			for (Assembly a:uniques)
				a.normalizeScore(sumProbs);
		}
		
		// 3 Assign the BIO call to the highest probability
		if (uniques.size() > 0) {
			if (indices.size() == 1) {
				uniques.get(maxIndx).setCall(CallType.BIO);
			} else {
				// if not a unique assembly, choose the lowest stoichiometry
				for (Integer i:indices) {
					if (i == Collections.min(indices))
						uniques.get(i).setCall(CallType.BIO);
					else
						uniques.get(i).setCall(CallType.CRYSTAL);
				}
			}
		}
		
		// 4 Compute call confidence
		for (Assembly a:uniques)
			a.calcConfidence();
	}
}
