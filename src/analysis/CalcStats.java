package analysis;

import gnu.getopt.Getopt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import crk.CallType;
import crk.InterfaceEvolContextList;
import crk.InterfaceScore;
import crk.PdbScore;

import owl.core.util.FileFormatException;
import owl.core.util.RegexFileFilter;

public class CalcStats {


	private static final String   PROGRAM_NAME ="CalcStats";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		File bioDir    = null;
		File xtalDir   = null;
		//CallType truth = null;
		File bioList   = null;
		File xtalList  = null;
		int maxIndex = 1;
		int minIndexForZoomingCall = 0;
		
		String help = "Usage: \n" +
		PROGRAM_NAME+"\n" +
		"   -B         :  input dir containing the interfaces given with -b\n" +
		"   -X         :  input dir containing the interfaces given with -x\n"+
		"   -b         :  list file containing all the pdbIds + interface serials to \n" +
		"                 analyse that are known to be true bio contacts\n" +
		"   -x         :  list file containing all the pdbIds + interface serials to \n" +
		"                 analyse that are known to be true xtal contacts\n"+
		"   [-m <int>] :  max index of the scoring files (when using several bio/xtal \n" +
		"                 call cutoffs)\n" +
		"   [-z <int>] :  the minimum index of the bsaToAsaCutoffs array to use for zooming call \n\n";

