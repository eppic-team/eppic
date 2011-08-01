package analysis;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbLoadException;
import owl.core.structure.SpaceGroup;
import owl.core.util.FileFormatException;

public class InterfaceSorter {

	private static final String   LOCAL_CIF_DIR = "/nfs/data/dbs/pdb/data/structures/all/mmCIF";
	
	private static final String BASENAME = "interf_sort";
	private static final String TMPDIR = System.getProperty("java.io.tmpdir");
	
	private static final int NSPHEREPOINTS =9600;
	private static final int NTHREADS = Runtime.getRuntime().availableProcessors();
	private static final double CUTOFF = 5.9; 

	
	private class SimpleInterface implements Comparable<SimpleInterface>{
		String pdbCode;
		double area;
		String operator;
		String pdbChainCode1;
		String pdbChainCode2;
		double resolution;
		double rFree;
		double rSym;
		
		int clashes;
		
		public SimpleInterface(String pdbCode,double area, String operator, String pdbChainCode1, String pdbChainCode2, int clashes, double resolution, double rFree, double rSym) {
			this.pdbCode = pdbCode;
			this.area = area;
			this.operator = operator;
			this.pdbChainCode1 = pdbChainCode1;
			this.pdbChainCode2 = pdbChainCode2;
			this.clashes = clashes;
			this.resolution = resolution;
			this.rFree = rFree;
			this.rSym = rSym;
		}

		@Override
		public int compareTo(SimpleInterface o) {
			// this will sort descending on interface areas
			return (Double.compare(o.area,this.area));
		}
	}
	
	
	public static void main(String[] args) throws IOException {

		int numThreads = NTHREADS;
		
		if (args.length<2) {
			System.err.println("Usage: InterfaceSorter <list file> <ouput file> [<number of threads for asa calculation>]");
			System.exit(1);
		}		
		File listFile = new File(args[0]);
		File outFile = new File(args[1]);
		if (args.length>2) {
			numThreads = Integer.parseInt(args[2]);
		}
		
		
		List<String> pdbCodes = new ArrayList<String>(); 
		pdbCodes.addAll(Utils.readListFile(listFile).keySet());
		
		List<SimpleInterface> firstInterfaces = new ArrayList<SimpleInterface>();
		
		System.out.println("Calculating interfaces...");
		for (String pdbCode:pdbCodes) {
			System.out.print(pdbCode);
			File cifFile = new File(TMPDIR,BASENAME+"_"+pdbCode+".cif");
			try {
				PdbAsymUnit.grabCifFile(LOCAL_CIF_DIR, null, pdbCode, cifFile, false);
			} catch (IOException e) {
				System.out.println("\nError while reading cif.gz file ("+new File(LOCAL_CIF_DIR,pdbCode+".cif.gz").toString()+") or writing temp cif file: "+e.getMessage());
				continue;
			}
			PdbAsymUnit pdb = null;
			try {
				pdb = new PdbAsymUnit(cifFile);
			} catch (PdbLoadException e) {
				System.out.println("\nError. Couldn't load cif file "+cifFile+". Error: "+e.getMessage());
				continue;
			} catch (IOException e) {
				System.out.println("\nError. Couldn't load cif file "+cifFile+". Error: "+e.getMessage());
				continue;
			} catch (FileFormatException e) {
				System.out.println("\nError. Couldn't load cif file "+cifFile+". Error: "+e.getMessage());
				continue;
			}
			cifFile.delete();
			
			long start = System.currentTimeMillis();
			ChainInterfaceList interfList = null;
			try {
				interfList = pdb.getAllInterfaces(CUTOFF, null, NSPHEREPOINTS, numThreads, false, false, false);
			} catch (IOException e) {
				// do nothing, this won't happen as we are not using naccess
			}
			long end = System.currentTimeMillis();
			System.out.printf("\t%4d\n",(end-start)/1000l);
			//System.out.print(".");
			if (interfList.size()==0) {
				System.out.println("\nEmpty interface list!");
				continue;
			}
			ChainInterface firstInterf = interfList.get(1);
			
			
			String pdbChainCode1 = firstInterf.getFirstMolecule().getPdbChainCode();
			String pdbChainCode2 = firstInterf.getSecondMolecule().getPdbChainCode();
			String op = SpaceGroup.getAlgebraicFromMatrix(firstInterf.getSecondTransf());
			double area = firstInterf.getInterfaceArea();
			int clashes = firstInterf.getAICGraph().getNumClashes();
			
			firstInterfaces.add(new InterfaceSorter().new SimpleInterface(pdbCode, area, op, pdbChainCode1, pdbChainCode2, clashes,
					firstInterf.getFirstMolecule().getParent().getResolution(),
					firstInterf.getFirstMolecule().getParent().getRfree(),
					firstInterf.getFirstMolecule().getParent().getRsym()));
		}
		System.out.println();
		
		Collections.sort(firstInterfaces);
		
		try {
			PrintWriter pw = new PrintWriter(outFile);

			for (SimpleInterface interf:firstInterfaces) {
				pw.printf("%4s\t%4s\t%20s\t%8.2f\t%3d\t%3.1f\t%4.2f\t%4.2f\n",
						interf.pdbCode,
						interf.pdbChainCode1+"+"+interf.pdbChainCode2,
						interf.operator,interf.area,interf.clashes,
						interf.resolution,interf.rFree,interf.rSym);
			}
			pw.close();
		} catch (IOException e) {
			System.err.println("Couldn't write output file "+outFile);
			System.exit(1);
		}
		
	}
	
}
