package eppic.assembly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3i;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.xtal.SpaceGroup;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class LatticeGraph {
	
	private static final Logger logger = LoggerFactory.getLogger(LatticeGraph.class);


	private UndirectedGraph<ChainVertex,InterfaceEdge> graph;
	
	private SpaceGroup sg;
	
	
	public LatticeGraph(Structure struct, StructureInterfaceList interfaces) {
				
		graph = new Pseudograph<ChainVertex, InterfaceEdge>(InterfaceEdge.class);
		
		sg = struct.getCrystallographicInfo().getSpaceGroup();

		// init the graph
		initLatticeGraph(struct, interfaces);
		
		logger.info("Found {} vertices and {} edges in unit cell", graph.vertexSet().size(), graph.edgeSet().size());
		
		List<InterfaceEdge> sortedEdges = new ArrayList<InterfaceEdge>();
		sortedEdges.addAll(graph.edgeSet());
		Collections.sort(sortedEdges, new Comparator<InterfaceEdge>() {
			@Override
			public int compare(InterfaceEdge o1, InterfaceEdge o2) {
				return new Integer(o1.getInterfaceId()).compareTo(new Integer(o2.getInterfaceId()));
			}			
		});
		
		for (InterfaceEdge edge:sortedEdges) {
			ChainVertex first = graph.getEdgeSource(edge);
			ChainVertex second = graph.getEdgeTarget(edge);
			Point3i xtalT = edge.getXtalTrans();
			logger.info("Edge {} ({}) between {} ({}) - {} ({})"+
					String.format(" [%2d,%2d,%2d]", xtalT.x,xtalT.y,xtalT.z), 
					edge.getInterfaceId(),
					edge.getClusterId(),
					first.getChainId()+first.getOpId(), 
					first.getEntity(),
					second.getChainId()+second.getOpId(),
					second.getEntity());

		}
		
	}
	
	public UndirectedGraph<ChainVertex, InterfaceEdge> getGraph() {
		return graph;
	}
	
	private void initLatticeGraph(Structure struct, StructureInterfaceList interfaces) {		
		
		Map<String, Integer> chainIds2entityIds = new HashMap<String, Integer>();
		
		for (Chain c:struct.getChains()) {
			for (int i=0;i<sg.getNumOperators();i++) {
				graph.addVertex(new ChainVertex(c.getChainID(), i, c.getCompound().getMolId()));
				chainIds2entityIds.put(c.getChainID(), c.getCompound().getMolId());
			}
		}
		
		for (StructureInterface interf:interfaces) {
			Matrix4d Ci = interf.getTransforms().getSecond().getMatTransform();
			
			for (int j=0;j<sg.getNumOperators();j++) {
				Matrix4d Tj = sg.getTransformation(j);
				
				Matrix4d Cij = new Matrix4d(Ci);
				Cij.mul(Tj);
				
				int endOpId = getEndAuCell(Cij);
				
				// we calculate translation by obtaining the matrix Ci expressed in basis Tj: 
				// Ciprime = Tj * Ci * Tjinv
				// see for instance http://en.wikipedia.org/wiki/Change_of_basis#The_matrix_of_an_endomorphism
				Matrix4d Tjinv = new Matrix4d(Tj);				
				Matrix4d Ciprime = new Matrix4d();
				Ciprime.mul(Tj, Ci);
				Ciprime.mul(Tjinv);
				
				Point3i xtalTrans = new Point3i(
						(int) Math.round(Ciprime.m03), (int) Math.round(Ciprime.m13), (int) Math.round(Ciprime.m23));
				
				InterfaceEdge edge = new InterfaceEdge(interf, xtalTrans);
				
				String sourceChainId = interf.getMoleculeIds().getFirst();
				String targetChainId = interf.getMoleculeIds().getSecond();
				ChainVertex sVertex = new ChainVertex(sourceChainId, j, chainIds2entityIds.get(sourceChainId));
				ChainVertex tVertex = new ChainVertex(targetChainId, endOpId, chainIds2entityIds.get(targetChainId));
				
				graph.addEdge(sVertex, tVertex, edge);
				
			}
		}
		
	}
	
	/**
	 * Given an operator, returns the operator id of the matching generator
	 * @param m
	 * @return
	 */
	private int getEndAuCell(Matrix4d m) {
		
		for (int j=0;j<sg.getNumOperators();j++) {
			Matrix4d Tj = sg.getTransformation(j);
			
			if (epsilonEqualsModulusXtal(Tj, m)) {
				return j;
			}
			// inverse does not seem to be necessary
			//else if (epsilonEqualsModulusXtal(Tj, mInv)) {
			//	return j;
			//}
		}
		
		logger.warn("No matching generator found for operator {}", m.toString());
		return -1;
	}
	
	private static boolean epsilonEqualsModulusXtal(Matrix4d T, Matrix4d m) {
		
		Matrix4d sub = new Matrix4d(T);
		sub.sub(m);
		
		for (int i=0;i<3;i++) {
			for (int j=0;j<3;j++) {
				if (Math.abs(sub.getElement(i, j))>0.0001) {
					return false;
				}
			}
		}
		
		for (int i=0;i<3;i++) {

			// making sure we are numerically stable with the modulo operator
			
			double x = sub.getElement(i,3) % 1;

			if (x+(x<0?1:0) > .0001) {
				return false;
			}
		}
		
		return true;
	}
	
}
