package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import model.HomologsInfoItemDB;
import model.QueryWarningItemDB;

/**
 * DTO class for HomologsInfo item.
 * @author AS
 */
public class HomologsInfoItem implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String chains;
	private String uniprotId;
	private int numHomologs;
	private String subInterval;
	private String alignedSeq1;
	private String alignedSeq2;
	private String markupLine;
	
	private boolean hasQueryMatch;	
	private List<QueryWarningItem> queryWarnings;
	
	private double idCutoffUsed;
	private int clusteringPercentIdUsed;
	
	public HomologsInfoItem() 
	{
		
	}
	
	public HomologsInfoItem(int uid,
							String chains,
							String uniprotId,
							int numHomologs,
							String subInterval,
							String alignedSeq1,
							String alignedSeq2,
							String markupLine,
							boolean hasQueryMatch,
							List<QueryWarningItem> queryWarnings,
							double idCutoffUsed,
							int clusteringPercentIdUsed) 
	{
		this.uid = uid;
		this.chains = chains;
		this.uniprotId = uniprotId;
		this.numHomologs = numHomologs;
		this.subInterval = subInterval;
		this.alignedSeq1 = alignedSeq1;
		this.alignedSeq2 = alignedSeq2;
		this.markupLine = markupLine;
		this.hasQueryMatch = hasQueryMatch;
		this.queryWarnings = queryWarnings;
		this.idCutoffUsed = idCutoffUsed;
		this.clusteringPercentIdUsed = clusteringPercentIdUsed;
	}
	
	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}

	public String getChains() {
		return chains;
	}

	public void setChains(String chains) {
		this.chains = chains;
	}

	public String getUniprotId() {
		return uniprotId;
	}

	public void setUniprotId(String uniprotId) {
		this.uniprotId = uniprotId;
	}

	public int getNumHomologs() {
		return numHomologs;
	}

	public void setNumHomologs(int numHomologs) {
		this.numHomologs = numHomologs;
	}

	public String getSubInterval() {
		return subInterval;
	}

	public void setSubInterval(String subInterval) {
		this.subInterval = subInterval;
	}

	public String getAlignedSeq1() {
		return alignedSeq1;
	}

	public void setAlignedSeq1(String alignedSeq1) {
		this.alignedSeq1 = alignedSeq1;
	}

	public String getAlignedSeq2() {
		return alignedSeq2;
	}

	public void setAlignedSeq2(String alignedSeq2) {
		this.alignedSeq2 = alignedSeq2;
	}

	public String getMarkupLine() {
		return markupLine;
	}

	public void setMarkupLine(String markupLine) {
		this.markupLine = markupLine;
	}
	
	public boolean isHasQueryMatch() {
		return hasQueryMatch;
	}

	public void setHasQueryMatch(boolean hasQueryMatch) {
		this.hasQueryMatch = hasQueryMatch;
	}

	public List<QueryWarningItem> getQueryWarnings() {
		return queryWarnings;
	}

	public void setQueryWarnings(List<QueryWarningItem> queryWarnings) {
		this.queryWarnings = queryWarnings;
	}

	public double getIdCutoffUsed() {
		return idCutoffUsed;
	}

	public void setIdCutoffUsed(double idCutoffUsed) {
		this.idCutoffUsed = idCutoffUsed;
	}
	
	public int getClusteringPercentIdUsed() {
		return clusteringPercentIdUsed;
	}
	
	public void setClusteringPercentIdUsed(int clusteringPercentIdUsed) {
		this.clusteringPercentIdUsed = clusteringPercentIdUsed;
	}
	
	/**
	 * Converts DB model item into DTO one.
	 * @param homologsInfoItemDB model item to convert
	 * @return DTO representation of model item
	 */
	public static HomologsInfoItem create(HomologsInfoItemDB homologsInfoItemDB)
	{
		HomologsInfoItem homologsInfoItem = new HomologsInfoItem();
		homologsInfoItem.setAlignedSeq1(homologsInfoItemDB.getAlignedSeq1());
		homologsInfoItem.setAlignedSeq2(homologsInfoItemDB.getAlignedSeq2());
		homologsInfoItem.setChains(homologsInfoItemDB.getChains());
		homologsInfoItem.setMarkupLine(homologsInfoItemDB.getMarkupLine());
		homologsInfoItem.setNumHomologs(homologsInfoItemDB.getNumHomologs());
		homologsInfoItem.setSubInterval(homologsInfoItemDB.getSubInterval());
		homologsInfoItem.setUniprotId(homologsInfoItemDB.getUniprotId());
		homologsInfoItem.setUid(homologsInfoItemDB.getUid());
		homologsInfoItem.setHasQueryMatch(homologsInfoItemDB.isHasQueryMatch());
		
		List<QueryWarningItem> queryWarningItems = new ArrayList<QueryWarningItem>();
		if(homologsInfoItemDB.getQueryWarnings() != null)
		{
			for(QueryWarningItemDB queryWarningItemDB : homologsInfoItemDB.getQueryWarnings())
			{
				QueryWarningItem queryWarningItem = QueryWarningItem.create(queryWarningItemDB);
				queryWarningItems.add(queryWarningItem);
			}
		}
		homologsInfoItem.setQueryWarnings(queryWarningItems);
		homologsInfoItem.setIdCutoffUsed(homologsInfoItemDB.getIdCutoffUsed());
		homologsInfoItem.setClusteringPercentIdUsed(homologsInfoItemDB.getClusteringPercentIdUsed());
		
		return homologsInfoItem;
	}

}
