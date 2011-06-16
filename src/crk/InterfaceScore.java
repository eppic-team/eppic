package crk;

import java.io.Serializable;
import java.util.List;


public class InterfaceScore implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private PdbScore parent;
	
	private int id;
	private String operator;
	private double interfArea;
	private String firstChainId;
	private String secondChainId;
	private int numHomologs1;
	private int numHomologs2;
	
	private List<String> warnings;
	private String callReason;

	private int coreSize1;
	private int coreSize2;
	private double rim1Score;
	private double core1Score;
	private double ratio1Score;
	private double rim2Score;
	private double core2Score;
	private double ratio2Score;
	private double finalScore; 					// final score (average of ratios of both sides)
	private CallType call;
	
	public InterfaceScore()
	{
		
	}
	
	public InterfaceScore(PdbScore parent) {
		this.parent = parent;
	}

	public int getId() {
		return id;
	}
	
	public String getOperator() {
		return operator;
	}
	
	public void setOperator(String operator) {
		this.operator = operator;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getInterfArea() {
		return interfArea;
	}

	public void setInterfArea(double interfArea) {
		this.interfArea = interfArea;
	}

	public String getFirstChainId() {
		return firstChainId;
	}

	public void setFirstChainId(String firstChainId) {
		this.firstChainId = firstChainId;
	}

	public String getSecondChainId() {
		return secondChainId;
	}

	public void setSecondChainId(String secondChainId) {
		this.secondChainId = secondChainId;
	}

	public int getNumHomologs1() {
		return numHomologs1;
	}

	public void setNumHomologs1(int numHomologs1) {
		this.numHomologs1 = numHomologs1;
	}

	public int getNumHomologs2() {
		return numHomologs2;
	}

	public void setNumHomologs2(int numHomologs2) {
		this.numHomologs2 = numHomologs2;
	}

	public double getRim1Score() {
		return rim1Score;
	}

	public void setRim1Score(double rim1Score) {
		this.rim1Score = rim1Score;
	}

	public double getCore1Score() {
		return core1Score;
	}

	public void setCore1Score(double core1Score) {
		this.core1Score = core1Score;
	}

	public double getRim2Score() {
		return rim2Score;
	}

	public void setRim2Score(double rim2Score) {
		this.rim2Score = rim2Score;
	}

	public double getCore2Score() {
		return core2Score;
	}

	public void setCore2Score(double core2Score) {
		this.core2Score = core2Score;
	}

	public double getFinalScore() {
		return finalScore;
	}

	public void setFinalScore(double finalScore) {
		this.finalScore = finalScore;
	}

	public int getCoreSize1() {
		return coreSize1;
	}

	public void setCoreSize1(int coreSize1) {
		this.coreSize1 = coreSize1;
	}

	public int getCoreSize2() {
		return coreSize2;
	}

	public void setCoreSize2(int coreSize2) {
		this.coreSize2 = coreSize2;
	}

	public CallType getCall() {
		return call;
	}

	public void setCall(CallType call) {
		this.call = call;
	}
	
	public PdbScore getParent() {
		return parent;
	}
	
	public double getRatio1Score() {
		return ratio1Score;
	}

	public void setRatio1Score(double ratio1Score) {
		this.ratio1Score = ratio1Score;
	}

	public double getRatio2Score() {
		return ratio2Score;
	}

	public void setRatio2Score(double ratio2Score) {
		this.ratio2Score = ratio2Score;
	}

	public List<String> getWarnings() {
		return warnings;
	}

	public void setWarnings(List<String> warnings) {
		this.warnings = warnings;
	}
	
	public String getCallReason() {
		return this.callReason;
	}
	
	public void setCallReason(String callReason) {
		this.callReason = callReason;
	}
	
}
