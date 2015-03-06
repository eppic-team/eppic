package eppic.assembly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.vecmath.Point3i;

import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
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
	
	private int totalNumEntities;
	
	private int size;
	private String symmetry;
	private int[] stoichiometry;
	
	public Assembly(StructureInterfaceList interfaces, 
			UndirectedGraph<ChainVertex,InterfaceEdge> graph, boolean[] engagedSet, int totalNumEntities) {
		this.engagedSet = engagedSet;
		this.interfaces = interfaces;
		this.graph = graph;
		this.totalNumEntities = totalNumEntities;
		
		this.size = -1;
		this.symmetry = null;
		this.stoichiometry = null;
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
	
	public String getSymmetry() {
		return symmetry;
	}
		
	public int[] getStoichiometry() {
		if (stoichiometry!=null) return stoichiometry;
		
		UndirectedGraph<ChainVertex, InterfaceEdge> subgraph = getSubgraph();
		
		ConnectivityInspector<ChainVertex, InterfaceEdge> ci = new ConnectivityInspector<ChainVertex, InterfaceEdge>(subgraph);
		
		List<Set<ChainVertex>> ccs = ci.connectedSets();
		
		logger.info("Total of {} connected components", ccs.size());
		List<int[]> stoichiometries = new ArrayList<int[]>();
		for (Set<ChainVertex> cc:ccs) {			
			int [] s = new int[totalNumEntities];
			stoichiometries.add(s);
			for (ChainVertex v:cc) {
				// note: this relies on mol ids in the PDB being 1 to n, that might not be true, we need to check!
				s[v.getEntity()-1]++;
			}
			logger.info("Stoichiometry of connected component: {}", Arrays.toString(s));
		}
		
		// we assign the first stoichiometry found, and check and warn if the others are different 
		stoichiometry = stoichiometries.get(0);
		for (int i=1;i<stoichiometries.size();i++) {
			if (!Arrays.equals(stoichiometry, stoichiometries.get(i))) {
				logger.warn("Stoichiometry {} ({}) does not coincide with stoichiometry 0 ({})",
						i, Arrays.toString(stoichiometries.get(i)), Arrays.toString(stoichiometry));
			}
		}
		
		
		return stoichiometry;
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
				Assembly a = new Assembly(interfaces, graph, c, totalNumEntities);
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
		
		// TODO rule iii is still missing here

		
		// first we check for infinites, like that we save to compute the graph cycles for infinite cases
		if (containsInfinites()) {
			logger.info("Discarding assembly {} because it contains infinite interfaces", toString());
			return false;
		}
		
		return isClosedSymmetry();
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

	private boolean isClosedSymmetry() {
		
		// pre-check for assemblies with 1 engaged interface that is isologous: the cycle detection doesn't work for isologous
		if (getNumEngagedInterfaceClusters()==1) {
			for (StructureInterface interf : getInterfaceClusters().get(0).getMembers()) {
				// with a single interface in cluster isologous, we call the whole isologous
				if (interf.isIsologous()) {
					logger.info("Assembly {} contains just 1 isologous interface cluster: closed symmetry, won't check cycles",toString());
					return true;
				}
			}
		}
		
		// we check the cycles in the graph and whether they stay in same cell

		UndirectedGraph<ChainVertex,InterfaceEdge> subgraph = getSubgraph();

		int numVertices = subgraph.vertexSet().size();
		int numEdges = subgraph.edgeSet().size();

		logger.info("Subgraph of assembly {} has {} vertices and {} edges",this.toString(),numVertices, numEdges); 
		
		getStoichiometry();

		PatonCycleBase<ChainVertex, InterfaceEdge> paton = new PatonCycleBase<ChainVertex, InterfaceEdge>(subgraph);

		List<List<ChainVertex>> cycles = paton.findCycleBase();
		
		if (cycles.size()==0) {
			// no cycles at all: can't be closed!
			logger.info("No cycles in assembly {}: discarding because it can't be a closed-symmetry", toString());
			return false;
		}
		
		logger.info("{} cycles in total",cycles.size());
		
		// TODO check that all cycles are isomorphous, if they aren't this can't be an assembly
		
		for (List<ChainVertex> cycle:cycles) {
			logger.info("Cycle of size {}", cycle.size());
			StringBuilder sb = new StringBuilder();
			for (ChainVertex c:cycle) {
				sb.append(c.toString()+" -> ");
			}
			logger.info(sb.toString());
			
			if (isZeroTranslation(subgraph, cycle)) {
				logger.info("Closed cycle (0 translation)");
				// we continue to next cycle, if all cycles are translation 0, then we'll return true below
			} else {
				logger.info("Non-closed cycle (non-0 translation)");
				// one cycle has non-zero translation: we abort straight away: return false
				return false;
			}
		}
		

		
		return true;
	}
	
	private boolean isZeroTranslation(UndirectedGraph<ChainVertex, InterfaceEdge> subgraph, List<ChainVertex> cycle) {
			
		Point3i p = new Point3i(0,0,0);
		// Each edge sequentially
		for (int i=0;i<cycle.size();i++) {
			ChainVertex s = cycle.get(i);
			ChainVertex t = cycle.get( (i+1)%cycle.size());
			Set<InterfaceEdge> edges = subgraph.getAllEdges(s,t);
			
			if (edges.isEmpty()) {
				logger.warn("Empty list of edges between 2 vertices {} and {} belonging to same cycle {}",
						s.toString(),t.toString(), cycle.toString());
				continue;
			}
			
			Iterator<InterfaceEdge> edgeIt = edges.iterator();
			InterfaceEdge edge = edgeIt.next();
			Point3i trans = edge.getXtalTrans();
			if(!s.equals(subgraph.getEdgeSource(edge))) {
				if (!s.equals(subgraph.getEdgeTarget(edge))) {
					// a sanity check: should not happen unless there is a bug in jgrapht
					logger.warn("Something is wrong: edge {} hasn't got expected vertex source {} or target {}",
							edge.toString(), s.toString(), t.toString());
				}
				trans.negate();
			}

			// Check that any other edges have same xtaltrans
			while (edgeIt.hasNext()) {
				InterfaceEdge edge2 = edgeIt.next();
				Point3i trans2 = edge2.getXtalTrans();
				if(!s.equals(subgraph.getEdgeSource(edge2))) {
					trans2.negate();
				}
				if(!trans.equals(trans2)) {
					logger.info("Multiple edges with unequal translation");
					return false;
				}
			}
			
			p.add(trans);
		}
		
		logger.info("Total translation is [{}, {}, {}] ",p.x, p.y, p.z);
		return p.equals(new Point3i(0,0,0));
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
