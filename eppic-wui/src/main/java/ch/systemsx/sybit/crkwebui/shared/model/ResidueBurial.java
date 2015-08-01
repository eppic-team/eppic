package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import eppic.model.ResidueBurialDB;

/**
 * DTO class for ResidueBurial.
 * @author AS
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ResidueBurial implements Serializable
{

	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private boolean side;
	private double asa;
	private double bsa;
	private double bsaPercentage;
	private short region; // one of the constants above: SURFACE, RIM, CORE
	
	private ResidueInfo residueInfo;
	

	public ResidueBurial(double asa, double bsa, short region) { 
					
		this.asa = asa;
		this.bsa = bsa;
		this.bsaPercentage = bsa/asa;
		this.region = region;
	}
	
	public ResidueBurial()
	{
		
	}

	

	public double getAsa() {
		return asa;
	}

	public void setAsa(double asa) {
		this.asa = asa;
	}

	public double getBsa() {
		return bsa;
	}

	public void setBsa(double bsa) {
		this.bsa = bsa;
	}

	public double getBsaPercentage() {
		return bsaPercentage;
	}

	public void setBsaPercentage(double bsaPercentage) {
		this.bsaPercentage = bsaPercentage;
	}

	public int getRegion() {
		return this.region;
	}
	
	public void setRegion(short region) {
		this.region = region;
	}
	
	public void setSide(boolean side) {
		this.side = side;
	}

	public boolean getSide() {
		return side;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}
	
	public ResidueInfo getResidueInfo() {
		return residueInfo;
	}

	public void setResidueInfo(ResidueInfo residueInfo) {
		this.residueInfo = residueInfo;
	}

	/**
	 * Converts DB model item into DTO one.
	 * @param residueBurialDB model item to convert
	 * @return DTO representation of model item
	 */
	public static ResidueBurial create(ResidueBurialDB residueBurialDB)
	{
		ResidueBurial residue = new ResidueBurial();
		residue.setAsa(residueBurialDB.getAsa());
		residue.setRegion(residueBurialDB.getRegion());
		residue.setBsa(residueBurialDB.getBsa());
		
		residue.setBsaPercentage(residueBurialDB.getBsa() / residueBurialDB.getAsa());

		residue.setSide(residueBurialDB.getSide());
		residue.setUid(residueBurialDB.getUid());
		
		residue.setResidueInfo(ResidueInfo.create(residueBurialDB.getResidueInfo()));
		
		return residue;
	}
}
