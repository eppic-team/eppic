package analysis;

import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.PdbChain;
import owl.core.structure.Residue;

import crk.CallType;
import crk.ChainEvolContext;
import crk.ChainEvolContextList;
import crk.InterfaceEvolContext;
import crk.InterfaceEvolContextList;
import crk.ScoringType;

/**
 * Executable class to print ASA and entropies of a dataset in 
 * tabular form for plotting and calculation of correlations.
 * 
 * @author duarte_j
 *
 */
public class EntropyVsBurial {

	private static final String PROGRAM_NAME = "EntropyVsBurial";
	
	private static final Logger ROOTLOGGER = Logger.getRootLogger();


	private static File bioDir    = null;
	private static File xtalDir   = null;
	private static File bioList   = null;
	private static File xtalList  = null;
	
	
	
	
	public static void main(String[] args) throws Exception {
		
		String help = "Usage: \n" +
		PROGRAM_NAME+"\n" +
		"   -B         :  input dir containing the interfaces given with -b\n" +
		"   -X         :  input dir containing the interfaces given with -x\n"+
		"   -b         :  list file containing all the pdbIds + interface serials to \n" +
		"                 analyse that are known to be true bio contacts\n" +
		"   -x         :  list file containing all the pdbIds + interface serials to \n" +
		"                 analyse that are known to be true xtal contacts\n\n";
		
		Getopt g = new Getopt(PROGRAM_NAME, args, "B:X:b:x:h?");
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
		

		

		// initialising logging
		ConsoleAppender errorAppender = new ConsoleAppender(new PatternLayout("%5p - %m%n"),ConsoleAppender.SYSTEM_ERR);
		errorAppender.setThreshold(Level.WARN);
		ROOTLOGGER.addAppender(errorAppender);

		
		if (bioDir!=null) {
			
			TreeMap<String,List<Integer>> bioToAnalyse = Utils.readListFile(bioList);
			doAnalysis(bioToAnalyse,bioDir,CallType.BIO);
			
		}
		
		if (xtalDir!=null) {
			
			TreeMap<String,List<Integer>> xtalToAnalyse = Utils.readListFile(xtalList);
			doAnalysis(xtalToAnalyse,xtalDir,CallType.CRYSTAL);
			
		}
		
		
	}

	private static void doAnalysis(TreeMap<String,List<Integer>> toAnalyse, File dir, CallType truth) throws ClassNotFoundException, IOException {
		
	
		for (String pdbCode:toAnalyse.keySet()) {
			File chainevolcontextdatFile = new File(dir,pdbCode+".chainevolcontext.dat");
			File interfdatFile = new File(dir,pdbCode+".interfaces.dat");
			if (!chainevolcontextdatFile.exists() && !interfdatFile.exists()) {
				ROOTLOGGER.warn("Both chainevolcontext.dat and interf.dat files missing for "+pdbCode);
				continue; // failed predictions
			} else if (!chainevolcontextdatFile.exists()) {
				ROOTLOGGER.warn("chainevolcontext.dat missing but interf.dat file present for "+pdbCode);
				continue;
			} else if (!interfdatFile.exists()) {
				ROOTLOGGER.warn("interf.dat missing but chainevolcontext.dat file present for "+pdbCode);
				continue;				
			}

			ChainInterfaceList cil = Utils.readChainInterfaceList(interfdatFile);
			ChainEvolContextList cecl = Utils.readChainEvolContextList(chainevolcontextdatFile);
			InterfaceEvolContextList iecList = new InterfaceEvolContextList(pdbCode, cil, cecl);
			
			
			for (int id:toAnalyse.get(pdbCode)) {
				
				ChainInterface interf = cil.get(id);
				InterfaceEvolContext iec = iecList.get(id-1);
				ChainEvolContext cec1 = iec.getChainEvolContext(0);
				ChainEvolContext cec2 = iec.getChainEvolContext(1);
				
				if (!cec1.hasQueryMatch() || !cec2.hasQueryMatch()) continue;
				if (cec1.getNumHomologs()<10 || cec2.getNumHomologs()<10) continue;


				printTabular(interf.getFirstMolecule(),cec1,truth);
				
				if (cec2.getRepresentativeChainCode().equals(cec1.getRepresentativeChainCode())) continue;
				
				printTabular(interf.getSecondMolecule(),cec2,truth);
				
			}
		}

	}
	
	private static void printTabular(PdbChain chain, ChainEvolContext cec, CallType truth) {
		
		System.out.printf("%s\t%s\t%s\t%s\t%6s\t%4s\t%6s\t%4s\n","pdb","resser","pdbser","res","asa","rsa","bsa","s"); 
				
		String pdbCode = chain.getParent().getPdbCode();
		
		for (Residue res:chain) {
			double asa = res.getAsa();
			double bsa = res.getBsa();
			double rsa = res.getRsa();
			int uniprotPos = cec.getQueryUniprotPosForPDBPos(res.getSerial());
			if (uniprotPos==-1) continue;
			double entropy = cec.getConservationScores(ScoringType.ENTROPY).get(uniprotPos);
			
			// the idea is to print the "real" ASAs, i.e. if it is more than a monomer, we have to print the ASAs in the complex
			if (truth==CallType.BIO) {
				asa = asa - bsa;
			}
			
			System.out.printf("%s\t%d\t%s\t%s\t%6.2f\t%4.2f\t%6.2f\t%5.2f\n",
					pdbCode,res.getSerial(),res.getPdbSerial(),res.getLongCode(),asa,rsa,bsa,entropy);
		}
	}
	
}
