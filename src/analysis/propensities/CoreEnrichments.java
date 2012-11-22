/**
 * 
 */
package analysis.propensities;

import java.io.PrintWriter;


/**
 * @author biyani_n
 *
 */
public class CoreEnrichments {
	public ResiduesDistribution fullEnrich;
	public ResiduesDistribution surfaceEnrich;
	
	//Constructor
	public CoreEnrichments(){
		this.fullEnrich = new ResiduesDistribution();
		this.surfaceEnrich = new ResiduesDistribution();
	}
	
	//Add data
	public void addData(ProteinFrequencies local){
		this.fullEnrich.count++;
		this.surfaceEnrich.count++;
		for (ResidueCount res:local.interfCore.resid){
			if(local.interfCore.count != 0){
				// Get for each residue in each region one counter !!!
				double coreFreq = res.getFrequency()/local.interfCore.count;
				double fullFreq = local.full.getFrequency(res.aa)/local.full.count;
				double surfaceFreq = local.surface.getFrequency(res.aa)/local.surface.count;
				
				if(fullFreq !=0 ) this.fullEnrich.resid[res.aa.getNumber()-1].count += coreFreq/fullFreq;
				if(surfaceFreq !=0 ) this.surfaceEnrich.resid[res.aa.getNumber()-1].count += coreFreq/surfaceFreq;
			}
		}
	}
	
	public void printData(PrintWriter out){
		out.print('\n');
		out.println("# Relative Abundance w.r.t Full Protein");
		this.fullEnrich.printRelativeAbundance(out);
		out.print('\n');
		out.println("# Relative Abundacne w.r.t Surface");
		this.surfaceEnrich.printRelativeAbundance(out);
		out.print('\n');
	}
	
	public void printDataTable(PrintWriter out){
		out.print('\n');
		out.println("# Total number of interfaces considered in averaging w.r.t full chain: " + this.fullEnrich.count);
		out.println("# Total number of interfaces considered in avaraging w.r.t surface chain: " + this.surfaceEnrich.count);
		out.print('\n');
		out.println("# AA    Full   Surf");
		for(ResidueCount res:fullEnrich.resid){
			double fullfreq = this.fullEnrich.getFrequency(res.aa)/this.fullEnrich.count;
			double surfacefreq = this.surfaceEnrich.getFrequency(res.aa)/this.surfaceEnrich.count;
			out.println(String.format(" %3s  %6.2f %6.2f", res.aa.getThreeLetterCode(), fullfreq, surfacefreq));
		}
		out.print('\n');
	}
		
}
