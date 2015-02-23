package eppic.assembly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Matrix4d;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.xtal.CrystalBuilder;
import org.biojava.nbio.structure.xtal.CrystalTransform;
import org.biojava.nbio.structure.xtal.SpaceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;



public class LatticeGraph {
	
	private static final Logger logger = LoggerFactory.getLogger(LatticeGraph.class);


	private Graph<ChainVertex,InterfaceEdge> graph;
	
	// We need this to keep track of duplicate edges (i.e. same interface id edges) between 2 chain vertices.
	// The jung implementation of multigraph is not so flexible in what you can do with it:
	// adding equals and hashCode to InterfaceEdge (based on interfaceId) does not do it because
	// the UndirectedOrderedSparseMultigraph implementation requires edges to be globally unique
	private Set<ChainPairInterfaceId> chainpairsInterfaceIds; 
	
	
	public LatticeGraph(Structure struct, StructureInterfaceList interfaces) {
		
		graph = new UndirectedOrderedSparseMultigraph<ChainVertex, InterfaceEdge>();
		chainpairsInterfaceIds = new HashSet<ChainPairInterfaceId>();

		// init the graph
		initLatticeGraph(struct, interfaces);
		
		logger.info("Found {} vertices and {} edges in unit cell", graph.getVertexCount(), graph.getEdgeCount());
		
		List<InterfaceEdge> sortedEdges = new ArrayList<InterfaceEdge>();
		sortedEdges.addAll(graph.getEdges());
		Collections.sort(sortedEdges, new Comparator<InterfaceEdge>() {
			@Override
			public int compare(InterfaceEdge o1, InterfaceEdge o2) {
				return new Integer(o1.getInterfaceId()).compareTo(new Integer(o2.getInterfaceId()));
			}			
		});
		
		for (InterfaceEdge edge:sortedEdges) {
			Pair<ChainVertex> vertices = graph.getEndpoints(edge);
			//logger.info("Edge {} (cluster {}) between {} (entity {}) and {} (entity {})", 
			//		edge.getInterfaceId(), edge.getClusterId(),
			//		vertices.getFirst().getChainId()+vertices.getFirst().getOpId(), vertices.getFirst().getEntity(),
			//		vertices.getSecond().getChainId()+vertices.getSecond().getOpId(), vertices.getSecond().getEntity());
			logger.info("Edge {} between {} - {} ", 
					edge.getInterfaceId(), 
					vertices.getFirst().getChainId()+vertices.getFirst().getOpId(), 
					vertices.getSecond().getChainId()+vertices.getSecond().getOpId());

		}
		
	}
	
