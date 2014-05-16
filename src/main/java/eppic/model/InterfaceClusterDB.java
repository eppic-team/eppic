package eppic.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InterfaceClusterDB implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int uid;
	private int clusterId;
	
	private String pdbCode;
	
	private double avgArea;
	
	private int numMembers;
	
	private int globalInterfClusterId;
	
	private List<InterfaceDB> interfaces;
	
	private List<InterfaceClusterScoreDB> interfaceClusterScores;
	
	private PdbInfoDB pdbInfo;
	
	private AssemblyDB assembly;
	
	public InterfaceClusterDB() {
		this.interfaceClusterScores = new ArrayList<InterfaceClusterScoreDB>();
	}

	/**
	 * Returns the first InterfaceClusterScoreDB corresponding to the given method or null
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

	public AssemblyDB getAssembly() {
		return assembly;
	}

	public void setAssembly(AssemblyDB assembly) {
		this.assembly = assembly;
	}
	
	public int size() {
		return this.interfaces.size();
	}
}
