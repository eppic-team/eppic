package eppic.model.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import eppic.model.db.HomologDB;
import eppic.model.db.ChainClusterDB;
import eppic.model.db.ResidueInfoDB;
import eppic.model.db.UniProtRefWarningDB;

/**
 * DTO class for HomologsInfo item.
 * @author AS
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ChainCluster implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String repChain;	 // the PDB chain code of representative chain
	private String memberChains; // comma separated list of member PDB chain codes
	private int numMembers;
	
	private boolean protein;
	
	private String refUniProtId;
	private int refUniProtStart;
	private int refUniProtEnd;
	private int pdbStart;
	private int pdbEnd;
	private String pdbAlignedSeq;
	private String refAlignedSeq;
	
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
	
	private SeqCluster seqCluster;

	private List<ResidueInfo> residueInfos;
	
	public ChainCluster() {
		
	}
	
	public ChainCluster(int uid,
							String repChain,
							String memberChains,
							int numMembers,
							boolean protein,
							String uniprotId,
							int numHomologs,
							int refUniProtStart,
							int refUniProtEnd,
							int pdbStart,
							int pdbEnd,
							String alignedSeq1,
							String alignedSeq2,							
							boolean hasQueryMatch,
							List<UniProtRefWarning> queryWarnings,
							double idCutoffUsed,
							int clusteringPercentIdUsed,
							List<Homolog> homologs) {
		
		this.uid = uid;
		this.repChain = repChain;
		this.memberChains = memberChains;
		this.numMembers = numMembers;
		this.protein = protein;
		this.refUniProtId = uniprotId;
		this.numHomologs = numHomologs;
		this.refUniProtStart = refUniProtStart;
		this.refUniProtEnd = refUniProtEnd;
		this.pdbStart = pdbStart;
		this.pdbEnd = pdbEnd;
		this.pdbAlignedSeq = alignedSeq1;
		this.refAlignedSeq = alignedSeq2;
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

	public boolean isProtein() {
		return this.protein;
	}
	
	public void setProtein(boolean protein) {
		this.protein = protein;
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

	public int getPdbStart() {
		return pdbStart;
	}

	public void setPdbStart(int pdbStart) {
		this.pdbStart = pdbStart;
	}

	public int getPdbEnd() {
		return pdbEnd;
	}

	public void setPdbEnd(int pdbEnd) {
		this.pdbEnd = pdbEnd;
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

	public double getSeqIdCutoff() {
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
	
	public String getMemberChainsNoRepresentative() {
		
		if (memberChains==null || repChain==null) return null;

		// length==1 means only member is the rep chain: return null 
		if (memberChains.length()<2) return null;			
		
		String chainsStr = null;
		chainsStr = memberChains.replaceAll(repChain+",", ""); // if the rep chain is not the last one
		chainsStr = chainsStr.replaceAll(","+repChain, ""); // in case the rep chain was the last one 
		return chainsStr;
	}

	public void setMemberChains(String memberChains) {
		this.memberChains = memberChains;
	}

	public int getNumMembers() {
		return numMembers;
	}

	public void setNumMembers(int numMembers) {
		this.numMembers = numMembers;
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

	public SeqCluster getSeqCluster() {
		return seqCluster;
	}
	
	public void setSeqCluster(SeqCluster seqCluster) {
		this.seqCluster = seqCluster;
	}

	public List<ResidueInfo> getResidueInfos() {
		return residueInfos;
	}

	public void setResidueInfos(List<ResidueInfo> residueInfos) {
		this.residueInfos = residueInfos;
	}

	/**
	 * Converts DB model item into DTO one.
	 * @param chainClusterDB model item to convert
	 * @return DTO representation of model item
	 */
	public static ChainCluster create(ChainClusterDB chainClusterDB) {
		
		ChainCluster chainCluster = new ChainCluster();
		
		chainCluster.setUid(chainClusterDB.getUid());
		
		chainCluster.setPdbAlignedSeq(chainClusterDB.getPdbAlignedSeq());
		chainCluster.setRefAlignedSeq(chainClusterDB.getRefAlignedSeq());
		
		chainCluster.setProtein(chainClusterDB.isProtein());
		
		chainCluster.setRepChain(chainClusterDB.getRepChain());
		chainCluster.setMemberChains(chainClusterDB.getMemberChains());
		chainCluster.setNumMembers(chainClusterDB.getNumMembers());
		
		chainCluster.setSeqIdCutoff(chainClusterDB.getSeqIdCutoff());
		chainCluster.setClusteringSeqId(chainClusterDB.getClusteringSeqId());
		
		chainCluster.setNumHomologs(chainClusterDB.getNumHomologs());
		chainCluster.setRefUniProtStart(chainClusterDB.getRefUniProtStart());
		chainCluster.setRefUniProtEnd(chainClusterDB.getRefUniProtEnd());
		chainCluster.setRefUniProtId(chainClusterDB.getRefUniProtId());
		chainCluster.setPdbStart(chainClusterDB.getPdbStart());
		chainCluster.setPdbEnd(chainClusterDB.getPdbEnd());
		
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
		
		if (chainClusterDB.getSeqCluster()!=null) {
			chainCluster.setSeqCluster(SeqCluster.create(chainClusterDB.getSeqCluster()));
		}

		List<ResidueInfo> residueInfos = new ArrayList<ResidueInfo>();
		if (chainClusterDB.getResidueInfos()!=null) {
			for (ResidueInfoDB residueInfoDB : chainClusterDB.getResidueInfos()) {
				residueInfos.add(ResidueInfo.create(residueInfoDB));
			}
		}
		chainCluster.setResidueInfos(residueInfos);

		return chainCluster;
	}

}