	/**
	 * A triplet of 2 chain vertices and an interface id to be able to track duplicate edges.
	 * The 2 chain vertices must occur in same order to be equal.
	 * @author duarte_j 
	 *
	 */
	private class ChainPairInterfaceId {
		public ChainVertex c1;
		public ChainVertex c2;
		public int interfId;
		public ChainPairInterfaceId(ChainVertex c1, ChainVertex c2, int interfId) {
			this.c1 = c1;
			this.c2 = c2;
			this.interfId = interfId;
		}
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof ChainPairInterfaceId)) return false;
			ChainPairInterfaceId o = (ChainPairInterfaceId) other;
			if (!o.c1.equals(this.c1)) {
				return false;
			}
			if (!o.c2.equals(this.c2)) {
				return false;
			}
			if (o.interfId != this.interfId) return false;
			
			return true;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((c1 == null) ? 0 : c1.hashCode());
			result = prime * result + ((c2 == null) ? 0 : c2.hashCode());
			result = prime * result + interfId;
			return result;
		}
	}

	public Graph<ChainVertex, InterfaceEdge> getGraph() {
		return graph;
	}

	private void initLatticeGraph(Structure struct, StructureInterfaceList interfaces) {
		
		// we compare all chains of 0 unit cell to all chains of itself and 1 neighboring cell around
		List<UnitCellChain> iChains = listUnitCells(struct, 0);
		List<UnitCellChain> jChains = listUnitCells(struct, CrystalBuilder.DEF_NUM_CELLS);
		
		for (int i=0;i<iChains.size();i++) {
			for (int j=0;j<jChains.size();j++) {
				if (j<=i) continue;
				
				Chain iChain = iChains.get(i).chain;
				Chain jChain = jChains.get(j).chain;
				String iChainId = iChains.get(i).chain.getChainID();
				String jChainId = jChains.get(j).chain.getChainID();
				
				Matrix4d m0k = getEquivalentAuTransform(iChains.get(i).m, jChains.get(j).m);
				
				int interfaceId = 
						getMatchingInterfaceId(iChainId, jChainId, m0k, interfaces);

				if (interfaceId>0) {
					logger.debug("Interface id {} matched for pair \n{}\n{}{}\n{}",
							interfaceId, iChainId, iChains.get(i).m.toString(), jChainId, jChains.get(j).m.toString());

					ChainVertex ivert = new ChainVertex(iChainId, iChains.get(i).opId);
					ivert.setEntity(iChain.getCompound().getMolId());
					ChainVertex jvert = new ChainVertex(jChainId, jChains.get(j).opId);
					jvert.setEntity(jChain.getCompound().getMolId());
					
					List<ChainVertex> vertexPair = new ArrayList<ChainVertex>();
					vertexPair.add(ivert);
					vertexPair.add(jvert);
					ChainVertex maxVert = Collections.max(vertexPair,new Comparator<ChainVertex>() {
						@Override
						public int compare(ChainVertex o1, ChainVertex o2) {
							if (o1.getChainId().compareTo(o2.getChainId())==0) {
								return new Integer(o1.getOpId()).compareTo(new Integer(o2.getOpId()));
							} 
							return o1.getChainId().compareTo(o2.getChainId());
						}
					});
					ChainVertex minVert = (ivert==maxVert)?jvert:ivert;
					
					InterfaceEdge edge = new InterfaceEdge(interfaceId);
					edge.setClusterId(interfaces.get(interfaceId).getCluster().getId());
					edge.setIsologous(interfaces.get(interfaceId).isIsologous());
					edge.setInfinite(interfaces.get(interfaceId).isInfinite());

					// The jung implementation of multigraph is not so flexible in what you can do with it:
					// adding equals and hashCode to InterfaceEdge (based on interfaceId) does not do it because
					// the UndirectedOrderedSparseMultigraph implementation requires edges to be globally unique

					// Like this we track that no chain pair has 2 edges with the same interface id
					ChainPairInterfaceId triplet = new ChainPairInterfaceId(minVert, maxVert, edge.getInterfaceId());
					if (!chainpairsInterfaceIds.contains(triplet)) {
						graph.addEdge(edge, minVert, maxVert, EdgeType.UNDIRECTED);
						chainpairsInterfaceIds.add(triplet);
					}
					
					
					
				} else {
					logger.debug("No interface id matched for pair \n{}\n{}{}\n{}",
							iChainId, iChains.get(i).m.toString(), jChainId, jChains.get(j).m.toString());

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
	
	private class UnitCellChain {
		public Chain chain;
		public Matrix4d m;
		public int opId;
		public int a;
		public int b;
		public int c;
		
		public UnitCellChain(Chain chain, Matrix4d m, int opId, int a, int b, int c) { 
			this.chain=chain;this.m=m;this.opId=opId;this.a=a;this.b=b;this.c=c; 
		};
		public String toString() { return chain.getChainID()+opId+"("+a+","+b+","+c+")" ;};
	}
	
	private List<UnitCellChain> listUnitCells(Structure struct, int numCells) {
		List<UnitCellChain> list = new ArrayList<LatticeGraph.UnitCellChain>();
		SpaceGroup sg = struct.getCrystallographicInfo().getSpaceGroup();

		for (int a=-numCells;a<=numCells;a++) {
			for (int b=-numCells;b<=numCells;b++) {
				for (int c=-numCells;c<=numCells;c++) {

					for (int opId=0;opId<sg.getNumOperators();opId++) {

						Matrix4d m = (Matrix4d) sg.getTransformation(opId).clone();
						m.m03 += a;
						m.m13 += b;
						m.m23 += c;
						
						for (Chain chain:struct.getChains()) {
							list.add(new UnitCellChain(chain, m, opId, a, b, c));
						}
					}
				}
			}
		}
		return list;
	}
}
