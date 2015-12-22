package eppic.assembly;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.vecmath.Point3i;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.cycle.PatonCycleBase;
import org.jgrapht.graph.UndirectedSubgraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A representation of the graph corresponding to a certain Assembly, i.e.
 * a subgraph of the entire lattice graph. 
 * 
 * Each Assembly corresponds to one and only one AssemblyGraph
 * 
 * @author Jose Duarte
 *
 */
public class AssemblyGraph {
	
	private static final Logger logger = LoggerFactory.getLogger(AssemblyGraph.class);

	private Assembly assembly;
	
	private UndirectedGraph<ChainVertex, InterfaceEdge> subgraph;
	
	/**
	 * The connected components of subgraph 
	 */
	private List<SubAssembly> subAssemblies;
	
	private List<List<SubAssembly>> subAssembliesGroupedByStoichiometries;
	
	public AssemblyGraph (Assembly assembly) {
		this.assembly = assembly;
		
		init();
	}
	
	/**
	 * Initialises the subgraph containing only this assembly's engaged interface clusters.
	 * This initialises both the subgraph and connectedComponents members
	 * @param clusterId
	 * @return
	 */
	private void init() {		
		
		// note that the subgraph will contain all vertices even if they are not connected to the rest by any interface
		
		Set<ChainVertex> vertexSet = assembly.getCrystalAssemblies().getLatticeGraph().getGraph().vertexSet();
		Set<InterfaceEdge> edgeSubset = new HashSet<InterfaceEdge>();
		for(InterfaceEdge edge:assembly.getCrystalAssemblies().getLatticeGraph().getGraph().edgeSet()) {
			for (int i=0;i<assembly.getEngagedSet().size();i++) {
				if (assembly.getEngagedSet().isOn(i)  && edge.getClusterId()==i+1) {
					edgeSubset.add(edge);
				}
			}
		}
		
		this.subgraph = 
				new UndirectedSubgraph<ChainVertex, InterfaceEdge>(
						assembly.getCrystalAssemblies().getLatticeGraph().getGraph(), vertexSet, edgeSubset);
		 

		
		// initialising also the connected components
		ConnectivityInspector<ChainVertex, InterfaceEdge> ci = new ConnectivityInspector<ChainVertex, InterfaceEdge>(subgraph);
		
		List<Set<ChainVertex>> connectedSets = ci.connectedSets();
		
		logger.debug("Subgraph of assembly {} has {} vertices and {} edges, with {} connected components",
				assembly.toString(), subgraph.vertexSet().size(), subgraph.edgeSet().size(), connectedSets.size());
		
		StringBuilder sb = new StringBuilder();
		for (Set<ChainVertex> cc:connectedSets) {
			sb.append(cc.size());
			sb.append(' ');
		}
		logger.debug("Connected component sizes: {}",sb.toString()); 
	
		// now we create the connectedGraphs from the sets of connected vertices
		
		subAssemblies = new ArrayList<SubAssembly>(connectedSets.size());
		
		for (Set<ChainVertex> vertexSubsubSet:connectedSets) {
			Set<InterfaceEdge> edgeSubsubSet = new HashSet<InterfaceEdge>();
			// fill the edges
			int i = -1;
			for (ChainVertex iVertex:vertexSubsubSet) {
				i++;
				int j = -1;
				for (ChainVertex jVertex:vertexSubsubSet) {
					j++;
					if (j>i) {
						edgeSubsubSet.addAll(subgraph.getAllEdges(iVertex, jVertex));
					}
				}
			}
			
			subAssemblies.add(new SubAssembly(new UndirectedSubgraph<ChainVertex, InterfaceEdge>(subgraph, vertexSubsubSet, edgeSubsubSet), assembly.getCrystalAssemblies()));
		}
				
		
		// now we find the unique stoichiometries present in all subAssemblies
		Set<Stoichiometry> uniqueStoichiometries = new HashSet<Stoichiometry>();
		for (SubAssembly subAssembly: subAssemblies) {
			uniqueStoichiometries.add(subAssembly.getStoichiometry());
		}
		
		// and with those we can group subAssemblies by stoichiometries
		subAssembliesGroupedByStoichiometries = new ArrayList<List<SubAssembly>>();
		for (Stoichiometry uniqueSto: uniqueStoichiometries) {
		
			List<SubAssembly> group = new ArrayList<SubAssembly>();
			for (SubAssembly subAssembly: subAssemblies) {
				if (subAssembly.getStoichiometry().equals(uniqueSto)) {
					group.add(subAssembly);
				}
			}
			subAssembliesGroupedByStoichiometries.add(group);
		}
				
	}
	
	
	public List<SubAssembly> getSubAssemblies() {
		return subAssemblies;
	}
	
