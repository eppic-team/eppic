package eppic.assembly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AssemblyFinder {

	private static final Logger logger = LoggerFactory.getLogger(AssemblyFinder.class);
	
	private LatticeGraph lattice;	
	private StructureInterfaceList interfaces;
	
	private int numEntities;
	private int numInterfClusters;
	private int[] xtalStoichiometry;
	
	public AssemblyFinder(Structure structure, StructureInterfaceList interfaces) {
		this.lattice = new LatticeGraph(structure, interfaces);;
		this.interfaces = interfaces;
		
		this.numEntities = structure.getCompounds().size();
		this.numInterfClusters = interfaces.getClusters().size();
		this.xtalStoichiometry = new int[numEntities];
		
		for (Chain c: structure.getChains()) {
			// this relies on molId being 1 to n (we have to check that actually it is like that in the PDB)
			xtalStoichiometry[c.getCompound().getMolId() - 1] ++;
		}
		logger.info ("The stoichiometry of the crystal is {}", Arrays.toString(xtalStoichiometry));
	}
	
	public int getNumEntities() {
		return numEntities;
	}
	
	/**
	 * Returns all topologically valid assemblies present in the crystal.
	 * @return
	 */
	public List<Assembly> getValidAssemblies() {
		
		List<Assembly> validAssemblies = new ArrayList<Assembly>();

		List<boolean[]>[] combinations = PowerSet.powerSetBinary(numInterfClusters);
		
		List<boolean[]> invalidGroups = new ArrayList<boolean[]>();		
		
		
		for (int k = 1; k<=numInterfClusters;k++) {
			
			for (int i = 0;i<combinations[k].size();i++) {
				
				boolean[] g = combinations[k].get(i);
				
				
				if (isInvalidGroup(invalidGroups, g)) continue;
				
				if (!isValidEngagedSet(g)) {
					
					invalidGroups.add(g);
					
				} else {
					
					// add assembly as valid
					validAssemblies.add(new Assembly(interfaces, g));
				}
			}
			
		}
		
		

		return validAssemblies;
	}
	
	/**
	 * Returns true if given group is a child of any of the given invalidGroups, false otherwise
	 * @param invalidGroups
	 * @param g
	 * @return
	 */
	private boolean isInvalidGroup(List<boolean[]> invalidGroups, boolean[] g) {

		for (boolean[] invalidGroup:invalidGroups) {
			if (isChild(invalidGroup, g)) return true;
		}
		return false;
	}
	
	/**
	 * Tells whether a particular set of engaged interface clusters is the child of another set.
	 * 
	 * @param potentialParent the set of engaged interfaces potentially parent of potentialChild
	 * @param potentialChild the set of engaged interfaces potentially child of potentialParent
	 * @return true if is a child false if not
	 */
	private boolean isChild(boolean[] potentialParent, boolean[] potentialChild) {
		
		for (int i=0;i<numInterfClusters;i++) {
			if (potentialParent[i]	&& potentialChild[i]) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the subgraph containing only the given cluster id edges
	 * @param clusterId
	 * @return
	 */
//	private Graph<ChainVertex, InterfaceEdge> getSubgraph(final boolean[] engagedClusters) {
//		EdgePredicateFilter<ChainVertex, InterfaceEdge> edgeFilter = 
//				new EdgePredicateFilter<ChainVertex, InterfaceEdge>(new Predicate<InterfaceEdge>() {
//
//					@Override
//					public boolean evaluate(InterfaceEdge edge) {
//						for (int i=0;i<engagedClusters.length;i++) {
//							if (engagedClusters[i]  && edge.getClusterId()==i+1) {							
//								return true;							
//							}
//						}
//						return false;
//					}
//					
//				});
//		
//		Graph<ChainVertex,InterfaceEdge> graph = lattice.getGraph();
//		return edgeFilter.transform(graph);		
//	}
	
	private boolean isValidEngagedSet(boolean[] g) {
//		Graph<ChainVertex,InterfaceEdge> subgraph = getSubgraph(g);
//		
//		int numVertices = subgraph.vertexSet().size();
//		int numEdges = subgraph.edgeSet().size();
		
		// just a test case
		if (g[2]) return false;
		if (g[3]) return false;
		return true;
		
		// TODO implement based on rules iii and iv
		
	}
	
}
