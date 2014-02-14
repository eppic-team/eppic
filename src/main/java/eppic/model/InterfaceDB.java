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
	private List<InterfaceWarningDB> interfaceWarnings;
	
	private List<InterfaceScoreDB> interfaceScores;
	private List<ResidueDB> residues;
	
	private PdbInfoDB pdbInfo;

	public InterfaceDB() {
		interfaceScores = new ArrayList<InterfaceScoreDB>();
		interfaceWarnings = new ArrayList<InterfaceWarningDB>();
		residues = new ArrayList<ResidueDB>();

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

	public void setPdbInfo(PdbInfoDB pdbInfo) {
		this.pdbInfo = pdbInfo;
	}

	public PdbInfoDB getPdbInfo() {
		return pdbInfo;
	}

	public List<ResidueDB> getResidues() {
		return residues;
	}

	public void setResidues(List<ResidueDB> interfaceResidues) {
		this.residues = interfaceResidues;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}
}
