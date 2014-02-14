package eppic.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class PdbInfoDB implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String title;
	private Date releaseDate;
	private String spaceGroup;
	private double resolution;
	private double rfreeValue;
	private String expMethod;
	
	private String pdbCode;
	
	private RunParametersDB runParameters;
	
	private List<ChainClusterDB> chainClusters;

	private List<InterfaceDB> interfaceItems;
	
	private List<AssemblyDB> assemblies;
	
	private JobDB job;
	
	public PdbInfoDB() {
		interfaceItems = new ArrayList<InterfaceDB>();
		chainClusters = new ArrayList<ChainClusterDB>();
		assemblies = new ArrayList<AssemblyDB>();
	}
	
	public PdbInfoDB(int uid,
						JobDB jobItem,
						String title,
						String spaceGroup,
						String expMethod,
						double resolution,
						double rfreeValue,
						RunParametersDB runParameters) {
		
		interfaceItems = new ArrayList<InterfaceDB>();
		chainClusters = new ArrayList<ChainClusterDB>();
		assemblies = new ArrayList<AssemblyDB>();
		this.uid = uid;
		this.title = title;
		this.spaceGroup = spaceGroup;
		this.expMethod = expMethod;
		this.resolution = resolution;
		this.runParameters = runParameters;
		this.job = jobItem;
		this.rfreeValue = rfreeValue;
	}
	
	public List<AssemblyDB> getAssemblies() {
		return assemblies;
	}

	public void setAssemblies(List<AssemblyDB> assemblies) {
		this.assemblies = assemblies;
	}
	
	public void addAssembly(AssemblyDB assembly) {
		this.assemblies.add(assembly);
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public void setInterfaceItems(List<InterfaceDB> interfaceItems) {
		this.interfaceItems = interfaceItems;
	}

	public List<InterfaceDB> getInterfaceItems() {
		return interfaceItems;
	}
	
	public void addInterfaceItem(InterfaceDB interfaceItem) {
		this.interfaceItems.add(interfaceItem);
	}
	
	public InterfaceDB getInterfaceItem(int i) {
		return this.interfaceItems.get(i);
	}

	public void setChainClusters(List<ChainClusterDB> chainClusters) {
		this.chainClusters = chainClusters;
	}
	
	public List<ChainClusterDB> getChainClusters() {
		return this.chainClusters;
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

	public String getPdbCode() {
		return pdbCode;
	}
	
	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
	}
	
	
	public void setRunParameters(RunParametersDB runParameters) {
		this.runParameters = runParameters;
	}

	public RunParametersDB getRunParameters() {
		return runParameters;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}

	public void setJob(JobDB job) {
		this.job = job;
	}

	public JobDB getJob() {
		return job;
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

	public String getExpMethod() {
		return expMethod;
	}

	public void setExpMethod(String expMethod) {
		this.expMethod = expMethod;
	}
	
	
}
