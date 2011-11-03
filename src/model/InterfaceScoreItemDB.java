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
	
	private Double weightedRim1Scores;
	private Double weightedCore1Scores;
	private Double weightedRatio1Scores;
	private Double weightedRim2Scores;
	private Double weightedCore2Scores;
	private Double weightedRatio2Scores;
	private Double weightedFinalScores; 
	
	private Double unweightedRim1Scores;
	private Double unweightedCore1Scores;
	private Double unweightedRatio1Scores;
	private Double unweightedRim2Scores;
	private Double unweightedCore2Scores;
	private Double unweightedRatio2Scores;
	private Double unweightedFinalScores; 
	
	// we only have one call here: the one from the unweighted scores
	private String callName;
	private String callReason;
	
	private InterfaceItemDB interfaceItem;
	
	public InterfaceScoreItemDB()
	{
		setUnweightedCore1Scores(0.0);
		setUnweightedCore2Scores(0.0);
		setUnweightedRim1Scores(0.0);
		setUnweightedRim2Scores(0.0);
		setUnweightedRatio1Scores(0.0);
		setUnweightedRatio2Scores(0.0);
		setUnweightedFinalScores(0.0);
		setWeightedCore1Scores(0.0);
		setWeightedCore2Scores(0.0);
		setWeightedRim1Scores(0.0);
		setWeightedRim2Scores(0.0);
		setWeightedRatio1Scores(0.0);
		setWeightedRatio2Scores(0.0);
		setWeightedFinalScores(0.0);
	}
	
	public InterfaceScoreItemDB(PDBScoreItemDB pdbScoreItem)
	{
//		this.pdbScoreItem = pdbScoreItem;
	}
	
	public Double getWeightedRim1Scores() {
		return weightedRim1Scores;
	}
	public void setWeightedRim1Scores(Double weightedRim1Scores) {
		this.weightedRim1Scores = weightedRim1Scores;
	}
	public Double getWeightedCore1Scores() {
		return weightedCore1Scores;
	}
	public void setWeightedCore1Scores(Double weightedCore1Scores) {
		this.weightedCore1Scores = weightedCore1Scores;
	}
	public Double getWeightedRatio1Scores() {
		return weightedRatio1Scores;
	}
	public void setWeightedRatio1Scores(Double weightedRatio1Scores) {
		this.weightedRatio1Scores = weightedRatio1Scores;
	}
	public Double getWeightedRim2Scores() {
		return weightedRim2Scores;
	}
	public void setWeightedRim2Scores(Double weightedRim2Scores) {
		this.weightedRim2Scores = weightedRim2Scores;
	}
	public Double getWeightedCore2Scores() {
		return weightedCore2Scores;
	}
	public void setWeightedCore2Scores(Double weightedCore2Scores) {
		this.weightedCore2Scores = weightedCore2Scores;
	}
	public Double getWeightedRatio2Scores() {
		return weightedRatio2Scores;
	}
	public void setWeightedRatio2Scores(Double weightedRatio2Scores) {
		this.weightedRatio2Scores = weightedRatio2Scores;
	}
	public Double getWeightedFinalScores() {
		return weightedFinalScores;
	}
	public void setWeightedFinalScores(Double weightedFinalScores) {
		this.weightedFinalScores = weightedFinalScores;
	}
	public Double getUnweightedRim1Scores() {
		return unweightedRim1Scores;
	}
	public void setUnweightedRim1Scores(Double unweightedRim1Scores) {
		this.unweightedRim1Scores = unweightedRim1Scores;
	}
	public Double getUnweightedCore1Scores() {
		return unweightedCore1Scores;
	}
	public void setUnweightedCore1Scores(Double unweightedCore1Scores) {
		this.unweightedCore1Scores = unweightedCore1Scores;
	}
	public Double getUnweightedRatio1Scores() {
		return unweightedRatio1Scores;
	}
	public void setUnweightedRatio1Scores(Double unweightedRatio1Scores) {
		this.unweightedRatio1Scores = unweightedRatio1Scores;
	}
	public Double getUnweightedRim2Scores() {
		return unweightedRim2Scores;
	}
	public void setUnweightedRim2Scores(Double unweightedRim2Scores) {
		this.unweightedRim2Scores = unweightedRim2Scores;
	}
	public Double getUnweightedCore2Scores() {
		return unweightedCore2Scores;
	}
	public void setUnweightedCore2Scores(Double unweightedCore2Scores) {
		this.unweightedCore2Scores = unweightedCore2Scores;
	}
	public Double getUnweightedRatio2Scores() {
		return unweightedRatio2Scores;
	}
	public void setUnweightedRatio2Scores(Double unweightedRatio2Scores) {
		this.unweightedRatio2Scores = unweightedRatio2Scores;
	}
	public Double getUnweightedFinalScores() {
		return unweightedFinalScores;
	}
	public void setUnweightedFinalScores(Double unweightedFinalScores) {
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
