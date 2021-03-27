package eppic.model.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import eppic.model.db.AssemblyDB;
import eppic.model.db.ChainClusterDB;
import eppic.model.db.InterfaceClusterDB;
import eppic.model.db.PdbInfoDB;

/**
 * DTO class for PDBInfo.
 * @author AS
 */

@XmlRootElement(name = "eppicAnalysis")
@XmlType(propOrder = { "pdbCode", "title", "releaseDate", "expMethod",
		"spaceGroup", "resolution", "rfreeValue", "numChainClusters",
		"cellA","cellB","cellC","cellAlpha","cellBeta","cellGamma",
		"crystalFormId", "ncsOpsPresent", "nonStandardSg", "nonStandardCoordFrameConvention", 
		"exhaustiveAssemblyEnumeration", "maxNumClashesAnyInterface",
		"chainClusters", "interfaceClusters",  "assemblies",
"runParameters"})
@XmlAccessorType(XmlAccessType.FIELD)
public class PdbInfo implements Serializable//, ProcessingData
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

	private int numChainClusters;

	private double cellA;
	private double cellB;
	private double cellC;
	private double cellAlpha;
	private double cellBeta;
	private double cellGamma;

	private int crystalFormId;
	
	private boolean ncsOpsPresent;
	
	private boolean nonStandardSg;
	private boolean nonStandardCoordFrameConvention;
	
	private boolean exhaustiveAssemblyEnumeration;
	
	private int maxNumClashesAnyInterface;

	private RunParameters runParameters;

	@XmlElementWrapper(name = "chainClusters")
	@XmlElement(name = "chainCluster")
	private List<ChainCluster> chainClusters;

	@XmlElementWrapper(name = "interfaceClusters")
	@XmlElement(name = "interfaceCluster")
	private List<InterfaceCluster> interfaceClusters;

	@XmlElementWrapper(name = "assemblies")
	@XmlElement(name = "assembly")
	private List<Assembly> assemblies;

	@XmlTransient
	private Map<Integer, Interface> interfaces;

	public PdbInfo() 
	{
		chainClusters = new ArrayList<ChainCluster>();
		interfaceClusters = new ArrayList<InterfaceCluster>();
		assemblies = new ArrayList<Assembly>();
	}

	public PdbInfo(int uid,
			String pdbCode,
			String title,
			String spaceGroup,
			String expMethod,
			double resolution,
			double rfreeValue,
			int numChainClusters,
			double cellA,
			double cellB,
			double cellC,
			double cellAlpha,
			double cellBeta,
			double cellGamma,
			int crystalFormId,
			boolean ncsOpsPresent,
			boolean nonStandardSg,
			boolean nonStandardCoordFrameConvention,
			boolean exhaustiveAssemblyEnumration,
			int maxNumClashesAnyInterface,
			RunParameters runParameters) 
	{
		this.interfaceClusters = new ArrayList<InterfaceCluster>();
		this.chainClusters = new ArrayList<ChainCluster>();
		this.assemblies = new ArrayList<Assembly>();
		this.uid = uid;
		this.pdbCode = pdbCode;
		this.title = title;
		this.spaceGroup = spaceGroup;
		this.expMethod = expMethod;
		this.resolution = resolution;
		this.rfreeValue = rfreeValue;
		this.numChainClusters = numChainClusters;
		this.cellA = cellA;
		this.cellB = cellB;
		this.cellC = cellC;
		this.cellAlpha = cellAlpha;
		this.cellBeta = cellBeta;
		this.cellGamma = cellGamma;
		this.crystalFormId = crystalFormId;
		this.ncsOpsPresent = ncsOpsPresent;
		this.nonStandardSg = nonStandardSg;
		this.nonStandardCoordFrameConvention = nonStandardCoordFrameConvention;
		this.exhaustiveAssemblyEnumeration = exhaustiveAssemblyEnumration;
		this.maxNumClashesAnyInterface = maxNumClashesAnyInterface;
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
		createInterfaceList(interfaceClusters);
	}

	private void createInterfaceList(List<InterfaceCluster> interfaceClusters) {
		interfaces = new HashMap<Integer, Interface>();
		if (interfaceClusters==null) return;
		for(InterfaceCluster cluster : interfaceClusters)
			for(Interface inf : cluster.getInterfaces())
				interfaces.put(inf.getInterfaceId(), inf);
	}

	public List<InterfaceCluster> getInterfaceClusters() {
		return interfaceClusters;
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

	/**
	 * Get the list of valid assemblies, i.e. those with id larger than 0.
	 * Assemblies with id=0 represent the full unit cell, whilst assemblies with
	 * id <0 represent topologically invalid assemblies (but annotated by PDB).
	 * @return a list of topologically valid assemblies
	 * @since 3.1.0
	 */
	public List<Assembly> getValidAssemblies() {
		List<Assembly> validAssemblies = new ArrayList<Assembly>();
		for (Assembly assembly : assemblies) {
			if (assembly.getId()>0) {
				validAssemblies.add(assembly);
			}
		}
		return validAssemblies;
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

	public int getNumChainClusters() {
		return numChainClusters;
	}

	public void setNumChainClusters(int numChainClusters) {
		this.numChainClusters = numChainClusters;
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
	 * Returns the truncated extension input name of the job
	 * This method should be used to read in files like thumbnails etc..
	 * @return
	 */
	public String getTruncatedInputName(){
		return truncateFileName(inputName);
	}

	/**
	 * Truncates the given fileName by removing anything after the last dot.
	 * If no dot present in fileName then nothing is truncated.
	 * @param fileName
	 * @return
	 */
	public static String truncateFileName(String fileName) {
		if( fileName == null) return null;
		
		String newName = fileName;
		int lastPeriodPos = fileName.lastIndexOf('.');
		if (lastPeriodPos >= 0)
		{
			newName = fileName.substring(0, lastPeriodPos);
		}
		return newName;

	}

	 /**
	  * return an interface with the id
	  * @param interfaceId
	  * @return the interface
	  */
	 public Interface getInterface(int interfaceId){

		 if(interfaces == null) return null;
		 return interfaces.get(interfaceId);	
	 }


	 /**
	  * Converts DB model item into DTO one.
	  * @param pdbInfoDB model item to convert
	  * @return DTO representation of model item
	  */
	 public static PdbInfo create(PdbInfoDB pdbInfoDB)
	 {
		 PdbInfo pdbInfo = new PdbInfo();

		 if(pdbInfoDB.getInterfaceClusters() != null)
		 {
			 List<InterfaceClusterDB> interfaceClusterDBs = pdbInfoDB.getInterfaceClusters();

			 List<InterfaceCluster> interfaceClusters = new ArrayList<InterfaceCluster>();

			 for(InterfaceClusterDB interfaceClusterDB : interfaceClusterDBs)
			 {
				 interfaceClusters.add(InterfaceCluster.create(interfaceClusterDB));
			 }

			 pdbInfo.setInterfaceClusters(interfaceClusters);
		 }

		 if(pdbInfoDB.getAssemblies() != null)
		 {
			 List<AssemblyDB> assemblyDBs = pdbInfoDB.getAssemblies();

			 List<Assembly> assemblies = new ArrayList<Assembly>();

			 for(AssemblyDB assemblyDB : assemblyDBs)
			 {
				 assemblies.add(Assembly.create(assemblyDB));
			 }

			 pdbInfo.setAssemblies(assemblies);
		 }

		 if(pdbInfoDB.getChainClusters() != null)
		 {
			 List<ChainClusterDB> chainClusters = pdbInfoDB.getChainClusters();

			 List<ChainCluster> clustersCopy = new ArrayList<ChainCluster>();

			 for(ChainClusterDB cluster : chainClusters)
			 {
				 clustersCopy.add(ChainCluster.create(cluster));
			 }

			 pdbInfo.setChainClusters(clustersCopy);
		 }

		 pdbInfo.setPdbCode(pdbInfoDB.getPdbCode());
		 pdbInfo.setReleaseDate(pdbInfoDB.getReleaseDate());
		 pdbInfo.setRunParameters(RunParameters.create(pdbInfoDB.getRunParameters()));
		 pdbInfo.setSpaceGroup(pdbInfoDB.getSpaceGroup());
		 pdbInfo.setExpMethod(pdbInfoDB.getExpMethod());
		 pdbInfo.setResolution(pdbInfoDB.getResolution());
		 pdbInfo.setRfreeValue(pdbInfoDB.getRfreeValue());
		 pdbInfo.setTitle(pdbInfoDB.getTitle());
		 pdbInfo.setNumChainClusters(pdbInfoDB.getNumChainClusters());
		 pdbInfo.setUid(pdbInfoDB.getUid());
		 pdbInfo.setCellA(pdbInfoDB.getCellA());
		 pdbInfo.setCellB(pdbInfoDB.getCellB());
		 pdbInfo.setCellC(pdbInfoDB.getCellC());
		 pdbInfo.setCellAlpha(pdbInfoDB.getCellAlpha());
		 pdbInfo.setCellBeta(pdbInfoDB.getCellBeta());
		 pdbInfo.setCellGamma(pdbInfoDB.getCellGamma());
		 pdbInfo.setCrystalFormId(pdbInfo.getCrystalFormId());
		 pdbInfo.setNcsOpsPresent(pdbInfoDB.isNcsOpsPresent());
		 pdbInfo.setNonStandardSg(pdbInfoDB.isNonStandardSg());
		 pdbInfo.setNonStandardCoordFrameConvention(pdbInfoDB.isNonStandardCoordFrameConvention());
		 pdbInfo.setExhaustiveAssemblyEnumeration(pdbInfoDB.isExhaustiveAssemblyEnumeration());
		 pdbInfo.setMaxNumClashesAnyInterface(pdbInfoDB.getMaxNumClashesAnyInterface());
		 
		 return pdbInfo;
	 }
	 
	 public Assembly getAssemblyById(int assemblyID){
		 for(Assembly a :assemblies){
			 if(assemblyID == a.getId())
				 return a;
		 }
		 return null;
	 }
}
