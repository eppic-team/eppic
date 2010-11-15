package analysis;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import crk.CallType;
import crk.InterfaceScore;

import owl.core.util.RegexFileFilter;

public class CalcCorr2SidesInterf {

	private static final double   DEF_ENTR_BIO_CUTOFF = 0.95;
	private static final double   DEF_ENTR_XTAL_CUTOFF = 1.05;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		File dir = new File(args[0]);
		File outDir = new File(args[1]);
		
		File[] filesW = dir.listFiles(new RegexFileFilter("^\\d\\w\\w\\w\\.1\\.scoreW\\.dat"));
		File outW = new File(outDir,"ratio.correlations.w");
		analyseFiles(filesW, outW);
		
		File[] filesNW = dir.listFiles(new RegexFileFilter("^\\d\\w\\w\\w\\.1\\.scoreNW\\.dat"));
		File outNW = new File(outDir,"ratio.correlations.nw");
		analyseFiles(filesNW, outNW);
		
	}
	
	private static void analyseFiles(File[] files, File out) throws IOException, ClassNotFoundException {
		PrintStream ps = new PrintStream(out);
		for (int i=0;i<files.length;i++	){
			InterfaceScore interfSc = InterfaceScore.readFromFile(files[i]);
			String pdbId = files[i].getName().substring(0,4);
			CallType call = interfSc.getCall(DEF_ENTR_BIO_CUTOFF, DEF_ENTR_XTAL_CUTOFF).getType();
			InterfaceMemberScore interfMemberSc1 = interfSc.getMemberScore(0);
			InterfaceMemberScore interfMemberSc2 = interfSc.getMemberScore(1);
			double ratio1 = interfMemberSc1.getRatio();
			double ratio2 = interfMemberSc2.getRatio();
			if (!call.equals(CallType.NO_PREDICTION)) {
				ps.printf("%4s\t%5.2f\t%5.2f\t%s\n",pdbId,ratio1,ratio2,call.getName());
			}
			
			//if (call.equals(CallType.BIO)) {
			//	
			//} else if (call.equals(CallType.CRYSTAL)) {
			//	
			//} else if (call.equals(CallType.GRAY)) {
				
			//}
		}
		
	}
}
