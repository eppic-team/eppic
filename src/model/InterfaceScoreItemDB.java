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
//		setUnweightedCore1Scores(0.0);
//		setUnweightedCore2Scores(0.0);
//		setUnweightedRim1Scores(0.0);
//		setUnweightedRim2Scores(0.0);
//		setUnweightedRatio1Scores(0.0);
//		setUnweightedRatio2Scores(0.0);
//		setUnweightedFinalScores(0.0);
	}
	
	public InterfaceScoreItemDB(PDBScoreItemDB pdbScoreItem)
	{
//		this.pdbScoreItem = pdbScoreItem;
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
