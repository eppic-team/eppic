package eppic.assembly;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jgrapht.UndirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.CallType;

/**
 * Each of the sub-assemblies corresponding to the connected component subgraphs of an Assembly
 * 
 * Each AssemblyGraph is composed by 1 or more SubAssemblies
 * 
 * @author Jose Duarte
 *
 */
public class SubAssembly {
	
	
	private static final Logger logger = LoggerFactory.getLogger(SubAssembly.class);

	private CrystalAssemblies crystalAssemblies;
	
	private UndirectedGraph<ChainVertex, InterfaceEdge> connectedGraph;
	private Stoichiometry sto;
	
	public SubAssembly(UndirectedGraph<ChainVertex, InterfaceEdge> connectedGraph, CrystalAssemblies crystalAssemblies) {
		this.connectedGraph = connectedGraph;
		this.crystalAssemblies = crystalAssemblies;
		
		this.sto = new Stoichiometry(connectedGraph, crystalAssemblies);
	}
	
	public Stoichiometry getStoichiometry() {
		return sto;
	}
	
	public UndirectedGraph<ChainVertex, InterfaceEdge> getConnectedGraph() {
		return connectedGraph;
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
		int numEntities = sto.getNumPresentEntities();

		
		boolean heteromer = false;
		if (numEntities>1) heteromer = true;

		int n = sto.getFirstNonZero();
		
		if (n==-1) {
			logger.warn("All counts are 0 for this stoichiometry. Something is wrong!");
			return null;
		}
		
		
		UndirectedGraph<ChainVertex, InterfaceEdge> g = connectedGraph;
		GraphContractor gctr = new GraphContractor(g);


		if (heteromer) {
			
			g = gctr.contract();
			
		}		
		
		if (heteromer && !sto.isEven()) { 
			// this should not happen since we disallow uneven stoichiometries in the search for valid assemblies
			logger.warn("Uneven stoichiometry found while getting symmetry. Something is wrong!");
			return null;
		}
		
		// FINDING SYMMETRY:

		// this should work fine for both homomer and pseudo-homomer graph
		int numDistinctInterfaces = GraphUtils.getDistinctInterfaceCount(g);
		
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
		if (n==60 && fiveMultCycleExists) {
			// icosahedral
			return new PointGroupSymmetry('I', 0);
		}
		
		// none of the above return: it has to be a D n/2
		return new PointGroupSymmetry('D', n/2);


	}
	
