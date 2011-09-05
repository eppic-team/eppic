package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InterfaceItem implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int id;
	private double area;
	private String name;
	private int size1;
	private int size2;
	private double asaC1;
	private double asaC2;
	private double bsaC1;
	private double bsaC2;
	private double asaR1;
	private double asaR2;
	private double bsaR1;
	private double bsaR2;
	private String finalCall;
	private String operator; 
	private List<String> warnings;
	
	private String jmolScript;
	
	private List<InterfaceScoreItem> interfaceScores;
	private List<InterfaceResidueItem> interfaceResidues;
	
	private PDBScoreItem pdbScoreItem;

	public InterfaceItem()
	{
		interfaceScores = new ArrayList<InterfaceScoreItem>();
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

//	public void setInterfaceStructures(List<InterfaceStructureItem> interfaceStructures) {
//		this.interfaceStructures = interfaceStructures;
//	}
//
//	public List<InterfaceStructureItem> getInterfaceStructures() {
//		return interfaceStructures;
//	}
	
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

	public void setFinalCall(String finalCall) {
		this.finalCall = finalCall;
	}

	public String getFinalCall() {
		return finalCall;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getOperator() {
		return operator;
	}
	
	public List<String> getWarnings() {
		return warnings;
	}
	
	public void setWarnings(List<String> warnings) {
		this.warnings = warnings;
	}

	public String getJmolScript() {
		return jmolScript;
	}
	
	public void setJmolScript(String jmolScript) {
		this.jmolScript = jmolScript;
	}
	
	public List<InterfaceScoreItem> getInterfaceScores() {
		return this.interfaceScores;
	}

	public void setInterfaceScores(List<InterfaceScoreItem> interfaceScores) {
		this.interfaceScores = interfaceScores;	
	}
	
	public void addInterfaceScore(InterfaceScoreItem interfaceScore) {
		this.interfaceScores.add(interfaceScore);
	}

	public void setInterfaceStructures(List<InterfaceResidueItem> interfaceResidues) {
		this.interfaceResidues = interfaceResidues;
	}

	public List<InterfaceResidueItem> getInterfaceStructures() {
		return interfaceResidues;
	}

	public void setPdbScoreItem(PDBScoreItem pdbScoreItem) {
		this.pdbScoreItem = pdbScoreItem;
	}

	public PDBScoreItem getPdbScoreItem() {
		return pdbScoreItem;
	}

	public List<InterfaceResidueItem> getInterfaceResidues() {
		return interfaceResidues;
	}

	public void setInterfaceResidues(List<InterfaceResidueItem> interfaceResidues) {
		this.interfaceResidues = interfaceResidues;
	}

	public double getAsaC1() {
		return asaC1;
	}

	public void setAsaC1(double asaC1) {
		this.asaC1 = asaC1;
	}

	public double getAsaC2() {
		return asaC2;
	}

	public void setAsaC2(double asaC2) {
		this.asaC2 = asaC2;
	}

	public double getBsaC1() {
		return bsaC1;
	}

	public void setBsaC1(double bsaC1) {
		this.bsaC1 = bsaC1;
	}

	public double getBsaC2() {
		return bsaC2;
	}

	public void setBsaC2(double bsaC2) {
		this.bsaC2 = bsaC2;
	}

	public double getAsaR1() {
		return asaR1;
	}

	public void setAsaR1(double asaR1) {
		this.asaR1 = asaR1;
	}

	public double getAsaR2() {
		return asaR2;
	}

	public void setAsaR2(double asaR2) {
		this.asaR2 = asaR2;
	}

	public double getBsaR1() {
		return bsaR1;
	}

	public void setBsaR1(double bsaR1) {
		this.bsaR1 = bsaR1;
	}

	public double getBsaR2() {
		return bsaR2;
	}

	public void setBsaR2(double bsaR2) {
		this.bsaR2 = bsaR2;
	}

}
