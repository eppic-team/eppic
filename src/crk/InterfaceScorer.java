package crk;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.Pdb;
import owl.core.structure.PdbAsymUnit;

public class InterfaceScorer {

	private static final Log LOGGER = LogFactory.getLog(InterfaceScorer.class);

	private InterfaceEvolContextList iecList;
	
	private ChainInterfaceList interfaces;
	private TreeMap<String, ChainEvolContext> cecs;
	private PdbAsymUnit pdb;
	
	private File outDir;
	private String baseName;
	private String pdbName;
	
	
	public InterfaceScorer(ChainInterfaceList interfaces, TreeMap<String,ChainEvolContext> cecs, PdbAsymUnit pdb, File outDir, String baseName, String pdbName) {
		this.interfaces = interfaces;
		this.cecs = cecs;
		this.pdb = pdb;
		this.outDir = outDir;
		this.baseName = baseName;
		this.pdbName = pdbName;
	}
	
	public void createInterfEvolContextList(int minHomologsCutoff, int minNumResCA, int minNumResMemberCA, double idCutoff, double queryCoverageCutoff, int maxNumSeqsSelecton, double minInterfAreaReporting) {
		iecList = new InterfaceEvolContextList(pdbName, minHomologsCutoff, minNumResCA, minNumResMemberCA, 
				idCutoff, queryCoverageCutoff, maxNumSeqsSelecton, minInterfAreaReporting);
		for (ChainInterface pi:interfaces) {
			if (pi.isProtein()) {
				ArrayList<ChainEvolContext> chainsEvCs = new ArrayList<ChainEvolContext>();
				Pdb molec1 = pi.getFirstMolecule();
				Pdb molec2 = pi.getSecondMolecule();
				chainsEvCs.add(cecs.get(pdb.getRepChain(molec1.getPdbChainCode())));
				chainsEvCs.add(cecs.get(pdb.getRepChain(molec2.getPdbChainCode())));
				InterfaceEvolContext iec = new InterfaceEvolContext(pi, chainsEvCs);
				iecList.add(iec);
			}
		}		
	}
	
	public void doEntropyScoring(String entropiesFileSuffix, double[] entrCallCutoff, double grayZoneWidth) {
		try {
			for (int callCutoffIdx=0;callCutoffIdx<entrCallCutoff.length;callCutoffIdx++) {
				String suffix = null;
				if (entrCallCutoff.length==1) suffix="";
				else suffix="."+Integer.toString(callCutoffIdx+1);
				PrintStream scoreEntrPS = new PrintStream(new File(outDir,baseName+entropiesFileSuffix+".scores"+suffix));
				// entropy nw
				iecList.scoreEntropy(false);
				iecList.printScoresTable(System.out, entrCallCutoff[callCutoffIdx]-grayZoneWidth, entrCallCutoff[callCutoffIdx]+grayZoneWidth);
				iecList.printScoresTable(scoreEntrPS, entrCallCutoff[callCutoffIdx]-grayZoneWidth, entrCallCutoff[callCutoffIdx]+grayZoneWidth);
				// entropy w
				iecList.scoreEntropy(true);
				iecList.printScoresTable(System.out, entrCallCutoff[callCutoffIdx]-grayZoneWidth, entrCallCutoff[callCutoffIdx]+grayZoneWidth);
				iecList.printScoresTable(scoreEntrPS, entrCallCutoff[callCutoffIdx]-grayZoneWidth, entrCallCutoff[callCutoffIdx]+grayZoneWidth);
				iecList.writeScoresPDBFiles(outDir, baseName, entropiesFileSuffix+".pdb");
				iecList.writeRimCorePDBFiles(outDir, baseName, ".rimcore.pdb");
				scoreEntrPS.close();
			}
		} catch (IOException e) {
			LOGGER.error("Couldn't write final interface entropy scores or related PDB files");
			LOGGER.error(e.getMessage());
			System.exit(1);
		}
	}
	
	public void doKaksScoring(boolean doScoreCRK, String kaksFileSuffix, double[] kaksCallCutoff, double grayZoneWidth) {
		try {
			// ka/ks scoring
			if (doScoreCRK) {
				for (int callCutoffIdx=0;callCutoffIdx<kaksCallCutoff.length;callCutoffIdx++) {
					String suffix = null;
					if (kaksCallCutoff.length==1) suffix="";
					else suffix="."+Integer.toString(callCutoffIdx+1);
					PrintStream scoreKaksPS = new PrintStream(new File(outDir,baseName+kaksFileSuffix+".scores"+suffix));
					// kaks nw
					iecList.scoreKaKs(false);
					iecList.printScoresTable(System.out,  kaksCallCutoff[callCutoffIdx]-grayZoneWidth, kaksCallCutoff[callCutoffIdx]+grayZoneWidth);
					iecList.printScoresTable(scoreKaksPS,  kaksCallCutoff[callCutoffIdx]-grayZoneWidth, kaksCallCutoff[callCutoffIdx]+grayZoneWidth);
					// kaks w
					iecList.scoreKaKs(true);
					iecList.printScoresTable(System.out,  kaksCallCutoff[callCutoffIdx]-grayZoneWidth, kaksCallCutoff[callCutoffIdx]+grayZoneWidth);
					iecList.printScoresTable(scoreKaksPS,  kaksCallCutoff[callCutoffIdx]-grayZoneWidth, kaksCallCutoff[callCutoffIdx]+grayZoneWidth);
					iecList.writeScoresPDBFiles(outDir, baseName, kaksFileSuffix+".pdb");
					iecList.writeRimCorePDBFiles(outDir, baseName, ".rimcore.pdb");
					scoreKaksPS.close();
				}
			}
		} catch (IOException e) {
			LOGGER.error("Couldn't write final interface Ka/Ks scores or related PDB files");
			LOGGER.error(e.getMessage());
			System.exit(1);
		}

	}
}
