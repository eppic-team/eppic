package ch.systemsx.sybit.crkwebui.client.homologs.data;

import java.io.Serializable;

import ch.systemsx.sybit.crkwebui.shared.model.HomologIdentityData;

/**
 * Class used as a model for homologs info grid
 * @author nikhil
 *
 */
public class HomologsItemModel implements Serializable {

	private static final long serialVersionUID = 1L;

	private int uid;
	
	private String uniId;
	private HomologIdentityData idData;
	private double queryCov;
	private String firstTaxon;
	private String lastTaxon;
	
	public HomologsItemModel(int uid,
							String uniId,
							HomologIdentityData idData,
							double queryCov,
							String firstTaxon,
							String lastTaxon)
	{
		this.uid = uid;
		this.uniId = uniId;
		this.idData = idData;
		this.queryCov = queryCov;
		this.firstTaxon = firstTaxon;
		this.lastTaxon = lastTaxon;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getUniId() {
		return uniId;
	}

	public void setUniId(String uniId) {
		this.uniId = uniId;
	}

	public HomologIdentityData getIdData() {
		return idData;
	}

	public void setIdData(HomologIdentityData idData) {
		this.idData = idData;
	}

	public double getQueryCov() {
		return queryCov/100;
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
	
}
