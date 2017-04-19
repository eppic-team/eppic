package eppic.assembly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

	/**
	 * Encodes a tree with which nodes were merged.
	 * Access with {@link #getContractedVertex(ChainVertexInterface)}
	 */
	private Map<V,V> contractedVertices;
	
	/**
	 * The set of edges that were contracted.
	 */
	private Set<E> contractedEdges;
	
	/**
	 * The set of contracted interface cluster ids
	 */
	private Set<Integer> contractedInterfClusterIds;

	public GraphContractor(UndirectedGraph<V, E> g) {
		this.g = g;
		this.contractedVertices = new HashMap<V, V>();
		this.contractedInterfClusterIds = new TreeSet<>();
		this.contractedEdges = new HashSet<E>();
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

		//logger.debug("Pre-contraction graph:\n"+GraphUtils.toDot(inputGraph, "Precontraction"));

		// shallow copying of the graph
		UndirectedGraph<V, E> contGraph = new Pseudograph<>(edgeClass);
		Graphs.addGraph(contGraph, inputGraph);

		// let's first gather the edges to remove: all of them correspond to a single interface cluster and thus 
		// source and target nodes will be always the same 2 entities
		Set<E> toRemove = getEdgesWithInterfClusterId(inputGraph, interfClusterId);

		// Choose which end of the edges to keep
		int referenceEntityId = -1;

		// Remove each edge individually
		for (E e:toRemove) {


			// Extract the following info from e with the convention ( vToKeep -> vToRemove )
			V vToKeep,vToRemove;
			Point3i xtalTrans;

			{ // local variable scope

				logger.debug("Removing edge {}", e.toString());

				V s = inputGraph.getEdgeSource(e);
				V t = inputGraph.getEdgeTarget(e);

				// Use the first edge to establish the reference direction
				if (s.getEntityId()<0) logger.error("Entity id for vertex {} is negative!",s.getEntityId());
				if (referenceEntityId<0) {
					referenceEntityId = s.getEntityId();
					logger.debug("Chose reference entity id {}. Vertices that have this entity id will be kept.", referenceEntityId); 
				}

				xtalTrans = e.getXtalTrans();

				boolean keepSource = s.getEntityId() == referenceEntityId;
				assert keepSource != (t.getEntityId() == referenceEntityId); // either the source or target should be the ref

				if(keepSource) {
					vToKeep = s;
					vToRemove = t;
				} else {
					vToKeep = t;
					vToRemove = s;
					// need to negate translation to keep with the convention
					xtalTrans.negate();
				}

				logger.debug("Graph has {} vertices and {} edges, before add edges loop", contGraph.vertexSet().size(), contGraph.edgeSet().size());
			}
			
			contractedEdges.add(e);

			// we need to add to vToKeep all edges that were connecting vToRemove to any other vertex 
			for (E eToAdd : contGraph.edgesOf(vToRemove)) {

				if (eToAdd == e) {
					//Don't add the joining edges as a self-edge
					continue;
				}
				
				if (contGraph.getEdgeSource(eToAdd).equals(vToKeep) || contGraph.getEdgeTarget(eToAdd).equals(vToKeep)) {
					// don't add any other extra joining edges as a self-edge
					logger.debug("An extra edge ({}), different than edge being removed ({}), exists between vToKeep ({}) and vToRemove ({}). Adding it to the list of contracted edges.", 
							eToAdd, e, vToKeep, vToRemove);
					contractedEdges.add(eToAdd);
					continue;
				}
				
				// extract similar info from eToAdd with the convention ( vToRemove -> vToLink )
				V vToLink;
				Point3i xtalTransLink;
				
				{ // local variable scope
					V s = contGraph.getEdgeSource(eToAdd);
					V t = contGraph.getEdgeTarget(eToAdd);
	
					xtalTransLink = eToAdd.getXtalTrans();
					
					if (vToRemove.equals(s)) {
						vToLink = t;
					} else {
						assert vToRemove.equals(t); // should be one or the other
						vToLink = s;
						// preserve direction convention
						xtalTransLink.negate();
					}

					if(vToLink == vToKeep) {
						//Don't add the joining edges as a self-edge
						continue;
					}
					// Now we have all the info to make a new edge ( vToKeep -> vToLink )
					logger.debug("Adding edge {} between {} -> {}. Before it was {}->{}", 
							eToAdd.toString(), vToKeep.toString(), vToLink.toString(), s.toString(), t.toString());
				}

				// we create a new edge so that we can keep the original edges from the original graph intact
				InterfaceEdge newEdge = new InterfaceEdge();

				newEdge.setInterfaceId(eToAdd.getInterfaceId());
				newEdge.setClusterId(eToAdd.getClusterId());
				//TODO It can be that two non-infinite edges together are infinite. Does that matter here? -SB
				newEdge.setIsInfinite(eToAdd.isInfinite() || e.isInfinite());
				newEdge.setIsIsologous(eToAdd.isIsologous() || e.isIsologous());

				// xtalTrans should have already been negated if necessary
				Point3i newTrans = new Point3i(xtalTrans);
				newTrans.add(xtalTransLink);
				newEdge.setXtalTrans(newTrans);

				// the casting should be safe
				contGraph.addEdge(vToKeep, vToLink, (E) newEdge);

				logger.debug("Graph has {} vertices and {} edges", contGraph.vertexSet().size(), contGraph.edgeSet().size());
			}

			// Remove the vertex!
			contGraph.removeVertex(vToRemove);
			contractedVertices.put(vToRemove,vToKeep);
		}

		logger.debug("Post-contraction graph:\n"+GraphUtils.toDot(contGraph, "Postcontraction"));

		

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
		// TODO select the best scoring interface instead of largest (in most cases it will coincide with the largest anyway)
		// TODO we can only select here edges that are isomorphic in the whole graph, i.e. that happen between every pair of chains of the 2 entities
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
			// TODO select the best scoring interface instead of largest (in most cases it will coincide with the largest anyway)
			interfClusterId = GraphUtils.getLargestHeteroInterfaceCluster(cg);

			// if no more heteromeric interfaces we break: end of iteration
			if (interfClusterId == -1) break;
		}
		
		// gathering list of contracted interface clusters
		Set<Integer> remainingClusters = GraphUtils.getDistinctInterfaceClusters(cg);
		for (E e : contractedEdges) {
			if (!remainingClusters.contains(e.getClusterId())) {
				contractedInterfClusterIds.add(e.getClusterId());
			}
		}
		
		// finally we trim in case we still have duplicate edges
		trim(cg);

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
	 * @return the replacement for v, or v if it was not contracted
	 */
	public V getContractedVertex(V v) {
		if(!contractedVertices.containsKey(v)) {
			return v; //replaced with itself
		}
		V replacement = contractedVertices.get(v);
		// Check for additional replacements recursively
		if( replacement != v) {
			V replacement2 = getContractedVertex(replacement);
			if( replacement != replacement2) {
				// shorten search path for future searches
				replacement = replacement2;
				contractedVertices.put(v,replacement);
			}
		}
		return replacement;
	}

	/**
	 * Returns the set of removed vertices
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
	
	public Set<Integer> getContractedInterfClusterIds() {
		return contractedInterfClusterIds;
	}

	/**
	 * Eliminates duplicate edges in the given graph: for any 2 nodes joined by more than 1 edge 
	 * only 1 edge is kept. Also eliminates all loop edges (of a node to itself). 
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
					SortedMap<Integer,Set<E>> groups = GraphUtils.groupIntoTypes(edges, true);

					int k = -1;
					for (Entry<Integer, Set<E>> entry : groups.entrySet()) {
						k++;
						// each group below contains all edges of one type
						int clusterId = entry.getKey();
						List<E> group = new ArrayList<>(entry.getValue());

						if (k==0) { // only for first group

							// we remove all but first
							for (int l=1;l<group.size();l++) {
								logger.debug("Removed (after contraction) duplicate edge {} between vertices {},{}", group.get(l), iVertex, jVertex); 
								toRemove.add(group.get(l));
							}


						} else { // i.e. second group and beyond

							// there's more than 1 group: we get rid of all edges beyond first group
							// TODO check that this is the right thing to do, do we lose anything by removing all the edges except for one type?
							// TODO or do we want to keep one edge per type?

							toRemove.addAll(group);
							logger.debug("Removed (after contraction) {} edges with cluster id {} between vertices {},{}", group.size(), clusterId, iVertex.toString(), jVertex.toString());
							continue;
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
