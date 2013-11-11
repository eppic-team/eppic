package crk;

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
	
	private static final String IDENTIFIER_HEADER        = "# PDB identifier:";
	private static final String SCORE_METHOD_HEADER 	 = "# Score method:";
	private static final String MIN_NUM_SEQS_HEADER      = "# Min number homologs required:";
	private static final String HOM_SOFT_ID_HEADER  	 = "# Sequence identity soft cutoff:";
	private static final String HOM_HARD_ID_HEADER  	 = "# Sequence identity hard cutoff:";
	private static final String QUERY_COV_HEADER    	 = "# Query coverage cutoff:";
	private static final String MAX_NUM_SEQS_HEADER      = "# Max num sequences used:";
	private static final String BIO_XTAL_CALL_HEADER     = "# Bio/xtal call core-rim cutoff:";
	private static final String ZSCORE_CUTOFF_HEADER     = "# Bio/xtal call core-surface cutoff:";
	private static final String BSA_TO_ASA_CUTOFF_HEADER = "# Core assignment cutoff:";
	
	
	private List<InterfaceEvolContext> list;
	private List<EvolRimCorePredictor> evolRimCorePredictors;
	private List<EvolInterfZPredictor> evolInterfZPredictors;
	
	private ChainInterfaceList chainInterfList; // we keep the reference also to be able to call methods from it
	private ChainEvolContextList cecs;
		
	private String pdbName;
	private ScoringType scoType;

	private int minNumSeqs;
	private double homSoftIdCutoff;
	private double homHardIdCutoff;
	private double queryCovCutoff;
	private int maxNumSeqs;
	private double caCutoffForRimCore;
	private double caCutoffForZscore;

	private double coreRimScoreCutoff;
	private double coreSurfScoreCutoff;
	
	private boolean usePdbResSer;
	
	/**
	 * Constructs a InterfaceEvolContextList given a ChainInterfaceList with all 
	 * interfaces of a given PDB and a ChainEvolContextList with all evolutionary 
	 * contexts of chains of that same PDB. Adds an InterfaceEvolContext (containing a pair
	 * of ChainEvolContext and a ChainInterface) to this list for each protein-protein interface. 
	 * @param interfaces
	 * @param cecs
	 */
	public InterfaceEvolContextList(String pdbName, ChainInterfaceList interfaces, ChainEvolContextList cecs) {
		this.pdbName = pdbName;
		this.minNumSeqs = cecs.getMinNumSeqs();
		this.homSoftIdCutoff = cecs.getHomSoftIdCutoff();
		this.homHardIdCutoff = cecs.getHomHardIdCutoff();
		this.queryCovCutoff = cecs.getQueryCovCutoff();
		this.maxNumSeqs = cecs.getMaxNumSeqs();
				
		
		list = new ArrayList<InterfaceEvolContext>();
		evolRimCorePredictors = new ArrayList<EvolRimCorePredictor>();
		evolInterfZPredictors = new ArrayList<EvolInterfZPredictor>();
	
		this.chainInterfList = interfaces;
		this.cecs = cecs;
		
		for (ChainInterface pi:interfaces) {
			if (pi.isProtein()) {
				InterfaceEvolContext iec = new InterfaceEvolContext(pi, this);
				this.add(iec);
			}
		}
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
	
	private void add(InterfaceEvolContext iec) {
		list.add(iec);
		evolRimCorePredictors.add(new EvolRimCorePredictor(iec));
		evolInterfZPredictors.add(new EvolInterfZPredictor(iec));
	}
	
	@Override
	public Iterator<InterfaceEvolContext> iterator() {
		return list.iterator();
	}
	
	public void scoreEntropy(boolean weighted) {
		this.scoType = ScoringType.ENTROPY;
		for (int i=0;i<list.size();i++) {
			evolRimCorePredictors.get(i).scoreEntropy(weighted);
		}
	}
	
	public void scoreZscore() {
		this.scoType = ScoringType.ZSCORE;
		for (int i=0;i<list.size();i++) {
			evolInterfZPredictors.get(i).scoreEntropy();
		}
	}
	
	public void printScoresTable(PrintStream ps) {
		
		printScoringParams(ps, false);
		printScoringHeaders(ps);
		
		for (int i=0;i<list.size();i++) {
			list.get(i).setMinNumSeqs(minNumSeqs);
			evolRimCorePredictors.get(i).printScoresLine(ps);
		}
	}
	
	public void printZscoresTable(PrintStream ps) {
		
		printScoringParams(ps, true);
		printZscoringHeaders(ps);
		
		for (int i=0;i<list.size();i++) {
			list.get(i).setMinNumSeqs(minNumSeqs);
			evolInterfZPredictors.get(i).printScoresLine(ps);
		}
		
	}
	
	public void setCoreRimScoreCutoff(double coreRimScoreCutoff) {
		this.coreRimScoreCutoff = coreRimScoreCutoff;
		for (int i=0;i<list.size();i++) {
			evolRimCorePredictors.get(i).setCallCutoff(coreRimScoreCutoff);	
		}
	}

	public void setCoreSurfScoreCutoff(double coreSurfScoreCutoff) {
		this.coreSurfScoreCutoff = coreSurfScoreCutoff;
		for (int i=0;i<list.size();i++) {
			evolInterfZPredictors.get(i).setCallCutoff(coreSurfScoreCutoff);
		}
	}

	public void setRimCorePredBsaToAsaCutoff(double bsaToAsaCutoff, double minAsaForSurface) {
		this.caCutoffForRimCore = bsaToAsaCutoff;
		chainInterfList.calcRimAndCores(bsaToAsaCutoff, minAsaForSurface);
		
		for (int i=0;i<list.size();i++) {
			evolRimCorePredictors.get(i).setBsaToAsaCutoff(bsaToAsaCutoff, minAsaForSurface);
		}		
	}
	
	public void setZPredBsaToAsaCutoff(double bsaToAsaCutoff, double minAsaForSurface) {
		this.caCutoffForZscore = bsaToAsaCutoff;
		chainInterfList.calcRimAndCores(bsaToAsaCutoff, minAsaForSurface);
		
		for (int i=0;i<list.size();i++) {
			evolInterfZPredictors.get(i).setBsaToAsaCutoff(bsaToAsaCutoff, minAsaForSurface);
		}		
	}
	
	/**
	 * Whether the output warnings and PDB files are to be written with
	 * PDB residue serials or CIF (SEQRES) residue serials
	 * @param usePdbResSer if true PDB residue serials are used, if false CIF
	 * residue serials are used
	 */
	public boolean isUsePdbResSer() {
		return usePdbResSer;
	}
	
	/**
	 * Sets whether the output warnings and PDB files are to be written with
	 * PDB residue serials or CIF (SEQRES) residue serials
	 * @param usePdbResSer if true PDB residue serials are used, if false CIF
	 * residue serials are used
	 */
	public void setUsePdbResSer(boolean usePdbResSer) {
		this.usePdbResSer = usePdbResSer;
	}
	
	public int getHomologsCutoff() {
		return minNumSeqs;
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
		ps.println(MIN_NUM_SEQS_HEADER+" "+minNumSeqs);
		ps.printf (HOM_SOFT_ID_HEADER+" %4.2f\n",homSoftIdCutoff);
		ps.printf (HOM_HARD_ID_HEADER+" %4.2f\n",homHardIdCutoff);
		ps.printf (QUERY_COV_HEADER+" %4.2f\n",queryCovCutoff);
		ps.println(MAX_NUM_SEQS_HEADER+" "+maxNumSeqs);
		if (!zscore) ps.printf (BIO_XTAL_CALL_HEADER+"  %4.2f\n",coreRimScoreCutoff);
		if (zscore)  ps.printf (ZSCORE_CUTOFF_HEADER+" %5.2f\n",coreSurfScoreCutoff);
		if (!zscore) ps.printf (BSA_TO_ASA_CUTOFF_HEADER+" %4.2f\n", caCutoffForRimCore);
		if (zscore)  ps.printf (BSA_TO_ASA_CUTOFF_HEADER+" %4.2f\n", caCutoffForZscore);
	}
	
	private static void printScoringHeaders(PrintStream ps) {
		ps.printf("%15s\t%6s\t","interface","area");
		ps.printf("%5s\t%5s\t%5s\t","size1", "size2","CA");
		ps.printf("%2s\t%2s\t","n1","n2");
		ps.printf("%5s\t%5s\t%5s\t%6s\t","core1","rim1","cr1","call1");
		ps.printf("%5s\t%5s\t%5s\t%6s\t","core2","rim2","cr2","call2");
		ps.printf("%6s\t%5s","call","score");
		ps.println();
	}
	
	private static void printZscoringHeaders(PrintStream ps) {
		ps.printf("%15s\t%6s\t","interface","area");
		ps.printf("%5s\t%5s\t%5s","size1", "size2","CA");
		ps.print("\t");
		ps.printf("%2s\t%2s\t","n1","n2");
		ps.printf("%5s\t%5s\t%5s\t%5s\t%6s\t","core1","mean","sigma","cs1","call1");
		ps.printf("%5s\t%5s\t%5s\t%5s\t%6s\t","core2","mean","sigma","cs2","call2");
		ps.printf("%6s\t%5s","call","score");
		ps.println();
		
	}
	
	public ChainEvolContextList getChainEvolContextList() {
		return cecs;	
	}
	
	/**
	 * Given a PDB chain code returns the Set of residues that are in the surface but belong to NO
	 * interface (above given minInterfArea) 
	 * @param pdbChainCode
	 * @param minInterfArea
	 * @return
	 */
	public List<Residue> getResiduesNotInInterfaces(String pdbChainCode, double minInterfArea, double minAsaForSurface) {
		return this.chainInterfList.getResiduesNotInInterfaces(pdbChainCode, minInterfArea, minAsaForSurface);
	}
	
	/**
	 * Gets the ChainEvolContext corresponding to the given PDB chain code (can be 
	 * any PDB chain code, representative or not)
	 * 
	 * @param pdbChainCode
	 * @return
	 */
	public ChainEvolContext getChainEvolContext(String pdbChainCode) {
		return getChainEvolContextList().getChainEvolContext(pdbChainCode);
	}
	
	/**
	 * Returns the distribution of evolutionary scores of random subsets of residues in the surface (not belonging 
	 * to any interface above minInterfArea) for given pdbChainCode and scoType.
	 * @param pdbChainCode
	 * @param minInterfArea the residues considered will be those that are not in interfaces above this area value
	 * @param numSamples number of samples of size sampleSize to be taken from the surface
	 * @param sampleSize number of residues in each sample
	 * @param scoType
	 * @param minAsaForSurface the minimum ASA for a residue to be considered surface
	 * @return
	 */
	public double[] getSurfaceScoreDist(String pdbChainCode, double minInterfArea, int numSamples, int sampleSize, ScoringType scoType, double minAsaForSurface) {
		if (sampleSize==0) return new double[0];
		
		double[] dist = new double[numSamples];

		RandomDataImpl rd = new RandomDataImpl();
		for (int i=0;i<numSamples;i++) {
			Object[] sample = rd.nextSample(getResiduesNotInInterfaces(pdbChainCode, minInterfArea, minAsaForSurface), sampleSize);
			List<Residue> residues = new ArrayList<Residue>(sample.length);
			for (int j=0;j<sample.length;j++){
				residues.add((Residue)sample[j]);
			}
			// note that we must pass weighted=false as the weighting is done on bsas which doesn't make sense at all here 
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
