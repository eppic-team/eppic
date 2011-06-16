package model;

import java.io.Serializable;
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
	private int numHomologs1;
	private int numHomologs2;
	private int size1;
	private int size2;
	private String finalCall;
	private String operator; 
	
	private List<String> warnings;
	private String callReason;
	
//	private List<InterfaceStructureItem> interfaceStructures;

	public InterfaceItem()
	{
		
	}
	
	public InterfaceItem(PDBScoreItem pdbScoreItem)
	{
//		this.setPdbScoreItem(pdbScoreItem);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getNumHomologs1() {
		return numHomologs1;
	}

	public void setNumHomologs1(int numHomologs1) {
		this.numHomologs1 = numHomologs1;
	}

	public int getNumHomologs2() {
		return numHomologs2;
	}

	public void setNumHomologs2(int numHomologs2) {
		this.numHomologs2 = numHomologs2;
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

	public String getCallReason() {
		return callReason;
	}
	
	public void setCallReason(String callReason) {
		this.callReason = callReason;
	}

}
