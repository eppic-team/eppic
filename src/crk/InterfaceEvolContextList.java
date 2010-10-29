package crk;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InterfaceEvolContextList implements Iterable<InterfaceEvolContext> {

	private List<InterfaceEvolContext> list;

	private ScoringType scoType;
	private boolean isScoreWeighted;
	private double bioCutoff;
	private double xtalCutoff;
	private int homologsCutoff;
	private int minCoreSize;
	private int minMemberCoreSize;
	private double idCutoff;
	private double queryCovCutoff;
	private int maxNumSeqsCutoff;
	private double minInterfAreaReporting;
	
	public InterfaceEvolContextList(int homologsCutoff, int minCoreSize, int minMemberCoreSize, 
			double idCutoff, double queryCovCutoff, int maxNumSeqsCutoff, double minInterfAreaReporting) {
		this.homologsCutoff = homologsCutoff;
		this.minCoreSize = minCoreSize;
		this.minMemberCoreSize = minMemberCoreSize;
		this.idCutoff = idCutoff;
		this.queryCovCutoff = queryCovCutoff;
		this.maxNumSeqsCutoff = maxNumSeqsCutoff;
		this.minInterfAreaReporting = minInterfAreaReporting;
		
		list = new ArrayList<InterfaceEvolContext>();
	}
	
	public void add(InterfaceEvolContext iec) {
		list.add(iec);
	}
	
	@Override
	public Iterator<InterfaceEvolContext> iterator() {
		return list.iterator();
	}

	
	
	public void scoreEntropy(boolean weighted) {
		this.scoType = ScoringType.ENTROPY;
		this.isScoreWeighted = weighted;
		for (InterfaceEvolContext iec:this) {
			iec.scoreEntropy(weighted);
		}
	}
	
	public void scoreKaKs(boolean weighted) {
		this.scoType = ScoringType.KAKS;
		this.isScoreWeighted = weighted;
		for (InterfaceEvolContext iec:this) {
			if (iec.canDoCRK()) {
				iec.scoreKaKs(weighted);
			}
		}		
	}
	
	public boolean isScoreWeighted() {
		return isScoreWeighted;
	}
	
	public void printScoresTable(PrintStream ps, double bioCutoff, double xtalCutoff) {
		this.bioCutoff = bioCutoff;
		this.xtalCutoff = xtalCutoff;
		
		printScoringParams(ps);
		printScoringHeaders(ps);
		
		for (InterfaceEvolContext iec:this) {
			if (iec.getInterface().getInterfaceArea()>minInterfAreaReporting) {
				iec.printScoresTable(ps, bioCutoff, xtalCutoff, homologsCutoff, minCoreSize, minMemberCoreSize);
			}
		}
	}
	
	public void writeScoresPDBFiles(File outDir, String baseName, String suffix, boolean transform) throws IOException {
		for (InterfaceEvolContext iec:this) {
			if (iec.getInterface().getInterfaceArea()>minInterfAreaReporting) {
				iec.writePdbFile(new File(outDir, baseName+"."+iec.getInterface().getId()+suffix), InterfaceEvolContext.SCORES, transform);
			}
		}
	}
	
	public void writeRimCorePDBFiles(File outDir, String baseName, String suffix, boolean transform) throws IOException {
		for (InterfaceEvolContext iec:this) {
			if (iec.getInterface().getInterfaceArea()>minInterfAreaReporting) {
				iec.writePdbFile(new File(outDir, baseName+"."+iec.getInterface().getId()+suffix), InterfaceEvolContext.RIMCORE, transform);
			}
		}
	}	
	
	public double getBioCutoff() {
		return bioCutoff;
	}

	public double getXtalCutoff() {
		return xtalCutoff;
	}

	public int getHomologsCutoff() {
		return homologsCutoff;
	}

	public int getMinCoreSize() {
		return minCoreSize;
	}

	public int getMinMemberCoreSize() {
		return minMemberCoreSize;
	}
	
	public ScoringType getScoringType() {
		return scoType;
	}

	private void printScoringParams(PrintStream ps) {
		ps.println("# Score method: "+scoType.getName());
		ps.println("# Score type:   "+(isScoreWeighted?"weighted":"unweighted"));
		ps.printf ("# Sequence identity cutoff: %4.2f\n",idCutoff);
		ps.printf ("# Query coverage cutoff: %4.2f\n",queryCovCutoff);
		ps.println("# Max num sequences used: "+maxNumSeqsCutoff);
		ps.printf ("# Bio-call cutoff:  %4.2f\n",bioCutoff);
		ps.printf ("# Xtal-call cutoff: %4.2f\n",xtalCutoff);
		ps.println("# Total core size xtal-call cutoff: "+minCoreSize);
		ps.println("# Per-member core size xtal-call cutoff: "+minMemberCoreSize);
		
	}
	
	private static void printScoringHeaders(PrintStream ps) {
		ps.printf("%15s\t%6s\t","interface","area");
		ps.printf("%5s\t%5s\t%5s","size1", "size2","CA");
		ps.print("\t");
		ps.printf("%2s\t%2s\t","n1","n2");
		ps.printf("%5s\t%5s\t%5s","core1","rim1","rat1");
		ps.print("\t");
		ps.printf("%5s\t%5s\t%5s","core2","rim2","rat2");
		ps.print("\t");
		ps.printf("%6s\t%5s\t%6s\t%6s\t%6s\t%6s",
				"call","score",CallType.BIO.getName(),CallType.CRYSTAL.getName(),CallType.GRAY.getName(),CallType.NO_PREDICTION.getName());
		ps.println();
	}
	

}
