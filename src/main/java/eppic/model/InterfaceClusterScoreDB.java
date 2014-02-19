package eppic.model;

import java.io.Serializable;

public class InterfaceClusterScoreDB implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String pdbCode;
	private int clusterId;
	
	private String callName;
	
	private String callReason;
	
	private double score;	
	private double confidence;
	
	private double score1;
	private double score2;
	
	private String method;
	
	private InterfaceClusterDB interfaceCluster;

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getPdbCode() {
		return pdbCode;
	}

	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
	}

	public int getClusterId() {
		return clusterId;
	}

	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}

	public String getCallName() {
		return callName;
	}

	public void setCallName(String callName) {
		this.callName = callName;
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

	public double getScore1() {
		return score1;
	}

	public void setScore1(double score1) {
		this.score1 = score1;
	}

	public double getScore2() {
		return score2;
	}

	public void setScore2(double score2) {
		this.score2 = score2;
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
