package eppic.model.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import eppic.model.db.InterfaceDB;
import eppic.model.db.ResidueBurialDB;
import eppic.model.db.InterfaceScoreDB;
import eppic.model.db.InterfaceWarningDB;

/**
 * DTO class for Interface.
 * @author AS
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Interface implements Serializable, Comparable<Interface> 
{

	private static final long serialVersionUID = 1L;

	private int uid;
	
	private int interfaceId;
	private int clusterId;
	
	private double area;
	private String chain1;
	private String chain2;

	private String operator; 
	// The types are the short names of {@link org.biojava.bio.structure.xtal.TransformType} enum
	private String operatorType;
	
	private boolean infinite;
	
	private boolean isologous;
	
	private double selfContactOverlapScore;
	
	private boolean prot1;
	private boolean prot2;
	
	private int operatorId;
	private int xtalTrans_x;
	private int xtalTrans_y;
	private int xtalTrans_z; 
	
	private int globalInterfClusterId;
	
	private List<InterfaceWarning> interfaceWarnings;
	
	//things from the interface Score table
	@XmlElementWrapper(name = "interfaceScores")
	@XmlElement(name = "interfaceScore")
	private List<InterfaceScore> interfaceScores;

	@XmlElementWrapper(name = "residues")
	@XmlElement(name = "residue")
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
	
	public boolean isInfinite() {
		return this.infinite;
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
		interfaceItem.setInfinite(interfaceDB.isInfinite());
		interfaceItem.setIsologous(interfaceDB.isIsologous());
		interfaceItem.setProt1(interfaceDB.isProt1());
		interfaceItem.setProt2(interfaceDB.isProt2());
		interfaceItem.setOperatorId(interfaceDB.getOperatorId());
		interfaceItem.setXtalTrans_x(interfaceDB.getXtalTrans_x());
		interfaceItem.setXtalTrans_y(interfaceDB.getXtalTrans_y());
		interfaceItem.setXtalTrans_z(interfaceDB.getXtalTrans_z());
		interfaceItem.setGlobalInterfClusterId(interfaceDB.getGlobalInterfClusterId());
		interfaceItem.setSelfContactOverlapScore(interfaceDB.getSelfContactOverlapScore());
		
		if(interfaceDB.getResidueBurials() != null)
		{
			List<ResidueBurialDB> residueDBs = interfaceDB.getResidueBurials();
			
			List<Residue> residues = new ArrayList<Residue>();
			
			for(ResidueBurialDB residueDB : residueDBs)
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
