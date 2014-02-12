package analysis;

import java.io.PrintStream;

import eppic.CallType;

/**
 * A class to store a full Interface Prediction with all 3 methods + combined.
 * Useful for reporting of statistics of many interfaces.
 * 
 * 
 * @author duarte_j
 *
 */
public class InterfacePrediction {

	private String pdbCode;
	private int interfaceId;
	private double area;
	
	private int numHomologs1;
	private int numHomologs2;
	
	private CallType geomCall;
	private double geomScore;
	
	private CallType crCall;
	private double crScore;

	private CallType zCall;
	private double zScore;
	
	private CallType combCall;
	private double combScore; // total votes
	
	public InterfacePrediction() {
		
	}
	
	public void printTabular(PrintStream ps) {
		ps.println(pdbCode+"\t"+interfaceId+"\t"+String.format("%7.2f", area)+"\t"+
				String.format("%3d",numHomologs1)+"\t"+String.format("%3d",numHomologs2)+"\t"+
				formatIntScore(geomScore,3)+"\t" + (geomCall!=null?geomCall.getName():"null") +"\t"+
				formatDoubleScore(crScore)+"\t"  + (crCall!=null?crCall.getName():"null")     +"\t"+
				formatDoubleScore(zScore)+"\t"   + (zCall!=null?zCall.getName():"null")       +"\t"+
				formatIntScore(combScore,1)+"\t" + (combCall!=null?combCall.getName():"null")       );
	}
	
	private String formatIntScore(double score, int digits) {
		return String.format("%"+digits+".0f",score);
	}
	
	private String formatDoubleScore(double score) {
		return String.format("%6.2f", score);
	}

	public String getPdbCode() {
		return pdbCode;
	}

	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
	}

	public int getInterfaceId() {
		return interfaceId;
	}

	public void setInterfaceId(int interfaceId) {
		this.interfaceId = interfaceId;
	}

	public double getArea() {
		return area;
	}
	
	public void setArea(double area) {
		this.area = area;
	}
	
	public CallType getGeomCall() {
		return geomCall;
	}

	public void setGeomCall(CallType geomCall) {
		this.geomCall = geomCall;
	}

	public double getGeomScore() {
		return geomScore;
	}

	public void setGeomScore(double geomScore) {
		this.geomScore = geomScore;
	}

	public CallType getCrCall() {
		return crCall;
	}

	public void setCrCall(CallType crCall) {
		this.crCall = crCall;
	}

	public double getCrScore() {
		return crScore;
	}

	public void setCrScore(double crScore) {
		this.crScore = crScore;
	}

	public CallType getzCall() {
		return zCall;
	}

	public void setzCall(CallType zCall) {
		this.zCall = zCall;
	}

	public double getzScore() {
		return zScore;
	}

	public void setzScore(double zScore) {
		this.zScore = zScore;
	}

	public CallType getCombCall() {
		return combCall;
	}

	public void setCombCall(CallType combCall) {
		this.combCall = combCall;
	}

	public double getCombScore() {
		return combScore;
	}

	public void setCombScore(double combScore) {
		this.combScore = combScore;
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
	
	
	
	

	
}
