package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import eppic.model.InterfaceDB;
import eppic.model.ResidueDB;
import eppic.model.InterfaceScoreDB;
import eppic.model.InterfaceWarningDB;

/**
 * DTO class for Interface.
 * @author AS
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Interface implements Serializable, Comparable<Interface> 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int uid;
	
	private int interfaceId;
	private int clusterId;
	
	private double area;
	private String chain1;
	private String chain2;

	private String operator; 
	// the types are the short names of the owl.core.structure.TransformType enum
	private String operatorType;
	
	private boolean isInfinite;
	private List<InterfaceWarning> interfaceWarnings;
	
	//things from the interface Score table
	@XmlElementWrapper(name = "interfaceScores")
	@XmlElement(name = "interfaceScore")
	private List<InterfaceScore> interfaceScores;
	
	private List<Residue> residues;
	
	
	
	public Interface()
	{
		interfaceScores = new ArrayList<InterfaceScore>();
		interfaceWarnings = new ArrayList<InterfaceWarning>();
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
	
	public boolean getIsInfinite() {
		return this.isInfinite;
	}
	
	public void setIsInfinite(boolean isInfinite) {
		this.isInfinite = isInfinite;
	}
	
	public List<InterfaceWarning> getInterfaceWarnings() {
		return interfaceWarnings;
	}
	
	public void setInterfaceWarnings(List<InterfaceWarning> interfaceWarnings) {
		this.interfaceWarnings = interfaceWarnings;
	}

	public List<InterfaceScore> getInterfaceScores() {
		return this.interfaceScores;
	}

	public void setInterfaceScores(List<InterfaceScore> interfaceScores) {
		this.interfaceScores = interfaceScores;	
	}
	
	public void addInterfaceScore(InterfaceScore interfaceScore) {
		this.interfaceScores.add(interfaceScore);
	}

	public List<Residue> getResidues() {
		return residues;
	}

	public void setResidues(List<Residue> residues) {
		this.residues = residues;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}
	
	/**
	 * Converts DB model item into DTO one.
	 * @param interfaceDB model item to convert
	 * @return DTO representation of model item
	 */
	public static Interface create(InterfaceDB interfaceDB)
	{
		Interface interfaceItem = new Interface();
		
		interfaceItem.setUid(interfaceDB.getUid());
		interfaceItem.setArea(interfaceDB.getArea());
		interfaceItem.setInterfaceId(interfaceDB.getInterfaceId());
		interfaceItem.setClusterId(interfaceDB.getClusterId());
		interfaceItem.setChain1(interfaceDB.getChain1());
		interfaceItem.setChain2(interfaceDB.getChain2());
		interfaceItem.setOperator(interfaceDB.getOperator());
		interfaceItem.setOperatorType(interfaceDB.getOperatorType());
		interfaceItem.setIsInfinite(interfaceDB.getIsInfinite());
		
		if(interfaceDB.getResidues() != null)
		{
			List<ResidueDB> residueDBs = interfaceDB.getResidues();
			
			List<Residue> residues = new ArrayList<Residue>();
			
			for(ResidueDB residueDB : residueDBs)
			{
				residues.add(Residue.create(residueDB));
			}
			
			interfaceItem.setResidues(residues);
		}
		
		if(interfaceDB.getInterfaceScores() != null)
		{
			List<InterfaceScoreDB> interfaceScoreDBs = interfaceDB.getInterfaceScores();
			
			List<InterfaceScore> interfaceScores = new ArrayList<InterfaceScore>();
			
			for(InterfaceScoreDB interfaceScoreDB : interfaceScoreDBs)
			{
				interfaceScores.add(InterfaceScore.create(interfaceScoreDB));
			}
			
			interfaceItem.setInterfaceScores(interfaceScores);
		}
		
		if(interfaceDB.getInterfaceWarnings() != null)
		{
			List<InterfaceWarningDB> warningItemDBs = interfaceDB.getInterfaceWarnings();
			
			List<InterfaceWarning> interfaceWarnings = new ArrayList<InterfaceWarning>();
			
			for(InterfaceWarningDB warningItemDB : warningItemDBs)
			{
				interfaceWarnings.add(InterfaceWarning.create(warningItemDB));
			}
			
			interfaceItem.setInterfaceWarnings(interfaceWarnings);
		}
		
		return interfaceItem;
	}

	@Override
	public int compareTo(Interface that) 
	{
		return this.interfaceId - that.interfaceId;
	}
}
