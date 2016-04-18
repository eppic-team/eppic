package eppic.assembly;

import org.biojava.nbio.structure.Chain;


/**
 * A vertex representing a single chain.
 * Uniquely identified by opId and chainId.
 * 
 * 
 * @author spencer
 *
 */
public class ChainVertex {
	// Primary Key:
	private int opId; // operator to generate this position within the unit cell
	
	private Chain c;
	
	private int entityId;
	
	private String chainId;
	 
	/**
	 * Default constructor for factory methods. After construction, at a minimum
	 * call {@link #setChain(Chain)} and {@link #setOpId(int)}.
	 * Use {@link #ChainVertex(Chain, int)} whenever possible.
	 */
	public ChainVertex() {
		this(null,-1);
		this.entityId = -1;
		this.chainId = null;
	}
	
	public ChainVertex(Chain c, int opId) {
		this.c = c;
		this.opId = opId;
		this.entityId = c.getCompound().getMolId();
		this.chainId = c.getChainID();
	}


	public int getOpId() {
		return opId;
	}
	
	public void setOpId(int i) {
		this.opId = i;
	}
	
	public String getChainId() {
		return chainId;
	}
	
	public void setChainId(String chainId) {
		this.chainId = chainId;
	}
	
	public Chain getChain() {
		return c;
	}
	public void setChain(Chain c) {
		this.c = c;
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
		return entityId;
	}
	
	public void setEntity(int entityId) {
		this.entityId = entityId;
	}

}