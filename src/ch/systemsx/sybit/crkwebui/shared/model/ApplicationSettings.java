package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.Map;

public class ApplicationSettings implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// supported scoring methods
	private String[] scoresTypes;
	
	// grid settings - column specification
	private Map<String, String> gridProperties;

	public void setScoresTypes(String[] scoresTypes) {
		this.scoresTypes = scoresTypes;
	}

	public String[] getScoresTypes() {
		return scoresTypes;
	}

	public void setGridProperties(Map<String, String> gridProperties) {
		this.gridProperties = gridProperties;
	}

	public Map<String, String> getGridProperties() {
		return gridProperties;
	}
}
