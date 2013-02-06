/**
 * 
 */
package analysis.propensities;

import java.io.PrintWriter;
import java.lang.Math;

/**
 * @author biyani_n
 *
 */
public class ProteinFrequencies {
	public ResiduesDistribution full;
	public ResiduesDistribution surface;
	public ResiduesDistribution interfCore;
	public ResiduesDistribution chainCore;
	
	//Constructor
	public ProteinFrequencies(){
		this.full = new ResiduesDistribution();
		this.surface = new ResiduesDistribution();
		this.interfCore = new ResiduesDistribution();
		this.chainCore = new ResiduesDistribution();
	}
	
	//Add data of another FullChain
	public void addData(ProteinFrequencies tempchain){
		this.full.addData(tempchain.full);
		this.surface.addData(tempchain.surface);
		this.interfCore.addData(tempchain.interfCore);
		this.chainCore.addData(tempchain.chainCore);
	}
	
	
	//Print Methods
	public void printEnrichments(PrintWriter out){
		out.print('\n');
		out.println("# AA   CoreF w.r.tF  Ratio log2()");
		out.println("# CORE w.r.t FULL CHAIN Enrichment ");
		this.interfCore.printEnrichment(this.full,out);
		
		out.println("# CORE w.r.t SURFACE residues Enrichment");
		this.interfCore.printEnrichment(this.surface,out);
		
		out.println("# CORE w.r.t PROTEIN CORE Enrichment");
		this.interfCore.printEnrichment(this.chainCore,out);
		out.print('\n');
	}
	
	public void printPropensities(PrintWriter out){
		out.print('\n');
		out.println("# Full Protein Amino-Acid Propensities");
		this.full.printData(out);
		out.print('\n');
		out.println("# Surface Amino-Acid Propensities");
		this.surface.printData(out);
		out.print('\n');
		out.println("# Interface Core Amino-Acid Propensities");
		this.interfCore.printData(out);
		out.print('\n');
		out.println("# Chain Core Amino-Acid Propensities");
		this.chainCore.printData(out);
		out.print('\n');
	}
	
	public void printEnrichmentsTable(PrintWriter out){
		double interfcorefreq, fullfreq, surfacefreq, chaincorefreq, ratiofull, ratiosurface, ratiochaincore;
		out.println("# Enrichment of Interface Core");
		out.println("# AA     Full    Surf    Prot.Core");
		for(ResidueCount res:interfCore.resid){
			if(this.interfCore.count != 0) interfcorefreq = res.getFrequency()/this.interfCore.count;
			else interfcorefreq = 0;
			if(this.full.count != 0) fullfreq = this.full.getFrequency(res.aa)/this.full.count;
			else fullfreq = 0;
			if(this.surface.count != 0) surfacefreq = this.surface.getFrequency(res.aa)/this.surface.count;
			else surfacefreq=0;
			if(this.chainCore.count != 0) chaincorefreq = this.chainCore.getFrequency(res.aa)/this.chainCore.count;
			else chaincorefreq = 0;
			
			ratiofull = Math.log(interfcorefreq/fullfreq)/Math.log(2);
			ratiosurface = Math.log(interfcorefreq/surfacefreq)/Math.log(2);
			ratiochaincore = Math.log(interfcorefreq/chaincorefreq)/Math.log(2);
			
			out.println(String.format(" %3s  %7.3f %7.3f %7.3f", res.aa.getThreeLetterCode(), ratiofull, ratiosurface, ratiochaincore));
		}
		out.print('\n');
	}
	
	public void printPropensitiesTable(PrintWriter out){
		out.print('\n');
		out.println("# Total number of Residues encountered: " + this.full.count);
		out.println("# Total number of Surface Residues encountered: " + this.surface.count);
		out.println("# Total number of Interface Core Residues encountered: " + this.interfCore.count);
		out.println("# Total number of Protein Core Residues encountered: " + this.chainCore.count);
		out.print('\n');
		out.println("# AA     Core    Full    Surf    Prot.Core");
		for(ResidueCount res:interfCore.resid){
			double interfcorefreq = res.getFrequency()/this.interfCore.count;
			double fullfreq = this.full.getFrequency(res.aa)/this.full.count;
			double surfacefreq = this.surface.getFrequency(res.aa)/this.surface.count;
			double chaincorefreq = this.chainCore.getFrequency(res.aa)/this.chainCore.count;
			out.println(String.format(" %3s  %7.3f %7.3f %7.3f %7.3f", res.aa.getThreeLetterCode(), interfcorefreq, fullfreq, surfacefreq, chaincorefreq));
		}
		out.print('\n');
	}
	
	public void printProperties(PrintWriter out){
		String[] props = {"Hydrophobicity","Aromatic","Aliphatic","Polar","Charged","Positive","Negative","Small","Tiny"};
		double[] propFull = this.full.getProperties(out);
		double[] propSurface = this.surface.getProperties(out);
		double[] propInterfCore = this.interfCore.getProperties(out);
		double[] propChainCore = this.chainCore.getProperties(out);
		
		out.print('\n');
		out.println("# Property         Full    Surf   InCore   ProtCore");
		for(int i=0; i<9; i++)
			out.println(String.format("%15s %7.3f %7.3f %7.3f %7.3f", props[i], propFull[i], propSurface[i], propInterfCore[i], propChainCore[i]));
		out.print('\n');
	}

}
