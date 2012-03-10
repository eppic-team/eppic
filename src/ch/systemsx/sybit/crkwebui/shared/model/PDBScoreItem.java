package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import model.HomologsInfoItemDB;
import model.InterfaceItemDB;
import model.PDBScoreItemDB;

/**
 * DTO class for PDBScore item
 * @author AS
 */
public class PDBScoreItem implements Serializable, ProcessingData
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
	private int inputType;
	private RunParametersItem runParameters;
	
	private List<HomologsInfoItem> homologsInfoItems;

	private List<InterfaceItem> interfaceItems;
	
	private String jobId;
	
	public PDBScoreItem() 
	{
		interfaceItems = new ArrayList<InterfaceItem>();
		homologsInfoItems = new ArrayList<HomologsInfoItem>();
	}
	
	public PDBScoreItem(int uid,
						String pdbName,
						String title,
						String spaceGroup,
						String expMethod,
						double resolution,
						RunParametersItem runParameters) 
	{
		this.interfaceItems = new ArrayList<InterfaceItem>();
		this.homologsInfoItems = new ArrayList<HomologsInfoItem>();
		this.uid = uid;
		this.pdbName = pdbName;
		this.title = title;
		this.spaceGroup = spaceGroup;
		this.expMethod = expMethod;
		this.resolution = resolution;
		this.runParameters = runParameters;
	}
	
	public String getPdbName() {
		return pdbName;
	}

	public void setPdbName(String pdbName) {
		this.pdbName = pdbName;
	}

//	public String getJobId()
//	{
//		return jobId;
//	}
//	
//	public void setJobId(String jobId)
//	{
//		this.jobId = jobId;
//	}

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

	public void setHomologsInfoItems(List<HomologsInfoItem> homologsInfoItems) {
		this.homologsInfoItems = homologsInfoItems;
	}
	
	public List<HomologsInfoItem> getHomologsInfoItems() {
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
	
	public String getExpMethod() {
		return expMethod;
	}
	
	public void setExpMethod(String expMethod) {
		this.expMethod = expMethod;
	}
	
	public double getResolution() {
		return resolution;
	}
	
	public void setResolution(double resolution) {
		this.resolution = resolution;
	}

	public void setRunParameters(RunParametersItem runParameters) {
		this.runParameters = runParameters;
	}

	public RunParametersItem getRunParameters() {
		return runParameters;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}
	
	/**
	 * Convert DB model item into DTO one
	 * @param pdbScoreItemDB model item to convert
	 * @return DTO representation of model item
	 */
	public static PDBScoreItem create(PDBScoreItemDB pdbScoreItemDB)
	{
		PDBScoreItem pdbScoreItem = new PDBScoreItem();
		
		if(pdbScoreItemDB.getInterfaceItems() != null)
		{
			List<InterfaceItemDB> interfaceItemDBs = pdbScoreItemDB.getInterfaceItems();
			
			List<InterfaceItem> interfaceItems = new ArrayList<InterfaceItem>();
			
			for(InterfaceItemDB interfaceResidueItemDB : interfaceItemDBs)
			{
				interfaceItems.add(InterfaceItem.create(interfaceResidueItemDB));
			}
			
			pdbScoreItem.setInterfaceItems(interfaceItems);
		}
		
		if(pdbScoreItemDB.getHomologsInfoItems() != null)
		{
			List<HomologsInfoItemDB> homologsInfoItemDBs = pdbScoreItemDB.getHomologsInfoItems();
			
			List<HomologsInfoItem> homologsStringItems = new ArrayList<HomologsInfoItem>();
			
			for(HomologsInfoItemDB homologsStringItemDB : homologsInfoItemDBs)
			{
				homologsStringItems.add(HomologsInfoItem.create(homologsStringItemDB));
			}
			
			pdbScoreItem.setHomologsInfoItems(homologsStringItems);
		}
		
		pdbScoreItem.setPdbName(pdbScoreItemDB.getPdbName());
		pdbScoreItem.setRunParameters(RunParametersItem.create(pdbScoreItemDB.getRunParameters()));
		pdbScoreItem.setSpaceGroup(pdbScoreItemDB.getSpaceGroup());
		pdbScoreItem.setExpMethod(pdbScoreItemDB.getExpMethod());
		pdbScoreItem.setResolution(pdbScoreItemDB.getResolution());
		pdbScoreItem.setTitle(pdbScoreItemDB.getTitle());
		pdbScoreItem.setUid(pdbScoreItemDB.getUid());
		return pdbScoreItem;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobId() {
		return jobId;
	}

	public void setInputType(int inputType) {
		this.inputType = inputType;
	}

	public int getInputType() {
		return inputType;
	}
}
