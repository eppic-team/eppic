package eppic.assembly.util;

import static org.junit.Assert.assertTrue;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.junit.Test;

import eppic.commons.util.GeomTools;

public class TestGeomTools {
	private final double tol = 1e-8;

	@Test
	public void testMatrixFromPlanePoints() {
		Point3d center, p1, p2;
		Matrix4d result, expected;
		final double sq2 = 1/Math.sqrt(2);

		center = new Point3d(0,0,0);
		p1 = new Point3d(5,0,0);
		p2 = new Point3d(0,15,0);
		expected = new Matrix4d(
				1,0,0,0,
				0,1,0,0,
				0,0,1,0,
				0,0,0,1);
		result = GeomTools.matrixFromPlane(center, p1, p2);
		assertTrue(String.format("Incorrect matrix from %s,%s,%s",center,p1,p2),expected.epsilonEquals(result, tol));

		center = new Point3d(0,0,0);
		p1 = new Point3d(4,0,0);
		p2 = new Point3d(2,2,0);
		expected = new Matrix4d(
				1,0,0,0,
				0,1,0,0,
				0,0,1,0,
				0,0,0,1);
		result = GeomTools.matrixFromPlane(center, p1, p2);
		assertTrue(String.format("Incorrect matrix from %s,%s,%s",center,p1,p2),expected.epsilonEquals(result, tol));

		center = new Point3d(0,0,0);
		p1 = new Point3d(0,4,0);
		p2 = new Point3d(0,2,2);
		expected = new Matrix4d(
				0,0,1,0,
				1,0,0,0,
				0,1,0,0,
				0,0,0,1);
		result = GeomTools.matrixFromPlane(center, p1, p2);
		assertTrue(String.format("Incorrect matrix from %s,%s,%s",center,p1,p2),expected.epsilonEquals(result, tol));

		center = new Point3d(1,1,1);
		p1 = new Point3d(1,4,4);
		p2 = new Point3d(1,0,2);
		expected = new Matrix4d(
				0,0,1,1,
				sq2,-sq2,0,1,
				sq2,sq2,0,1,
				0,0,0,1);
		result = GeomTools.matrixFromPlane(center, p1, p2);
		assertTrue(String.format("Incorrect matrix from %s,%s,%s",center,p1,p2),expected.epsilonEquals(result, tol));
	}
	
	@Test
	public void testMatrixFromPlaneNormal() {
		Point3d center, p, expected;
		Vector3d normal, axis;
		Matrix4d mat;
		int i=0;
		
		// Rotation by 45 deg around y
		center = new Point3d(1,1,1);
		normal = new Vector3d(1,0,1);
		axis = new Vector3d(1,0,0);
		mat = GeomTools.matrixFromPlane(center, normal, axis);
		
		p = new Point3d(0,0,0);
		expected = new Point3d(1,1,1);
		mat.transform(p);
		assertTrue("Test "+(i++),expected.epsilonEquals(p, tol));

		p = new Point3d(Math.sqrt(2),0,0);
		expected = new Point3d(2,1,0);
		mat.transform(p);
		assertTrue("Test "+(i++),expected.epsilonEquals(p, tol));
		
		p = new Point3d(0,0,Math.sqrt(2));
		expected = new Point3d(2,1,2);
		mat.transform(p);
		assertTrue("Test "+(i++),expected.epsilonEquals(p, tol));

		// Normal defaults to x
		Matrix4d mat2 = GeomTools.matrixFromPlane(center, normal, null);
		assertTrue("Default X axis at 45 deg",mat.epsilonEquals(mat2, tol));
		
		center = new Point3d();
		normal = new Vector3d(0,0,1);
		mat = GeomTools.matrixFromPlane(center,normal, null);
		assertTrue("Default X axis",GeomTools.getIdentityMatrix().epsilonEquals(mat, tol));
		
		

	}
}
