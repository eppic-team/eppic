package eppic.assembly;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.biojava.nbio.structure.Structure;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.jgrapht.UndirectedGraph;

public class StoichiometrySet {
	
	//private static final Logger logger = LoggerFactory.getLogger(StoichiometrySet.class);
	

	private Structure structure;
	private List<UndirectedGraph<ChainVertex, InterfaceEdge>> connectedComponents;
	
	private List<Stoichiometry> stoichiometries;
	
	private Set<Stoichiometry> uniqueStoichiometries;
	
	public StoichiometrySet(Structure structure, Assembly assembly, List<UndirectedGraph<ChainVertex, InterfaceEdge>> connectedComponents) {
		this.structure = structure;
		this.connectedComponents = connectedComponents;
		initStoichiometries(assembly);
	}
	
	private void initStoichiometries(Assembly assembly) {
		stoichiometries = new ArrayList<Stoichiometry>();
		for (UndirectedGraph<ChainVertex, InterfaceEdge> cc:connectedComponents) {			
			Stoichiometry s = new Stoichiometry(structure, assembly);
			stoichiometries.add(s);
			for (ChainVertex v:cc.vertexSet()) {
				s.add(v.getChain());
			}			
		}
		
		uniqueStoichiometries = new HashSet<Stoichiometry>();
		uniqueStoichiometries.addAll(stoichiometries);
		
	}

	public boolean isIsomorphic() {

		// once we have the unique ones, if there is any kind of overlap then it can't be isomorphic, e.g. B2,B ; A2B,A
		// otherwise they are all orthogonal to each other and the assembly is fine in terms of entity stoichiometry
		
		int i = -1;
		for (Stoichiometry iSto:uniqueStoichiometries) {
			i++;			
			int j = -1;
			for (Stoichiometry jSto:uniqueStoichiometries) {
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
	 * Returns true if all stoichiometries of this set are even
	 * @return
	 * @see {@link Stoichiometry#isEven()}
	 */
	public boolean isEven() {
		for (Stoichiometry sto:uniqueStoichiometries) {
			if (!sto.isEven()) return false;
		}
		return true;
	}
	
	/**
	 * Returns true if this set is composed of only 1 unique 
	 * stoichiometry covering all entities in crystal.
	 * @return
	 * @see {@link Stoichiometry#isFullyCovering()}
	 */
	public boolean isFullyCovering() {
		if (uniqueStoichiometries.size()>1) return false;
		
		return getFirst().isFullyCovering();
	}
	
	public Stoichiometry getFirst() {
		return uniqueStoichiometries.iterator().next();
	}
	
	public Set<Stoichiometry> getUniqueStoichiometries() {
		return uniqueStoichiometries;
	}
	
	/**
	 * Returns the description corresponding to this StoichiometrySet as a list 
	 * of AssemblyDescriptions per disjoint set.
	 * @return
	 */
	public List<AssemblyDescription> getDescription() {
		
		List<AssemblyDescription> ds = new ArrayList<AssemblyDescription>();
		for (Stoichiometry s:uniqueStoichiometries) {
			AssemblyDescription ad = 
					new AssemblyDescription(
							s.getTotalSize(), s.getSymmetry(), s.toFormattedCompositionString(), s.toFormattedString(), s.getChainIdsString());
			ds.add(ad);
		}
		
		return ds;
	}	
	
	/**
	 * Get all indices of stoichiometries in this set with given stoichiometry content
	 * @param sto
	 * @return
	 */
	protected List<Integer> getIndicesWithOverlappingStoichiometry(Stoichiometry sto) {
		List<Integer> indices = new ArrayList<Integer>();
		
		for (int i=0;i<stoichiometries.size();i++) {
			if (stoichiometries.get(i).isOverlapping(sto)) {
				indices.add(i);
			}
		}
		return indices;
	}
	
}
