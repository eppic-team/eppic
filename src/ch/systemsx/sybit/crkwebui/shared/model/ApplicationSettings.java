package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.List;
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
	
	private InputParameters defaultParametersValues;
	
	private List<Integer> reducedAlphabetList;

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

	public void setDefaultParametersValues(InputParameters defaultParametersValues) {
		this.defaultParametersValues = defaultParametersValues;
	}

	public InputParameters getDefaultParametersValues() {
		return defaultParametersValues;
	}

	public void setReducedAlphabetList(List<Integer> reducedAlphabetList) {
		this.reducedAlphabetList = reducedAlphabetList;
	}

	public List<Integer> getReducedAlphabetList() {
		return reducedAlphabetList;
	}
}
