package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import model.HomologItemDB;

/**
 * DTO class for HomologItem
 * 
 * @author duarte_j
 *
 */
public class HomologItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private int uid;
	
	private String uniId;
	private double seqIdToQuery;
	private double queryCov;
	private String firstTaxon;
	private String lastTaxon;
	private int queryStart;
	private int queryEnd;
	
	public HomologItem() {
		
	}
	

	public String getUniId() {
		return uniId;
	}

	public void setUniId(String uniId) {
		this.uniId = uniId;
	}

	public double getSeqIdToQuery() {
		return seqIdToQuery;
	}

	public void setSeqIdToQuery(double seqIdToQuery) {
		this.seqIdToQuery = seqIdToQuery;
	}

	public double getQueryCov() {
		return queryCov;
	}

	public void setQueryCov(double queryCov) {
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


	/**
	 * Converts DB model item into DTO one.
	 * @param homologItemDB model item to convert
	 * @return DTO representation of model item
	 */
	public static HomologItem create(HomologItemDB homologItemDB)
	{
		HomologItem homologItem = new HomologItem();		
		homologItem.setUid(homologItemDB.getUid());
		homologItem.setUniId(homologItemDB.getUniId());
		homologItem.setFirstTaxon(homologItemDB.getFirstTaxon());
		homologItem.setLastTaxon(homologItemDB.getLastTaxon());
		homologItem.setSeqIdToQuery(homologItemDB.getSeqIdToQuery());
		homologItem.setQueryCov(homologItemDB.getQueryCov());
		homologItem.setQueryStart(homologItemDB.getQueryStart());
		homologItem.setQueryEnd(homologItemDB.getQueryEnd());
		return homologItem;
	}
}
