package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import model.NumHomologsStringItemDB;

public class NumHomologsStringItem implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String text;
	
	public NumHomologsStringItem() 
	{
		
	}
	
	public NumHomologsStringItem(int uid,
								 String text) 
	{
		this.uid = uid;
		this.text = text;
	}
	
	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
	public static NumHomologsStringItem create(NumHomologsStringItemDB numHomologsStringItemDB)
	{
		NumHomologsStringItem numHomologsStringItem = new NumHomologsStringItem();
		numHomologsStringItem.setText(numHomologsStringItemDB.getText());
		numHomologsStringItem.setUid(numHomologsStringItemDB.getUid());
		return numHomologsStringItem;
	}

}
