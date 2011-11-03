package model;

import java.io.Serializable;

public class InterfaceResidueMethodItemDB implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String method;
	private Float score;
	
	private InterfaceResidueItemDB interfaceResidueItem;
	
	public InterfaceResidueMethodItemDB()
	{
		
	}
	
	public InterfaceResidueMethodItemDB(Float score, String method) {
		this.score = score;
		this.method = method;
	}

	public void setScore(Float score) {
		this.score = score;
	}

	public Float getScore() {
		return score;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getMethod() {
		return method;
	}

	public void setInterfaceResidueItem(InterfaceResidueItemDB interfaceResidueItem) {
		this.interfaceResidueItem = interfaceResidueItem;
	}

	public InterfaceResidueItemDB getInterfaceResidueItem() {
		return interfaceResidueItem;
	}
	
	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}
}
