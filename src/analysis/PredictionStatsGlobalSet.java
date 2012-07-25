package analysis;

import java.io.PrintStream;

public class PredictionStatsGlobalSet {

	private PredictionStatsSet bioPred;
	private PredictionStatsSet xtalPred;
	private int total;
	private int totalBio;
	private int totalXtal;
	private String name;
	private String callCutoffs;
	private String caCutoffs;
	
	public PredictionStatsGlobalSet(PredictionStatsSet bioPred, PredictionStatsSet xtalPred) {
		if (!bioPred.callCutoffs.equals(xtalPred.callCutoffs) ||
			!bioPred.caCutoffs.equals(xtalPred.caCutoffs) ||
			bioPred.scoType!=xtalPred.scoType) {
			throw new IllegalArgumentException("Different type of predictions combined in same global set!");
		}
		
		this.bioPred = bioPred;
		this.xtalPred = xtalPred;
		this.total = bioPred.total + xtalPred.total;
		this.totalBio = bioPred.total;
		this.totalXtal = xtalPred.total;
		this.name = bioPred.name+"-"+xtalPred.name;
		this.callCutoffs = bioPred.callCutoffs;
		this.caCutoffs = bioPred.caCutoffs;
	}
	
	public int getFailed() {
		return total-getCalculated();
	}
	
	public int getCalculated() {
		return getCountBioCalls() + getCountXtalCalls();
	}
	
	public int getCountBioCalls() {
		return bioPred.countBioCalls + xtalPred.countBioCalls;
	}
	
	public int getCountXtalCalls() {
		return bioPred.countXtalCalls + xtalPred.countXtalCalls;
	}
	
	public int getTP() {
		return bioPred.getTP();
	}
	
	public int getTN() {
		return xtalPred.getTP();
	}
	
	public int getFP() {
		return xtalPred.getFN();
	}

	public int getFN() {
		return bioPred.getFN(); 
	}
	
	
	public double getAccuracy() {
		return (double)(getTP()+getTN())/(double)total;
	}
	
	public double getSensitivity() {
		return (double)getTP()/(double)(getTP()+getFN()) ;
	}
	
	public double getSpecificity() {
		return (double)getTN()/(double)(getFP()+getTN()) ;
	}
	
	public double getFPR() {
		return (1.0 - getSpecificity());
	}
	
	public double getMCC() {
		return ((getTP()*getTN()-getFP()*getFN())/Math.sqrt(totalBio*totalXtal*(getTP()+getFP())*(getTN()+getFN())));
		
	}
	
	public void print(PrintStream ps) {
		ps.printf("%25s\t%12s\t%10s\t%4d\t%4d\t%4d\t%4d\t%4d\t%4d\t%4d\t%4.2f\t%4.2f\t%4.2f\t%4.2f\n",
				name, callCutoffs, caCutoffs, total, totalBio, totalXtal, getTP(), getFP(), getTN(), getFN(), getAccuracy(), getSensitivity(), getSpecificity(), getMCC());
	}
	
	public static void printHeader(PrintStream ps) {
		ps.printf("%25s\t%12s\t%10s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\n",
				"name","call-cutoffs","CA-cutoffs","tot","totb","totx","tp","fp","tn","fn","acc","sens","spec","mcc");
	}
}
