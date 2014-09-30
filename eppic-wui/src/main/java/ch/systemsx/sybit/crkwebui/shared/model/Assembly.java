package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import eppic.model.AssemblyDB;
import eppic.model.InterfaceClusterScoreDB;


public class Assembly implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int uid;

	private String method;
	private int mmSize;
	
	private double confidence;
	
	private List<InterfaceClusterScore> interfaceClusterScores;

	public Assembly() {
		this.interfaceClusterScores = new ArrayList<InterfaceClusterScore>();
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

	public List<InterfaceClusterScore> getInterfaceClusterScores() {
		return interfaceClusterScores;
	}

	public void setInterfaceClusterScores(List<InterfaceClusterScore> interfaceClusterScores) {
		this.interfaceClusterScores = interfaceClusterScores;
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
		
		if(assemblyDB.getInterfaceClusterScores() != null)
		{
			List<InterfaceClusterScoreDB> interfClusterScoreDBs = assemblyDB.getInterfaceClusterScores();
			
			List<InterfaceClusterScore> clusterScores = new ArrayList<InterfaceClusterScore>();
			
			for(InterfaceClusterScoreDB interfClusterScoreDB : interfClusterScoreDBs)
			{
				clusterScores.add(InterfaceClusterScore.create(interfClusterScoreDB));
			}
			
			assembly.setInterfaceClusterScores(clusterScores);
		}
		
		return assembly;
	}
	
	
}
