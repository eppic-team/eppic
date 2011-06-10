package analysis;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import crk.CallType;
import crk.ChainEvolContext;
import crk.ChainEvolContextList;
import crk.InterfaceEvolContext;

//import owl.core.structure.AaResidue;
//import owl.core.structure.AminoAcid;
import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
//import owl.core.structure.InterfaceRimCore;
//import owl.core.structure.Residue;
import owl.core.util.Goodies;

/**
 * Script to go through output files of crk (*.interfaces.dat) in given directories 
 * (one for a bio set, one for a xtal set) and predict the bio/xtal character of 
 * interfaces based on core size alone. 
 * Outputs statistics of prediction (accuracy, TP, FN) first bio, then xtal, then global
 * We also output entropy/kaks evolutionary scores and calls read from *.chainevolcontext.dat 
 * files in same directories
 * 
 * @author duarte_j
 *
 */
public class CoreSizePredictor {

	private static final double[] DEF_CA_CUTOFF = {0.95};
	private static final int DEF_MIN_NUMBER_CORE_RESIDUES_FOR_BIO = 7;
	
	//private static final int MINIMUM_INTERF_AREA_TO_REPORT = 1000;
	private static final int MIN_NUM_HOMOLOGS = 10;
	private static final double BIO_CUTOFF = 0.84;
	private static final double XTAL_CUTOFF = 0.86;
	
	private static final String PROGRAM_NAME = "CoreSizePredictor";
	
	private static int minNumberCoreResForBio = DEF_MIN_NUMBER_CORE_RESIDUES_FOR_BIO;
	private static double[] caCutoff = DEF_CA_CUTOFF;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		
		File bioDir    = null;
		File xtalDir   = null;
		File bioList   = null;
		File xtalList  = null;
		
		String help = "Usage: \n" +
		PROGRAM_NAME+"\n" +
		"   -B         :  input dir containing the interfaces given with -b\n" +
		"   -X         :  input dir containing the interfaces given with -x\n"+
		"   -b         :  list file containing all the pdbIds + interface serials to \n" +
		"                 analyse that are known to be true bio contacts\n" +
		"   -x         :  list file containing all the pdbIds + interface serials to \n" +
		"                 analyse that are known to be true xtal contacts\n" +
		"   [-m]       :  minimum number of core residues to call bio, below this we call xtal\n" +
		"   [-c]       :  core assignment cutoff. Default: "+String.format("%3.2f", DEF_CA_CUTOFF[0])+"\n";

		Getopt g = new Getopt(PROGRAM_NAME, args, "B:X:b:x:m:c:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'B':
				bioDir = new File(g.getOptarg());
				break;
			case 'X':
				xtalDir = new File(g.getOptarg());
				break;
			case 'b':
				bioList = new File(g.getOptarg());
				break;
			case 'x':
				xtalList = new File(g.getOptarg());
				break;
			case 'm':
				minNumberCoreResForBio = Integer.parseInt(g.getOptarg());
				break;
			case 'c':
				caCutoff[0] = Double.parseDouble(g.getOptarg());
				break;
			case 'h':
			case '?':
				System.out.println(help);
				System.exit(0);
				break; // getopt() already printed an error
			}
		}
		
		if (bioDir==null && xtalDir==null) {
			System.err.println("Must specify either a bio list/dir or xtal list/dir");
			System.exit(1);
		}
		if ((bioDir==null && bioList!=null)|| (bioDir!=null && bioList==null)) {
			System.err.println("Must specify both a bio dir and a bio list");
			System.exit(1);
		}
		if ((xtalDir==null && xtalList!=null)|| (xtalDir!=null && xtalList==null)) {
			System.err.println("Must specify both a xtal dir and a xtal list");
			System.exit(1);	
		}
		
		int total = 0;
		int[] xtalStats = new int[2];
		int[] bioStats = new int[2];
		if (bioDir!=null) {
			TreeMap<String,List<Integer>> bioToAnalyse = CalcStats.readListFile(bioList);
			for (List<Integer> vals:bioToAnalyse.values()) {
				total+=vals.size();
			}
			Map<String,File> bioFiles = getListInterfDatFiles(bioDir, bioToAnalyse.keySet());
			bioStats = analyse(bioToAnalyse,bioFiles,CallType.BIO);
		}
		
		if (xtalDir!=null) {
			TreeMap<String,List<Integer>> xtalToAnalyse = CalcStats.readListFile(xtalList);
			for (List<Integer> vals:xtalToAnalyse.values()) {
				total+=vals.size();
			}
			Map<String,File> xtalFiles = getListInterfDatFiles(xtalDir, xtalToAnalyse.keySet());
			xtalStats = analyse(xtalToAnalyse,xtalFiles,CallType.CRYSTAL);
		}
		
		int[] globalStats = {bioStats[0]+xtalStats[0],bioStats[1]+xtalStats[1]};

		if (bioDir!=null) {
			System.out.println("Bio set: ");
			printStats(bioStats);
		}
		if (xtalDir!=null) {
			System.out.println("Xtal set: ");
			printStats(xtalStats);
		}

		System.out.println("Global: ");
		printStats(globalStats);
	}
	
	private static int[] analyse(TreeMap<String,List<Integer>> toAnalyse, Map<String,File> files, CallType truth) throws IOException, ClassNotFoundException{
		int[] stats = new int[2]; // tp, fn
		for (String pdbCode:toAnalyse.keySet()) {
			if (!files.containsKey(pdbCode)) continue;
			ChainInterfaceList interfaces = (ChainInterfaceList)Goodies.readFromFile(files.get(pdbCode));
			for (int id:toAnalyse.get(pdbCode)) {
				ChainInterface interf = interfaces.get(id-1);
				interf.calcRimAndCore(caCutoff);
				int size1 = interf.getFirstRimCores()[0].getCoreSize();
				int size2 = interf.getSecondRimCores()[0].getCoreSize();
				int size = size1+size2;				
				double area = interf.getInterfaceArea();
				int disulfides = interf.getAICGraph().getDisulfidePairs().size();
				
				String predictStr = null;
				
				if (size<minNumberCoreResForBio) {
					if (truth==CallType.CRYSTAL) stats[0]++;
					else if (truth==CallType.BIO) stats[1]++;
					predictStr = CallType.CRYSTAL.getName();
				} else {
					if (truth==CallType.CRYSTAL)  stats[1]++;
					else if (truth==CallType.BIO) stats[0]++;
					predictStr = CallType.BIO.getName();
				}
				
				File chainevolfile = new File(files.get(pdbCode).getParent(),pdbCode+".chainevolcontext.dat");
				String callStr = getEvolCall(chainevolfile,interf);

				System.out.println(pdbCode+" "+id);
				System.out.printf("%2d %2d %7.2f %5.1f %4d %4s -- %s\n",size1,size2,area,area/(double)size,disulfides,predictStr,callStr);

			}
		}
		return stats;
	}
	
	private static Map<String,File> getListInterfDatFiles(File dir, Set<String> pdbCodes) {
		Map<String,File> files = new HashMap<String,File>();
		for (String pdbCode:pdbCodes) {
			File interfFile = new File(dir,pdbCode+".interfaces.dat");
			if (interfFile.exists()) files.put(pdbCode,interfFile);
		}
		return files;
	}

	private static void printStats(int[] stats) {
		double accuracy = (double)stats[0]/((double)(stats[0]+stats[1]));
		System.out.println("Total: "+(stats[0]+stats[1])+", TP: "+stats[0]+", FN: "+stats[1]);
		System.out.printf("Accuracy: %4.2f\n",accuracy);
	}
	
