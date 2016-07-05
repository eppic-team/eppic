package eppic.assembly.layout;

import javax.vecmath.Point3d;

/**
 * Simple interface to return the 3D position of a particular vertex. Used by
 * {@link GraphLayout} implementations for fetching and assigning coordinates.
 * @author Spencer Bliven
 *
 * @param <V>
 */
public interface VertexPositioner<V> {
	/**
	 * Get the 3D position of a vertex
	 * @param vertex
	 * @return
	 */
	public Point3d getPosition(V vertex);
	/**
	 * Set the position of a vertex
	 * @param vertex
	 * @param pos
	 * @throws UnsupportedOperationException for read-only vertices
	 */
	public void setPosition(V vertex, Point3d pos);
}