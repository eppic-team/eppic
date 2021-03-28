package eppic.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;
import eppic.model.adapters.HomologListener;

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
@Table(name = "Homolog")
@EntityListeners(HomologListener.class)
public class HomologDB implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int uid;
	
	private String uniProtId;
	private double seqId;
	private double queryCoverage;
	@Column(length = 255)
	private String firstTaxon;
	@Column(length = 255)
	private String lastTaxon;
	private int queryStart;
	private int queryEnd;
	private int subjectStart;
	private int subjectEnd;

	@Column(length = 40000, columnDefinition = "TEXT")
	private String alignedSeq;

	@ManyToOne
	@JsonBackReference
	private ChainClusterDB chainCluster;
	
	public HomologDB() {
		
	}
	
	public String getUniProtId() {
		return uniProtId;
	}

	public void setUniProtId(String uniProtId) {
		this.uniProtId = uniProtId;
	}

	public double getSeqId() {
		return seqId;
	}

	public void setSeqId(double seqId) {
		this.seqId = seqId;
	}

	public double getQueryCoverage() {
		return queryCoverage;
	}

	public void setQueryCoverage(double queryCoverage) {
		this.queryCoverage = queryCoverage;
	}

	public String getFirstTaxon() {
		return firstTaxon;
	}

	public void setFirstTaxon(String firstTaxon) {
		this.firstTaxon = firstTaxon;
	}

	public String getLastTaxon() {
		return lastTaxon;
	}

	public void setLastTaxon(String lastTaxon) {
		this.lastTaxon = lastTaxon;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public ChainClusterDB getChainCluster() {
		return chainCluster;
	}

	public void setChainCluster(ChainClusterDB chainCluster) {
		this.chainCluster = chainCluster;
	}

	public int getQueryStart() {
		return queryStart;
	}

	public void setQueryStart(int queryStart) {
		this.queryStart = queryStart;
	}

	public int getQueryEnd() {
		return queryEnd;
	}

	public void setQueryEnd(int queryEnd) {
		this.queryEnd = queryEnd;
	}

	public int getSubjectStart() {
		return subjectStart;
	}

	public void setSubjectStart(int subjectStart) {
		this.subjectStart = subjectStart;
	}

	public int getSubjectEnd() {
		return subjectEnd;
	}

	public void setSubjectEnd(int subjectEnd) {
		this.subjectEnd = subjectEnd;
	}

	public String getAlignedSeq() {
		return alignedSeq;
	}

	public void setAlignedSeq(String alignedSeq) {
		this.alignedSeq = alignedSeq;
	}
	
}
