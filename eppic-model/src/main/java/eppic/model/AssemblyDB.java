package eppic.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AssemblyDB implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private int uid;
	
	private String pdbCode;
	

	private boolean topologicallyValid;
	
	private int mmSize;
	
	private String composition;
	
	private String symmetry;
	private String stoichiometry;
	
	private String pseudoSymmetry;
	private String pseudoStoichiometry;

	private String interfaceClusterIds;
	
	private PdbInfoDB pdbInfo;
	
	private Set<InterfaceClusterDB> interfaceClusters;
	
	private List<AssemblyScoreDB> assemblyScores;

	public AssemblyDB() {
		assemblyScores = new ArrayList<AssemblyScoreDB>();
		interfaceClusters = new HashSet<InterfaceClusterDB>();
	}

	public void addAssemblyScore(AssemblyScoreDB assemblyScore) {
		assemblyScores.add(assemblyScore);
	}
	
	public String getPdbCode() {
		return pdbCode;
	}

	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
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

	public String getPseudoSymmetry() {
		return pseudoSymmetry;
	}

	public void setPseudoSymmetry(String pseudoSymmetry) {
		this.pseudoSymmetry = pseudoSymmetry;
	}

	public String getStoichiometry() {
		return stoichiometry;
	}

	public void setStoichiometry(String stoichiometry) {
		this.stoichiometry = stoichiometry;
	}

	public String getPseudoStoichiometry() {
		return pseudoStoichiometry;
	}

	public void setPseudoStoichiometry(String pseudoStoichiometry) {
		this.pseudoStoichiometry = pseudoStoichiometry;
	}

	public PdbInfoDB getPdbInfo() {
		return pdbInfo;
	}

	public void setPdbInfo(PdbInfoDB pdbInfo) {
		this.pdbInfo = pdbInfo;
	}

	public boolean isTopologicallyValid() {
		return topologicallyValid;
	}

	public void setTopologicallyValid(boolean topologicallyValid) {
		this.topologicallyValid = topologicallyValid;
	}

	public String getComposition() {
		return composition;
	}

	public void setComposition(String composition) {
		this.composition = composition;
	}

	public String getInterfaceClusterIds() {
		return interfaceClusterIds;
	}

	public void setInterfaceClusterIds(String interfaceClusterIds) {
		this.interfaceClusterIds = interfaceClusterIds;
	}

	public Set<InterfaceClusterDB> getInterfaceClusters() {
		return interfaceClusters;
	}

	public void setInterfaceClusters(Set<InterfaceClusterDB> interfaceClusters) {
		this.interfaceClusters = interfaceClusters;
	}

	public List<AssemblyScoreDB> getAssemblyScores() {
		return assemblyScores;
	}

	public void setAssemblyScores(List<AssemblyScoreDB> assemblyScores) {
		this.assemblyScores = assemblyScores;
	}	
	

}
