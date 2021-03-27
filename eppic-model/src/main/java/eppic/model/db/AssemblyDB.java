package eppic.model.db;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Assembly")
public class AssemblyDB implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int uid;

	private int id;

	@Column(length = 4)
	private String pdbCode;

	private boolean unitCellAssembly;

	private boolean topologicallyValid;

	@Column(length = 20000) // Entries like ribosomes can have very long list of interfaces here
	private String interfaceClusterIds;

	@ManyToOne
	private PdbInfoDB pdbInfo;

	@ManyToMany()
	@JoinTable(name = "InterfaceClusterAssembly",
			joinColumns = @JoinColumn(name = "assembly_uid", referencedColumnName = "uid"),
			inverseJoinColumns = @JoinColumn(name = "interfaceCluster_uid", referencedColumnName = "uid"))
	private Set<InterfaceClusterDB> interfaceClusters;

	@OneToMany(mappedBy = "assembly", cascade = CascadeType.ALL)
	private List<AssemblyScoreDB> assemblyScores;

	@OneToMany(mappedBy = "assembly", cascade = CascadeType.ALL)
	private List<AssemblyContentDB> assemblyContents;

	@OneToMany(mappedBy = "assembly", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<GraphNodeDB> graphNodes;

	@OneToMany(mappedBy = "assembly", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<GraphEdgeDB> graphEdges;

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
