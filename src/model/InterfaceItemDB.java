package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InterfaceItemDB implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int uid;
	
	private int id;
	private Double area;
	private String chain1;
	private String chain2;
	private int size1;
	private int size2;
	private Double asaC1;
	private Double asaC2;
	private Double bsaC1;
	private Double bsaC2;
	private Double asaR1;
	private Double asaR2;
	private Double bsaR1;
	private Double bsaR2;
	private String finalCallName;
	private String finalCallReason;
	private String operator; 
	// the types are the short names of the owl.core.structure.TransformType enum
	private String operatorType;
	private boolean isInfinite;
	private List<WarningItemDB> warnings;
	
	private List<InterfaceScoreItemDB> interfaceScores;
	private List<InterfaceResidueItemDB> interfaceResidues;
	
	private PDBScoreItemDB pdbScoreItem;

	public InterfaceItemDB()
	{
		interfaceScores = new ArrayList<InterfaceScoreItemDB>();
		warnings = new ArrayList<WarningItemDB>();
		interfaceResidues = new ArrayList<InterfaceResidueItemDB>();
	}
	

	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Double getArea() {
		return area;
	}

	public void setArea(Double area) {
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

	public int getSize1() {
		return size1;
	}

	public void setSize1(int size1) {
		this.size1 = size1;
	}

	public int getSize2() {
		return size2;
	}

	public void setSize2(int size2) {
		this.size2 = size2;
	}

	public void setFinalCallName(String finalCallName) {
		this.finalCallName = finalCallName;
	}

	public String getFinalCallName() {
		return finalCallName;
	}
	
	public void setFinalCallReason(String finalCallReason) {
		this.finalCallReason = finalCallReason;
	}
	
	public String getFinalCallReason() {
		return finalCallReason;
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
	
	public List<WarningItemDB> getWarnings() {
		return warnings;
	}
	
	public void setWarnings(List<WarningItemDB> warnings) {
		this.warnings = warnings;
	}

	public List<InterfaceScoreItemDB> getInterfaceScores() {
		return this.interfaceScores;
	}

	public void setInterfaceScores(List<InterfaceScoreItemDB> interfaceScores) {
		this.interfaceScores = interfaceScores;	
	}
	
	public void addInterfaceScore(InterfaceScoreItemDB interfaceScore) {
		this.interfaceScores.add(interfaceScore);
	}

	public void setPdbScoreItem(PDBScoreItemDB pdbScoreItem) {
		this.pdbScoreItem = pdbScoreItem;
	}

	public PDBScoreItemDB getPdbScoreItem() {
		return pdbScoreItem;
	}

	public List<InterfaceResidueItemDB> getInterfaceResidues() {
		return interfaceResidues;
	}

	public void setInterfaceResidues(List<InterfaceResidueItemDB> interfaceResidues) {
		this.interfaceResidues = interfaceResidues;
	}

	public Double getAsaC1() {
		return asaC1;
	}

	public void setAsaC1(Double asaC1) {
		this.asaC1 = asaC1;
	}

	public Double getAsaC2() {
		return asaC2;
	}

	public void setAsaC2(Double asaC2) {
		this.asaC2 = asaC2;
	}

	public Double getBsaC1() {
		return bsaC1;
	}

	public void setBsaC1(Double bsaC1) {
		this.bsaC1 = bsaC1;
	}

	public Double getBsaC2() {
		return bsaC2;
	}

	public void setBsaC2(Double bsaC2) {
		this.bsaC2 = bsaC2;
	}

	public Double getAsaR1() {
		return asaR1;
	}

	public void setAsaR1(Double asaR1) {
		this.asaR1 = asaR1;
	}

	public Double getAsaR2() {
		return asaR2;
	}

	public void setAsaR2(Double asaR2) {
		this.asaR2 = asaR2;
	}

	public Double getBsaR1() {
		return bsaR1;
	}

	public void setBsaR1(Double bsaR1) {
		this.bsaR1 = bsaR1;
	}

	public Double getBsaR2() {
		return bsaR2;
	}

	public void setBsaR2(Double bsaR2) {
		this.bsaR2 = bsaR2;
	}

	
	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}
}
