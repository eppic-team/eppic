package eppic.assembly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Point3i;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.Compound;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.cycle.PatonCycleBase;
import org.jgrapht.graph.UndirectedSubgraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.EppicParams;

/**
 * An Assembly of molecules within a crystal, represented by a set of engaged interface clusters.
 * 
 * @author jose
 *
 */
public class Assembly {
	
	private static final Logger logger = LoggerFactory.getLogger(Assembly.class);

	/**
	 * The set of engaged interface clusters, represented as a boolean vector.
	 */
	private boolean[] engagedSet;
	
	private Structure structure;
	private StructureInterfaceList interfaces;
	private UndirectedGraph<ChainVertex,InterfaceEdge> graph;
	
	private int totalNumEntities;
	
	private int size;
	private String symmetry;
	private int[] stoichiometry;
	
	private UndirectedGraph<ChainVertex, InterfaceEdge> subgraph;
	
	public Assembly(Structure structure, StructureInterfaceList interfaces,
			UndirectedGraph<ChainVertex,InterfaceEdge> graph, boolean[] engagedSet) {
		this.engagedSet = engagedSet;
		this.structure = structure;
		this.interfaces = interfaces;
		this.graph = graph;
		if (structure==null) {
			this.totalNumEntities = 0;
		} else {
			this.totalNumEntities = structure.getCompounds().size();
		}
		
		this.size = -1;
		this.symmetry = null;
		this.stoichiometry = null;
	}
	
	public Assembly(boolean[] engagedSet) {
		this.engagedSet = engagedSet;
	}
	
	public List<StructureInterfaceCluster> getInterfaceClusters() {
		List<StructureInterfaceCluster> interfaceClusters = new ArrayList<StructureInterfaceCluster>();
		
		for (StructureInterfaceCluster cluster:interfaces.getClusters(EppicParams.CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF)) {
			for (int i=0;i<engagedSet.length;i++) {
				if (engagedSet[i] && cluster.getId() == i+1) {
					interfaceClusters.add(cluster);
				}
			}
		}
		return interfaceClusters;
	}
	
	/**
	 * Return the macromolecular size of this assembly.
	 * @return
	 */
	public int getSize() {
		getStoichiometry(); // lazily initialises stoichiometry var
		size = 0; 
		for (int i=0;i<stoichiometry.length;i++) {
			size += stoichiometry[i];
		}
		return size;
	}
	
	public int getTotalNumEntities() {
		return totalNumEntities;
	}
	
	public UndirectedGraph<ChainVertex,InterfaceEdge> getGraph() {
		return graph;
	}
	
	/**
	 * Gets the stoichiometry as an int array with counts of entities per entity id. 
	 * The indices of the array are the entity ids, i.e. the molecule ids of each Compound, see {@link Compound#getMolId()}
	 * The results are cached so that subsequent calls to this method don't recalculate them.
	 * This will only work correctly on assemblies that have been previously checked
	 * to be valid with {@link #isValid()}, otherwise if the assembly is not isomorphic a warning is logged
	 * @return
	 */
	public int[] getStoichiometry() {
		if (stoichiometry!=null) return stoichiometry;
		
		getSubgraph(); // lazily initialises the subgraph variable 
		
		ConnectivityInspector<ChainVertex, InterfaceEdge> ci = new ConnectivityInspector<ChainVertex, InterfaceEdge>(subgraph);
		
		List<Set<ChainVertex>> ccs = ci.connectedSets();
		
		logger.info("Total of {} connected components", ccs.size());
		List<int[]> stoichiometries = new ArrayList<int[]>();
		for (Set<ChainVertex> cc:ccs) {			
			int [] s = new int[totalNumEntities];
			stoichiometries.add(s);
			for (ChainVertex v:cc) {
				// note: this relies on mol ids in the PDB being 1 to n, that might not be true, we need to check!
				s[v.getEntity()-1]++;
			}			
		}
		
		// we assign the first stoichiometry found, and check and warn if the others are different 
		stoichiometry = stoichiometries.get(0);
		for (int i=1;i<stoichiometries.size();i++) {
			if (!Arrays.equals(stoichiometry, stoichiometries.get(i))) {
				logger.warn("Stoichiometry {} ({}) does not coincide with stoichiometry 0 ({})",
						i, Arrays.toString(stoichiometries.get(i)), Arrays.toString(stoichiometry));
			}
		}
		
		logger.info("Stoichiometry of assembly {} is: {}",toString(), getStoichiometryString());
		return stoichiometry;
	}
	
