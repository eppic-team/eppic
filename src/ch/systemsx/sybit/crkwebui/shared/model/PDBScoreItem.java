package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import model.InterfaceItemDB;
import model.NumHomologsStringItemDB;
import model.PDBScoreItemDB;


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
	private RunParametersItem runParameters;
	
	private List<NumHomologsStringItem> numHomologsStrings;

	private List<InterfaceItem> interfaceItems;
	
	private String jobId;
	
	public PDBScoreItem() 
	{
		interfaceItems = new ArrayList<InterfaceItem>();
		numHomologsStrings = new ArrayList<NumHomologsStringItem>();
	}
	
	public PDBScoreItem(int uid,
						String pdbName,
						String title,
						String spaceGroup,
						RunParametersItem runParameters) 
	{
		interfaceItems = new ArrayList<InterfaceItem>();
		numHomologsStrings = new ArrayList<NumHomologsStringItem>();
		this.uid = uid;
		this.pdbName = pdbName;
		this.title = title;
		this.spaceGroup = spaceGroup;
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

	public void setNumHomologsStrings(List<NumHomologsStringItem> numHomologsStrings) {
		this.numHomologsStrings = numHomologsStrings;
	}
	
	public List<NumHomologsStringItem> getNumHomologsStrings() {
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

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}
	
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
		
		if(pdbScoreItemDB.getNumHomologsStrings() != null)
		{
			List<NumHomologsStringItemDB> numHomologsStringItemDBs = pdbScoreItemDB.getNumHomologsStrings();
			
			List<NumHomologsStringItem> numHomologsStringItems = new ArrayList<NumHomologsStringItem>();
			
			for(NumHomologsStringItemDB numHomologsStringItemDB : numHomologsStringItemDBs)
			{
				numHomologsStringItems.add(NumHomologsStringItem.create(numHomologsStringItemDB));
			}
			
			pdbScoreItem.setNumHomologsStrings(numHomologsStringItems);
		}
		
		pdbScoreItem.setPdbName(pdbScoreItemDB.getPdbName());
		pdbScoreItem.setRunParameters(RunParametersItem.create(pdbScoreItemDB.getRunParameters()));
		pdbScoreItem.setSpaceGroup(pdbScoreItemDB.getSpaceGroup());
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
}
