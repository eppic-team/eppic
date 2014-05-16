package eppic.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InterfaceDB implements Serializable {
	
	
	private static final long serialVersionUID = 1L;

	private int uid;
	
	private String pdbCode;
	
	private int interfaceId;
	private int clusterId;
	
	private double area;
	private String chain1;
	private String chain2;

	private String operator; 
	// the types are the short names of the owl.core.structure.TransformType enum
	private String operatorType;
	private boolean isInfinite;
	
	private int operatorId;
	private int xtalTrans_x;
	private int xtalTrans_y;
	private int xtalTrans_z;
	
	private int globalInterfClusterId;
	
	private List<InterfaceWarningDB> interfaceWarnings;
	
	private List<InterfaceScoreDB> interfaceScores;
	private List<ResidueDB> residues;
	private List<ContactDB> contacts;
	
	private InterfaceClusterDB interfaceCluster;

	public InterfaceDB() {
		interfaceScores = new ArrayList<InterfaceScoreDB>();
		interfaceWarnings = new ArrayList<InterfaceWarningDB>();
		residues = new ArrayList<ResidueDB>();

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
	
	public void setIsInfinite(boolean isInfinite) {
		this.isInfinite = isInfinite;
	}
	
	public boolean getIsInfinite() {
		return isInfinite;
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

	public List<ResidueDB> getResidues() {
		return residues;
	}

	public void setResidues(List<ResidueDB> interfaceResidues) {
		this.residues = interfaceResidues;
	}

	public List<ContactDB> getContacts() {
		return contacts;
	}

	public void setContacts(List<ContactDB> contacts) {
		this.contacts = contacts;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}
}
