package eppic.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;

import java.io.Serializable;

public class InterfaceWarningDB implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String text;

	@JsonBackReference
	private InterfaceDB interfaceItem;
	
	public InterfaceWarningDB() {
		
	}
	
	public void setInterfaceItem(InterfaceDB interfaceItem) {
		this.interfaceItem = interfaceItem;
	}

	public InterfaceDB getInterfaceItem() {
		return interfaceItem;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

}
