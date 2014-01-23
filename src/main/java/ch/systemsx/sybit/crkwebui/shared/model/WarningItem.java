package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import model.WarningItemDB;

/**
 * DTO class for warning item entry.
 */
public class WarningItem implements Serializable 
{
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

	/**
	 * Converts DB model item into DTO one.
	 * @param warningItemDB model item to convert
	 * @return DTO representation of model item
	 */
	public static WarningItem create(WarningItemDB warningItemDB)
	{
		WarningItem warningItem = new WarningItem();
		warningItem.setText(warningItemDB.getText());
		warningItem.setUid(warningItemDB.getUid());
		return warningItem;
	}
}
