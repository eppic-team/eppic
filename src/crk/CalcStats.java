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

		List<File> entrFilesW = new ArrayList<File>();
		List<File> entrFilesNW = new ArrayList<File>();
		List<File> kaksFilesW = new ArrayList<File>();
		List<File> kaksFilesNW = new ArrayList<File>();
		
		for (String pdbId:toAnalyse.keySet()) {
			List<Integer> interfaces = toAnalyse.get(pdbId);
			for (int interf:interfaces) {
				entrFilesW.addAll (Arrays.asList(dir.listFiles(new RegexFileFilter("^"+pdbId+"\\."+interf+"\\.entropies\\.scoreW\\.dat"))));
				entrFilesNW.addAll(Arrays.asList(dir.listFiles(new RegexFileFilter("^"+pdbId+"\\."+interf+"\\.entropies\\.scoreNW\\.dat"))));
				kaksFilesW.addAll (Arrays.asList(dir.listFiles(new RegexFileFilter("^"+pdbId+"\\."+interf+"\\.kaks\\.scoreW\\.dat"))));
				kaksFilesNW.addAll(Arrays.asList(dir.listFiles(new RegexFileFilter("^"+pdbId+"\\."+interf+"\\.kaks\\.scoreNW\\.dat"))));
			}
		}

		
		//int total = entrFilesW.size();
		int total = toAnalyse.size();
		
		int[] entrCountsW = analyseFiles(entrFilesW, ScoringType.ENTROPY, kaksBioCutoff, kaksXtalCutoff, entrBioCutoff, entrXtalCutoff);
		int[] entrCountsNW = analyseFiles(entrFilesNW, ScoringType.ENTROPY, kaksBioCutoff, kaksXtalCutoff, entrBioCutoff, entrXtalCutoff);
		int[] kaksCountsW = analyseFiles(kaksFilesW, ScoringType.KAKS, kaksBioCutoff, kaksXtalCutoff, entrBioCutoff, entrXtalCutoff);
		int[] kaksCountsNW = analyseFiles(kaksFilesNW, ScoringType.KAKS, kaksBioCutoff, kaksXtalCutoff, entrBioCutoff, entrXtalCutoff);
		
		System.out.printf("%4s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\n","set","tot","chk","tp","fn","gray","fail","acc","rec");
		printStats(System.out,entrCountsW,total,"entrW",truth);
		printStats(System.out,entrCountsNW,total,"entrNW",truth);
		printStats(System.out,kaksCountsW,total,"kaksW",truth);
		printStats(System.out,kaksCountsNW,total,"kaksNW",truth);
	}
	
	private static void printStats(PrintStream ps, int[] counts, int total, String title, CallType truth) {
		int bio = counts[0];
		int xtal = counts[1];
		int gray = counts[2];
		
		int tp = 0;
		int fn = 0;
		
		if (truth.equals(CallType.BIO)) {
			tp = bio;
			fn = xtal;
		} else if (truth.equals(CallType.CRYSTAL)) {
			tp = xtal;
			fn = bio;
		} else {
			throw new IllegalArgumentException("Invalid value "+truth);
		}
		int failed = total-tp-fn-gray;
		double accuracy = (double)tp/(double)(tp+fn+gray);
		double recall = (double)(total-failed)/(double)total;
		int checksum = tp+fn+gray+failed;
		ps.printf("%6s\t%4d\t%4d\t%4d\t%4d\t%4d\t%4d\t%4.2f\t%4.2f\n",title,total,checksum,tp,fn,gray,failed,accuracy,recall);

	}
	
	
	private static int[] analyseFiles(List<File> files, ScoringType scoType, 
			double kaksBioCutoff, double kaksXtalCutoff, double entrBioCutoff, double entrXtalCutoff) throws IOException, ClassNotFoundException {
		
		//ps.printf("%4s\t%4s\t%4s\t%4s\t%4s\n","tot","tp","fn","prec","rec");
		
		int bio = 0;
		int xtal = 0;
		int gray = 0;

		for (int i=0;i<files.size();i++	){
			File file = files.get(i);
			InterfaceScore interfSc = InterfaceScore.readFromFile(file);
			double bioCutoff = 0;
			double xtalCutoff = 0;
			if (scoType.equals(ScoringType.ENTROPY)) {
				bioCutoff = entrBioCutoff;
				xtalCutoff = entrXtalCutoff;
			} else if (scoType.equals(ScoringType.KAKS)) {
				bioCutoff = kaksBioCutoff;
				xtalCutoff = kaksXtalCutoff;
			}

			CallType call = interfSc.getCall(bioCutoff, xtalCutoff).getType();
			//System.out.println(title+"\t"+pdbId+"."+interfaceSerial+"\t"+String.format("%8.2f",interfArea)+"\t"+call.getName());

			if (call.equals(CallType.BIO)) {
				bio++;
			} else if (call.equals(CallType.CRYSTAL)) {
				xtal++;
			} else if (call.equals(CallType.GRAY)) {
				gray++;
			}// else if (call.equals(CallType.NO_PREDICTION)) {
			//	failed++;
			//}
		}
		int[] counts = {bio,xtal,gray};
		return counts;
	}
	
	private static TreeMap<String,List<Integer>> readListFile(File list) throws IOException {
		TreeMap<String,List<Integer>> map = new TreeMap<String, List<Integer>>();
		BufferedReader br = new BufferedReader(new FileReader(list));
		String line;
		while ((line=br.readLine())!=null){
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
