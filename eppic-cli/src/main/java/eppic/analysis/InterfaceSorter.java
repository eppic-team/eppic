package eppic.analysis;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureIO;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.xtal.CrystalBuilder;
import org.biojava.nbio.structure.xtal.SpaceGroup;

import eppic.EppicParams;

public class InterfaceSorter {

	private static final int NSPHEREPOINTS =9600;
	private static final int NTHREADS = Runtime.getRuntime().availableProcessors();

	
	private class SimpleInterface implements Comparable<SimpleInterface>{
		String pdbCode;
		double area;
		String operator;
		String pdbChainCode1;
		String pdbChainCode2;
		double resolution;
		double rFree;
		
		int clashes;
		
		public SimpleInterface(String pdbCode,double area, String operator, String pdbChainCode1, String pdbChainCode2, int clashes, double resolution, double rFree) {
			this.pdbCode = pdbCode;
			this.area = area;
			this.operator = operator;
			this.pdbChainCode1 = pdbChainCode1;
			this.pdbChainCode2 = pdbChainCode2;
			this.clashes = clashes;
			this.resolution = resolution;
			this.rFree = rFree;
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
		
		AtomCache cache = new AtomCache();		
		cache.setUseMmCif(true);		
		StructureIO.setAtomCache(cache); 

		
		List<String> pdbCodes = new ArrayList<String>(); 
		pdbCodes.addAll(Utils.readListFile(listFile).keySet());
		
		List<SimpleInterface> firstInterfaces = new ArrayList<SimpleInterface>();
		
		System.out.println("Calculating interfaces...");
		for (String pdbCode:pdbCodes) {
			System.out.print(pdbCode);
			
			Structure pdb = null;
			try {
				pdb = StructureIO.getStructure(pdbCode);
			} catch (IOException|StructureException e) {
				System.out.println("\nError. Couldn't load PDB "+pdbCode+". Error: "+e.getMessage());
				continue;
			} 
			
			
			
			long start = System.currentTimeMillis();
							
			CrystalBuilder cb = new CrystalBuilder(pdb);
			
			StructureInterfaceList interfList = cb.getUniqueInterfaces(EppicParams.INTERFACE_DIST_CUTOFF);
			
			interfList.calcAsas(NSPHEREPOINTS, numThreads, -1);
			interfList.removeInterfacesBelowArea(EppicParams.MIN_INTERFACE_AREA_TO_KEEP);
		
			
			long end = System.currentTimeMillis();
			System.out.printf("\t%4d\n",(end-start)/1000l);
			//System.out.print(".");
			if (interfList.size()==0) {
				System.out.println("\nEmpty interface list!");
				continue;
			}
			StructureInterface firstInterf = interfList.get(1);
			
			
			String pdbChainCode1 = firstInterf.getMoleculeIds().getFirst();
			String pdbChainCode2 = firstInterf.getMoleculeIds().getSecond();
			String op = SpaceGroup.getAlgebraicFromMatrix(firstInterf.getTransforms().getSecond().getMatTransform());
			double area = firstInterf.getTotalArea();
			int clashes = firstInterf.getContacts().getContactsWithinDistance(EppicParams.CLASH_DISTANCE).size();
			
			firstInterfaces.add(new InterfaceSorter().new SimpleInterface(pdbCode, area, op, pdbChainCode1, pdbChainCode2, clashes,
					pdb.getPDBHeader().getResolution(),
					pdb.getPDBHeader().getRfree()
					));
		}
		System.out.println();
		
		Collections.sort(firstInterfaces);
		
		try {
			PrintWriter pw = new PrintWriter(outFile);

			for (SimpleInterface interf:firstInterfaces) {
				pw.printf("%4s\t%4s\t%20s\t%8.2f\t%3d\t%3.1f\t%4.2f\n",
						interf.pdbCode,
						interf.pdbChainCode1+"+"+interf.pdbChainCode2,
						interf.operator,interf.area,interf.clashes,
						interf.resolution,interf.rFree);
			}
			pw.close();
		} catch (IOException e) {
			System.err.println("Couldn't write output file "+outFile);
			System.exit(1);
		}
		
	}
	
}
