package eppic.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;

import java.io.Serializable;

public class AssemblyContentDB implements Serializable {

	private static final long serialVersionUID = 1L;

	private int mmSize;
	private String symmetry;
	
	/**
	 * Composition based on sequential letters, A, ... Z, AA, ..., AZ, BA, ... ZZ
	 */
	private String stoichiometry;
	/**
	 * Composition based on chain ids
	 */
	private String composition;
	/**
	 * Composition based on representative chain ids
	 */
	private String compositionRepChainIds;

	private String chainIds; // comma separated list of chainId+_+opId belonging to assembly

	@JsonBackReference(value = "assemblyContents-ref")
	private AssemblyDB assembly;

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
