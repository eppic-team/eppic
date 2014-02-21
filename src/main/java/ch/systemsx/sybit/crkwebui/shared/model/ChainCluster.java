package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import eppic.model.HomologDB;
import eppic.model.ChainClusterDB;
import eppic.model.UniProtRefWarningDB;

/**
 * DTO class for HomologsInfo item.
 * @author AS
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ChainCluster implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String repChain;	 // the PDB chain code of representative chain
	private String memberChains; // comma separated list of member PDB chain codes
	
	private String refUniProtId;
	private int refUniProtStart;
	private int refUniProtEnd;
	private String pdbAlignedSeq;
	private String refAlignedSeq;
	private String aliMarkupLine;
	
	private boolean hasUniProtRef;
	private List<UniProtRefWarning> uniProtRefWarnings;
	
	private int numHomologs;
	private String msaAlignedSeq;
	
	private double seqIdCutoff;
	private double clusteringSeqId;
	
	private String firstTaxon;
	private String lastTaxon;
	
	@XmlElementWrapper(name = "homologs")
	@XmlElement(name = "homolog")
	private List<Homolog> homologs;
	
	private String pdbCode;
	
	public ChainCluster() 
	{
		
	}
	
	public ChainCluster(int uid,
							String repChain,
							String memberChains,
							String uniprotId,
							int numHomologs,
							int refUniProtStart,
							int refUniProtEnd,
							String alignedSeq1,
							String alignedSeq2,
							String markupLine,
							boolean hasQueryMatch,
							List<UniProtRefWarning> queryWarnings,
							double idCutoffUsed,
							int clusteringPercentIdUsed,
							List<Homolog> homologs) 
	{
		this.uid = uid;
		this.repChain = repChain;
		this.memberChains = memberChains;
		this.refUniProtId = uniprotId;
		this.numHomologs = numHomologs;
		this.refUniProtStart = refUniProtStart;
		this.refUniProtEnd = refUniProtEnd;
		this.pdbAlignedSeq = alignedSeq1;
		this.refAlignedSeq = alignedSeq2;
		this.aliMarkupLine = markupLine;
		this.hasUniProtRef = hasQueryMatch;
		this.uniProtRefWarnings = queryWarnings;
		this.seqIdCutoff = idCutoffUsed;
		this.clusteringSeqId = clusteringPercentIdUsed;
		this.homologs = homologs;
	}
	
	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}

	public String getRefUniProtId() {
		return refUniProtId;
	}

	public void setRefUniProtId(String refUniProtId) {
		this.refUniProtId = refUniProtId;
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

	public String getPdbAlignedSeq() {
		return pdbAlignedSeq;
	}

	public void setPdbAlignedSeq(String pdbAlignedSeq) {
		this.pdbAlignedSeq = pdbAlignedSeq;
	}

	public String getRefAlignedSeq() {
		return refAlignedSeq;
	}

	public void setRefAlignedSeq(String refAlignedSeq) {
		this.refAlignedSeq = refAlignedSeq;
	}

	public String getAliMarkupLine() {
		return aliMarkupLine;
	}

	public void setAliMarkupLine(String aliMarkupLine) {
		this.aliMarkupLine = aliMarkupLine;
	}
	
	public boolean isHasUniProtRef() {
		return hasUniProtRef;
	}

	public void setHasUniProtRef(boolean hasUniProtRef) {
		this.hasUniProtRef = hasUniProtRef;
	}

	public List<UniProtRefWarning> getUniProtRefWarnings() {
		return uniProtRefWarnings;
	}

	public void setUniProtRefWarnings(List<UniProtRefWarning> uniProtRefWarnings) {
		this.uniProtRefWarnings = uniProtRefWarnings;
	}

	public double getseqIdCutoff() {
		return seqIdCutoff;
	}

	public void setSeqIdCutoff(double seqIdCutoff) {
		this.seqIdCutoff = seqIdCutoff;
	}
	
	public double getClusteringSeqId() {
		return clusteringSeqId;
	}
	
	public void setClusteringSeqId(double clusteringSeqId) {
		this.clusteringSeqId = clusteringSeqId;
	}
	
	public List<Homolog> getHomologs() {
		return homologs;
	}

	public void setHomologs(List<Homolog> homologs) {
		this.homologs = homologs;
	}

	public String getRepChain() {
		return repChain;
	}

	public void setRepChain(String repChain) {
		this.repChain = repChain;
	}

	public String getMemberChains() {
		return memberChains;
	}

	public void setMemberChains(String memberChains) {
		this.memberChains = memberChains;
	}

	public String getMsaAlignedSeq() {
		return msaAlignedSeq;
	}

	public void setMsaAlignedSeq(String msaAlignedSeq) {
		this.msaAlignedSeq = msaAlignedSeq;
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

	public String getPdbCode() {
		return pdbCode;
	}

	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
	}

	/**
	 * Converts DB model item into DTO one.
	 * @param chainClusterDB model item to convert
	 * @return DTO representation of model item
	 */
	public static ChainCluster create(ChainClusterDB chainClusterDB)
	{
		ChainCluster chainCluster = new ChainCluster();
		
		chainCluster.setUid(chainClusterDB.getUid());
		
		chainCluster.setPdbAlignedSeq(chainClusterDB.getPdbAlignedSeq());
		chainCluster.setRefAlignedSeq(chainClusterDB.getRefAlignedSeq());
		chainCluster.setAliMarkupLine(chainClusterDB.getAliMarkupLine());
		
		chainCluster.setRepChain(chainClusterDB.getRepChain());
		chainCluster.setMemberChains(chainClusterDB.getMemberChains());
		
		chainCluster.setSeqIdCutoff(chainClusterDB.getSeqIdCutoff());
		chainCluster.setClusteringSeqId(chainClusterDB.getClusteringSeqId());
		
		chainCluster.setNumHomologs(chainClusterDB.getNumHomologs());
		chainCluster.setRefUniProtStart(chainClusterDB.getRefUniProtStart());
		chainCluster.setRefUniProtEnd(chainClusterDB.getRefUniProtEnd());
		chainCluster.setRefUniProtId(chainClusterDB.getRefUniProtId());
		
		chainCluster.setHasUniProtRef(chainClusterDB.isHasUniProtRef());
		
		chainCluster.setPdbCode(chainClusterDB.getPdbCode());
		
		chainCluster.setFirstTaxon(chainClusterDB.getFirstTaxon());
		chainCluster.setLastTaxon(chainClusterDB.getLastTaxon());
		
		List<UniProtRefWarning> uniProtRefWarnings = new ArrayList<UniProtRefWarning>();
		if(chainClusterDB.getUniProtRefWarnings() != null)
		{
			for(UniProtRefWarningDB uniProtRefWarningDB : chainClusterDB.getUniProtRefWarnings())
			{
				UniProtRefWarning uniProtRefWarning = UniProtRefWarning.create(uniProtRefWarningDB);
				uniProtRefWarnings.add(uniProtRefWarning);
			}
		}
		chainCluster.setUniProtRefWarnings(uniProtRefWarnings);
		
		List<Homolog> homologs = new ArrayList<Homolog>();
		if (chainClusterDB.getHomologs()!=null) 
		{
			
			for (HomologDB homologItemDB : chainClusterDB.getHomologs()) 
			{
				Homolog homolog = Homolog.create(homologItemDB);
				homologs.add(homolog);				
			}
		}
		chainCluster.setHomologs(homologs);
		
		return chainCluster;
	}

}
