package eppic.assembly;

/**
 * Edge between an InterfaceVertex and an AtomVertex.
 * 
 * By default, this will be positioned between its endpoints, but the layout
 * can also be overridden by defining one or more segments.
 * 
 * @author spencer
 *
 */
class InterfaceEdge {
	// annotation data
	private int interfaceId;
	private int clusterId;
	private boolean isIsologous;
	private boolean isInfinite;
	
	public InterfaceEdge(int interfaceId) {
		this.interfaceId = interfaceId;
		
	}
	
	public int getInterfaceId() {
		return interfaceId;
	}
	
	
	@Override
	public String toString() {
		return String.format("-%d-",interfaceId);
	}

	public int getClusterId() {
		return clusterId;
	}

	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}

	public boolean isIsologous() {
		return isIsologous;
	}

	public void setIsologous(boolean isIsologous) {
		this.isIsologous = isIsologous;
	}

	public boolean isInfinite() {
		return isInfinite;
	}

	public void setInfinite(boolean isInfinite) {
		this.isInfinite = isInfinite;
	}
}