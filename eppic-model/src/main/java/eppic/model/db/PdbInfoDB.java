package eppic.model.db;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "PdbInfo")
public class PdbInfoDB implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int uid;

	@Column(length = 10000)
	private String title;
	private Date releaseDate;
	@Column(length = 10)
	private String spaceGroup;
	private double resolution;
	private double rfreeValue;
	@Column(length = 255)
	private String expMethod;

	@Column(length = 4)
	private String pdbCode;
	
	// the stoichiometry of the pdb structure
	private int numChainClusters;
	
	// whether ncs ops are present in this entry
	private boolean ncsOpsPresent;
	
	// unit cell parameters
	private double cellA;
	private double cellB;
	private double cellC;
	private double cellAlpha;
	private double cellBeta;
	private double cellGamma;
	
	private int crystalFormId;
	
	/**
	 * Whether this structure is a PDB structure in a non-standard space group 
	 */
	private boolean nonStandardSg;
	
	/**
	 * Whether this structure is a PDB structure in a 
	 * non-standard frame (for which our scale matrix calculation and thus the crystal reconstruction
	 * will be incorrect).
	 * There's ~ 200 old structures in the PDB affected by the non-standard frame problem, hopefully they will
	 * be remediated in the future.
	 * For more info see:
	 * https://github.com/eppic-team/eppic/issues/37 and
	 * https://github.com/eppic-team/owl/issues/4
	 */
	private boolean nonStandardCoordFrameConvention;
	
	/**
	 * Whether the assemblies were exhaustively enumerated by finding ALL 
	 * valid ones (true) or by reducing the search space heuristically via 
	 * contraction of highest scoring heteromeric edges.
	 */
	private boolean exhaustiveAssemblyEnumeration;
	
	/**
	 * The maximum number of clashes in any interface. Used for warnings in UI.
	 */
	private int maxNumClashesAnyInterface;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private RunParametersDB runParameters;

	@OneToMany(mappedBy = "pdbInfo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ChainClusterDB> chainClusters;

	@OneToMany(mappedBy = "pdbInfo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<InterfaceClusterDB> interfaceClusters;

	@OneToMany(mappedBy = "pdbInfo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<AssemblyDB> assemblies;

	@OneToOne(fetch = FetchType.LAZY)
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
						double cellA,
						double cellB,
						double cellC,
						double cellAlpha,
						double cellBeta,
						double cellGamma,
						int crystalFormId,
						RunParametersDB runParameters,						
						boolean nonStandardSg,
						boolean nonStandardCoordFrameConvention,
						boolean exhaustiveAssemblyEnumeration) {
		
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
		this.cellA = cellA;
		this.cellB = cellB;
		this.cellC = cellC;
		this.cellAlpha = cellAlpha;
		this.cellBeta = cellBeta;
		this.cellGamma = cellGamma;
		this.crystalFormId = crystalFormId;
		this.nonStandardSg = nonStandardSg;
		this.nonStandardCoordFrameConvention = nonStandardCoordFrameConvention;
		this.exhaustiveAssemblyEnumeration = exhaustiveAssemblyEnumeration;
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
	 * @param clusterId
	 * @return
	 */
	public InterfaceClusterDB getInterfaceCluster(int clusterId) {
		for (InterfaceClusterDB ic:interfaceClusters) {
			if (ic.getClusterId()==clusterId) return ic;
		}
		return null;
	}
	
	/**
	 * Returns the corresponding ChainClusterDB given the representative chain id
	 * @param repChainId
	 * @return
	 */
	public ChainClusterDB getChainCluster(String repChainId) {
		for (ChainClusterDB cc:chainClusters) {
			if (cc.getRepChain().equals(repChainId)) {
				return cc;
			}
		}
		return null;
	}
	
	public List<AssemblyDB> getAssemblies() {
		return assemblies;
	}

	/**
	 * Get the list of valid assemblies, i.e. those with id larger than 0.
	 * Assemblies with id=0 represent the full unit cell, whilst assemblies with
	 * id <0 represent topologically invalid assemblies (but annotated by PDB).
	 * @return a list of topologically valid assemblies
	 * @since 3.1.0
	 */
	@Transient
	public List<AssemblyDB> getValidAssemblies() {
		List<AssemblyDB> validAssemblies = new ArrayList<AssemblyDB>();
		for (AssemblyDB assemblyDB : assemblies) {
			if (assemblyDB.getId()>0) {
				validAssemblies.add(assemblyDB);
			}
		}
		return validAssemblies;
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

	public int getNumChainClusters() {
		return numChainClusters;
	}
	
	public void setNumChainClusters(int numChainClusters) {
		this.numChainClusters = numChainClusters;
	}
	
	public boolean isNcsOpsPresent() {
		return ncsOpsPresent;
	}

	public void setNcsOpsPresent(boolean ncsOpsPresent) {
		this.ncsOpsPresent = ncsOpsPresent;
	}

	public boolean isNonStandardSg() {
		return nonStandardSg;
	}
	
	public void setNonStandardSg(boolean nonStandardSg) {
		this.nonStandardSg = nonStandardSg;
	}

	public boolean isNonStandardCoordFrameConvention() {
		return nonStandardCoordFrameConvention;
	}
	
	public void setNonStandardCoordFrameConvention(boolean nonStandardCoordFrameConvention) {
		this.nonStandardCoordFrameConvention = nonStandardCoordFrameConvention;
	}

	public boolean isExhaustiveAssemblyEnumeration() {
		return exhaustiveAssemblyEnumeration;
	}

	public void setExhaustiveAssemblyEnumeration(boolean exhaustiveAssemblyEnumeration) {
		this.exhaustiveAssemblyEnumeration = exhaustiveAssemblyEnumeration;
	}

	/**
	 * @return the maxNumClashesAnyInterface
	 */
	public int getMaxNumClashesAnyInterface() {
		return maxNumClashesAnyInterface;
	}

	/**
	 * @param maxNumClashesAnyInterface the maxNumClashesAnyInterface to set
	 */
	public void setMaxNumClashesAnyInterface(int maxNumClashesAnyInterface) {
		this.maxNumClashesAnyInterface = maxNumClashesAnyInterface;
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

	public int getCrystalFormId() {
		return crystalFormId;
	}

	public void setCrystalFormId(int crystalFormId) {
		this.crystalFormId = crystalFormId;
	}

	public AssemblyDB getAssemblyById(int assemblyID){
		for(AssemblyDB a :assemblies){
			if(assemblyID == a.getId())
				return a;
		}
		return null;
	}
}
