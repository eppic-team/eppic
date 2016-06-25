package eppic.assembly;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.cycle.PatonCycleBase;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jgrapht.graph.UndirectedSubgraph;

public class GraphUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(GraphUtils.class);

	/**
	 * Given a set of edges groups them into interface id or interface cluster id groups
	 * @param edges
	 * @param byClusters if true grouping is by interface cluster ids, if false grouping by interface ids
	 * @return a map of interface ids (or interface cluster ids) to sets of edges with the corresponding id
	 */
	public static Map<Integer,Set<InterfaceEdge>> groupIntoTypes(Set<InterfaceEdge> edges, boolean byClusters) {
		Map<Integer,Set<InterfaceEdge>> map = new HashMap<Integer,Set<InterfaceEdge>>();

		for (InterfaceEdge edge:edges) {
			int id = -1;
			if (byClusters) id = edge.getClusterId();
			else 			id = edge.getInterfaceId();
			
			Set<InterfaceEdge> set = null;
			if (!map.containsKey(id)) {
				set = new HashSet<InterfaceEdge>();
				map.put(id, set);
			} else {
				set = map.get(id);
			}
			set.add(edge);

		}
		return map;
	}

	/**
	 * Copies the given Graph to a new Graph with same vertices and edges.
	 * The vertices and edges are the same references as the original Graph.  
	 * @param g
	 * @return
	 */
	public static UndirectedGraph<ChainVertex, InterfaceEdge> copyGraph(UndirectedGraph<ChainVertex, InterfaceEdge> g) {
		
		//if (! (g instanceof Pseudograph)) throw new IllegalArgumentException("Given graph is not a pseudograph!");
		
		UndirectedGraph<ChainVertex, InterfaceEdge> og = new Pseudograph<ChainVertex, InterfaceEdge>(InterfaceEdge.class);
		
		for (ChainVertex v:g.vertexSet()) {
			og.addVertex(v);
		}
		
		for (InterfaceEdge e:g.edgeSet()) {
			og.addEdge(g.getEdgeSource(e), g.getEdgeTarget(e), e);
		}
		
		return og;
	}
	
	/**
	 * Returns the multiplicity of the given interface cluster id in the given graph,
	 * i.e. how many times an edge with that interface cluster id is present in the given graph
	 * @param g
	 * @param interfClusterId
	 * @return
	 */
	public static int getEdgeMultiplicity(UndirectedGraph<ChainVertex, InterfaceEdge> g, int interfClusterId) {
		int count = 0;
		for (InterfaceEdge edge:g.edgeSet()) {
			if (edge.getClusterId() == interfClusterId) count++;
		}
		return count;
	}
	
	/**
	 * For each of the given interface clusters, finds out their multiplicity in the given graph.
	 * @param sto
	 * @return
	 */
	public static int[] getMultiplicities(List<StructureInterfaceCluster> interfaceClusters, UndirectedGraph<ChainVertex,InterfaceEdge> g) {
		
		int[] mult = new int[interfaceClusters.size()];
		
		int i = 0;
		for (StructureInterfaceCluster interfCluster:interfaceClusters) {
			
			mult[i] = getEdgeMultiplicity(g, interfCluster.getId());
			i++;
		}
		
		return mult;
	}
	
	/**
	 * Get the multiplicities of all interface clusters present in the given graph
	 * @param g
	 * @return a map of interface cluster ids to interface cluster count
	 */
	public static TreeMap<Integer,Integer> getMultiplicities(UndirectedGraph<ChainVertex, InterfaceEdge> g) {
		TreeMap<Integer,Integer> counts = new TreeMap<Integer,Integer>();
		
		for (InterfaceEdge e:g.edgeSet()) {
			if (counts.containsKey(e.getClusterId())) {
				counts.put(e.getClusterId(), counts.get(e.getClusterId())+1);
			} else {
				counts.put(e.getClusterId(), 1);
			}
		}
		
		return counts;
	}
	
	/**
	 * For each interface cluster id present in the given graph, find out the cycle size for
	 * a subgraph containing only each interface cluster id
	 * @param g
	 * @return a map of interface cluster ids to cycle sizes
	 */
	public static TreeMap<Integer,Integer> getCycleMultiplicities(UndirectedGraph<ChainVertex, InterfaceEdge> g) {
		
		TreeSet<Integer> interfaceClusterIds = new TreeSet<Integer>();
		TreeMap<Integer,Integer> counts = new TreeMap<Integer,Integer>();
				
		
		for (InterfaceEdge e:g.edgeSet()) {
			interfaceClusterIds.add(e.getClusterId());
		}
		
		for (int interfaceClusterId:interfaceClusterIds) {
			UndirectedGraph<ChainVertex, InterfaceEdge> singleInterfClusterG = 
					getSubgraphWithSingleInterfaceCluster(g, interfaceClusterId);

			PatonCycleBase<ChainVertex, InterfaceEdge> paton = new PatonCycleBase<ChainVertex, InterfaceEdge>(singleInterfClusterG);

			List<List<ChainVertex>> cycles = paton.findCycleBase();
			if (cycles.size()==0) {
				counts.put(interfaceClusterId, 0);
			} else if (cycles.size()==1) {
				counts.put(interfaceClusterId, cycles.get(0).size());
			} else {
				int count = cycles.get(0).size();
				for (int i=1;i<cycles.size();i++) {
					if (cycles.get(i).size()!=count) 
						logger.warn("Found {} cycle bases in subgraph with single interface cluster {} with different sizes ({} and {}). Will use only first size",
								cycles.size(), interfaceClusterId, count, cycles.get(i).size());
				}
				counts.put(interfaceClusterId, cycles.get(0).size());
			}
		}

		
		return counts;
	}
	
	public static UndirectedGraph<ChainVertex, InterfaceEdge> getSubgraphWithSingleInterfaceCluster(
			UndirectedGraph<ChainVertex, InterfaceEdge> g, int interfaceClusterId) {

		
		Set<InterfaceEdge> edges = new HashSet<InterfaceEdge>();
		
		for (InterfaceEdge e:g.edgeSet()) {
			if (e.getClusterId() == interfaceClusterId) edges.add(e);
		}
		
		// we create the subgraph with a single engaged interface cluster
		return new UndirectedSubgraph<ChainVertex, InterfaceEdge>(g, g.vertexSet(), edges);
	}
	
	/**
	 * Count the number of distinct interface clusters in the given graph via looking at the number
	 * of distinct interface cluster ids
	 * @param g
	 * @return
	 */
	public static int getDistinctInterfaceCount(UndirectedGraph<ChainVertex, InterfaceEdge> g) {
		Set<Integer> interfClusterIds = new HashSet<Integer>();
		for (InterfaceEdge e:g.edgeSet()) {
			interfClusterIds.add(e.getClusterId());
		}
		return interfClusterIds.size();
	}
	
	/**
	 * Get the interface cluster id corresponding to the largest interface cluster present in given graph
	 * @param g
	 * @return the largest heteromeric interface cluster id, or -1 if none found
	 */
	public static int getLargestHeteroInterfaceCluster(UndirectedGraph<ChainVertex, InterfaceEdge> g) {
		TreeSet<Integer> clusterIds = new TreeSet<Integer>();
		for (InterfaceEdge e:g.edgeSet()) {
			
			ChainVertex s = g.getEdgeSource(e);
			ChainVertex t = g.getEdgeTarget(e);
			
			if (s.getEntityId() != t.getEntityId()) { // i.e. heteromeric
				clusterIds.add(e.getClusterId());
			}
		}
		
		if (clusterIds.isEmpty()) return -1;
		
		return clusterIds.first();
	}
	
	/**
	 * Checks that the given graph is automorphic in terms of entities and interface clusters.
	 * i.e. if every vertex of entity i has the same number and type of edges (interface cluster ids) 
	 * that any other vertex with entity i
	 * @param g
	 * @return
	 */
	public static boolean isAutomorphic(UndirectedGraph<ChainVertexInterface, InterfaceEdgeInterface> g) {
		
		
		// we'll store in a map each of the first vertex types seen with their content in terms of interface cluster ids
		Map<Integer, Map<Integer,Integer>> repVs = new HashMap<>();
		
		// go through all vertices
		for (ChainVertexInterface v: g.vertexSet()) {
			
			
			if (!repVs.containsKey(v.getEntityId())) {
				// this kind of entity wasn't seen yet, first of the kind will be the representative
				repVs.put(v.getEntityId(), getInterfaceClusterIdsForVertex(g, v));
			} else {
				// we already have a representative for this kind, let's check it has the same content
				Map<Integer,Integer> content = getInterfaceClusterIdsForVertex(g, v);
				Map<Integer,Integer> repContent = repVs.get(v.getEntityId());
				if (repContent.size() != content.size())
					// the sizes (number of distinct interface cluster ids) doesn't coincide, can't be automorphic
					return false;
				
				for (Entry<Integer, Integer> entry : repContent.entrySet()) {
					
					Integer count = content.get(entry.getKey());
					if (count == null) 
						// we don't have the interface cluster id in the list of edges: can't be automorphic
						return false;
					
					if (count != entry.getValue()) 
						// the count for the intercace cluster id doesn't coincide, can't be automorphic
						return false;
				}
			}
		}
		
		return true;
	}
	
	private static Map<Integer,Integer> getInterfaceClusterIdsForVertex(UndirectedGraph<ChainVertexInterface, InterfaceEdgeInterface> g, ChainVertexInterface v) {
		Map<Integer,Integer> set = new HashMap<>();
		for (InterfaceEdgeInterface e:g.edgesOf(v)) {
			if (set.containsKey(e.getClusterId())) {
				set.put(e.getClusterId(), set.get(e.getClusterId()) + 1 );
			} else {
				set.put(e.getClusterId(), 1);	
			}							
		}
		return set;
	}
	
}
