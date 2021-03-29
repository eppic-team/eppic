package eppic.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AssemblyDB implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private int id;

	private boolean unitCellAssembly;

	private boolean topologicallyValid;

	private String interfaceClusterIds;

	@JsonBackReference
	private PdbInfoDB pdbInfo;

	@JsonManagedReference
	private Set<InterfaceClusterDB> interfaceClusters;

	@JsonManagedReference
	private List<AssemblyScoreDB> assemblyScores;

	@JsonManagedReference
	private List<AssemblyContentDB> assemblyContents;

	@JsonManagedReference
	private List<GraphNodeDB> graphNodes;

	@JsonManagedReference
	private List<GraphEdgeDB> graphEdges;

	public AssemblyDB() {
		assemblyScores = new ArrayList<AssemblyScoreDB>();
		interfaceClusters = new HashSet<InterfaceClusterDB>();
	}

	public void addAssemblyScore(AssemblyScoreDB assemblyScore) {
		assemblyScores.add(assemblyScore);
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public boolean isUnitCellAssembly() {
		return unitCellAssembly;
	}

	public List<GraphNodeDB> getGraphNodes() {
		return graphNodes;
	}

	public List<GraphEdgeDB> getGraphEdges() {
		return graphEdges;
	}

	public void setUnitCellAssembly(boolean unitCellAssembly) {
		this.unitCellAssembly = unitCellAssembly;
	}

	public void setGraphNodes(List<GraphNodeDB> graphNodes) {
		this.graphNodes = graphNodes;
	}

	public void setGraphEdges(List<GraphEdgeDB> graphEdges) {
		this.graphEdges = graphEdges;
	}

	/**
	 * Return a comma separated list of all chain ids present in the assembly.
	 * A single list is returned, whether assembly is disjoint or not.
	 * @return
	 */
	@JsonIgnore
	public String getChainIdsString() {

		if (assemblyContents.size()==0) return null;
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<assemblyContents.size();i++) {
			if (i!=0) sb.append(",");
			sb.append(assemblyContents.get(i).getChainIds());
		}
		return sb.toString();
	}
}
