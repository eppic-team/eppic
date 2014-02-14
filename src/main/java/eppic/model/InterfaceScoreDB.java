package eppic.model;

import java.io.Serializable;

public class InterfaceScoreDB implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String pdbCode;
	
	private String method;
	private int interfaceId;
	
	private double score1;
	
	private double score2;
	
	private double score; 
	
	private double confidence;
	
	private String call;
	private String callReason;
	
	private InterfaceDB interfaceItem;
	
	public InterfaceScoreDB() {
	}
	
	public InterfaceScoreDB(PdbInfoDB pdbScoreItem) {
	}
	
	public String getPdbCode() {
		return pdbCode;
	}

	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
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

	public void setMethod(String method) {
		this.method = method;
	}

	public String getMethod() {
		return method;
	}

	public void setInterfaceId(int interfaceId) 
	{
		this.interfaceId = interfaceId;
	}
	
	public int getInterfaceId()
	{
		return interfaceId;
	}

	public void setCall(String call) {
		this.call = call;
	}

	public String getCall() {
		return call;
	}
	
	public String getCallReason() {
		return callReason;
	}
	
	public void setCallReason(String callReason) {
		this.callReason = callReason;
	}

	public void setInterface(InterfaceDB interfaceItem) {
		this.interfaceItem = interfaceItem;
	}

	public InterfaceDB getInterface() {
		return interfaceItem;
	}
	
	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}

}
