package eppic.assembly;

import java.util.Arrays;

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
	
	public Stoichiometry(Structure structure) {
		
		this.structure = structure;		
		
		int totalNumEntities = structure.getCompounds().size();		

		sto = new int[totalNumEntities];
	}
	
	public void addEntity(int entityId) {
		sto[entityId-1]++;
	}
	
	public int getNumEntities() {
		return sto.length;
	}
	
	public int[] getStoichiometry() {
		return sto;
	}
	
	public int getCount(int entityId) {
		return sto[entityId-1];
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
				stoSb.append(structure.getCompoundById(i+1).getRepresentative().getChainID());			
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
}
