package martin;

public class DisopredResidue {
	private char residue;
	private boolean prediction;
	private double confidence;
	
	public DisopredResidue(char residue, boolean prediction, double confidence) {
		this.confidence = confidence;
		this.prediction = prediction;
		this.residue = residue;
	}
	
	public char getResidue() {
		return residue;
	}
	public void setResidue(char residue) {
		this.residue = residue;
	}
	public boolean isPrediction() {
		return prediction;
	}
	public void setPrediction(boolean prediction) {
		this.prediction = prediction;
	}
	public double getConfidence() {
		return confidence;
	}
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
}
