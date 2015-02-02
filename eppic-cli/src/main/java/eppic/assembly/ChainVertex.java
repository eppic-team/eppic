package eppic.assembly;

import javax.vecmath.Point3d;


/**
 * A vertex representing a single chain.
 * Uniquely identified by cell (assumed to be [0,0,0]), asymmetric unit (given
 * by an operator ID relative to the enclosing CrystalCell), and chain ID.
 * @author spencer
 *
 */
public class ChainVertex implements LatticeGraphVertex {
	// Primary Key:
	private int opId; // operator to generate this position within the unit cell
	private String chainId;
	
	// Metadata
	private int entity; //TODO
	private Point3d auPosition;
	
	public ChainVertex(String chainId, int opId) {
		super();
		this.chainId = chainId;
		this.opId = opId;
		this.auPosition = null;
	}

	public String getChainId() {
		return chainId;
	}
	public int getOpId() {
		return opId;
	}
	
	@Override
	public String toString() {
		return chainId+opId;
	}
	/**
	 * Hash key based on chain and op
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chainId == null) ? 0 : chainId.hashCode());
		result = prime * result + opId;
		return result;
	}
	/**
	 * Equality based on chain and op
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChainVertex other = (ChainVertex) obj;
		if (chainId == null) {
			if (other.chainId != null)
				return false;
		} else if (!chainId.equals(other.chainId))
			return false;
		if (opId != other.opId)
			return false;
		return true;
	}

	public void setAUPosition(Point3d centroidAU) {
		this.auPosition = centroidAU;
	}
	
	public Point3d getAUPosition() {
		return this.auPosition;
	}
}