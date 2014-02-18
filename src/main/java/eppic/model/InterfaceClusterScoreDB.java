package eppic.model;

import java.io.Serializable;

public class InterfaceClusterScoreDB implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private int clusterId;
	
	private String call;
	
	private String callReason;
	
	private double score;	
	private double confidence;
	
	private String method;
	
	private InterfaceClusterDB interfaceCluster;

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

	public String getCall() {
		return call;
	}

	public void setCall(String call) {
		this.call = call;
	}

	public String getCallReason() {
		return callReason;
	}

	public void setCallReason(String callReason) {
		this.callReason = callReason;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public InterfaceClusterDB getInterfaceCluster() {
		return interfaceCluster;
	}

	public void setInterfaceCluster(InterfaceClusterDB interfaceCluster) {
		this.interfaceCluster = interfaceCluster;
	}

}
