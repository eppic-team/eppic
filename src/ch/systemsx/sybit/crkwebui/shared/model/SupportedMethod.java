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
	 * Name of the method. 
	 */
	private String name;
	
	/**
	 * Flag to specify whether there is input fieldset for this method. 
	 */
	private boolean hasFieldSet;
	
	public SupportedMethod()
	{
		
	}
	
	/**
	 * Sets name of the supported method.
	 * @param name name of the supported method
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Retrieves name of the supported method.
	 * @return name of the supported method
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets whether method should have field set displayed in input panel.
	 * @param hasFieldSet flag specifying whether method should have field set displayed in input panel
	 */
	public void setHasFieldSet(boolean hasFieldSet) {
		this.hasFieldSet = hasFieldSet;
	}
	
	/**
	 * Retrieves information whether method should have field set displayed in input panel.
	 * @return flag specifying whether method should have field set displayed in input panel
	 */
	public boolean isHasFieldSet() {
		return hasFieldSet;
	}
}
