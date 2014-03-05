package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import eppic.model.ContactDB;
import eppic.model.InterfaceDB;

public class Contact implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private int iResNumber;
	private int jResNumber;
	private char iResType;
	private char jResType;
	
	private double iBurial;
	private double jBurial;
	
	private int nAtomsInContact;
	
	private boolean isClash;
	
	private int nHBonds;
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

	public int getiResNumber() {
		return iResNumber;
	}

	public void setiResNumber(int iResNumber) {
		this.iResNumber = iResNumber;
	}

	public int getjResNumber() {
		return jResNumber;
	}

	public void setjResNumber(int jResNumber) {
		this.jResNumber = jResNumber;
	}

	public char getiResType() {
		return iResType;
	}

	public void setiResType(char iResType) {
		this.iResType = iResType;
	}

	public char getjResType() {
		return jResType;
	}

	public void setjResType(char jResType) {
		this.jResType = jResType;
	}

	public double getiBurial() {
		return iBurial;
	}

	public void setiBurial(double iBurial) {
		this.iBurial = iBurial;
	}

	public double getjBurial() {
		return jBurial;
	}

	public void setjBurial(double jBurial) {
		this.jBurial = jBurial;
	}

	public int getnAtomsInContact() {
		return nAtomsInContact;
	}

	public void setnAtomsInContact(int nAtomsInContact) {
		this.nAtomsInContact = nAtomsInContact;
	}

	public boolean isClash() {
		return isClash;
	}

	public void setClash(boolean isClash) {
		this.isClash = isClash;
	}

	public int getnHBonds() {
		return nHBonds;
	}

	public void setnHBonds(int nHBonds) {
		this.nHBonds = nHBonds;
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
		contact.setiResNumber(contactDB.getiResNumber());
		contact.setiResType(contactDB.getiResType());
		contact.setiBurial(contactDB.getiBurial());
		contact.setjResNumber(contactDB.getjResNumber());
		contact.setjResType(contactDB.getjResType());
		contact.setjBurial(contactDB.getjBurial());
		
		contact.setnAtomsInContact(contactDB.getnAtomsInContact());
		contact.setnHBonds(contactDB.getnHBonds());
		contact.setClash(contactDB.isClash());
		contact.setDisulfide(contactDB.isDisulfide());

		contact.setInterfaceId(contactDB.getInterfaceId()); 		
		contact.setPdbCode(contactDB.getPdbCode()); 
		
		contact.setInterfaceItem(contactDB.getInterfaceItem()); 
		
		contact.setUid(contactDB.getUid());
		
		return contact;
	}
}
