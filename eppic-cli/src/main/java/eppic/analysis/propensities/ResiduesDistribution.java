/**
 * 
 */
package eppic.analysis.propensities;

import java.io.PrintWriter;
import java.lang.Math;

import owl.core.structure.AminoAcid;


/**
 * @author biyani_n
 *
 */
public class ResiduesDistribution{
	//Variables
	public ResidueCount[] resid = new ResidueCount[20];
	public int count;
	
	
	//------------Constructors----------------------
	/**
	 * Main Constructor initializing the frequencies of all residues to 0 and the total count to 0.
	 * @return An object of this class
	 */
	public ResiduesDistribution() {
		for(int i=0; i<20; i++) this.resid[i] = new ResidueCount(i+1);
		this.count = 0;
	}
	
	/**
	 * Copy Constructor initializing the frequencies of all residues and total count to the object passed.
	 * @return An object of this class
	 */
	public ResiduesDistribution(ResiduesDistribution tempregion) {
		this.resid = tempregion.resid;
		this.count = tempregion.count;
	}
	
	//------------Get Methods----------------------
	/**
	 * Get the Count/Frequency of a particular Amino Acid in the class
	 * @param An object of class Amino Acid
	 * @return double value
	 */
	public double getFrequency(AminoAcid aa){
		ResidueCount res = this.resid[aa.getNumber()-1];
		return res.getFrequency();
	}
	
	/**
	 * Method to get an 20 element array with frequencies of each amino acid 
	 * @return double[20] array
	 */
	public double[] getAllFrequency(){
		double[] AllFreq = new double[20];
		int i=0;
		for(ResidueCount res:this.resid){
			AllFreq[i] = res.getFrequency();
			i++;
		}
		return AllFreq;
	}
	
	public int[] getResidueIndex(){
		int[] resIndex = new int[20];
		int i=0;
		for(ResidueCount res:this.resid){
			resIndex[i] = res.aa.getNumber()-1;
			i++;
		}
		return resIndex;
	}
	
	//------------Add Data Methods----------------------
	//Add data from another ChainRegion
	public void addData(ResiduesDistribution tempregion){
		for (ResidueCount res:this.resid){
			res.count+=tempregion.getFrequency(res.aa);
		}
		this.count+=tempregion.count;
	}
	
	public void addResidue(AminoAcid aa){
		this.count++;
		this.resid[aa.getNumber()-1].count++;
	}
	
	public int[] sortData(double[] array){
		int[] sortIndex = new int[array.length];
		
		boolean[] ifDone = new boolean[array.length];
		for(int i=0; i<array.length; i++) ifDone[i]=false;
		
		double max;
		int indexMax = 0;
		for(int i=0; i<array.length; i++){
			indexMax=0;
			while(ifDone[indexMax]!=false) indexMax++;
			max = array[indexMax];
			for(int j = 0; j < (array.length); j++){
				if(array[j] > max && ifDone[j] == false ) {
					max = array[j];
					indexMax=j;
				}
			}
			sortIndex[i]=indexMax;
			ifDone[indexMax]=true;
		}
		return sortIndex;
	}
	
	//------------Print Methods----------------------
	public void printData(PrintWriter out){
		int[] sortIndex = this.sortData(this.getAllFrequency());
		out.println("#Total Number of residues encountered: " + this.count);
		
		for(int i:sortIndex){
			AminoAcid aa = AminoAcid.getByNumber(i+1);
			double thisfreq = this.getFrequency(aa)/this.count;
			out.println(String.format(" %3s %6.3f %10.0f", aa.getThreeLetterCode(), thisfreq, this.getFrequency(aa)));
		}		
	}
	
	public void printRelativeAbundance(PrintWriter out){
		int[] sortIndex = this.sortData(this.getAllFrequency());
		out.println("#Total number of chains encountered: " + this.count);
		out.println("# AA  RelAbund log2(RelAbun)");
		
		for(int i:sortIndex){
			AminoAcid aa = AminoAcid.getByNumber(i+1);
			double thisfreq = this.getFrequency(aa)/this.count;
			out.println(String.format(" %3s %8.3f %8.3f", aa.getThreeLetterCode(), thisfreq, Math.log(thisfreq)/Math.log(2.0)));
		}		
	}
	
	public void printEnrichment(ResiduesDistribution tempregion, PrintWriter out){
		double[] ratioEnrich = new double[20];
		int ii=0;
		double tempfreq, thisfreq;
		for(ResidueCount res:this.resid){
			if(tempregion.count == 0 ) tempfreq = 0;
			else tempfreq = tempregion.getFrequency(res.aa)/tempregion.count;
			if(this.count != 0) thisfreq = res.count/this.count;
			else thisfreq =0;
			ratioEnrich[ii] = thisfreq/tempfreq;
			ii++;
		}
		
		int[] sortIndex = this.sortData(ratioEnrich);
		out.print('\n');
		for(int i:sortIndex){
			AminoAcid aa = AminoAcid.getByNumber(i+1);
			if(tempregion.count == 0 ) tempfreq = 0;
			else tempfreq = tempregion.getFrequency(aa)/tempregion.count;
			if(this.count == 0) thisfreq = 0;
			else thisfreq = this.getFrequency(aa)/this.count;
			if(ratioEnrich[i] > 1){
				out.println(String.format(" %3s  %6.3f %6.3f %6.3f %6.3f", aa.getThreeLetterCode(), thisfreq, tempfreq, ratioEnrich[i], Math.log(ratioEnrich[i])/Math.log(2) ) );
			}		
		}
		out.print('\n');
	}
	
	public double[] getProperties(PrintWriter out){
		double[] prop = {0,0,0,0,0,0,0,0,0};
		for(ResidueCount res:this.resid){
			if(res.aa.isHydrophobic()) prop[0]+= res.getFrequency();
			if(res.aa.isAromatic()) prop[1]+= res.getFrequency();
			if(res.aa.isAliphatic()) prop[2]+= res.getFrequency();
			if(res.aa.isPolar()) prop[3]+= res.getFrequency();
			if(res.aa.isCharged()) prop[4]+= res.getFrequency();
			if(res.aa.isPositive()) prop[5]+= res.getFrequency();
			if(res.aa.isNegative()) prop[6]+= res.getFrequency();
			if(res.aa.isSmall()) prop[7]+= res.getFrequency();
			if(res.aa.isTiny()) prop[8]+= res.getFrequency();
		}
		
		for(int i=0; i<prop.length; i++){
			if(this.count != 0) prop[i] = prop[i]/this.count;
			else prop[i]=0;
		}
		return prop;
	}

}
