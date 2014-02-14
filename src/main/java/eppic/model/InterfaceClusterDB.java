package eppic.model;

import java.io.Serializable;
import java.util.List;

public class InterfaceClusterDB implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int uid;
	private int clusterId;
	
	private String pdbCode;
	
	private double confidence;
	
	private List<InterfaceDB> interfaceItems;
	
	private List<InterfaceClusterScoreDB> interfaceClusterScores;

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

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public List<InterfaceDB> getInterfaceItems() {
		return interfaceItems;
	}

	public void setInterfaceItems(List<InterfaceDB> interfaceItems) {
		this.interfaceItems = interfaceItems;
	}

	public List<InterfaceClusterScoreDB> getInterfaceClusterScores() {
		return interfaceClusterScores;
	}

	public void setInterfaceClusterScores(
			List<InterfaceClusterScoreDB> interfaceClusterScores) {
		this.interfaceClusterScores = interfaceClusterScores;
	}
	

}
