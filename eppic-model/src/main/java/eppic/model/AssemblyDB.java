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
	
	private String interfaceClusterIds;
	
	private PdbInfoDB pdbInfo;
	
	private Set<InterfaceClusterDB> interfaceClusters;
	
	private List<AssemblyScoreDB> assemblyScores;
	
	private List<AssemblyContentDB> assemblyContents;

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

	public List<AssemblyContentDB> getAssemblyContents() {
		return assemblyContents;
	}

	public void setAssemblyContents(List<AssemblyContentDB> assemblyContents) {
		this.assemblyContents = assemblyContents;
	}	
	

}
