package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceClusterScoreDB;
import eppic.model.InterfaceDB;

public class InterfaceCluster implements Serializable, Comparable<InterfaceCluster>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	private int clusterId;
	
	private String pdbCode;
	
	private List<Interface> interfaces;
	
	private List<InterfaceClusterScore> interfaceClusterScores;
	
	public InterfaceCluster()
	{
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
	public static InterfaceCluster create(InterfaceClusterDB clusterDB)
	{
		InterfaceCluster cluster = new InterfaceCluster();
		
		cluster.setUid(clusterDB.getUid());
		cluster.setClusterId(clusterDB.getClusterId());
		
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
