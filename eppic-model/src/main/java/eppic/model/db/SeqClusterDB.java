package eppic.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "SeqCluster")
public class SeqClusterDB implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int uid;
	@Column(length = 4)
	private String pdbCode;

	// 			The chain code is actually case sensitive but MySQL by default uses a case insensitive
	//			collation (ci). The WUI actually needs it to be case sensitive since the repChain is
	//			part of the primary key when the sequence clusters are queried by providing a
	//			pdbCode+repChain in the URL.
	//			Instead of explicitly defining a case sensitive	collation for this column only, we do
	//			it for the whole server using the 'collation-server=latin1_general_cs' setting. Otherwise
	//			other things break while trying to query (in offline analyses), e.g. one needs to use binary
	//			comparisons to be case sensitive but the binary comparisons don't use indexes and thus they
	//			are slow.
	//			See issues
	//			https://github.com/eppic-team/eppic-wui/issues/4
	//			https://github.com/eppic-team/eppic/issues/36
	@Column(length = 4)
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

	@OneToOne
	private ChainClusterDB chainCluster;

	public SeqClusterDB() {
		
	}
	
	public SeqClusterDB(int c100, int c95, int c90, int c80, int c70, int c60, int c50, int c40, int c30) {
		this.c100 = c100;
		this.c95 = c95;
		this.c90 = c90;
		this.c80 = c80;
		this.c70 = c70;
		this.c60 = c60;
		this.c50 = c50;
		this.c40 = c40;
		this.c30 = c30;
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
