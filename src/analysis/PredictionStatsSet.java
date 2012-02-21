package analysis;

import java.io.PrintStream;

import crk.CallType;
import crk.ScoringType;

public class PredictionStatsSet {

	public String name;   // e.g. plp, duarte_capitani, ...
	public CallType type; // the type of set: bio or xtal
	public ScoringType scoType; // geometry, entropy rim-core, entropy z-score 
	
	//public double caCutoff;
	//public int minNumberCoreResForBio;
	//public double bioCallCutoff;
	public String callCutoffs;
	public String caCutoffs;
	
	public int countBioCalls;
	public int countXtalCalls;
	public int total; // total entries in set, including failed ones
	
	public PredictionStatsSet(String name, CallType type, ScoringType scoType, String caCutoffs, String callCutoffs,
			int countBioCalls, int countXtalCalls, int total) {
		this.name = name;
		this.type = type;
		this.scoType = scoType;
		//this.caCutoff = caCutoff;
		//this.minNumberCoreResForBio = minNumberCoreResForBio;
		//this.bioCallCutoff = bioCallCutoff;
		this.callCutoffs = callCutoffs;
		this.caCutoffs = caCutoffs;
		this.countBioCalls = countBioCalls;
		this.countXtalCalls = countXtalCalls;
		this.total = total;
	}

	public int getFailed() {
		return total-getCalculated();
	}
	
	public int getCalculated() {
		return countBioCalls+countXtalCalls;
	}
	
	public int getTP() {
		if (type==CallType.BIO) {
			return countBioCalls;
		}
		if (type==CallType.CRYSTAL) {
			return countXtalCalls;
		}
		return -1;
	}
	
	public int getFN() {
		if (type==CallType.BIO) {
			return countXtalCalls;
		}
		if (type==CallType.CRYSTAL) {
			return countBioCalls;
		}		
		return -1;
	}
	
	public double getAccuracy() {
		return (double)getTP()/(double)getCalculated();
	}
	
	public double getRecall() {
		return (double)getTP()/(double)total;
	}
	
	public void print(PrintStream ps) {
		ps.printf("%25s\t%4s\t%16s\t%12s\t%10s\t%4d\t%4d\t%4d\t%4d\t%4.2f\t%4.2f\n",
				name, type.getName(), scoType.getName(), callCutoffs, caCutoffs, total, getTP(),getFN(),getFailed(),getAccuracy(),getRecall());
	}
	
	public static void printHeader(PrintStream ps) {
		ps.printf("%25s\t%4s\t%16s\t%12s\t%10s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\n",
				"name","type","scoType","call-cutoffs","CA-cutoffs","tot","tp","fn","fail","acc","rec");
	}
	
}
