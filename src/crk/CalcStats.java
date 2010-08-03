package crk;

import gnu.getopt.Getopt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import owl.core.util.RegexFileFilter;

public class CalcStats {

	private static final double   DEF_ENTR_BIO_CUTOFF = 0.95;
	private static final double   DEF_ENTR_XTAL_CUTOFF = 1.05;
	private static final double   DEF_KAKS_BIO_CUTOFF = 0.95;
	private static final double   DEF_KAKS_XTAL_CUTOFF = 1.05;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		File dir    = null;
		double minInterfSize = 0;
		double kaksBioCutoff = DEF_KAKS_BIO_CUTOFF;
		double kaksXtalCutoff = DEF_KAKS_XTAL_CUTOFF;
		double entrBioCutoff = DEF_ENTR_BIO_CUTOFF;
		double entrXtalCutoff = DEF_ENTR_XTAL_CUTOFF;

		String help = "Usage: \n" +
		"CalcStats\n" +
		"   -i         :  input dir\n" +
		"   -m         :  minimum interface size to consider\n" +
		"  [-k]        :  rim to core kaks ratio cutoff for calling bio\n" +
		"  [-K]        :  rim to core kaks ratio cutoff for calling xtal\n"+
		"  [-e]        :  rim to core entropy ratio cutoff for calling bio\n"+
		"  [-E]        :  rim to core entropy ratio cutoff for calling xtal\n\n";

		Getopt g = new Getopt("CalcStats", args, "i:m:k:K:e:E:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'i':
				dir = new File(g.getOptarg());
				break;
			case 'm':
				minInterfSize = Double.parseDouble(g.getOptarg());
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
		
		if (dir==null || minInterfSize==0.0){
			System.err.println("Input dir and minimum interface area parameters needed");
			System.exit(1);
		}
		
		List<File> entrFilesWtmp = Arrays.asList(dir.listFiles(new RegexFileFilter("^\\d\\w\\w\\w\\.\\d\\.entropies\\.scoreW\\.dat")));
		Collections.sort(entrFilesWtmp);
		List<File> entrFilesNWtmp = Arrays.asList(dir.listFiles(new RegexFileFilter("^\\d\\w\\w\\w\\.\\d\\.entropies\\.scoreNW\\.dat")));
		Collections.sort(entrFilesNWtmp);
		List<File> kaksFilesWtmp = Arrays.asList(dir.listFiles(new RegexFileFilter("^\\d\\w\\w\\w\\.\\d\\.kaks\\.scoreW\\.dat")));
		Collections.sort(kaksFilesWtmp);
		List<File> kaksFilesNWtmp = Arrays.asList(dir.listFiles(new RegexFileFilter("^\\d\\w\\w\\w\\.\\d\\.kaks\\.scoreNW\\.dat")));
		Collections.sort(kaksFilesNWtmp);
		// the lists returned by Arrays.asList are inmutable, we've got to create new ones
		List<File> entrFilesW = new ArrayList<File>();
		entrFilesW.addAll(entrFilesWtmp);
		List<File> entrFilesNW = new ArrayList<File>();
		entrFilesNW.addAll(entrFilesNWtmp);
		List<File> kaksFilesW = new ArrayList<File>();
		kaksFilesW.addAll(kaksFilesWtmp);
		List<File> kaksFilesNW = new ArrayList<File>();
		kaksFilesNW.addAll(kaksFilesNWtmp);


		File[] entrScoreFiles = dir.listFiles(new RegexFileFilter("^\\d\\w\\w\\w\\.entropies\\.scores"));
		HashMap<String,HashMap<Integer,Double>> areas = parseAllScoreFiles(entrScoreFiles);
		
		trimToBelowCutoffArea(entrFilesW,areas,minInterfSize);
		trimToBelowCutoffArea(entrFilesNW,areas,minInterfSize);
		trimToBelowCutoffArea(kaksFilesW,areas,minInterfSize);
		trimToBelowCutoffArea(kaksFilesNW,areas,minInterfSize);
		
		int total = entrFilesW.size();
		
		System.out.printf("%4s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\n","set","tot","chk","tp","fn","gray","fail","acc","rec");
		analyseFiles(total, entrFilesW, System.out, ScoringType.ENTROPY, "entW", kaksBioCutoff, kaksXtalCutoff, entrBioCutoff, entrXtalCutoff);
		analyseFiles(total, entrFilesNW, System.out, ScoringType.ENTROPY, "entNW", kaksBioCutoff, kaksXtalCutoff, entrBioCutoff, entrXtalCutoff);
		analyseFiles(total, kaksFilesW, System.out, ScoringType.KAKS, "kakW", kaksBioCutoff, kaksXtalCutoff, entrBioCutoff, entrXtalCutoff);
		analyseFiles(total, kaksFilesNW, System.out, ScoringType.KAKS, "kakNW", kaksBioCutoff, kaksXtalCutoff, entrBioCutoff, entrXtalCutoff);
	}
	
