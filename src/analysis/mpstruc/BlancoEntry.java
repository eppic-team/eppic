package analysis.mpstruc;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbLoadException;
import owl.core.util.FileFormatException;

public class BlancoEntry implements Comparable<BlancoEntry> {

	private static final String BASENAME = "ciftemp";
	private static final String TMPDIR = System.getProperty("java.io.tmpdir");

	
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

		File cifFile = new File(TMPDIR,BASENAME+"_"+pdbCode+".cif");
		cifFile.deleteOnExit();
		try {
			PdbAsymUnit.grabCifFile(cifDir.getAbsolutePath(), null, pdbCode, cifFile, false);
		} catch (IOException e) {
			System.err.println("Warning: error while reading cif.gz file ("+new File(cifDir,pdbCode+".cif.gz").toString()+") or writing temp cif file: "+e.getMessage());
			hasPdbData = false;
			return;
		}
		
		PdbAsymUnit pdb = null;
		try {
			pdb = new PdbAsymUnit(cifFile);
		} catch (PdbLoadException e) {
			System.err.println("Warning: couldn't load cif file "+cifFile+". Error: "+e.getMessage());
			hasPdbData = false;
			return;
		} catch (IOException e) {
			System.err.println("Warning: couldn't load cif file "+cifFile+". Error: "+e.getMessage());
			hasPdbData = false;
			return;
		} catch (FileFormatException e) {
			System.err.println("Warning: couldn't load cif file "+cifFile+". Error: "+e.getMessage());
			hasPdbData = false;
			return;
		}
		
		this.resolution = pdb.getResolution();
		this.expMethod = pdb.getExpMethod();
		this.rFree = pdb.getRfree();
		
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
