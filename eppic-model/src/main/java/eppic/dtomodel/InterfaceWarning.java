package eppic.dtomodel;

import java.io.Serializable;

import eppic.model.InterfaceWarningDB;

/**
 * DTO class for interface warning entry.
 */
public class InterfaceWarning implements Serializable 
{
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String text;
	
	public InterfaceWarning() 
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
	 * @param interfaceWarningDB model item to convert
	 * @return DTO representation of model item
	 */
	public static InterfaceWarning create(InterfaceWarningDB interfaceWarningDB)
	{
		InterfaceWarning interfaceWarning = new InterfaceWarning();
		interfaceWarning.setText(interfaceWarningDB.getText());
		interfaceWarning.setUid(interfaceWarningDB.getUid());
		return interfaceWarning;
	}
}
