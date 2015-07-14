package eppic.assembly;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphTools {

	private static final Logger logger = LoggerFactory.getLogger(GraphTools.class);
	
	
	public static UndirectedGraph<ChainVertex, InterfaceEdge> contract(UndirectedGraph<ChainVertex, InterfaceEdge> g, List<Integer> clusterIds) {
		Set<InterfaceEdge> toRemove = new HashSet<InterfaceEdge>();
		for (int interfClusterId:clusterIds) {
			toRemove.addAll(getEdgesWithInterfClusterId(g, interfClusterId));
		}
		
		return contract(g, toRemove);
	}
	
	public static UndirectedGraph<ChainVertex, InterfaceEdge> contract(UndirectedGraph<ChainVertex, InterfaceEdge> g, int interfClusterId) {
		
		// let's first gather the edges to remove
		Set<InterfaceEdge> toRemove = getEdgesWithInterfClusterId(g, interfClusterId);
		
		return contract(g, toRemove);
	}
	
	public static UndirectedGraph<ChainVertex, InterfaceEdge> contract(UndirectedGraph<ChainVertex, InterfaceEdge> g, Set<InterfaceEdge> toRemove) {
		UndirectedGraph<ChainVertex, InterfaceEdge> cg = copyGraph(g);
		
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
			
			//cg.removeEdge(s,t);
			cg.removeVertex(vToRemove);
			
			logger.debug("Graph has {} vertices and {} edges, before add edges loop", cg.vertexSet().size(), cg.edgeSet().size());
			
			// we need to add to vToKeep all edges that were connecting vToRemove to any other vertex 
			for (InterfaceEdge eToAdd : g.edgesOf(vToRemove)) {
				
				logger.debug("Graph has {} vertices and {} edges", cg.vertexSet().size(), cg.edgeSet().size());
				
				if (eToAdd == e) {
					logger.debug("Won't add the joining edge as a self-edge");
					continue;
				}
				
				// first we remove it, in case removing the vertex didn't do it implicitly
				//cg.removeEdge(eToAdd); 
				
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
					logger.debug("Vertex {}, needed to add new edge is not in graph, skipping", target);
					continue;
				}
				
				// we make sure we put them back in the same direction as we encountered them
				if (!invert) 
					cg.addEdge(vToKeep, target, eToAdd);
				else
					cg.addEdge(target, vToKeep, eToAdd);
				
				
			}
		}
		
		
		
		return cg;
	}
	
	/**
	 * Copies the given Graph to a new Graph with same vertices and edges.
	 * The vertices and edges are the same references as the original Graph.  
	 * @param g
	 * @return
	 */
	public static UndirectedGraph<ChainVertex, InterfaceEdge> copyGraph(UndirectedGraph<ChainVertex, InterfaceEdge> g) {
		
		if (! (g instanceof Pseudograph)) throw new IllegalArgumentException("Given graph is not a pseudograph!");
		
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