	/**
	 * Return the stoichiometry string by using entities (actually representative chain ids of each entity)
	 * @return
	 */
	public String getStoichiometryString() {
		StringBuilder stoSb = new StringBuilder();
		getStoichiometry(); // this lazily initialises the stoichiometry variable
		
		for (int i=0;i<stoichiometry.length;i++){
			// note: this relies on mol ids in the PDB being 1 to n, that might not be true, we need to check!
			if (stoichiometry[i]>0) {
				stoSb.append(structure.getCompoundById(i+1).getRepresentative().getChainID());			
				if (stoichiometry[i]>1) stoSb.append(stoichiometry[i]); // for A1B1 we do AB (we ommit 1s)
			}
		}
		return stoSb.toString();
	}
	
	/**
	 * Return the stoichiometry string by using chain ids (composition).
	 * This will only work correctly on assemblies that have been previously checked
	 * to be valid with {@link #isValid()}.
	 * The chain ids will be those of the first connected component found in the subgraph.
	 * @return
	 */
	public String getCompositionString() {
		
		getSubgraph(); // lazily initialises the subgraph variable 
		
		Map<String,Integer> chainIds2Idx = new HashMap<String,Integer>();
		Map<Integer,String> idx2ChainIds = new HashMap<Integer,String>();
		int i = 0;
		for (Chain c:structure.getChains()) {
			chainIds2Idx.put(c.getChainID(),i);
			idx2ChainIds.put(i,c.getChainID());
			i++;
		}
		
		ConnectivityInspector<ChainVertex, InterfaceEdge> ci = new ConnectivityInspector<ChainVertex, InterfaceEdge>(subgraph);

		List<Set<ChainVertex>> ccs = ci.connectedSets();

		List<int[]> compositions = new ArrayList<int[]>();
		for (Set<ChainVertex> cc:ccs) {
			
			int [] s = new int[structure.getChains().size()];
			compositions.add(s);
			for (ChainVertex v:cc) {
				s[chainIds2Idx.get(v.getChainId())]++;
			}			
		}

		// we assume that the assembly has been already checked to be isomorphic, we can just take the first composition found
		
		StringBuilder stoSb = new StringBuilder();
		int[] comp = compositions.get(0);
		for (i=0;i<comp.length;i++){
			if (comp[i]>0) {
				stoSb.append(idx2ChainIds.get(i));			
				if (comp[i]>1) stoSb.append(comp[i]); // for A1B1 we do AB (we ommit 1s)
			}
		}
		logger.info("The composition of assembly {} is {}",this.toString(), stoSb.toString());
		return stoSb.toString();
	}
		
	public int getNumEngagedInterfaceClusters() {
		int count=0;
		for (int i=0;i<engagedSet.length;i++) {
			if (engagedSet[i]) count++;
		}
		return count;
	}
	
	/**
	 * Returns true if this assembly is a child of any of the given parents, false otherwise
	 * @param parents
	 * @return
	 */
	public boolean isChild(List<Assembly> parents) {

		for (Assembly invalidGroup:parents) {
			if (this.isChild(invalidGroup)) return true;
		}
		return false;
	}
	
	/**
	 * Returns true if this assembly is child of the given potentialParent
	 * 
	 * @param potentialParent
	 * @return true if is a child false if not
	 */
	public boolean isChild(Assembly potentialParent) {
		
		for (int i=0;i<this.engagedSet.length;i++) {
			if (potentialParent.engagedSet[i]) {
				if (!this.engagedSet[i]) return false;
			}
		}
		return true;
	}

	/**
	 * Gets all children in assembly tree of this assembly, not adding those that are also
	 * children of other invalidParents
	 * @param invalidParents
	 * @return
	 */
	public List<Assembly> getChildren(List<Assembly> invalidParents) {
		
		List<Assembly> children = new ArrayList<Assembly>();

		for (int i=0;i<this.engagedSet.length;i++) {

			if (!this.engagedSet[i]) {
				boolean[] c = this.engagedSet.clone();
				c[i] = true;
				Assembly a = new Assembly(structure, interfaces, graph, c);
				// first we need to check that this is not a child of another parent already known to be invalid
				if (a.isChild(invalidParents)) continue;
				
				children.add(a);
			}
		}

		return children;
	}
	
