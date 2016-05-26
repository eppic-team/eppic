package eppic.assembly;

import javax.vecmath.Point3d;

public class OrientedCircle {
	private Point3d center;
	private Point3d perpendicular;
	private double radius;
	private String uniqueName;
	
	public OrientedCircle(Point3d center, Point3d perpendicular, double radius) {
		this.center = center;
		this.perpendicular = perpendicular;
		this.radius = radius;
	}
	
	/**
	 * Copy constructor
	 * @param cir
	 */
	public OrientedCircle(OrientedCircle c) {
		this.center = new Point3d(c.center);
		this.perpendicular = new Point3d(c.perpendicular);
		this.radius = c.radius;
		this.uniqueName = c.uniqueName;
	}

	public Point3d getCenter() {
		return center;
	}
	public void setCenter(Point3d center) {
		this.center = center;
	}
	public Point3d getPerpendicular() {
		return perpendicular;
	}
	public void setPerpendicular(Point3d perpendicular) {
		this.perpendicular = perpendicular;
	}
	public double getRadius() {
		return radius;
	}
	public double getDiameter() {
		return radius*2;
	}
	public void setRadius(double radius) {
		this.radius = radius;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((center == null) ? 0 : center.hashCode());
		result = prime * result
				+ ((perpendicular == null) ? 0 : perpendicular.hashCode());
		long temp;
		temp = Double.doubleToLongBits(radius);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OrientedCircle other = (OrientedCircle) obj;
		if (center == null) {
			if (other.center != null)
				return false;
		} else if (!center.equals(other.center))
			return false;
		if (perpendicular == null) {
			if (other.perpendicular != null)
				return false;
		} else if (!perpendicular.equals(other.perpendicular))
			return false;
		if (Double.doubleToLongBits(radius) != Double
				.doubleToLongBits(other.radius))
			return false;
		return true;
	}
	
	/**
	 * Returns whether two circles are approximately equivalent, e.g. that their
	 * position and norms are equal to within the specified tolerance.
	 * @param o
	 * @param tol
	 * @return
	 */
	public boolean fuzzyEquals(OrientedCircle o,double tol) {
		//TODO 
		return equals(o);
	}
}
