package eppic.assembly;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A straight line segment
 * @author blivens
 *
 */
public class ParametricLineSegment {

	private Point3d start, end;
	private String uniqueName;
	
	public ParametricLineSegment(Point3d start, Point3d end) {
		this.start = start;
		this.end = end;
	}

	/**
	 * Interpolate between the start (pos=0) and the end (pos=1)
	 * @param relpos Position along the line, from 0 to 1
	 * @return the 3D coordinate of that position
	 */
	public Point3d getRelativePosition(double relpos) {
		Point3d mid = new Point3d();
		mid.interpolate(start, end, relpos);
		return mid;
	}
	
	/**
	 * Interpolate a fixed distance along the line. Positive values are
	 * taken as distance from the start towards the end. Negative distances
	 * are from the end towards the start.
	 * @param abspos Distance along the line
	 * @return
	 */
	public Point3d getAbsolutePosition(double abspos) {
		double rellen = getLength()/abspos;
		if(abspos >= 0) {
			return getRelativePosition(rellen);
		} else {
			return getRelativePosition(1-rellen);
		}
	}

	public Point3d getStart() {
		return getRelativePosition(0);
	}
	public Point3d getEnd() {
		return getRelativePosition(1);
	}
	public Point3d getMid() {
		return getRelativePosition(.5);
	}
	/**
	 * Get the length along this arc
	 * @return
	 */
	public double getLength() {
		Vector3d ab = new Vector3d();
		ab.sub(getEnd(), getStart());
		
		return ab.length();
	}

	public String getUniqueName() {
		return uniqueName;
	}
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}
}