	/**
	 * Gets the subgraph containing only this assembly's engaged interface clusters
	 * @param clusterId
	 * @return
	 */
	private UndirectedGraph<ChainVertex, InterfaceEdge> getSubgraph() {
		
		if (subgraph != null) return subgraph; 
		
		// note that the subgraph will contain all vertices even if they are not connected to the rest by any interface
		
		Set<ChainVertex> vertexSet = graph.vertexSet();
		Set<InterfaceEdge> edgeSubset = new HashSet<InterfaceEdge>();
		for(InterfaceEdge edge:graph.edgeSet()) {
			for (int i=0;i<this.engagedSet.length;i++) {
				if (this.engagedSet[i]  && edge.getClusterId()==i+1) {
					edgeSubset.add(edge);
				}
			}
		}
		
		this.subgraph = 
				new UndirectedSubgraph<ChainVertex, InterfaceEdge>(
						graph, vertexSet, edgeSubset);
				
		
		return subgraph;		
	}
	
	/**
	 * Returns true if this Assembly (i.e. this set of engaged interface clusters)
	 * constitutes a valid assembly topologically and isomorphically (rules iii and iv)
	 * @return
	 */
	public boolean isValid() {
		
		getSubgraph(); // initialises subgraph variable
		
		if (!checkSubgraphIsomorphism()) {
			logger.info("Assembly {} contains non-isomorphic subgraphs, discarding it",this.toString());
			return false;
		}
		
		return isClosedSymmetry();
	}
	
	private boolean containsInfinites() {
		for (StructureInterfaceCluster cluster: getInterfaceClusters()) {
			
			for (StructureInterface interf:cluster.getMembers()) {
				// if a single member of cluster is infinite we consider the cluster infinite
				if (interf.isInfinite()) return true;
			}
		}
		return false;
	}

	public boolean isClosedSymmetry() {
		
		// first we check for infinites, like that we save to compute the graph cycles for infinite cases
		if (containsInfinites()) {
			logger.info("Discarding assembly {} because it contains infinite interfaces", toString());
			return false;
		}

		// pre-check for assemblies with 1 engaged interface that is isologous: the cycle detection doesn't work for isologous
		if (getNumEngagedInterfaceClusters()==1) {
			for (StructureInterface interf : getInterfaceClusters().get(0).getMembers()) {
				// with a single interface in cluster isologous, we call the whole isologous
				if (interf.isIsologous()) {
					logger.info("Assembly {} contains just 1 isologous interface cluster: closed symmetry, won't check cycles",toString());
					return true;
				}
			}
		}
		
		// we check the cycles in the graph and whether they stay in same cell
		getSubgraph(); // initialises subgraph variable

		int numVertices = subgraph.vertexSet().size();
		int numEdges = subgraph.edgeSet().size();

		logger.info("Subgraph of assembly {} has {} vertices and {} edges",this.toString(),numVertices, numEdges); 

		// The PatonCycle detection does not work for multigraphs, e.g. in 1pfc engaging interfaces 1,5 it goes in an infinite loop
		// Thus we need to pre-check multi-edges and discard whenever they have non-zero sum translations (which directly invalidates the whole subgraph)
		// If the the subgraph is multi and this check still returns false, we'd have a multigraph to deal with below,
		// but hopefully that doesn't happen (remember we've also removed all duplicate edges from the main graph in any case)
		if (precheckMultiEdges(subgraph)) {
			logger.info("Discarding assembly because some of its multi-edges have non-zero sum translations and thus can't be closed");
			return false;
		}
		
		PatonCycleBase<ChainVertex, InterfaceEdge> paton = new PatonCycleBase<ChainVertex, InterfaceEdge>(subgraph);

		List<List<ChainVertex>> cycles = paton.findCycleBase();
		
		if (cycles.size()==0) {
			// no cycles at all: can't be closed!
			logger.info("No cycles in assembly {}: discarding because it can't be a closed-symmetry", toString());
			return false;
		}
		
		logger.info("{} cycles in total",cycles.size());
		
		for (List<ChainVertex> cycle:cycles) {
			
			StringBuilder sb = new StringBuilder();
			for (ChainVertex c:cycle) {
				sb.append(c.toString()+" -> ");
			}
			logger.info("Cycle of size {}: {}", cycle.size(),sb.toString());
			
			if (isZeroTranslation(subgraph, cycle)) {
				logger.info("Closed cycle (0 translation)");
				// we continue to next cycle, if all cycles are translation 0, then we'll return true below
			} else {
				logger.info("Non-closed cycle (non-0 translation)");
				// one cycle has non-zero translation: we abort straight away: return false
				logger.info("Assembly {} has a non-closed cycle: discarding", toString());
				return false;
			}
		}
		

		logger.info("All cycles of assembly {} are closed: valid assembly",toString());
		return true;
	}
	
