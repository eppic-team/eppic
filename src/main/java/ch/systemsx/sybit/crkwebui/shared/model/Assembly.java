package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import eppic.model.AssemblyDB;
import eppic.model.InterfaceClusterDB;


public class Assembly implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int uid;

	private String method;
	private int mmSize;
	
	private double confidence;
	
	private List<InterfaceCluster> interfaceClusters;

	public Assembly() {
		this.interfaceClusters = new ArrayList<InterfaceCluster>();
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public int getMmSize() {
		return mmSize;
	}

	public void setMmSize(int mMSize) {
		this.mmSize = mMSize;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public List<InterfaceCluster> getInterfaceClusters() {
		return interfaceClusters;
	}

	public void setInterfaceClusters(List<InterfaceCluster> interfaceClusters) {
		this.interfaceClusters = interfaceClusters;
	}

	/**
	 * Converts DB model item into DTO one.
	 * @param assemblyDB model item to convert
	 * @return DTO representation of model item
	 */
	public static Assembly create(AssemblyDB assemblyDB) {
		Assembly assembly = new Assembly();
		
		assembly.setUid(assemblyDB.getUid());
		
		assembly.setMmSize(assemblyDB.getMmSize());
		assembly.setMethod(assemblyDB.getMethod());
		assembly.setConfidence(assemblyDB.getConfidence());
		
		if(assemblyDB.getInterfaceClusters() != null)
		{
			List<InterfaceClusterDB> clusterDBs = assemblyDB.getInterfaceClusters();
			
			List<InterfaceCluster> clusters = new ArrayList<InterfaceCluster>();
			
			for(InterfaceClusterDB clusterDB : clusterDBs)
			{
				clusters.add(InterfaceCluster.create(clusterDB));
			}
			
			assembly.setInterfaceClusters(clusters);
		}
		
		return assembly;
	}
	
	
}
