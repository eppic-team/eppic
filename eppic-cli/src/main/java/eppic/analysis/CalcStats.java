package eppic.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.ChainEvolContextList;
import eppic.EppicParams;
import eppic.InterfaceEvolContext;
import eppic.InterfaceEvolContextList;
import eppic.ScoringType;
import eppic.commons.util.CallType;
import eppic.predictors.CombinedPredictor;
import eppic.predictors.EvolCoreSurfacePredictor;
import eppic.predictors.GeometryPredictor;
import eppic.predictors.InterfaceTypePredictor;
import gnu.getopt.Getopt;


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
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CalcStats.class);
	
	private static final int MIN_NUM_HOMOLOGS = EppicParams.DEF_MIN_NUM_SEQUENCES;
	
	private static final double DEFCORERIMCALLCUTOFF = EppicParams.DEF_CORERIM_SCORE_CUTOFF;
	private static final int DEFMINNUMBERCORERESFORBIO = EppicParams.DEF_MIN_CORE_SIZE_FOR_BIO;
	private static final double DEFZSCORECUTOFF = EppicParams.DEF_CORESURF_SCORE_CUTOFF;
	
	private static final double DEFCACUTOFF_FOR_G = EppicParams.DEF_CA_CUTOFF_FOR_GEOM;
	private static final double DEFCACUTOFF_FOR_Z = EppicParams.DEF_CA_CUTOFF_FOR_ZSCORE;
	private static final double DEFCACUTOFF_FOR_CR = EppicParams.DEF_CA_CUTOFF_FOR_RIMCORE;
	
	private static final double DEF_MIN_ASA_FOR_SURFACE = EppicParams.DEF_MIN_ASA_FOR_SURFACE;
	
	private static final boolean DEF_USE_PDB_RES_SER = EppicParams.DEF_USE_PDB_RES_SER;
		
	private static final String UNKNOWN_TAXON = "Unknown";
	
	private static final String[] TAXON_CLASSES = {"Eukaryota", "Bacteria", "Archaea", "Viruses", UNKNOWN_TAXON};
	

	private static File bioDir    = null;
	private static File xtalDir   = null;
	private static File bioList   = null;
	private static File xtalList  = null;
	
	private static boolean statsByTaxon = false;
	
	private static double[] corerimCallCutoffs = {DEFCORERIMCALLCUTOFF};
	private static int[] minNumberCoreResForBios = {DEFMINNUMBERCORERESFORBIO};
	private static double[] zscoreCutoffs = {DEFZSCORECUTOFF};
	
	private static double[] caCutoffsG = {DEFCACUTOFF_FOR_G};
	private static double[] caCutoffsCR = {DEFCACUTOFF_FOR_CR};
	private static double[] caCutoffsZ = {DEFCACUTOFF_FOR_Z};
	
	private static double minAsaForSurface = DEF_MIN_ASA_FOR_SURFACE;

	private static File outFile = null;
	private static TreeMap<String,List<Integer>> crPredicted;
	private static TreeMap<String,List<Integer>> zPredicted;

	private static File perInterfPredBioFile = null;
	private static File perInterfPredXtalFile = null;
	
		
	
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
	    "   [-s]       :  minimum ASA value to call a residue in surface. If omitted default value \n" +
	    "                 used: "+String.format("%4.1f",DEF_MIN_ASA_FOR_SURFACE)+"\n"+
		"   [-m]       :  minimum number of core residues for geometry calls: below xtal, equals\n" +
		"                 or above bio. Comma separated. If omitted only one used: "+DEFMINNUMBERCORERESFORBIO+"\n" +
		"   [-t]       :  evolutionary score core/rim ratio cutoffs to call bio/xtal, comma separated. If \n" +
		"                 omitted then only one used: "+String.format("%4.2f",DEFCORERIMCALLCUTOFF)+"\n" +
		"   [-y]       :  z-score cutoff to call bio/xtal, comma separated. If omitted\n" +
		"                 then only one used: "+String.format("%4.2f",DEFZSCORECUTOFF)+"\n" +
		"   [-w]       :  a file to write pdb_codes+interface_ids of interfaces that \n" +
		"                 could be predicted with both evolutionary methods\n" +
		"   [-d]       :  output also statistics by taxonomy group (domains of life)\n" +
		"   [-f]       :  file to write bio interfaces summary table of scores and calls \n" +
		"                 per interface\n" +
		"   [-F]       :  file to write xtal interfaces summary table of scores and calls \n" +
		"                 per interface\n\n";

		Getopt g = new Getopt(PROGRAM_NAME, args, "B:X:b:x:e:c:z:s:m:t:y:w:df:F:h?");
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
			case 's':
				minAsaForSurface = Double.parseDouble(g.getOptarg());
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
			case 'd':
				statsByTaxon = true;
				break;
			case 'f':
				perInterfPredBioFile = new File(g.getOptarg());
				break;
			case 'F':
				perInterfPredXtalFile = new File(g.getOptarg());
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
			
			TreeMap<String, InterfacePrediction> perInterfPreds = new TreeMap<String,InterfacePrediction>();			
			
			TreeMap<String,List<Integer>> bioToAnalyse = Utils.readListFile(bioList);
			bioPreds = doPreds(bioToAnalyse,bioDir,CallType.BIO,perInterfPreds);
			
			System.out.println("Bio set: ");
			PredictionStatsSet.printHeader(System.out);
			for (PredictionStatsSet bioPred:bioPreds) {
				bioPred.print(System.out);
				if (statsByTaxon) {
					for (String taxon:TAXON_CLASSES) {
						if (bioPred.getSubTotal(taxon)>0)
							bioPred.printByTaxon(System.out, taxon);
					}
				}
			}
			
			if (perInterfPredBioFile!=null) {
				printPerInterfacePreds(perInterfPredBioFile,perInterfPreds);
			}

		}
		
		
		ArrayList<PredictionStatsSet> xtalPreds = null;
		if (xtalDir!=null) {
			
			TreeMap<String, InterfacePrediction> perInterfPreds = new TreeMap<String,InterfacePrediction>();			
			
			TreeMap<String,List<Integer>> xtalToAnalyse = Utils.readListFile(xtalList);
			xtalPreds = doPreds(xtalToAnalyse,xtalDir,CallType.CRYSTAL,perInterfPreds);
			
			System.out.println("Xtal set: ");
			PredictionStatsSet.printHeader(System.out);
			for (PredictionStatsSet xtalPred:xtalPreds) {
				xtalPred.print(System.out);
				if (statsByTaxon) {
					for (String taxon:TAXON_CLASSES) {
						if (xtalPred.getSubTotal(taxon)>0) 
							xtalPred.printByTaxon(System.out, taxon);
					}
				}
			}
			if (perInterfPredXtalFile!=null) {
				printPerInterfacePreds(perInterfPredXtalFile,perInterfPreds);
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
					if (smallest.get(key).size()>1) {
						// many members, we print them all
						for (int id:smallest.get(key)) {
							out.print(" "+id);
						}						
					} else {
						// if only 1 member, then we only print it if it's not 1
						for (int id:smallest.get(key)) {
							if (id!=1) out.print(" "+id);
						}												
					}
					out.println();
				}
			}
			out.close();
		}
		
		
		

	}
	
	private static void printPerInterfacePreds(File file, TreeMap<String,InterfacePrediction> perInterfPreds) throws FileNotFoundException {
		PrintStream ps = new PrintStream(file);
		for (InterfacePrediction perInterfPred:perInterfPreds.values()) {
			perInterfPred.printTabular(ps);
		}
		ps.close();

	}
	
	private static ArrayList<PredictionStatsSet> doPreds(TreeMap<String,List<Integer>> toAnalyse, File dir, CallType truth, TreeMap<String,InterfacePrediction> perInterfPreds) 
	throws IOException, ClassNotFoundException {
		
		// we pass only dir name and then read files   
		// we could read only once from outer loop here, keep them all in memory and calculate scores from there (would result in much simpler code)
		// but interfaces objects read from file are too big (~20-30MB) and we run out of memory quickly 
		// basically for big sets of 100 or more entries it is unfeasable

		ArrayList<PredictionStatsSet> preds = new ArrayList<PredictionStatsSet>();
		
		preds.addAll(doGeometryScoring(toAnalyse, dir, truth, perInterfPreds));
		preds.addAll(doEvolScoring(toAnalyse, dir, truth, perInterfPreds));
		
		return preds;
	}
	
	private static ArrayList<PredictionStatsSet> doGeometryScoring(TreeMap<String,List<Integer>> toAnalyse,	File dir, CallType truth, TreeMap<String,InterfacePrediction> perInterfPreds) 
	throws IOException, ClassNotFoundException {
		
		// in order to read files just once, we need to loop in a slightly contrived way
		// it would be a lot easier to loop with the pdbCode/id as the inner-most loops and then generate the PredictionStatsSet object directly
		// doing it in this way we need to store the counts in arrays and then create the PredictionStatsSet objects in a separate loop

		int total = 0;
		for (List<Integer> vals:toAnalyse.values()) {
			total+=vals.size();
		}
		

		
		PredCounter[][] counters = new PredCounter[caCutoffsG.length][minNumberCoreResForBios.length];
		initialiseCounters(counters,total);

		for (String pdbCode:toAnalyse.keySet()) {
			File interfdatFile = new File(dir,pdbCode+".interfaces.dat");
			if (!interfdatFile.exists()) continue;
			StructureInterfaceList interfaces = Utils.readChainInterfaceList(interfdatFile);
			
			for (int id:toAnalyse.get(pdbCode)) {
				
				String interfStr = pdbCode+"_"+id;
				if (!perInterfPreds.containsKey(interfStr)) {
					InterfacePrediction perInterfPred = new InterfacePrediction();
					perInterfPred.setPdbCode(pdbCode);
					perInterfPred.setInterfaceId(id);
					perInterfPreds.put(interfStr, perInterfPred);
				}
				InterfacePrediction perInterfPred = perInterfPreds.get(interfStr);
				
				
				for (int i=0;i<caCutoffsG.length;i++) {
					for (int j=0;j<minNumberCoreResForBios.length;j++) {											
						
						StructureInterface interf = interfaces.get(id);
						GeometryPredictor gp = new GeometryPredictor(interf);
						gp.setBsaToAsaCutoff(caCutoffsG[i]);
						gp.setMinAsaForSurface(minAsaForSurface);
						gp.setMinCoreSizeForBio(minNumberCoreResForBios[j]);

						CallType call = gp.getCall();

						if (call==CallType.BIO) counters[i][j].countBio(UNKNOWN_TAXON);
						else if (call==CallType.CRYSTAL) counters[i][j].countXtal(UNKNOWN_TAXON);
						
						if (i==0 && j==0) {
							// only using first pair of cutoffs for the per interface predictions file
							perInterfPred.setArea(interf.getTotalArea());
							perInterfPred.setGeomCall(call);
							perInterfPred.setGeomScore(gp.getScore());
						}						


					}
				}				
			}
		}

		ArrayList<PredictionStatsSet> list = new ArrayList<PredictionStatsSet>();
		for (int i=0;i<caCutoffsG.length;i++) {
			for (int j=0;j<minNumberCoreResForBios.length;j++) {
				list.add(new PredictionStatsSet(dir.getName(),truth,ScoringType.GEOMETRY,
						String.format("%4.2f",caCutoffsG[i]),String.format("%d",minNumberCoreResForBios[j]),
						counters[i][j]));
			}
		}
		return list;
	}
	
	private static ArrayList<PredictionStatsSet> doEvolScoring(TreeMap<String,List<Integer>> toAnalyse,	File dir, CallType truth, TreeMap<String,InterfacePrediction> perInterfPreds) 
	throws IOException, ClassNotFoundException {
		
		// in order to read files just once, we need to loop in a slightly contrived way
		// it would be a lot easier to loop with the pdbCode/id as the inner-most loops and then generate the PredictionStatsSet object directly
		// doing it in this way we need to store the counts in arrays and then create the PredictionStatsSet objects in a separate loop
		int total = 0;
		for (List<Integer> vals:toAnalyse.values()) {
			total+=vals.size();
		}
		

		// counts for core/rim scoring
		PredCounter[][] countersCR = new PredCounter[caCutoffsCR.length][corerimCallCutoffs.length];
		initialiseCounters(countersCR, total);
		// counts for z-score scoring
		PredCounter[][] countersZ = new PredCounter[caCutoffsZ.length][zscoreCutoffs.length];
		initialiseCounters(countersZ, total);
		// counts for combined scoring: fixed geom cutoffs, 2 indices for core/rim, 2 indices for z-scoring
		PredCounter[][][][] countersComb = new PredCounter[caCutoffsCR.length][corerimCallCutoffs.length][caCutoffsZ.length][zscoreCutoffs.length];
		initialiseCounters(countersComb, total);

		
		for (String pdbCode:toAnalyse.keySet()) {
			File chainevolcontextdatFile = new File(dir,pdbCode+".chainevolcontext.dat");
			File interfdatFile = new File(dir,pdbCode+".interfaces.dat");
			if (!chainevolcontextdatFile.exists() && !interfdatFile.exists()) {
				LOGGER.warn("Both chainevolcontext.dat and interf.dat files missing for "+pdbCode);
				continue; // failed predictions
			} else if (!chainevolcontextdatFile.exists()) {
				LOGGER.warn("chainevolcontext.dat missing but interf.dat file present for "+pdbCode);
				continue;
			} else if (!interfdatFile.exists()) {
				LOGGER.warn("interf.dat missing but chainevolcontext.dat file present for "+pdbCode);
				continue;				
			}

			StructureInterfaceList cil = Utils.readChainInterfaceList(interfdatFile);
			ChainEvolContextList cecl = Utils.readChainEvolContextList(chainevolcontextdatFile);
			InterfaceEvolContextList iecList = new InterfaceEvolContextList(cil, cecl);
			iecList.setUsePdbResSer(DEF_USE_PDB_RES_SER);
			iecList.setMinNumSeqs(MIN_NUM_HOMOLOGS);
			
			String dol = cecl.getDomainOfLife();
			if (dol==null) dol = UNKNOWN_TAXON;
			
			for (int id:toAnalyse.get(pdbCode)) {
				
				String interfStr = pdbCode+"_"+id;
				if (!perInterfPreds.containsKey(interfStr)) {
					InterfacePrediction perInterfPred = new InterfacePrediction();
					perInterfPred.setPdbCode(pdbCode);
					perInterfPred.setInterfaceId(id);
					perInterfPreds.put(interfStr, perInterfPred);
				}
				InterfacePrediction perInterfPred = perInterfPreds.get(interfStr);

				
				countSubTotal(countersCR, dol);
				countSubTotal(countersZ, dol);
				countSubTotal(countersComb, dol);
				
				StructureInterface interf = cil.get(id);
				InterfaceEvolContext iec = iecList.get(id-1);

				// evol z-score scoring
				for (int i=0;i<caCutoffsZ.length;i++) {
					for (int k=0;k<zscoreCutoffs.length;k++) {						
						doSingleEvolZScoring(pdbCode, iec, interf, countersZ, i, k, dol, perInterfPred);
					}
				}
				
				// combined scoring
				for (int i=0;i<caCutoffsCR.length;i++) {
					for (int k=0;k<corerimCallCutoffs.length;k++) {
						for (int l=0;l<caCutoffsZ.length;l++) {
							for (int m=0;m<zscoreCutoffs.length;m++) {
								doSingleCombinedScoring(iec, interf, countersComb, i, k, l, m, dol, perInterfPred);
							}
						}
					}
				}
			}
		}
		
		
		ArrayList<PredictionStatsSet> list = new ArrayList<PredictionStatsSet>();
		
		for (int i=0;i<caCutoffsCR.length;i++) {
			for (int k=0;k<corerimCallCutoffs.length;k++) {
				
				list.add(new PredictionStatsSet(dir.getName(),truth,ScoringType.CORERIM, 
						String.format("%4.2f",caCutoffsCR[i]),String.format("%4.2f",corerimCallCutoffs[k]),
						countersCR[i][k]));
				
			}
		}
		
		for (int i=0;i<caCutoffsZ.length;i++) {
			for (int k=0;k<zscoreCutoffs.length;k++) {
				list.add(new PredictionStatsSet(dir.getName(),truth,ScoringType.CORESURFACE, 
						String.format("%4.2f",caCutoffsZ[i]),String.format("%+5.2f",zscoreCutoffs[k]),
						countersZ[i][k]));
			}
		}
		
		for (int i=0;i<caCutoffsCR.length;i++) {
			for (int k=0;k<corerimCallCutoffs.length;k++) {
				for (int l=0;l<caCutoffsZ.length;l++) {
					for (int m=0;m<zscoreCutoffs.length;m++) {
						String caCutoffs = String.format("%4.2f,%4.2f",caCutoffsCR[i],caCutoffsZ[l]);
						String callCutoffs = String.format("%4.2f,%+5.2f",corerimCallCutoffs[k],zscoreCutoffs[m]);
						list.add(new PredictionStatsSet(dir.getName(),truth,ScoringType.COMBINED, 
								caCutoffs,callCutoffs,
								countersComb[i][k][l][m]));
					
					}
				}
			}
		}
		
		return list;
	}
	
	private static void doSingleEvolZScoring(String pdbCode, InterfaceEvolContext iec, StructureInterface interf,
			PredCounter[][] counters, int i, int k, String dol, InterfacePrediction perInterfPred) {
		
		//interf.calcRimAndCore(caCutoffsZ[i]);
		
		EvolCoreSurfacePredictor eizp = new EvolCoreSurfacePredictor(iec);
		eizp.setBsaToAsaCutoff(caCutoffsZ[i], minAsaForSurface);
		
		eizp.computeScores();
		eizp.setCallCutoff(zscoreCutoffs[k]);
		
		CallType call = eizp.getCall();
		if (call==CallType.BIO) counters[i][k].countBio(dol);
		else if (call==CallType.CRYSTAL) counters[i][k].countXtal(dol); 
		
		perInterfPred.setzCall(call);
		perInterfPred.setzScore(eizp.getScore());

		
		if (outFile!=null) {
			if (call==CallType.BIO || call==CallType.CRYSTAL) {
				String key = pdbCode;
				if (zPredicted.containsKey(key)) {
					zPredicted.get(key).add(interf.getId());
				} else {
					List<Integer> list = new ArrayList<Integer>();
					list.add(interf.getId());
					zPredicted.put(key,list);
				}
			}						
		}
		
		
	}
	
	private static void doSingleCombinedScoring(InterfaceEvolContext iec, StructureInterface interf, 
			PredCounter[][][][] counters, int i, int k, int l, int m, String dol, InterfacePrediction perInterfPred) {

		GeometryPredictor gp = new GeometryPredictor(interf);
		//gp.setBsaToAsaCutoff(caCutoffsG[i]);
		gp.setBsaToAsaCutoff(DEFCACUTOFF_FOR_G);
		gp.setMinAsaForSurface(minAsaForSurface);
		//gp.setMinCoreSizeForBio(minNumberCoreResForBios[j]);
		gp.setMinCoreSizeForBio(DEFMINNUMBERCORERESFORBIO);
		gp.computeScores();
		
		EvolCoreSurfacePredictor eizp = new EvolCoreSurfacePredictor(iec);
		eizp.setBsaToAsaCutoff(caCutoffsZ[l], minAsaForSurface);
		eizp.computeScores();
		eizp.setCallCutoff(zscoreCutoffs[m]);
		
		InterfaceTypePredictor cp = new CombinedPredictor(iec, gp, eizp);

		cp.computeScores();
		CallType call = cp.getCall();
		if (call==CallType.BIO) counters[i][k][l][m].countBio(dol);
		else if (call==CallType.CRYSTAL) counters[i][k][l][m].countXtal(dol);

		perInterfPred.setCombCall(call);
		perInterfPred.setCombScore(cp.getScore());
		
		
	}
	

	
	private static void initialiseCounters(PredCounter[][] counters, int total) {
		for (int i=0;i<counters.length;i++) {
			for (int j=0;j<counters[i].length;j++) {
				counters[i][j] = new PredCounter(TAXON_CLASSES);
				counters[i][j].setTotal(total);
			}
		}
	}
	
	private static void countSubTotal(PredCounter[][] counters, String taxon) {
		for (int i=0;i<counters.length;i++) {
			for (int j=0;j<counters[i].length;j++) {
				counters[i][j].count(taxon);
			}
		}		
	}
	
	private static void initialiseCounters(PredCounter[][][][] counters, int total) {
		for (int i=0;i<counters.length;i++) {
			for (int j=0;j<counters[i].length;j++) {
				for (int k=0;k<counters[i][j].length;k++) {
					for (int l=0;l<counters[i][j][k].length;l++) {				
						counters[i][j][k][l] = new PredCounter(TAXON_CLASSES);
						counters[i][j][k][l].setTotal(total);
					}
				}
			}
		}
	}
	
	private static void countSubTotal(PredCounter[][][][] counters, String taxon) {
		for (int i=0;i<counters.length;i++) {
			for (int j=0;j<counters[i].length;j++) {
				for (int k=0;k<counters[i][j].length;k++) {
					for (int l=0;l<counters[i][j][k].length;l++) {				
						counters[i][j][k][l].count(taxon);
					}
				}
			}
		}
	}
}
