package eppic.assembly;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.biojava.nbio.structure.Structure;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class StoichiometrySet {
	
	//private static final Logger logger = LoggerFactory.getLogger(StoichiometrySet.class);
	

	private Structure structure;
	private List<Set<ChainVertex>> connectedComponents;
	
	private List<Stoichiometry> stoichiometries;
	
	private Set<Stoichiometry> uniqueStoichiometries;
	
	public StoichiometrySet(Structure structure, Assembly assembly, List<Set<ChainVertex>> connectedComponents) {
		this.structure = structure;
		this.connectedComponents = connectedComponents;
		initStoichiometries(assembly);
	}
	
	private void initStoichiometries(Assembly assembly) {
		stoichiometries = new ArrayList<Stoichiometry>();
		for (Set<ChainVertex> cc:connectedComponents) {			
			Stoichiometry s = new Stoichiometry(structure, assembly);
			stoichiometries.add(s);
			for (ChainVertex v:cc) {
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
	 * Return true if all stoichiometries of this set are even
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
	 * Returns the description corresponding to this StoichiometrySet as a list 
	 * of AssemblyDescriptions per disjoint set.
	 * @return
	 */
	public List<AssemblyDescription> getDescription() {
		
		List<AssemblyDescription> ds = new ArrayList<AssemblyDescription>();
		for (Stoichiometry s:uniqueStoichiometries) {
			AssemblyDescription ad = 
					new AssemblyDescription(s.getTotalSize(), s.getSymmetry(), s.toFormattedCompositionString(), s.toFormattedString());
			ds.add(ad);
		}
		
		return ds;
	}	
	
	
}
