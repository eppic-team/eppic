package eppic.assembly;

import static org.junit.Assert.*;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.Pseudograph;
import org.junit.Test;

public class TestGraphUtils {

	@Test
	public void testIsAutomorphic() {
		
		
		for (int n=2;n<10;n++) {
			UndirectedGraph<ChainVertexInterface, InterfaceEdgeInterface> g = createCnGraph(n);

			assertTrue(GraphUtils.isAutomorphic(g));
		}
		
		for (int n=3;n<10;n++) {
			UndirectedGraph<ChainVertexInterface, InterfaceEdgeInterface> g = createDnGraph(n);

			assertTrue(GraphUtils.isAutomorphic(g));
			
		}
	}
	
	private static UndirectedGraph<ChainVertexInterface, InterfaceEdgeInterface> createCnGraph(int n) {

		UndirectedGraph<ChainVertexInterface, InterfaceEdgeInterface> g = new Pseudograph<>(SimpleInterfaceEdge.class);
				
		addCycle(g, n, "A", 1);

		return g;
	}
	
	/**
	 * Add a cycle to the given graph with entityId 1 and interfaceClusterId 1. The opIds will go from 1 to n.
	 * @param g the graph
	 * @param n order of cycle
	 * @param chainId the chain id
	 * @param interfaceId the interface id
	 */
	private static void addCycle(UndirectedGraph<ChainVertexInterface, InterfaceEdgeInterface> g, int n, String chainId, int interfaceId) {
		ChainVertexInterface prevV = null;
		ChainVertexInterface firstV = null;
		
		for (int i=1;i<=n;i++) {
						
			ChainVertexInterface v = new SimpleChainVertex(chainId, i, 1);
			g.addVertex(v);
			
			if (i==1) firstV = v;
						
			if (prevV!=null) {
				InterfaceEdgeInterface e1 = new SimpleInterfaceEdge(interfaceId, 1);
				g.addEdge(v, prevV, e1);
			}
			
			prevV = v;
		}

		// finally we connect first and last to close the ring
		InterfaceEdgeInterface e1 = new SimpleInterfaceEdge(interfaceId, 1);
		g.addEdge(firstV, prevV, e1);
	}

	private static UndirectedGraph<ChainVertexInterface, InterfaceEdgeInterface> createDnGraph(int n) {
		
		UndirectedGraph<ChainVertexInterface, InterfaceEdgeInterface> g = new Pseudograph<>(SimpleInterfaceEdge.class);
		
		// first ring: chainId A and interfaceId 1
		addCycle(g, n, "A", 1);
		
		// second ring: chain Id B and interfaceId 2
		addCycle(g, n, "B", 2);
		
		// now we need to connect the top and bottom rings

		for (ChainVertexInterface v1 : g.vertexSet()) {
			if (v1.getChainId().equals("A")) {
				
				for (ChainVertexInterface v2 : g.vertexSet()) {
					// adding a "straight" edge top to bottom
					if (v2.getChainId().equals("B") && v2.getOpId() == v1.getOpId()) {
						// the cross interfaces get a interfaceId 3 (but same clusterId that all others)
						InterfaceEdgeInterface e = new SimpleInterfaceEdge(3, 1);
						g.addEdge(v1, v2, e);
					}
					
					// adding a "crossed" edge top to bottom
					if (v2.getChainId().equals("B") && v2.getOpId() == ((v1.getOpId()+1)%n) + 1 ) {
						// the cross interfaces get a interfaceId 4 (but same clusterId that all others)
						InterfaceEdgeInterface e = new SimpleInterfaceEdge(4, 1);
						g.addEdge(v1, v2, e);
						
					}
						
				}
				
				
			}
		}
		

		return g;
	}
}
