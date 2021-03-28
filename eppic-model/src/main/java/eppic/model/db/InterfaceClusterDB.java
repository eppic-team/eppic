package eppic.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "InterfaceCluster")
public class InterfaceClusterDB implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int uid;
	private int clusterId;

	@Column(length = 4)
	private String pdbCode;
	
	private double avgArea;
	private double avgContactOverlapScore;
	
	private boolean infinite;
	private boolean isologous;

	private int numMembers;
	
	private int globalInterfClusterId;

	@ManyToMany(mappedBy = "interfaceClusters", cascade = CascadeType.ALL)
	@JsonBackReference
	private Set<AssemblyDB> assemblies;

	@OneToMany(mappedBy = "interfaceCluster", cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<InterfaceDB> interfaces;

	@OneToMany(mappedBy = "interfaceCluster", cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<InterfaceClusterScoreDB> interfaceClusterScores;

	@ManyToOne
	@JsonBackReference
	private PdbInfoDB pdbInfo;
	
	public InterfaceClusterDB() {
		this.interfaceClusterScores = new ArrayList<InterfaceClusterScoreDB>();
		this.assemblies = new HashSet<AssemblyDB>();
		this.interfaces = new ArrayList<InterfaceDB>();
	}

	/**
	 * Returns the InterfaceClusterScoreDB corresponding to the given method or null
	 * if no InterfaceClusterScoreDB exists for the method
	 * @param method
	 * @return
	 */
	public InterfaceClusterScoreDB getInterfaceClusterScore(String method) {
		
		for (InterfaceClusterScoreDB ics:interfaceClusterScores) {
			if (ics.getMethod().equals(method)) return ics;				
		}
		return null;
	}
	
	public void addAssembly(AssemblyDB assembly) {
		assemblies.add(assembly);
	}
	
	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getClusterId() {
		return clusterId;
	}

	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}

	public String getPdbCode() {
		return pdbCode;
	}

	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
	}

	public double getAvgArea() {
		return avgArea;
	}

	public void setAvgArea(double avgArea) {
		this.avgArea = avgArea;
	}
	
	public double getAvgContactOverlapScore() {
		return avgContactOverlapScore;
	}

	public void setAvgContactOverlapScore(double avgContactOverlapScore) {
		this.avgContactOverlapScore = avgContactOverlapScore;
	}

	public boolean isInfinite() {
		return infinite;
	}

	public void setInfinite(boolean infinite) {
		this.infinite = infinite;
	}

	public boolean isIsologous() {
		return isologous;
	}

	public void setIsologous(boolean isologous) {
		this.isologous = isologous;
	}

	public int getNumMembers() {
		return numMembers;
	}

	public void setNumMembers(int numMembers) {
		this.numMembers = numMembers;
	}

	public int getGlobalInterfClusterId() {
		return globalInterfClusterId;
	}

	public void setGlobalInterfClusterId(int globalInterfClusterId) {
		this.globalInterfClusterId = globalInterfClusterId;
	}

	public Set<AssemblyDB> getAssemblies() {
		return assemblies;
	}

	public void setAssemblies(Set<AssemblyDB> assemblies) {
		this.assemblies = assemblies;
	}

	public List<InterfaceDB> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<InterfaceDB> interfaces) {
		this.interfaces = interfaces;
	}

	public List<InterfaceClusterScoreDB> getInterfaceClusterScores() {
		return interfaceClusterScores;
	}

	public void setInterfaceClusterScores(List<InterfaceClusterScoreDB> interfaceClusterScores) {
		this.interfaceClusterScores = interfaceClusterScores;
	}
	
	public void addInterfaceClusterScore(InterfaceClusterScoreDB interfaceClusterScore) {
		this.interfaceClusterScores.add(interfaceClusterScore);
	}

	public PdbInfoDB getPdbInfo() {
		return pdbInfo;
	}

	public void setPdbInfo(PdbInfoDB pdbInfo) {
		this.pdbInfo = pdbInfo;
	}

	public int size() {
		return this.interfaces.size();
	}
}
