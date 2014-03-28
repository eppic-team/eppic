package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import eppic.model.SeqClusterDB;


public class SeqCluster implements Serializable {

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
	
	// TODO do we need the chainCluster here?
	//private ChainCluster chainCluster;

	public SeqCluster() {
		
	}
	
	public SeqCluster(SeqClusterDB seqClusterDB) {
		this.setUid(seqClusterDB.getUid());
		this.setPdbCode(seqClusterDB.getPdbCode());
		this.setRepChain(seqClusterDB.getRepChain());
		this.setC100(seqClusterDB.getC100());
		this.setC95(seqClusterDB.getC95());
		this.setC90(seqClusterDB.getC90());
		this.setC80(seqClusterDB.getC80());
		this.setC70(seqClusterDB.getC70());
		this.setC60(seqClusterDB.getC60());
		this.setC50(seqClusterDB.getC50());
		
	}
	
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

	
	/**
	 * Converts DB model item into DTO one.
	 * @param seqClusterDB model item to convert
	 * @return DTO representation of model item
	 */
	public static SeqCluster create(SeqClusterDB seqClusterDB) {
		SeqCluster seqCluster = new SeqCluster();
		
		seqCluster.setUid(seqClusterDB.getUid()); 
		
		seqCluster.setPdbCode(seqClusterDB.getPdbCode());
		seqCluster.setRepChain(seqClusterDB.getRepChain());
		seqCluster.setC100(seqClusterDB.getC100());
		seqCluster.setC95(seqClusterDB.getC95());
		seqCluster.setC90(seqClusterDB.getC90());
		seqCluster.setC80(seqClusterDB.getC80());
		seqCluster.setC70(seqClusterDB.getC70());
		seqCluster.setC60(seqClusterDB.getC60());
		seqCluster.setC50(seqClusterDB.getC50());

		return seqCluster;
	}

}
