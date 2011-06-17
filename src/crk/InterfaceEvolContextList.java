package crk;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;

public class InterfaceEvolContextList implements Iterable<InterfaceEvolContext>, Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final String IDENTIFIER_HEADER       = "# PDB identifier:";
	private static final String SCORE_METHOD_HEADER 	= "# Score method:";
	private static final String SCORE_TYPE_HEADER   	= "# Score type:";
	private static final String NUM_HOMS_CUTOFF_HEADER  = "# Min number homologs required:";
	private static final String SEQUENCE_ID_HEADER  	= "# Sequence identity cutoff:";
	private static final String QUERY_COV_HEADER    	= "# Query coverage cutoff:";
	private static final String MAX_NUM_SEQS_HEADER     = "# Max num sequences used:";
	private static final String BIO_CALL_HEADER         = "# Bio-call cutoff:";
	private static final String XTAL_CALL_HEADER        = "# Xtal-call cutoff:";
	private static final String BSA_TO_ASA_CUTOFFS_HEADER = "# Core assignment cutoffs:";
	
	
	private List<InterfaceEvolContext> list;

	private String pdbName;
	private ScoringType scoType;
	private boolean isScoreWeighted;
	private double bioCutoff;
	private double xtalCutoff;
	private int homologsCutoff;
	private double idCutoff;
	private double queryCovCutoff;
	private int maxNumSeqsCutoff;
	private double minInterfAreaReporting;
	
	public InterfaceEvolContextList(){
		list = new ArrayList<InterfaceEvolContext>();		
	}
	
	public int size() {
		return list.size();
	}
	
	public InterfaceEvolContext get(int i){
		return this.list.get(i);
	}
	
	public InterfaceEvolContextList(String pdbName, int homologsCutoff,  
			double idCutoff, double queryCovCutoff, int maxNumSeqsCutoff, double minInterfAreaReporting) {
		this.pdbName = pdbName;
		this.homologsCutoff = homologsCutoff;
		this.idCutoff = idCutoff;
		this.queryCovCutoff = queryCovCutoff;
		this.maxNumSeqsCutoff = maxNumSeqsCutoff;
		this.minInterfAreaReporting = minInterfAreaReporting;
		
		list = new ArrayList<InterfaceEvolContext>();
	}
	
	public void add(InterfaceEvolContext iec) {
		list.add(iec);
	}
	
	/**
	 * Given a ChainInterfaceList with all interfaces of a given PDB and a ChainEvolContextList with
	 * all evolutionary contexts of chains of that same PDB adds an InterfaceEvolContext (containing a pair
	 * of ChainEvolContext and a ChainInterface) to this list for each protein-portein interface. 
	 * @param interfaces
	 * @param cecs
	 */
	public void addAll(ChainInterfaceList interfaces, ChainEvolContextList cecs) {
		for (ChainInterface pi:interfaces) {
			if (pi.isProtein()) {
				ChainEvolContext[] chainsEvCs = new ChainEvolContext[2];
				chainsEvCs[0] = cecs.getChainEvolContext(pi.getFirstMolecule().getPdbChainCode());
				chainsEvCs[1] = cecs.getChainEvolContext(pi.getSecondMolecule().getPdbChainCode());
				InterfaceEvolContext iec = new InterfaceEvolContext(pi, chainsEvCs);
				this.add(iec);
			}
		}
	}
	
	@Override
	public Iterator<InterfaceEvolContext> iterator() {
		return list.iterator();
	}

	
	
	public void scoreEntropy(boolean weighted) {
		this.scoType = ScoringType.ENTROPY;
		this.isScoreWeighted = weighted;
		for (InterfaceEvolContext iec:this) {
			iec.scoreEntropy(weighted);
		}
	}
	
	public void scoreKaKs(boolean weighted) {
		this.scoType = ScoringType.KAKS;
		this.isScoreWeighted = weighted;
		for (InterfaceEvolContext iec:this) {
			if (iec.canDoCRK()) {
				iec.scoreKaKs(weighted);
			}
		}		
	}
	
	public boolean isScoreWeighted() {
		return isScoreWeighted;
	}
	
	public void printScoresTable(PrintStream ps, double bioCutoff, double xtalCutoff) {
		this.bioCutoff = bioCutoff;
		this.xtalCutoff = xtalCutoff;
		
		printScoringParams(ps);
		printScoringHeaders(ps);
		
		for (InterfaceEvolContext iec:this) {
			if (iec.getInterface().getInterfaceArea()>minInterfAreaReporting) {
				iec.setBioCutoff(bioCutoff);
				iec.setXtalCutoff(xtalCutoff);
				iec.setHomologsCutoff(homologsCutoff);
				iec.printScoresLine(ps);
			}
		}
	}
	
	public void writeScoresPDBFiles(CRKParams params, String suffix) throws IOException {
		for (InterfaceEvolContext iec:this) {
			if (iec.getInterface().getInterfaceArea()>minInterfAreaReporting) {
				iec.writePdbFile(params.getOutputFile("."+iec.getInterface().getId()+suffix));
			}
		}
	}
	
	public void writeResidueDetailsFiles(CRKParams params, String suffix) throws IOException {
		for (InterfaceEvolContext iec:this) {
			if (iec.getInterface().getInterfaceArea()>minInterfAreaReporting) {
				iec.writeResidueDetailsFile(params.getOutputFile("."+iec.getInterface().getId()+"."+suffix),params.isDoScoreCRK());
			}
		}
	}
	
	public double getBioCutoff() {
		return bioCutoff;
	}

	public double getXtalCutoff() {
		return xtalCutoff;
	}

	public int getHomologsCutoff() {
		return homologsCutoff;
	}

	public ScoringType getScoringType() {
		return scoType;
	}
	
	/**
	 * Resets to null all calls, callReasons and warnings of all InterfaceEvolContext in this list.
	 */
	public void resetCalls() {
		for (InterfaceEvolContext iec:this) {
			iec.resetCall();
		}
	}

	private void printScoringParams(PrintStream ps) {
		ps.println(IDENTIFIER_HEADER+" "+pdbName);
		ps.println(SCORE_METHOD_HEADER+" "+scoType.getName());
		ps.println(SCORE_TYPE_HEADER+"   "+(isScoreWeighted?"weighted":"unweighted"));
		ps.println(NUM_HOMS_CUTOFF_HEADER+" "+homologsCutoff);
		ps.printf (SEQUENCE_ID_HEADER+" %4.2f\n",idCutoff);
		ps.printf (QUERY_COV_HEADER+" %4.2f\n",queryCovCutoff);
		ps.println(MAX_NUM_SEQS_HEADER+" "+maxNumSeqsCutoff);
		ps.printf (BIO_CALL_HEADER+"  %4.2f\n",bioCutoff);
		ps.printf (XTAL_CALL_HEADER+" %4.2f\n",xtalCutoff);
		ps.print(BSA_TO_ASA_CUTOFFS_HEADER+" ");
		if (list.get(0).getInterface().isRimAndCoreZoomed()){
			ps.printf("zoomed (%4.2f,%4.2f,%4.2f)\n",
					list.get(0).getInterface().getBsaToAsaSoftCutoff(),
					list.get(0).getInterface().getBsaToAsaCutoff(),
					list.get(0).getInterface().getBsaToAsaRelaxStep());
		} else {
			ps.printf("%4.2f\n",list.get(0).getInterface().getBsaToAsaCutoff());
		}
	}
	
	private static void printScoringHeaders(PrintStream ps) {
		ps.printf("%15s\t%6s\t","interface","area");
		ps.printf("%5s\t%5s\t%5s","size1", "size2","CA");
		ps.print("\t");
		ps.printf("%2s\t%2s\t","n1","n2");
		ps.printf("%5s\t%5s\t%5s","core1","rim1","rat1");
		ps.print("\t");
		ps.printf("%5s\t%5s\t%5s","core2","rim2","rat2");
		ps.print("\t");
		ps.printf("%6s\t%5s\t%6s\t%6s\t%6s\t%6s",
				"call","score",CallType.BIO.getName(),CallType.CRYSTAL.getName(),CallType.GRAY.getName(),CallType.NO_PREDICTION.getName());
		ps.println();
	}
	
	public List<String> getNumHomologsStrings() {
		TreeSet<String> set = new TreeSet<String>(); 
		for (InterfaceEvolContext iec:this) {
			int numHomologs = -1;
			if (scoType==ScoringType.ENTROPY) {
				numHomologs = iec.getFirstChainEvolContext().getNumHomologs();
			} else if (scoType==ScoringType.KAKS) {
				numHomologs = iec.getFirstChainEvolContext().getNumHomologsWithValidCDS();
			}
			set.add(iec.getFirstChainEvolContext().getSeqIndenticalChainStr()+": "+numHomologs);
			numHomologs = -1;
			if (scoType==ScoringType.ENTROPY) {
				numHomologs = iec.getSecondChainEvolContext().getNumHomologs();
			} else if (scoType==ScoringType.KAKS) {
				numHomologs = iec.getSecondChainEvolContext().getNumHomologsWithValidCDS();
			}
			set.add(iec.getSecondChainEvolContext().getSeqIndenticalChainStr()+": "+numHomologs);
		}
		List<String> list = new ArrayList<String>();
		list.addAll(set);
		return list;
	}
	
	
}
