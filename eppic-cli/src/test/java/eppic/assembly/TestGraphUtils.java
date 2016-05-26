package eppic.assembly;

import static org.junit.Assert.*;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.Pseudograph;
import org.junit.Test;

public class TestGraphUtils {

	@Test
	public void testIsAutomorphic() {
		UndirectedGraph<ChainVertexInterface, InterfaceEdgeInterface> g = createC2graph();
		
		assertTrue(GraphUtils.isAutomorphic(g));
	}
	
	
	private static UndirectedGraph<ChainVertexInterface, InterfaceEdgeInterface> createAutomorphicGraph(int numEntities, int numInterfClustersPerVert) {
		
		UndirectedGraph<ChainVertexInterface, InterfaceEdgeInterface> g = new Pseudograph<>(SimpleInterfaceEdge.class);
		
		for (int i=1;i<=numEntities;i++) {
			
			ChainVertexInterface v = new SimpleChainVertex("A", 0, i);
			
			g.addVertex(v);
		}


		return g;
	}

	private static UndirectedGraph<ChainVertexInterface, InterfaceEdgeInterface> createC2graph() {

		UndirectedGraph<ChainVertexInterface, InterfaceEdgeInterface> g = new Pseudograph<>(SimpleInterfaceEdge.class);

		

		ChainVertexInterface v1 = new SimpleChainVertex("A", 0, 1);
		ChainVertexInterface v2 = new SimpleChainVertex("A", 1, 1);

		g.addVertex(v1);
		g.addVertex(v2);
		
		InterfaceEdgeInterface e1 = new SimpleInterfaceEdge(1, 1);		
		
		g.addEdge(v1, v2, e1);

		return g;
	}

}
