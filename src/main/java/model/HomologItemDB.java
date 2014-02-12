package model;

import java.io.Serializable;

public class HomologItemDB implements Serializable {

	private static final long serialVersionUID = 1L;

	private int uid;
	
	private String uniId;
	private Double seqIdToQuery;
	private Double queryCov;
	private String firstTaxon;
	private String lastTaxon;
	private int queryStart;
	private int queryEnd;
	
	private HomologsInfoItemDB homologsInfoItem;
	
	public HomologItemDB() {
		
	}
	
	public HomologItemDB(int uid,
						 String uniId,
						 double seqIdToQuery,
						 double queryCov,
						 String firstTaxon,
						 String lastTaxon,
						 int queryStart,
						 int queryEnd) {
		
		this.uid = uid;
		this.uniId = uniId;
		this.seqIdToQuery = seqIdToQuery;
		this.queryCov = queryCov;
		this.firstTaxon = firstTaxon;
		this.lastTaxon = lastTaxon;
		this.queryStart = queryStart;
		this.queryEnd = queryEnd;
		
	}

	public String getUniId() {
		return uniId;
	}

	public void setUniId(String uniId) {
		this.uniId = uniId;
	}

	public Double getSeqIdToQuery() {
		return seqIdToQuery;
	}

	public void setSeqIdToQuery(Double seqIdToQuery) {
		this.seqIdToQuery = seqIdToQuery;
	}

	public Double getQueryCov() {
		return queryCov;
	}

	public void setQueryCov(Double queryCov) {
		this.queryCov = queryCov;
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

	public HomologsInfoItemDB getHomologsInfoItem() {
		return homologsInfoItem;
	}

	public void setHomologsInfoItem(HomologsInfoItemDB homologsInfoItem) {
		this.homologsInfoItem = homologsInfoItem;
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
	
}
