package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import eppic.model.UniProtRefWarningDB;

/**
 * DTO class for UniProt Ref Warning.
 * @author AS
 */
public class UniProtRefWarning implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String text;
	
	public UniProtRefWarning() 
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
	 * @param uniProtRefWarningDB model item to convert
	 * @return DTO representation of model item
	 */
	public static UniProtRefWarning create(UniProtRefWarningDB uniProtRefWarningDB)
	{
		UniProtRefWarning warningItem = new UniProtRefWarning();
		warningItem.setText(uniProtRefWarningDB.getText());
		warningItem.setUid(uniProtRefWarningDB.getUid());
		return warningItem;
	}

}