//	private static int countGlycines(InterfaceRimCore rimcore) {
//		int count = 0;
//		for (Residue res:rimcore.getCoreResidues()) {
//			if (res instanceof AaResidue && (((AaResidue)res).getAaType()==AminoAcid.GLY)) {
//				count++;
//			}
//		}
//		return count;
//	}
	
	private static String getEvolCall(File chainevolfile, ChainInterface interf) throws IOException, ClassNotFoundException {
		if (!chainevolfile.exists()) return "No evol score calculated";
		
		ChainEvolContextList cecs = (ChainEvolContextList)Goodies.readFromFile(chainevolfile);
		ArrayList<ChainEvolContext> chainsEvCs = new ArrayList<ChainEvolContext>();
		chainsEvCs.add(cecs.getChainEvolContext(interf.getFirstMolecule().getPdbChainCode()));
		chainsEvCs.add(cecs.getChainEvolContext(interf.getSecondMolecule().getPdbChainCode()));
		InterfaceEvolContext iec = new InterfaceEvolContext(interf, chainsEvCs);
		
		iec.scoreEntropy(false);
		CallType entCall = iec.getCalls(BIO_CUTOFF, XTAL_CUTOFF, MIN_NUM_HOMOLOGS, 6, 3)[0];
		String entCallStr = entCall.getName() +"("+String.format("%4.2f", iec.getFinalScores()[0])+")";
		iec.scoreEntropy(true);
		entCall = iec.getCalls(BIO_CUTOFF, XTAL_CUTOFF, MIN_NUM_HOMOLOGS, 6, 3)[0];
		entCallStr += " "+entCall.getName() +"("+String.format("%4.2f", iec.getFinalScores()[0])+")";
		
		String kaksCallStr = CallType.NO_PREDICTION.getName()+" (can't do kaks)";
		if (iec.canDoCRK()) {
			iec.scoreKaKs(false);
			CallType kaksCall = iec.getCalls(BIO_CUTOFF, XTAL_CUTOFF, MIN_NUM_HOMOLOGS, 6, 3)[0];
			kaksCallStr = kaksCall.getName()+"("+String.format("%4.2f", iec.getFinalScores()[0])+")";
			iec.scoreKaKs(true);
			kaksCall = iec.getCalls(BIO_CUTOFF, XTAL_CUTOFF, MIN_NUM_HOMOLOGS, 6, 3)[0];
			kaksCallStr += " "+kaksCall.getName()+"("+String.format("%4.2f", iec.getFinalScores()[0])+")";
		}
		
		String callStr = entCallStr+" "+kaksCallStr;

		callStr += " homologs: "+iec.getFirstChainEvolContext().getNumHomologs()+","+iec.getSecondChainEvolContext().getNumHomologs();
			
		return callStr;
	}
}
