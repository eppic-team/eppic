package eppic.analysis;

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

import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.GroupType;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureIO;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.asa.GroupAsa;
import org.biojava.nbio.structure.contact.Pair;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.io.StructureFiletype;
import org.biojava.nbio.structure.xtal.CrystalBuilder;

import eppic.EppicParams;

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

	private static final double CA_CUTOFF = 0.95;
	private static final double RASA_CUTOFF = 0.25;
	private static final double MIN_ASA_FOR_SURFACE = 5;
	
	private static final int NSPHEREPOINTS = 3000;
	private static final int NTHREADS = 1;

	
	private class InterfList {
		public String pdbCode;
		public List<StructureInterface> interfaces;
		public InterfList(String pdbCode, List<StructureInterface> interfaces) {
			this.pdbCode = pdbCode;
			this.interfaces = interfaces;
		}
		
		public List<StructureInterface> getInterfaces() {
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
			
			Structure pdb = null;
			try {
				pdb = StructureIO.getStructure(pdbCode);
			} catch (IOException|StructureException e) {
				System.out.println("\nError. Couldn't load PDB "+pdbCode+". Error: "+e.getMessage());
				return null;
			} 
			
			long start = System.currentTimeMillis();

			CrystalBuilder cb = new CrystalBuilder(pdb);
			
			StructureInterfaceList interfList = cb.getUniqueInterfaces(EppicParams.INTERFACE_DIST_CUTOFF);
			
			interfList.calcAsas(NSPHEREPOINTS, 1, -1);
			interfList.removeInterfacesBelowArea(EppicParams.MIN_INTERFACE_AREA_TO_KEEP);
			
			long end = System.currentTimeMillis();
			System.out.printf(pdbCode+"\t%4d\n",(end-start)/1000l);

			List<StructureInterface> list = new ArrayList<StructureInterface>();
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
		
		AtomCache cache = new AtomCache();
		cache.setFiletype(StructureFiletype.CIF);
		StructureIO.setAtomCache(cache); 
		
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
				
				for (StructureInterface interf:interfList.getInterfaces()) {
					double area = interf.getTotalArea();
					int core1Schaerer = interf.getCoreResidues(CA_CUTOFF, MIN_ASA_FOR_SURFACE).getFirst().size();
					int core2Schaerer = interf.getCoreResidues(CA_CUTOFF, MIN_ASA_FOR_SURFACE).getSecond().size();
					Pair<List<Group>> coresChak = getCoreChakrabarti(interf, MIN_ASA_FOR_SURFACE); 
					int core1Chak = coresChak.getFirst().size();
					int core2Chak = coresChak.getSecond().size();
					Pair<List<Group>> coresLevy = getCoreLevy(interf, RASA_CUTOFF);
					int core1Levy = coresLevy.getFirst().size();
					int core2Levy = coresLevy.getSecond().size();

					
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

	/**
	 * Returns 2 lists of core residues in the Chakrabarti definition.
	 * Following the Chakrabarti definition (see Chakrabarti, Janin Proteins 2002):
	 * core residues have at least one atom fully buried (bsa/asa=1) and rim residues are all the
	 * rest still with bsa>0 but bsa/asa<1 (all atoms partially accessible)
	 * Residues will be considered at interface surface only if their ASA is above 
	 * given minAsaForSurface 
	 * @param interf  
	 * @param minAsaForSurface
	 * @return
	 */
	public static Pair<List<Group>> getCoreChakrabarti(StructureInterface interf, double minAsaForSurface) {
		List<Group> core1 = new ArrayList<Group>();
		List<Group> core2 = new ArrayList<Group>();
		
		for (GroupAsa groupAsa:interf.getFirstGroupAsas().values()) {
			
			if (groupAsa.getAsaU()>minAsaForSurface && groupAsa.getBsa()>0) {
				boolean iscore = false;
				for (int i=0; i<groupAsa.getAtomAsaCs().size();i++) {
					double atomAsaC = groupAsa.getAtomAsaCs().get(i);
					double atomAsaU = groupAsa.getAtomAsaUs().get(i);
					if ( ( (atomAsaU-atomAsaC)/atomAsaU ) > 0.999 ) {
						iscore = true;
						break;
					}
				}
				if (iscore) core1.add(groupAsa.getGroup());
			}
		}
		
		for (GroupAsa groupAsa:interf.getSecondGroupAsas().values()) {
			
			if (groupAsa.getAsaU()>minAsaForSurface && groupAsa.getBsa()>0) {
				boolean iscore = false;
				for (int i=0; i<groupAsa.getAtomAsaCs().size();i++) {
					double atomAsaC = groupAsa.getAtomAsaCs().get(i);
					double atomAsaU = groupAsa.getAtomAsaUs().get(i);
					if ( ( (atomAsaU-atomAsaC)/atomAsaU ) > 0.999 ) {
						iscore = true;
						break;
					}
				}
				if (iscore) core2.add(groupAsa.getGroup());
			}
		}

		return new Pair<List<Group>>(core1,core2);
	}
	
	/**
	 * Returns 2 lists of core residues in the Levy definition. 
	 * Following the Levy definition (see Levy JMB 2010):
	 * core residues have rASA(c) below 25% while rASA(u) was above 25%, 
	 * rim residues have rASA(c) above 25%
	 * The paper also introduces a third class of residues: support residues, 
	 * those with rASA(u)<25% 
	 * @param interf
	 * @param rASAcutoff
	 * @return
	 */
	public static Pair<List<Group>> getCoreLevy(StructureInterface interf, double rASAcutoff) {
		List<Group> core1 = new ArrayList<Group>();
		List<Group> core2 = new ArrayList<Group>();

		for (GroupAsa groupAsa:interf.getFirstGroupAsas().values()) {
			Group g = groupAsa.getGroup();
			if (!g.getType().equals(GroupType.AMINOACID)) continue;
			
			if (groupAsa.getBsa()>0) {
				double rasau = groupAsa.getRelativeAsaU();
				double rasac = groupAsa.getRelativeAsaC();
				if ( rasac<rASAcutoff && rasau>rASAcutoff) {  
					core1.add(g);
				}				
			}
		}
		
		for (GroupAsa groupAsa:interf.getSecondGroupAsas().values()) {
			Group g = groupAsa.getGroup();
			if (!g.getType().equals(GroupType.AMINOACID)) continue;
			
			if (groupAsa.getBsa()>0) {
				double rasau = groupAsa.getRelativeAsaU();
				double rasac = groupAsa.getRelativeAsaC();
				if ( rasac<rASAcutoff && rasau>rASAcutoff) {  
					core2.add(g);
				}				
			}
		}
		
		return new Pair<List<Group>>(core1,core2);
	}
	
}
