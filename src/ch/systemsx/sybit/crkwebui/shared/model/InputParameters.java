package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

	private List<String> methods;

	private int maxNrOfSequences;
	private int reducedAlphabet;
	private String searchMode;

	private float softIdentityCutoff;
	private float hardIdentityCutoff;

	public InputParameters()
	{
		this.methods = new ArrayList<String>();
	}

	public List<String> getMethods() {
		return methods;
	}

	public void setMethods(List<String> methods) {
		this.methods = methods;
	}

	public int getMaxNrOfSequences() {
		return maxNrOfSequences;
	}

	public void setMaxNrOfSequences(int maxNrOfSequences) {
		this.maxNrOfSequences = maxNrOfSequences;
	}

	public int getReducedAlphabet() {
		return reducedAlphabet;
	}

	public void setReducedAlphabet(int reducedAlphabet) {
		this.reducedAlphabet = reducedAlphabet;
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
