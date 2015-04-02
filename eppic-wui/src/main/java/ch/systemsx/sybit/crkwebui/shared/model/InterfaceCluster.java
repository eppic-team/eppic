package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceClusterScoreDB;
import eppic.model.InterfaceDB;

@XmlAccessorType(XmlAccessType.FIELD)
public class InterfaceCluster implements Serializable, Comparable<InterfaceCluster>{

	private static final long serialVersionUID = 1L;
	
	private int uid;
	private int clusterId;
	
	private String pdbCode;
	private double avgArea;
	private double avgContactOverlapScore;
	

	private int numMembers;
	
	private int globalInterfClusterId;
	
	@XmlElementWrapper(name = "interfaces")
	@XmlElement(name = "interface")
	private List<Interface> interfaces;
	
	@XmlElementWrapper(name = "interfaceClusterScores")
	@XmlElement(name = "interfaceClusterScore")
	private List<InterfaceClusterScore> interfaceClusterScores;
	
	public InterfaceCluster() {
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

	public List<Interface> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<Interface> interfaces) {
		this.interfaces = interfaces;
	}

	public List<InterfaceClusterScore> getInterfaceClusterScores() {
		return interfaceClusterScores;
	}

	public void setInterfaceClusterScores(
			List<InterfaceClusterScore> interfaceClusterScores) {
		this.interfaceClusterScores = interfaceClusterScores;
	}
	
	/**
	 * Converts DB model item into DTO one.
	 * @param interfaceClusterDB model item to convert
	 * @return DTO representation of model item
	 */
	public static InterfaceCluster create(InterfaceClusterDB clusterDB) {
		
		InterfaceCluster cluster = new InterfaceCluster();
		
		cluster.setUid(clusterDB.getUid());
		cluster.setClusterId(clusterDB.getClusterId());
		
		cluster.setAvgArea(clusterDB.getAvgArea());
		cluster.setAvgContactOverlapScore(clusterDB.getAvgContactOverlapScore());
		cluster.setNumMembers(clusterDB.getNumMembers());
		cluster.setGlobalInterfClusterId(clusterDB.getGlobalInterfClusterId()); 
		
		if(clusterDB.getInterfaceClusterScores() != null)
		{
			List<InterfaceClusterScoreDB> clusterScoreDBs = clusterDB.getInterfaceClusterScores();
			
			List<InterfaceClusterScore> clusterScores = new ArrayList<InterfaceClusterScore>();
			
			for(InterfaceClusterScoreDB clusterScoreDB : clusterScoreDBs)
			{
				clusterScores.add(InterfaceClusterScore.create(clusterScoreDB));
			}
			
			cluster.setInterfaceClusterScores(clusterScores);
		}
		
		if(clusterDB.getInterfaces() != null)
		{
			List<InterfaceDB> interfaceDBs = clusterDB.getInterfaces();
			
			List<Interface> interfaces = new ArrayList<Interface>();
			
			for(InterfaceDB interfaceDB : interfaceDBs)
			{
				interfaces.add(Interface.create(interfaceDB));
			}
			
			cluster.setInterfaces(interfaces);
		}
		
		return cluster;
	}
	
	@Override
	public int compareTo(InterfaceCluster that) 
	{
		return this.clusterId - that.clusterId;
	}

}
