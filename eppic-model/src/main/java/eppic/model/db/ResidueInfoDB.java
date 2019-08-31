package eppic.model.db;

import eppic.model.adapters.ResidueInfoListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "ResidueInfo")
@EntityListeners(ResidueInfoListener.class)
public class ResidueInfoDB implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int uid;

	@Column(length = 4)
	private String pdbCode;
	@Column(length = 4)
	private String repChain;

	private int residueNumber;
	private String pdbResidueNumber;

	@Column(length = 3)
	private String residueType;
	
	private int uniProtNumber;
	
	private boolean mismatchToRef;
	
	private double entropyScore;

	@ManyToOne
	private ChainClusterDB chainCluster;

	// Not adding the one to many to ResidueBurials because it's not so useful.
	// It's only needed in the other direction

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
