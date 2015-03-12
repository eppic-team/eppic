package eppic.assembly;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.vecmath.Point3i;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureIO;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.biojava.nbio.structure.xtal.CrystalBuilder;

import org.junit.Test;

public class TestGraph {

	@Test
	public void testInterfaceEdge() throws IOException, StructureException {
		AtomCache cache = new AtomCache();
		FileParsingParameters params = new FileParsingParameters();
		params.setAlignSeqRes(true); 
		cache.setFileParsingParams(params);
		
		Structure s =  StructureIO.getStructure("1smt");
		
		CrystalBuilder cb = new CrystalBuilder(s);
		StructureInterfaceList interfaces = cb.getUniqueInterfaces();
		
		
		// we need InterfaceEdge to be unique per unique object, all the lattice graph code 
		// is implemented with that assumption in mind
		// This test checks for that, in case someone decides to add an equals() and hashCode() in InterfaceEdge
		InterfaceEdge edge1 = new InterfaceEdge(interfaces.get(1),new Point3i(0,0,0));
		InterfaceEdge edge2 = new InterfaceEdge(interfaces.get(1),new Point3i(0,0,0));
		
		InterfaceEdge edge3 = edge1;
		
		assertNotEquals(edge1,edge2);
		
		assertEquals(edge1, edge3);
		
		//LatticeGraph lg = new LatticeGraph(s, interfaces);
		
		//UndirectedGraph<ChainVertex, InterfaceEdge> g = lg.getGraph();		
		
		//for (InterfaceEdge e : g.edgeSet()) {
		//	
		//}
		
		
	}

}
