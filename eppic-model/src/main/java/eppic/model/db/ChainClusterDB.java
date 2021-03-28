package eppic.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import eppic.model.adapters.ChainClusterListener;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "ChainCluster")
@EntityListeners(ChainClusterListener.class)
public class ChainClusterDB implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int uid;

	@Column(length = 4)
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

	@Column(length = 40000, columnDefinition = "TEXT")
	private String pdbAlignedSeq;
	@Column(length = 40000, columnDefinition = "TEXT")
	private String refAlignedSeq;
	
	private boolean hasUniProtRef;

	@OneToMany(mappedBy = "chainCluster", cascade = CascadeType.ALL)
	private List<UniProtRefWarningDB> uniProtRefWarnings;
	
	private int numHomologs;
	@Column(length = 40000, columnDefinition = "TEXT")
	private String msaAlignedSeq;
	
	private double seqIdCutoff;
	private double clusteringSeqId;
	
	private String firstTaxon;
	private String lastTaxon;

	@OneToMany(mappedBy = "chainCluster", cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<HomologDB> homologs;

	@Column(length = 4)
	private String pdbCode;

	@ManyToOne
	@JsonBackReference
	private PdbInfoDB pdbInfo;

	@OneToOne(mappedBy = "chainCluster", cascade = CascadeType.ALL)
	private SeqClusterDB seqCluster;

	@OneToMany(mappedBy = "chainCluster", cascade = CascadeType.ALL)
	@JsonManagedReference
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
