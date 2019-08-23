package eppic.model.db;

import eppic.model.adapters.ResidueBurialListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "ResidueBurial")
@EntityListeners(ResidueBurialListener.class)
public class ResidueBurialDB implements Serializable {


	private static final long serialVersionUID = 1L;
	
	public static final short OTHER = -1;
	public static final short SURFACE = 0;
	public static final short RIM_EVOLUTIONARY = 1;
	public static final short CORE_EVOLUTIONARY = 2;
	public static final short CORE_GEOMETRY = 3;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int uid;
	
	private boolean side; // 0 or 1 for 1st or 2nd partner of interface (used to be 1 or 2)
	private double asa;
	private double bsa;

	// TODO the field is now a short, we should double check that it translates to a data
	//			     type that takes less space than int in mysql. Other solution would be to force type
	//			     smallint here but I don't know how to do that
	private short region; // one of the constants above

	@ManyToOne
	private InterfaceDB interfaceItem;

	@ManyToOne
	private ResidueInfoDB residueInfo;
	
	public ResidueBurialDB() {
		
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
	
	public short getRegion() {
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

	public void setInterfaceItem (InterfaceDB interfaceItem) {
		this.interfaceItem = interfaceItem;
	}

	public InterfaceDB getInterfaceItem() {
		return interfaceItem;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}
	
	public ResidueInfoDB getResidueInfo() {
		return residueInfo;
	}


	public void setResidueInfo(ResidueInfoDB residueInfo) {
		this.residueInfo = residueInfo;
	}

}
