package eppic.model;

import java.io.Serializable;

public class LatticeGraphVertexDB implements Serializable {

	private static final long serialVersionUID = 1L;

	private int uid;
	
	/**
	 * The vertex id: chain_id+_+op_id
	 */
	private String vertexId;
	
	/**
	 * The id for the entity or chain cluster 
	 */
	private String repChainId;
	
	private AssemblyDB assembly;
	
	private double x2d;
	private double y2d;
	
	private String color;

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getVertexId() {
		return vertexId;
	}

	public void setVertexId(String vertexId) {
		this.vertexId = vertexId;
	}

	public String getRepChainId() {
		return repChainId;
	}

	public void setRepChainId(String repChainId) {
		this.repChainId = repChainId;
	}

	public AssemblyDB getAssembly() {
		return assembly;
	}

	public void setAssembly(AssemblyDB assembly) {
		this.assembly = assembly;
	}

	public double getX2d() {
		return x2d;
	}

	public void setX2d(double x2d) {
		this.x2d = x2d;
	}

	public double getY2d() {
		return y2d;
	}

	public void setY2d(double y2d) {
		this.y2d = y2d;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
}
