package eppic.model.db;

import java.io.Serializable;
import java.util.List;

public class ChainClusterDB implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String repChain;	 // the PDB chain code of representative chain
	private String memberChains; // comma separated list of member PDB chain codes
	
	private int numMembers;
	
	private boolean protein;	// whether the chain is a protein chain or not, if not all the below will be null
	
	// if any of the following is null then there's no homologs and thus no info to display	
	private String refUniProtId;
	
	private int refUniProtStart;	
	private int refUniProtEnd;
	
	private int pdbStart;
	private int pdbEnd;
	
	private String pdbAlignedSeq;
	private String refAlignedSeq;
	
	private boolean hasUniProtRef;
		
	private List<UniProtRefWarningDB> uniProtRefWarnings;
	
	private int numHomologs;
	private String msaAlignedSeq;
	
	private double seqIdCutoff;
	private double clusteringSeqId;
	
	private String firstTaxon;
	private String lastTaxon;
	
	private List<HomologDB> homologs;
	
	private String pdbCode;
	
	private PdbInfoDB pdbInfo;
	
	private SeqClusterDB seqCluster;
	
	private List<ResidueInfoDB> residueInfos;
	
	public ChainClusterDB() {
		
	}
	
	public ChainClusterDB(int uid, String pdbCode, String repChain, String pdbAlignedSeq, String msaAlignedSeq, String refAlignedSeq) {
		this.uid = uid;
		this.pdbCode = pdbCode;
		this.repChain = repChain;
		this.pdbAlignedSeq = pdbAlignedSeq;
		this.msaAlignedSeq = msaAlignedSeq;
		this.refAlignedSeq = refAlignedSeq;
	}
	
	public ResidueInfoDB getResidue(int resSerial) {
		for (ResidueInfoDB res:residueInfos) {
			if (res.getResidueNumber()==resSerial) 
				return res;
		}
		return null;
	}
	
	public void setPdbInfo(PdbInfoDB pdbInfo) {
		this.pdbInfo = pdbInfo;
	}

	public PdbInfoDB getPdbInfo() {
		return pdbInfo;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
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
	
	public int getNumMembers() {
		return numMembers;
	}

	public void setNumMembers(int numMembers) {
		this.numMembers = numMembers;
	}

	public boolean isProtein() {
		return protein;
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

	public String getMsaAlignedSeq() {
		return msaAlignedSeq;
	}

	public void setMsaAlignedSeq(String msaAlignedSeq) {
		this.msaAlignedSeq = msaAlignedSeq;
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

	public List<UniProtRefWarningDB> getUniProtRefWarnings() {
		return uniProtRefWarnings;
	}

	public void setUniProtRefWarnings(List<UniProtRefWarningDB> uniProtRefWarnings) {
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

	public List<HomologDB> getHomologs() {
		return homologs;
	}

	public void setHomologs(List<HomologDB> homologs) {
		this.homologs = homologs;
	}

	public String getPdbCode() {
		return pdbCode;
	}

	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
	}

	public SeqClusterDB getSeqCluster() {
		return seqCluster;
	}

	public void setSeqCluster(SeqClusterDB seqCluster) {
		this.seqCluster = seqCluster;
	}

	public List<ResidueInfoDB> getResidueInfos() {
		return residueInfos;
	}

	public void setResidueInfos(List<ResidueInfoDB> residueInfos) {
		this.residueInfos = residueInfos;
	}


}