	private boolean isZeroTranslation(UndirectedGraph<ChainVertex, InterfaceEdge> subgraph, List<ChainVertex> cycle) {
			
		Point3i p = new Point3i(0,0,0);
		for (int i=0;i<cycle.size();i++) {
			ChainVertex s = cycle.get(i);
			ChainVertex t = cycle.get( (i+1)%cycle.size());
			Set<InterfaceEdge> edges = subgraph.getAllEdges(s,t);
			
			if (edges.isEmpty()) {
				// this should not happen, but there's a bug in jgrapht Paton's implementation
				// where cycles between 2 vertices are reported with a duplicate vertex,
				// e.g. for graph A0=A1 the vertices given as belonging to cycle are A0,A1,A0, when it should be just A0,A1
				logger.warn("Empty list of edges between vertices {},{} belonging to cycle {}",
						s.toString(),t.toString(), cycle.toString());
				continue;
			}
			
			Iterator<InterfaceEdge> edgeIt = edges.iterator();
			InterfaceEdge edge = edgeIt.next();
			Point3i trans = new Point3i(edge.getXtalTrans());
			// this is a way to decide an arbitrary directionality and invert if the directionality is not the right one
			if(!s.equals(subgraph.getEdgeSource(edge))) {
				if (!s.equals(subgraph.getEdgeTarget(edge))) {
					// a sanity check: should not happen unless there is a bug in jgrapht
					logger.warn("Something is wrong: edge {} hasn't got expected vertex source {} or target {}",
							edge.toString(), s.toString(), t.toString());
				}
				trans.negate();
			}

			// Check that any other edges have same xtaltrans
			while (edgeIt.hasNext()) {
				InterfaceEdge edge2 = edgeIt.next();
				Point3i trans2 = new Point3i(edge2.getXtalTrans());
				if(!s.equals(subgraph.getEdgeSource(edge2))) {
					trans2.negate();
				}
				if(!trans.equals(trans2)) {
					logger.info("Multiple edges with unequal translation between vertices {},{} of cycle {}",
							s.toString(),t.toString(),cycle.toString());
					return false;
				}
			}
			
			p.add(trans);
		}
		
		logger.info("Total translation is [{}, {}, {}] ",p.x, p.y, p.z);
		return p.equals(new Point3i(0,0,0));
	}

	/**
	 * Returns true if any of the multi-edges in given subgraph has non-zero sum translations,
	 * false otherwise
	 * @param subgraph
	 * @return
	 */
	private boolean precheckMultiEdges(UndirectedGraph<ChainVertex,InterfaceEdge> subgraph) {
		
		for (InterfaceEdge edge:subgraph.edgeSet()) {
			Set<InterfaceEdge> edges = subgraph.getAllEdges(subgraph.getEdgeSource(edge), subgraph.getEdgeTarget(edge));
			if (edges.size()==1) continue;
			
			Point3i t = new Point3i(0,0,0);
			for (InterfaceEdge e: edges) {
				t.add(e.getXtalTrans());
			}
			if (!t.equals(new Point3i(0,0,0))) {
				logger.info("Vertices {},{} are connected by {} edges with non-0 sum translation: {} ",
						subgraph.getEdgeSource(edge), subgraph.getEdgeTarget(edge), edges.size(), t.toString()); 
				return true; 
			} else {
				logger.warn("Unexpected multi-edge: vertices {},{} are connected by {} edges with 0 sum translation! ",
						subgraph.getEdgeSource(edge), subgraph.getEdgeTarget(edge), edges.size());
			}
		}
		return false;
	}
	
