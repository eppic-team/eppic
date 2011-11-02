package model;

import java.io.Serializable;

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
	
	// we only have one call here: the one from the unweighted scores
	private String call;
	private String callReason;
	
	private InterfaceItem interfaceItem;
	
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

	public void setInterfaceItem(InterfaceItem interfaceItem) {
		this.interfaceItem = interfaceItem;
	}

	public InterfaceItem getInterfaceItem() {
		return interfaceItem;
	}
	
	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}

}
