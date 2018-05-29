package eppic.model.dto;

import java.io.Serializable;

import eppic.model.db.ContactDB;
import eppic.model.db.InterfaceDB;

public class Contact implements Serializable {

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
	
	private boolean isClash;
	
	private int numHBonds;
	private boolean isDisulfide;
	
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
		return isClash;
	}

	public void setClash(boolean isClash) {
		this.isClash = isClash;
	}

	public int getNumHBonds() {
		return numHBonds;
	}

	public void setNumHBonds(int nHBonds) {
		this.numHBonds = nHBonds;
	}

	public boolean isDisulfide() {
		return isDisulfide;
	}

	public void setDisulfide(boolean isDisulfide) {
		this.isDisulfide = isDisulfide;
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

	public static Contact create(ContactDB contactDB) {
		
		Contact contact = new Contact();
		contact.setFirstResNumber(contactDB.getFirstResNumber());
		contact.setFirstResType(contactDB.getFirstResType());
		contact.setFirstBurial(contactDB.getFirstBurial());
		contact.setSecondResNumber(contactDB.getSecondResNumber());
		contact.setSecondResType(contactDB.getSecondResType());
		contact.setSecondBurial(contactDB.getSecondBurial());
		
		contact.setMinDistance(contactDB.getMinDistance());
		contact.setNumAtoms(contactDB.getNumAtoms());
		contact.setNumHBonds(contactDB.getNumHBonds());
		contact.setClash(contactDB.isClash());
		contact.setDisulfide(contactDB.isDisulfide());

		contact.setInterfaceId(contactDB.getInterfaceId()); 		
		contact.setPdbCode(contactDB.getPdbCode()); 
		
		contact.setInterfaceItem(contactDB.getInterfaceItem()); 
		
		contact.setUid(contactDB.getUid());
		
		return contact;
	}
}
