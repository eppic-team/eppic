package eppic.assembly;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.EppicParams;


public class AssemblyFinder {

	private static final Logger logger = LoggerFactory.getLogger(AssemblyFinder.class);
	
	private LatticeGraph lattice;
	private Structure structure;
	private StructureInterfaceList interfaces;
	private List<StructureInterfaceCluster> interfaceClusters;
	
	private int numInterfClusters;
	private Stoichiometry xtalStoichiometry;
	
	public AssemblyFinder(Structure structure, StructureInterfaceList interfaces) throws StructureException {
		this.structure = structure;
		this.lattice = new LatticeGraph(structure, interfaces);
		this.interfaces = interfaces;
		this.interfaceClusters = interfaces.getClusters(EppicParams.CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF);
		this.numInterfClusters = interfaceClusters.size();
		this.xtalStoichiometry = new Stoichiometry(structure);
		
		for (Chain c: structure.getChains()) {
			xtalStoichiometry.addEntity(c.getCompound().getMolId());
		}
				
		logger.info ("The stoichiometry of the crystal is {}", xtalStoichiometry.toString());
	}
	
	public int getNumInterfClusters() {
		return numInterfClusters;
	}
	
	public LatticeGraph getLatticeGraph() {
		return lattice;
	}
	
	public Structure getStructure() {
		return structure;
	}
	
	public StructureInterfaceList getInterfaces() {
		return interfaces;
	}
	
	public List<StructureInterfaceCluster> getInterfaceClusters() {
		return interfaceClusters;
	}
	
	/**
	 * Returns all topologically valid assemblies present in the crystal.
	 * The method traverses the tree of all possible combinations of n interface
	 * clusters (2^n in total), e.g. for a structure with 3 clusters {0,1,2} the tree looks like:
	 * <pre>
	 *         {}
	 *       /  |  \
	 *    {0}  {1}  {2}
	 *    |  X     X   |
	 *  {0,1} {0,2} {1,2}
	 *      \   |   /
	 *       {0,1,2}   
	 * </pre>
	 * As the tree is traversed, if a node is found to be an invalid assembly, then all of its children
	 * are pruned off and not tried. Thus the number of combinations reduces very quickly with a few
	 * pruned top nodes.
	 * @return
	 */
	public Set<Assembly> getValidAssemblies() {
		
		lattice.removeDuplicateEdges();
		
		Set<Assembly> validSet = new HashSet<Assembly>();

		// the list of nodes in the tree found to be invalid: all of their children will also be invalid
		List<Assembly> invalidNodes = new ArrayList<Assembly>();		
		
		Assembly emptyAssembly = new Assembly(structure, interfaces, interfaceClusters, lattice.getGraph(), new boolean[numInterfClusters]);
		
		validSet.add(emptyAssembly); // the empty assembly (no engaged interfaces) is always a valid assembly
		
		Set<Assembly> prevLevel = new HashSet<Assembly>();
		prevLevel.add(emptyAssembly);
		Set<Assembly> nextLevel = null;
		
		for (int k = 1; k<=numInterfClusters;k++) {
			
			logger.debug("Traversing level {} of tree: {} parent nodes",k,prevLevel.size());
			
			nextLevel = new HashSet<Assembly>();
					
			for (Assembly p:prevLevel) {
				List<Assembly> children = p.getChildren(invalidNodes);
				
				for (Assembly c:children) {
					
					if (!c.isValid()) {
						logger.debug("Node {} is invalid, will prune off all of its children",c.toString());
						invalidNodes.add(c);
					} else {
						// we only add a child for next level if we know it's valid, if it wasn't valid 
						// then it's not added and thus the whole branch is pruned
						nextLevel.add(c);
						// add assembly as valid
						validSet.add(c);
					}
				}
			}
			prevLevel = new HashSet<Assembly>(nextLevel); 
			
		}
		
		

		return validSet;
	}
	
	


}
