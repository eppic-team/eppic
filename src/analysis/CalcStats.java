package analysis;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import crk.CallType;
import crk.ChainEvolContext;
import crk.ChainEvolContextList;
import crk.GeometryPredictor;
import crk.InterfaceEvolContext;
import crk.ScoringType;

import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;

/**
 * Script to calculate statistics for sets of bio/xtal interfaces
 * It goes over directories (given with -B, -X) and list files (-b, -x), tries to find
 * for each entry a interfaces.dat and a chainevolcontext.dat file and from then calculate 
 * the scores for the parameters given (optionally with -t, -c, -m). Geometry scores and 
 * evolutionary scores (entropy and kaks) are calculated. 
 * Output is a list of one line per set with info about the set and prediction statistics:
 * total predictions, failed, tp, fn, accuracy, recall.
 * 
 * 
 * @author duarte_j
 *
 */
public class CalcStats {


	private static final String   PROGRAM_NAME ="CalcStats";
	
	private static final int MIN_NUM_HOMOLOGS = 10;
	
	private static final double CA_SOFT_CUTOFF_ZOOMING = 0.95;
	private static final double CA_HARD_CUTOFF_ZOOMING = 0.82;
	private static final double CA_RELAX_STEP_ZOOMING = 0.01;
	private static final int MIN_NUMBER_CORE_RESIDUES_ZOOMING = 6;
	
	private static final double DEFBIOCALLCUTOFF = 0.85;
	private static final double DEFCACUTOFF = 0.95;
	private static final int DEFMINNUMBERCORERESFORBIO = 6;

	private static File bioDir    = null;
	private static File xtalDir   = null;
	private static File bioList   = null;
	private static File xtalList  = null;
	
	private static double[] bioCallCutoffs = {DEFBIOCALLCUTOFF};
	private static double[] caCutoffs = {DEFCACUTOFF};
	private static double[] caCutoffsEvol = {DEFCACUTOFF};
	private static int[] minNumberCoreResForBios = {DEFMINNUMBERCORERESFORBIO};

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		
		String help = "Usage: \n" +
		PROGRAM_NAME+"\n" +
		"   -B         :  input dir containing the interfaces given with -b\n" +
		"   -X         :  input dir containing the interfaces given with -x\n"+
		"   -b         :  list file containing all the pdbIds + interface serials to \n" +
		"                 analyse that are known to be true bio contacts\n" +
		"   -x         :  list file containing all the pdbIds + interface serials to \n" +
		"                 analyse that are known to be true xtal contacts\n" +
		"   [-t]       :  evolutionary score cutoffs to call bio/xtal, comma separated. If omitted\n" +
		"                 then only one used: "+String.format("%4.2f",DEFBIOCALLCUTOFF)+"\n" +
		"   [-c]       :  core assignment cutoffs for geometry scoring, comma separated. If " +
		"                 omitted then only one used: "+String.format("%4.2f",DEFCACUTOFF)+"\n" +
	    "   [-C]       :  core assignment cutoffs for evolutionary scoring, comma separated. If\n" +
	    "                 omitted then only one used: "+String.format("%4.2f",DEFCACUTOFF)+"\n" +
		"   [-m]       :  minimum number of core residues for calling bio, comma separated.\n" +
		"                 If omitted only one used: "+DEFMINNUMBERCORERESFORBIO+"\n\n";

		Getopt g = new Getopt(PROGRAM_NAME, args, "B:X:b:x:t:c:C:m:h?");
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
			case 't':
				String[] tokens = g.getOptarg().split(",");
				bioCallCutoffs = new double[tokens.length];
				for (int i=0;i<tokens.length;i++) {
					bioCallCutoffs[i] = Double.parseDouble(tokens[i]);
				}
				break;
			case 'c':
				tokens = g.getOptarg().split(",");
				caCutoffs = new double[tokens.length];
				for (int i=0;i<tokens.length;i++) {
					caCutoffs[i] = Double.parseDouble(tokens[i]);
				}				
				break;
			case 'C':
				tokens = g.getOptarg().split(",");
				caCutoffsEvol = new double[tokens.length];
				for (int i=0;i<tokens.length;i++) {
					caCutoffsEvol[i] = Double.parseDouble(tokens[i]);
				}				
				break;				
			case 'm':
				tokens = g.getOptarg().split(",");
				minNumberCoreResForBios = new int[tokens.length];
				for (int i=0;i<tokens.length;i++) {
					minNumberCoreResForBios[i] = Integer.parseInt(tokens[i]);
				}				
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
		

