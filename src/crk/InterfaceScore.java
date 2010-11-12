package crk;

import java.util.Arrays;


public class InterfaceScore {

	private PdbScore parent;
	
	private int id;
	private double interfArea;
	private String firstChainId;
	private String secondChainId;
	private int numHomologs1;
	private int numHomologs2;

	private int[] coreSize1;
	private int[] coreSize2;
	private double[] rim1Scores;
	private double[] core1Scores;
	private double[] rim2Scores;
	private double[] core2Scores;
	private double[] finalScores; 					// final scores (average of ratios of both sides), one per bsaToAsaCutoff
	private CallType[] calls;
	
	
	public InterfaceScore(PdbScore parent) {
		this.parent = parent;
	}

	public int getId() {
		return id;
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

	public double[] getRim1Scores() {
		return rim1Scores;
	}

	public void setRim1Scores(double[] rim1Scores) {
		this.rim1Scores = rim1Scores;
	}

	public double[] getCore1Scores() {
		return core1Scores;
	}

	public void setCore1Scores(double[] core1Scores) {
		this.core1Scores = core1Scores;
	}

	public double[] getRim2Scores() {
		return rim2Scores;
	}

	public void setRim2Scores(double[] rim2Scores) {
		this.rim2Scores = rim2Scores;
	}

	public double[] getCore2Scores() {
		return core2Scores;
	}

	public void setCore2Scores(double[] core2Scores) {
		this.core2Scores = core2Scores;
	}

	public double[] getFinalScores() {
		return finalScores;
	}

	public void setFinalScores(double[] finalScores) {
		this.finalScores = finalScores;
	}

	public int[] getCoreSize1() {
		return coreSize1;
	}

	public void setCoreSize1(int[] coreSize1) {
		this.coreSize1 = coreSize1;
	}

	public int[] getCoreSize2() {
		return coreSize2;
	}

	public void setCoreSize2(int[] coreSize2) {
		this.coreSize2 = coreSize2;
	}

	public CallType[] getCalls() {
		return calls;
	}

	public void setCalls(CallType[] calls) {
		this.calls = calls;
	}
	
	public int getNumBsaToAsaCutoffs() {
		return coreSize1.length;
	}
	
	public PdbScore getParent() {
		return parent;
	}
	
	/**
	 * Gets the prediction call for this interface by using the interface zooming protocol
	 * with the calculated data, i.e. bsaToAsa soft cutoff will be the highest of the calculated
	 * cutoffs and hard the given minIndex. The bsaToAsa cutoffs are asummed to be ordered ascending.
	 * @return
	 * @param minIndex the minimum index of the bsaToAsa cutoffs array to be taken as the hard
	 * cutoff
	 * @throws IllegalArgumentException if the bsaToAsa cutoffs are not in ascending order
	 */
	public CallType getZoomingCall(int minIndex) {
		
		double[] bsaToAsaCutoffs = parent.getBsaToAsaCutoffs();
		double[] toSort = Arrays.copyOf(bsaToAsaCutoffs, bsaToAsaCutoffs.length);
		Arrays.sort(toSort);
		if (toSort[0]!=bsaToAsaCutoffs[0]) {
			throw new IllegalArgumentException("Core assignment cutoffs are not in ascending order.");
		}

		int i = 0;
		for (i=bsaToAsaCutoffs.length-1;i>=minIndex;i--) {
			if ((coreSize1[i]+coreSize2[i])>=parent.getMinCoreSize()) {
				break;
			}
		}
		if (i==minIndex-1) {
			// core it's never above minimum required core size, we return the minIndex cut-off (hard-cutoff) call
			return calls[minIndex];
		} else {
			return calls[i];
		}

	}
	
}
