package eppic.db;

//import owl.core.structure.AminoAcid;

public class SimpleResidue implements Comparable<SimpleResidue> {

	//private AminoAcid aa;
	private int serial;
	
	public SimpleResidue(int serial) {
		//this.aa = aa;
		this.serial = serial;
	}
	
	//public AminoAcid getAminoAcid() {
	//	return aa;
	//}
	
	public int getSerial() {
		return serial;
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof SimpleResidue)) return false;
		
		SimpleResidue o = (SimpleResidue) other;
		
		return (this.serial==o.serial);
	}
	
	public int hashCode() {
		return new Integer(serial).hashCode();
	}

	@Override
	public int compareTo(SimpleResidue o) {
		return new Integer(serial).compareTo(o.serial);
	}
	
}