	public List<List<SubAssembly>> getSubAssembliesGroupedByStoichiometries() {
		return subAssembliesGroupedByStoichiometries;
	}
	
	
	/**
	 * Returns true if all stoichiometries of this AssemblyGraph are even
	 * @return
	 * @see {@link Stoichiometry#isEven()}
	 */
	public boolean isStoichiometryEven() {
		for (List<SubAssembly> group : subAssembliesGroupedByStoichiometries) {
			Stoichiometry sto = group.get(0).getStoichiometry();
			if (!sto.isEven()) return false;
		}
		return true;
	}
	
	/**
	 * Returns true if this AssemblyGraph is composed of only 1 unique 
	 * stoichiometry covering all entities in crystal.
	 * @return
	 * @see {@link Stoichiometry#isFullyCovering()}
	 */
	public boolean isFullyCovering() {
		if (subAssembliesGroupedByStoichiometries.size()>1) return false; 
		
		// we have 1 stoichiometry only: we take any of the subAssemblies, for instance first one
		
		return subAssemblies.iterator().next().getStoichiometry().isFullyCovering();
	}
	
	/**
	 * Returns true if this AssemblyGraph is entity-isomorphic, i.e. if none of the unique stoichiometries overlap
	 * @return
	 */
	public boolean isEntityIsomorphic() {

		// once we have the subAssemblies grouped by unique stoichiometries, 
		// if there is any kind of overlap between the stoichiometries then it can't be isomorphic, e.g. B2,B ; A2B,A
		// otherwise they are all orthogonal to each other and the assembly is fine in terms of entity stoichiometry
		
		int i = -1;
		for (List<SubAssembly> iGroup : subAssembliesGroupedByStoichiometries) {
			Stoichiometry iSto = iGroup.get(0).getStoichiometry();
			i++;			
			int j = -1;
			for (List<SubAssembly> jGroup : subAssembliesGroupedByStoichiometries) {
				Stoichiometry jSto = jGroup.get(0).getStoichiometry();
				j++;
				if (j<=i) continue;
				if (iSto.isOverlapping(jSto)) {
					return false;
				}

			}

		}

		return true;
	}

	/**
	 * Return true if all cycles in subgraph are closed, false otherwise.
	 * @return
	 */
	public boolean areAllCyclesClosed () {
		// we check the cycles in the graph and whether they stay in same cell

		// The PatonCycle detection does not work for multigraphs, e.g. in 1pfc engaging interfaces 1,5 it goes in an infinite loop
		// Thus we need to pre-check multi-edges and discard whenever they have non-zero sum translations (which directly invalidates the whole subgraph)
		// If the the subgraph is multi and this check still returns false, we'd have a multigraph to deal with below,
		// but hopefully that doesn't happen (remember we've also removed all duplicate edges from the main graph in any case)
		if (precheckMultiEdges(subgraph)) {
			logger.info("Discarding assembly because some of its multi-edges have non-zero sum translations and thus can't be closed");
			return false;
		}

		PatonCycleBase<ChainVertex, InterfaceEdge> paton = new PatonCycleBase<ChainVertex, InterfaceEdge>(subgraph);

		List<List<ChainVertex>> cycles = paton.findCycleBase();

		if (cycles.size()==0) {
			// no cycles at all:
			// heteromeric interfaces are ok
			if (containsHeteromeric()) {
				logger.info("Assembly {} contains heteromeric interfaces and no cycles, assuming it has closed-symmetry",toString());
				return true;
			}
			// homomeric aren't
			// homomeric interfaces and no cycles: can't be closed!
			logger.debug("No cycles in assembly {}: discarding because it can't be a closed-symmetry", toString());
			return false;
		}

		logger.debug("{} cycles in total",cycles.size());

		for (List<ChainVertex> cycle:cycles) {

			StringBuilder sb = new StringBuilder();
			for (ChainVertex c:cycle) {
				sb.append(c.toString()+" -> ");
			}
			logger.debug("Cycle of size {}: {}", cycle.size(),sb.toString());

			if (isZeroTranslation(subgraph, cycle)) {
				logger.debug("Closed cycle (0 translation)");
				// we continue to next cycle, if all cycles are translation 0, then we'll return true below
			} else {
				// one cycle has non-zero translation: we abort straight away: return false
				logger.debug("Non-closed cycle (non-0 translation). Discarding assembly {}",toString());
				return false;
			}
		}


		logger.debug("All cycles of assembly {} are closed: valid assembly",toString());
		return true;
	}
	
