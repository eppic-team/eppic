package eppic.assembly;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GraphContractor {
	
	private static final Logger logger = LoggerFactory.getLogger(GraphContractor.class);

	/**
	 * The original graph
	 */
	private UndirectedGraph<ChainVertex, InterfaceEdge> g;
	
	/**
	 * The contracted graph
	 */
	private UndirectedGraph<ChainVertex, InterfaceEdge> cg;
	
	private Map<ChainVertex,ChainVertex> contractedVertices;
	
	public GraphContractor(UndirectedGraph<ChainVertex, InterfaceEdge> g) {
		this.g = g;
		this.contractedVertices = new HashMap<ChainVertex, ChainVertex>();
	}
	
	/**
	 * Contract all edges in given interfClusterId.
	 * Only one of the 2 vertices of each removed edge is kept (always the one corresponding
	 * to a certain arbitrary reference entity id). The edges belonging to the removed vertex are
	 * then attached to the remaining vertex.
	 * @param inputGraph
	 * @param interfClusterId 
	 * @return
	 */
	private UndirectedGraph<ChainVertex, InterfaceEdge> contractInterfaceCluster(
			UndirectedGraph<ChainVertex, InterfaceEdge> inputGraph, int interfClusterId) {
		
		// let's first gather the edges to remove: all of them correspond to a single interface cluster and thus 
		// source and target nodes will be always the same 2 entities
		Set<InterfaceEdge> toRemove = getEdgesWithInterfClusterId(inputGraph, interfClusterId);

		UndirectedGraph<ChainVertex, InterfaceEdge> contGraph = GraphUtils.copyGraph(inputGraph);

		int referenceEntityId = -1;

		for (InterfaceEdge e:toRemove) {

			logger.debug("Removing edge {}", e.toString());

			ChainVertex s = inputGraph.getEdgeSource(e);			
			ChainVertex t = inputGraph.getEdgeTarget(e);			

			if (s.getEntity()<0) logger.error("Entity id for vertex {} is negative!",s.getEntity());

			if (referenceEntityId<0) {
				referenceEntityId = s.getEntity();
				logger.debug("Chose reference entity id {}. Vertices that have this entity id will be kept.", referenceEntityId); 
			}

			// we will keep the vertices matching referenceEntityId
			ChainVertex vToRemove = null;
			ChainVertex vToKeep = null;
			if (s.getEntity() == referenceEntityId) {
				vToRemove = t;
				vToKeep = s;
			} else if (t.getEntity() == referenceEntityId) {
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
			for (InterfaceEdge eToAdd : inputGraph.edgesOf(vToRemove)) {				

				if (eToAdd == e) {
					logger.debug("Won't add the joining edge as a self-edge");
					continue;
				}

				boolean invert = false;
				ChainVertex target = null;
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
				if (!invert) 					
					contGraph.addEdge(vToKeep, target, eToAdd);
				else
					contGraph.addEdge(target, vToKeep, eToAdd);



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
	 * replacements by {@link #getContractedVertex(ChainVertex)} 
	 * @return
	 */
	public UndirectedGraph<ChainVertex, InterfaceEdge> contract() {
			
		// for first iteration
		int interfClusterId = GraphUtils.getLargestHeteroInterfaceCluster(g);
		
		cg = g;

		int i = 0;
		while (true) {
			i++;
			logger.debug("Round {} of contraction: contracting interface cluster {}",i,interfClusterId);
			logger.debug("Starting graph before contraction has {} vertices and {} edges",cg.vertexSet().size(),cg.edgeSet().size());
			// logging the entity counts
			if (logger.isDebugEnabled()) {
				
				Map<Integer, Integer> counts = getEntityCounts(cg);
				
				logger.debug("Counts per entities of starting graph:");
				for (Entry<Integer, Integer> entry:counts.entrySet()) {
					logger.debug("  entity {} -> {} vertices", entry.getKey(), entry.getValue());
				}
				
				
			}

			
			cg = contractInterfaceCluster(cg, interfClusterId);
			
			// we get the interfClusterId for next iteration
			interfClusterId = GraphUtils.getLargestHeteroInterfaceCluster(cg);
			
			// if no more heteromeric interfaces we break: end of iteration
			if (interfClusterId == -1) break;
		}
		
		return cg;
	}

	public UndirectedGraph<ChainVertex, InterfaceEdge> getOriginalGraph() {
		return g;
	}
	
	public UndirectedGraph<ChainVertex, InterfaceEdge> getContractedGraph() {
		return cg;
	}
	
	/**
	 * Returns the replacement vertex given a contracted vertex
	 * @param v
	 * @return
	 */
	public ChainVertex getContractedVertex(ChainVertex v) {
		return contractedVertices.get(v);
	}
	
	/**
	 * Returns the set of contracted vertices
	 * @return
	 */
	public Set<ChainVertex> getContractedVertices() {
		return contractedVertices.keySet();
	}
	
	public Set<Integer> getContractedEntityIds() {
		Set<Integer> set = new TreeSet<Integer>();
		
		for (ChainVertex v: contractedVertices.keySet()) {
			set.add(v.getEntity());
		}
		return set;
	}
	
	private static void trim(UndirectedGraph<ChainVertex, InterfaceEdge> contGraph) {
		
		Set<InterfaceEdge> toRemove = new HashSet<InterfaceEdge>();

		int i = -1;
		for (ChainVertex iVertex:contGraph.vertexSet()) {
			i++;
			int j = -1;
			for (ChainVertex jVertex:contGraph.vertexSet()) {
				j++;
				if (j<i) continue; // i.e. we include i==j (to remove loop edges)

				Set<InterfaceEdge> edges = contGraph.getAllEdges(iVertex, jVertex);
				Map<Integer,Set<InterfaceEdge>> groups = GraphUtils.groupIntoTypes(edges, true);

				for (int interfaceId:groups.keySet()){
					Set<InterfaceEdge> group = groups.get(interfaceId);

					if (group.size()==0) {
						continue;
					} else if (group.size()==1 && i!=j) { 
						continue;
					} else if (group.size()==1 && i==j) {
						// i!=j condition makes sure that loop edges are removed below (i==j case)
						toRemove.add(group.iterator().next());
						logger.debug("Removed loop edge with interface id {}",interfaceId);
						continue;
					}
					// now we are in case 2 or more edges 
					// we keep first and remove the rest
					Iterator<InterfaceEdge> it = group.iterator();
					it.next(); // first edge: we keep it
					while (it.hasNext()) {						
						InterfaceEdge edge = it.next();
						toRemove.add(edge);
						logger.debug("Removed edge with interface id {} between vertices {},{} ", 
								interfaceId,iVertex.toString(),jVertex.toString());
					}

				}


			}

		}
		// now we do the removal
		for (InterfaceEdge edge:toRemove) {
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
	private static Set<InterfaceEdge> getEdgesWithInterfClusterId(Graph<ChainVertex, InterfaceEdge> g, int interfClusterId) {
		// let's first gather the edges to remove
		Set<InterfaceEdge> toRemove = new HashSet<InterfaceEdge>();

		// the source and target entity ids should coincide for all edges
		int sEntity = -1;
		int tEntity = -1;

		for (InterfaceEdge edge:g.edgeSet()) {
			if (edge.getClusterId() == interfClusterId) {

				toRemove.add(edge);

				// we double check that indeed the source and target chain ids are the same for all of them
				ChainVertex s = g.getEdgeSource(edge);
				ChainVertex t = g.getEdgeTarget(edge);

				if (s.getEntity() == t.getEntity()) 
					logger.warn("This looks like a homomeric interface! We should not be contracting it, something is wrong!");
				
				if (sEntity>0 && tEntity>0) {
					if ( !( sEntity == s.getEntity() && tEntity == t.getEntity() ) &&
						 !( sEntity == t.getEntity() && tEntity == s.getEntity() )   ) {
						logger.warn("The source and target entity ids for edge {} don't match the expected ones. Something is wrong!", edge.toString());
					}
				} else {
					// we set source and target as the reference ones the first time
					sEntity = s.getEntity();
					tEntity = t.getEntity();
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
	private static Map<Integer,Integer> getEntityCounts(UndirectedGraph<ChainVertex, InterfaceEdge> g) {
		Map<Integer, Integer> counts = new TreeMap<Integer,Integer>();
		for (ChainVertex v:g.vertexSet()) {
			int currentEntity = v.getEntity();
			if (!counts.containsKey(currentEntity)) {
				counts.put(currentEntity, 0);
			} 
			
			counts.put(currentEntity, counts.get(currentEntity)+1);
			
		}
		return counts;
	}
}
