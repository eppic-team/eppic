package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import model.QueryWarningItemDB;

/**
 * DTO class for Query Warning item.
 * @author AS
 */
public class QueryWarningItem implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String text;
	
	public QueryWarningItem() 
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
	 * @param queryWarningItemDB model item to convert
	 * @return DTO representation of model item
	 */
	public static QueryWarningItem create(QueryWarningItemDB queryWarningItemDB)
	{
		QueryWarningItem warningItem = new QueryWarningItem();
		warningItem.setText(queryWarningItemDB.getText());
		warningItem.setUid(queryWarningItemDB.getUid());
		return warningItem;
	}

}
