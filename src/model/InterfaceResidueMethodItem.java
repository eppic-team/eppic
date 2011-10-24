package model;

import java.io.Serializable;

public class InterfaceResidueMethodItem implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String method;
	private float score;
	
	private InterfaceResidueItem interfaceResidueItem;
	
	public InterfaceResidueMethodItem()
	{
		
	}
	
	public InterfaceResidueMethodItem(float score, String method) {
		this.score = score;
		this.method = method;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public float getScore() {
		return score;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getMethod() {
		return method;
	}

	public void setInterfaceResidueItem(InterfaceResidueItem interfaceResidueItem) {
		this.interfaceResidueItem = interfaceResidueItem;
	}

	public InterfaceResidueItem getInterfaceResidueItem() {
		return interfaceResidueItem;
	}
	
	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}
}
