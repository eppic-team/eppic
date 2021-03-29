package eppic.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InterfaceDB implements Serializable {
	
	
	private static final long serialVersionUID = 1L;

	private String pdbCode;

	private int interfaceId;
	private int clusterId;
	
	private double area;
	private String chain1;
	private String chain2;

	private String operator;
	
	/**
	 * The types are the short names of org.biojava.bio.structure.xtal.TransformType enum
	 */
	private String operatorType;
	
	private boolean infinite;
	
	private boolean isologous;
	
	/**
	 * The contact overlap score of the interface with itself, used to call the interface isologous or heterologous.
	 */
	private double selfContactOverlapScore;
	
	/**
	 * Whether first chain is a protein
	 */
	private boolean prot1;
	
	/**
	 * Whether second chain is a protein
	 */
	private boolean prot2;
	
	private int operatorId;
	private int xtalTrans_x;
	private int xtalTrans_y;
	private int xtalTrans_z;
	
	private int globalInterfClusterId;

	@JsonManagedReference
	private List<InterfaceWarningDB> interfaceWarnings;

	@JsonManagedReference
	private List<InterfaceScoreDB> interfaceScores;
	@JsonManagedReference
	private List<ResidueBurialDB> residueBurials;
	@JsonManagedReference
	private List<ContactDB> contacts;

	@JsonBackReference
	private InterfaceClusterDB interfaceCluster;

	public InterfaceDB() {
		interfaceScores = new ArrayList<InterfaceScoreDB>();
		interfaceWarnings = new ArrayList<InterfaceWarningDB>();
		residueBurials = new ArrayList<ResidueBurialDB>();

	}
	
	/**
	 * Returns the first InterfaceScoreDB corresponding to the given method or null if no 
	 * InterfaceScoreDB exists for the method. 
	 * Note if multiple InterfaceScoreDB correspond to the method, only the first one is returned.
	 * @param method
	 * @return
	 */
	public InterfaceScoreDB getInterfaceScore(String method) {
		for (InterfaceScoreDB is:interfaceScores) {
			if (is.getMethod().equals(method)) return is;
		}
		return null;
	}

	
	public String getPdbCode() {
		return pdbCode;
	}

	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
	}

	public int getInterfaceId() {
		return interfaceId;
	}

	public void setInterfaceId(int interfaceId) {
		this.interfaceId = interfaceId;
	}

	public int getClusterId() {
		return clusterId;
	}
	
	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}
	
	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	public String getChain1() {
		return chain1;
	}

	public void setChain1(String chain1) {
		this.chain1 = chain1;
	}
	
	public String getChain2() {
		return chain2;
	}

	public void setChain2(String chain2) {
		this.chain2 = chain2;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getOperator() {
		return operator;
	}
	
	public void setOperatorType(String operatorType) {
		this.operatorType = operatorType;
	}
	
	public String getOperatorType() {
		return operatorType;
	}
	
	public void setInfinite(boolean infinite) {
		this.infinite = infinite;
	}
	
	public boolean isInfinite() {
		return infinite;
	}
	
	public boolean isIsologous() {
		return isologous;
	}

	public void setIsologous(boolean isologous) {
		this.isologous = isologous;
	}
	
	public boolean isProt1() {
		return prot1;
	}
	
	public void setProt1(boolean prot1) {
		this.prot1 = prot1;
	}
	
	public boolean isProt2() {
		return prot2;
	}

	public void setProt2(boolean prot2) {
		this.prot2 = prot2;
	}

	public int getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(int operatorId) {
		this.operatorId = operatorId;
	}

	public int getXtalTrans_x() {
		return xtalTrans_x;
	}

	public void setXtalTrans_x(int xtalTrans_x) {
		this.xtalTrans_x = xtalTrans_x;
	}

	public int getXtalTrans_y() {
		return xtalTrans_y;
	}

	public void setXtalTrans_y(int xtalTrans_y) {
		this.xtalTrans_y = xtalTrans_y;
	}

	public int getXtalTrans_z() {
		return xtalTrans_z;
	}

	public void setXtalTrans_z(int xtalTrans_z) {
		this.xtalTrans_z = xtalTrans_z;
	}

	public int getGlobalInterfClusterId() {
		return globalInterfClusterId;
	}

	public void setGlobalInterfClusterId(int globalInterfClusterId) {
		this.globalInterfClusterId = globalInterfClusterId;
	}

	public List<InterfaceWarningDB> getInterfaceWarnings() {
		return interfaceWarnings;
	}
	
	public void setInterfaceWarnings(List<InterfaceWarningDB> interfaceWarnings) {
		this.interfaceWarnings = interfaceWarnings;
	}

	public List<InterfaceScoreDB> getInterfaceScores() {
		return this.interfaceScores;
	}

	public void setInterfaceScores(List<InterfaceScoreDB> interfaceScores) {
		this.interfaceScores = interfaceScores;	
	}
	
	public void addInterfaceScore(InterfaceScoreDB interfaceScore) {
		this.interfaceScores.add(interfaceScore);
	}

	public void setInterfaceCluster(InterfaceClusterDB interfaceCluster) {
		this.interfaceCluster = interfaceCluster;
	}

	public InterfaceClusterDB getInterfaceCluster() {
		return interfaceCluster;
	}

	public List<ResidueBurialDB> getResidueBurials() {
		return residueBurials;
	}

	public void setResidueBurials(List<ResidueBurialDB> interfaceResidues) {
		this.residueBurials = interfaceResidues;
	}

	public List<ContactDB> getContacts() {
		return contacts;
	}

	public void setContacts(List<ContactDB> contacts) {
		this.contacts = contacts;
	}

	/**
	 * @return the selfContactOverlapScore
	 */
	public double getSelfContactOverlapScore() {
		return selfContactOverlapScore;
	}

	/**
	 * @param selfContactOverlapScore the selfContactOverlapScore to set
	 */
	public void setSelfContactOverlapScore(double selfContactOverlapScore) {
		this.selfContactOverlapScore = selfContactOverlapScore;
	}
}
