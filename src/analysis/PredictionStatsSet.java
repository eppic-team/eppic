package analysis;

import java.io.PrintStream;

import crk.CallType;
import crk.ScoringType;

public class PredictionStatsSet {

	public String name;   // e.g. plp, duarte_capitani, ...
	public CallType type; // the type of set: bio or xtal
	public ScoringType scoType; // geometry, entropy rim-core, entropy z-score 
	
	public String callCutoffs;
	public String caCutoffs;
	
	public int countBioCalls;
	public int countXtalCalls;
	
	private PredCounter counter;
	
	public int total; // total entries in set, including failed ones
	
	public PredictionStatsSet(String name, CallType type, ScoringType scoType, String caCutoffs, String callCutoffs,
			PredCounter counter) {
		
		this.name = name;
		this.type = type;
		this.scoType = scoType;
		
		this.callCutoffs = callCutoffs;
		this.caCutoffs = caCutoffs;
		
		this.counter = counter;
		
		this.countBioCalls = counter.getTotalCountBio();
		this.countXtalCalls = counter.getTotalCountXtal();
		
		this.total = counter.getTotal();
	}

	public int getFailed() {
		return total-getCalculated();
	}
	
	public int getFailed(String taxon) {
		return counter.getSubTotal(taxon)-getCalculated(taxon);
	}
	
	public int getCalculated() {
		return countBioCalls+countXtalCalls;
	}
	
	public int getCalculated(String taxon) {
		return counter.getCountBio(taxon)+counter.getCountXtal(taxon);
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
	
	public int getTP(String taxon) {
		if (type==CallType.BIO) {
			return counter.getCountBio(taxon);
		}
		if (type==CallType.CRYSTAL) {
			return counter.getCountXtal(taxon);
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
	
	public int getFN(String taxon) {
		if (type==CallType.BIO) {
			return counter.getCountXtal(taxon);
		}
		if (type==CallType.CRYSTAL) {
			return counter.getCountBio(taxon);
		}		
		return -1;
	}
	
	public double getAccuracy() {
		return (double)getTP()/(double)getCalculated();
	}

	public double getAccuracy(String taxon) {
		return (double)getTP(taxon)/(double)getCalculated(taxon);
	}
	
	public double getRecall() {
		return (double)getTP()/(double)total;
	}
	
	public double getRecall(String taxon) {
		return (double)getTP(taxon)/(double)(counter.getSubTotal(taxon));
	}
	
	public void print(PrintStream ps) {
		ps.printf("%25s\t%4s\t%16s\t%12s\t%10s\t%4d\t%4d\t%4d\t%4d\t%4.2f\t%4.2f\n",
				name, type.getName(), scoType.getName(), callCutoffs, caCutoffs, total, getTP(),getFN(),getFailed(),getAccuracy(),getRecall());
	}
	
	public void printByTaxon(PrintStream ps, String taxon) {
		ps.printf("%25s\t%4s\t%16s\t%12s\t%10s\t%4d\t%4d\t%4d\t%4d\t%4.2f\t%4.2f\n",
				"", "", taxon, callCutoffs, caCutoffs, counter.getSubTotal(taxon), getTP(taxon),getFN(taxon),getFailed(taxon),getAccuracy(taxon),getRecall(taxon));		
	}
	
	public static void printHeader(PrintStream ps) {
		ps.printf("%25s\t%4s\t%16s\t%12s\t%10s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\n",
				"name","type","scoType","call-cutoffs","CA-cutoffs","tot","tp","fn","fail","acc","rec");
	}
	
}
