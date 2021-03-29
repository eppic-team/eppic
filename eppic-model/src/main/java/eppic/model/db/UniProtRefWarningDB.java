package eppic.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;
import java.io.Serializable;

public class UniProtRefWarningDB implements Serializable {

	private static final long serialVersionUID = 1L;

	private String text;

	@JsonBackReference
	private ChainClusterDB chainCluster;
	
	public UniProtRefWarningDB() {
		
	}
	
	public void setChainCluster(ChainClusterDB chainCluster) {
		this.chainCluster = chainCluster;
	}

	public ChainClusterDB getChainCluster() {
		return chainCluster;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

}
