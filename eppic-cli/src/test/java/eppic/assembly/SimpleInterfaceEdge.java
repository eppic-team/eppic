package eppic.assembly;

public class SimpleInterfaceEdge implements InterfaceEdgeInterface {
	
	private int interfaceId;
	private int clusterId;
	
	public SimpleInterfaceEdge(int interfaceId, int clustertId) {
		this.interfaceId = interfaceId;
		this.clusterId = clustertId;
	}

	@Override
	public int getInterfaceId() {
		return interfaceId;
	}

	@Override
	public int getClusterId() {
		return clusterId;
	}

}
