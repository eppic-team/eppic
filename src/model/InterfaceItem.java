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
	private double asa1;
	private double asa2;
	private double bsa1;
	private double bsa2;
	private String finalCall;
	private String operator; 
	private List<String> warnings;
	
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

	public double getAsa1() {
		return asa1;
	}

	public void setAsa1(double asa1) {
		this.asa1 = asa1;
	}

	public double getAsa2() {
		return asa2;
	}

	public void setAsa2(double asa2) {
		this.asa2 = asa2;
	}

	public double getBsa1() {
		return bsa1;
	}

	public void setBsa1(double bsa1) {
		this.bsa1 = bsa1;
	}

	public double getBsa2() {
		return bsa2;
	}

	public void setBsa2(double bsa2) {
		this.bsa2 = bsa2;
	}
}
