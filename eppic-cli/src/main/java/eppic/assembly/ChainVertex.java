package eppic.assembly;

import org.biojava.nbio.structure.Chain;


/**
 * A vertex representing a single chain.
 * Uniquely identified by opId and chainId
 * 
 * 
 * @author spencer
 *
 */
public class ChainVertex {
	// Primary Key:
	private int opId; // operator to generate this position within the unit cell
	
	private Chain c;
	 
	
	public ChainVertex(Chain c, int opId) {
		this.c = c;
		this.opId = opId;
	}

	public String getChainId() {
		return c.getChainID();
	}
	
	public int getOpId() {
		return opId;
	}
	
	public Chain getChain() {
		return c;
	}
	
	@Override
	public String toString() {
		return getChainId()+opId;
	}
	/**
	 * Hash key based on chain and op
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getChainId() == null) ? 0 : getChainId().hashCode());
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
		if (getChainId() == null) {
			if (other.getChainId() != null)
				return false;
		} else if (!getChainId().equals(other.getChainId()))
			return false;
		if (opId != other.opId)
			return false;
		return true;
	}

	public int getEntity() {
		return c.getCompound().getMolId();
	}

}