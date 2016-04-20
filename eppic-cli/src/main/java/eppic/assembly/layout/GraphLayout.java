package eppic.assembly.layout;

import org.jgrapht.UndirectedGraph;

/**
 * Modify the 3D coordinates of a graph such that they give a visually appealing
 * 2D layout with z=0 for all positions. Thus, they are really 2D layouts, even
 * though they may still use {@link eppic.assembly.ChainVertex3D} objects to
 * store positions.

 * @author Spencer Bliven
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public interface GraphLayout<V,E> {

	/**
	 * Helper object to control getting and setting 3D coordinates for the vertex
	 * @return
	 */
	VertexPositioner<V> getVertexPositioner();
	
	/**
	 * Update the coordinates of all vertices
	 * @param graph
	 */
	void projectLatticeGraph(UndirectedGraph<V,E> graph);

}
