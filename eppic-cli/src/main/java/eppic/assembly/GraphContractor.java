package eppic.assembly;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.vecmath.Point3i;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.util.Pair;
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
	@SuppressWarnings("unchecked")
	private UndirectedGraph<V, E> contractInterfaceCluster(
			UndirectedGraph<V, E> inputGraph, int interfClusterId, Class<? extends E> edgeClass) {
		
		// let's first gather the edges to remove: all of them correspond to a single interface cluster and thus 
		// source and target nodes will be always the same 2 entities
		Set<E> toRemove = getEdgesWithInterfClusterId(inputGraph, interfClusterId);

		// shallow copying of the graph
		UndirectedGraph<V, E> contGraph = new Pseudograph<>(edgeClass);
		Graphs.addGraph(contGraph, inputGraph);

		int referenceEntityId = -1;

		// 1. We go through each edge to remove, find the end point vertices and build a map of vertices to remove and their replacements.
		//    We need to have the full removal map before starting the rewiring of edges to be able to connect to the right vertices.
		for (E e:toRemove) {
			
			V s = inputGraph.getEdgeSource(e);
			
			if (s.getEntityId()<0) logger.error("Entity id for vertex {} is negative!",s.getEntityId());

			if (referenceEntityId<0) {
				referenceEntityId = s.getEntityId();
				logger.debug("Chose reference entity id {}. Vertices that have this entity id will be kept.", referenceEntityId); 
			}

			
			Pair<V,V> pair = findRemoveKeepPair(inputGraph, e, referenceEntityId);

			V vToRemove = pair.first;
			V vToKeep  = pair.second;					

			contGraph.removeVertex(vToRemove);

			contractedVertices.put(vToRemove, vToKeep);

		}


		// 2. Now we go ahead and start removing vertices and rearranging edges
		for (E e:toRemove) {

			logger.debug("Removing edge {}", e.toString());						
			
			Pair<V,V> pair = findRemoveKeepPair(inputGraph, e, referenceEntityId);

			V vToRemove = pair.first;
			V vToKeep  = pair.second;					
			

			logger.debug("Graph has {} vertices and {} edges, before add edges loop", contGraph.vertexSet().size(), contGraph.edgeSet().size());

			// we need to add to vToKeep all edges that were connecting vToRemove to any other vertex 
			for (E eToAdd : inputGraph.edgesOf(vToRemove)) {				

				if (eToAdd == e) {
					logger.debug("Won't add the joining edge {} as a self-edge", e);
					continue;
				}

				boolean invert = false;
				V vToLink = null;
				
				if (vToRemove.equals(inputGraph.getEdgeSource(eToAdd))) {
					vToLink = inputGraph.getEdgeTarget(eToAdd);
				} else if (vToRemove.equals(inputGraph.getEdgeTarget(eToAdd))) {
					vToLink = inputGraph.getEdgeSource(eToAdd);
					invert = true;
				} else {
					logger.error("vToRemove is neither source nor target");
					continue;
				}

				if (!contGraph.containsVertex(vToLink)) {
					logger.debug("Vertex {}, needed to add new edge {} is not in graph, replacing it by its contracted vertex {}", 
							vToLink, eToAdd, contractedVertices.get(vToLink));
					vToLink = contractedVertices.get(vToLink);
				}
				
				V src = null;
				V trt = null;
						
				if (invert) {
					src = vToLink;
					trt = vToKeep;
				} else {
					src = vToKeep;
					trt = vToLink;
				}
				

				// we make sure we put them back in the same direction as we encountered them
				logger.debug("Adding edge {} between {} -> {}. Before it was {}-{}", 
						eToAdd.toString(), src.toString(), trt.toString(), vToRemove.toString(), vToLink.toString());
				
				// we create a new edge so that we can keep the original edges from the original graph intact
				InterfaceEdge newEdge = new InterfaceEdge();
				
				newEdge.setInterfaceId(eToAdd.getInterfaceId());
				newEdge.setClusterId(eToAdd.getClusterId());
				newEdge.setIsInfinite(eToAdd.isInfinite());
				newEdge.setIsIsologous(eToAdd.isIsologous());
				
				Point3i newTrans = new Point3i(eToAdd.getXtalTrans());
				newEdge.setXtalTrans(newTrans);
				
				if (invert) newTrans.negate();
				
				newTrans.add(e.getXtalTrans());
							
				// the casting should be safe				 				
				contGraph.addEdge(src, trt, (E) newEdge);

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
	 * Given the inputGraph and an edge to remove finds the pairs of vertices to remove and keep 
	 * @param inputGraph
	 * @param e
	 * @param referenceEntityId
	 * @return the pair to remove and keep (first is the vertex to remove, second is the vertex to keep)
	 */
	private Pair<V,V> findRemoveKeepPair(UndirectedGraph<V, E> inputGraph, E e, int referenceEntityId) {
		V s = inputGraph.getEdgeSource(e);			
		V t = inputGraph.getEdgeTarget(e);			

		V vToRemove = null;
		V vToKeep = null;
		
		// we will keep the vertices matching referenceEntityId
		if (s.getEntityId() == referenceEntityId) {
			vToRemove = t;
			vToKeep = s;
		} else if (t.getEntityId() == referenceEntityId) {
			vToRemove = s;
			vToKeep = t;
		} else {
			logger.warn("Neither vertex matched entity id {}. Something is wrong!",referenceEntityId);
		}
		
		return new Pair<>(vToRemove, vToKeep);
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

				// note edges contains all edges between iVertex and jVertex, BUT there source/targets can be either iVertex/jVertex or viceversa (see docs of getAllEdges)
				Set<E> edges = contGraph.getAllEdges(iVertex, jVertex);
				
				if (i==j) {
					// loop edges: we remove them all
					for (E e : edges) {
						toRemove.add(e);
						logger.debug("Removed loop edge {} on vertex {} after contraction", e, iVertex);
					}
					
				} else {
					// before removing them, we go by edge types and find the total translation for all edges of one type and assign that to the edge that we will keep
					SortedMap<Integer,Set<E>> groups = GraphUtils.groupIntoTypes(edges, true);
					
					int k = -1;
					for (Entry<Integer, Set<E>> entry : groups.entrySet()) {
						k++;
						// each group below contains all edges of one type
						int clusterId = entry.getKey();
						Set<E> group = entry.getValue();
						
						if (k>0) { // i.e. second iteration and beyond
							// there's more than 1 group: we get rid of all edges beyond first group
							// TODO check that that's the right thing to do, do we lose anything by removing all the edges except for one type?							
							toRemove.addAll(group);
							logger.info("Removed after contraction {} edges with cluster id {} between vertices {},{}", group.size(), clusterId, iVertex.toString(), jVertex.toString());
							continue;
						}
						
						// only for first group
						if (group.size()>1) {
							E firstEdgeInGroup = null;
							Point3i totalTrans = new Point3i(0,0,0);
							V sourceRef = null;
							for (E edge : group) {
								if (sourceRef==null) {
									// we initialize sourceRef and then take as the reference for the other iterations
									sourceRef = contGraph.getEdgeSource(edge);
								}
								if (firstEdgeInGroup==null) {
									firstEdgeInGroup = edge;
								} else {
									// not the first one: we add edge to remove list
									logger.info("Removed after contraction duplicate edge {} between vertices {},{}", edge, iVertex, jVertex);
									toRemove.add(edge);
								}
								Point3i trans = new Point3i(edge.getXtalTrans());
								if (!contGraph.getEdgeSource(edge).equals(sourceRef)) {
									trans.negate();
								}
								totalTrans.add(trans);
							}
							// finally we set the new totalTrans to the edge that we will keep
							logger.info("Resetting after contraction edge {} to translation {}", firstEdgeInGroup.toString(), totalTrans.toString());
							firstEdgeInGroup.setXtalTrans(totalTrans);
						}
						
					}
					
				}


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
