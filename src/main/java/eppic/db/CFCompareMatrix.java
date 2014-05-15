package eppic.db;

/**
 * A class to contain a matrix of lattice overlap scores for a set of PDBs,
 * from which the crystal from clusters can be obtained.
 * 
 * 
 * @author duarte_j
 *
 */
public class CFCompareMatrix {
	
	private LatticeOverlapScore[][] matrix;

	public CFCompareMatrix(LatticeOverlapScore[][] matrix) {
		this.matrix = matrix;
	}
	
	public LatticeOverlapScore[][] getMatrix() {
		return matrix;
	}
}
