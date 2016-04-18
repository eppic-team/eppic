package eppic.assembly;

import static org.junit.Assert.*;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.Pseudograph;
import org.junit.Test;

public class TestGraphUtils {

	@Test
	public void testIsAutomorphic() {
		
	}
	
	
	private static UndirectedGraph<ChainVertex, InterfaceEdge> createAutomorphicGraph(int numEntities, int numInterfClustersPerVert) {
		UndirectedGraph<ChainVertex, InterfaceEdge> g = new Pseudograph<>(InterfaceEdge.class);
		
		for (int i=1;i<=numEntities;i++) {
			
			ChainVertex v = new ChainVertex();
			v.setOpId(0);
			v.setChainId("A");
			v.setEntity(i);
			
			g.addVertex(v);
		}
		
		
		return g;
	}

}
