package analysis;

import gnu.getopt.Getopt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import crk.CallType;
import crk.InterfaceScore;

import owl.core.util.RegexFileFilter;

public class CalcCorrEntrKaks {

	private static final double   DEF_ENTR_BIO_CUTOFF = 0.95;
	private static final double   DEF_ENTR_XTAL_CUTOFF = 1.05;
	private static final double   DEF_KAKS_BIO_CUTOFF = 0.95;
	private static final double   DEF_KAKS_XTAL_CUTOFF = 1.05;

	private static final String   PROGRAM_NAME = "CalcCorrEntrKaks";
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		File dir    = null;
		File list   = null;
		double kaksBioCutoff = DEF_KAKS_BIO_CUTOFF;
		double kaksXtalCutoff = DEF_KAKS_XTAL_CUTOFF;
		double entrBioCutoff = DEF_ENTR_BIO_CUTOFF;
		double entrXtalCutoff = DEF_ENTR_XTAL_CUTOFF;

		String help = "Usage: \n" +
		PROGRAM_NAME+"\n" +
		"   -i         :  input dir\n" +
		"   -l         :  list file containing all the pdbIds + interface serials to analyse\n"+
		"  [-k]        :  rim to core kaks ratio cutoff for calling bio\n" +
		"  [-K]        :  rim to core kaks ratio cutoff for calling xtal\n"+
		"  [-e]        :  rim to core entropy ratio cutoff for calling bio\n"+
		"  [-E]        :  rim to core entropy ratio cutoff for calling xtal\n\n";

		Getopt g = new Getopt(PROGRAM_NAME, args, "i:l:k:K:e:E:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'i':
				dir = new File(g.getOptarg());
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
		
		if (dir==null || list==null){
			System.err.println("Missing parameter: input dir or list file");
			System.exit(1);
		}
		
		
		TreeMap<String,List<Integer>> toAnalyse = readListFile(list);
		
		for (String pdbId:toAnalyse.keySet()) {
			List<Integer> interfaces = toAnalyse.get(pdbId);
			for (int interf:interfaces) {
				File[] entr = dir.listFiles(new RegexFileFilter("^"+pdbId+"\\."+interf+"\\.entropies.scoreW\\.dat"));
				File[] kaks = dir.listFiles(new RegexFileFilter("^"+pdbId+"\\."+interf+"\\.kaks.scoreW\\.dat"));
				if (entr.length>1 || kaks.length>1) {
					System.err.println("More than 1 file returned by regex");
					System.exit(1);
				}
				if (entr.length==0 || kaks.length==0) {
					continue;
				}
				System.out.print(pdbId+"\t"+interf+"\t");
				analyse(entr[0], kaks[0], entrBioCutoff, entrXtalCutoff, kaksBioCutoff, kaksXtalCutoff);
			}
		}
		
		
	}
	
	private static void analyse(File entr, File kaks, double entrBioCutoff, double entrXtalCutoff, double kaksBioCutoff, double kaksXtalCutoff) throws IOException, ClassNotFoundException {
		InterfaceScore entrInterfSc = InterfaceScore.readFromFile(entr);
		InterfaceScore kaksInterfSc = InterfaceScore.readFromFile(kaks);
		CallType entrCall = entrInterfSc.getCall(entrBioCutoff, entrXtalCutoff).getType();
		CallType kaksCall = kaksInterfSc.getCall(kaksBioCutoff, kaksXtalCutoff).getType();
		List<Integer> indices = new ArrayList<Integer>();
		indices.add(0);
		indices.add(1);
		double entrSc = entrInterfSc.getAvrgRatio(indices);
		double kaksSc = kaksInterfSc.getAvrgRatio(indices);
		System.out.printf("%6s\t%6s\t%4.2f\t%4.2f\n",entrCall.getName(),kaksCall.getName(),entrSc,kaksSc);
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