		if (bioDir!=null) {
			TreeMap<String,List<Integer>> bioToAnalyse = Utils.readListFile(bioList);
			ArrayList<PredictionStatsSet> bioPreds = doPreds(bioToAnalyse,bioDir,CallType.BIO);
			
			System.out.println("Bio set: ");
			PredictionStatsSet.printHeader(System.out);
			for (PredictionStatsSet bioPred:bioPreds) {
				bioPred.print(System.out);
			}
		}
		
		
		if (xtalDir!=null) {
			TreeMap<String,List<Integer>> xtalToAnalyse = Utils.readListFile(xtalList);
			ArrayList<PredictionStatsSet> xtalPreds = doPreds(xtalToAnalyse,xtalDir,CallType.CRYSTAL);
			
			System.out.println("Xtal set: ");
			PredictionStatsSet.printHeader(System.out);
			for (PredictionStatsSet xtalPred:xtalPreds) {
				xtalPred.print(System.out);
			}
		}

		

		//System.out.println("Global: ");
		//printStats(globalStats);
		
		

	}
	
	private static ArrayList<PredictionStatsSet> doPreds(TreeMap<String,List<Integer>> toAnalyse, File dir, CallType truth) 
	throws IOException, ClassNotFoundException {
		
		// we pass only dir name and then read files   
		// we could read only once from outer loop here, keep them all in memory and calculate scores from there (would result in much simpler code)
		// but interfaces objects read from file are too big (~20-30MB) and we run out of memory quickly 
		// basically for big sets of 100 or more entries it is unfeasable

		ArrayList<PredictionStatsSet> preds = new ArrayList<PredictionStatsSet>();
		
		preds.addAll(doGeometryScoring(toAnalyse, dir, truth));
		preds.addAll(doEvolScoring(toAnalyse, dir, truth));
		
		return preds;
	}
	
	private static ArrayList<PredictionStatsSet> doGeometryScoring(TreeMap<String,List<Integer>> toAnalyse,	File dir, CallType truth) 
	throws IOException, ClassNotFoundException {
		
		// in order to read files just once, we need to loop in a slightly contrived way
		// it would be a lot easier to loop with the pdbCode/id as the inner-most loops and then generate the PredictionStatsSet object directly
		// doing it in this way we need to store the counts in arrays and then create the PredictionStatsSet objects in a separate loop

		int total = 0;
		for (List<Integer> vals:toAnalyse.values()) {
			total+=vals.size();
		}
		
		ArrayList<PredictionStatsSet> list = new ArrayList<PredictionStatsSet>();
		int[][] countBios = new int[caCutoffs.length][minNumberCoreResForBios.length];
		int[][] countXtals = new int[caCutoffs.length][minNumberCoreResForBios.length];

		for (String pdbCode:toAnalyse.keySet()) {
			File interfdatFile = new File(dir,pdbCode+".interfaces.dat");
			if (!interfdatFile.exists()) continue;
			ChainInterfaceList interfaces = Utils.readChainInterfaceList(interfdatFile);
			for (int id:toAnalyse.get(pdbCode)) {

				for (int i=0;i<caCutoffs.length;i++) {
					for (int j=0;j<minNumberCoreResForBios.length;j++) {
						ChainInterface interf = interfaces.get(id);
						GeometryPredictor gp = new GeometryPredictor(interf);
						gp.setBsaToAsaCutoff(caCutoffs[i]);
						gp.setMinCoreSizeForBio(minNumberCoreResForBios[j]);

						interf.calcRimAndCore(caCutoffs[i]);
						CallType call = gp.getCall();

						if (call==CallType.BIO) countBios[i][j]++;
						else if (call==CallType.CRYSTAL) countXtals[i][j]++;

					}
				}				
			}
		}

		for (int i=0;i<caCutoffs.length;i++) {
			for (int j=0;j<minNumberCoreResForBios.length;j++) {
				list.add(new PredictionStatsSet(dir.getName(),truth,ScoringType.GEOMETRY,false,false,
						caCutoffs[i],minNumberCoreResForBios[j],-1,countBios[i][j],countXtals[i][j],total));
			}
		}
		return list;
	}
	
	private static ArrayList<PredictionStatsSet> doEvolScoring(TreeMap<String,List<Integer>> toAnalyse,	File dir, CallType truth) 
	throws IOException, ClassNotFoundException {
		
		// in order to read files just once, we need to loop in a slightly contrived way
		// it would be a lot easier to loop with the pdbCode/id as the inner-most loops and then generate the PredictionStatsSet object directly
		// doing it in this way we need to store the counts in arrays and then create the PredictionStatsSet objects in a separate loop
		int total = 0;
		for (List<Integer> vals:toAnalyse.values()) {
			total+=vals.size();
		}
		
		ArrayList<PredictionStatsSet> list = new ArrayList<PredictionStatsSet>();
		// the 3 outer indices correspond to the 3 parameters scoType, weighted, zoomed (each can have 2 values)
		int[][][][][] countBios = new int[caCutoffsEvol.length][bioCallCutoffs.length][2][2][2];
		int[][][][][] countXtals = new int[caCutoffsEvol.length][bioCallCutoffs.length][2][2][2];
		
		for (String pdbCode:toAnalyse.keySet()) {
			File chainevolcontextdatFile = new File(dir,pdbCode+".chainevolcontext.dat");
			File interfdatFile = new File(dir,pdbCode+".interfaces.dat");
			if (!chainevolcontextdatFile.exists() || !interfdatFile.exists()) continue; // failed predictions

			ChainInterfaceList cil = Utils.readChainInterfaceList(interfdatFile);
			ChainEvolContextList cecl = Utils.readChainEvolContextList(chainevolcontextdatFile);
			for (int id:toAnalyse.get(pdbCode)) {

				for (int i=0;i<caCutoffsEvol.length;i++) {
					for (int k=0;k<bioCallCutoffs.length;k++) {

						ChainInterface interf = cil.get(id);

						ChainEvolContext[] chainsEvCs = new ChainEvolContext[2];
						chainsEvCs[0] = cecl.getChainEvolContext(interf.getFirstMolecule().getPdbChainCode());
						chainsEvCs[1] = cecl.getChainEvolContext(interf.getSecondMolecule().getPdbChainCode());
						InterfaceEvolContext iec = new InterfaceEvolContext(interf, chainsEvCs);

						if (i==0) {
							// we only do zoom predictions once per bioCallCutoff
							doSingleEvolScoring(iec, interf, ScoringType.ENTROPY, false, true, countBios, countXtals, i, k, 0, 0, 1);
							doSingleEvolScoring(iec, interf, ScoringType.ENTROPY, true, true, countBios, countXtals, i, k, 0, 1, 1);
							doSingleEvolScoring(iec, interf, ScoringType.KAKS, false, true, countBios, countXtals, i, k, 1, 0, 1);
							doSingleEvolScoring(iec, interf, ScoringType.KAKS, true, true, countBios, countXtals, i, k, 1, 1, 1);
						}

						doSingleEvolScoring(iec, interf, ScoringType.ENTROPY, false, false, countBios, countXtals, i, k, 0, 0, 0);
						doSingleEvolScoring(iec, interf, ScoringType.ENTROPY,  true, false, countBios, countXtals, i, k, 0, 1, 0);
						doSingleEvolScoring(iec, interf, ScoringType.KAKS,    false, false, countBios, countXtals, i, k, 1, 0, 0);
						doSingleEvolScoring(iec, interf, ScoringType.KAKS,     true, false, countBios, countXtals, i, k, 1, 1, 0);
					}
				}
			}
		}
		
		for (int i=0;i<caCutoffsEvol.length;i++) {
			for (int k=0;k<bioCallCutoffs.length;k++) {
				if (i==0) {
					// we only do zoom predictions once per bioCallCutoff
					list.add(new PredictionStatsSet(dir.getName(),truth,ScoringType.ENTROPY, false, true,
							-1,-1,bioCallCutoffs[k],countBios[i][k][0][0][1],countXtals[i][k][0][0][1],total));
					list.add(new PredictionStatsSet(dir.getName(),truth,ScoringType.ENTROPY, true, true,
							-1,-1,bioCallCutoffs[k],countBios[i][k][0][1][1],countXtals[i][k][0][1][1],total));
					list.add(new PredictionStatsSet(dir.getName(),truth,ScoringType.KAKS, false, true,
							-1,-1,bioCallCutoffs[k],countBios[i][k][1][0][1],countXtals[i][k][1][0][1],total));
					list.add(new PredictionStatsSet(dir.getName(),truth,ScoringType.KAKS, true, true,
							-1,-1,bioCallCutoffs[k],countBios[i][k][1][1][1],countXtals[i][k][1][1][1],total));
				}
				list.add(new PredictionStatsSet(dir.getName(),truth,ScoringType.ENTROPY, false, false,
						caCutoffsEvol[i],-1,bioCallCutoffs[k],countBios[i][k][0][0][0],countXtals[i][k][0][0][0],total));
				list.add(new PredictionStatsSet(dir.getName(),truth,ScoringType.ENTROPY, true, false,
						caCutoffsEvol[i],-1,bioCallCutoffs[k],countBios[i][k][0][1][0],countXtals[i][k][0][1][0],total));
				list.add(new PredictionStatsSet(dir.getName(),truth,ScoringType.KAKS, false, false,
						caCutoffsEvol[i],-1,bioCallCutoffs[k],countBios[i][k][1][0][0],countXtals[i][k][1][0][0],total));
				list.add(new PredictionStatsSet(dir.getName(),truth,ScoringType.KAKS, true, false,
						caCutoffsEvol[i],-1,bioCallCutoffs[k],countBios[i][k][1][1][0],countXtals[i][k][1][1][0],total));
			}
		}
		return list;
	}
	
	private static void doSingleEvolScoring(InterfaceEvolContext iec, ChainInterface interf, ScoringType scoType, boolean weighted, boolean zoomed, 
			int[][][][][] countBios, int[][][][][] countXtals, int i, int k, int l, int m, int n) {

		
		if (scoType==ScoringType.KAKS && !iec.canDoCRK()) return;

		if (zoomed) {
			interf.calcRimAndCore(CA_SOFT_CUTOFF_ZOOMING, CA_HARD_CUTOFF_ZOOMING, CA_RELAX_STEP_ZOOMING, MIN_NUMBER_CORE_RESIDUES_ZOOMING);
		} else {
			interf.calcRimAndCore(caCutoffsEvol[i]);
		}


		if (scoType==ScoringType.ENTROPY) {
			iec.scoreEntropy(weighted);
		} else if (scoType==ScoringType.KAKS) {
			iec.scoreKaKs(weighted);
		}
		iec.setBioCutoff(bioCallCutoffs[k]);
		iec.setXtalCutoff(bioCallCutoffs[k]);
		iec.setHomologsCutoff(MIN_NUM_HOMOLOGS);

		CallType call = iec.getCall();
		if (call==CallType.BIO) countBios[i][k][l][m][n]++;
		else if (call==CallType.CRYSTAL) countXtals[i][k][l][m][n]++;
		
		iec.resetCall();

	}
}
