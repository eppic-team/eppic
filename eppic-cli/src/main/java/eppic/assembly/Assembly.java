package eppic.assembly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.vecmath.Point3i;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.cycle.PatonCycleBase;
import org.jgrapht.graph.UndirectedSubgraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
	private List<StructureInterfaceCluster> interfaceClusters;
	private UndirectedGraph<ChainVertex, InterfaceEdge> graph;
	
	private StoichiometrySet stoichiometrySet;
	
	private UndirectedGraph<ChainVertex, InterfaceEdge> subgraph;
	private List<Set<ChainVertex>> connectedComponents;
	
	
	public Assembly(Structure structure, StructureInterfaceList interfaces, List<StructureInterfaceCluster> interfaceClusters,
			UndirectedGraph<ChainVertex,InterfaceEdge> graph, boolean[] engagedSet) {
		this.engagedSet = engagedSet;
		this.structure = structure;
		this.interfaces = interfaces;
		this.interfaceClusters = interfaceClusters;
		this.graph = graph;
				
		if (graph!=null) {
			initSubgraph(); // inits subgraph and connectedComponents
			this.stoichiometrySet = new StoichiometrySet(structure, this, connectedComponents);
		} 
	}
	
	public Assembly(boolean[] engagedSet) {
		this.engagedSet = engagedSet;
	}
	
	public List<StructureInterfaceCluster> getEngagedInterfaceClusters() {
		List<StructureInterfaceCluster> engagedInterfaceClusters = new ArrayList<StructureInterfaceCluster>();
		
		for (StructureInterfaceCluster cluster:interfaceClusters) {
			for (int i=0;i<engagedSet.length;i++) {
				if (engagedSet[i] && cluster.getId() == i+1) {
					engagedInterfaceClusters.add(cluster);
				}
			}
		}
		return engagedInterfaceClusters;
	}
		
	public int getNumEngagedInterfaceClusters() {
		int count=0;
		for (int i=0;i<engagedSet.length;i++) {
			if (engagedSet[i]) count++;
		}
		return count;
	}
	
	public int getNumHomoEngagedInterfaceClusters() {
		int count=0;
		for (StructureInterfaceCluster cluster: getEngagedInterfaceClusters()) {
			if (cluster.getMembers().get(0).isHomomeric()) {
				count++;
			}
		}
		return count;
	}
	
	public int getNumHeteroEngagedInterfaceClusters() {
		int count=0;
		for (StructureInterfaceCluster cluster: getEngagedInterfaceClusters()) {
			if (!cluster.getMembers().get(0).isHomomeric()) {
				count++;
			}
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
				Assembly a = new Assembly(structure, interfaces, interfaceClusters, graph, c);
				// first we need to check that this is not a child of another parent already known to be invalid
				if (a.isChild(invalidParents)) continue;
				
				children.add(a);
			}
		}

		return children;
	}
	
	/**
	 * Initialises the subgraph containing only this assembly's engaged interface clusters.
	 * This initialises both the subgraph and connectedComponents members
	 * @param clusterId
	 * @return
	 */
	private void initSubgraph() {		
		
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
		 

		
		// initialising also the connected components
		ConnectivityInspector<ChainVertex, InterfaceEdge> ci = new ConnectivityInspector<ChainVertex, InterfaceEdge>(subgraph);
		
		this.connectedComponents = ci.connectedSets();
		
		logger.info("Subgraph of assembly {} has {} vertices and {} edges, with {} connected components",
				this.toString(), subgraph.vertexSet().size(), subgraph.edgeSet().size(), connectedComponents.size());
		
		StringBuilder sb = new StringBuilder();
		for (Set<ChainVertex> cc:connectedComponents) {
			sb.append(cc.size());
			sb.append(' ');
		}
		logger.info("Connected component sizes: {}",sb.toString()); 
	
				
	}
	
	/**
	 * Returns true if this Assembly (i.e. this set of engaged interface clusters)
	 * constitutes a valid assembly topologically and isomorphically (rules iii and iv)
	 * @return
	 */
	public boolean isValid() {
		
		if (!isIsomorphic()) {
			logger.info("Assembly {} contains non-isomorphic subgraphs, discarding it",this.toString());
			return false;
		}
		
		return isClosedSymmetry();
	}
	
	private boolean containsInfinites() {
		for (StructureInterfaceCluster cluster: getEngagedInterfaceClusters()) {
			
			for (StructureInterface interf:cluster.getMembers()) {
				// if a single member of cluster is infinite we consider the cluster infinite
				if (interf.isInfinite()) return true;
			}
		}
		return false;
	}

	private boolean containsHeteromeric() {
		for (StructureInterfaceCluster cluster: getEngagedInterfaceClusters()) {
			
			for (StructureInterface interf:cluster.getMembers()) {
				if (!interf.isHomomeric()) return true;
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
			for (StructureInterface interf : getEngagedInterfaceClusters().get(0).getMembers()) {
				// with a single interface in cluster isologous, we call the whole isologous
				if (interf.isIsologous()) {
					logger.info("Assembly {} contains just 1 isologous interface cluster: closed symmetry, won't check cycles",toString());
					return true;
				}
			}
			
		}
		
		// for heteromeric assemblies, uneven stoichiometries implies non-closed. We can discard uneven ones straight away
		if (!stoichiometrySet.isEven()) {
			logger.info("Uneven stoichiometry for assembly {}, can't be a closed symmetry. Discarding",toString());
			return false;
		}
		
		// we check the cycles in the graph and whether they stay in same cell

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
			// no cycles at all:
			// heteromeric interfaces are ok
			if (containsHeteromeric()) {
				logger.info("Assembly {} contains heteromeric interfaces and no cycles, assuming it has closed-symmetry",toString());
				return true;
			}
			// homomeric aren't
			// homomeric interfaces and no cycles: can't be closed!
			logger.info("No cycles in assembly {}: discarding because it can't be a closed-symmetry", toString());
			return false;
		}
		
		logger.info("{} cycles in total",cycles.size());
		
		for (List<ChainVertex> cycle:cycles) {
			
			StringBuilder sb = new StringBuilder();
			for (ChainVertex c:cycle) {
				sb.append(c.toString()+" -> ");
			}
			logger.debug("Cycle of size {}: {}", cycle.size(),sb.toString());
			
			if (isZeroTranslation(subgraph, cycle)) {
				logger.debug("Closed cycle (0 translation)");
				// we continue to next cycle, if all cycles are translation 0, then we'll return true below
			} else {
				// one cycle has non-zero translation: we abort straight away: return false
				logger.info("Non-closed cycle (non-0 translation). Discarding assembly {}",toString());
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
	
	/**
	 * Checks whether this Assembly is isomorphic, that is if all the connected components of its subgraph
	 * are isomorphic graphs in terms of vertex labels (i.e. they have the same stoichiometries) and edge labels
	 * @return
	 */
	public boolean isIsomorphic() {
		
		
		// 1) Isomorphism of entities: they have to be all equals or if different then they must be orthogonal 
		
		if (!stoichiometrySet.isIsomorphic()) {
			logger.info("Some stoichiometries of assembly {} are overlapping, assembly can't be isomorphic",this.toString());
			return false;
		}
		
		// 2) Isomorphic in edge types: the count of edges per interface cluster type should be the same for all connected components
		
		// TODO implement
		
		//List<int[]> edgeStoichiometries = new ArrayList<int[]>();
		//for (Set<ChainVertex> connectedComponents) {
		//	int[] edgeStoichiometry = new int[engagedSet.length];
		//	edgeStoichiometries.add(edgeStoichiometry);
		//	for (InterfaceEdge edge:cc.edgeSet()) {
		//		edgeStoichiometry[edge.getClusterId()-1]++;
		//	}
		//}
		
		
		// 3) Isomorphic in connectivity
		// TODO implement isomorphism check of graph connectivity
		
		return true;
	}
	
	/**
	 * Returns the description corresponding to this Assembly as a list 
	 * of AssemblyDescriptions per disjoint set,
	 * e.g. in a crystal with 2 entities A,B and no engaged interfaces, 
	 * this would return a List of size 2:
	 * - AssemblyDescription 1: size 1, stoichiometry A, symmetry C1
	 * - AssemblyDescription 2: size 1, stoichiometry B, symmetry C1
	 * The same crystal where both A and B form a dimer would return a List of size 1
	 * with an AssemblyDescription: size 2, stoichiometry AB, symmetry C1
	 * @return
	 */
	public List<AssemblyDescription> getDescription() {
		List<AssemblyDescription> list = this.stoichiometrySet.getDescription();
		StringBuilder sb = new StringBuilder();
		int i = -1;
		for (AssemblyDescription ad:list) {
			i++;
			sb.append(ad.getSize()+"/"+ad.getStoichiometry()+"/"+ad.getSymmetry());
			if (i!=list.size()-1) sb.append(",");
		}
		logger.info("Assembly {} size/stoichometry/symmetry: {}",toString(),sb.toString()); 
		return list;
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

}
