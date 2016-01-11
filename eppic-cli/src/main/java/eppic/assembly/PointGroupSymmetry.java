package eppic.assembly;

/**
 * A string representation of a point group symmetry: Cn, Dn, T, O, I
 * 
 * @author Jose Duarte
 *
 */
public final class PointGroupSymmetry {

	public static final String UNKNOWN =  "unknown";
	
	private char type;
	private int mult;
	
	
	public PointGroupSymmetry(char type, int mult) {
		this.type = type;
		this.mult = mult;
	}
	
	public char getType() {
		return type;
	}
	
	public int getMultiplicity() {
		return mult;
	}
	
	public boolean isCyclic() {
		return type == 'C';
	}
	
	public String toString() {
		
		if (type == 'C' || type == 'D') 			
			return "" + type + mult;
		
		else
			return ""+type; // T, I, O
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mult;
		result = prime * result + type;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PointGroupSymmetry other = (PointGroupSymmetry) obj;
		if (mult != other.mult)
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}
