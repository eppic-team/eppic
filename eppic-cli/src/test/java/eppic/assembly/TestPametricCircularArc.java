package eppic.assembly;

import static java.lang.Math.PI;
import static java.lang.Math.sqrt;
import static org.junit.Assert.*;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.junit.Test;

public class TestPametricCircularArc {
	private final double tol = 1e-8;



	/**
	 * Circle in XY plane at origin
	 */
	@Test
	public void testTrivialCircle() {
		final Matrix4d identity = new Matrix4d();
		identity.setIdentity();
		final double sq3 = sqrt(3);

		ParametricCircularArc arc = new ParametricCircularArc(10, PI/3, PI*5/3,identity);

		Point3d expected,actual;
		expected = new Point3d( 5,5*sq3,0);
		actual = arc.getStart();
		assertTrue("Wrong start", expected.epsilonEquals(actual,tol));
		expected = new Point3d( 5,-5*sq3,0);
		actual = arc.getEnd();
		assertTrue("Wrong end", expected.epsilonEquals(actual,tol));
		expected = new Point3d( -10,0,0);
		actual = arc.getMid();
		assertTrue("Wrong mid", expected.epsilonEquals(actual,tol));
		expected = new Point3d( -5,5*sq3,0);
		actual = arc.getRelativePosition(1./4.);
		assertTrue("Wrong fourth", expected.epsilonEquals(actual,tol));
		expected = new Point3d( 0,-10,0);
		actual = arc.getRelativePosition(7./8.);
		assertTrue("Wrong South", expected.epsilonEquals(actual,tol));

		assertEquals("wrong length", 20*PI*2/3, arc.getLength(),tol);
		assertEquals("wrong chord", 10*sq3, arc.getChordLength(),tol);
	}
	/*
	 * half circle, defined from endpoints
	 */
	@Test
	public void testTrivialCircleAB() {
		final double sq2 = sqrt(2);

		Point3d start = new Point3d(10,0,0);
		Point3d end = new Point3d(-10,0,0);
		Vector3d pinnacle = new Vector3d(0,10,0);
		ParametricCircularArc arc = new ParametricCircularArc(start ,end,pinnacle);

		Point3d expected,actual;
		expected = new Point3d( 10,0,0);
		actual = arc.getStart();
		assertTrue("Wrong start", expected.epsilonEquals(actual,tol));
		expected = new Point3d( -10,0,0);
		actual = arc.getEnd();
		assertTrue("Wrong end", expected.epsilonEquals(actual,tol));
		expected = new Point3d(0,10,0);
		actual = arc.getMid();
		assertTrue("Wrong mid", expected.epsilonEquals(actual,tol));
		expected = new Point3d( 10/sq2,10/sq2,0);
		actual = arc.getRelativePosition(1./4.);
		assertTrue("Wrong fourth", expected.epsilonEquals(actual,tol));

		assertEquals("wrong length", 20*PI/2, arc.getLength(),tol);
		assertEquals("wrong chord", 20, arc.getChordLength(),tol);
	}
	/*
	 * Inclined, elevated circle. Values calculated externally.
	 */
	@Test
	public void testInclined() {
		Point3d start = new Point3d(0,1,0);
		Point3d end = new Point3d(10,1,0);
		Vector3d pinnacle = new Vector3d(0,2,3);
		ParametricCircularArc arc = new ParametricCircularArc(start ,end,pinnacle);

		Point3d expected,actual;
		// Circle parameters
		assertEquals("Wrong radius", 19*sqrt(13)/13, arc.getRadius(),tol);
		assertEquals("Wrong start angle",0,arc.getStartAngle(),tol);
		assertEquals("Wrong end angle",2.49901547506,arc.getEndAngle(),tol);

		expected = new Point3d( 5,1./13,-18./13);
		actual = arc.getCenter();
		assertTrue("Wrong center", expected.epsilonEquals(actual,tol));
		// Interpolation
		expected = new Point3d( 0,1,0);
		actual = arc.getStart();
		assertTrue("Wrong start", expected.epsilonEquals(actual,tol));
		expected = new Point3d( 10,1,0);
		actual = arc.getEnd();
		assertTrue("Wrong end", expected.epsilonEquals(actual,tol));
		expected = new Point3d(5,3,3);
		actual = arc.getMid();
		assertTrue("Wrong mid", expected.epsilonEquals(actual,tol));
		expected = new Point3d( 1.91779299852,2.4478515396,2.17177730941);
		actual = arc.getRelativePosition(.25);
		assertTrue("Wrong fourth", expected.epsilonEquals(actual,tol));

		assertEquals("wrong chord", 10, arc.getChordLength(),tol);
	}
}
