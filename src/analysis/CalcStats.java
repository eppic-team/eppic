package analysis;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import crk.CRKParams;
import crk.CallType;
import crk.ChainEvolContextList;
import crk.InterfaceEvolContext;
import crk.InterfaceEvolContextList;
import crk.ScoringType;
import crk.predictors.CombinedPredictor;
import crk.predictors.EvolInterfZPredictor;
import crk.predictors.EvolRimCorePredictor;
import crk.predictors.GeometryPredictor;

import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;

/**
 * Script to calculate statistics for sets of bio/xtal interfaces
 * It goes over directories (given with -B, -X) and list files (-b, -x), tries to find
 * for each entry a interfaces.dat and a chainevolcontext.dat file and from them calculate 
 * the scores for the parameters given (optionally with -e, -c, -z, -m, -t, -y). 
 * Geometry scores (parameters -e, -m), evolutionary core/rim ratio scores (entropy and kaks, 
 * parameters -c, -t) and evolutionary z-scores (parameters -z, -y) 
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
	
	private static final double DEFCORERIMCALLCUTOFF = CRKParams.DEF_ENTR_CALL_CUTOFF;
	private static final int DEFMINNUMBERCORERESFORBIO = CRKParams.DEF_MIN_CORE_SIZE_FOR_BIO;
	private static final double DEFZSCORECUTOFF = CRKParams.DEF_ZSCORE_CUTOFF;
	
	private static final double DEFCACUTOFF_FOR_G = CRKParams.DEF_CA_CUTOFF_FOR_GEOM;
	private static final double DEFCACUTOFF_FOR_Z = CRKParams.DEF_CA_CUTOFF_FOR_ZSCORE;
	private static final double DEFCACUTOFF_FOR_CR = CRKParams.DEF_CA_CUTOFF_FOR_RIMCORE;
	

	private static File bioDir    = null;
	private static File xtalDir   = null;
	private static File bioList   = null;
	private static File xtalList  = null;
	
	private static double[] corerimCallCutoffs = {DEFCORERIMCALLCUTOFF};
	private static int[] minNumberCoreResForBios = {DEFMINNUMBERCORERESFORBIO};
	private static double[] zscoreCutoffs = {DEFZSCORECUTOFF};
	
	private static double[] caCutoffsG = {DEFCACUTOFF_FOR_G};
	private static double[] caCutoffsCR = {DEFCACUTOFF_FOR_CR};
	private static double[] caCutoffsZ = {DEFCACUTOFF_FOR_Z};

	private static File outFile = null;
	private static TreeMap<String,List<Integer>> crPredicted;
	private static TreeMap<String,List<Integer>> zPredicted;

	
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
		"   [-e]       :  core assignment cutoffs for geometry scoring, comma separated. If\n" +
		"                 omitted then only one used: "+String.format("%4.2f",DEFCACUTOFF_FOR_G)+"\n" +
	    "   [-c]       :  core assignment cutoffs for evolutionary core/rim scoring, comma separated.\n" +
	    "                 If omitted then only one used: "+String.format("%4.2f",DEFCACUTOFF_FOR_CR)+"\n" +
	    "   [-z]       :  core assignemnt cutoffs for evolutionary z-scoring, comma separated. If \n" +
	    "                 omitted then only one used: "+String.format("%4.2f", DEFCACUTOFF_FOR_Z)+"\n"+
		"   [-m]       :  minimum number of core residues for geometry calls: below xtal, equals\n" +
		"                 or above bio. Comma separated. If omitted only one used: "+DEFMINNUMBERCORERESFORBIO+"\n" +
		"   [-t]       :  evolutionary score core/rim ratio cutoffs to call bio/xtal, comma separated. If \n" +
		"                 omitted then only one used: "+String.format("%4.2f",DEFCORERIMCALLCUTOFF)+"\n" +
		"   [-y]       :  z-score cutoff to call bio/xtal, comma separated. If omitted\n" +
		"                 then only one used: "+String.format("%4.2f",DEFZSCORECUTOFF)+"\n" +
		"   [-w]       :  a file to write pdb_codes+interface_ids of interfaces that \n" +
		"                 could be predicted with both evolutionary methods\n\n";

		Getopt g = new Getopt(PROGRAM_NAME, args, "B:X:b:x:e:c:z:m:t:y:w:h?");
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
			case 'e':
				String[] tokens = g.getOptarg().split(",");
				caCutoffsG = new double[tokens.length];
				for (int i=0;i<tokens.length;i++) {
					caCutoffsG[i] = Double.parseDouble(tokens[i]);
				}				
				break;
			case 'c':
				tokens = g.getOptarg().split(",");
				caCutoffsCR = new double[tokens.length];
				for (int i=0;i<tokens.length;i++) {
					caCutoffsCR[i] = Double.parseDouble(tokens[i]);
				}				
				break;
			case 'z':
				tokens = g.getOptarg().split(",");
				caCutoffsZ = new double[tokens.length];
				for (int i=0;i<tokens.length;i++) {
					caCutoffsZ[i] = Double.parseDouble(tokens[i]);
				}				
				break;
			case 'm':
				tokens = g.getOptarg().split(",");
				minNumberCoreResForBios = new int[tokens.length];
				for (int i=0;i<tokens.length;i++) {
					minNumberCoreResForBios[i] = Integer.parseInt(tokens[i]);
				}				
				break;
			case 't':
				tokens = g.getOptarg().split(",");
				corerimCallCutoffs = new double[tokens.length];
				for (int i=0;i<tokens.length;i++) {
					corerimCallCutoffs[i] = Double.parseDouble(tokens[i]);
				}
				break;
			case 'y':
				tokens = g.getOptarg().split(",");
				zscoreCutoffs = new double[tokens.length];
				for (int i=0;i<tokens.length;i++) {
					zscoreCutoffs[i] = Double.parseDouble(tokens[i]);
				}
				break;
			case 'w':
				outFile = new File(g.getOptarg());
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
		

		if (outFile!=null) {
			crPredicted = new TreeMap<String, List<Integer>>();
			zPredicted = new TreeMap<String, List<Integer>>();
		}
		
		ArrayList<PredictionStatsSet> bioPreds = null;
		if (bioDir!=null) {
			TreeMap<String,List<Integer>> bioToAnalyse = Utils.readListFile(bioList);
			bioPreds = doPreds(bioToAnalyse,bioDir,CallType.BIO);
			
			System.out.println("Bio set: ");
			PredictionStatsSet.printHeader(System.out);
			for (PredictionStatsSet bioPred:bioPreds) {
				bioPred.print(System.out);
			}
		}
		
		
		ArrayList<PredictionStatsSet> xtalPreds = null;
		if (xtalDir!=null) {
			TreeMap<String,List<Integer>> xtalToAnalyse = Utils.readListFile(xtalList);
			xtalPreds = doPreds(xtalToAnalyse,xtalDir,CallType.CRYSTAL);
			
			System.out.println("Xtal set: ");
			PredictionStatsSet.printHeader(System.out);
			for (PredictionStatsSet xtalPred:xtalPreds) {
				xtalPred.print(System.out);
			}
		}

		if (bioDir!=null && xtalDir!=null) {
			System.out.println("Global statistics for geometry and combined sets: ");
			PredictionStatsGlobalSet.printHeader(System.out);
			for (int i=0;i<bioPreds.size();i++) {
				// we don't do it for the purely evol methods because there's the issue that we can't predict all cases (not enough homologs)
				if (bioPreds.get(i).scoType==ScoringType.COMBINED ||
					bioPreds.get(i).scoType==ScoringType.GEOMETRY) {  
				
					PredictionStatsGlobalSet combPredStats = new PredictionStatsGlobalSet(bioPreds.get(i),xtalPreds.get(i));
					combPredStats.print(System.out);
				}
			}
		}

		if (outFile!=null) {
			PrintWriter out = new PrintWriter(outFile);
			TreeMap<String, List<Integer>> smallest = null;
			TreeMap<String, List<Integer>> other = null;
			if (crPredicted.size()<=zPredicted.size()) {
				smallest = crPredicted;
				other = zPredicted;
			} else {
				smallest = zPredicted;
				other = crPredicted;
			}			
					
			for (String key:smallest.keySet()) {
				if (other.containsKey(key)) {
					out.print(key);
					for (int id:smallest.get(key)) {
						if (id!=1) out.print(" "+id);
					}
					out.println();
				}
			}
			out.close();
		}
		

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
		int[][] countBios = new int[caCutoffsG.length][minNumberCoreResForBios.length];
		int[][] countXtals = new int[caCutoffsG.length][minNumberCoreResForBios.length];

		for (String pdbCode:toAnalyse.keySet()) {
			File interfdatFile = new File(dir,pdbCode+".interfaces.dat");
			if (!interfdatFile.exists()) continue;
			ChainInterfaceList interfaces = Utils.readChainInterfaceList(interfdatFile);
			for (int id:toAnalyse.get(pdbCode)) {

				for (int i=0;i<caCutoffsG.length;i++) {
					for (int j=0;j<minNumberCoreResForBios.length;j++) {
						ChainInterface interf = interfaces.get(id);
						GeometryPredictor gp = new GeometryPredictor(interf);
						gp.setBsaToAsaCutoff(caCutoffsG[i]);
						gp.setMinCoreSizeForBio(minNumberCoreResForBios[j]);

						interf.calcRimAndCore(caCutoffsG[i]);
						CallType call = gp.getCall();

						if (call==CallType.BIO) countBios[i][j]++;
						else if (call==CallType.CRYSTAL) countXtals[i][j]++;

					}
				}				
			}
		}

		for (int i=0;i<caCutoffsG.length;i++) {
			for (int j=0;j<minNumberCoreResForBios.length;j++) {
				list.add(new PredictionStatsSet(dir.getName(),truth,ScoringType.GEOMETRY,
						String.format("%4.2f",caCutoffsG[i]),String.format("%d",minNumberCoreResForBios[j]),
						countBios[i][j],countXtals[i][j],total));
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
		// counts for core/rim scoring
		int[][] countBiosCR = new int[caCutoffsCR.length][corerimCallCutoffs.length];
		int[][] countXtalsCR = new int[caCutoffsCR.length][corerimCallCutoffs.length];
		// counts for z-score scoring
		int[][] countBiosZ = new int[caCutoffsZ.length][zscoreCutoffs.length];
		int[][] countXtalsZ = new int[caCutoffsZ.length][zscoreCutoffs.length];
		// counts for combined scoring: fixed geom cutoffs, 2 indices for core/rim, 2 indices for z-scoring
		int[][][][] countBiosComb = new int[caCutoffsCR.length][corerimCallCutoffs.length][caCutoffsZ.length][zscoreCutoffs.length];
		int[][][][] countXtalsComb = new int[caCutoffsCR.length][corerimCallCutoffs.length][caCutoffsZ.length][zscoreCutoffs.length];
		
		for (String pdbCode:toAnalyse.keySet()) {
			File chainevolcontextdatFile = new File(dir,pdbCode+".chainevolcontext.dat");
			File interfdatFile = new File(dir,pdbCode+".interfaces.dat");
			if (!chainevolcontextdatFile.exists() && !interfdatFile.exists()) {
				System.err.println("Warning: both chainevolcontext.dat and interf.dat files missing for "+pdbCode);
				continue; // failed predictions
			} else if (!chainevolcontextdatFile.exists()) {
				System.err.println("Warning: chainevolcontext.dat missing but interf.dat file present for "+pdbCode);
				continue;
			} else if (!interfdatFile.exists()) {
				System.err.println("Warning: interf.dat missing but chainevolcontext.dat file present for "+pdbCode);
				continue;				
			}

			ChainInterfaceList cil = Utils.readChainInterfaceList(interfdatFile);
			ChainEvolContextList cecl = Utils.readChainEvolContextList(chainevolcontextdatFile);
			InterfaceEvolContextList iecList = new InterfaceEvolContextList(pdbCode, cil, cecl);
			
			for (int id:toAnalyse.get(pdbCode)) {

				ChainInterface interf = cil.get(id);
				InterfaceEvolContext iec = iecList.get(id-1);

				// evol core/rim scoring
				for (int i=0;i<caCutoffsCR.length;i++) {
					for (int k=0;k<corerimCallCutoffs.length;k++) {
						doSingleEvolCRScoring(iec, interf, ScoringType.ENTROPY, countBiosCR, countXtalsCR, i, k);
					}
				}
				
				// evol z-score scoring
				for (int i=0;i<caCutoffsZ.length;i++) {
					for (int k=0;k<zscoreCutoffs.length;k++) {						
						doSingleEvolZScoring(iec, interf, countBiosZ, countXtalsZ, i, k);
					}
				}
				
				// combined scoring
				for (int i=0;i<caCutoffsCR.length;i++) {
					for (int k=0;k<corerimCallCutoffs.length;k++) {
						for (int l=0;l<caCutoffsZ.length;l++) {
							for (int m=0;m<zscoreCutoffs.length;m++) {
								doSingleCombinedScoring(iec, interf, countBiosComb, countXtalsComb, i, k, l, m);
							}
						}
					}
				}
			}
		}
		
		
		for (int i=0;i<caCutoffsCR.length;i++) {
			for (int k=0;k<corerimCallCutoffs.length;k++) {
				
				list.add(new PredictionStatsSet(dir.getName(),truth,ScoringType.ENTROPY, 
						String.format("%4.2f",caCutoffsCR[i]),String.format("%4.2f",corerimCallCutoffs[k]),
						countBiosCR[i][k],countXtalsCR[i][k],total));
				
			}
		}
		
		for (int i=0;i<caCutoffsZ.length;i++) {
			for (int k=0;k<zscoreCutoffs.length;k++) {
				list.add(new PredictionStatsSet(dir.getName(),truth,ScoringType.ZSCORE, 
						String.format("%4.2f",caCutoffsZ[i]),String.format("%+5.2f",zscoreCutoffs[k]),
						countBiosZ[i][k],countXtalsZ[i][k],total));
			}
		}
		
		for (int i=0;i<caCutoffsCR.length;i++) {
			for (int k=0;k<corerimCallCutoffs.length;k++) {
				for (int l=0;l<caCutoffsZ.length;l++) {
					for (int m=0;m<zscoreCutoffs.length;m++) {
						String caCutoffs = String.format("%4.2f,%4.2f",caCutoffsCR[i],caCutoffsZ[l]);
						String callCutoffs = String.format("%4.2f,%+5.2f",corerimCallCutoffs[k],zscoreCutoffs[m]);
						list.add(new PredictionStatsSet(dir.getName(),truth,ScoringType.COMBINED, 
								caCutoffs,callCutoffs,countBiosComb[i][k][l][m],countXtalsComb[i][k][l][m],total));
					
					}
				}
			}
		}
		
		return list;
	}
	
	private static void doSingleEvolCRScoring(InterfaceEvolContext iec, ChainInterface interf, ScoringType scoType,   
			int[][] countBios, int[][] countXtals, int i, int k) {

		
		
		//interf.calcRimAndCore(caCutoffsCR[i]);

		EvolRimCorePredictor ercp = new EvolRimCorePredictor(iec);
		ercp.setBsaToAsaCutoff(caCutoffsCR[i]);
		
		if (scoType==ScoringType.ENTROPY) {
			ercp.scoreEntropy(false);
		} 
		
		ercp.setCallCutoff(corerimCallCutoffs[k]);
		iec.setMinNumSeqs(MIN_NUM_HOMOLOGS);

		CallType call = ercp.getCall();
		if (call==CallType.BIO) countBios[i][k]++;
		else if (call==CallType.CRYSTAL) countXtals[i][k]++;
		
		if (outFile!=null) {
			if (call==CallType.BIO || call==CallType.CRYSTAL) {
				String key = interf.getFirstMolecule().getPdbCode();
				if (crPredicted.containsKey(key)) {
					crPredicted.get(key).add(interf.getId());
				} else {
					List<Integer> list = new ArrayList<Integer>();
					list.add(interf.getId());
					crPredicted.put(key,list);
				}
			}						
		}
		
		ercp.resetCall();

	}
	
	private static void doSingleEvolZScoring(InterfaceEvolContext iec, ChainInterface interf, 
			int[][] countBios, int[][] countXtals, int i, int k) {
		
		//interf.calcRimAndCore(caCutoffsZ[i]);
		
		EvolInterfZPredictor eizp = new EvolInterfZPredictor(iec);
		eizp.setBsaToAsaCutoff(caCutoffsZ[i]);
		
		eizp.scoreEntropy();
		eizp.setZscoreCutoff(zscoreCutoffs[k]);
		iec.setMinNumSeqs(MIN_NUM_HOMOLOGS);
		
		CallType call = eizp.getCall();
		if (call==CallType.BIO) countBios[i][k]++;
		else if (call==CallType.CRYSTAL) countXtals[i][k]++;
		
		if (outFile!=null) {
			if (call==CallType.BIO || call==CallType.CRYSTAL) {
				String key = interf.getFirstMolecule().getPdbCode();
				if (zPredicted.containsKey(key)) {
					zPredicted.get(key).add(interf.getId());
				} else {
					List<Integer> list = new ArrayList<Integer>();
					list.add(interf.getId());
					zPredicted.put(key,list);
				}
			}						
		}
		
		eizp.resetCall();
		
	}
	
	private static void doSingleCombinedScoring(InterfaceEvolContext iec, ChainInterface interf, 
			int[][][][] countBios, int[][][][] countXtals, int i, int k, int l, int m) {

		//interf.calcRimAndCore(caCutoffsG[i]);
		interf.calcRimAndCore(DEFCACUTOFF_FOR_G);
		GeometryPredictor gp = new GeometryPredictor(interf);
		//gp.setBsaToAsaCutoff(caCutoffsG[i]);
		gp.setBsaToAsaCutoff(DEFCACUTOFF_FOR_G);
		//gp.setMinCoreSizeForBio(minNumberCoreResForBios[j]);
		gp.setMinCoreSizeForBio(DEFMINNUMBERCORERESFORBIO);
		
		//interf.calcRimAndCore(caCutoffsCR[i]);
		EvolRimCorePredictor ercp = new EvolRimCorePredictor(iec);
		ercp.setBsaToAsaCutoff(caCutoffsCR[i]);
		ercp.scoreEntropy(false);
		ercp.setCallCutoff(corerimCallCutoffs[k]);
		iec.setMinNumSeqs(MIN_NUM_HOMOLOGS);
		
		//interf.calcRimAndCore(caCutoffsZ[l]);
		EvolInterfZPredictor eizp = new EvolInterfZPredictor(iec);
		eizp.setBsaToAsaCutoff(caCutoffsZ[l]);
		eizp.scoreEntropy();
		eizp.setZscoreCutoff(zscoreCutoffs[m]);
		iec.setMinNumSeqs(MIN_NUM_HOMOLOGS);
		
		CombinedPredictor cp = new CombinedPredictor(iec, gp, ercp, eizp);

		CallType call = cp.getCall();
		if (call==CallType.BIO) countBios[i][k][l][m]++;
		else if (call==CallType.CRYSTAL) countXtals[i][k][l][m]++;
		else {
			// do nothing, just a place holder for debugging
			return;
		}
		
		
		
	}
}
