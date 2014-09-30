package eppic.model;

import java.io.Serializable;

public class InterfaceWarningDB implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String text;
	
	private InterfaceDB interfaceItem;
	
	public InterfaceWarningDB() {
		
	}
	
	public void setInterfaceItem(InterfaceDB interfaceItem) {
		this.interfaceItem = interfaceItem;
	}

	public InterfaceDB getInterfaceItem() {
		return interfaceItem;
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

}
