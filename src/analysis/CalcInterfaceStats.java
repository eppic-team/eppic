package analysis;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbLoadException;
import owl.core.structure.SpaceGroup;
import owl.core.util.FileFormatException;

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

	private static final String   LOCAL_CIF_DIR = new File(System.getProperty("user.home"),"cifrepo").getAbsolutePath();
	
	private static final double CA_CUTOFF = 0.95;
	
	private static final String BASENAME = "interf_sort";
	private static final String TMPDIR = System.getProperty("java.io.tmpdir");
	
	private static final int NSPHEREPOINTS =9600;
	private static final int NTHREADS = 1;
	private static final double CUTOFF = 5.9; 	
	
	
	private class Counter extends Thread {
		public int total;
		private List<CalcInterfaceStats> ciss;
		public Counter(List<CalcInterfaceStats> ciss) {
			this.ciss = ciss;
		}
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
				
				// very important indeed: remove the H atoms! otherwise we get uncomparable areas/core sizes!!!!
				pdb.removeHatoms();
				
				long start = System.currentTimeMillis();
				ChainInterfaceList interfList = null;
				try {
					interfList = pdb.getAllInterfaces(CUTOFF, null, NSPHEREPOINTS, NTHREADS, false, false);
				} catch (IOException e) {
					// do nothing, this won't happen as we are not using naccess
				}
				long end = System.currentTimeMillis();
				System.out.printf(pdbCode+"\t%4d\n",(end-start)/1000l);
				//System.out.print(".");
				if (interfList.size()==0) {
					System.out.println(pdbCode+"\tEmpty interface list!");
					continue;
				}
				
				for (ChainInterface interf:interfList) {
					if (interf.getNumClashes()>5) continue entries;
				}
				
				for (ChainInterface interf:interfList) {
				
					int interfId = interf.getId();
					String pdbChainCode1 = interf.getFirstMolecule().getPdbChainCode();
					String pdbChainCode2 = interf.getSecondMolecule().getPdbChainCode();
					String op = SpaceGroup.getAlgebraicFromMatrix(interf.getSecondTransf());
					double area = interf.getInterfaceArea();
					int clashes = interf.getAICGraph().getNumClashes();
					
					interf.calcRimAndCore(CA_CUTOFF);
					int core1 = interf.getFirstRimCore().getCoreSize();
					int core2 = interf.getSecondRimCore().getCoreSize();


					
					outputStrings.add(String.format("%4s\t%2d\t%4s\t%20s\t%8.2f\t%3d\t%3.1f\t%4.2f\t%4.2f\t%3d\t%3d",
							pdbCode,interfId,
							pdbChainCode1+"+"+pdbChainCode2,
							op,area,clashes,
							pdb.getResolution(),pdb.getRfree(),pdb.getRsym(),
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
