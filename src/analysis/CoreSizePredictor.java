package analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import crk.CallType;
import crk.ChainEvolContext;
import crk.ChainEvolContextList;
import crk.InterfaceEvolContext;

//import owl.core.structure.AaResidue;
//import owl.core.structure.AminoAcid;
import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
//import owl.core.structure.InterfaceRimCore;
//import owl.core.structure.Residue;
import owl.core.util.Goodies;
import owl.core.util.RegexFileFilter;

/**
 * Script to go through output files of crk (*.interfaces.dat) in a given directory
 * and predict the bio/xtal character of interfaces based on core size alone. 
 * Outputs statistics of prediction: accuracy TP, FN
 * This script assumes that we are analysing a set of xtal interfaces thus all output (TP, FN etc)
 * follows that assumption. 
 * For predicted bio interfaces we also output entropy/kaks evolutionary scores and calls
 * read from *.chainevolcontext.dat files in same directory 
 * 
 * @author duarte_j
 *
 */
public class CoreSizePredictor {

	private static final double[] BSATOASA_CUTOFFS = {0.95};
	private static final int MIN_NUMBER_CORE_RESIDUES_FOR_BIO = 7;
	
	private static final int MINIMUM_INTERF_AREA_TO_REPORT = 1000;
	private static final int MIN_NUM_HOMOLOGS = 10;
	private static final double BIO_CUTOFF = 0.84;
	private static final double XTAL_CUTOFF = 0.86;
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
			
		File dir = new File(args[0]);
		File[] interffiles = dir.listFiles(new RegexFileFilter(".*\\.interfaces.dat"));
		
		int[] globalStats = new int[2];
		for (File file:interffiles) {
			
			int[] stats = predict(file);
			globalStats[0]+=stats[0];
			globalStats[1]+=stats[1];
		}
		double accuracy = (double)globalStats[0]/((double)(globalStats[0]+globalStats[1]));
		System.out.println("Total: "+(globalStats[0]+globalStats[1])+", TP: "+globalStats[0]+", FN: "+globalStats[1]);
		System.out.printf("Accuracy: %4.2f\n",accuracy);
	}
	
	
	private static int[] predict(File file) throws IOException, ClassNotFoundException {
		String pdbCode = file.getName().substring(0, 4);
		System.out.println(pdbCode);
		
		int[] stats = new int[2]; // tp, fn
		
		ChainInterfaceList interfaces = (ChainInterfaceList)Goodies.readFromFile(file);

		for (ChainInterface interf:interfaces) {
			
			if (interf.getId()>1 && interf.getInterfaceArea()<MINIMUM_INTERF_AREA_TO_REPORT) continue; 
			
			interf.calcRimAndCore(BSATOASA_CUTOFFS);
			int size1 = interf.getFirstRimCores()[0].getCoreSize();
			int size2 = interf.getSecondRimCores()[0].getCoreSize();
			int size = size1+size2;
			
			
			String callStr = "";
			
			String predictStr = null;
			if (size<MIN_NUMBER_CORE_RESIDUES_FOR_BIO) {
				stats[0]++;
				predictStr = "xtal";
			} else {
				stats[1]++;
				predictStr = "bio ";
				
				
				// in this case we go and see what we would predict with evolutionary scores
				File chainevolfile = new File(file.getParent(),pdbCode+".chainevolcontext.dat");
				callStr = getEvolCall(chainevolfile,interf);
				
				
			}
			
			// to output glycine warnings
//			int numGly1 = countGlycines(interf.getFirstRimCores()[0]);
//			int numGly2 = countGlycines(interf.getSecondRimCores()[0]);
//			String glywarn = "";
//			if ((numGly1+numGly2)>((double)size/4.0)) {
//				glywarn = " ("+numGly1+","+numGly2+")";
//			}
			
			System.out.printf("%2d %2d %s %s\n",interf.getFirstRimCores()[0].getCoreSize(),interf.getSecondRimCores()[0].getCoreSize(),
					predictStr,callStr);
			
			
		}
		return stats;
	}

//	private static int countGlycines(InterfaceRimCore rimcore) {
//		int count = 0;
//		for (Residue res:rimcore.getCoreResidues()) {
//			if (res instanceof AaResidue && (((AaResidue)res).getAaType()==AminoAcid.GLY)) {
//				count++;
//			}
//		}
//		return count;
//	}
	
	private static String getEvolCall(File chainevolfile, ChainInterface interf) throws IOException, ClassNotFoundException {
		if (!chainevolfile.exists()) return "No evol score calculated";
		
		ChainEvolContextList cecs = (ChainEvolContextList)Goodies.readFromFile(chainevolfile);
		ArrayList<ChainEvolContext> chainsEvCs = new ArrayList<ChainEvolContext>();
		chainsEvCs.add(cecs.getChainEvolContext(interf.getFirstMolecule().getPdbChainCode()));
		chainsEvCs.add(cecs.getChainEvolContext(interf.getSecondMolecule().getPdbChainCode()));
		InterfaceEvolContext iec = new InterfaceEvolContext(interf, chainsEvCs);
		
		iec.scoreEntropy(false);
		CallType entCall = iec.getCalls(BIO_CUTOFF, XTAL_CUTOFF, MIN_NUM_HOMOLOGS, 6, 3)[0];
		String entCallStr = entCall.getName() +"("+String.format("%4.2f", iec.getFinalScores()[0])+")";
		iec.scoreEntropy(true);
		entCall = iec.getCalls(BIO_CUTOFF, XTAL_CUTOFF, MIN_NUM_HOMOLOGS, 6, 3)[0];
		entCallStr += " "+entCall.getName() +"("+String.format("%4.2f", iec.getFinalScores()[0])+")";
		
		String kaksCallStr = CallType.NO_PREDICTION.getName()+" (can't do kaks)";
		if (iec.canDoCRK()) {
			iec.scoreKaKs(false);
			CallType kaksCall = iec.getCalls(BIO_CUTOFF, XTAL_CUTOFF, MIN_NUM_HOMOLOGS, 6, 3)[0];
			kaksCallStr = kaksCall.getName()+"("+String.format("%4.2f", iec.getFinalScores()[0])+")";
			iec.scoreKaKs(true);
			kaksCall = iec.getCalls(BIO_CUTOFF, XTAL_CUTOFF, MIN_NUM_HOMOLOGS, 6, 3)[0];
			kaksCallStr += " "+kaksCall.getName()+"("+String.format("%4.2f", iec.getFinalScores()[0])+")";
		}
		
		String callStr = entCallStr+" "+kaksCallStr;

		callStr += " homologs: "+iec.getFirstChainEvolContext().getNumHomologs()+","+iec.getSecondChainEvolContext().getNumHomologs();
			
		return callStr;
	}
}
