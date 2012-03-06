package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

/**
 * This class represents supported method.
 * @author AS
 */
public class SupportedMethod implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Name of the method 
	 */
	private String name;
	
	/**
	 * Flag to specify whether there is input fieldset for this method 
	 */
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