	private static HashMap<String,HashMap<Integer,Double>> parseAllScoreFiles(File[] files) throws IOException {
		HashMap<String,HashMap<Integer,Double>> map = new HashMap<String, HashMap<Integer,Double>>();
		for (File file:files) {
			String pdbId = file.getName().substring(0,4);
			map.put(pdbId,parseScoreFile(file));
		}
		return map;
	}
	
	private static HashMap<Integer,Double> parseScoreFile (File file) throws IOException {
		HashMap<Integer, Double> areas = new HashMap<Integer, Double>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		Pattern p = Pattern.compile("^(\\d+)\\(.*");
		int lineNum = 0;
		while ((line=br.readLine())!=null){
			lineNum++;
			if (lineNum==1) continue;
			
			String[] fields = line.trim().split("\\s+");
			int interf = -1;
			Matcher m = p.matcher(fields[0]);
			if (m.matches()) {
				interf = Integer.parseInt(m.group(1));
			}
			double area = Double.parseDouble(fields[1]);
			
			areas.put(interf,area);
		}
		br.close();
		return areas;
	}
	
	private static void trimToBelowCutoffArea(List<File> list, HashMap<String,HashMap<Integer,Double>> areas, double minInterfArea) {
		Iterator<File> it = list.iterator();
		while (it.hasNext()) {
			File file = it.next();
			String pdbId = file.getName().substring(0,4);
			int interfaceSerial = Integer.parseInt(file.getName().substring(5,6));
			double interfArea = areas.get(pdbId).get(interfaceSerial);
			
			if (interfArea<minInterfArea) {
				it.remove();
			}
		}
	}
	
	private static void analyseFiles(int total, List<File> files, PrintStream ps, ScoringType scoType, String title, 
			double kaksBioCutoff, double kaksXtalCutoff, double entrBioCutoff, double entrXtalCutoff) throws IOException, ClassNotFoundException {
		
		//ps.printf("%4s\t%4s\t%4s\t%4s\t%4s\n","tot","tp","fn","prec","rec");
		
		//int total = 0;
		int tp = 0;
		//int fp = 0;
		//int tn = 0;
		int fn = 0;
		int gray = 0;
		//int failed = 0;

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
				tp++;
			} else if (call.equals(CallType.CRYSTAL)) {
				fn++;
			} else if (call.equals(CallType.GRAY)) {
				gray++;
			}// else if (call.equals(CallType.NO_PREDICTION)) {
			//	failed++;
			//}
		}
		int failed = total-tp-fn-gray;
		double accuracy = (double)tp/(double)(tp+fn+gray);
		double recall = (double)(total-failed)/(double)total;
		int checksum = tp+fn+gray+failed;
		ps.printf("%5s%4d\t%4d\t%4d\t%4d\t%4d\t%4d\t%4.2f\t%4.2f\n",title,total,checksum,tp,fn,gray,failed,accuracy,recall);

	}
}