		Getopt g = new Getopt(PROGRAM_NAME, args, "B:X:b:x:m:z:h?");
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
				maxIndex = Integer.parseInt(g.getOptarg());
				break;
			case 'z':
				minIndexForZoomingCall = Integer.parseInt(g.getOptarg());
				break;
			case 'h':
			case '?':
				System.out.println(help);
				System.exit(0);
				break; // getopt() already printed an error
			}
		}
		
		if (bioDir==null || bioList==null || xtalDir==null || xtalList==null) {
			System.err.println("Must specify a bio list, a bio dir, a xtal list and xtal dir");
			System.exit(1);
		}
		
		// read the list files containing the interfaces to analyse
		TreeMap<String,List<Integer>> bioToAnalyse = readListFile(bioList);
		TreeMap<String,List<Integer>> xtalToAnalyse = readListFile(xtalList);
		int total = 0;
		for (List<Integer> vals:bioToAnalyse.values()) {
			total+=vals.size();
		}
		for (List<Integer> vals:xtalToAnalyse.values()) {
			total+=vals.size();
		}

		System.out.printf("%6s\t%4s\t%4s\t%5s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\n",
				"set","bio","xtal","CA","tot","chk","tp","fn","gray","fail","acc","rec");
		

		for (int index=1;index<=maxIndex;index++) {
			String indexRegex = "\\."+index;
			if (maxIndex==1) {
				indexRegex = "";
			}
			// parse the files and return a list of 2 lists of entropy scores per interface (one non-weighted, one weighted)
			List<List<InterfaceScore>> bioEntScores = parseFiles(bioToAnalyse, bioDir, "\\.entropies\\.scores"+indexRegex);
			List<InterfaceScore> bioEntNwScores = bioEntScores.get(0);
			List<InterfaceScore> bioEntWScores  = bioEntScores.get(1);
			// parse the files and return a list of 2 lists of ka/ks scores per interface (one non-weighted, one weighted)
			List<List<InterfaceScore>> bioKScores = parseFiles(bioToAnalyse, bioDir, "\\.kaks\\.scores"+indexRegex);
			List<InterfaceScore> bioKNwScores = bioKScores.get(0);
			List<InterfaceScore> bioKWScores  = bioKScores.get(1);

			// parse the files and return a list of 2 lists of entropy scores per interface (one non-weighted, one weighted)
			List<List<InterfaceScore>> xtalEntScores = parseFiles(xtalToAnalyse, xtalDir, "\\.entropies\\.scores"+indexRegex);
			List<InterfaceScore> xtalEntNwScores = xtalEntScores.get(0);
			List<InterfaceScore> xtalEntWScores  = xtalEntScores.get(1);
			// parse the files and return a list of 2 lists of ka/ks scores per interface (one non-weighted, one weighted)
			List<List<InterfaceScore>> xtalKScores = parseFiles(xtalToAnalyse, xtalDir, "\\.kaks\\.scores"+indexRegex);
			List<InterfaceScore> xtalKNwScores = xtalKScores.get(0);
			List<InterfaceScore> xtalKWScores  = xtalKScores.get(1);
			
			
			// print out statistics
			printStats(System.out,bioEntNwScores,xtalEntNwScores, total,"entrNW",minIndexForZoomingCall);
			printStats(System.out,bioEntWScores,xtalEntWScores, total,"entrW",minIndexForZoomingCall);
			
			printStats(System.out,bioKNwScores,xtalKNwScores, total,"kaksNW",minIndexForZoomingCall);
			printStats(System.out,bioKWScores,xtalKWScores, total,"kaksW",minIndexForZoomingCall);

		}
		
	}
	
	private static List<List<InterfaceScore>> parseFiles(TreeMap<String,List<Integer>> toAnalyse, File dir, String regexSuffix) throws IOException, FileFormatException {
		
		List<List<InterfaceScore>> lists = new ArrayList<List<InterfaceScore>>();
		lists.add(new ArrayList<InterfaceScore>()); // here we store non-weighted scores
		lists.add(new ArrayList<InterfaceScore>()); // here we store weighted scores
		
		for (String pdbId:toAnalyse.keySet()) {
			String regex = "^"+pdbId+regexSuffix;
			
			List<Integer> interfIds = toAnalyse.get(pdbId);
			File[] files = dir.listFiles(new RegexFileFilter(regex));
			for (File file: files) {
				PdbScore[] pdbScores = InterfaceEvolContextList.parseScoresFile(file);
				PdbScore scoresNW = pdbScores[0];
				PdbScore scoresW = pdbScores[1];
				for (int interfId:interfIds) {
					InterfaceScore interfScoreNW = scoresNW.getInterfScore(interfId);
					InterfaceScore interfScoreW = scoresW.getInterfScore(interfId);
					lists.get(0).add(interfScoreNW);
					lists.get(1).add(interfScoreW);
				}
				
			}
		}
		return lists;
	}
	
	private static void printStats(PrintStream ps, List<InterfaceScore> bioScores, List<InterfaceScore> xtalScores, int total, String title, int minIndexForZoomingCall) {
		
		if (bioScores.isEmpty() || xtalScores.isEmpty()) return;
		
		// compute the counts of predictions
		int[][] bioCounts = analyse(bioScores, minIndexForZoomingCall);
		int[][] xtalCounts = analyse(xtalScores, minIndexForZoomingCall);
		
		PdbScore parent = bioScores.get(0).getParent();
		double bioCutoff = parent.getBioCutoff();
		double xtalCutoff = parent.getXtalCutoff();
		double[] bsaToAsaCutoffs = parent.getBsaToAsaCutoffs();
		
		int[] bioSetbioCalls = bioCounts[0];
		int[] bioSetxtalCalls = bioCounts[1];
		int[] bioSetgrayCalls = bioCounts[2];
		int[] xtalSetbioCalls = xtalCounts[0];
		int[] xtalSetxtalCalls = xtalCounts[1];
		int[] xtalSetgrayCalls = xtalCounts[2];

		int numBsaToAsaCutoffsPlus1 = bioSetbioCalls.length;
		
		for (int i=0;i<numBsaToAsaCutoffsPlus1;i++) {
			int tp = 0;
			int fn = 0;

			tp += bioSetbioCalls[i];
			fn += bioSetxtalCalls[i];

			tp += xtalSetxtalCalls[i];
			fn += xtalSetbioCalls[i]; 
			
			int gray = bioSetgrayCalls[i]+xtalSetgrayCalls[i];
			
			int failed = total-tp-fn-gray;
			double accuracy = (double)tp/(double)(tp+fn+gray);
			double recall = (double)(total-failed)/(double)total;
			int checksum = tp+fn+gray+failed;
			double bsaToAsaCutoffOutput = -1.0;
			if (i<numBsaToAsaCutoffsPlus1-1) {
				bsaToAsaCutoffOutput = bsaToAsaCutoffs[i];
			}
			
			ps.printf("%6s\t%4.2f\t%4.2f\t%5.2f\t%4d\t%4d\t%4d\t%4d\t%4d\t%4d\t%4.2f\t%4.2f\n",
					title,bioCutoff,xtalCutoff,bsaToAsaCutoffOutput,total,checksum,tp,fn,gray,failed,accuracy,recall);
		}
	}
	
	
	private static int[][] analyse(List<InterfaceScore> scores, int minIndexForZoomingCall) {
		
		int numBsaToAsaCutoffs = scores.get(0).getNumBsaToAsaCutoffs();
		
		int[] bio = new int[numBsaToAsaCutoffs+1];
		int[] xtal = new int[numBsaToAsaCutoffs+1];
		int[] gray = new int[numBsaToAsaCutoffs+1];

		for (InterfaceScore interfScore:scores) {
			CallType[] calls = interfScore.getCalls();
			for (int i=0;i<numBsaToAsaCutoffs;i++) {
				if (calls[i].equals(CallType.BIO)) {
					bio[i]++;
				} else if (calls[i].equals(CallType.CRYSTAL)) {
					xtal[i]++;
				} else if (calls[i].equals(CallType.GRAY)) {
					gray[i]++;
				}
			}
			CallType zoomingCall = interfScore.getZoomingCall(minIndexForZoomingCall);
			if (zoomingCall.equals(CallType.BIO)) bio[numBsaToAsaCutoffs]++;
			else if (zoomingCall.equals(CallType.CRYSTAL)) xtal[numBsaToAsaCutoffs]++;
			else if (zoomingCall.equals(CallType.GRAY)) gray[numBsaToAsaCutoffs]++;
		}
		int[][] counts = new int[3][];
		counts[0] = bio;
		counts[1] = xtal;
		counts[2] = gray;

		return counts;
	}
	
	/**
	 * Reads a list file containing pdb codes and interface ids one per line and tab/space separated,
	 * e.g. 
	 *  1bxy 1
	 *  1bxy 2
	 *  2bxy 1
	 *  ...
	 * If only a pdb code is given without an id then a 1 is assumed.
	 * @param list
	 * @return a map of pdb codes to list of interface ids 
	 * @throws IOException
	 */
	public static TreeMap<String,List<Integer>> readListFile(File list) throws IOException {
		TreeMap<String,List<Integer>> map = new TreeMap<String, List<Integer>>();
		BufferedReader br = new BufferedReader(new FileReader(list));
		String line;
		while ((line=br.readLine())!=null){
			if (line.startsWith("#")) continue;
			String[] fields = line.trim().split("\\s+");
			String pdbId = fields[0];
			int interf = 1;
			if (fields.length>1) {
				interf = Integer.parseInt(fields[1]);
			}
			if (map.containsKey(pdbId)) {
				map.get(pdbId).add(interf);
			} else {
				List<Integer> interfaces = new ArrayList<Integer>();
				interfaces.add(interf);
				map.put(pdbId,interfaces);
			}
			
		}
		br.close();
		return map;
	}
}
