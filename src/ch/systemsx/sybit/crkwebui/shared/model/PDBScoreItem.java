package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.HomologsInfoItemDB;
import model.InterfaceItemDB;
import model.PDBScoreItemDB;
import model.PdbBioUnitItemDB;

/**
 * DTO class for PDBScore item.
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
	private Date releaseDate;
	private String spaceGroup;
	private double resolution;
	private double rfreeValue;
	private String expMethod;
	private RunParametersItem runParameters;
	
	private List<HomologsInfoItem> homologsInfoItems;

	private List<InterfaceItem> interfaceItems;
	
	private List<PdbBioUnitItem> bioUnitItems;

	private int inputType;
	private String jobId;
	
	public PDBScoreItem() 
	{
		interfaceItems = new ArrayList<InterfaceItem>();
		homologsInfoItems = new ArrayList<HomologsInfoItem>();
		bioUnitItems = new ArrayList<PdbBioUnitItem>();
	}
	
	public PDBScoreItem(int uid,
						String pdbName,
						String title,
						String spaceGroup,
						String expMethod,
						double resolution,
						double rfreeValue,
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
		this.rfreeValue = rfreeValue;
		this.runParameters = runParameters;
	}
	
	public String getPdbName() {
		return pdbName;
	}

	public void setPdbName(String pdbName) {
		this.pdbName = pdbName;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
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

	public void setHomologsInfoItems(List<HomologsInfoItem> homologsInfoItems) {
		this.homologsInfoItems = homologsInfoItems;
	}
	
	public List<HomologsInfoItem> getHomologsInfoItems() {
		return this.homologsInfoItems;
	}
	
	public List<PdbBioUnitItem> getBioUnitItems() {
		return bioUnitItems;
	}

	public void setBioUnitItems(List<PdbBioUnitItem> bioUnitItems) {
		this.bioUnitItems = bioUnitItems;
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
	
	public double getRfreeValue() {
		return rfreeValue;
	}
	
	public void setRfreeValue(double rfreeValue) {
		this.rfreeValue = rfreeValue;
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
	
	/**
	 * Converts DB model item into DTO one.
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
		
		if(pdbScoreItemDB.getBioUnitItems() != null)
		{
			List<PdbBioUnitItemDB> bioUnitItemDBs = pdbScoreItemDB.getBioUnitItems();
			
			List<PdbBioUnitItem> bioUnitItems = new ArrayList<PdbBioUnitItem>();
			
			for(PdbBioUnitItemDB bioUnitItemDB : bioUnitItemDBs)
			{
				bioUnitItems.add(PdbBioUnitItem.create(bioUnitItemDB));
			}
			
			pdbScoreItem.setBioUnitItems(bioUnitItems);
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
		pdbScoreItem.setReleaseDate(pdbScoreItemDB.getReleaseDate());
		pdbScoreItem.setRunParameters(RunParametersItem.create(pdbScoreItemDB.getRunParameters()));
		pdbScoreItem.setSpaceGroup(pdbScoreItemDB.getSpaceGroup());
		pdbScoreItem.setExpMethod(pdbScoreItemDB.getExpMethod());
		pdbScoreItem.setResolution(pdbScoreItemDB.getResolution());
		pdbScoreItem.setRfreeValue(pdbScoreItemDB.getRfreeValue());
		pdbScoreItem.setTitle(pdbScoreItemDB.getTitle());
		pdbScoreItem.setUid(pdbScoreItemDB.getUid());
		return pdbScoreItem;
	}
}
