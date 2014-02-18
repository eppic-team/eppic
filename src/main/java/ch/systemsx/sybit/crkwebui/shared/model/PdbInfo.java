package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eppic.model.ChainClusterDB;
import eppic.model.InterfaceClusterDB;
import eppic.model.PdbInfoDB;
import eppic.model.AssemblyDB;

/**
 * DTO class for PDBScore item.
 * @author AS
 */

@XmlRootElement(name = "eppicAnalysis")
@XmlType(propOrder = { "pdbCode", "title", "releaseDate", "expMethod",
						"spaceGroup", "resolution", "rfreeValue",
						"chainClusters", "interfaces",
						"runParameters", "assemblies"})
@XmlAccessorType(XmlAccessType.FIELD)
public class PdbInfo implements Serializable, ProcessingData
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@XmlAttribute
	private int uid;
	
	@XmlAttribute
	private String jobId;
	@XmlAttribute
	private int inputType;
	@XmlAttribute
	private String inputName;
	
	private String pdbCode;
	private String title;
	private Date releaseDate;
	private String spaceGroup;
	private double resolution;
	private double rfreeValue;
	private String expMethod;
	
	private RunParameters runParameters;
	
	private List<ChainCluster> chainClusters;
	
	private List<InterfaceCluster> interfaceClusters;
	
	private List<Assembly> assemblies;
	
	public PdbInfo() 
	{
		chainClusters = new ArrayList<ChainCluster>();
		interfaceClusters = new ArrayList<InterfaceCluster>();
		assemblies = new ArrayList<Assembly>();
	}
	
	public PdbInfo(int uid,
						String pdbName,
						String title,
						String spaceGroup,
						String expMethod,
						double resolution,
						double rfreeValue,
						RunParameters runParameters) 
	{
		this.interfaceClusters = new ArrayList<InterfaceCluster>();
		this.chainClusters = new ArrayList<ChainCluster>();
		this.assemblies = new ArrayList<Assembly>();
		this.uid = uid;
		this.pdbCode = pdbName;
		this.title = title;
		this.spaceGroup = spaceGroup;
		this.expMethod = expMethod;
		this.resolution = resolution;
		this.rfreeValue = rfreeValue;
		this.runParameters = runParameters;
	}
	
	public String getPdbCode() {
		return pdbCode;
	}

	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public void setInterfaceClusters(List<InterfaceCluster> interfaceClusters) {
		this.interfaceClusters = interfaceClusters;
	}

	public List<InterfaceCluster> getInterfaceClusters() {
		return interfaceClusters;
	}
	
	public void addInterfaceCluster(InterfaceCluster interfaceCluster) {
		this.interfaceClusters.add(interfaceCluster);
	}

	public void setChainClusters(List<ChainCluster> chainClusters) {
		this.chainClusters = chainClusters;
	}
	
	public List<ChainCluster> getChainClusters() {
		return this.chainClusters;
	}
	
	public List<Assembly> getAssemblies() {
		return assemblies;
	}

	public void setAssemblies(List<Assembly> assemblies) {
		this.assemblies = assemblies;
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

	public void setRunParameters(RunParameters runParameters) {
		this.runParameters = runParameters;
	}

	public RunParameters getRunParameters() {
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
	
	public String getInputName() {
		return inputName;
	}

	public void setInputName(String inputName) {
		this.inputName = inputName;
	}
	
	/**
	 * Returns a list of interfaces present in this pdb
	 * @return the list of interfaces
	 */
	public List<Interface> getInterfaces(){
		
		if(this.getInterfaceClusters() == null) return null;
		
		List<Interface> interfaces = new ArrayList<Interface>();
		
		for(InterfaceCluster cluster:this.getInterfaceClusters()){
			for(Interface interf: cluster.getInterfaces()){
				interfaces.add(interf);
			}
		}
		
		return interfaces;
	}
	
	/**
	 * return an interface with the id
	 * @param interfaceId
	 * @return the interface
	 */
	public Interface getInterface(int interfaceId){
		
		if(this.getInterfaces() == null) return null;
		
		return getInterfaces().get(interfaceId - 1);
	}
	

	/**
	 * Converts DB model item into DTO one.
	 * @param pdbScoreItemDB model item to convert
	 * @return DTO representation of model item
	 */
	public static PdbInfo create(PdbInfoDB pdbScoreItemDB)
	{
		PdbInfo pdbInfo = new PdbInfo();
		
		if(pdbScoreItemDB.getInterfaceClusters() != null)
		{
			List<InterfaceClusterDB> interfaceClusterDBs = pdbScoreItemDB.getInterfaceClusters();
			
			List<InterfaceCluster> interfaceClusters = new ArrayList<InterfaceCluster>();
			
			for(InterfaceClusterDB interfaceClusterDB : interfaceClusterDBs)
			{
				interfaceClusters.add(InterfaceCluster.create(interfaceClusterDB));
			}
			
			pdbInfo.setInterfaceClusters(interfaceClusters);
		}
		
		if(pdbScoreItemDB.getAssemblies() != null)
		{
			List<AssemblyDB> assemblyDBs = pdbScoreItemDB.getAssemblies();
			
			List<Assembly> assemblies = new ArrayList<Assembly>();
			
			for(AssemblyDB assemblyDB : assemblyDBs)
			{
				assemblies.add(Assembly.create(assemblyDB));
			}
			
			pdbInfo.setAssemblies(assemblies);
		}
		
		if(pdbScoreItemDB.getChainClusters() != null)
		{
			List<ChainClusterDB> homologsInfoItemDBs = pdbScoreItemDB.getChainClusters();
			
			List<ChainCluster> homologsStringItems = new ArrayList<ChainCluster>();
			
			for(ChainClusterDB homologsStringItemDB : homologsInfoItemDBs)
			{
				homologsStringItems.add(ChainCluster.create(homologsStringItemDB));
			}
			
			pdbInfo.setChainClusters(homologsStringItems);
		}
		
		pdbInfo.setPdbCode(pdbScoreItemDB.getPdbCode());
		pdbInfo.setReleaseDate(pdbScoreItemDB.getReleaseDate());
		pdbInfo.setRunParameters(RunParameters.create(pdbScoreItemDB.getRunParameters()));
		pdbInfo.setSpaceGroup(pdbScoreItemDB.getSpaceGroup());
		pdbInfo.setExpMethod(pdbScoreItemDB.getExpMethod());
		pdbInfo.setResolution(pdbScoreItemDB.getResolution());
		pdbInfo.setRfreeValue(pdbScoreItemDB.getRfreeValue());
		pdbInfo.setTitle(pdbScoreItemDB.getTitle());
		pdbInfo.setUid(pdbScoreItemDB.getUid());
		return pdbInfo;
	}
}
