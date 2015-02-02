package eppic.assembly;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

import edu.uci.ics.jung.graph.util.Pair;

/**
 * Edge between an InterfaceVertex and an AtomVertex.
 * 
 * By default, this will be positioned between its endpoints, but the layout
 * can also be overridden by defining one or more segments.
 * 
 * @author spencer
 *
 */
class InterfaceEdge {
	// annotation data
	private int interfaceId;
	private List<Pair<Point3d>> segments;
	private Color color;
	
	public InterfaceEdge(int interfaceId) {
		this.interfaceId = interfaceId;
		segments = new ArrayList<Pair<Point3d>>(2);
		color = null;
		}
	/**
	 * Adds a new segment for graph layout
	 * @param segment
	 */
	public void addSegment(Pair<Point3d> segment) {
		segments.add(segment);
	}
	public void addSegment(Point3d start, Point3d end) {
		addSegment(new Pair<Point3d>(start,end));
	}
	public List<Pair<Point3d>> getSegments() {
		return segments;
	}
	public int getInterfaceId() {
		return interfaceId;
	}
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
	}
	
	@Override
	public String toString() {
		return String.format("-%d-",interfaceId);
	}
}