	private boolean isZeroTranslation(UndirectedGraph<ChainVertex, InterfaceEdge> subgraph, List<ChainVertex> cycle) {
		
		Point3i p = new Point3i(0,0,0);
		for (int i=0;i<cycle.size();i++) {
			ChainVertex s = cycle.get(i);
			ChainVertex t = cycle.get( (i+1)%cycle.size());
			Set<InterfaceEdge> edges = subgraph.getAllEdges(s,t);
			
			if (edges.isEmpty()) {
				// this should not happen, but there's a bug in jgrapht Paton's implementation
				// where cycles between 2 vertices are reported with a duplicate vertex,
				// e.g. for graph A0=A1 the vertices given as belonging to cycle are A0,A1,A0, when it should be just A0,A1
				logger.warn("Empty list of edges between vertices {},{} belonging to cycle {}",
						s.toString(),t.toString(), cycle.toString());
				continue;
			}
			
			Iterator<InterfaceEdge> edgeIt = edges.iterator();
			InterfaceEdge edge = edgeIt.next();
			Point3i trans = new Point3i(edge.getXtalTrans());
			// this is a way to decide an arbitrary directionality and invert if the directionality is not the right one
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
				Point3i trans2 = new Point3i(edge2.getXtalTrans());
				if(!s.equals(subgraph.getEdgeSource(edge2))) {
					trans2.negate();
				}
				if(!trans.equals(trans2)) {
					logger.debug("Multiple edges with unequal translation between vertices {},{} of cycle {}",
							s.toString(),t.toString(),cycle.toString());
					return false;
				}
			}
			
			p.add(trans);
		}
		
		logger.debug("Total translation is [{}, {}, {}] ",p.x, p.y, p.z);
		return p.equals(new Point3i(0,0,0));
	}

	/**
	 * Returns true if any of the multi-edges in given subgraph has non-zero sum translations,
	 * false otherwise
	 * @param subgraph
	 * @return
	 */
	private boolean precheckMultiEdges(UndirectedGraph<ChainVertex,InterfaceEdge> subgraph) {
		
		for (InterfaceEdge edge:subgraph.edgeSet()) {
			Set<InterfaceEdge> edges = subgraph.getAllEdges(subgraph.getEdgeSource(edge), subgraph.getEdgeTarget(edge));
			if (edges.size()==1) continue;
			
			Point3i t = new Point3i(0,0,0);
			for (InterfaceEdge e: edges) {
				t.add(e.getXtalTrans());
			}
			if (!t.equals(new Point3i(0,0,0))) {
				logger.debug("Vertices {},{} are connected by {} edges with non-0 sum translation: {} ",
						subgraph.getEdgeSource(edge), subgraph.getEdgeTarget(edge), edges.size(), t.toString()); 
				return true; 
			} else {
				logger.warn("Unexpected multi-edge: vertices {},{} are connected by {} edges with 0 sum translation! ",
						subgraph.getEdgeSource(edge), subgraph.getEdgeTarget(edge), edges.size());
			}
		}
		return false;
	}

	/**
	 * Returns true if at least one edge of this AssemblyGraph is heteromeric, i.e. its 2 end-vertices are different entities
	 * @return
	 */
	private boolean containsHeteromeric() {
		for (InterfaceEdge e:subgraph.edgeSet()) {
			ChainVertex s = subgraph.getEdgeSource(e);
			ChainVertex t = subgraph.getEdgeTarget(e);
			
			if (s.getEntity() != t.getEntity()) return true;
		}
		return false;
	}
	
	/**
	 * Returns the description corresponding to this AssemblyGraph as a list 
	 * of AssemblyDescriptions per disjoint set.
	 * @return
	 */
	public List<AssemblyDescription> getDescription() {
		
		List<AssemblyDescription> ds = new ArrayList<AssemblyDescription>();
		
		for (List<SubAssembly> group : subAssembliesGroupedByStoichiometries) {
			
			SubAssembly subAssembly = group.get(0);
			
			String symString = PointGroupSymmetry.UNKNOWN;
			if (subAssembly.getSymmetry()!=null) symString = subAssembly.getSymmetry().toString();
		
			AssemblyDescription ad = 
					new AssemblyDescription(
							subAssembly.getStoichiometry().getTotalSize(), 
							symString, 
							subAssembly.getStoichiometry().toFormattedCompositionString(), 
							subAssembly.getStoichiometry().toFormattedString(), 
							subAssembly.getChainIdsString());
			ds.add(ad);
		}
		
		return ds;
	}
	
	public String toString() {
		return  "(" + 
					subAssemblies.size()+ " sub-assemblies - "+
					subAssembliesGroupedByStoichiometries.size() + " sub-assembly groups - " + 
					subgraph.toString() +
				")";
	}
}
