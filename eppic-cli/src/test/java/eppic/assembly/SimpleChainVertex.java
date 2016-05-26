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

}
