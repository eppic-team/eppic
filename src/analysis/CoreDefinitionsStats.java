package analysis;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import crk.CRKParams;

import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbLoadException;
import owl.core.util.FileFormatException;

/**
 * Executable to produce statistics of different core definitions for a set of interfaces.
 * Given a file of pdbCodes and interface ids the interfaces are calculated and output is produced
 * containing columns:
 * pdbCode_interfaceId  core1-Schaerer core2-Schaerer core1-Chakrabarti core2-Chakrabarti core1-Levy core2-Levy area
 * 
 * Can run multi-threaded (one interface calc per thread) by giving number of threads as third parameter
 * 
 * @author duarte_j
 *
 */
public class CoreDefinitionsStats {

	private static String   LOCAL_CIF_DIR;
	
	private static final double CA_CUTOFF = 0.95;
	private static final double RASA_CUTOFF = 0.25;
	
	private static final String BASENAME = "interf_coredef";
	private static final String TMPDIR = System.getProperty("java.io.tmpdir");
	
	private static final int NSPHEREPOINTS =9600;
	private static final int NTHREADS = 1;
	private static final double CUTOFF = 5.9; 	

	
	private class InterfList {
		public String pdbCode;
		public List<ChainInterface> interfaces;
		public InterfList(String pdbCode, List<ChainInterface> interfaces) {
			this.pdbCode = pdbCode;
			this.interfaces = interfaces;
		}
		
		public List<ChainInterface> getInterfaces() {
			return interfaces;
		}

	}
	
	
	private class InterfCalcWorker implements Callable<InterfList> {
		private String pdbCode;
		private List<Integer> interfaceIds;
		
		public InterfCalcWorker(String pdbCode, List<Integer> interfaceIds) {
			this.pdbCode = pdbCode;
			this.interfaceIds = interfaceIds;
		}
		
		@Override
		public InterfList call() throws Exception {
			
			File cifFile = new File(TMPDIR,BASENAME+"_"+pdbCode+".cif");
			try {
				PdbAsymUnit.grabCifFile(LOCAL_CIF_DIR, null, pdbCode, cifFile, false);
			} catch (IOException e) {
				System.out.println("\nError while reading cif.gz file ("+new File(LOCAL_CIF_DIR,pdbCode+".cif.gz").toString()+") or writing temp cif file: "+e.getMessage());
				return null;
			}
			PdbAsymUnit pdb = null;
			try {
				pdb = new PdbAsymUnit(cifFile);
			} catch (PdbLoadException e) {
				System.out.println("\nError. Couldn't load cif file "+cifFile+". Error: "+e.getMessage());
				return null;
			} catch (IOException e) {
				System.out.println("\nError. Couldn't load cif file "+cifFile+". Error: "+e.getMessage());
				return null;
			} catch (FileFormatException e) {
				System.out.println("\nError. Couldn't load cif file "+cifFile+". Error: "+e.getMessage());
				return null;
			} finally {
				cifFile.delete();
			}
			// very important indeed: remove the H atoms! otherwise we get uncomparable core sizes!!!!
			pdb.removeHatoms();
			
			long start = System.currentTimeMillis();
			ChainInterfaceList interfList = null;
			try {
				interfList = pdb.getAllInterfaces(CUTOFF, null, NSPHEREPOINTS, 1, true, false);
			} catch (IOException e) {
				// do nothing, this won't happen as we are not using naccess
			}
			long end = System.currentTimeMillis();
			System.out.printf(pdbCode+"\t%4d\n",(end-start)/1000l);

			List<ChainInterface> list = new ArrayList<ChainInterface>();
			for (int interfId:interfaceIds) {
				list.add(interfList.get(interfId));
			}
			
			return new InterfList(pdbCode,list);
		}
		
	}
	
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		int numThreads = NTHREADS;
		
		if (args.length<2) {
			System.err.println("Usage: CoreDefinitionsStats <list file> <output file> [<number of threads to spawn>]");
			System.exit(1);
		}		
		File listFile = new File(args[0]);
		File outFile = new File(args[1]);
		if (args.length>2) {
			numThreads = Integer.parseInt(args[2]);
		}
		
		CRKParams params = loadConfigFile(); 
		LOCAL_CIF_DIR = params.getLocalCifDir();
		
		 
		TreeMap<String,List<Integer>> entries2interfaces = Utils.readListFile(listFile);
		
		System.out.println("Calculating interfaces...");		

		System.out.println("Total entries to calculate: "+entries2interfaces.size());
		System.out.println("Spawning "+numThreads+" threads");

		
		// the thread pool
		ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
		// the futures (to get returned values of workers)
		List<Future<InterfList>> futures = new ArrayList<Future<InterfList>>();

		
		long start = System.currentTimeMillis();

		// throwing jobs at workers
		for (String pdbCode:entries2interfaces.keySet()) {			
			Callable<InterfList> worker = new CoreDefinitionsStats().new InterfCalcWorker(pdbCode,entries2interfaces.get(pdbCode));
			futures.add(threadPool.submit(worker));
		}
		
		PrintWriter out = new PrintWriter(outFile);
		
		for (Future<InterfList> future:futures) {
			InterfList interfList = future.get();
			if (interfList!=null) {
				
				for (ChainInterface interf:interfList.getInterfaces()) {
					double area = interf.getInterfaceArea();
					interf.calcRimAndCore(CA_CUTOFF);
					int core1Schaerer = interf.getFirstRimCore().getCoreSize();
					int core2Schaerer = interf.getSecondRimCore().getCoreSize();
					interf.calcRimAndCoreChakrabarti();
					int core1Chak = interf.getFirstRimCore().getCoreSize();
					int core2Chak = interf.getSecondRimCore().getCoreSize();
					interf.calcRimAndCoreLevy(RASA_CUTOFF);
					int core1Levy = interf.getFirstRimCore().getCoreSize();
					int core2Levy = interf.getSecondRimCore().getCoreSize();

					
					out.printf("%s_%d %2d %2d %2d %2d %2d %2d %7.2f\n",
							interfList.pdbCode,interf.getId(),core1Schaerer,core2Schaerer,core1Chak,core2Chak,core1Levy,core2Levy,area);
				}

			}
		}
		
		threadPool.shutdown();
		
		while (!threadPool.isTerminated()) {

		}
		out.close();

		long end = System.currentTimeMillis();
		System.out.printf("Total: %4d\n",(end-start)/1000l);

	
	}

	private static CRKParams loadConfigFile() {
		CRKParams params = new CRKParams();
		// loading settings from config file
		File userConfigFile = new File(System.getProperty("user.home"),".crk.conf");  
		try {
			if (userConfigFile.exists()) {
				System.out.println("Loading user configuration file " + userConfigFile);
				params.readConfigFile(userConfigFile);
			} else {
				System.out.println("No config file found. Using default locations");
			}
		} catch (IOException e) {
			System.err.println("Error while reading from config file " + userConfigFile + ": " + e.getMessage());
			System.exit(1);
		}
		return params;
	}
	
}
