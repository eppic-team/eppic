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
	
	// unit cell parameters
	private double cellA;
	private double cellB;
	private double cellC;
	private double cellAlpha;
	private double cellBeta;
	private double cellGamma;
	
	private RunParametersDB runParameters;
	
	private List<ChainClusterDB> chainClusters;

	private List<InterfaceClusterDB> interfaceClusters;
	
	private List<AssemblyDB> assemblies;
	
	private JobDB job;
	
	public PdbInfoDB() {
		chainClusters = new ArrayList<ChainClusterDB>();
		interfaceClusters = new ArrayList<InterfaceClusterDB>();
		assemblies = new ArrayList<AssemblyDB>();
	}
	
	public PdbInfoDB(int uid,
						JobDB job,
						String pdbCode,
						String title,
						String spaceGroup,
						String expMethod,
						double resolution,
						double rfreeValue,
						RunParametersDB runParameters) {
		
		chainClusters = new ArrayList<ChainClusterDB>();
		assemblies = new ArrayList<AssemblyDB>();
		this.uid = uid;
		this.pdbCode = pdbCode;
		this.title = title;
		this.spaceGroup = spaceGroup;
		this.expMethod = expMethod;
		this.resolution = resolution;
		this.runParameters = runParameters;
		this.job = job;
		this.rfreeValue = rfreeValue;
	}
	
	/**
	 * Returns the InterfaceDB for the given interfaceId or null if no such interfaceId exists
	 * @param interfaceId
	 * @return
	 */
	public InterfaceDB getInterface (int interfaceId) {
		for (InterfaceClusterDB ic:interfaceClusters) {
			for (InterfaceDB ii:ic.getInterfaces()) {
				if (ii.getInterfaceId()==interfaceId) {
					return ii;
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the InterfaceClusterDB corresponding to the given clusterId or null if no such clusterId exists
	 * @param interfaceId
	 * @return
	 */
	public InterfaceClusterDB getInterfaceCluster(int clusterId) {
		for (InterfaceClusterDB ic:interfaceClusters) {
			if (ic.getClusterId()==clusterId) return ic;
		}
		return null;
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

	public void setChainClusters(List<ChainClusterDB> chainClusters) {
		this.chainClusters = chainClusters;
	}
	
	public List<ChainClusterDB> getChainClusters() {
		return this.chainClusters;
	}

	public List<InterfaceClusterDB> getInterfaceClusters() {
		return interfaceClusters;
	}

	public void setInterfaceClusters(List<InterfaceClusterDB> interfaceClusters) {
		this.interfaceClusters = interfaceClusters;
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

	public double getCellA() {
		return cellA;
	}

	public void setCellA(double cellA) {
		this.cellA = cellA;
	}

	public double getCellB() {
		return cellB;
	}

	public void setCellB(double cellB) {
		this.cellB = cellB;
	}

	public double getCellC() {
		return cellC;
	}

	public void setCellC(double cellC) {
		this.cellC = cellC;
	}

	public double getCellAlpha() {
		return cellAlpha;
	}

	public void setCellAlpha(double cellAlpha) {
		this.cellAlpha = cellAlpha;
	}

	public double getCellBeta() {
		return cellBeta;
	}

	public void setCellBeta(double cellBeta) {
		this.cellBeta = cellBeta;
	}

	public double getCellGamma() {
		return cellGamma;
	}

	public void setCellGamma(double cellGamma) {
		this.cellGamma = cellGamma;
	}
	
	
}
