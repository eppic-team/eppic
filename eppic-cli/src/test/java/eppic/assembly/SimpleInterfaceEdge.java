package eppic.assembly;

import javax.vecmath.Point3i;

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

	@Override
	public Point3i getXtalTrans() {
		return new Point3i(0,0,0);
	}

	public String toString() {
		return interfaceId+"-"+clusterId;
	}
	
}
