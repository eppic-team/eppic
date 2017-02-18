package eppic.commons.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureTools;

/**
 * Helper functions for geometric calculations
 * 
 * @author Spencer Bliven
 *
 */
public class GeomTools {
	/**
	 * Generate a vector perpendicular to the input vector, in a random direction.
	 * 
	 * Used for randomly orienting arcs.
	 * @param n The plane normal
	 * @return A random normalized vector orthogonal to n
	 */
	public static Vector3d randomOrthogonalVector(Vector3d n) {
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
	public static Vector3d randomOrthogonalVector(Vector3d n,double length) {
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
	public static Vector3d randomOrthogonalVector(Point3d start, Point3d end,
			double height) {
		Vector3d ab = new Vector3d();
		ab.sub(end,start);
		return randomOrthogonalVector(ab,height);
	}
	
	/**
	 * Get the identity matrix
	 */
	public static Matrix4d getIdentityMatrix() {
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
	
	/**
	 * Yet another way to construct an orthonormal orientation matrix, this time
	 * using the plane normal rather than two vectors in the plane.
	 * @param center Origin point
	 * @param normal Vector normal to the plane
	 * @param axis (Optional) A reference axis. If null, chooses an axis based on
	 *  either the x or the y axes, depending on the orientation of the plane.
	 * @return The transformation matrix converting the plane's frame of reference
	 *  to cartesian coordinates.
	 */
	public static Matrix4d matrixFromPlane(Point3d center, Vector3d normal, Vector3d axis) {

		normal = new Vector3d(normal);
		normal.normalize();
		
		// Default axis to X (or Y if normal is X)
		if( axis == null ) {
			axis = new Vector3d(1,0,0);
			// If the normal points generally towards x
			if( Math.abs(axis.dot(normal)) > 1/Math.sqrt(2) )
				axis = new Vector3d(0,1,0);
		}

		// Rotation matrix for a permuted coordinate system with normal along the x
		// and axis along the y (e.g. [Z|X|Y])
		Matrix3d ortho = matrixFromPlane(normal, axis);

		Matrix4d mat = new Matrix4d(
				ortho.m01, ortho.m02, ortho.m00, center.x,
				ortho.m11, ortho.m12, ortho.m10, center.y,
				ortho.m21, ortho.m22, ortho.m20, center.z,
				0,0,0,1 );
		return mat;
	}
	
	/**
	 * Calculate the centroid for a chain
	 * @param c Chain
	 * @return centroid for all representative atoms
	 */
	public static Point3d getCentroid(Chain c) {
		Atom[] ca = StructureTools.getRepresentativeAtomArray(c);
		if (ca.length==0) {
			// if no representative atoms found let's try with all atoms, issue #167
			ca = StructureTools.getAllAtomArray(c);
		}
		Atom centroidAtom = Calc.getCentroid(ca);
		return new Point3d(centroidAtom.getCoords());
	}
	/**
	 * Calculate the centroid for a whole structure
	 * @param s
	 * @return centroid for all representative atoms
	 */
	public static Point3d getCentroid(Structure s) {
		Atom[] ca = StructureTools.getRepresentativeAtomArray(s);
		Atom centroidAtom = Calc.getCentroid(ca);
		return new Point3d(centroidAtom.getCoords());
	}
	
	/**
	 * Calculate the centroid for a list of chains
	 * @param chains
	 * @return centroid for all representative atoms
	 */
	public static Point3d getCentroid(List<Chain> chains) {
		List<Atom> atoms = new ArrayList<>();
		for(Chain c : chains) {
			Atom[] ca = StructureTools.getRepresentativeAtomArray(c);
			Collections.addAll(atoms, ca);
		}
		Atom centroidAtom = Calc.getCentroid(atoms.toArray(new Atom[atoms.size()]));
		return new Point3d(centroidAtom.getCoords());
	}
}
