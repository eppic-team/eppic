package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

public class SupportedMethod implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String name;
	private boolean hasFieldSet;
	
	public SupportedMethod()
	{
		
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setHasFieldSet(boolean hasFieldSet) {
		this.hasFieldSet = hasFieldSet;
	}
	public boolean isHasFieldSet() {
		return hasFieldSet;
	}
}
