package eppic.assembly;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jgrapht.UndirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Each of the sub-assemblies corresponding to the connected component subgraphs of an Assembly.
 * In the case of co-crystalization, the SubAssemblies can have different composition.
 * <p>
 * Each AssemblyGraph is composed by 1 or more SubAssemblies.
 * 
 * @author Jose Duarte
 * @author Aleix Lafita
 *
 */
public class SubAssembly {
	
	
	private static final Logger logger = LoggerFactory.getLogger(SubAssembly.class);

	private CrystalAssemblies crystalAssemblies;
	
	private UndirectedGraph<ChainVertex, InterfaceEdge> connectedGraph;
	private Stoichiometry<Integer> sto;
	
	public SubAssembly(UndirectedGraph<ChainVertex, InterfaceEdge> connectedGraph, CrystalAssemblies crystalAssemblies) {
		this.connectedGraph = connectedGraph;
		this.crystalAssemblies = crystalAssemblies;
		
		List<Integer> entities = crystalAssemblies.getLatticeGraph().getDistinctEntities();
		List<Integer> nodeEntities = connectedGraph.vertexSet().stream()
				.map(v -> v.getEntityId())
				.collect(Collectors.toList());
		this.sto = new Stoichiometry<>(nodeEntities,entities);
	}
	
	/**
	 * Get the stoichiometry of this subassembly.
	 * The stoichiometry values are vertex entity's molId numbers.
	 * @return
	 */
	public Stoichiometry<Integer> getStoichiometry() {
		return sto;
	}
	
	public UndirectedGraph<ChainVertex, InterfaceEdge> getConnectedGraph() {
		return connectedGraph;
	}
	
	/**
	 * Checks that this SubAssembly is automorphic in terms of entities and interface clusters. i.e. if every 
	 * vertex of entity i has the same number and type of edges (interface cluster ids) that any other vertex
	 * with entity i
	 * @return
	 */
	public boolean isAutomorphic() {
		return GraphUtils.isAutomorphic(connectedGraph);
	}
	
	/**
	 * Return the PointGroupSymmetry for this SubAssembly:
	 * cyclic Cn, dihedral Dn, tetrahedral T, octahedral O or icosahedral I. 
	 * This will work correctly only on assemblies that have been previously checked
	 * to be valid with {@link Assembly#isValid()}
	 * @return the PointGroupSymmetry or null if it can't be found
	 */
	public PointGroupSymmetry getSymmetry() {
		
		
		// we get the number of present entities in this stoichiometry (those with count>0)
		int numEntities = sto.getNumPresent();

		
		boolean heteromer = false;
		if (numEntities>1) heteromer = true;

		int n = sto.getFirstNonZero();
		
		if (n==-1) {
			logger.warn("All counts are 0 for this stoichiometry. Something is wrong!");
			return null;
		}
		
		
		UndirectedGraph<ChainVertex, InterfaceEdge> g = connectedGraph;
		GraphContractor<ChainVertex, InterfaceEdge> gctr = new GraphContractor<>(g);


		if (heteromer) {
			
			g = gctr.contract(InterfaceEdge.class);
			
		}		
		
		if (heteromer && !sto.isEven()) { 
			// this should not happen since we disallow uneven stoichiometries in the search for valid assemblies
			logger.warn("Uneven stoichiometry found while getting symmetry. Something is wrong!");
			return null;
		}
		
		// FINDING SYMMETRY:

		// this should work fine for both homomer and pseudo-homomer graph
		int numDistinctInterfaces = GraphUtils.getNumDistinctInterfaces(g);
		
		// CASE A) n==1
		
		if (n==1) {
			
			return new PointGroupSymmetry('C', 1);
			
		} 
		
		// CASE B) n==2 or n is odd
		
		if (n%2 != 0 || n==2) {
			
			return new PointGroupSymmetry('C', n);

		} 
		
		// CASE C) even number larger than 2 (n%2==0 with n>2)

		if (numDistinctInterfaces==1) {
			return new PointGroupSymmetry('C', n);			
		} 


		//NOTE: in principle we could just assume that if numDistinctInterfaces>1 this will be a D,
		//      but! it can happen that a Cn assembly has cross-interfaces, e.g. 4hi5 (a C4)

		
		TreeMap<Integer, Integer> cycleSizes = GraphUtils.getCycleMultiplicities(g);
		boolean nMultCycleExists = false;
		boolean threeMultCycleExists = false;
		boolean fourMultCycleExists = false;
		//boolean fiveMultCycleExists = false;
		
		for (int cycleMult:cycleSizes.values()) {
			if (cycleMult==n) nMultCycleExists = true;
			if (cycleMult==3) threeMultCycleExists = true;
			if (cycleMult==4) fourMultCycleExists = true;
			//if (cycleMult==5) fiveMultCycleExists = true;
		}
		

		if (nMultCycleExists) {
			return new PointGroupSymmetry('C', n);
		}

		if (n==12 && threeMultCycleExists) {
			// tetrahedral
			return new PointGroupSymmetry('T', 0);
		}
		if (n==24 && fourMultCycleExists) {
			// octahedral
			return new PointGroupSymmetry('O', 0);
		}
		// as a temporary solution for issue https://github.com/eppic-team/eppic/issues/142
		// we'll just call any multiple of 60 icosahedral
		//if (n==60 && fiveMultCycleExists) {
		if (n%60==0) {
			// icosahedral
			return new PointGroupSymmetry('I', 0);
		}
		
		// none of the above return: it has to be a D n/2
		return new PointGroupSymmetry('D', n/2);


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
	
	/**
	 * Returns the description corresponding to this SubAssembly
	 * @return
	 */
	public AssemblyDescription getDescription() {
		
		String symString = PointGroupSymmetry.UNKNOWN;
		if (getSymmetry()!=null) symString = getSymmetry().toString();
		
		List<String> chains = IntStream.range(0, crystalAssemblies.getNumChainsInStructure())
				.mapToObj(i -> crystalAssemblies.getChainId(i))
				.collect(Collectors.toList());
		List<String> nodeChains = connectedGraph.vertexSet().stream()
				.map(v -> v.getChain().getName())
				.collect(Collectors.toList());

		Stoichiometry<String> chainStoich = new Stoichiometry<>(nodeChains,chains);
		AssemblyDescription ad = 
				new AssemblyDescription(
						getStoichiometry().getTotalSize(), 
						symString, 
						chainStoich.toFormattedString(), 
						getStoichiometry().toFormattedString(
								entityId -> crystalAssemblies.getRepresentativeChainIdForEntityIndex(
										crystalAssemblies.getEntityIndex(entityId))),
						getStoichiometry().toFormattedStringRelettered(), 
						getChainIdsString());
		return ad;
	}

	@Override
	public String toString() {
		return "(Stoichiometry:"+ sto.toString() + " - " +
				connectedGraph.vertexSet().size() + " vertices - " +
				connectedGraph.edgeSet().size() + " edges" +
				")"
				;
	}
}
