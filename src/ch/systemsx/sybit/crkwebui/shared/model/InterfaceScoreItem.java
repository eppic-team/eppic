package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import model.InterfaceScoreItemDB;

public class InterfaceScoreItem implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String method;
	private int id;
	
	private double weightedRim1Scores;
	private double weightedCore1Scores;
	private double weightedRatio1Scores;
	private double weightedRim2Scores;
	private double weightedCore2Scores;
	private double weightedRatio2Scores;
	private double weightedFinalScores; 
	
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
	
	public InterfaceScoreItem(PDBScoreItem pdbScoreItem)
	{
//		this.pdbScoreItem = pdbScoreItem;
	}
	
	public double getWeightedRim1Scores() {
		return weightedRim1Scores;
	}
	public void setWeightedRim1Scores(double weightedRim1Scores) {
		this.weightedRim1Scores = weightedRim1Scores;
	}
	public double getWeightedCore1Scores() {
		return weightedCore1Scores;
	}
	public void setWeightedCore1Scores(double weightedCore1Scores) {
		this.weightedCore1Scores = weightedCore1Scores;
	}
	public double getWeightedRatio1Scores() {
		return weightedRatio1Scores;
	}
	public void setWeightedRatio1Scores(double weightedRatio1Scores) {
		this.weightedRatio1Scores = weightedRatio1Scores;
	}
	public double getWeightedRim2Scores() {
		return weightedRim2Scores;
	}
	public void setWeightedRim2Scores(double weightedRim2Scores) {
		this.weightedRim2Scores = weightedRim2Scores;
	}
	public double getWeightedCore2Scores() {
		return weightedCore2Scores;
	}
	public void setWeightedCore2Scores(double weightedCore2Scores) {
		this.weightedCore2Scores = weightedCore2Scores;
	}
	public double getWeightedRatio2Scores() {
		return weightedRatio2Scores;
	}
	public void setWeightedRatio2Scores(double weightedRatio2Scores) {
		this.weightedRatio2Scores = weightedRatio2Scores;
	}
	public double getWeightedFinalScores() {
		return weightedFinalScores;
	}
	public void setWeightedFinalScores(double weightedFinalScores) {
		this.weightedFinalScores = weightedFinalScores;
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
		interfaceScoreItem.setWeightedCore1Scores(interfaceScoreItemDB.getWeightedCore1Scores());
		interfaceScoreItem.setWeightedCore2Scores(interfaceScoreItemDB.getWeightedCore2Scores());
		interfaceScoreItem.setWeightedRim1Scores(interfaceScoreItemDB.getWeightedRim1Scores());
		interfaceScoreItem.setWeightedRim2Scores(interfaceScoreItemDB.getWeightedRim2Scores());
		interfaceScoreItem.setWeightedRatio1Scores(interfaceScoreItemDB.getWeightedRatio1Scores());
		interfaceScoreItem.setWeightedRatio2Scores(interfaceScoreItemDB.getWeightedRatio2Scores());
		interfaceScoreItem.setWeightedFinalScores(interfaceScoreItemDB.getWeightedFinalScores());
		return interfaceScoreItem;
	}

}
