package eppic.assembly;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class ParametricCircularArc {


	private double radius;
	private double startAngle;
	private double endAngle;
	private Matrix4d transformation;

	private String uniqueName;
	/**
	 * Create a unit circle in the XY plane
	 */
	public ParametricCircularArc() {
		this(1, 0, 2*PI, getIdentityMatrix() );
	}
	/**
	 * Create an arc around the specified circle.
	 * 
	 * @param center Center of the circle
	 * @param radius Radius of the circle
	 * @param startAngle Start of the arc (in [0,2PI) )
	 * @param endAngle End of the arc (in (0, 2PI] )
	 * @param transformation A transformation matrix rotating and translating the
	 *  circle from its default position at the origin in the XY plane
	 */
	public ParametricCircularArc(double radius,
			double startAngle, double endAngle, Matrix4d transformation) {
		super();
		this.radius = radius;
		this.startAngle = startAngle;
		this.endAngle = endAngle;
		this.transformation = transformation;
	}

	/**
	 * Create an arc between two points
	 * @param start Starting point
	 * @param end Ending point
	 * @param pinnacle A vector specifying the direction and height of the
	 *  midpoint of the arc relative to a flat segment
	 */
	public ParametricCircularArc(Point3d start, Point3d end, Vector3d pinnacle) {
		// radius
		// r = ( ||b-a||^2/4 + ||h||^2)/(2*||h||)
		Vector3d ab = new Vector3d();
		ab.sub(end, start);
		double len2 = ab.lengthSquared(); // length of chord ^2
		double h2 = pinnacle.lengthSquared(); // height perpendicular to chord ^2
		if(h2 == 0) {
			throw new IllegalArgumentException("Arc has no curvature.");
		}
		radius = ( len2/4 + h2 )/(2*sqrt(h2));

		// center point
		// center = (a+b)/2-h*( ||a-b||^2/(8*||h||^2) - 1/2 )
		Point3d midpoint = new Point3d();
		midpoint.interpolate(start,end, .5);
		double scaledHeight = 0.5 - len2/(8*h2);
		Point3d center = new Point3d(pinnacle);
		center.scaleAdd(scaledHeight, midpoint);

		// construct orthonormal basis from center->start and h
		Point3d ch = new Point3d();
		ch.add(center, pinnacle);
		transformation = matrixFromPlane(center, start, ch);

		startAngle = 0;
		// ||ab||^2 = ||ca||^2+||cb||^2-2*||ca||*||cb||*cos(acb)
		endAngle = Math.acos(1-len2/(2*radius*radius));
		// detect if our arc goes the long way around
		if( radius*radius < h2) {
			endAngle = 2*PI-endAngle;
		}
	}

	public ParametricCircularArc(Point3d start, Point3d end, double height) {
		this(start,end, randomOrthogonalVector(start,end,height) );
	}

	/**
	 * Interpolate between the start (pos=0) and the end (pos=1)
	 * @param relpos Position along the arc, from 0 to 1
	 * @return the position on the arc
	 */
	public Point3d getRelativePosition(double relpos) {
		double angle = startAngle*(1-relpos) + endAngle*relpos;
		// Position on the plane
		Point3d pos = new Point3d( radius*cos(angle), radius*sin(angle), 0);
		// Transform to 3D
		transformation.transform(pos);

		return pos;
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


	public double getRadius() {
		return radius;
	}
	public double getStartAngle() {
		return startAngle;
	}
	public double getEndAngle() {
		return endAngle;
	}
	public Matrix4d getTransformation() {
		return transformation;
	}
	/**
	 * @return The center point of the circle
	 */
	public Point3d getCenter() {
		Point3d center = new Point3d();
		transformation.transform(center); // Equivalent to translational component
		return center;
	}
	/**
	 * Get the length along this arc
	 * @return
	 */
	public double getLength() {
		return radius*(endAngle-startAngle);
	}
	/**
	 * Get the distance between endpoints
	 * @return The length between {@link #getStart()} and {@link #getEnd()}.
	 */
	public double getChordLength() {
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
	 * Used for randomly orienting arcs.
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
	/**
	 * Generate a vector perpendicular to the input vector, in a random direction
	 * and with the specified length.
	 * 
	 * Used for randomly orienting arcs.
	 * @param n The plane normal
	 * @param length The length of the returned vector
	 * @return A random vector orthogonal to n
	 */
	private static Vector3d randomOrthogonalVector(Vector3d n,double length) {
		Vector3d random = randomOrthogonalVector(n);
		random.scale(length);
		return random;
	}
	/**
	 * Generate a vector perpendicular to the line between two point, in a
	 * random direction and with the specified length.
	 * 
	 * Used for randomly orienting arcs.
	 * @param start First point
	 * @param end Second point
	 * @param length The length of the returned vector
	 * @return A random vector orthogonal to the line between start and end
	 */
	private static Vector3d randomOrthogonalVector(Point3d start, Point3d end,
			double height) {
		Vector3d ab = new Vector3d();
		ab.sub(end,start);
		return randomOrthogonalVector(ab,height);
	}

	/**
	 * Get the identity matrix
	 */
	private static Matrix4d getIdentityMatrix() {
		Matrix4d i = new Matrix4d();
		i.setIdentity();
		return i;
	}

	/**
	 * Calculates the transformation matrix to convert between a point on a plane
	 * and its 3D coordinates. The plane goes through the origin and is
	 * specified by two vectors in the plane. The vectors need not be orthogonal, as
	 * the Gram–Schmidt process is used to create a valid transformation matrix.
	 * <p>
	 * The inverse of the returned matrix can be used to project points
	 * back onto the plane.
	 * <p>
	 * For planes which do not go through though the origin, use the three-point
	 * plane specification with {@link #matrixFromPlane(Point3d, Point3d, Point3d)}.
	 * @param v1 A vector parallel to the plane
	 * @param v2 A second vector parallel to the plane (but not parallel to v1)
	 * @return The transformation matrix converting the plane's frame of reference
	 *  to cartesian coordinates.
	 */
	public static Matrix3d matrixFromPlane(Vector3d v1, Vector3d v2) {
		// Use Gram–Schmidt process to orthonormalize the v1,v2 space
		Vector3d u1 = new Vector3d(v1);
		u1.normalize();

		// <v1,v2>/<v1,v1>
		double dot = v1.dot(v2)/v1.lengthSquared();
		// v2 - <v1,v2>/<v1,v1>*v1
		Vector3d u2 = new Vector3d(v1);
		u2.scaleAdd(-dot, new Vector3d(v2));
		if(u2.epsilonEquals(new Vector3d(), 1e-10)) {
			throw new IllegalArgumentException("Input vectors are parallel");
		}
		u2.normalize();

		Vector3d u3 = new Vector3d();
		u3.cross(u1, u2);

		//combine vectors into a matrix
		Matrix3d m = new Matrix3d();
		m.setColumn(0, u1);
		m.setColumn(1, u2);
		m.setColumn(2, u3);
		return m;
	}
	/**
	 * Calculates the transformation matrix to convert between a point on a plane
	 * and its 3D coordinates. The plane is specified by a origin point
	 * and two vectors in the plane. The vectors need not be orthogonal, as
	 * the Gram–Schmidt process is used to create a valid transformation matrix.
	 * <p>
	 * The inverse of the returned matrix can be used to project points
	 * back onto the plane.
	 * @param center A point on the plane to use as the origin for the transformed system
	 * @param p1 A point on the plane
	 * @param v2 A second point on the plane. Should not be on the line between center and p1.
	 * @return The transformation matrix converting the plane's frame of reference
	 *  to cartesian coordinates.
	 */
	public static Matrix4d matrixFromPlane(Point3d center, Point3d p1, Point3d p2) {
		Vector3d v1 = new Vector3d();
		v1.sub(p1, center);
		Vector3d v2 = new Vector3d();
		v2.sub(p2,center);
		Matrix3d rot = matrixFromPlane(v1, v2);
		Matrix4d m = new Matrix4d(rot, new Vector3d(center), 1.);
		return m;
	}
}
