package eppic.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "AssemblyContent")
public class AssemblyContentDB implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int uid;
	@Column(length = 4)
	private String pdbCode;
	private int mmSize;
	@Column(length = 10)
	private String symmetry;
	
	/**
	 * Composition based on sequential letters, A, ... Z, AA, ..., AZ, BA, ... ZZ
	 */
	@Column(length = 15000) // Some large structures, specially viral capsid proteins with NCS operators can have very long list of chains in their assemblies
	private String stoichiometry;
	/**
	 * Composition based on chain ids
	 */
	@Column(length = 15000) // Some large structures, specially viral capsid proteins with NCS operators can have very long list of chains in their assemblies
	private String composition;
	/**
	 * Composition based on representative chain ids
	 */
	@Column(length = 15000) // Some large structures, specially viral capsid proteins with NCS operators can have very long list of chains in their assemblies
	private String compositionRepChainIds;

	@Column(length = 15000) // Some large structures, specially viral capsid proteins with NCS operators can have very long list of chains in their assemblies
	private String chainIds; // comma separated list of chainId+_+opId belonging to assembly

	@ManyToOne
	private AssemblyDB assembly;

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getPdbCode() {
		return pdbCode;
	}

	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
	}

	public int getMmSize() {
		return mmSize;
	}

	public void setMmSize(int mmSize) {
		this.mmSize = mmSize;
	}

	public String getSymmetry() {
		return symmetry;
	}

	public void setSymmetry(String symmetry) {
		this.symmetry = symmetry;
	}

	public String getStoichiometry() {
		return stoichiometry;
	}

	public void setStoichiometry(String stoichiometry) {
		this.stoichiometry = stoichiometry;
	}

	public String getComposition() {
		return composition;
	}

	public void setComposition(String composition) {
		this.composition = composition;
	}

	public String getCompositionRepChainIds() {
		return compositionRepChainIds;
	}

	public void setCompositionRepChainIds(String compositionRepChainIds) {
		this.compositionRepChainIds = compositionRepChainIds;
	}

	public String getChainIds() {
		return chainIds;
	}

	public void setChainIds(String chains) {
		this.chainIds = chains;
	}

	public AssemblyDB getAssembly() {
		return assembly;
	}

	public void setAssembly(AssemblyDB assembly) {
		this.assembly = assembly;
	}
	
	
}
