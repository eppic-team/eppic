package eppic.assembly;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class ParametricCircularArc {

	private Point3d start, end;
	private Vector3d normal;
	private String uniqueName;

	public ParametricCircularArc(Point3d start, Point3d end, double height) {
		this.start = start;
		this.end = end;
		
		// Compute random normal
		Vector3d ab = new Vector3d();
		ab.sub(end,start);
		normal = randomOrthogonalVector(ab);
		normal.scale(height);
	}
	public ParametricCircularArc(Point3d start, Point3d end, Vector3d normal) {
		this.start = start;
		this.end = end;
		this.normal = normal;
	}


	/**
	 * Interpolate between the start (pos=0) and the end (pos=1)
	 * @param relpos Position along the arc, from 0 to 1
	 * @return the position on the arc
	 */
	public Point3d getRelativePosition(double relpos) {
		//TODO ignores normal
		Point3d mid = new Point3d();
		mid.interpolate(start, end, relpos);
		return mid;
	}
	/**
	 * Interpolate a fixed distance along the arc. Positive values are
	 * taken as distance from the start towards the end. Negative distances
	 * are from the end towards the start
	 * @param abspos Distance along the arc, in Angstroms
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
		//TODO ignores normal
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
	/**
	 * Generate a vector perpendicular to the input vector, in a random direction.
	 * 
	 * Used for randomly orienting arcs
	 * @param n The plane normal
	 * @return A random normalized vector orthogonal to n
	 */
	private static Vector3d randomOrthogonalVector(Vector3d n) {
		Vector3d random;
		do {
			// Sample uniformly at random from unit sphere
			random = new Vector3d( Math.random()*2-1.0, Math.random()*2-1.0, Math.random()*2-1.0 );
		} while( random.lengthSquared() > 1);

		// Project random onto the plane defined by n and the origin
		Vector3d vecToPlane = new Vector3d(n);
		vecToPlane.normalize();
		vecToPlane.scale( random.dot(vecToPlane) );
		random.sub(vecToPlane);

		// Normalize
		random.normalize();
		return random;
	}
}
