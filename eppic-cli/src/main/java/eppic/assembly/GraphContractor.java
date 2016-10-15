package eppic.assembly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.vecmath.Point3i;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GraphContractor<V extends ChainVertexInterface, E extends InterfaceEdgeInterface> {
	
	private static final Logger logger = LoggerFactory.getLogger(GraphContractor.class);

	/**
	 * The original graph
	 */
	private UndirectedGraph<V, E> g;
	
	/**
	 * The contracted graph
	 */
	private UndirectedGraph<V, E> cg;
	
	private Map<V,V> contractedVertices;
	
	public GraphContractor(UndirectedGraph<V, E> g) {
		this.g = g;
		this.contractedVertices = new HashMap<V, V>();
	}
	
	/**
	 * Contract all edges in given interfClusterId.
	 * Only one of the 2 vertices of each removed edge is kept (always the one corresponding
	 * to a certain arbitrary reference entity id). The edges belonging to the removed vertex are
	 * then attached to the remaining vertex.
	 * @param inputGraph
	 * @param interfClusterId 
	 * @param edgeClass
	 * @return
	 */
	private UndirectedGraph<V, E> contractInterfaceCluster(
			UndirectedGraph<V, E> inputGraph, int interfClusterId, Class<? extends E> edgeClass) {
		
		// let's first gather the edges to remove: all of them correspond to a single interface cluster and thus 
		// source and target nodes will be always the same 2 entities
		Set<E> toRemove = getEdgesWithInterfClusterId(inputGraph, interfClusterId);

		// shallow copying of the graph
		UndirectedGraph<V, E> contGraph = new Pseudograph<>(edgeClass);
		Graphs.addGraph(contGraph, inputGraph);

		int referenceEntityId = -1;

		for (E e:toRemove) {

			logger.debug("Removing edge {}", e.toString());

			V s = inputGraph.getEdgeSource(e);			
			V t = inputGraph.getEdgeTarget(e);			

			if (s.getEntityId()<0) logger.error("Entity id for vertex {} is negative!",s.getEntityId());

			if (referenceEntityId<0) {
				referenceEntityId = s.getEntityId();
				logger.debug("Chose reference entity id {}. Vertices that have this entity id will be kept.", referenceEntityId); 
			}

			// we will keep the vertices matching referenceEntityId
			V vToRemove = null;
			V vToKeep = null;
			if (s.getEntityId() == referenceEntityId) {
				vToRemove = t;
				vToKeep = s;
			} else if (t.getEntityId() == referenceEntityId) {
				vToRemove = s;
				vToKeep = t;
			} else {
				logger.warn("Neither vertex matched entity id {}. Something is wrong!",referenceEntityId);
			}

			logger.debug("Graph has {} vertices and {} edges, before removing anything", contGraph.vertexSet().size(), contGraph.edgeSet().size());

			contGraph.removeVertex(vToRemove);
			
			contractedVertices.put(vToRemove, vToKeep);

			logger.debug("Graph has {} vertices and {} edges, before add edges loop", contGraph.vertexSet().size(), contGraph.edgeSet().size());

			// we need to add to vToKeep all edges that were connecting vToRemove to any other vertex 
			for (E eToAdd : inputGraph.edgesOf(vToRemove)) {				

				if (eToAdd == e) {
					logger.debug("Won't add the joining edge {} as a self-edge", e);
					continue;
				}

				boolean invert = false;
				V target = null;
				if (vToRemove.equals(inputGraph.getEdgeSource(eToAdd))) {
					target = inputGraph.getEdgeTarget(eToAdd);
				} else if (vToRemove.equals(inputGraph.getEdgeTarget(eToAdd))) {
					target = inputGraph.getEdgeSource(eToAdd);
					invert = true;
				} else {
					logger.error("vToRemove is neither source nor target");
					continue;
				}

				if (!contGraph.containsVertex(target)) {
					logger.debug("Vertex {}, needed to add new edge {} is not in graph, replacing it by its contracted vertex {}", 
							target, eToAdd, contractedVertices.get(target));
					target = contractedVertices.get(target);
				}

				// we make sure we put them back in the same direction as we encountered them
				logger.debug("Adding edge {} between {} {} {}. Before it was {}-{}", 
						eToAdd.toString(), vToKeep.toString(), ( invert?"<-":"->" ), target.toString(),vToRemove.toString(),target.toString());
				if (!invert) {
					Point3i newTrans = new Point3i(eToAdd.getXtalTrans());
					newTrans.sub(e.getXtalTrans());
					eToAdd.setXtalTrans(newTrans);
					contGraph.addEdge(vToKeep, target, eToAdd);
				} else {
					Point3i newTrans = new Point3i(e.getXtalTrans());
					newTrans.sub(eToAdd.getXtalTrans());
					eToAdd.setXtalTrans(newTrans);					
					contGraph.addEdge(target, vToKeep, eToAdd);
				}



				logger.debug("Graph has {} vertices and {} edges", contGraph.vertexSet().size(), contGraph.edgeSet().size());
			}
		}

		// let's now remove duplicated edges
		trim(contGraph);

		// logging the entity counts
		if (logger.isDebugEnabled()) {
			
			Map<Integer, Integer> counts = getEntityCounts(contGraph);
			
			logger.debug("Counts per entities of contracted graph:");
			for (Entry<Integer, Integer> entry:counts.entrySet()) {
				logger.debug("  entity {} -> {} vertices", entry.getKey(), entry.getValue());
			}
			
			
		}
		
		return contGraph;
	}
	
	/**
	 * Contract the graph iteratively using the largest heteromeric interface in each iteration,
	 * in order to create a pseudo-homomeric graph.
	 * Contracted vertices can be obtained subsequently with {@link #getContractedVertices()} and their 
	 * replacements by {@link #getContractedVertex(V)} 
	 * @return the contracted graph, also accessible via {@link #getContractedGraph()}
	 * @param edgeClass
	 */
	public UndirectedGraph<V, E> contract(Class<? extends E> edgeClass) {
			
		// for first iteration
		int interfClusterId = GraphUtils.getLargestHeteroInterfaceCluster(g);
		
		cg = g;

		int i = 0;
		while (true) {
			i++;
			logger.info("Round {} of contraction: contracting interface cluster {}",i,interfClusterId);
			logger.debug("Starting graph before contraction has {} vertices and {} edges",cg.vertexSet().size(),cg.edgeSet().size());
			// logging the entity counts
			if (logger.isDebugEnabled()) {
				
				Map<Integer, Integer> counts = getEntityCounts(cg);
				
				logger.debug("Counts per entities of starting graph:");
				for (Entry<Integer, Integer> entry:counts.entrySet()) {
					logger.debug("  entity {} -> {} vertices", entry.getKey(), entry.getValue());
				}
				
				
			}

			
			cg = contractInterfaceCluster(cg, interfClusterId, edgeClass);
			
			// we get the interfClusterId for next iteration
			interfClusterId = GraphUtils.getLargestHeteroInterfaceCluster(cg);
			
			// if no more heteromeric interfaces we break: end of iteration
			if (interfClusterId == -1) break;
		}
		
		logger.debug("Final contracted graph ({} vertices, {} edges):\n{}", cg.vertexSet().size(), cg.edgeSet().size(),
				GraphUtils.asString(cg));

		
		return cg;
	}

	public UndirectedGraph<V, E> getOriginalGraph() {
		return g;
	}
	
	public UndirectedGraph<V, E> getContractedGraph() {
		return cg;
	}
	
	/**
	 * Returns the replacement vertex given a contracted vertex
	 * @param v
	 * @return
	 */
	public V getContractedVertex(V v) {
		return contractedVertices.get(v);
	}
	
	/**
	 * Returns the set of contracted vertices
	 * @return
	 */
	public Set<V> getContractedVertices() {
		return contractedVertices.keySet();
	}
	
	public Set<Integer> getContractedEntityIds() {
		Set<Integer> set = new TreeSet<Integer>();
		
		for (V v: contractedVertices.keySet()) {
			set.add(v.getEntityId());
		}
		return set;
	}
	
	/**
	 * Eliminates duplicate edges in the given graph: for any 2 nodes joined by more than 1 edge of the same type (interface cluster id)
	 * only 1 edge per type is kept. Also eliminates all loop edges (of a node to itself). 
	 * 
	 * @param contGraph
	 */
	private static <V extends ChainVertexInterface, E extends InterfaceEdgeInterface> void trim(UndirectedGraph<V, E> contGraph) {
		
		Set<E> toRemove = new HashSet<E>();

		int i = -1;
		for (V iVertex:contGraph.vertexSet()) {
			i++;
			int j = -1;
			for (V jVertex:contGraph.vertexSet()) {
				j++;
				if (j<i) continue; // i.e. we include i==j (to remove loop edges)

				Set<E> edges = contGraph.getAllEdges(iVertex, jVertex);
				
				List<E> sortedEdges = new ArrayList<>(edges);
				
				Collections.sort(sortedEdges, new Comparator<E>() {
					@Override
					public int compare(E e1, E e2) {
						return Integer.compare(e1.getClusterId(), e2.getClusterId());
					}
				});
				
//				Map<Integer,Set<E>> groups = GraphUtils.groupIntoTypes(edges, true);
//				
//				if (edges.size()>1 && groups.size()>1) {
//					logger.info("Duplicate edge of different type between vertices {},{}", iVertex, jVertex);
//					for (E edge: sortedEdges) {
//						logger.info("Edge {} - {}", edge.toString(), edge.getXtalTrans().toString());
//					}
//				}
				
				if (i==j) {
					// loop edges: we remove them all
					for (E e : sortedEdges) {
						toRemove.add(e);
						logger.debug("Removed loop edge {} on vertex {} after contraction", e, iVertex);
					}
					
				} else {
					// others: we keep first from sorted list (the one with lowest interf cluster id)
					for (int k=1; k<sortedEdges.size(); k++) {						
						toRemove.add(sortedEdges.get(k));
						logger.debug("Removed duplicate edge {} between vertices {},{} after contraction", sortedEdges.get(k), iVertex, jVertex);
					}
				}

//				for (Entry<Integer, Set<E>> entry : groups.entrySet()){
//					int interfaceId = entry.getKey();
//					Set<E> group = entry.getValue();					
//
//					if (group.size()==0) {
//						continue;
//					} else if (group.size()==1 && i!=j) { 
//						continue;
//					} else if (group.size()==1 && i==j) {
//						// i!=j condition makes sure that loop edges are removed below (i==j case)
//						toRemove.add(group.iterator().next());
//						logger.debug("Removed loop edge with interface id {}",interfaceId);
//						continue;
//					}
//					// now we are in case 2 or more edges 
//					// we keep first and remove the rest
//					Iterator<E> it = group.iterator();
//					it.next(); // first edge: we keep it
//					while (it.hasNext()) {						
//						E edge = it.next();
//						toRemove.add(edge);
//						logger.debug("Removed edge with interface id {} between vertices {},{} ", 
//								interfaceId,iVertex.toString(),jVertex.toString());
//					}
//
//				}


			}

		}
		// now we do the removal
		for (E edge:toRemove) {
			contGraph.removeEdge(edge);
		}

	}
	
	/**
	 * Gets all edges that have the given interfClusterId, giving a warning if the source/target entities
	 * don't coincide for any of them.
	 * @param g
	 * @param interfClusterId
	 * @return
	 */
	private static <V extends ChainVertexInterface, E extends InterfaceEdgeInterface> Set<E> getEdgesWithInterfClusterId(Graph<V, E> g, int interfClusterId) {
		// let's first gather the edges to remove
		Set<E> toRemove = new HashSet<E>();

		// the source and target entity ids should coincide for all edges
		int sEntity = -1;
		int tEntity = -1;

		for (E edge:g.edgeSet()) {
			if (edge.getClusterId() == interfClusterId) {

				toRemove.add(edge);

				// we double check that indeed the source and target chain ids are the same for all of them
				V s = g.getEdgeSource(edge);
				V t = g.getEdgeTarget(edge);

				if (s.getEntityId() == t.getEntityId()) 
					logger.warn("This looks like a homomeric interface! We should not be contracting it, something is wrong!");
				
				if (sEntity>0 && tEntity>0) {
					if ( !( sEntity == s.getEntityId() && tEntity == t.getEntityId() ) &&
						 !( sEntity == t.getEntityId() && tEntity == s.getEntityId() )   ) {
						logger.warn("The source and target entity ids for edge {} don't match the expected ones. Something is wrong!", edge.toString());
					}
				} else {
					// we set source and target as the reference ones the first time
					sEntity = s.getEntityId();
					tEntity = t.getEntityId();
				}
			}
		}
		
		return toRemove;
	}
	
	/**
	 * Gets a map of entity ids to counts of vertices of that entity for the given graph
	 * @param g
	 * @return
	 */
	private static <V extends ChainVertexInterface, E extends InterfaceEdgeInterface> Map<Integer,Integer> getEntityCounts(UndirectedGraph<V, E> g) {
		Map<Integer, Integer> counts = new TreeMap<Integer,Integer>();
		for (V v:g.vertexSet()) {
			int currentEntity = v.getEntityId();
			if (!counts.containsKey(currentEntity)) {
				counts.put(currentEntity, 0);
			} 
			
			counts.put(currentEntity, counts.get(currentEntity)+1);
			
		}
		return counts;
	}
}
