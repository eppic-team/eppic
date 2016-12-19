package eppic.model;

import java.io.Serializable;

public class LatticeGraphEdgeDB implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private int uid;
	
	private int interfaceId;
	
	private int interfaceClusterId;
	
	private String iVertexId;	
	private String jVertexId;
	
	private AssemblyDB assembly;
	
	private int xtalTransX;
	private int xtalTransY;
	private int xtalTransZ;
	
	private String color;

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getInterfaceId() {
		return interfaceId;
	}

	public void setInterfaceId(int interfaceId) {
		this.interfaceId = interfaceId;
	}

	public int getInterfaceClusterId() {
		return interfaceClusterId;
	}

	public void setInterfaceClusterId(int interfaceClusterId) {
		this.interfaceClusterId = interfaceClusterId;
	}

	public String getiVertexId() {
		return iVertexId;
	}

	public void setiVertexId(String iVertexId) {
		this.iVertexId = iVertexId;
	}

	public String getjVertexId() {
		return jVertexId;
	}

	public void setjVertexId(String jVertexId) {
		this.jVertexId = jVertexId;
	}

	public AssemblyDB getAssembly() {
		return assembly;
	}

	public void setAssembly(AssemblyDB assembly) {
		this.assembly = assembly;
	}

	public int getXtalTransX() {
		return xtalTransX;
	}

	public void setXtalTransX(int xtalTransX) {
		this.xtalTransX = xtalTransX;
	}

	public int getXtalTransY() {
		return xtalTransY;
	}

	public void setXtalTransY(int xtalTransY) {
		this.xtalTransY = xtalTransY;
	}

	public int getXtalTransZ() {
		return xtalTransZ;
	}

	public void setXtalTransZ(int xtalTransZ) {
		this.xtalTransZ = xtalTransZ;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

}
