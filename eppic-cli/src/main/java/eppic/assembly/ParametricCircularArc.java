package eppic.assembly;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import eppic.commons.util.GeomTools;

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
		this(1, 0, 2*PI, GeomTools.getIdentityMatrix() );
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
		transformation = GeomTools.matrixFromPlane(center, start, ch);

		startAngle = 0;
		// ||ab||^2 = ||ca||^2+||cb||^2-2*||ca||*||cb||*cos(acb)
		endAngle = Math.acos(1-len2/(2*radius*radius));
		// detect if our arc goes the long way around
		if( radius*radius < h2) {
			endAngle = 2*PI-endAngle;
		}
	}

	public ParametricCircularArc(Point3d start, Point3d end, double height) {
		this(start,end, GeomTools.randomOrthogonalVector(start,end,height) );
	}

	/** Copy constructor */
	public ParametricCircularArc(ParametricCircularArc o) {
		this.radius = o.radius;
		this.startAngle = o.startAngle;
		this.endAngle = o.endAngle;
		this.transformation = new Matrix4d(o.transformation);
		this.uniqueName = o.uniqueName;
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

	public void shrinkAbsolute(double abslen) {
		double angle = abslen/radius;
		startAngle += angle;
		endAngle -= angle;
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

}
