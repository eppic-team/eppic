/**
 * 
 */
package analysis.propensities;

import owl.core.structure.AminoAcid;

/**
 * @author biyani_n
 *
 */
public class ResidueCount implements Comparable<ResidueCount> {
	public AminoAcid aa;
	public double count;
	
	//Constructor
	public ResidueCount(int aanum){
		this.aa = AminoAcid.getByNumber(aanum);
		this.count = 0;
	}
	
	public ResidueCount(AminoAcid aa){
		this.aa = aa;
		this.count = 0;
	}
	
	public double getFrequency(){
		return this.count;
	}

	@Override
	public int compareTo(ResidueCount o) {
		int result;
		if (this.count > o.count) result = 1;
		else if (this.count < o.count) result = -1;
		else result = 0;
		return result;
	}
}
