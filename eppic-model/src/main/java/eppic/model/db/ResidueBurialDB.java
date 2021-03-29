package eppic.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;
import java.io.Serializable;

public class ResidueBurialDB implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final short OTHER = -1;
	public static final short SURFACE = 0;
	public static final short RIM_EVOLUTIONARY = 1;
	public static final short CORE_EVOLUTIONARY = 2;
	public static final short CORE_GEOMETRY = 3;

	private boolean side; // 0 or 1 for 1st or 2nd partner of interface (used to be 1 or 2)
	// note this is by orders of magnitude the largest table. It is critical to save space using the shortest possible types. Switch double->float Jan 2021
	private float asa;
	private float bsa;

	// this translates to a smallint in mysql
	private short region; // one of the constants above

	@JsonBackReference(value = "residueBurials-ref")
	private InterfaceDB interfaceItem;

	private ResidueInfoDB residueInfo;
	
	public ResidueBurialDB() {
		
	}


	public double getAsa() {
		return asa;
	}

	public void setAsa(double asa) {
		this.asa = (float)asa;
	}

	public double getBsa() {
		return bsa;
	}

	public void setBsa(double bsa) {
		this.bsa = (float)bsa;
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

	public ResidueInfoDB getResidueInfo() {
		return residueInfo;
	}


	public void setResidueInfo(ResidueInfoDB residueInfo) {
		this.residueInfo = residueInfo;
	}

}
