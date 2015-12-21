package eppic.assembly;

import java.util.TreeMap;

import org.jgrapht.UndirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Each of the sub-assemblies corresponding to the connected component subgraphs of an Assembly
 * 
 * Each AssemblyGraph is composed by 1 or more SubAssemblies
 * 
 * @author Jose Duarte
 *
 */
public class SubAssembly {
	
	private static final String UNKNOWN_SYMMETRY =  "unknown";	
	
	private static final Logger logger = LoggerFactory.getLogger(SubAssembly.class);

	private UndirectedGraph<ChainVertex, InterfaceEdge> connectedGraph;
	private Stoichiometry sto;
	
	public SubAssembly(UndirectedGraph<ChainVertex, InterfaceEdge> connectedGraph, CrystalAssemblies crystalAssemblies) {
		this.connectedGraph = connectedGraph;
		
		this.sto = new Stoichiometry(connectedGraph, crystalAssemblies);
	}
	
	public Stoichiometry getStoichiometry() {
		return sto;
	}
	
	public UndirectedGraph<ChainVertex, InterfaceEdge> getConnectedGraph() {
		return connectedGraph;
	}
	
	/**
	 * Return the symmetry string for this SubAssembly:
	 * cyclic Cn, dihedral Dn, tetrahedral T, octahedral O or icosahedral I. 
	 * This will work correctly only on assemblies that have been previously checked
	 * to be valid with {@link Assembly#isValid()}
	 * @return
	 */
	public String getSymmetry() {
		
		
		// we get the number of present entities in this stoichiometry (those with count>0)
		int numEntities = sto.getNumPresentEntities();

		
		boolean heteromer = false;
		if (numEntities>1) heteromer = true;

		int n = sto.getFirstNonZero();
		
		if (n==-1) {
			logger.warn("All counts are 0 for this stoichiometry. Something is wrong!");
			return UNKNOWN_SYMMETRY;
		}
		
		
		UndirectedGraph<ChainVertex, InterfaceEdge> g = connectedGraph;
		GraphContractor gctr = new GraphContractor(g);


		if (heteromer) {
			
			g = gctr.contract();
			
		}		
		
		if (heteromer && !sto.isEven()) { 
			// this should not happen since we disallow uneven stoichiometries in the search for valid assemblies
			logger.warn("Uneven stoichiometry found while getting symmetry. Something is wrong!");
			return UNKNOWN_SYMMETRY;
		}
		
		// FINDING SYMMETRY:

		// this should work fine for both homomer and pseudo-homomer graph
		int numDistinctInterfaces = GraphUtils.getDistinctInterfaceCount(g);
		
		// CASE A) n==1
		
		if (n==1) {
			
			return "C1";
			
		} 
		
		// CASE B) n==2 or n is odd
		
		if (n%2 != 0 || n==2) {
			
			return "C"+n;

		} 
		
		// CASE C) even number larger than 2 (n%2==0 with n>2)

		if (numDistinctInterfaces==1) {
			return "C"+n;			
		} 


		//NOTE: in principle we could just assume that if numDistinctInterfaces>1 this will be a D,
		//      but! it can happen that a Cn assembly has cross-interfaces, e.g. 4hi5 (a C4)

		
		TreeMap<Integer, Integer> cycleSizes = GraphUtils.getCycleMultiplicities(g);
		boolean nMultCycleExists = false;
		boolean threeMultCycleExists = false;
		boolean fourMultCycleExists = false;
		boolean fiveMultCycleExists = false;
		
		for (int cycleMult:cycleSizes.values()) {
			if (cycleMult==n) nMultCycleExists = true;
			if (cycleMult==3) threeMultCycleExists = true;
			if (cycleMult==4) fourMultCycleExists = true;
			if (cycleMult==5) fiveMultCycleExists = true;
		}
		
//		boolean nMultExists = false;
//		boolean threeMultExists = false;
//		boolean fourMultExists = false;
//		boolean fiveMultExists = false;
//
//		// this should work fine for both homomer and pseudo-homomer graph
//		for (int mult:Assembly.getMultiplicities(g).values()) {
//			if (mult==n) nMultExists = true;
//			if (mult==3) threeMultExists = true;
//			if (mult==4) fourMultExists = true;
//			if (mult==5) fiveMultExists = true;
//		}

		if (nMultCycleExists) {
			return "C"+n;
		}

		if (n==12 && threeMultCycleExists) {
			// tetrahedral
			return "T";
		}
		if (n==24 && fourMultCycleExists) {
			// octahedral
			return "O";
		}
		if (n==60 && fiveMultCycleExists) {
			// icosahedral
			return "I";
		}
		
		// none of the above return: it has to be a D n/2
		return "D"+(n/2);


	}
	
	public String getChainIdsString() {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (ChainVertex v:connectedGraph.vertexSet()) {
			if (i!=0) sb.append(',');
			sb.append(v.getChainId()+"_"+v.getOpId());
			i++;
		}
		
		return sb.toString();
	}

}
