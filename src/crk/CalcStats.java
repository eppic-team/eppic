package crk;

import gnu.getopt.Getopt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import owl.core.util.FileFormatError;
import owl.core.util.RegexFileFilter;

public class CalcStats {


	private static final String   PROGRAM_NAME ="CalcStats";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		File dir    = null;
		CallType truth = null;
		File list   = null;
		int maxIndex = 1;
		int minIndexForZoomingCall = 0;
		
		String help = "Usage: \n" +
		PROGRAM_NAME+"\n" +
		"   -i         :  input dir\n" +
		"   -t         :  truth: either bio or xtal\n" +
		"   -l         :  list file containing all the pdbIds + interface serials to \n" +
		"                 analyse\n" +
		"   [-m <int>] :  max index of the scoring files (when using several bio/xtal \n" +
		"                 call cutoffs)\n" +
		"   [-z <int>] :  the minimum index of the bsaToAsaCutoffs array to use for zooming call \n\n";

		Getopt g = new Getopt(PROGRAM_NAME, args, "i:t:l:m:z:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'i':
				dir = new File(g.getOptarg());
				break;
			case 't':
				truth = CallType.getByName(g.getOptarg());
				break;				
			case 'l':
				list = new File(g.getOptarg());
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
		
		if (dir==null || truth==null || list==null){
			System.err.println("Missing parameter: input dir, truth call or list file");
			System.exit(1);
		}
		
		// read the list file containing the interfaces to analyse (known to be bio or xtal depending on -t)
		TreeMap<String,List<Integer>> toAnalyse = readListFile(list);
		int total = 0;
		for (List<Integer> vals:toAnalyse.values()) {
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
			List<List<InterfaceScore>> entScores = parseFiles(toAnalyse, dir, "\\.entropies\\.scores"+indexRegex);
			List<InterfaceScore> entNwScores = entScores.get(0);
			List<InterfaceScore> entWScores  = entScores.get(1);

			// parse the files and return a list of 2 lists of ka/ks scores per interface (one non-weighted, one weighted)
			List<List<InterfaceScore>> kScores = parseFiles(toAnalyse, dir, "\\.kaks\\.scores."+indexRegex);
			List<InterfaceScore> kNwScores = kScores.get(0);
			List<InterfaceScore> kWScores  = kScores.get(1);

			// print out statistics
			printStats(System.out,entNwScores,total,"entrNW",truth,minIndexForZoomingCall);
			printStats(System.out,entWScores,total,"entrW",truth,minIndexForZoomingCall);
			
			printStats(System.out,kNwScores,total,"kaksNW",truth,minIndexForZoomingCall);
			printStats(System.out,kWScores,total,"kaksW",truth,minIndexForZoomingCall);

		}
		
	}
	
	private static List<List<InterfaceScore>> parseFiles(TreeMap<String,List<Integer>> toAnalyse, File dir, String regexSuffix) throws IOException, FileFormatError {
		
		List<List<InterfaceScore>> lists = new ArrayList<List<InterfaceScore>>();
		lists.add(new ArrayList<InterfaceScore>()); // here we store non-weighted scores
		lists.add(new ArrayList<InterfaceScore>()); // here we store weighted scores
		
		for (String pdbId:toAnalyse.keySet()) {
			String regex = "^"+pdbId+regexSuffix;
			
			List<Integer> interfIds = toAnalyse.get(pdbId);
			for (File file: dir.listFiles(new RegexFileFilter(regex))) {
				PdbScore[] pdbScores = InterfaceEvolContextList.parseScoresFile(file);
				PdbScore scoresNW = pdbScores[0];
				PdbScore scoresW = pdbScores[1];
				for (int interfId:interfIds) {
					lists.get(0).add(scoresNW.getInterfScore(interfId));
					lists.get(1).add(scoresW.getInterfScore(interfId));
				}
				
			}
		}
		return lists;
	}
	
	private static void printStats(PrintStream ps, List<InterfaceScore> scores, int total, String title, CallType truth, int minIndexForZoomingCall) {
		
		// compute the counts of predictions
		if (scores.isEmpty()) return;
		int[][] counts = analyse(scores, minIndexForZoomingCall);
		
		PdbScore parent = scores.get(0).getParent();
		double bioCutoff = parent.getBioCutoff();
		double xtalCutoff = parent.getXtalCutoff();
		double[] bsaToAsaCutoffs = parent.getBsaToAsaCutoffs();
		
		int[] bio = counts[0];
		int[] xtal = counts[1];
		int[] gray = counts[2];
		int numBsaToAsaCutoffsPlus1 = bio.length;
		
		for (int i=0;i<numBsaToAsaCutoffsPlus1;i++) {
			int tp = 0;
			int fn = 0;

			if (truth.equals(CallType.BIO)) {
				tp = bio[i];
				fn = xtal[i];
			} else if (truth.equals(CallType.CRYSTAL)) {
				tp = xtal[i];
				fn = bio[i];
			} else {
				throw new IllegalArgumentException("Invalid value "+truth);
			}
			int failed = total-tp-fn-gray[i];
			double accuracy = (double)tp/(double)(tp+fn+gray[i]);
			double recall = (double)(total-failed)/(double)total;
			int checksum = tp+fn+gray[i]+failed;
			double bsaToAsaCutoffOutput = -1.0;
			if (i<numBsaToAsaCutoffsPlus1-1) {
				bsaToAsaCutoffOutput = bsaToAsaCutoffs[i];
			}
			
			ps.printf("%6s\t%4.2f\t%4.2f\t%5.2f\t%4d\t%4d\t%4d\t%4d\t%4d\t%4d\t%4.2f\t%4.2f\n",
					title,bioCutoff,xtalCutoff,bsaToAsaCutoffOutput,total,checksum,tp,fn,gray[i],failed,accuracy,recall);
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
	
	private static TreeMap<String,List<Integer>> readListFile(File list) throws IOException {
		TreeMap<String,List<Integer>> map = new TreeMap<String, List<Integer>>();
		BufferedReader br = new BufferedReader(new FileReader(list));
		String line;
		while ((line=br.readLine())!=null){
			if (line.startsWith("#")) continue;
			String[] fields = line.trim().split("\\s+");
			String pdbId = fields[0];
			int interf = Integer.parseInt(fields[1]);
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
