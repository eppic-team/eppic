package eppic.assembly;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * A representation of the stoichiometry of an Assembly in a crystal structure
 * 
 * 
 * @author duarte_j
 *
 */
public class Stoichiometry {

	//private static final Logger logger = LoggerFactory.getLogger(Stoichiometry.class);
	
	private CrystalAssemblies crystalAssemblies;
	
	private int[] sto;
	private int[] comp;
	
	public Stoichiometry(UndirectedGraph<ChainVertex, InterfaceEdge> connectedGraph, CrystalAssemblies crystalAssemblies) {
		
		this.crystalAssemblies = crystalAssemblies;
		
		sto = new int[crystalAssemblies.getNumEntitiesInStructure()];
		comp = new int[crystalAssemblies.getNumChainsInStructure()];

		for (ChainVertex v:connectedGraph.vertexSet()) {
			sto[crystalAssemblies.getEntityIndex(v.getChain().getCompound().getMolId())]++;
			comp[crystalAssemblies.getChainIndex(v.getChain().getChainID())]++;
		}

		
	}
	
	
	/**
	 * Get all entity ids present in this stoichiometry
	 * @return
	 */
	public Set<Integer> getEntityIds() {
		Set<Integer> entityIds = new HashSet<Integer>();
		for (int i=0;i<getNumEntities();i++) {
			if (sto[i]>0) {
				entityIds.add(crystalAssemblies.getEntityId(i));
			}
		}
		return entityIds;
	}
	
	private int getNumEntities() {
		return sto.length;
	}
	
	public int getNumPresentEntities() {
		int numPresentEntities = 0;
		for (int i=0;i<this.getNumEntities();i++) {
			if (sto[i]>0) {
				numPresentEntities ++;
			}
		}
		return numPresentEntities;
	}
	
	/**
	 * Returns the stoichiometry represented as a vector of positive integers with counts per entity
	 * @return
	 */
	public int[] getStoichiometry() {
		return sto;
	}
	
	/**
	 * Returns the composition represented as a vector of positive integers with counts per chain 
	 * @return
	 */
	public int[] getComposition() {
		return comp;
	}
	
	public int getCount(int entityId) {
		return sto[crystalAssemblies.getEntityIndex(entityId)];
	}
	
	public int getCountForIndex(int i) {
		return sto[i];
	}
	
	public int getTotalSize() {
		int size = 0; 
		for (int i=0;i<getNumEntities();i++) {
			size += sto[i];
		}
		return size;
	}
	
	/**
	 * Tells whether 2 stoichiometries are overlapping in any of their entities,
	 * this is equivalent to !{@link #isOrthogonal(Stoichiometry)}
	 * @param other
	 * @return
	 */
	public boolean isOverlapping(Stoichiometry other) {
		return !isOrthogonal(other);
	}
	
	/**
	 * Tells whether 2 stoichiometries are orthogonal: i.e. they don't share any of their entities
	 * @param other
	 * @return
	 */
	public boolean isOrthogonal(Stoichiometry other) {
		if (this.getNumEntities()!=other.getNumEntities()) 
			throw new IllegalArgumentException("Vectors of different length");
		if (this.getNumEntities()==0) 
			throw new IllegalArgumentException("Vectors have size 0"); 
		
		int scalarProduct = 0;
		for (int i=0;i<this.getNumEntities();i++) {
			scalarProduct += sto[i]*other.sto[i];
		}
		return scalarProduct == 0;
	}
	
	/**
	 * Tells whether this stoichiometry is even: a stoichiometry is even if and
	 * only if all of its non-zero members have the same count
	 * @return true if even, false otherwise, if all members have 0 count it also returns false
	 */
	public boolean isEven() {
		int nonzero = getFirstNonZero();
		// all are zero
		if (nonzero == -1) return false;
		
		for (int i=0;i<this.getNumEntities();i++) {
			if (sto[i]>0 && sto[i]!=nonzero) return false;
		}
		
		return true;
	}
	
	/**
	 * Return the first non-zero count or -1 if all are 0
	 * @return
	 */
	protected int getFirstNonZero() {
		int nonzero = -1;
		for (int i=0;i<this.getNumEntities();i++) {
			if (sto[i]>0) {
				nonzero = sto[i];
				break;
			}
		}
		return nonzero;
	}
	
	public String toFormattedString() {
		StringBuilder stoSb = new StringBuilder();
		
		for (int i=0;i<getNumEntities();i++){
			if (sto[i]>0) {
				stoSb.append(crystalAssemblies.getRepresentativeChainIdForEntityIndex(i));			
				if (sto[i]>1) stoSb.append(sto[i]); // for A1B1 we do AB (we ommit 1s)
			}
		}
		return stoSb.toString();
	}
	
	public String toFormattedCompositionString() {
		StringBuilder stoSb = new StringBuilder();
		
		for (int i=0;i<crystalAssemblies.getNumChainsInStructure();i++){
			if (comp[i]>0) {
				stoSb.append(crystalAssemblies.getChainId(i));			
				if (comp[i]>1) stoSb.append(comp[i]); // for A1B1 we do AB (we ommit 1s)
			}
		}
		return stoSb.toString();
	}
	
	/**
	 * Returns true if this stoichiometry covers all entities, i.e. it has 
	 * a count >0 for all of its member molecules. False otherwise
	 * @return
	 */
	public boolean isFullyCovering() {
		for (int i=0;i<sto.length;i++) {
			if (sto[i]==0) return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(sto);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(sto);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Stoichiometry other = (Stoichiometry) obj;
		if (!Arrays.equals(sto, other.sto))
			return false;
		return true;
	}
	
	/**
	 * Return the greatest common divisor (GCD, aka greatest common factor GCF) for the given ints
	 * This is an implementation of the Euclidean algorithm:
	 * http://en.wikipedia.org/wiki/Euclidean_algorithm
	 * Thanks to this SO question: 
	 * http://stackoverflow.com/questions/4009198/java-get-greatest-common-divisor
	 * @param a
	 * @param b
	 * @return
	 */
	public static int gcd(int a, int b) {		
		if (b==0) return a;
		return gcd(b,a%b);
	}

	/**
	 * Return the greatest common divisor (GCD) of the given array of ints
	 * @param values
	 * @return
	 */
	public static int gcd(int[] values) {
		if (values.length==0) throw new IllegalArgumentException("Can't calculate GCD for an empty array");
		
		// we go recursively		
		int result = values[0];
		for(int i = 1; i < values.length; i++){
		    result = gcd(result, values[i]);
		}
		return result;
		
	}
}
