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
	private String subInterval;
	private String alignedSeq1;
	private String alignedSeq2;
	private String markupLine;
	
	private boolean hasQueryMatch;	
	private List<String> queryWarnings;
	
	private double idCutoffUsed;
	
	private PDBScoreItemDB pdbScoreItem;
	
	public HomologsInfoItemDB() 
	{
		
	}
	
	public HomologsInfoItemDB(int uid,
							  String chains, 
							  String uniprotId, 
							  int numHomologs,
							  String subInterval,
							  String alignedSeq1,
							  String alignedSeq2,
							  String markupLine) 
	{
		this.uid = uid;
		this.chains = chains;
		this.uniprotId = uniprotId;
		this.numHomologs = numHomologs;
		this.subInterval = subInterval;
		this.alignedSeq1 = alignedSeq1;
		this.alignedSeq2 = alignedSeq2;
		this.markupLine = markupLine;
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

	public List<String> getQueryWarnings() {
		return queryWarnings;
	}

	public void setQueryWarnings(List<String> queryWarnings) {
		this.queryWarnings = queryWarnings;
	}

	public double getIdCutoffUsed() {
		return idCutoffUsed;
	}

	public void setIdCutoffUsed(double idCutoffUsed) {
		this.idCutoffUsed = idCutoffUsed;
	}


}
