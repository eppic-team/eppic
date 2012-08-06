package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import model.InterfaceScoreItemDB;

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
	
	private double unweightedRim1Scores;
	private double unweightedCore1Scores;
	private double unweightedRatio1Scores;
	private double unweightedRim2Scores;
	private double unweightedCore2Scores;
	private double unweightedRatio2Scores;
	private double unweightedFinalScores; 
	
	private String callName;
	private String callReason;
	
	public InterfaceScoreItem()
	{
		
	}
	
	public double getUnweightedRim1Scores() {
		return unweightedRim1Scores;
	}
	public void setUnweightedRim1Scores(double unweightedRim1Scores) {
		this.unweightedRim1Scores = unweightedRim1Scores;
	}
	public double getUnweightedCore1Scores() {
		return unweightedCore1Scores;
	}
	public void setUnweightedCore1Scores(double unweightedCore1Scores) {
		this.unweightedCore1Scores = unweightedCore1Scores;
	}
	public double getUnweightedRatio1Scores() {
		return unweightedRatio1Scores;
	}
	public void setUnweightedRatio1Scores(double unweightedRatio1Scores) {
		this.unweightedRatio1Scores = unweightedRatio1Scores;
	}
	public double getUnweightedRim2Scores() {
		return unweightedRim2Scores;
	}
	public void setUnweightedRim2Scores(double unweightedRim2Scores) {
		this.unweightedRim2Scores = unweightedRim2Scores;
	}
	public double getUnweightedCore2Scores() {
		return unweightedCore2Scores;
	}
	public void setUnweightedCore2Scores(double unweightedCore2Scores) {
		this.unweightedCore2Scores = unweightedCore2Scores;
	}
	public double getUnweightedRatio2Scores() {
		return unweightedRatio2Scores;
	}
	public void setUnweightedRatio2Scores(double unweightedRatio2Scores) {
		this.unweightedRatio2Scores = unweightedRatio2Scores;
	}
	public double getUnweightedFinalScores() {
		return unweightedFinalScores;
	}
	public void setUnweightedFinalScores(double unweightedFinalScores) {
		this.unweightedFinalScores = unweightedFinalScores;
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
	public static InterfaceScoreItem create(InterfaceScoreItemDB interfaceScoreItemDB)
	{
		InterfaceScoreItem interfaceScoreItem = new InterfaceScoreItem();
		interfaceScoreItem.setCallName(interfaceScoreItemDB.getCallName());
		interfaceScoreItem.setCallReason(interfaceScoreItemDB.getCallReason());
		interfaceScoreItem.setId(interfaceScoreItemDB.getId());
		interfaceScoreItem.setMethod(interfaceScoreItemDB.getMethod());
		interfaceScoreItem.setUid(interfaceScoreItemDB.getUid());
		interfaceScoreItem.setUnweightedCore1Scores(interfaceScoreItemDB.getUnweightedCore1Scores());
		interfaceScoreItem.setUnweightedCore2Scores(interfaceScoreItemDB.getUnweightedCore2Scores());
		interfaceScoreItem.setUnweightedRim1Scores(interfaceScoreItemDB.getUnweightedRim1Scores());
		interfaceScoreItem.setUnweightedRim2Scores(interfaceScoreItemDB.getUnweightedRim2Scores());
		interfaceScoreItem.setUnweightedRatio1Scores(interfaceScoreItemDB.getUnweightedRatio1Scores());
		interfaceScoreItem.setUnweightedRatio2Scores(interfaceScoreItemDB.getUnweightedRatio2Scores());
		interfaceScoreItem.setUnweightedFinalScores(interfaceScoreItemDB.getUnweightedFinalScores());
		return interfaceScoreItem;
	}

}
