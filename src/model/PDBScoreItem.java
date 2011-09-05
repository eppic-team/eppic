package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class PDBScoreItem implements Serializable, ProcessingData
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String jobId;
	private String pdbName;
	private String title;
	private String spaceGroup;
	private RunParametersItem runParameters;
	
	private List<String> numHomologsStrings;

	private List<InterfaceItem> interfaceItems;
	
	public PDBScoreItem() 
	{
		interfaceItems = new ArrayList<InterfaceItem>();
	}
	
	public String getPdbName() {
		return pdbName;
	}

	public void setPdbName(String pdbName) {
		this.pdbName = pdbName;
	}

	public String getJobId()
	{
		return jobId;
	}
	
	public void setJobId(String jobId)
	{
		this.jobId = jobId;
	}

	public void setInterfaceItems(List<InterfaceItem> interfaceItems) {
		this.interfaceItems = interfaceItems;
	}

	public List<InterfaceItem> getInterfaceItems() {
		return interfaceItems;
	}
	
	public void addInterfaceItem(InterfaceItem interfaceItem) {
		this.interfaceItems.add(interfaceItem);
	}
	
	public InterfaceItem getInterfaceItem(int i) {
		return this.interfaceItems.get(i);
	}

	public void setNumHomologsStrings(List<String> numHomologsStrings) {
		this.numHomologsStrings = numHomologsStrings;
	}
	
	public List<String> getNumHomologsStrings() {
		return this.numHomologsStrings;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
	
	public String getSpaceGroup() {
		return spaceGroup;
	}
	
	public void setSpaceGroup(String spaceGroup) {
		this.spaceGroup = spaceGroup;
	}

	public void setRunParameters(RunParametersItem runParameters) {
		this.runParameters = runParameters;
	}

	public RunParametersItem getRunParameters() {
		return runParameters;
	}
}
