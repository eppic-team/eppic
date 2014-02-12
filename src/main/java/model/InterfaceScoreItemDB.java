package model;

import java.io.Serializable;

public class InterfaceScoreItemDB implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String method;
	private int id;
	
	private Double score1;
	
	private Double score2;
	
	private Double score; 
	
	private String callName;
	private String callReason;
	
	private InterfaceItemDB interfaceItem;
	
	public InterfaceScoreItemDB() {
	}
	
	public InterfaceScoreItemDB(PDBScoreItemDB pdbScoreItem) {
	}
	
	public Double getScore1() {
		return score1;
	}
	
	public void setScore1(Double score1) {
		this.score1 = score1;
	}
	
	public Double getScore2() {
		return score2;
	}
	
	public void setScore2(Double score2) {
		this.score2 = score2;
	}
	
	public Double getScore() {
		return score;
	}
	
	public void setScore(Double score) {
		this.score = score;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getMethod() {
		return method;
	}

	public void setId(int id) 
	{
		this.id = id;
	}
	
	public int getId()
	{
		return id;
	}

	public void setCallName(String callName) {
		this.callName = callName;
	}

	public String getCallName() {
		return callName;
	}
	
	public String getCallReason() {
		return callReason;
	}
	
	public void setCallReason(String callReason) {
		this.callReason = callReason;
	}

	public void setInterfaceItem(InterfaceItemDB interfaceItem) {
		this.interfaceItem = interfaceItem;
	}

	public InterfaceItemDB getInterfaceItem() {
		return interfaceItem;
	}
	
	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}

}
