package crk;

import gnu.getopt.Getopt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import owl.core.util.FileFormatError;
import owl.core.util.RegexFileFilter;

public class CalcStats {

	private static final double   DEF_ENTR_BIO_CUTOFF = 0.95;
	private static final double   DEF_ENTR_XTAL_CUTOFF = 1.05;
	private static final double   DEF_KAKS_BIO_CUTOFF = 0.95;
	private static final double   DEF_KAKS_XTAL_CUTOFF = 1.05;

	private static final String   PROGRAM_NAME ="CalcStats";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		File dir    = null;
		CallType truth = null;
		File list   = null;
		double kaksBioCutoff = DEF_KAKS_BIO_CUTOFF;
		double kaksXtalCutoff = DEF_KAKS_XTAL_CUTOFF;
		double entrBioCutoff = DEF_ENTR_BIO_CUTOFF;
		double entrXtalCutoff = DEF_ENTR_XTAL_CUTOFF;

		String help = "Usage: \n" +
		PROGRAM_NAME+"\n" +
		"   -i         :  input dir\n" +
		"   -t         :  truth: either bio or xtal\n" +
		"   -l         :  list file containing all the pdbIds + interface serials to analyse\n"+
		"  [-k]        :  rim to core kaks ratio cutoff for calling bio\n" +
		"  [-K]        :  rim to core kaks ratio cutoff for calling xtal\n"+
		"  [-e]        :  rim to core entropy ratio cutoff for calling bio\n"+
		"  [-E]        :  rim to core entropy ratio cutoff for calling xtal\n\n";

		Getopt g = new Getopt(PROGRAM_NAME, args, "i:t:l:k:K:e:E:h?");
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
			case 'k':
				kaksBioCutoff = Double.parseDouble(g.getOptarg());
				break;
			case 'K':
				kaksXtalCutoff = Double.parseDouble(g.getOptarg());
				break;
			case 'e':
				entrBioCutoff = Double.parseDouble(g.getOptarg());
				break;				
			case 'E':
				entrXtalCutoff = Double.parseDouble(g.getOptarg());
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
		
		TreeMap<String,List<Integer>> toAnalyse = readListFile(list);
		int total = 0;
		for (List<Integer> vals:toAnalyse.values()) {
			total+=vals.size();
		}
		

		List<File> entrFiles = new ArrayList<File>();
		List<File> kaksFiles = new ArrayList<File>();
		
		for (String pdbId:toAnalyse.keySet()) {
			
			entrFiles.addAll (Arrays.asList(dir.listFiles(new RegexFileFilter("^"+pdbId+"\\.entropies\\.scores"))));
			kaksFiles.addAll (Arrays.asList(dir.listFiles(new RegexFileFilter("^"+pdbId+"\\.kaks\\.scores"))));
			
		}

		List<List<InterfaceScore>> entScores = parseFiles(toAnalyse, dir, ScoringType.ENTROPY);
		List<InterfaceScore> entNwScores = entScores.get(0);
		List<InterfaceScore> entWScores  = entScores.get(1);
		

		List<List<InterfaceScore>> kScores = parseFiles(toAnalyse, dir, ScoringType.KAKS);
		List<InterfaceScore> kNwScores = kScores.get(0);
		List<InterfaceScore> kWScores  = kScores.get(1);
				
		
		int[][] entrCountsNW = analyse(entNwScores, entrBioCutoff, entrXtalCutoff);
		int[][] entrCountsW  = analyse(entWScores, entrBioCutoff, entrXtalCutoff);

		int[][] kaksCountsNW = analyse(kNwScores, kaksBioCutoff, kaksXtalCutoff);
		int[][] kaksCountsW  = analyse(kWScores, kaksBioCutoff, kaksXtalCutoff);

		
		System.out.printf("%4s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\n","set","tot","chk","tp","fn","gray","fail","acc","rec");
		printStats(System.out,entrCountsW,total,"entrW",truth);
		printStats(System.out,entrCountsNW,total,"entrNW",truth);
		if (kaksCountsNW!=null) printStats(System.out,kaksCountsW,total,"kaksW",truth);
		if (kaksCountsW!=null) printStats(System.out,kaksCountsNW,total,"kaksNW",truth);
		

		
	}
	
	private static List<List<InterfaceScore>> parseFiles(TreeMap<String,List<Integer>> toAnalyse, File dir, ScoringType scoType) throws IOException, FileFormatError {
		
		List<List<InterfaceScore>> lists = new ArrayList<List<InterfaceScore>>();
		lists.add(new ArrayList<InterfaceScore>()); // here we store non-weighted scores
		lists.add(new ArrayList<InterfaceScore>()); // here we store weighted scores
		
		for (String pdbId:toAnalyse.keySet()) {
			String regex = null;
			if (scoType==ScoringType.ENTROPY) {
				regex = "^"+pdbId+"\\.entropies\\.scores";
			} else if (scoType==ScoringType.KAKS) {
				regex = "^"+pdbId+"\\.kaks\\.scores";
			}
			
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
	
	private static void printStats(PrintStream ps, int[][] counts, int total, String title, CallType truth) {
		int[] bio = counts[0];
		int[] xtal = counts[1];
		int[] gray = counts[2];
		int numBsaToAsaCutoffs = bio.length;
		
		for (int i=0;i<numBsaToAsaCutoffs;i++) {
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
			if (i==0) {
				ps.printf("%6s\t%4d\t%4d\t%4d\t%4d\t%4d\t%4d\t%4.2f\t%4.2f\n",title,total,checksum,tp,fn,gray[i],failed,accuracy,recall);
			} else {
				ps.printf("%6s\t%4d\t%4d\t%4d\t%4d\t%4d\t%4d\t%4.2f\t%4.2f\n","",total,checksum,tp,fn,gray[i],failed,accuracy,recall);
			}
		}
	}
	
	
	private static int[][] analyse(List<InterfaceScore> scores, double bioCutoff, double xtalCutoff) {
		
		if (scores.isEmpty()) {
			return null;
		}
			
		int numBsaToAsaCutoffs = scores.get(0).getNumBsaToAsaCutoffs();
		
		int[] bio = new int[numBsaToAsaCutoffs];
		int[] xtal = new int[numBsaToAsaCutoffs];
		int[] gray = new int[numBsaToAsaCutoffs];

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
