package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import model.WarningItemDB;

public class WarningItem implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String text;
	
	public WarningItem() 
	{
		
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

	public static WarningItem create(WarningItemDB warningItemDB)
	{
		WarningItem warningItem = new WarningItem();
		warningItem.setText(warningItemDB.getText());
		warningItem.setUid(warningItemDB.getUid());
		return warningItem;
	}
}
