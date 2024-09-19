package eppic.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;
import java.io.Serializable;

public class ResidueInfoDB implements Serializable {

	private static final long serialVersionUID = 1L;

	private int residueNumber;
	private String pdbResidueNumber;

	private String residueType;
	
	private int uniProtNumber;
	
	private boolean mismatchToRef;
	
	private double entropyScore;

	@JsonBackReference(value = "residueInfos-ref")
	private ChainClusterDB chainCluster;

	// Not adding the one to many to ResidueBurials because it's not so useful.
	// It's only needed in the other direction

	public int getResidueNumber() {
		return residueNumber;
	}

	public void setResidueNumber(int residueNumber) {
		this.residueNumber = residueNumber;
	}

	public String getPdbResidueNumber() {
		return pdbResidueNumber;
	}

	public void setPdbResidueNumber(String pdbResidueNumber) {
		this.pdbResidueNumber = pdbResidueNumber;
	}

	public String getResidueType() {
		return residueType;
	}

	public void setResidueType(String residueType) {
		this.residueType = residueType;
	}


	public int getUniProtNumber() {
		return uniProtNumber;
	}

	public void setUniProtNumber(int uniProtNumber) {
		this.uniProtNumber = uniProtNumber;
	}

	public boolean isMismatchToRef() {
		return mismatchToRef;
	}

	public void setMismatchToRef(boolean mismatchToRef) {
		this.mismatchToRef = mismatchToRef;
	}

	public double getEntropyScore() {
		return entropyScore;
	}

	public void setEntropyScore(double entropyScore) {
		this.entropyScore = entropyScore;
	}

	public ChainClusterDB getChainCluster() {
		return chainCluster;
	}

	public void setChainCluster(ChainClusterDB chainCluster) {
		this.chainCluster = chainCluster;
	}

	
	
}
