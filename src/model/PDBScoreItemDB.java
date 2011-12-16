package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class PDBScoreItemDB implements Serializable, ProcessingDataDB
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String pdbName;
	private String title;
	private String spaceGroup;
	private double resolution;
	private String expMethod;
	private RunParametersItemDB runParameters;
	
	private List<HomologsInfoItemDB> homologsInfoItems;

	private List<InterfaceItemDB> interfaceItems;
	
	private JobDB jobItem;
	
	public PDBScoreItemDB() 
	{
		interfaceItems = new ArrayList<InterfaceItemDB>();
		homologsInfoItems = new ArrayList<HomologsInfoItemDB>();
	}
	
	public PDBScoreItemDB(int uid,
						JobDB jobItem,
						String pdbName,
						String title,
						String spaceGroup,
						RunParametersItemDB runParameters) 
	{
		interfaceItems = new ArrayList<InterfaceItemDB>();
		homologsInfoItems = new ArrayList<HomologsInfoItemDB>();
		this.uid = uid;
		this.pdbName = pdbName;
		this.title = title;
		this.spaceGroup = spaceGroup;
		this.runParameters = runParameters;
		this.jobItem = jobItem;
	}
	
	public String getPdbName() {
		return pdbName;
	}

	public void setPdbName(String pdbName) {
		this.pdbName = pdbName;
	}

	public void setInterfaceItems(List<InterfaceItemDB> interfaceItems) {
		this.interfaceItems = interfaceItems;
	}

	public List<InterfaceItemDB> getInterfaceItems() {
		return interfaceItems;
	}
	
	public void addInterfaceItem(InterfaceItemDB interfaceItem) {
		this.interfaceItems.add(interfaceItem);
	}
	
	public InterfaceItemDB getInterfaceItem(int i) {
		return this.interfaceItems.get(i);
	}

	public void setHomologsInfoItems(List<HomologsInfoItemDB> homologsInfoItems) {
		this.homologsInfoItems = homologsInfoItems;
	}
	
	public List<HomologsInfoItemDB> getHomologsInfoItems() {
		return this.homologsInfoItems;
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

	public void setRunParameters(RunParametersItemDB runParameters) {
		this.runParameters = runParameters;
	}

	public RunParametersItemDB getRunParameters() {
		return runParameters;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}

	public void setJobItem(JobDB jobItem) {
		this.jobItem = jobItem;
	}

	public JobDB getJobItem() {
		return jobItem;
	}

	public double getResolution() {
		return resolution;
	}

	public void setResolution(double resolution) {
		this.resolution = resolution;
	}

	public String getExpMethod() {
		return expMethod;
	}

	public void setExpMethod(String expMethod) {
		this.expMethod = expMethod;
	}
	
	
}
