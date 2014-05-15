package eppic.model;

import java.io.Serializable;

public class SeqClusterDB implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private int uid;
	private String pdbCode;
	private String repChain;
	private int c100;
	private int c95;
	private int c90;
	private int c80;
	private int c70;
	private int c60;
	private int c50;
	private int c40;
	private int c30;
	 
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

	public int getC100() {
		return c100;
	}

	public void setC100(int c100) {
		this.c100 = c100;
	}

	public int getC95() {
		return c95;
	}

	public void setC95(int c95) {
		this.c95 = c95;
	}

	public int getC90() {
		return c90;
	}

	public void setC90(int c90) {
		this.c90 = c90;
	}

	public int getC80() {
		return c80;
	}

	public void setC80(int c80) {
		this.c80 = c80;
	}

	public int getC70() {
		return c70;
	}

	public void setC70(int c70) {
		this.c70 = c70;
	}

	public int getC60() {
		return c60;
	}

	public void setC60(int c60) {
		this.c60 = c60;
	}

	public int getC50() {
		return c50;
	}

	public void setC50(int c50) {
		this.c50 = c50;
	}

	public int getC40() {
		return c40;
	}

	public void setC40(int c40) {
		this.c40 = c40;
	}

	public int getC30() {
		return c30;
	}

	public void setC30(int c30) {
		this.c30 = c30;
	}

	public ChainClusterDB getChainCluster() {
		return chainCluster;
	}

	public void setChainCluster(ChainClusterDB chainCluster) {
		this.chainCluster = chainCluster;
	}
	
}
