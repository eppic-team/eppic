package eppic.assembly.gui;

import org.jgrapht.Graph;
import org.jgrapht.graph.Pseudograph;

import eppic.assembly.InterfaceEdge3D;

/**
 * InterfaceEdge3D, extended the source and target nodes.
 * 
 * This is used for mustache templates, since we can't use parameters in functions.
 * 
 * @author blivens
 *
 */

public class InterfaceEdge3DSourced<V> extends InterfaceEdge3D {
	private final V source, target;
	public InterfaceEdge3DSourced(InterfaceEdge3D e, Graph<V,InterfaceEdge3D> graph) {
		super(e);
		source = graph.getEdgeSource(e);
		target = graph.getEdgeTarget(e);
	}
	/**
	 * @return the source
	 */
	public V getSource() {
		return source;
	}
	/**
	 * @return the target
	 */
	public V getTarget() {
		return target;
	}
	
	/**
	 * Construct a new graph, identical to the input except with all edges
	 * annotated with source and target nodes
	 * @param graph
	 * @return graph, with InterfaceEdge3D replaced by InterfaceEdge3DSourced
	 */
	public static <V> Pseudograph<V,InterfaceEdge3DSourced<V>> addSources( Graph<V,InterfaceEdge3D> graph ){
		@SuppressWarnings("unchecked")
		Pseudograph<V,InterfaceEdge3DSourced<V>> out = new Pseudograph<V, InterfaceEdge3DSourced<V>>((Class<? extends InterfaceEdge3DSourced<V>>) InterfaceEdge3DSourced.class);
		for(V vert : graph.vertexSet() ) {
			out.addVertex(vert);
		}
		for( InterfaceEdge3D edge : graph.edgeSet()) {
			InterfaceEdge3DSourced<V> sourced = new InterfaceEdge3DSourced<V>(edge, graph);
			out.addEdge(sourced.getSource(), sourced.getTarget(), sourced);
		}
		return out;
	}
}