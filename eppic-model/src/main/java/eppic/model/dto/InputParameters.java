package eppic.model.dto;

import java.io.Serializable;

/**
 * This class is used to transfer parameters selected by the user.
 * @author srebniak_a
 *
 */
public class InputParameters implements Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private int maxNrOfSequences;
	private String searchMode;

	private float softIdentityCutoff;
	private float hardIdentityCutoff;

	public InputParameters()
	{
	}

	public int getMaxNrOfSequences() {
		return maxNrOfSequences;
	}

	public void setMaxNrOfSequences(int maxNrOfSequences) {
		this.maxNrOfSequences = maxNrOfSequences;
	}

	public float getSoftIdentityCutoff() {
		return softIdentityCutoff;
	}

	public void setSoftIdentityCutoff(float softIdentityCutoff) {
		this.softIdentityCutoff = softIdentityCutoff;
	}

	public void setSoftIdentityCutoff(int softIdentityCutoff) {
		this.softIdentityCutoff = (float) (softIdentityCutoff/100.0);
	}
	
	public float getHardIdentityCutoff() {
		return hardIdentityCutoff;
	}

	public void setHardIdentityCutoff(float hardIdentityCutoff) {
		this.hardIdentityCutoff = hardIdentityCutoff;
	}
	
	public void setHardIdentityCutoff(int hardIdentityCutoff) {
		this.hardIdentityCutoff = (float) (hardIdentityCutoff/100.0);
	}

	public void setSearchMode(String searchMode) {
		this.searchMode = searchMode;
	}

	public String getSearchMode() {
		return searchMode;
	}
}
