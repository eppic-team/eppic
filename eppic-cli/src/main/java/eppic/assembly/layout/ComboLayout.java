package eppic.assembly.layout;

import java.util.List;

import org.jgrapht.UndirectedGraph;

/**
 * 
 * @author Spencer Bliven
 *
 * @param <V> Vertex type
 * @param <E> edge type
 */
public class ComboLayout<V,E> implements GraphLayout<V, E> {
	private final List<GraphLayout<V,E>> layouts;
	
	/**
	 * Construct a ComboLayout which will sequentially apply all the sublayouts
	 * @param layouts List of layouts. May not be empty.
	 */
	public ComboLayout(List<GraphLayout<V,E>> layouts) {
		if(layouts.isEmpty()) {
			throw new IndexOutOfBoundsException("No sublayouts specified");
		}
		this.layouts = layouts;
	}

	/**
	 * Project all sublayouts sequentially
	 */
	@Override
	public void projectLatticeGraph(UndirectedGraph<V, E> graph) {
		for( GraphLayout<V, E> layout : layouts) {
			layout.projectLatticeGraph(graph);
		}
	}

	/**
	 * Returns the vertexPositioner from the first layout
	 */
	@Override
	public VertexPositioner<V> getVertexPositioner() {
		return layouts.get(0).getVertexPositioner();
	}

	/**
	 * @return the sublayouts
	 */
	public List<GraphLayout<V, E>> getLayouts() {
		return layouts;
	}
	
}
