package eppic.assembly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.cycle.PatonCycleBase;
import org.jgrapht.graph.UndirectedSubgraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Assembly of molecules within a crystal, represented by a set of engaged interface clusters.
 * 
 * @author jose
 *
 */
public class Assembly {
	
	private static final Logger logger = LoggerFactory.getLogger(Assembly.class);

	/**
	 * The set of engaged interface clusters, represented as a boolean vector.
	 */
	private boolean[] engagedSet;
	
	private StructureInterfaceList interfaces;
	private UndirectedGraph<ChainVertex,InterfaceEdge> graph;
	
	private int size;
	private String symmetry;
	private String stoichiometry;
	
	public Assembly(StructureInterfaceList interfaces, 
			UndirectedGraph<ChainVertex,InterfaceEdge> graph, boolean[] engagedSet) {
		this.engagedSet = engagedSet;
		this.interfaces = interfaces;
		this.graph = graph;
	}
	
	public List<StructureInterfaceCluster> getInterfaceClusters() {
		List<StructureInterfaceCluster> interfaceClusters = new ArrayList<StructureInterfaceCluster>();
		
		for (StructureInterfaceCluster cluster:interfaces.getClusters()) {
			for (int i=0;i<engagedSet.length;i++) {
				if (engagedSet[i] && cluster.getId() == i+1) {
					interfaceClusters.add(cluster);
				}
			}
		}
		return interfaceClusters;
	}
	
	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public String getSymmetry() {
		return symmetry;
	}
	
	public void setSymmetry(String symmetry) {
		this.symmetry = symmetry;
	}
	
	public String getStoichiometry() {
		return stoichiometry;
	}
	
	public void setStoichiometry(String stoichiometry) {
		this.stoichiometry = stoichiometry;
	}
	
	public int getNumEngagedInterfaceClusters() {
		int count=0;
		for (int i=0;i<engagedSet.length;i++) {
			if (engagedSet[i]) count++;
		}
		return count;
	}
	
	/**
	 * Returns true if this assembly is a child of any of the given parents, false otherwise
	 * @param parents
	 * @return
	 */
	private boolean isChild(List<Assembly> parents) {

		for (Assembly invalidGroup:parents) {
			if (this.isChild(invalidGroup)) return true;
		}
		return false;
	}
	
	/**
	 * Returns true if this assembly is child of the given potentialParent
	 * 
	 * @param potentialParent
	 * @return true if is a child false if not
	 */
	private boolean isChild(Assembly potentialParent) {
		
		for (int i=0;i<this.engagedSet.length;i++) {
			if (potentialParent.engagedSet[i]	&& this.engagedSet[i]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets all children in assembly tree of this assembly, not adding those that are also
	 * children of other invalidParents
	 * @param invalidParents
	 * @return
	 */
	public List<Assembly> getChildren(List<Assembly> invalidParents) {
		
		List<Assembly> children = new ArrayList<Assembly>();

		for (int i=0;i<this.engagedSet.length;i++) {

			if (!this.engagedSet[i]) {
				boolean[] c = this.engagedSet.clone();
				c[i] = true;
				Assembly a = new Assembly(interfaces, graph, c);
				// first we need to check that this is not a child of another parent already known to be invalid
				if (a.isChild(invalidParents)) continue;
				
				children.add(a);
			}
		}

		return children;
	}
	
	/**
	 * Gets the subgraph containing only this assembly's engaged interface clusters
	 * @param clusterId
	 * @return
	 */
	private UndirectedGraph<ChainVertex, InterfaceEdge> getSubgraph() {
		
		Set<ChainVertex> vertexSet = graph.vertexSet();
		Set<InterfaceEdge> edgeSubset = new HashSet<InterfaceEdge>();
		for(InterfaceEdge edge:graph.edgeSet()) {
			for (int i=0;i<this.engagedSet.length;i++) {
				if (this.engagedSet[i]  && edge.getClusterId()==i+1) {
					edgeSubset.add(edge);
				}
			}
		}
		
		UndirectedGraph<ChainVertex, InterfaceEdge> subgraph = 
				new UndirectedSubgraph<ChainVertex, InterfaceEdge>(
						graph, vertexSet, edgeSubset);
				
		
		return subgraph;		
	}
	
	/**
	 * Returns true if this Assembly (i.e. this set of engaged interface clusters)
	 * constitutes a valid assembly topologically and isomorphically (rules iii and iv)
	 * @return
	 */
	public boolean isValid() {
		
		// TODO implement based on rules iii and iv

		
		// first we check for infinites, like that we save to compute the graph cycles for infinite cases
		if (containsInfinites()) {
			logger.info("Discarding assembly {} because it contains infinite interfaces", toString());
			return false;
		}
		
		// then we check the cycles in the graph
		
		UndirectedGraph<ChainVertex,InterfaceEdge> subgraph = getSubgraph();
		
		int numVertices = subgraph.vertexSet().size();
		int numEdges = subgraph.edgeSet().size();
		
		logger.info("Subgraph of assembly {} has {} vertices and {} edges",this.toString(),numVertices, numEdges); 
		
		PatonCycleBase<ChainVertex, InterfaceEdge> paton = new PatonCycleBase<ChainVertex, InterfaceEdge>(subgraph);
		
		List<List<ChainVertex>> cycles = paton.findCycleBase();
		logger.info("{} cycles in total",cycles.size());
		for (List<ChainVertex> cycle:cycles) {
			logger.info("Cycle of size {}", cycle.size());
			StringBuilder sb = new StringBuilder();
			for (ChainVertex c:cycle) {
				sb.append(c.toString()+" -> ");
			}
			logger.info(sb.toString());
		}
		
		// TODO just a place holder for testing: remove!
		return false;
	}
	
	private boolean containsInfinites() {
		for (StructureInterfaceCluster cluster: getInterfaceClusters()) {
			
			for (StructureInterface interf:cluster.getMembers()) {
				// if a single member of cluster is infinite we consider the cluster infinite
				if (interf.isInfinite()) return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean equals(Object other) {
		if (! (other instanceof Assembly)) return false;
		
		Assembly o = (Assembly) other;
		
		return Arrays.equals(this.engagedSet, o.engagedSet);
		
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.engagedSet);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int numClusters = getNumEngagedInterfaceClusters();
		sb.append("{");
		int e = 0;
		for (int i=0;i<engagedSet.length;i++) {
			if (engagedSet[i]) {
				sb.append(i+1);
				e++;
				if (e!=numClusters) sb.append(",");
			}
		}
		sb.append("}");
		return sb.toString();
	}
	
}
