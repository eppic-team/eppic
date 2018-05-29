package eppic.model.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InterfaceClusterDB implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int uid;
	private int clusterId;
	
	private String pdbCode;
	
	private double avgArea;
	private double avgContactOverlapScore;
	
	private boolean infinite;
	private boolean isologous;

	private int numMembers;
	
	private int globalInterfClusterId;
	
	private Set<AssemblyDB> assemblies;
	
	private List<InterfaceDB> interfaces;
	
	private List<InterfaceClusterScoreDB> interfaceClusterScores;
	
	private PdbInfoDB pdbInfo;
	
	public InterfaceClusterDB() {
		this.interfaceClusterScores = new ArrayList<InterfaceClusterScoreDB>();
		this.assemblies = new HashSet<AssemblyDB>();
		this.interfaces = new ArrayList<InterfaceDB>();
	}

	/**
	 * Returns the InterfaceClusterScoreDB corresponding to the given method or null
	 * if no InterfaceClusterScoreDB exists for the method
	 * @param method
	 * @return
	 */
	public InterfaceClusterScoreDB getInterfaceClusterScore(String method) {
		
		for (InterfaceClusterScoreDB ics:interfaceClusterScores) {
			if (ics.getMethod().equals(method)) return ics;				
		}
		return null;
	}
	
	public void addAssembly(AssemblyDB assembly) {
		assemblies.add(assembly);
	}
	
	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getClusterId() {
		return clusterId;
	}

	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}

	public String getPdbCode() {
		return pdbCode;
	}

	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
	}

	public double getAvgArea() {
		return avgArea;
	}

	public void setAvgArea(double avgArea) {
		this.avgArea = avgArea;
	}
	
	public double getAvgContactOverlapScore() {
		return avgContactOverlapScore;
	}

	public void setAvgContactOverlapScore(double avgContactOverlapScore) {
		this.avgContactOverlapScore = avgContactOverlapScore;
	}

	public boolean isInfinite() {
		return infinite;
	}

	public void setInfinite(boolean infinite) {
		this.infinite = infinite;
	}

	public boolean isIsologous() {
		return isologous;
	}

	public void setIsologous(boolean isologous) {
		this.isologous = isologous;
	}

	public int getNumMembers() {
		return numMembers;
	}

	public void setNumMembers(int numMembers) {
		this.numMembers = numMembers;
	}

	public int getGlobalInterfClusterId() {
		return globalInterfClusterId;
	}

	public void setGlobalInterfClusterId(int globalInterfClusterId) {
		this.globalInterfClusterId = globalInterfClusterId;
	}

	public Set<AssemblyDB> getAssemblies() {
		return assemblies;
	}

	public void setAssemblies(Set<AssemblyDB> assemblies) {
		this.assemblies = assemblies;
	}

	public List<InterfaceDB> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<InterfaceDB> interfaces) {
		this.interfaces = interfaces;
	}

	public List<InterfaceClusterScoreDB> getInterfaceClusterScores() {
		return interfaceClusterScores;
	}

	public void setInterfaceClusterScores(List<InterfaceClusterScoreDB> interfaceClusterScores) {
		this.interfaceClusterScores = interfaceClusterScores;
	}
	
	public void addInterfaceClusterScore(InterfaceClusterScoreDB interfaceClusterScore) {
		this.interfaceClusterScores.add(interfaceClusterScore);
	}

	public PdbInfoDB getPdbInfo() {
		return pdbInfo;
	}

	public void setPdbInfo(PdbInfoDB pdbInfo) {
		this.pdbInfo = pdbInfo;
	}

	public int size() {
		return this.interfaces.size();
	}
}
