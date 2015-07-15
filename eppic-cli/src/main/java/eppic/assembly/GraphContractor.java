package eppic.assembly;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.Pseudograph;
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
	 * @param interfClusterId 
	 * @return
	 */
	public UndirectedGraph<ChainVertex, InterfaceEdge> contract(int interfClusterId) {
		
		// let's first gather the edges to remove: all of them correspond to a single interface cluster and thus 
		// source and target nodes will be always the same 2 entities
		Set<InterfaceEdge> toRemove = getEdgesWithInterfClusterId(g, interfClusterId);

		this.cg = copyGraph(g);

		int referenceEntityId = -1;

		for (InterfaceEdge e:toRemove) {

			logger.debug("Removing edge {}", e.toString());

			ChainVertex s = g.getEdgeSource(e);			
			ChainVertex t = g.getEdgeTarget(e);			

			if (s.getEntity()<0) logger.error("Entity id for vertex {} is negative!",s.getEntity());

			if (referenceEntityId<0) referenceEntityId = s.getEntity();

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

			logger.debug("Graph has {} vertices and {} edges, before removing anything", cg.vertexSet().size(), cg.edgeSet().size());

			cg.removeVertex(vToRemove);
			
			contractedVertices.put(vToRemove, vToKeep);

			logger.debug("Graph has {} vertices and {} edges, before add edges loop", cg.vertexSet().size(), cg.edgeSet().size());

			// we need to add to vToKeep all edges that were connecting vToRemove to any other vertex 
			for (InterfaceEdge eToAdd : g.edgesOf(vToRemove)) {				

				if (eToAdd == e) {
					logger.debug("Won't add the joining edge as a self-edge");
					continue;
				}

				boolean invert = false;
				ChainVertex target = null;
				if (vToRemove.equals(g.getEdgeSource(eToAdd))) {
					target = g.getEdgeTarget(eToAdd);
				} else if (vToRemove.equals(g.getEdgeTarget(eToAdd))) {
					target = g.getEdgeSource(eToAdd);
					invert = true;
				} else {
					logger.error("vToRemove is neither source nor target");
					continue;
				}

				if (!cg.containsVertex(target)) {
					logger.debug("Vertex {}, needed to add new edge {} is not in graph, replacing it by its contracted vertex {}", 
							target, eToAdd, contractedVertices.get(target));
					target = contractedVertices.get(target);
				}

				// we make sure we put them back in the same direction as we encountered them
				logger.debug("Adding edge {} between {} {} {}. Before it was {}-{}", 
						eToAdd.toString(), vToKeep.toString(), ( invert?"<-":"->" ), target.toString(),vToRemove.toString(),target.toString());
				if (!invert) 					
					cg.addEdge(vToKeep, target, eToAdd);
				else
					cg.addEdge(target, vToKeep, eToAdd);



				logger.debug("Graph has {} vertices and {} edges", cg.vertexSet().size(), cg.edgeSet().size());
			}
		}



		return cg;
	}
	
	/**
	 * Contract the graph by contracting one of the given interface clusters at a time.
	 * @param clusterIds
	 * @return
	 */
	public UndirectedGraph<ChainVertex, InterfaceEdge> contract(List<Integer> clusterIds) {
		
		// TODO implement
		
		//UndirectedGraph<ChainVertex, InterfaceEdge> cg = g;		
		// we contract one interface cluster at a time
		//for (int interfClusterId:clusterIds) {
		//	cg = contract(cg, interfClusterId);
		//}		
		//return cg;
		
		return null;
	}

	public UndirectedGraph<ChainVertex, InterfaceEdge> getOriginalGraph() {
		return g;
	}
	
	public UndirectedGraph<ChainVertex, InterfaceEdge> getContractedGraph() {
		return cg;
	}
	
	public ChainVertex getContractedVertex(ChainVertex v) {
		return contractedVertices.get(v);
	}
	
	/**
	 * Copies the given Graph to a new Graph with same vertices and edges.
	 * The vertices and edges are the same references as the original Graph.  
	 * @param g
	 * @return
	 */
	private static UndirectedGraph<ChainVertex, InterfaceEdge> copyGraph(UndirectedGraph<ChainVertex, InterfaceEdge> g) {
		
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
}
