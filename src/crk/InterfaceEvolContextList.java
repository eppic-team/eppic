package crk;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math.random.RandomDataImpl;

import crk.predictors.EvolInterfZPredictor;
import crk.predictors.EvolRimCorePredictor;

import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.Residue;

public class InterfaceEvolContextList implements Iterable<InterfaceEvolContext>, Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final String IDENTIFIER_HEADER       = "# PDB identifier:";
	private static final String SCORE_METHOD_HEADER 	= "# Score method:";
	private static final String SCORE_TYPE_HEADER   	= "# Score type:";
	private static final String NUM_HOMS_CUTOFF_HEADER  = "# Min number homologs required:";
	private static final String SEQUENCE_ID_HEADER  	= "# Sequence identity cutoff:";
	private static final String QUERY_COV_HEADER    	= "# Query coverage cutoff:";
	private static final String MAX_NUM_SEQS_HEADER     = "# Max num sequences used:";
	private static final String BIO_XTAL_CALL_HEADER         = "# Bio-xtal rim/core call cutoff:";
	private static final String ZSCORE_CUTOFF_HEADER    = "# Z-score cutoff:";
	private static final String BSA_TO_ASA_CUTOFF_HEADER = "# Core assignment cutoff:";
	
	
	private List<InterfaceEvolContext> list;
	private List<EvolRimCorePredictor> evolRimCorePredictors;
	private List<EvolInterfZPredictor> evolInterfZPredictors;
	
	private ChainInterfaceList chainInterfList; // we keep the reference also to be able to call methods from it
		
	private String pdbName;
	private ScoringType scoType;
	private boolean isScoreWeighted;
	private double callCutoff;
	private int homologsCutoff;
	private double idCutoff;
	private double queryCovCutoff;
	private int maxNumSeqsCutoff;
	
	private double zScoreCutoff;
	
	public InterfaceEvolContextList(String pdbName, int homologsCutoff,  
			double idCutoff, double queryCovCutoff, int maxNumSeqsCutoff) {
		this.pdbName = pdbName;
		this.homologsCutoff = homologsCutoff;
		this.idCutoff = idCutoff;
		this.queryCovCutoff = queryCovCutoff;
		this.maxNumSeqsCutoff = maxNumSeqsCutoff;
		
		list = new ArrayList<InterfaceEvolContext>();
		evolRimCorePredictors = new ArrayList<EvolRimCorePredictor>();
		evolInterfZPredictors = new ArrayList<EvolInterfZPredictor>();
		
	}
	
	public int size() {
		return list.size();
	}
	
	public InterfaceEvolContext get(int i){
		return this.list.get(i);
	}
	
	public EvolRimCorePredictor getEvolRimCorePredictor(int i) {
		return this.evolRimCorePredictors.get(i);
	}
	
	public EvolInterfZPredictor getEvolInterfZPredictor(int i) {
		return this.evolInterfZPredictors.get(i);
	}
	
	public void add(InterfaceEvolContext iec) {
		list.add(iec);
		evolRimCorePredictors.add(new EvolRimCorePredictor(iec));
		evolInterfZPredictors.add(new EvolInterfZPredictor(iec));
	}
	
	/**
	 * Given a ChainInterfaceList with all interfaces of a given PDB and a ChainEvolContextList with
	 * all evolutionary contexts of chains of that same PDB adds an InterfaceEvolContext (containing a pair
	 * of ChainEvolContext and a ChainInterface) to this list for each protein-protein interface. 
	 * @param interfaces
	 * @param cecs
	 */
	public void addAll(ChainInterfaceList interfaces, ChainEvolContextList cecs) {
		this.chainInterfList = interfaces;
		for (ChainInterface pi:interfaces) {
			if (pi.isProtein()) {
				InterfaceEvolContext iec = new InterfaceEvolContext(pi, cecs,this);
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
		for (int i=0;i<list.size();i++) {
			evolRimCorePredictors.get(i).scoreEntropy(weighted);
		}
	}
	
	public void scoreKaKs(boolean weighted) {
		this.scoType = ScoringType.KAKS;
		this.isScoreWeighted = weighted;
		for (int i=0;i<list.size();i++) {
			if (list.get(i).canDoKaks()) {
				evolRimCorePredictors.get(i).scoreKaKs(weighted);
			}
		}		
	}
	
	public void scoreZscore() {
		this.scoType = ScoringType.ZSCORE;
		this.isScoreWeighted = false;
		for (int i=0;i<list.size();i++) {
			evolInterfZPredictors.get(i).scoreEntropy();
		}
	}
	
	public boolean isScoreWeighted() {
		return isScoreWeighted;
	}
	
	public void printScoresTable(PrintStream ps) {
		
		printScoringParams(ps, false);
		printScoringHeaders(ps);
		
		for (int i=0;i<list.size();i++) {
			list.get(i).setHomologsCutoff(homologsCutoff);
			evolRimCorePredictors.get(i).printScoresLine(ps);
		}
	}
	
	public void printZscoresTable(PrintStream ps) {
		
		printScoringParams(ps, true);
		printZscoringHeaders(ps);
		
		for (int i=0;i<list.size();i++) {
			list.get(i).setHomologsCutoff(homologsCutoff);
			evolInterfZPredictors.get(i).printScoresLine(ps);
		}
		
	}
	
	public void writeScoresPDBFiles(CRKParams params, String suffix) throws IOException {
		for (InterfaceEvolContext iec:this) {
			if (scoType==ScoringType.ENTROPY || (scoType==ScoringType.KAKS && iec.canDoKaks())) {
				iec.writePdbFile(params.getOutputFile("."+iec.getInterface().getId()+suffix),scoType);
			}
		}
	}
	
	public double getCallCutoff() {
		return callCutoff;
	}
	
	public void setCallCutoff(double callCutoff) {
		this.callCutoff = callCutoff;
		for (int i=0;i<list.size();i++) {
			evolRimCorePredictors.get(i).setCallCutoff(callCutoff);	
		}
	}

	public double getZscoreCutoff() {
		return zScoreCutoff;
	}
	
	public void setZscoreCutoff(double zScoreCutoff) {
		this.zScoreCutoff = zScoreCutoff;
		for (int i=0;i<list.size();i++) {
			evolInterfZPredictors.get(i).setZscoreCutoff(zScoreCutoff);
		}
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
		for (int i=0;i<list.size();i++) {
			evolRimCorePredictors.get(i).resetCall();
			evolInterfZPredictors.get(i).resetCall();
		}
	}

	private void printScoringParams(PrintStream ps, boolean zscore) {
		ps.println(IDENTIFIER_HEADER+" "+pdbName);
		ps.println(SCORE_METHOD_HEADER+" "+scoType.getName());
		ps.println(SCORE_TYPE_HEADER+"   "+(isScoreWeighted?"weighted":"unweighted"));
		ps.println(NUM_HOMS_CUTOFF_HEADER+" "+homologsCutoff);
		ps.printf (SEQUENCE_ID_HEADER+" %4.2f\n",idCutoff);
		ps.printf (QUERY_COV_HEADER+" %4.2f\n",queryCovCutoff);
		ps.println(MAX_NUM_SEQS_HEADER+" "+maxNumSeqsCutoff);
		if (!zscore) ps.printf (BIO_XTAL_CALL_HEADER+"  %4.2f\n",callCutoff);
		if (zscore) ps.printf (ZSCORE_CUTOFF_HEADER+" %5.2f\n",zScoreCutoff);
		ps.printf (BSA_TO_ASA_CUTOFF_HEADER+" %4.2f\n",list.get(0).getInterface().getBsaToAsaCutoff());
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
	
	private static void printZscoringHeaders(PrintStream ps) {
		ps.printf("%15s\t%6s\t","interface","area");
		ps.printf("%5s\t%5s\t%5s","size1", "size2","CA");
		ps.print("\t");
		ps.printf("%2s\t%2s\t","n1","n2");
		ps.printf("%5s\t%5s\t%5s\t%5s","core1","mean","sigma","z1");
		ps.print("\t");
		ps.printf("%5s\t%5s\t%5s\t%5s","core2","mean","sigma","z2");
		ps.print("\t");
		ps.printf("%6s\t%5s\t%6s\t%6s\t%6s\t%6s",
				"call","score",CallType.BIO.getName(),CallType.CRYSTAL.getName(),CallType.GRAY.getName(),CallType.NO_PREDICTION.getName());
		ps.println();
		
	}
	
	public List<String> getNumHomologsStrings() {
		return this.get(0).getChainEvolContextList().getNumHomologsStrings(scoType);
	}
	
	/**
	 * Given a PDB chain code returns the Set of residues that are in the surface but belong to NO
	 * interface (above given minInterfArea) 
	 * @param pdbChainCode
	 * @param minInterfArea
	 * @return
	 */
	public List<Residue> getResiduesNotInInterfaces(String pdbChainCode, double minInterfArea) {
		return this.chainInterfList.getResiduesNotInInterfaces(pdbChainCode, minInterfArea);
	}
	
	private ChainEvolContext getChainEvolContext(String pdbChainCode) {
		return this.list.get(0).getChainEvolContextList().getChainEvolContext(pdbChainCode);
	}
	
	/**
	 * Returns the distribution of evolutionary scores of random subsets of residues in the surface (not belonging 
	 * to any interface above minInterfArea) for given pdbChainCode and scoType.
	 * @param pdbChainCode
	 * @param minInterfArea the residues considered will be those that are not in interfaces above this area value
	 * @param numSamples number of samples of size sampleSize to be taken from the surface
	 * @param sampleSize number of residues in each sample
	 * @param scoType
	 * @return
	 */
	public double[] getSurfaceScoreDist(String pdbChainCode, double minInterfArea, int numSamples, int sampleSize, ScoringType scoType) {
		if (sampleSize==0) return new double[0];
		
		double[] dist = new double[numSamples];

		RandomDataImpl rd = new RandomDataImpl();
		for (int i=0;i<numSamples;i++) {
			Object[] sample = rd.nextSample(getResiduesNotInInterfaces(pdbChainCode, minInterfArea), sampleSize);
			List<Residue> residues = new ArrayList<Residue>(sample.length);
			for (int j=0;j<sample.length;j++){
				residues.add((Residue)sample[j]);
			}
			dist[i] = getChainEvolContext(pdbChainCode).calcScoreForResidueSet(residues, scoType, false);
			
			//Collections.sort(residues, new Comparator<Residue>() {
			//	public int compare(Residue o1, Residue o2) {
			//		if (o1.getSerial()<o2.getSerial()) return -1;
			//		if (o1.getSerial()>o2.getSerial()) return 1;
			//		return 0;
			//	}
			//});
			//System.out.print("draw "+i+": [");
			//for (Residue res:residues) {
			//	System.out.print(res.getSerial()+" ");
			//}
			//System.out.println("]");
		}		
		
		return dist;
	}
}
