package eppic.assembly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.vecmath.Point3i;

import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.cycle.PatonCycleBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jgrapht.graph.UndirectedSubgraph;

public class GraphUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(GraphUtils.class);

	/**
	 * Given a set of edges groups them into interface id or interface cluster id groups
	 * @param edges
	 * @param byClusters if true grouping is by interface cluster ids, if false grouping by interface ids
	 * @return a sorted map of interface ids (or interface cluster ids) to sets of edges with the corresponding id
	 */
	public static <E extends InterfaceEdgeInterface> SortedMap<Integer,Set<E>> groupIntoTypes(Set<E> edges, boolean byClusters) {
		SortedMap<Integer,Set<E>> map = new TreeMap<>();

		for (E edge:edges) {
			int id = -1;
			if (byClusters) id = edge.getClusterId();
			else 			id = edge.getInterfaceId();
			
			Set<E> set = null;
			if (!map.containsKey(id)) {
				set = new HashSet<>();
				map.put(id, set);
			} else {
				set = map.get(id);
			}
			set.add(edge);

		}
		return map;
	}
	
	/**
	 * Returns the multiplicity of the given interface cluster id in the given graph,
	 * i.e. how many times an edge with that interface cluster id is present in the given graph
	 * @param g
	 * @param interfClusterId
	 * @return
	 */
	public static <V extends ChainVertexInterface,E extends InterfaceEdgeInterface> int getEdgeMultiplicity(UndirectedGraph<V, E> g, int interfClusterId) {
		int count = 0;
		for (E edge:g.edgeSet()) {
			if (edge.getClusterId() == interfClusterId) count++;
		}
		return count;
	}
	
	/**
	 * For each of the given interface clusters, finds out their multiplicity in the given graph.
	 * @param sto
	 * @return
	 */
	public static <V extends ChainVertexInterface,E extends InterfaceEdgeInterface> int[] getMultiplicities(List<StructureInterfaceCluster> interfaceClusters, UndirectedGraph<V,E> g) {
		
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
	public static <V extends ChainVertexInterface,E extends InterfaceEdgeInterface> TreeMap<Integer,Integer> getMultiplicities(UndirectedGraph<V, E> g) {
		TreeMap<Integer,Integer> counts = new TreeMap<Integer,Integer>();
		
		for (E e:g.edgeSet()) {
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
	public static <V extends ChainVertexInterface,E extends InterfaceEdgeInterface> TreeMap<Integer,Integer> getCycleMultiplicities(UndirectedGraph<V, E> g) {
		
		TreeSet<Integer> interfaceClusterIds = new TreeSet<Integer>();
		TreeMap<Integer,Integer> counts = new TreeMap<Integer,Integer>();
				
		
		for (E e:g.edgeSet()) {
			interfaceClusterIds.add(e.getClusterId());
		}
		
		for (int interfaceClusterId:interfaceClusterIds) {
			UndirectedGraph<V, E> singleInterfClusterG = 
					getSubgraphWithSingleInterfaceCluster(g, interfaceClusterId);

			PatonCycleBase<V, E> paton = new PatonCycleBase<V, E>(singleInterfClusterG);

			List<List<V>> cycles = paton.findCycleBase();
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
	
	public static <V extends ChainVertexInterface, E extends InterfaceEdgeInterface> UndirectedGraph<V, E> getSubgraphWithSingleInterfaceCluster(
			UndirectedGraph<V, E> g, int interfaceClusterId) {

		
		Set<E> edges = new HashSet<>();
		
		for (E e:g.edgeSet()) {
			if (e.getClusterId() == interfaceClusterId) edges.add(e);
		}
		
		// we create the subgraph with a single engaged interface cluster
		return new UndirectedSubgraph<V, E>(g, g.vertexSet(), edges);
	}
	
	/**
	 * Count the number of distinct interface clusters in the given graph by looking at the number
	 * of distinct interface cluster ids
	 * @param g
	 * @return
	 */
	public static <V extends ChainVertexInterface,E extends InterfaceEdgeInterface> int getNumDistinctInterfaces(UndirectedGraph<V, E> g) {
		Set<Integer> interfClusterIds = new HashSet<Integer>();
		for (E e:g.edgeSet()) {
			interfClusterIds.add(e.getClusterId());
		}
		return interfClusterIds.size();
	}
	
	/**
	 * Get the sorted set of distinct interface cluster ids in the given graph.
	 * @param g
	 * @return
	 */
	public static <V extends ChainVertexInterface, E extends InterfaceEdgeInterface> SortedSet<Integer> getDistinctInterfaceClusters(UndirectedGraph<V,E> g) {
		SortedSet<Integer> interfClusterIds = new TreeSet<Integer>();
		for (E e:g.edgeSet()) {
			interfClusterIds.add(e.getClusterId());
		}
		return interfClusterIds;
	}
	
	/**
	 * Get the number of distinct entities in the given graph by looking at the number of
	 * distinct entity ids. 
	 * @return
	 */
	public static <V extends ChainVertexInterface, E extends InterfaceEdgeInterface> int getNumDistinctEntities(UndirectedGraph<V,E> g) {
		
		Set<Integer> set = new HashSet<>();
		for (V vertex: g.vertexSet()) {
			set.add(vertex.getEntityId());
		}
					
		return set.size();
	}
	
	/**
	 * Get the sorted set of distinct entity ids in the given graph.
	 * @return
	 */
	public static <V extends ChainVertexInterface, E extends InterfaceEdgeInterface> SortedSet<Integer> getDistinctEntities(UndirectedGraph<V,E> g) {
		SortedSet<Integer> set = new TreeSet<>();
		for (V vertex: g.vertexSet()) {
			set.add(vertex.getEntityId());
		}
					
		return set;
	}
	
	/**
	 * Get the interface cluster id corresponding to the largest interface cluster present in given graph
	 * @param g
	 * @return the largest heteromeric interface cluster id, or -1 if none found
	 */
	public static <V extends ChainVertexInterface,E extends InterfaceEdgeInterface> int getLargestHeteroInterfaceCluster(UndirectedGraph<V, E> g) {
		TreeSet<Integer> clusterIds = new TreeSet<Integer>();
		for (E e:g.edgeSet()) {
			
			V s = g.getEdgeSource(e);
			V t = g.getEdgeTarget(e);
			
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
	public static <V extends ChainVertexInterface,E extends InterfaceEdgeInterface> boolean isAutomorphic(UndirectedGraph<V, E> g) {
		
		
		// we'll store in a map each of the first vertex types seen with their content in terms of interface cluster ids
		Map<Integer, Map<Integer,Integer>> repVs = new HashMap<>();
		
		// go through all vertices
		for (V v: g.vertexSet()) {
			
			
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
	
	private static <V extends ChainVertexInterface,E extends InterfaceEdgeInterface> Map<Integer,Integer> getInterfaceClusterIdsForVertex(UndirectedGraph<V, E> g, V v) {
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
	
	/**
	 * Returns a string representation of the graph with one edge per line.
	 * Edges are sorted according to their interface ids.
	 * @param g
	 * @return
	 */
	public static <V extends ChainVertexInterface,E extends InterfaceEdgeInterface> String asString(UndirectedGraph<V, E> g) {
		List<E> sortedEdges = new ArrayList<E>();
		sortedEdges.addAll(g.edgeSet());
		Collections.sort(sortedEdges, new Comparator<E>() {
			@Override
			public int compare(E o1, E o2) {
				return new Integer(o1.getInterfaceId()).compareTo(new Integer(o2.getInterfaceId()));
			}			
		});

		StringBuilder sb = new StringBuilder();
		
		for (E edge:sortedEdges) {
			V first = g.getEdgeSource(edge);
			V second = g.getEdgeTarget(edge);
			Point3i xtalT = edge.getXtalTrans();
			
			sb.append(String.format("Edge %d (%d) between %s (%d) - %s (%d) [%2d,%2d,%2d]\n", 
					edge.getInterfaceId(),
					edge.getClusterId(),
					first.getChainId()+first.getOpId(), 
					first.getEntityId(),
					second.getChainId()+second.getOpId(),
					second.getEntityId(),
					xtalT.x,xtalT.y,xtalT.z));
			

		}
		
		return sb.toString();

	}
	
	public static <V extends ChainVertexInterface,E extends InterfaceEdgeInterface> String toDot(UndirectedGraph<V, E> g, String title) {
//		// Run with dot -Tpng -Kneato -O -n2 dotfile
//		digraph "{{title}}" {
//		{{#graph2D.vertexSet}}
//			"{{toString}}" [ pos="{{center.x}},{{center.y}}"{{#colorStr}} color="#{{colorStr}}" fillcolor="#{{colorStr}}"{{/colorStr}}]
//		{{/graph2D.vertexSet}}
//
//		{{#graph2D.edgeSet}}
//			"{{source}}" -> "{{target}}" {{#colorStr}}[ color="#{{colorStr}}" ]{{/colorStr}}
//		{{/graph2D.edgeSet}}
//	}
		StringBuilder str = new StringBuilder()
				.append(String.format("digraph \"%s\" {%n", title));
		
		for(E e : g.edgeSet()) {
			V s = g.getEdgeSource(e);
			V t = g.getEdgeTarget(e);
			str.append(String.format("\"%s\" -> \"%s\" [label=\"%s %s\"]%n", s, t, e.toString(), e.getXtalTransString() ));
		}
		
		str.append(String.format("}%n"));
		return str.toString();
	}
}
