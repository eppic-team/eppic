package eppic.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An assembly of an entry, identified within the entry by its id: positive for the topologically
 * valid assemblies, 0 for the unit cell and negative for the topologically invalid ones.
 * <p>
 * In the data model it hangs from {@link PdbInfoDB#getAssemblies()}, but in the db every assembly
 * is a document of its own in a separate collection, since the assemblies (their graph nodes and
 * edges) are most of the size of an entry and all together in a single document they can go over
 * the document size limit. The id of the entry, which is in the index, is not a field of this
 * class: it exists only in the db documents and is handled by the DAO.
 */
@Table(name = "Assembly",
		indexes = @Index(name = "entryId-id_idx", columnList = "entryId,id", unique = true))
public class AssemblyDB implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private int id;

	private boolean unitCellAssembly;

	private boolean topologicallyValid;

	private String interfaceClusterIds;

	@JsonBackReference(value = "assemblies-ref")
	private PdbInfoDB pdbInfo;

	// note that many-to-many do not work in jackson (JsonManagedReference and JsonBackReference are for one-to-many)
	//@JsonManagedReference(value = "interfaceClusters-assembly-ref")
	private Set<InterfaceClusterDB> interfaceClusters;

	@JsonManagedReference(value = "assemblyScores-ref")
	private List<AssemblyScoreDB> assemblyScores;

	@JsonManagedReference(value = "assemblyContents-ref")
	private List<AssemblyContentDB> assemblyContents;

	@JsonManagedReference(value = "graphNodes-ref")
	private List<GraphNodeDB> graphNodes;

	@JsonManagedReference(value = "graphEdges-ref")
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
