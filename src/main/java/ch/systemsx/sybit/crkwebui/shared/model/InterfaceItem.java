package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import model.InterfaceItemDB;
import model.InterfaceResidueItemDB;
import model.InterfaceScoreItemDB;
import model.PdbBioUnitAssignmentItemDB;
import model.WarningItemDB;

/**
 * DTO class for Interface item.
 * @author AS
 */
public class InterfaceItem implements Serializable, Comparable<InterfaceItem> 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int uid;
	
	private int id;
	private int clusterId;
	private double area;
	private String chain1;
	private String chain2;
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
	private String finalCallName;
	private String finalCallReason;
	private String operator; 
	// the types are the short names of the owl.core.structure.TransformType enum
	private String operatorType;
	private boolean isInfinite;
	private List<WarningItem> warnings;
	
	private List<InterfaceScoreItem> interfaceScores;
	private List<InterfaceResidueItem> interfaceResidues;
	private List<PdbBioUnitAssignmentItem> bioUnitAssignments; 
	
	public InterfaceItem()
	{
		interfaceScores = new ArrayList<InterfaceScoreItem>();
		warnings = new ArrayList<WarningItem>();
		bioUnitAssignments = new ArrayList<PdbBioUnitAssignmentItem>();
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
	
	public boolean getIsInfinite() {
		return this.isInfinite;
	}
	
	public void setIsInfinite(boolean isInfinite) {
		this.isInfinite = isInfinite;
	}
	
	public List<WarningItem> getWarnings() {
		return warnings;
	}
	
	public void setWarnings(List<WarningItem> warnings) {
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

	public List<PdbBioUnitAssignmentItem> getBioUnitAssignments() {
		return bioUnitAssignments;
	}

	public void setBioUnitAssignments(
			List<PdbBioUnitAssignmentItem> bioUnitAssignments) {
		this.bioUnitAssignments = bioUnitAssignments;
	}
	
	public void addBioUnitAssignment(PdbBioUnitAssignmentItem bioUnitAssignmentItem){
		this.bioUnitAssignments.add(bioUnitAssignmentItem);
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

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}
	
	/**
	 * Converts DB model item into DTO one.
	 * @param interfaceItemDB model item to convert
	 * @return DTO representation of model item
	 */
	public static InterfaceItem create(InterfaceItemDB interfaceItemDB)
	{
		InterfaceItem interfaceItem = new InterfaceItem();
		interfaceItem.setArea(interfaceItemDB.getArea());
		interfaceItem.setAsaC1(interfaceItemDB.getAsaC1());
		interfaceItem.setAsaC2(interfaceItemDB.getAsaC2());
		interfaceItem.setAsaR1(interfaceItemDB.getAsaR1());
		interfaceItem.setAsaR2(interfaceItemDB.getAsaR2());
		interfaceItem.setBsaC1(interfaceItemDB.getBsaC1());
		interfaceItem.setBsaC2(interfaceItemDB.getBsaC2());
		interfaceItem.setBsaR1(interfaceItemDB.getBsaR1());
		interfaceItem.setBsaR2(interfaceItemDB.getBsaR2());
		interfaceItem.setFinalCallName(interfaceItemDB.getFinalCallName());
		interfaceItem.setFinalCallReason(interfaceItemDB.getFinalCallReason());
		interfaceItem.setId(interfaceItemDB.getId());
		interfaceItem.setClusterId(interfaceItemDB.getClusterId());
		
		if(interfaceItemDB.getInterfaceResidues() != null)
		{
			List<InterfaceResidueItemDB> interfaceResidueItemDBs = interfaceItemDB.getInterfaceResidues();
			
			List<InterfaceResidueItem> interfaceResidueItems = new ArrayList<InterfaceResidueItem>();
			
			for(InterfaceResidueItemDB interfaceResidueItemDB : interfaceResidueItemDBs)
			{
				interfaceResidueItems.add(InterfaceResidueItem.create(interfaceResidueItemDB));
			}
			
			interfaceItem.setInterfaceResidues(interfaceResidueItems);
		}
		
		if(interfaceItemDB.getInterfaceScores() != null)
		{
			List<InterfaceScoreItemDB> interfaceScoreItemDBs = interfaceItemDB.getInterfaceScores();
			
			List<InterfaceScoreItem> interfaceScoreItems = new ArrayList<InterfaceScoreItem>();
			
			for(InterfaceScoreItemDB interfaceScoreItemDB : interfaceScoreItemDBs)
			{
				interfaceScoreItems.add(InterfaceScoreItem.create(interfaceScoreItemDB));
			}
			
			interfaceItem.setInterfaceScores(interfaceScoreItems);
		}
		
		if(interfaceItemDB.getBioUnitAssignments() != null)
		{
			List<PdbBioUnitAssignmentItemDB> bioUnitAssignmentItemDBs = interfaceItemDB.getBioUnitAssignments();
			
			List<PdbBioUnitAssignmentItem> bioUnitAssignmentItems = new ArrayList<PdbBioUnitAssignmentItem>();
			
			for(PdbBioUnitAssignmentItemDB bioUnitAssignmentItemDB:bioUnitAssignmentItemDBs)
			{
				bioUnitAssignmentItems.add(PdbBioUnitAssignmentItem.create(bioUnitAssignmentItemDB));				
			}
			
			interfaceItem.setBioUnitAssignments(bioUnitAssignmentItems);
		}
		
		interfaceItem.setChain1(interfaceItemDB.getChain1());
		interfaceItem.setChain2(interfaceItemDB.getChain2());
		interfaceItem.setOperator(interfaceItemDB.getOperator());
		interfaceItem.setOperatorType(interfaceItemDB.getOperatorType());
		interfaceItem.setIsInfinite(interfaceItemDB.getIsInfinite());
		interfaceItem.setSize1(interfaceItemDB.getSize1());
		interfaceItem.setSize2(interfaceItemDB.getSize2());
		interfaceItem.setUid(interfaceItemDB.getUid());
		
		if(interfaceItemDB.getWarnings() != null)
		{
			List<WarningItemDB> warningItemDBs = interfaceItemDB.getWarnings();
			
			List<WarningItem> warningItems = new ArrayList<WarningItem>();
			
			for(WarningItemDB warningItemDB : warningItemDBs)
			{
				warningItems.add(WarningItem.create(warningItemDB));
			}
			
			interfaceItem.setWarnings(warningItems);
		}
		
		return interfaceItem;
	}

	@Override
	public int compareTo(InterfaceItem that) 
	{
		return this.id - that.id;
	}
}
