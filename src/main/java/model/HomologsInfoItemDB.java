package model;

import java.io.Serializable;
import java.util.List;

public class HomologsInfoItemDB implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String chains;
	// if any of the following is null then there's no homologs and thus no info to display
	private String uniprotId;
	private int numHomologs;
	private int refUniProtStart;
	private int refUniProtEnd;
	private String alignedSeq1;
	private String alignedSeq2;
	private String markupLine;
	
	private boolean hasQueryMatch;	
	private List<QueryWarningItemDB> queryWarnings;
	
	private Double idCutoffUsed;
	private int clusteringPercentIdUsed;
	
	private List<HomologItemDB> homologs;
	
	private PDBScoreItemDB pdbScoreItem;
	
	public HomologsInfoItemDB() 
	{
		
	}
	
	public HomologsInfoItemDB(int uid,
							  String chains, 
							  String uniprotId, 
							  int numHomologs,
							  int refUniProtStart,
							  int refUniProtEnd,
							  String alignedSeq1,
							  String alignedSeq2,
							  String markupLine,
							  boolean hasQueryMatch, 
							  List<QueryWarningItemDB> queryWarnings,
							  double idCutoffUsed,
							  int clusteringPercentIdUsed,
							  List<HomologItemDB> homologs) 
	{
		this.uid = uid;
		this.chains = chains;
		this.uniprotId = uniprotId;
		this.numHomologs = numHomologs;
		this.refUniProtStart = refUniProtStart;
		this.refUniProtEnd = refUniProtEnd;
		this.alignedSeq1 = alignedSeq1;
		this.alignedSeq2 = alignedSeq2;
		this.markupLine = markupLine;
		this.hasQueryMatch = hasQueryMatch;
		this.queryWarnings = queryWarnings;
		this.idCutoffUsed = idCutoffUsed;
		this.clusteringPercentIdUsed = clusteringPercentIdUsed;
		this.homologs = homologs;
	}
	
	public void setPdbScoreItem(PDBScoreItemDB pdbScoreItem) {
		this.pdbScoreItem = pdbScoreItem;
	}

	public PDBScoreItemDB getPdbScoreItem() {
		return pdbScoreItem;
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

	public int getRefUniProtStart() {
		return refUniProtStart;
	}

	public void setRefUniProtStart(int refUniProtStart) {
		this.refUniProtStart = refUniProtStart;
	}

	public int getRefUniProtEnd() {
		return refUniProtEnd;
	}

	public void setRefUniProtEnd(int refUniProtEnd) {
		this.refUniProtEnd = refUniProtEnd;
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

	public List<QueryWarningItemDB> getQueryWarnings() {
		return queryWarnings;
	}

	public void setQueryWarnings(List<QueryWarningItemDB> queryWarnings) {
		this.queryWarnings = queryWarnings;
	}

	public Double getIdCutoffUsed() {
		return idCutoffUsed;
	}

	public void setIdCutoffUsed(Double idCutoffUsed) {
		this.idCutoffUsed = idCutoffUsed;
	}
	
	public int getClusteringPercentIdUsed() {
		return clusteringPercentIdUsed;
	}
	
	public void setClusteringPercentIdUsed(int clusteringPercentIdUsed) {
		this.clusteringPercentIdUsed = clusteringPercentIdUsed;
	}

	public List<HomologItemDB> getHomologs() {
		return homologs;
	}

	public void setHomologs(List<HomologItemDB> homologs) {
		this.homologs = homologs;
	}

}
