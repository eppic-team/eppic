package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import eppic.model.InterfaceScoreDB;

/**
 * DTO class for InterfaceScore item.
 * @author AS
 */
public class InterfaceScoreItem implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String method;
	private int id;
	
	private double score1;
	private double score2;
	private double score; 
	
	private String callName;
	private String callReason;
	
	public InterfaceScoreItem()
	{
		
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

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}
	
	/**
	 * Converts DB model item into DTO one
	 * @param interfaceScoreItemDB model item to convert
	 * @return DTO representation of model item
	 */
	public static InterfaceScoreItem create(InterfaceScoreDB interfaceScoreItemDB)
	{
		InterfaceScoreItem interfaceScoreItem = new InterfaceScoreItem();
		interfaceScoreItem.setCallName(interfaceScoreItemDB.getCall());
		interfaceScoreItem.setCallReason(interfaceScoreItemDB.getCallReason());
		interfaceScoreItem.setId(interfaceScoreItemDB.getInterfaceId());
		interfaceScoreItem.setMethod(interfaceScoreItemDB.getMethod());
		interfaceScoreItem.setUid(interfaceScoreItemDB.getUid());
		interfaceScoreItem.setScore1(interfaceScoreItemDB.getScore1());
		interfaceScoreItem.setScore2(interfaceScoreItemDB.getScore2());
		interfaceScoreItem.setScore(interfaceScoreItemDB.getScore());
		return interfaceScoreItem;
	}

}
