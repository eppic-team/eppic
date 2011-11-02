package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import model.InterfaceResidueMethodItemDB;

public class InterfaceResidueMethodItem implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String method;
	private float score;
	
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

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}
	
	public static InterfaceResidueMethodItem create(InterfaceResidueMethodItemDB interfaceResidueMethodItemDB)
	{
		InterfaceResidueMethodItem interfaceResidueMethodItem = new InterfaceResidueMethodItem();
		interfaceResidueMethodItem.setMethod(interfaceResidueMethodItemDB.getMethod());
		interfaceResidueMethodItem.setScore(interfaceResidueMethodItemDB.getScore());
		interfaceResidueMethodItem.setUid(interfaceResidueMethodItemDB.getUid());
		return interfaceResidueMethodItem;
	}
}
