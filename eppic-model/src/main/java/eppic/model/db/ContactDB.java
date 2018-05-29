package eppic.model.db;

import java.io.Serializable;

public class ContactDB implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private int firstResNumber;
	private int secondResNumber;
	private String firstResType;
	private String secondResType;
	
	private double firstBurial;
	private double secondBurial;
	
	private double minDistance;
	
	private int numAtoms;
	
	private boolean clash;
	
	private int numHBonds;
	private boolean disulfide;
	
	private int interfaceId;
	private String pdbCode;
	
	private InterfaceDB interfaceItem;

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getFirstResNumber() {
		return firstResNumber;
	}

	public void setFirstResNumber(int iResNumber) {
		this.firstResNumber = iResNumber;
	}

	public int getSecondResNumber() {
		return secondResNumber;
	}

	public void setSecondResNumber(int jResNumber) {
		this.secondResNumber = jResNumber;
	}

	public String getFirstResType() {
		return firstResType;
	}

	public void setFirstResType(String iResType) {
		this.firstResType = iResType;
	}

	public String getSecondResType() {
		return secondResType;
	}

	public void setSecondResType(String jResType) {
		this.secondResType = jResType;
	}

	public double getFirstBurial() {
		return firstBurial;
	}

	public void setFirstBurial(double iBurial) {
		this.firstBurial = iBurial;
	}

	public double getSecondBurial() {
		return secondBurial;
	}

	public void setSecondBurial(double jBurial) {
		this.secondBurial = jBurial;
	}

	public double getMinDistance() {
		return minDistance;
	}

	public void setMinDistance(double minDistance) {
		this.minDistance = minDistance;
	}

	public int getNumAtoms() {
		return numAtoms;
	}

	public void setNumAtoms(int nAtomsInContact) {
		this.numAtoms = nAtomsInContact;
	}

	public boolean isClash() {
		return clash;
	}

	public void setClash(boolean isClash) {
		this.clash = isClash;
	}

	public int getNumHBonds() {
		return numHBonds;
	}

	public void setNumHBonds(int nHBonds) {
		this.numHBonds = nHBonds;
	}

	public boolean isDisulfide() {
		return disulfide;
	}

	public void setDisulfide(boolean isDisulfide) {
		this.disulfide = isDisulfide;
	}

	public int getInterfaceId() {
		return interfaceId;
	}

	public void setInterfaceId(int interfaceId) {
		this.interfaceId = interfaceId;
	}

	public String getPdbCode() {
		return pdbCode;
	}

	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
	}

	public InterfaceDB getInterfaceItem() {
		return interfaceItem;
	}

	public void setInterfaceItem(InterfaceDB interfaceItem) {
		this.interfaceItem = interfaceItem;
	}

}
