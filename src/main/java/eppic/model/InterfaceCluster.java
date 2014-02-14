package eppic.model;

import java.io.Serializable;

public class InterfaceCluster implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int uid;
	private int clusterId;
	
	private String pdbCode;
	
	private double confidence;

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
	

}
