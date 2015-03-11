package eppic.assembly;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestModuloIssues {

	@Test
	public void testIsInteger() {
		
		double a = 1e-6;
		
		assertTrue( isInteger(a));
		assertTrue( isInteger(-a));
		assertTrue( isInteger(-1-a));
		assertTrue( isInteger(-1+a));
		assertTrue( isInteger(5+a));
		// fails for these four:
		//assertFalse(isInteger(.5));
		//assertFalse(isInteger(-.5));
		//assertFalse(isInteger(.2));
		//assertFalse(isInteger(-.2));
		
		assertTrue(LatticeGraph.isInteger(a));
		assertTrue(LatticeGraph.isInteger(-a));
		assertTrue(LatticeGraph.isInteger(-1-a));
		assertTrue(LatticeGraph.isInteger(-1+a));
		assertTrue(LatticeGraph.isInteger(5+a));
		
		assertFalse(LatticeGraph.isInteger(.5));
		assertFalse(LatticeGraph.isInteger(-.5));
		assertFalse(LatticeGraph.isInteger(.2));
		assertFalse(LatticeGraph.isInteger(-.2));
	
	}
	
	private boolean isInteger(double x) {
		//System.out.println(x%1);
		return ((int) (x%1)) == 0;	
	}

	
}
