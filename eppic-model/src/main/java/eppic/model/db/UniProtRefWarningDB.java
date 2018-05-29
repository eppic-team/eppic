package eppic.model.db;

import java.io.Serializable;

public class UniProtRefWarningDB implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String text;
	
	private ChainClusterDB chainCluster;
	
	public UniProtRefWarningDB() {
		
	}
	
	public void setChainCluster(ChainClusterDB chainCluster) {
		this.chainCluster = chainCluster;
	}

	public ChainClusterDB getChainCluster() {
		return chainCluster;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

}
