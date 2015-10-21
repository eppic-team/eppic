package eppic.analysis;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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

/**
 * Script to calculate all interfaces of given PDB entries and output a table 
 * with statistics on interface (area, num. core residues, operator) and crystallographic
 * quality parameters. 
 * Can run multithreaded by splitting the entries list in as many lists as parallel
 * threads specified.
 * @author duarte_j
 *
 */
public class CalcInterfaceStats extends Thread {
	
	private static final double CA_CUTOFF = 0.95;
	private static final double MIN_ASA_FOR_SURFACE = 5;
	
	private static final int NSPHEREPOINTS = 3000;
	private static final int NTHREADS = 1;
	
	
	private class Counter extends Thread {
		public int total;
		private List<CalcInterfaceStats> ciss;
		public Counter(List<CalcInterfaceStats> ciss) {
			this.ciss = ciss;
		}
		@Override
		public void run() {
			total = 0;
			for (CalcInterfaceStats cis:ciss) {
				total+=cis.doneEntries;
			}
			
		}
	}
	
	
	private List<String> pdbCodes;
	private List<String> outputStrings;
	private int doneEntries;
	
	public CalcInterfaceStats(List<String> pdbCodes) {
		this.pdbCodes = pdbCodes;
		this.outputStrings = new ArrayList<String>();
		doneEntries = 0;
	}
	
	public void printList(PrintWriter out) {
		for (String line:outputStrings) {
			out.println(line);
		}
	}
	
	@Override
	public void run() {
		
		entries:
			for (String pdbCode:pdbCodes) {
				
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
				
				interfList.calcAsas(NSPHEREPOINTS, NTHREADS, -1);
				interfList.removeInterfacesBelowArea(EppicParams.MIN_INTERFACE_AREA_TO_KEEP);
				 
				long end = System.currentTimeMillis();
				System.out.printf(pdbCode+"\t%4d\n",(end-start)/1000l);
				//System.out.print(".");
				if (interfList.size()==0) {
					System.out.println(pdbCode+"\tEmpty interface list!");
					continue;
				}
				
				for (StructureInterface interf:interfList) {
					if ( interf.getContacts().getContactsWithinDistance(EppicParams.CLASH_DISTANCE).size() > 5 ) 
						continue entries;
				}
				
				for (StructureInterface interf:interfList) {
				
					int interfId = interf.getId();
					String pdbChainCode1 = interf.getMoleculeIds().getFirst();
					String pdbChainCode2 = interf.getMoleculeIds().getSecond();
					String op = SpaceGroup.getAlgebraicFromMatrix(interf.getTransforms().getSecond().getMatTransform());
					double area = interf.getTotalArea();
					int clashes = interf.getContacts().getContactsWithinDistance(EppicParams.CLASH_DISTANCE).size();
					
					int core1 = interf.getCoreResidues(CA_CUTOFF, MIN_ASA_FOR_SURFACE).getFirst().size();
					int core2 = interf.getCoreResidues(CA_CUTOFF, MIN_ASA_FOR_SURFACE).getSecond().size();


					
					outputStrings.add(String.format("%4s\t%2d\t%4s\t%20s\t%8.2f\t%3d\t%3.1f\t%4.2f\t%3d\t%3d",
							pdbCode,interfId,
							pdbChainCode1+"+"+pdbChainCode2,
							op,area,clashes,
							pdb.getPDBHeader().getResolution(),
							pdb.getPDBHeader().getRfree(),
							core1,core2));
				}
				doneEntries++;
			}
		
	}
	
	
	
	public static void main(String[] args) throws InterruptedException, IOException {

		int numThreads = NTHREADS;
		
		if (args.length<2) {
			System.err.println("Usage: CalcInterfaceStats <list file> <ouput file> [<number of threads to spawn>]");
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
		
		System.out.println("Calculating interfaces...");
		
		
		List<List<String>> lists = divideList(pdbCodes,numThreads);
		System.out.println("Total entries to calculate: "+pdbCodes.size());
		System.out.println("Spawning "+numThreads+" threads");
		System.out.println("Entries in each thread: ");
		//int total = 0;
		for (List<String> list:lists) {
			System.out.println(list.size());
			//total +=list.size();
		}
		
		
		List<CalcInterfaceStats> ciss = new ArrayList<CalcInterfaceStats>();
		
		for (List<String> list:lists) {
			CalcInterfaceStats cis = new CalcInterfaceStats(list);
			ciss.add(cis);
			cis.start();
		}
		
		// reporting progress from all threads
		int lastDiv = 0;
		while (checkAlive(ciss)) {
			Thread.sleep(10000); // every 10 seconds we check progress
			Counter c = new CalcInterfaceStats(null).new Counter(ciss);
			c.start();
			c.join();
			// we report every time we pass 100 more calculated entries
			int div = c.total/100;
			if (div!=lastDiv) { 
				System.out.println("## done "+c.total);
			}
			lastDiv = div;
		}
		
		for (CalcInterfaceStats cis:ciss){
			cis.join();
		}
		
		PrintWriter pw = new PrintWriter(outFile);

		for (CalcInterfaceStats cis:ciss) {
			cis.printList(pw);
		}
		
		
		pw.close();

		
	}
	
	private static boolean checkAlive(List<CalcInterfaceStats> ciss) {
		for (CalcInterfaceStats cis:ciss) {
			if (cis.isAlive()) return true; 
		}
		return false;
	}
	

	private static List<List<String>> divideList(List<String> list, int chunks) {
		List<List<String>> lists = new ArrayList<List<String>>();
		int chunksize = list.size()/chunks;
		List<String> sublist = null;
		for (int i=0;i<list.size();i++){
			if (i%chunksize==0 && lists.size()<chunks) {
				sublist = new ArrayList<String>();
				lists.add(sublist);
			}
			sublist.add(list.get(i));
			
		}
		return lists;
	}
	
	
	
}
