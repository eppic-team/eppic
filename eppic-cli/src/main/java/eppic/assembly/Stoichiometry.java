package eppic.assembly;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.biojava.nbio.structure.Compound;
import org.biojava.nbio.structure.Structure;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * A representation of the stoichiometry of a crystal structure
 * 
 * 
 * @author duarte_j
 *
 */
public class Stoichiometry {

	//private static final Logger logger = LoggerFactory.getLogger(Stoichiometry.class);
			
	private Structure structure;
	private int[] sto;
	
	private Map<Integer,Integer> entityId2Idx;
	private Map<Integer,Integer> idx2EntityId;
	
	public Stoichiometry(Structure structure) {
		
		this.structure = structure;		
		
		int totalNumEntities = structure.getCompounds().size();
		
		// since the entityIds are not guaranteed to be 1 to n, we need to map them to indices
		entityId2Idx = new HashMap<Integer,Integer>();
		idx2EntityId = new HashMap<Integer,Integer>();
		int i = 0;
		for (Compound c:structure.getCompounds()) {
			entityId2Idx.put(c.getMolId(),i);
			idx2EntityId.put(i,c.getMolId());
			i++;
		}

		sto = new int[totalNumEntities];
	}
	
	private int getIndex(int entityId) {
		return entityId2Idx.get(entityId);
	}
	
	private int getEntityId(int index) {
		return idx2EntityId.get(index);
	}
	
	public void addEntity(int entityId) {
		sto[getIndex(entityId)]++;
	}
	
	public int getNumEntities() {
		return sto.length;
	}
	
	public int[] getStoichiometry() {
		return sto;
	}
	
	public int getCount(int entityId) {
		return sto[getIndex(entityId)];
	}
	
	public int getTotalSize() {
		int size = 0; 
		for (int i=0;i<getNumEntities();i++) {
			size += sto[i];
		}
		return size;
	}
	
	public boolean isOverlapping(Stoichiometry other) {
		return !isOrthogonal(other);
	}
	
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
			// note: this relies on mol ids in the PDB being 1 to n, that might not be true, we need to check!
			if (sto[i]>0) {
				stoSb.append(structure.getCompoundById(getEntityId(i)).getRepresentative().getChainID());			
				if (sto[i]>1) stoSb.append(sto[i]); // for A1B1 we do AB (we ommit 1s)
			}
		}
		return stoSb.toString();
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
