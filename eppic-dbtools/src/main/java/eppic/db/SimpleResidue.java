package eppic.db;

//import owl.core.structure.AminoAcid;

/**
 * Simple object to store residue indices
 */
public class SimpleResidue implements Comparable<SimpleResidue> {

	//private AminoAcid aa;
	private int serial;
	
	/**
	 * @param serial 1-based index of the residue
	 */
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Integer.toString(serial);
	}
	
	
}
