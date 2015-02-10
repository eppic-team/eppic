package eppic.assembly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3i;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.xtal.CrystalTransform;
import org.biojava.nbio.structure.xtal.SpaceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import eppic.analysis.compare.InterfaceMatcher;



public class LatticeGraph {
	private static final Logger logger = LoggerFactory.getLogger(LatticeGraph.class);


	protected static class ChainVertexKey {
		public int opId;
		public String chainId;
		public ChainVertexKey(String chainId,int opId) {
			this.opId = opId;
			this.chainId = chainId;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((chainId == null) ? 0 : chainId.hashCode());
			result = prime * result + opId;
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
			ChainVertexKey other = (ChainVertexKey) obj;
			if (chainId == null) {
				if (other.chainId != null)
					return false;
			} else if (!chainId.equals(other.chainId))
				return false;
			if (opId != other.opId)
				return false;
			return true;
		}

	}

	private Graph<LatticeGraphVertex,InterfaceEdge> graph;
	
	// Maps chainId and unit cell operator id to a vertex
	private Map<ChainVertexKey, ChainVertex> chainNodes;
	
	
	public LatticeGraph(Structure struct, StructureInterfaceList interfaces) {
		graph = new DirectedOrderedSparseMultigraph<LatticeGraphVertex, InterfaceEdge>();
		chainNodes = new HashMap<ChainVertexKey,ChainVertex>();

		// Begin SciFi comments
		// SPACE OPS! Transform!
		Matrix4d[] spaceOps = struct.getCrystallographicInfo().getTransformationsOrthonormal();

		// Generate vertices for unit cell
		initChainVertices(struct, spaceOps);
		logger.info("Found "+graph.getVertexCount()+" chains in unit cell");

		// add the edges
		initLatticeGraph(struct, interfaces);
		
		logger.info("Found {} vertices and {} edges in unit cell", graph.getVertexCount(), graph.getEdgeCount()); 
		
	}

	
	
	/**
	 * Initialize the ChainVertex nodes of the graph
	 * @param struc
	 * @param spaceOps
	 */
	private void initChainVertices(Structure struc, Matrix4d[] spaceOps) {
		
		for(Chain c : struc.getChains()) {
			
			String chainId = c.getChainID();
			
			for(int opId = 0; opId < spaceOps.length; opId++) {
				// Create new vertex & add to the graph
				ChainVertex vert = new ChainVertex(chainId,opId);
				
				vert.setEntity(c.getCompound().getMolId());
				
				chainNodes.put(new ChainVertexKey(chainId,opId), vert);
				graph.addVertex(vert);
			}

		}
	}


	
	public Graph<LatticeGraphVertex, InterfaceEdge> getGraph() {
		return graph;
	}

	private void initLatticeGraph(Structure struct, StructureInterfaceList interfaces) {
		
		SpaceGroup sg = struct.getCrystallographicInfo().getSpaceGroup();
		
		for (int a=-1;a<=1;a++) {
			for (int b=-1;b<=1;b++) {
				for (int c=-1;c<=1;c++) {
					
					if (a==0 && b==0 && c==0) continue;
					
					for (ChainIdTransform iChainIdTransf : getChainIdTransforms(sg, struct, a, b, c)) {
						for (ChainIdTransform jChainIdTransf : getChainIdTransforms(sg, struct, 0, 0, 0)) {
					
							int interfaceId = getMatchingInterfaceId(iChainIdTransf, jChainIdTransf, interfaces);
							if (interfaceId>0) {
								logger.info("Interface id {} matched for pair {}  {}",
										interfaceId, iChainIdTransf.toString(), jChainIdTransf.toString());
								
								if (iChainIdTransf.transform.getTransformId()!=jChainIdTransf.transform.getTransformId()) {
									logger.warn("Transform ids are not matching ({}, {}) for matched interface id {}, ",
											iChainIdTransf.toString(), jChainIdTransf.toString(),
											interfaceId);
								}
								InterfaceVertex ivert = 
									new InterfaceVertex(iChainIdTransf.transform.getTransformId(), interfaceId);
								graph.addVertex(ivert);
								
								ChainVertex vertA = chainNodes.get(new ChainVertexKey(iChainIdTransf.chainId, iChainIdTransf.transform.getTransformId()));
								ChainVertex vertB = chainNodes.get(new ChainVertexKey(jChainIdTransf.chainId, jChainIdTransf.transform.getTransformId()));

								InterfaceEdge edgeA = new InterfaceEdge(interfaceId);
								InterfaceEdge edgeB = new InterfaceEdge(interfaceId);
								
								graph.addEdge(edgeA, vertA, ivert, EdgeType.DIRECTED);
								graph.addEdge(edgeB, vertB, ivert, EdgeType.DIRECTED);
							} else {
								//logger.info("No interface id matched for pair {}  {}",
								//		iChainIdTransf.toString(), jChainIdTransf.toString());
								
							}

						}
					}
				}
			}
		}
	}
	
	private int getMatchingInterfaceId(ChainIdTransform iChainIdTransform, ChainIdTransform jChainIdTransform, 
			StructureInterfaceList interfaces) {
		
		// 1 find Tij transform from given T0i and T0j
		Matrix4d m = InterfaceMatcher.findTransf12(
				iChainIdTransform.transform.getMatTransform(),
				jChainIdTransform.transform.getMatTransform());
		
		// 2 find matching interface id from given list
		
		for (StructureInterface interf:interfaces) {
			
			if ( (interf.getMoleculeIds().getFirst().equals(iChainIdTransform.chainId) && 
				  interf.getMoleculeIds().getSecond().equals(jChainIdTransform.chainId)   )  ||
				 (interf.getMoleculeIds().getFirst().equals(jChainIdTransform.chainId) && 
				  interf.getMoleculeIds().getSecond().equals(iChainIdTransform.chainId)   )) {

				if (interf.getTransforms().getSecond().getMatTransform().epsilonEquals(m, 0.0001)) {
					return interf.getId();
				}

				Matrix4d mul = new Matrix4d();
				mul.mul(interf.getTransforms().getSecond().getMatTransform(), m);

				if (mul.epsilonEquals(CrystalTransform.IDENTITY, 0.0001)) {
					return interf.getId();
				}
				
				
			}
		}
		
		// if not found, return -1
		return -1;
	}
	
	private List<ChainIdTransform> getChainIdTransforms(SpaceGroup sg, Structure struct, int a, int b, int c) {
		List<ChainIdTransform> list = new ArrayList<ChainIdTransform>();
		for (int j=0;j<sg.getNumOperators();j++) {
			
			for (Chain jChain:struct.getChains()) {
				String jChainId = jChain.getChainID();
				
				CrystalTransform tij = new CrystalTransform(sg, j);
				tij.translate(new Point3i(a,b,c));
				
				list.add(new ChainIdTransform(jChainId, tij));
			}
		}
		return list;
	}
	
	private class ChainIdTransform {
		String chainId;
		CrystalTransform transform;
		public ChainIdTransform(String chainId, CrystalTransform transform) {
			this.chainId = chainId;
			this.transform = transform;
		}
		
		public String toString() {
			return chainId+transform.getTransformId()+
					"("+
					transform.getCrystalTranslation().x+","+
					transform.getCrystalTranslation().y+","+
					transform.getCrystalTranslation().z+")";
		}
	}
	
}
