package eppic.model;

import java.io.Serializable;

public class ResidueInfoDB implements Serializable {

	private static final long serialVersionUID = 1L;

	
	private int uid;

	private String pdbCode;
	private String repChain;

	private int residueNumber;
	private String pdbResidueNumber;
	private String residueType;
	
	private int uniProtNumber;
	
	private boolean mismatchToRef;
	
	private double entropyScore;
	
	private ChainClusterDB chainCluster;
	

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getPdbCode() {
		return pdbCode;
	}

	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
	}

	public String getRepChain() {
		return repChain;
	}

	public void setRepChain(String repChain) {
		this.repChain = repChain;
	}

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
