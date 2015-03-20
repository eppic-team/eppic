package eppic.assembly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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


	private Structure struct;
	private StructureInterfaceList interfaces;
	
	private UndirectedGraph<ChainVertex,InterfaceEdge> graph;
	
	private SpaceGroup sg;
	
	private Map<String, Integer> chainIds2entityIds;
	
	
	public LatticeGraph(Structure struct, StructureInterfaceList interfaces) {
			
		this.struct = struct;
		this.interfaces = interfaces;		
		
		this.graph = new Pseudograph<ChainVertex, InterfaceEdge>(InterfaceEdge.class);
		
		this.sg = struct.getCrystallographicInfo().getSpaceGroup();

		this.chainIds2entityIds = new HashMap<String, Integer>();
		
		initLatticeGraphTopologically();
		logGraph();
		
	}
	
	public UndirectedGraph<ChainVertex, InterfaceEdge> getGraph() {
		return graph;
	}
	
	private void logGraph() {
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
	
	private void initLatticeGraphTopologically() {		
		
		
		
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
				
				// Cij = Ci * Tj
				Matrix4d Cij = new Matrix4d(Ci);
				Cij.mul(Tj);
				
				// with Cij we obtain the end operator id
				int k = getEndAuCell(Cij);
				
				// translation is given by: X = Tkinv * Ci * Tj
				Matrix4d X = new Matrix4d(sg.getTransformation(k));
				X.invert(); // Tkinv
				X.mul(Cij);
				
				Point3i xtalTrans = new Point3i(
						(int) Math.round(X.m03), (int) Math.round(X.m13), (int) Math.round(X.m23));

				InterfaceEdge edge = new InterfaceEdge(interf, xtalTrans);
				
				String sourceChainId = interf.getMoleculeIds().getFirst();
				String targetChainId = interf.getMoleculeIds().getSecond();
				ChainVertex sVertex = new ChainVertex(sourceChainId, j, chainIds2entityIds.get(sourceChainId));
				ChainVertex tVertex = new ChainVertex(targetChainId, k, chainIds2entityIds.get(targetChainId));
				
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
		}
		
		logger.warn("No matching generator found for operator:\n {}", m.toString());
		return -1;
	}
	
	private static boolean epsilonEqualsModulusXtal(Matrix4d T, Matrix4d m) {
		
		// T == m  <=>  T - m = 0
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
			
			if (!isInteger(sub.getElement(i,3))) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isInteger(double x) {
		// note that (x%1)==0 would not work, see test TestModuloIssues
		return Math.abs(Math.round(x)-x) < 0.0001;
	}
	
	/**
	 * For any 2 vertices in the graph that contain 2 or more edges with the same 
	 * interface id, remove all but first edges. 
	 */
	public void removeDuplicateEdges() {
		
		Set<InterfaceEdge> toRemove = new HashSet<InterfaceEdge>();
		
		int i = -1;
		for (ChainVertex iVertex:graph.vertexSet()) {
			i++;
			int j = -1;
			for (ChainVertex jVertex:graph.vertexSet()) {
				j++;
				if (j<i) continue; // i.e. we include i==j (to remove loop edges)
				
				Set<InterfaceEdge> edges = graph.getAllEdges(iVertex, jVertex);
				Map<Integer,Set<InterfaceEdge>> groups = groupIntoTypes(edges);
				
				for (int interfaceId:groups.keySet()){
					Set<InterfaceEdge> group = groups.get(interfaceId);
					
					if (group.size()==0) {
						continue;
					} else if (group.size()==1) {
						continue;
					} else if (group.size()>2) {
						// we warn for more than 2 edges, that should not occur
						logger.warn("More than 2 edges with interface id {} between vertices {},{}",
								interfaceId,iVertex.toString(),jVertex.toString()); 
					}
					// now we are in case 2 or more edges 
					// we keep first and remove the rest
					Iterator<InterfaceEdge> it = group.iterator();
					it.next(); // first edge: we keep it
					while (it.hasNext()) {						
						InterfaceEdge edge = it.next();
						toRemove.add(edge);
						logger.info("Removed edge with interface id {} between vertices {},{} ", 
								interfaceId,iVertex.toString(),jVertex.toString());
					}

				}
				
								
			}
			
		}
		// now we do the removal
		for (InterfaceEdge edge:toRemove) {
			graph.removeEdge(edge);
		}
		
	}
	
	/**
	 * Given a set of edges groups them into interface id groups
	 * @param edges
	 * @return a map of interface ids to sets of edges with the corresponding interface id
	 */
	private Map<Integer,Set<InterfaceEdge>> groupIntoTypes(Set<InterfaceEdge> edges) {
		Map<Integer,Set<InterfaceEdge>> map = new HashMap<Integer,Set<InterfaceEdge>>();
		
		for (InterfaceEdge edge:edges) {
			Set<InterfaceEdge> set = null;
			if (!map.containsKey(edge.getInterfaceId())) {
				set = new HashSet<InterfaceEdge>();
				map.put(edge.getInterfaceId(), set);
			} else {
				set = map.get(edge.getInterfaceId());
			}
			set.add(edge);
			
		}
		return map;
	}
	
}
