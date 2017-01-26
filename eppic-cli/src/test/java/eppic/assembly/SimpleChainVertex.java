package eppic.assembly;

public class SimpleChainVertex implements ChainVertexInterface {

	
	private String chainId;
	private int opId;
	private int entityId;
	
	public SimpleChainVertex(String chainId, int opId, int entityId) {
		this.chainId = chainId;
		this.opId = opId;
		this.entityId = entityId;
	}
	
	
	@Override
	public String getChainId() {
		return chainId;
	}

	@Override
	public int getOpId() {
		return opId;
	}

	@Override
	public int getEntityId() {
		return entityId;
	}

	public String toString() {
		return chainId+"("+entityId+")-"+opId;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chainId == null) ? 0 : chainId.hashCode());
		result = prime * result + opId;
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleChainVertex other = (SimpleChainVertex) obj;
		if (chainId == null) {
			if (other.chainId != null)
				return false;
		} else if (!chainId.equals(other.chainId))
			return false;
		if (opId != other.opId)
			return false;
		return true;
	}
}
