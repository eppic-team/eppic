package eppic.model.dto;

import java.io.Serializable;

import eppic.model.db.HomologDB;

/**
 * DTO class for Homolog
 * 
 * @author duarte_j
 *
 */
public class Homolog implements Serializable {

	private static final long serialVersionUID = 1L;

	private int uid;
	
	private String uniProtId;
	private double seqId;
	private double queryCoverage;
	private String firstTaxon;
	private String lastTaxon;
	private int queryStart;
	private int queryEnd;
	private int subjectStart;
	private int subjectEnd;
	
	private String alignedSeq;
	
	public Homolog() {
		
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


	/**
	 * Converts DB model item into DTO one.
	 * @param homologDB model item to convert
	 * @return DTO representation of model item
	 */
	public static Homolog create(HomologDB homologDB)
	{
		Homolog homolog = new Homolog();		
		homolog.setUid(homologDB.getUid());
		homolog.setUniProtId(homologDB.getUniProtId());
		homolog.setFirstTaxon(homologDB.getFirstTaxon());
		homolog.setLastTaxon(homologDB.getLastTaxon());
		homolog.setSeqId(homologDB.getSeqId());
		homolog.setQueryCoverage(homologDB.getQueryCoverage());
		homolog.setQueryStart(homologDB.getQueryStart());
		homolog.setQueryEnd(homologDB.getQueryEnd());
		homolog.setSubjectStart(homologDB.getSubjectStart());
		homolog.setSubjectEnd(homologDB.getSubjectEnd());
		homolog.setAlignedSeq(homologDB.getAlignedSeq());
		return homolog;
	}
}