	/**
	 * Get a call (bio/xtal prediction) for this SubAssembly
	 * @return
	 */
	public CallType score() {

		// TODO note: the code here goes along the same lines of getSymmetry, we should try to unify them a bit and to reuse the common parts

		CallType call = null;
						
		int n = sto.getFirstNonZero();
		
		if (n==-1) {
			logger.warn("All counts are 0 for first stoichiometry of assembly {}. Something is wrong: can't score assembly!",toString());
			return null;
		}
		
		
		int numEntities = sto.getNumPresentEntities();
		
		boolean heteromer = false;
		if (numEntities>1) heteromer = true;

		PointGroupSymmetry sym = getSymmetry();
		

		call = CallType.CRYSTAL; // set crystal as default call, only if found to be bio it will be overridden below

		if (n==1) {
			
			if (!heteromer) {
				// a C1 assembly (i.e. monomeric if homomeric):
				// no scoring at this stage, later we look at all assemblies and if no larger assembly is bio, we assign bios to C1 assemblies
				
			} else {
				// C1 heteromeric: 1:1:1 stoichiometry
				Set<Integer> set = getInterfaceClusterIds();
				int countBio = 0;
				for (int interfClusterId:set) {
					if (crystalAssemblies.getInterfaceEvolContextList().getCombinedClusterPredictor(interfClusterId).getCall() == CallType.BIO ) {
						countBio++;
					}
				}
				if (countBio>= (numEntities-1) ) 
					call = CallType.BIO;
			}
			return call;
		}
		

		UndirectedGraph<ChainVertex, InterfaceEdge> g = connectedGraph;
		GraphContractor gctr = new GraphContractor(g);
		
		
		if (heteromer) {

			g = gctr.contract();
			
			// TODO we should check the call of contracted interfaces and score properly based on 
			// them and the relevant interfaces below
		}

		
		TreeMap<Integer,Integer> clusterIdsToMult = GraphUtils.getCycleMultiplicities(g);
		
		
		if (sym.isCyclic()) {
			
			int clusterId = -1;

			if (sym.getMultiplicity()==2) {				
				// we've got to treat the C2 case especially because multiplicity=2 won't be detected in graph
				
				if (clusterIdsToMult.isEmpty()) {
					logger.error("Empty list of engaged interface clusters for a homomeric C2 symmetry. Something is wrong!");
					
				} else {
					clusterId = clusterIdsToMult.firstKey(); // the largest interface present (interface cluster ids are sorted on areas)
				}

			} else {

				for (int cId: clusterIdsToMult.keySet()) {
					if (clusterId==-1 && clusterIdsToMult.get(cId) == n) 
						clusterId = cId;
					else if (clusterIdsToMult.get(cId)==n) 
						logger.info("Assembly {} has more than 1 interface cluster with cycle multiplicity {}. Taking assembly call from first one.", toString(), n);
				}
				
				if (clusterId == -1) {
					logger.warn("Could not find the C{} interface for assembly {}. Something is wrong!", n, toString());
				} 
			}

			if (clusterId!=-1) {
				// the call for the Cn interface will be the call for the assembly
				call = crystalAssemblies.getInterfaceEvolContextList().getCombinedClusterPredictor(clusterId).getCall();
				// TODO in heteromeric cases we should check that the edges that we have contracted have also the same call

			} else {
				logger.warn("Could not find the relevant C{} interface, assembly {} will be NOPRED",n,toString());
				call = CallType.NO_PREDICTION;
			}


			
		} else {  // non-cyclic symmetries: Dn, T, O, I
			
			// In all other point group symmetries there's always 2 essential interfaces out of all of
			// the engaged ones that are needed to form the symmetry. 
			// Then a third one follows necessarily (though it might be too small and not show up in our 
			// list) because the other 2 are at the right angles to produce a third one. 
			
			// A possible strategy for scoring is simply taking the 2 largest interfaces and checking that both 
			// have a BIO call (that would be a sufficient condition), otherwise is XTAL. 
			// Another possibility would be to take any 2 interfaces out of the list and check if at 
			// least 2 are BIO.
						
			// In a D assembly most usually the 2 largest interfaces are the 2 isologous, 
			// the 3rd one being the heterologous. But that's not a general rule at all! there are counter-examples


			// the keys of the map should be sorted from first cluster id to last cluster id (which are sorted by areas)

			Iterator<Integer> it = clusterIdsToMult.keySet().iterator();
			int clusterId1 = it.next(); // the largest
			int clusterId2 = it.next(); // the second largest
			
			CallType firstCall = crystalAssemblies.getInterfaceEvolContextList().getCombinedClusterPredictor(clusterId1).getCall();
			CallType secondCall = crystalAssemblies.getInterfaceEvolContextList().getCombinedClusterPredictor(clusterId2).getCall();
			
			if (firstCall == CallType.BIO && secondCall == CallType.BIO) {
				call = CallType.BIO;
			}
			
		} 
		
		// can there be other symmetries?

		return call;

	}
	
	/**
	 * Get the set of all unique interface cluster ids in this SubGraph
	 * @return
	 */
	private Set<Integer> getInterfaceClusterIds() {
		
		Set<Integer> interfaceClusterIds = new TreeSet<Integer>();
		
		// TODO I'm not sure if it's even needed to check that the edges are contained in entities, I'd say that by definiton of SubAssembly they are... should double-check - JD 2015-12-21
		Set<Integer> entities = sto.getEntityIds();
		
		for (InterfaceEdge e : connectedGraph.edgeSet()) {
			ChainVertex s = connectedGraph.getEdgeSource(e);
			ChainVertex t = connectedGraph.getEdgeTarget(e);
			
			if (entities.contains(s.getEntity()) && entities.contains(t.getEntity())) {
				interfaceClusterIds.add(e.getClusterId());
			}
		}
		return interfaceClusterIds;
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

	public String toString() {
		return "(Stoichiometry:"+ sto.toString() + " - " +
				connectedGraph.vertexSet().size() + " vertices - " +
				connectedGraph.edgeSet().size() + " edges" +
				")"
				;
	}
}
