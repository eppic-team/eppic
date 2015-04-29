package eppic.assembly;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Compound;
import org.biojava.nbio.structure.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A representation of the stoichiometry of an Assembly in a crystal structure
 * 
 * 
 * @author duarte_j
 *
 */
public class Stoichiometry {

	private static final Logger logger = LoggerFactory.getLogger(Stoichiometry.class);
			
	private Structure structure;
	private Assembly assembly;
	private int[] sto;
	private int[] comp;
	
	private Map<Integer,Integer> entityId2Idx;
	private Map<Integer,Integer> idx2EntityId;
	
	private Map<String,Integer> chainIds2Idx;
	private Map<Integer,String> idx2ChainIds;
	
	public Stoichiometry(Structure structure, Assembly assembly) {
		
		this.structure = structure;		
		this.assembly = assembly;
		
		int totalNumEntities = structure.getCompounds().size();
		
		// since the entityIds are not guaranteed to be 1 to n, we need to map them to indices
		entityId2Idx = new HashMap<Integer,Integer>();
		idx2EntityId = new HashMap<Integer,Integer>();
		chainIds2Idx = new HashMap<String, Integer>();
		idx2ChainIds = new HashMap<Integer, String>();
		
		int i = 0;
		for (Compound c:structure.getCompounds()) {
			entityId2Idx.put(c.getMolId(),i);
			idx2EntityId.put(i,c.getMolId());
			i++;
		}

		i = 0;
		for (Chain c:structure.getChains()) {
			chainIds2Idx.put(c.getChainID(),i);
			idx2ChainIds.put(i,c.getChainID());
			i++;
		}
		
		sto = new int[totalNumEntities];
		comp = new int[structure.getChains().size()];
	}
	
	public int getEntityIndex(int entityId) {
		return entityId2Idx.get(entityId);
	}
	
	public int getEntityId(int index) {
		return idx2EntityId.get(index);
	}
	
	public int getChainIndex(String chainId) {
		return chainIds2Idx.get(chainId);
	}
	
	public String getChainId(int chainIdx) {
		return idx2ChainIds.get(chainIdx);
	}
	
	public void add(Chain c) {
		sto[getEntityIndex(c.getCompound().getMolId())]++;
		comp[getChainIndex(c.getChainID())]++;
	}
	
	public int getNumEntities() {
		return sto.length;
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
		return sto[getEntityIndex(entityId)];
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
	
	public String toFormattedString() {
		StringBuilder stoSb = new StringBuilder();
		
		for (int i=0;i<getNumEntities();i++){
			if (sto[i]>0) {
				stoSb.append(structure.getCompoundById(getEntityId(i)).getRepresentative().getChainID());			
				if (sto[i]>1) stoSb.append(sto[i]); // for A1B1 we do AB (we ommit 1s)
			}
		}
		return stoSb.toString();
	}
	
	public String toFormattedCompositionString() {
		StringBuilder stoSb = new StringBuilder();
		
		for (int i=0;i<getNumEntities();i++){
			if (comp[i]>0) {
				stoSb.append(getChainId(i));			
				if (comp[i]>1) stoSb.append(comp[i]); // for A1B1 we do AB (we ommit 1s)
			}
		}
		return stoSb.toString();
	}
	
	/**
	 * Return the symmetry string for this stoichiometry in its assembly. 
	 * This will only work correctly on assemblies that have been previously checked
	 * to be valid with {@link Assembly#isValid()}
	 * @return
	 */
	public String getSymmetry() {
		
		String symmetry = null;
		
		if (getNumEntities()>1) {
			logger.warn("Symmetry detection for heteromeric assemblies not supported yet, setting it to unknown");
			symmetry =  "unknown";
			return symmetry;
		}
		
		int n = sto[0];
		
		// for homomers of size n>1 it's clear:
		//  - if n==1: C1
		//  - if n uneven: there should be only 1 interface cluster engaged (otherwise warn!) ==> Cn
		//  - if n==2: there should be only 1 interface cluster engaged (otherwise warn!) ==> C2
		//  - else if n even (n=2m): there can be either 1 or more interface clusters engaged:
		//      if 1: Cn
		//      if >1: Dm
		
		if (n==1) {
			if (assembly.getNumEngagedInterfaceClusters()>0) {
				logger.warn("Some interface cluster is engaged for an assembly of size 1. Something is wrong!");
			}
			symmetry = "C1";
		} else if (n%2 != 0 || n==2) {
			if (assembly.getNumEngagedInterfaceClusters()>1) {
				logger.warn("More than 1 engaged interface clusters for a homomeric assembly of size {}. Something is wrong!",n);
			}
			symmetry = "C"+n;
		} else { // even number larger than 2
			if (assembly.getNumEngagedInterfaceClusters()==1) {
				symmetry = "C"+n;
			} else {
				symmetry = "D"+(n/2);
			}
		}
		 
		// TODO detect tetrahedral, octahedral and icosahedral symmetries
		
		return symmetry;
	}
	
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