	private boolean checkSubgraphIsomorphism() {
		
		// 1) Isomorphic in entities: i.e. all connected components need to have the same stoichiometry
		
		ConnectivityInspector<ChainVertex, InterfaceEdge> ci = new ConnectivityInspector<ChainVertex, InterfaceEdge>(subgraph);
		
		List<Set<ChainVertex>> ccs = ci.connectedSets();
		
		List<int[]> stoichiometries = new ArrayList<int[]>();
		for (Set<ChainVertex> cc:ccs) {			
			int [] s = new int[totalNumEntities];
			stoichiometries.add(s);
			for (ChainVertex v:cc) {
				// note: this relies on mol ids in the PDB being 1 to n, that might not be true, we need to check!
				s[v.getEntity()-1]++;
			}			
		}
		
		for (int i=1;i<stoichiometries.size();i++) {
			if (!Arrays.equals(stoichiometries.get(0), stoichiometries.get(i))) {
				logger.debug("Stoichiometry {} ({}) does not coincide with stoichiometry 0 ({})",
						i, Arrays.toString(stoichiometries.get(i)), Arrays.toString(stoichiometries.get(0)));
				return false;
			}
		}
		
		// 2) Isomorphic in connectivity
		// TODO implement isomorphism check of graph connectivity
		
		return true;
	}
	
	/**
	 * Return the symmetry string for this Assembly. 
	 * This will only work correctly on assemblies that have been previously checked
	 * to be valid with {@link #isValid()}
	 * @return
	 */
	public String getSymmetry() {
		if (symmetry!=null) return symmetry;
		
		int[] sto = getStoichiometry();
		
		if (totalNumEntities!=1) {
			logger.warn("Symmetry detection for heteromeric assemblies not supported yet, setting it to C1");
			symmetry =  "unknown";
			return symmetry;
		}
		
		int n = sto[0];
		
		// for homomers of size n>1 is clear:
		//  - if n==1: C1
		//  - if n uneven: there should be only 1 interface cluster engaged (otherwise warn!) ==> Cn
		//  - if n==2: there should be only 1 interface cluster engaged (otherwise warn!) ==> C2
		//  - else if n even (n=2m): there can be either 1 or more interface clusters engaged:
		//      if 1: Cn
		//      if >1: Dm
		
		if (n==1) {
			if (getNumEngagedInterfaceClusters()>0) {
				logger.warn("Some interface cluster is engaged for an assembly of size 1. Something is wrong!");
			}
			symmetry = "C1";
		} else if (n%2 != 0 || n==2) {
			if (getNumEngagedInterfaceClusters()>1) {
				logger.warn("More than 1 engaged interface clusters for a homomeric assembly of size {}. Something is wrong!",n);
			}
			symmetry = "C"+n;
		} else { // even number larger than 2
			if (getNumEngagedInterfaceClusters()==1) {
				symmetry = "C"+n;
			} else {
				symmetry = "D"+(n/2);
			}
		}
		
		logger.info("Symmetry of assembly {} is {}",this.toString(),symmetry); 
		// TODO detect tetrahedral, octahedral and icosahedral symmetries
		
		return symmetry;
	}
	
	@Override
	public boolean equals(Object other) {
		if (! (other instanceof Assembly)) return false;
		
		Assembly o = (Assembly) other;
		
		return Arrays.equals(this.engagedSet, o.engagedSet);
		
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.engagedSet);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int numClusters = getNumEngagedInterfaceClusters();
		sb.append("{");
		int e = 0;
		for (int i=0;i<engagedSet.length;i++) {
			if (engagedSet[i]) {
				sb.append(i+1);
				e++;
				if (e!=numClusters) sb.append(",");
			}
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Return the greatest common divisor (GCD, aka greatest common factor GCF) for the given ints
	 * This is an implementation of the Euclidean algorithm:
	 * http://en.wikipedia.org/wiki/Euclidean_algorithm
	 * Thanks to this SO question: 
	 * http://stackoverflow.com/questions/4009198/java-get-greatest-common-divisor
	 * @param a
	 * @param b
	 * @return
	 */
	public static int gcd(int a, int b) {		
		if (b==0) return a;
		return gcd(b,a%b);
	}

	/**
	 * Return the greatest common divisor (GCD) of the given array of ints
	 * @param values
	 * @return
	 */
	public static int gcd(int[] values) {
		if (values.length==0) throw new IllegalArgumentException("Can't calculate GCD for an empty array");
		
		// we go recursively		
		int result = values[0];
		for(int i = 1; i < values.length; i++){
		    result = gcd(result, values[i]);
		}
		return result;
		
	}
}
