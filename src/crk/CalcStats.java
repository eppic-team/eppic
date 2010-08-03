package crk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import owl.core.util.RegexFileFilter;

public class CalcStats {

	private static final double   DEF_ENTR_BIO_CUTOFF = 0.95;
	private static final double   DEF_ENTR_XTAL_CUTOFF = 1.05;
	private static final double   DEF_KAKS_BIO_CUTOFF = 0.95;//0.84;
	private static final double   DEF_KAKS_XTAL_CUTOFF = 1.05;// 0.86;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		File dir    = new File(args[0]);
		//File outDir = new File(args[1]);
		double minInterfSize = Double.parseDouble(args[1]);
		
		File[] entrFilesW = dir.listFiles(new RegexFileFilter("^\\d\\w\\w\\w\\.\\d\\.entropies\\.scoreW\\.dat"));
		Arrays.sort(entrFilesW);
		File[] entrFilesNW = dir.listFiles(new RegexFileFilter("^\\d\\w\\w\\w\\.\\d\\.entropies\\.scoreNW\\.dat"));
		Arrays.sort(entrFilesNW);
		File[] kaksFilesW = dir.listFiles(new RegexFileFilter("^\\d\\w\\w\\w\\.\\d\\.kaks\\.scoreW\\.dat"));
		Arrays.sort(kaksFilesW);
		File[] kaksFilesNW = dir.listFiles(new RegexFileFilter("^\\d\\w\\w\\w\\.\\d\\.kaks\\.scoreNW\\.dat"));
		Arrays.sort(kaksFilesNW);

		File[] entrScoreFiles = dir.listFiles(new RegexFileFilter("^\\d\\w\\w\\w\\.entropies\\.scores"));
		HashMap<String,HashMap<Integer,Double>> areas = parseAllScoreFiles(entrScoreFiles);
		
		//File entrOutW = new File(outDir,"ratio.correlations.w");
		//File entrOutNW = new File(outDir,"ratio.correlations.w");
		//File kaksOutW = new File(outDir,"ratio.correlations.w");
		//File kaksOutNW = new File(outDir,"ratio.correlations.w");
		
		System.out.printf("%4s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\t%4s\n","set","tot","tp","fn","gray","fail","prec","rec");
		analyseFiles(entrFilesW, System.out, minInterfSize, ScoringType.ENTROPY, "entW", areas );
		analyseFiles(entrFilesNW, System.out, minInterfSize, ScoringType.ENTROPY, "entNW", areas);
		analyseFiles(kaksFilesW, System.out, minInterfSize, ScoringType.KAKS, "kakW", areas);
		analyseFiles(kaksFilesNW, System.out, minInterfSize, ScoringType.KAKS, "kakNW", areas);
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
	
	private static void analyseFiles(File[] files, PrintStream ps, double minInterfSize, ScoringType scoType, String title, HashMap<String,HashMap<Integer,Double>> areas) throws IOException, ClassNotFoundException {
		
		//ps.printf("%4s\t%4s\t%4s\t%4s\t%4s\n","tot","tp","fn","prec","rec");
		
		int total = 0;
		int tp = 0;
		//int fp = 0;
		//int tn = 0;
		int fn = 0;
		int gray = 0;
		int failed = 0;

		for (int i=0;i<files.length;i++	){
			File file = files[i];
			InterfaceScore interfSc = InterfaceScore.readFromFile(file);
			String pdbId = file.getName().substring(0,4);
			int interfaceSerial = Integer.parseInt(file.getName().substring(5,6));
			double bioCutoff = 0;
			double xtalCutoff = 0;
			if (scoType.equals(ScoringType.ENTROPY)) {
				bioCutoff = DEF_ENTR_BIO_CUTOFF;
				xtalCutoff = DEF_ENTR_XTAL_CUTOFF;
			} else if (scoType.equals(ScoringType.KAKS)) {
				bioCutoff = DEF_KAKS_BIO_CUTOFF;
				xtalCutoff = DEF_KAKS_XTAL_CUTOFF;
			}
			
			double interfArea = areas.get(pdbId).get(interfaceSerial);
			
			if (interfArea>minInterfSize) {
				
				total++;
				CallType call = interfSc.getCall(bioCutoff, xtalCutoff).getType();
				//System.out.println(title+"\t"+pdbId+"."+interfaceSerial+"\t"+String.format("%8.2f",interfArea)+"\t"+call.getName());
				
				if (call.equals(CallType.BIO)) {
					tp++;
				} else if (call.equals(CallType.CRYSTAL)) {
					fn++;
				} else if (call.equals(CallType.GRAY)) {
					gray++;
				} else if (call.equals(CallType.NO_PREDICTION)) {
					failed++;
				}
				
			}
			
		}
		double precision = (double)tp/(double)(tp+fn+gray);
		double recall = (double)(total-failed)/(double)total;
		ps.printf("%5s%4d\t%4d\t%4d\t%4d\t%4d\t%4.2f\t%4.2f\n",title,total,tp,fn,gray,failed,precision,recall);

	}
}
