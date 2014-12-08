package eppic.analysis.mpstruc;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava3.structure.StructureIO;


public class BlancoEntry implements Comparable<BlancoEntry> {

	private BlancoCluster parent;
	private String pdbCode;
	
	private String expMethod;
	private double resolution;
	private double rFree;
	
	private boolean hasPdbData;
	
	public BlancoEntry() {
		hasPdbData = false;
	}
	
	public void retrievePdbData(File cifDir) {

		Structure pdb = null;
		try {
			pdb = StructureIO.getStructure(pdbCode);
		} catch (IOException|StructureException e) {
			System.err.println("Warning: couldn't load PDB "+pdbCode+". Error: "+e.getMessage());
			hasPdbData = false;
			return;
		} 
		
		
		this.resolution = pdb.getPDBHeader().getResolution();
		this.expMethod = pdb.getPDBHeader().getExperimentalTechniques().iterator().next().getName();
		this.rFree = pdb.getPDBHeader().getRfree();
		
		hasPdbData = true;
		return;
	}
	
	public void printTabular(PrintStream ps) {
		ps.println(pdbCode+" "+String.format("%4.2f",resolution));
	}
	
	public BlancoCluster getParent() {
		return parent;
	}
	
	public void setParent(BlancoCluster parent) {
		this.parent = parent;
	}
	
	public String getPdbCode() {
		return pdbCode;
	}
	
	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
	}
	
	public double getResolution() {
		return resolution;
	}
	
	public void setResolution(double resolution) {
		this.resolution = resolution;
	}

	public String getExpMethod() {
		return expMethod;
	}

	public void setExpMethod(String expMethod) {
		this.expMethod = expMethod;
	}

	public double getrFree() {
		return rFree;
	}

	public void setrFree(double rFree) {
		this.rFree = rFree;
	}

	public boolean hasPdbData() {
		return hasPdbData;
	}

	@Override
	public int compareTo(BlancoEntry o) {
		return Double.compare(this.resolution, o.resolution); 
	}
	
}
