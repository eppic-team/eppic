package eppic.assembly;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Matrix4d;

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

		for (int i=0;i<sg.getNumOperators();i++) {
			//CrystalTransform t0i = new CrystalTransform(sg, i);
			Matrix4d t0i = sg.getTransformation(i);

			for (int a=-1;a<=1;a++) {
				for (int b=-1;b<=1;b++) {
					for (int c=-1;c<=1;c++) {

						if (a==0 && b==0 && c==0) continue;

						for (int j=0;j<sg.getNumOperators();j++) {
							//CrystalTransform t0j = new CrystalTransform(sg, j);
							Matrix4d t0j = (Matrix4d)sg.getTransformation(j).clone();
							t0j.m03 += a;
							t0j.m13 += b;
							t0j.m23 += c;

							//CrystalTransform t0k = t0i.getEquivalentAuTransform(t0j);
							Matrix4d t0k = getEquivalentAuTransform(t0i, t0j);

							for (Chain iChain:struct.getChains()) {
								String iChainId = iChain.getChainID();
								for (Chain jChain:struct.getChains()) {
									String jChainId = jChain.getChainID();

									int interfaceId = 
											getMatchingInterfaceId(iChainId, jChainId, t0k, interfaces);

									if (interfaceId>0) {
										logger.info("Interface id {} matched for pair \n{}\n{}{}\n{}",
												interfaceId, iChainId, t0i.toString(), jChainId, t0j.toString());

										//if (iChainIdTransf.transform.getTransformId()!=jChainIdTransf.transform.getTransformId()) {
										//	logger.warn("Transform ids are not matching ({}, {}) for matched interface id {}, ",
										//			iChainIdTransf.toString(), jChainIdTransf.toString(),
										//			interfaceId);
										//}
										
										// TODO review this, what exactly is an opId for an interface vertex? how can that be defined?
										// setting it to -1 for the moment
										InterfaceVertex ivert = new InterfaceVertex(-1, interfaceId);
										graph.addVertex(ivert);

										ChainVertex vertA = chainNodes.get(new ChainVertexKey(iChainId, i));
										ChainVertex vertB = chainNodes.get(new ChainVertexKey(jChainId, j));

										InterfaceEdge edgeA = new InterfaceEdge(interfaceId);
										InterfaceEdge edgeB = new InterfaceEdge(interfaceId);

										graph.addEdge(edgeA, vertA, ivert, EdgeType.DIRECTED);
										graph.addEdge(edgeB, vertB, ivert, EdgeType.DIRECTED);
									} else {
										//logger.info("No interface id matched for pair {}  {}",
										//		iChainId+"-"+t0i.toString(), jChainId+"-"+t0j.toString());

									}
								}
							}
						}						
					}					
				}
			}
		}
	}
	
	private int getMatchingInterfaceId(String iChainId, String jChainId, Matrix4d t0k,  
			StructureInterfaceList interfaces) {
		
		// find matching interface id from given list
		
		for (StructureInterface interf:interfaces) {
			
			if ( (interf.getMoleculeIds().getFirst().equals(iChainId) && 
				  interf.getMoleculeIds().getSecond().equals(jChainId)   )  ||
				 (interf.getMoleculeIds().getFirst().equals(jChainId) && 
				  interf.getMoleculeIds().getSecond().equals(iChainId)   )) {

				if (interf.getTransforms().getSecond().getMatTransform().epsilonEquals(t0k, 0.0001)) {
					return interf.getId();
				}

				Matrix4d mul = new Matrix4d();
				mul.mul(interf.getTransforms().getSecond().getMatTransform(), t0k);

				if (mul.epsilonEquals(CrystalTransform.IDENTITY, 0.0001)) {
					return interf.getId();
				}
				
				
			}
		}
		
		// if not found, return -1
		return -1;
	}
	
	/**
	 * Finds the equivalent m0k operator given 2 operators m0i and m0j.
	 * That is, this will find the operator mij expressed in terms of an 
	 * original-AU operator (what we call m0k)
	 * @param m0i
	 * @param m0j
	 * @return the m0k original-AU operator
	 */
	public static Matrix4d getEquivalentAuTransform(Matrix4d m0i, Matrix4d m0j) {
		Matrix4d m0k = new Matrix4d();

		// we first need to find the mij
		// mij = m0j * m0i_inv
		Matrix4d m0iinv = new Matrix4d();
		m0iinv.invert(m0i);

		// Following my understanding, it should be m0j*m0i_inv as indicated above, but that didn't work 
		// and for some reason inverting the order in mul does work. Most likely my understanding is 
		// wrong, but need to check this better at some point
		// A possible explanation for this is that vecmath treats transform() as a pre-multiplication
		// rather than a post-multiplication as I was assuming, i.e. x1 = x0 * m0i instead of x1 = m0i * x0,
		// in that case then it is true that: mij = m0i_inv * m0j, which would explain the expression below
		m0k.mul(m0iinv, m0j);

		return m0k;
	}
	
}
