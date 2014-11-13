package eppic.db;

public class LatticeOverlapScore {

	public static final double DELTA = 0.000001;
	
	private double fAB;
	private double fBA;
	
	public LatticeOverlapScore(double fAB, double fBA) {
		this.fAB = fAB;
		this.fBA = fBA;
	}

	public double getfAB() {
		return fAB;
	}

	public void setfAB(double fAB) {
		this.fAB = fAB;
	}

	public double getfBA() {
		return fBA;
	}

	public void setfBA(double fBA) {
		this.fBA = fBA;
	}
	
	public double getAvgScore() {
		return (fAB+fBA)/2.0;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof LatticeOverlapScore)) return false;
		LatticeOverlapScore other = (LatticeOverlapScore) o;
		
		if (Math.abs(this.fAB-other.fAB)<DELTA &&
				Math.abs(this.fBA-other.fBA)<DELTA) {
			return true;
		}
		
		return false;
	}
	
	//TODO implement hashCode()
	
	public String toString() {
		return String.format("%5.3f:%5.3f", fAB, fBA);
	}
}
