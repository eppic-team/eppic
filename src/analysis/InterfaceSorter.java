package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.Pdb;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbLoadError;
import owl.core.structure.SpaceGroup;
import owl.core.util.FileFormatError;

public class InterfaceSorter {

	private static final String   LOCAL_CIF_DIR = "/nfs/data/dbs/pdb/data/structures/all/mmCIF";
	
	private static final Pattern PDBCODE_REGEX = Pattern.compile("^\\d\\w\\w\\w$");
	
	private static final String BASENAME = "interf_sort";
	private static final String TMPDIR = System.getProperty("java.io.tmpdir");
	
	private static final int NSPHEREPOINTS =9600;
	private static final int NTHREADS = Runtime.getRuntime().availableProcessors();
	private static final double CUTOFF = 5.9; 
	private static final double CLASHDISTANCE = 1.5;

	
	private class SimpleInterface implements Comparable<SimpleInterface>{
		String pdbCode;
		double area;
		String operator;
		Pdb molec1;
		Pdb molec2;
		int clashes;
		
		public SimpleInterface(String pdbCode,double area, String operator, Pdb molec1, Pdb molec2, int clashes) {
			this.pdbCode = pdbCode;
			this.area = area;
			this.operator = operator;
			this.molec1 = molec1;
			this.molec2 = molec2;
			this.clashes = clashes;
		}

		@Override
		public int compareTo(SimpleInterface o) {
			// this will sort descending on interface areas
			return (Double.compare(o.area,this.area));
		}
	}
	
	
	public static void main(String[] args) {

		if (args.length<2) {
			System.err.println("Usage: InterfaceSorter <list file> <ouput file>");
			System.exit(1);
		}
		
		File listFile = new File(args[0]);
		File outFile = new File(args[1]);
		
		List<String> pdbCodes = readListFile(listFile);
		
		List<SimpleInterface> firstInterfaces = new ArrayList<SimpleInterface>();
		
		System.out.println("Calculating interfaces...");
		for (String pdbCode:pdbCodes) {
			System.out.print(pdbCode);
			File cifFile = new File(TMPDIR,BASENAME+"_"+pdbCode+".cif");
			try {
				PdbAsymUnit.grabCifFile(LOCAL_CIF_DIR, null, pdbCode, cifFile, false);
			} catch (IOException e) {
				System.out.println("\nError. Couldn't find cif file "+new File(LOCAL_CIF_DIR,pdbCode+".cif.gz").toString());
				continue;
			}
			PdbAsymUnit pdb = null;
			try {
				pdb = new PdbAsymUnit(cifFile);
			} catch (PdbLoadError e) {
				System.out.println("\nError. Couldn't load cif file "+cifFile+". Error: "+e.getMessage());
				continue;
			} catch (IOException e) {
				System.out.println("\nError. Couldn't load cif file "+cifFile+". Error: "+e.getMessage());
				continue;
			} catch (FileFormatError e) {
				System.out.println("\nError. Couldn't load cif file "+cifFile+". Error: "+e.getMessage());
				continue;
			}
			long start = System.currentTimeMillis();
			ChainInterfaceList interfList = null;
			try {
				interfList = pdb.getAllInterfaces(CUTOFF, null, NSPHEREPOINTS, NTHREADS);
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
			ChainInterface firstInterf = interfList.get(0);
			
			
			Pdb molec1 = firstInterf.getFirstMolecule();
			Pdb molec2 = firstInterf.getSecondMolecule();
			String op = SpaceGroup.getAlgebraicFromMatrix(firstInterf.getSecondTransf());
			double area = firstInterf.getInterfaceArea();
			int clashes = firstInterf.getAICGraph().getNumClashes(CLASHDISTANCE);
			
			firstInterfaces.add(new InterfaceSorter().new SimpleInterface(pdbCode, area, op, molec1, molec2, clashes));
		}
		System.out.println();
		
		Collections.sort(firstInterfaces);
		
		try {
			PrintWriter pw = new PrintWriter(outFile);

			for (SimpleInterface interf:firstInterfaces) {
				pw.printf("%4s\t%4s\t%20s\t%8.2f\t%3d\t%3.1f\t%4.2f\t%4.2f\n",
						interf.pdbCode,
						interf.molec1.getPdbChainCode()+"+"+interf.molec2.getPdbChainCode(),
						interf.operator,interf.area,interf.clashes,
						interf.molec1.getResolution(),interf.molec1.getRfree(),interf.molec1.getRsym());
			}
			pw.close();
		} catch (IOException e) {
			System.err.println("Couldn't write output file "+outFile);
			System.exit(1);
		}
		
	}

	private static List<String> readListFile(File listFile) {
		List<String> pdbCodes = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(listFile));
			String line;
			while ((line=br.readLine())!=null) {
				if (line.startsWith("#")) continue;
				Matcher m = PDBCODE_REGEX.matcher(line);
				if (m.matches()) {
					pdbCodes.add(line.toLowerCase());
				}
			}
			br.close();
		} catch (IOException e) {
			System.err.println("Couldn't read list file "+listFile);
			System.exit(1);
		}
		return pdbCodes;
	}
	
	
	
}
