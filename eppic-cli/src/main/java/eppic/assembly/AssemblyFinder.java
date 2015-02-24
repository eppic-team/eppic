package eppic.assembly;

import java.util.List;

import org.apache.commons.collections15.Predicate;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.algorithms.filters.EdgePredicateFilter;
import edu.uci.ics.jung.graph.Graph;

public class AssemblyFinder {

	private static final Logger logger = LoggerFactory.getLogger(AssemblyFinder.class);
	
	private LatticeGraph lattice;	
	private StructureInterfaceList interfaces;
	
	private int numEntities;
	
	public AssemblyFinder(LatticeGraph lattice, StructureInterfaceList interfaces, Structure structure) {
		this.lattice = lattice;
		this.interfaces = interfaces;
		
		this.numEntities = structure.getCompounds().size();
	}
	
	public int getNumEntities() {
		return numEntities;
	}
	
	/**
	 * Returns all topologically valid assemblies present in the crystal.
	 * @return
	 */
	public List<Assembly> getValidAssemblies() {
		// TODO implement: return all topologically valid assemblies
		
		for (StructureInterfaceCluster interf:interfaces.getClusters()) {
			if (isValidAssemblyInterface(interf)) {
				logger.info("Interface cluster {} is a valid assembly interface", interf.getId());
			}
		}
		return null;
	}
	
	private boolean isValidAssemblyInterface(StructureInterfaceCluster cluster) {
		// pure infinite interfaces (translations and screw rotations between same chains)
		if (isInfinite(cluster)) return false;
		
		// isologous is always a valid interface
		if (isIsologous(cluster)) return true;
		
		// all other cases: heterelogous interfaces
		
		// is it forming a closed symmetry? if yes it is valid, otherwise it's not usable
		
		return isCycle(getSubgraph(cluster.getId()));
	}
	
	/**
	 * Gets the subgraph containing only the given cluster id edges
	 * @param clusterId
	 * @return
	 */
	private Graph<ChainVertex, InterfaceEdge> getSubgraph(final int clusterId) {
		EdgePredicateFilter<ChainVertex, InterfaceEdge> edgeFilter = 
				new EdgePredicateFilter<ChainVertex, InterfaceEdge>(new Predicate<InterfaceEdge>() {

					@Override
					public boolean evaluate(InterfaceEdge edge) {
						return edge.getClusterId()==clusterId;
					}
					
				});
		
		Graph<ChainVertex,InterfaceEdge> graph = lattice.getGraph();
		return edgeFilter.transform(graph);		
	}
	
	private boolean isCycle(Graph<ChainVertex, InterfaceEdge> subgraph) {
		int vertexCount= subgraph.getVertexCount();
		int edgeCount = subgraph.getEdgeCount();
		
		// necessary but not sufficient condition
		if (vertexCount!=edgeCount) return false;
		
		for (ChainVertex vertex:subgraph.getVertices()) {
			if (subgraph.getNeighborCount(vertex)!=2) return false;
		}
		return true;
	}
	
	private static boolean isInfinite(StructureInterfaceCluster cluster) {
		int infiniteCount = 0;
		for (StructureInterface interf:cluster.getMembers()) {
			if (interf.isInfinite()) infiniteCount++;
		}
		if (infiniteCount>0) {			
			if (infiniteCount!=cluster.getMembers().size()) {
				// as infinites are detected only for perfect CS, this can happen often: info
				logger.info("Interface cluster {} contains both infinite and non-infinite interfaces, considering it infinite.",cluster.getId());
			}
			return true;
		}
		return false;
	}
	
	private static boolean isIsologous(StructureInterfaceCluster cluster) {
		int isologousCount = 0;
		for (StructureInterface interf:cluster.getMembers()) {
			if (interf.isIsologous()) isologousCount++;
		}
		if (isologousCount>0) {			
			if (isologousCount!=cluster.getMembers().size()) {
				// isologous are detected through aproximate matching of interfaces, thus this should not really happen: warn!
				logger.warn("Found a mix of isologous and heterologous interfaces in cluster {}. Considering it isologous.",cluster.getId());
			}
			return true;
		}
		return false;
	}
}
