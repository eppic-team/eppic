package eppic.assembly.util;

import static org.junit.Assert.assertTrue;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import org.junit.Test;

import eppic.commons.util.GeomTools;

public class TestGeomTools {
	private final double tol = 1e-8;

	@Test
	public void testMatrixFromPlane() {
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
}
