package crk;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import owl.core.structure.ChainInterface;
import owl.core.structure.InterfaceRimCore;
import owl.core.structure.PdbChain;
import owl.core.structure.Residue;
import owl.core.structure.AaResidue;

public class InterfaceEvolContext implements Serializable, InterfaceTypePredictor {

	private static final long serialVersionUID = 1L;

	private static final Log LOGGER = LogFactory.getLog(InterfaceEvolContext.class);

	protected static final int FIRST  = 0;
	protected static final int SECOND = 1;

	private static final double MAX_ALLOWED_UNREL_RES = 0.05; // 5% maximum allowed unreliable residues for core or rim
	private static final int MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE = 6;
	
	private ChainInterface interf;

	private ChainEvolContextList cecs;
	
	// the cache scoring values, these are filled upon call of scoreInterface
	private double[] rimScores;	// cache of the last run scoreEntropy/scoreKaKs (strictly 2 members, 0 FIRST, 1 SECOND, use constants above)
	private double[] coreScores;
	private double finalScore; // cache of the last run final score (average of ratios of both sides) (filled by getCall)
	
	// cached values of scoring (filled upon call of score methods and getCall)
	private ArrayList<Integer> bioCalls; // cache of the votes (lists of interface member indices)
	private ArrayList<Integer> xtalCalls;
	private ArrayList<Integer> grayCalls;
	private ArrayList<Integer> noPredictCalls;
	
	private CallType call; // cached result of the last call to getCall(bioCutoff, xtalCutoff, homologsCutoff, minCoreSize, minMemberCoreSize)
	private ScoringType scoringType; // the type of the last scoring run (either kaks or entropy)
	private String callReason;
	private List<String> warnings;
	
	private double bioCutoff;
	private double xtalCutoff;
	private int homologsCutoff;
	
	private boolean isScoreWeighted;
	
	
	
	public InterfaceEvolContext(ChainInterface interf, ChainEvolContextList cecs) {
		this.interf = interf;
		this.cecs = cecs;
		this.warnings = new ArrayList<String>();
	}

	public ChainInterface getInterface() {
		return interf;
	}
	
	public ChainEvolContext getFirstChainEvolContext() {
		return cecs.getChainEvolContext(interf.getFirstMolecule().getPdbChainCode());
	}
	
	public ChainEvolContext getSecondChainEvolContext() {
		return cecs.getChainEvolContext(interf.getSecondMolecule().getPdbChainCode());
	}
	
	public ChainEvolContext getChainEvolContext(int molecId) {
		if (molecId==FIRST) return getFirstChainEvolContext();
		if (molecId==SECOND) return getSecondChainEvolContext();
		return null;
	}
	
	public ChainEvolContextList getChainEvolContextList() {
		return cecs;
	}
	
	public void setBioCutoff(double bioCutoff) {
		this.bioCutoff = bioCutoff;
	}
	
	public void setXtalCutoff(double xtalCutoff) {
		this.xtalCutoff = xtalCutoff;
	}
	
	public void setHomologsCutoff(int homologsCutoff) {
		this.homologsCutoff = homologsCutoff;
	}
	
	/**
	 * Calculates the entropy scores for this interface.
	 * Subsequently use {@link #getCalls(double, double, int, int, int)} and {@link #getFinalScores()}
	 * to get the calls and final scores.
	 * @param weighted
	 */
	public void scoreEntropy(boolean weighted) {
		scoreInterface(weighted, ScoringType.ENTROPY);
		scoringType = ScoringType.ENTROPY;
		isScoreWeighted = weighted;
	}
	
	/**
	 * Calculates the ka/ks scores for this interface.
	 * Subsequently use {@link #getCalls(double, double, int, int, int)} and {@link #getFinalScores()}
	 * to get the calls and final scores. 
	 * @param weighted
	 */
	public void scoreKaKs(boolean weighted) {
		scoreInterface(weighted, ScoringType.KAKS);
		scoringType = ScoringType.KAKS;
		isScoreWeighted = weighted;
	}

	private void scoreInterface(boolean weighted, ScoringType scoType) {
		
		rimScores = new double[2];
		coreScores = new double[2];

		if (this.interf.isFirstProtein()) {
			InterfaceRimCore rimCore = this.interf.getFirstRimCore();
			rimScores[FIRST]  = calcScore(rimCore.getRimResidues(), FIRST, scoType, weighted);
			coreScores[FIRST] = calcScore(rimCore.getCoreResidues(),FIRST, scoType, weighted);
		} else {
			rimScores[FIRST] = Double.NaN;
			coreScores[FIRST] = Double.NaN;
		}

		if (this.interf.isSecondProtein()) {
			InterfaceRimCore rimCore = this.interf.getSecondRimCore();
			rimScores[SECOND]  = calcScore(rimCore.getRimResidues(), SECOND, scoType, weighted);
			coreScores[SECOND] = calcScore(rimCore.getCoreResidues(),SECOND, scoType, weighted);
		} else {
			rimScores[SECOND] = Double.NaN;
			coreScores[SECOND] = Double.NaN;
		}

	}
		
	private List<Residue> checkResiduesForPDBReliability(List<Residue> residues, ChainEvolContext chain) {
		List<Residue> unreliableResidues = new ArrayList<Residue>();
		for (Residue res:residues){
			int resSer = res.getSerial(); // used to be: chain.getResSerFromPdbResSer(pdbChainCode, res.getPdbSerial()); pdbChainCode was passed
			if (resSer!=-1 && !chain.isPdbSeqPositionMatchingUniprot(resSer)) {
				unreliableResidues.add(res);
			}
		}
		if (!unreliableResidues.isEmpty()) {
			String msg = "Interface residue serials ";
			for (int i=0;i<unreliableResidues.size();i++) {
				msg+=unreliableResidues.get(i).getParent().getPdbChainCode()+" "+unreliableResidues.get(i).getLongCode()+unreliableResidues.get(i).getSerial();
				if (i!=unreliableResidues.size()-1) {
					msg+=",";
				}
			}
			msg+=" are not reliable because of PDB SEQRES not matching the Uniprot sequence at those positions.";
			LOGGER.warn(msg);
			warnings.add(msg);
		}
		return unreliableResidues;
	}
	
	private List<Residue> checkResiduesForCDSReliability(List<Residue> residues, ChainEvolContext chain) {
		List<Residue> unreliableResidues = new ArrayList<Residue>();
		for (Residue res:residues){
			int resSer = res.getSerial(); // used to be: chain.getResSerFromPdbResSer(pdbChainCode, res.getPdbSerial()); pdbChainCode was passed
			if (resSer!=-1 && !chain.isPdbSeqPositionReliable(resSer)) {
				unreliableResidues.add(res);
			}				
		}
		if (!unreliableResidues.isEmpty()) {
			String msg = "Interface residue serials ";
			for (int i=0;i<unreliableResidues.size();i++) {
				msg+=unreliableResidues.get(i).getParent().getPdbChainCode()+" "+unreliableResidues.get(i).getLongCode()+unreliableResidues.get(i).getSerial();
				if (i!=unreliableResidues.size()-1) {
					msg+=(",");
				}
			}
			msg+=" are not reliable because of inaccurate CDS sequence information.";		
			LOGGER.warn(msg);
			warnings.add(msg);
		}
		return unreliableResidues;
	}
	
	private double calcScore(List<Residue> residues, int molecId, ScoringType scoType, boolean weighted) {
		ChainEvolContext chain = getChainEvolContext(molecId);

		double totalScore = 0.0;
		double totalWeight = 0.0;
		List<Double> conservScores = chain.getConservationScores(scoType);
		for (Residue res:residues){
			int resSer = res.getSerial(); // used to be: chain.getResSerFromPdbResSer(pdbChainCode, res.getPdbSerial());

			if (resSer!=-1) {
				int queryPos = -2;
				if (scoType==ScoringType.ENTROPY) {
					queryPos = chain.getQueryUniprotPosForPDBPos(resSer); 
				} else if (scoType==ScoringType.KAKS) {
					queryPos = chain.getQueryCDSPosForPDBPos(resSer);
				}
				if (queryPos!=-1) {   
					double weight = 1.0;
					if (weighted) {
						weight = res.getBsa();
					}
					totalScore += weight*(conservScores.get(queryPos));
					totalWeight += weight;
				} else {
					
				}
			} else {
				String msg = "Can't map PISA pdb residue serial "+res.getPdbSerial()+" (res type: "+res.getLongCode()+", PISA serial: "+res.getSerial()+")";
				msg+=" The residue will not be used for scoring";
				LOGGER.warn(msg);
				warnings.add(msg);
			}
		}
		return totalScore/totalWeight;
	}
	
	/**
	 * Writes out a PDB file with the 2 chains of this interface with evolutionary scores 
	 * as b-factors. 
	 * In order for the file to be handled properly by molecular viewers whenever the two
	 * chains have the same code we rename the second one to the next letter in alphabet.
	 * PDB chain codes are used for the output, not cif codes.  
	 * @param file
	 * @throws IOException
	 */
	public void writePdbFile(File file) throws IOException {

		if (interf.isFirstProtein() && interf.isSecondProtein()) {

			setConservationScoresAsBfactors(FIRST,scoringType);
			setConservationScoresAsBfactors(SECOND,scoringType);
			
			this.interf.writeToPdbFile(file);
		}
	}
	
	/**
	 * Set the b-factors of the given molecId (FIRST or SECOND) to conservation score values (entropy or ka/ks).
	 * @param molecId
	 * @param scoType
	 * @throws NullPointerException if ka/ks ratios are not calculated yet by calling {@link #computeKaKsRatiosSelecton(File)}
	 */
	private void setConservationScoresAsBfactors(int molecId, ScoringType scoType) {
		List<Double> conservationScores = null;
		PdbChain pdb = null;
		if (molecId==FIRST) {
			pdb = interf.getFirstMolecule();
		} else if (molecId==SECOND) {
			pdb = interf.getSecondMolecule();
		}
		conservationScores = getChainEvolContext(molecId).getConservationScores(scoType);
		
		HashMap<Integer,Double> map = new HashMap<Integer, Double>();
		for (Residue residue:pdb) {
			if (!(residue instanceof AaResidue)) continue;
			int resser = residue.getSerial();
			int queryPos = -2;
			if (scoType==ScoringType.ENTROPY) {
				queryPos = getChainEvolContext(molecId).getQueryUniprotPosForPDBPos(resser); 
			} else if (scoType==ScoringType.KAKS) {
				queryPos = getChainEvolContext(molecId).getQueryCDSPosForPDBPos(resser);
			}
			if (queryPos!=-1) {   
				map.put(resser, conservationScores.get(queryPos));	
			}
		}
		pdb.setBFactorsPerResidue(map);		
	}
	
	private double getScoreRatio(int molecId) {
		double ratio = coreScores[molecId]/rimScores[molecId];

		return ratio;
	}
	
	private boolean hasEnoughHomologs(int molecId, int homologsCutoff){
		return this.getChainEvolContext(molecId).getNumHomologs()>=homologsCutoff;
	}

	private boolean isProtein(int molecId) {
		if (molecId==FIRST) {
			return this.interf.isFirstProtein();
		} else if (molecId==SECOND) {
			return this.interf.isSecondProtein();
		} else {
			throw new IllegalArgumentException("Fatal error! Wrong molecId "+molecId);
		}
	}
	
	private int countUnreliableCoreRes(int molecId) {
		InterfaceRimCore rimCore = null;
		if (molecId==FIRST) {
			rimCore = this.interf.getFirstRimCore();
		} else if (molecId==SECOND) {
			rimCore = this.interf.getSecondRimCore();
		}

		int count = 0;

		List<Residue> unreliableCoreResidues = new ArrayList<Residue>(); 
		unreliableCoreResidues.addAll(checkResiduesForPDBReliability(rimCore.getCoreResidues(), getChainEvolContext(molecId)));
		if (scoringType==ScoringType.KAKS) {
			unreliableCoreResidues.addAll(checkResiduesForCDSReliability(rimCore.getCoreResidues(), getChainEvolContext(molecId)));
		}
		count = unreliableCoreResidues.size();
		return count;
	}
	
	private int countUnreliableRimRes(int molecId) {
		InterfaceRimCore rimCore = null;
		if (molecId==FIRST) {
			rimCore = this.interf.getFirstRimCore();
		} else if (molecId==SECOND) {
			rimCore = this.interf.getSecondRimCore();
		}
		int count = 0;

		List<Residue> unreliableRimResidues = new ArrayList<Residue>();

		unreliableRimResidues.addAll(checkResiduesForPDBReliability(rimCore.getRimResidues(), getChainEvolContext(molecId)));
		if (scoringType==ScoringType.KAKS) {
			unreliableRimResidues.addAll(checkResiduesForCDSReliability(rimCore.getRimResidues(), getChainEvolContext(molecId)));
		}
		count=unreliableRimResidues.size();

		return count;
	}

	/**
	 * Gets the interface partners prediction calls.
	 * @param molecId
	 * @return
	 */
	private CallType getMemberCall(int molecId) {
		
		int memberSerial = molecId+1;
		InterfaceRimCore rimCore = null;
		if (molecId==FIRST) {
			rimCore = this.interf.getFirstRimCore();
		} else if (molecId==SECOND) {
			rimCore = this.interf.getSecondRimCore();
		}
		
		int countsUnrelCoreRes = countUnreliableCoreRes(molecId);
		int countsUnrelRimRes = countUnreliableRimRes(molecId);
		double ratio = this.getScoreRatio(molecId);
		
		CallType call = null;

		if (!isProtein(molecId)) {
			LOGGER.info("Interface "+this.interf.getId()+", member "+memberSerial+" calls NOPRED because it is not a protein");
			warnings.add("Interface member "+memberSerial+" calls NOPRED because it is not a protein");
			call = CallType.NO_PREDICTION;
		}
		else if (!hasEnoughHomologs(molecId,homologsCutoff)) {
			LOGGER.info("Interface "+this.interf.getId()+", member "+memberSerial+" calls NOPRED because there are not enough homologs to calculate evolutionary scores");
			warnings.add("Interface member "+memberSerial+" calls NOPRED because there are not enough homologs to calculate evolutionary scores");
			call = CallType.NO_PREDICTION;
		}
		else if (((double)countsUnrelCoreRes/(double)rimCore.getCoreSize())>MAX_ALLOWED_UNREL_RES) {
			LOGGER.info("Interface "+this.interf.getId()+", member "+memberSerial+" calls NOPRED because there are not enough reliable core residues ("+
					countsUnrelCoreRes+" unreliable residues out of "+rimCore.getCoreSize()+" residues in core)");
			warnings.add("Interface member "+memberSerial+" calls NOPRED because there are not enough reliable core residues: "+
					countsUnrelCoreRes+" unreliable out of "+rimCore.getCoreSize()+" in core");

			call = CallType.NO_PREDICTION;
		}
		else if (((double)countsUnrelRimRes/(double)rimCore.getRimSize())>MAX_ALLOWED_UNREL_RES) {
			LOGGER.info("Interface "+this.interf.getId()+", member "+memberSerial+" calls NOPRED because there are not enough reliable rim residues ("+
					countsUnrelRimRes+" unreliable residues out of "+rimCore.getRimSize()+" residues in rim)");
			warnings.add("Interface member "+memberSerial+" calls NOPRED because there are not enough reliable rim residues: "+
					countsUnrelRimRes+" unreliable out of "+rimCore.getRimSize()+" in rim");
			call = CallType.NO_PREDICTION;
		}
		else {
			if (ratio<bioCutoff) {
				call = CallType.BIO;
			} else if (ratio>xtalCutoff) {
				call = CallType.CRYSTAL;
			} else if (Double.isNaN(ratio)) {
				warnings.add("Interface member "+memberSerial+" calls NOPRED because score is NaN");
				call = CallType.NO_PREDICTION;
			} else {
				call = CallType.GRAY;
			}
		}

		
		return call;
	}

	@Override
	public CallType getCall() {

		if (call!=null) return call;
		
		// the votes with voters (no anonymous vote here!)
		bioCalls = new ArrayList<Integer>();
		xtalCalls = new ArrayList<Integer>();
		grayCalls = new ArrayList<Integer>();
		noPredictCalls = new ArrayList<Integer>();

		for (int k=0;k<2;k++) {
			CallType memberCall = getMemberCall(k);
			// cast your votes!
			if (memberCall == CallType.BIO) {
				bioCalls.add(k);
			}
			else if (memberCall == CallType.CRYSTAL) {
				xtalCalls.add(k);
			}
			else if (memberCall == CallType.GRAY) {
				grayCalls.add(k);
			}
			else if (memberCall == CallType.NO_PREDICTION) {
				noPredictCalls.add(k);
			}
		}
		
		int countBio = bioCalls.size();
		int countXtal = xtalCalls.size();
		int countGray = grayCalls.size();
		int countNoPredict = noPredictCalls.size();

		// decision time!
		if ((interf.getFirstRimCore().getCoreSize()+interf.getSecondRimCore().getCoreSize())<MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE) {
			finalScore = Double.NaN;
			call = CallType.NO_PREDICTION;
			callReason = "Not enough core residues to calculate evolutionary score";
		} else if (countNoPredict==2) {
			finalScore = getAvrgRatio(noPredictCalls);
			call = CallType.NO_PREDICTION;
			callReason = "Both interface members called NOPRED";
		} else if (countBio>countXtal) {
			//TODO check the discrepancies among the different voters. The variance could be a measure of the confidence of the call
			//TODO need to do a study about the correlation of scores in members of the same interface
			//TODO it might be the case that there is good agreement and bad agreement would indicate things like a bio-mimicking crystal interface
			finalScore = getAvrgRatio(bioCalls);
			call = CallType.BIO;
			callReason = "Majority BIO votes";
		} else if (countXtal>countBio) {
			finalScore = getAvrgRatio(xtalCalls);
			call = CallType.CRYSTAL;
			callReason = "Majority XTAL votes";
		} else if (countGray>countBio+countXtal) {
			// we use as final score the average of all gray member scores
			finalScore = getAvrgRatio(grayCalls);
			call = CallType.GRAY;
			callReason = "Majority GRAY votes";
		} else if (countBio==countXtal) {
			//TODO we are taking simply the average, is this the best solution?
			// weighting is not done here, scores are calculated either weighted/non-weighted before
			List<Integer> indices = new ArrayList<Integer>();
			indices.addAll(bioCalls);
			indices.addAll(xtalCalls);
			finalScore = getAvrgRatio(indices);
			callReason = "Interface member "+(bioCalls.get(0)+1)+" called BIO and member "+(xtalCalls.get(0)+1)+" called XTAL. ";
			if (finalScore<bioCutoff) {
				call = CallType.BIO;
				callReason += "Average score is below BIO cutoff ("+String.format("%4.2f", bioCutoff)+")";
			} else if (finalScore>xtalCutoff) {
				call = CallType.CRYSTAL;
				callReason += "Average score is above XTAL cutoff ("+String.format("%4.2f", xtalCutoff)+")";
			} else if (Double.isNaN(finalScore)) {
				call = CallType.NO_PREDICTION;
				callReason += "Average score is NaN";
			} else {
				call = CallType.GRAY;
				callReason += "Average score is in gray area (between BIO cutoff "+String.format("%4.2f", bioCutoff)+" and XTAL cutoff "+String.format("%4.2f", xtalCutoff)+")";
			}
		}
		return call;
	}	

	@Override
	public String getCallReason() {
		return callReason;
	}
	
	@Override
	public List<String> getWarnings() {
		return this.warnings;
	}
	
	public ScoringType getScoringType() {
		return this.scoringType;
	}
	
	/**
	 * Gets the average core to rim ratio for the list of given indices of members.
	 * @param a list of indices of the memberScores List
	 * @return
	 */
	private double getAvrgRatio(List<Integer> indices) {
		double sum = 0.0;
		for (int ind:indices) {
			sum+=coreScores[ind]/rimScores[ind];				
		}
		return sum/(double)indices.size();
	}

	/**
	 * Gets the final score computed upon last 
	 * call of {@link #getCall(double, double, int, int, int)}
	 * Final score is the score computed from both members of the interface.
	 * @return
	 */
	public double getFinalScore() {
		return finalScore;
	}
	
	public double getRimScore(int molecId) {
		return rimScores[molecId];
	}
	
	public double getCoreScore(int molecId) {
		return coreScores[molecId];
	}
	
	public boolean isScoreWeighted() {
		return this.isScoreWeighted;
	}
	
	/**
	 * Tells whether CRK analysis (ka/ks ratio) is possible for this interface.
	 * It will not be possible when there is no sufficient data from either chain.
	 * @return
	 */
	public boolean canDoCRK() {
		boolean canDoCRK = true;
		if ((this.interf.isFirstProtein() && !getFirstChainEvolContext().canDoCRK()) || 
			(this.interf.isSecondProtein() && !getSecondChainEvolContext().canDoCRK()) ) {
			canDoCRK = false;
		}
		return canDoCRK;
	}
	
	private String getVotersString() {
		String finalStr = "";
		for (CallType callType: CallType.values()) {
			List<Integer> calls = null;
			if (callType==CallType.BIO) {
				calls = bioCalls;
			} else if (callType==CallType.CRYSTAL) {
				calls = xtalCalls;
			} else if (callType==CallType.GRAY) {
				calls = grayCalls;
			} else if (callType==CallType.NO_PREDICTION) {
				calls = noPredictCalls;
			}
			String callsStr = "";
			for (int ind=0;ind<calls.size();ind++) {
				callsStr+=(calls.get(ind)+1);
				if (ind!=calls.size()-1) callsStr+=","; // skip the comma for the last member
			}
			finalStr+=String.format("\t%6s",callsStr);
		}
		return finalStr;
	}
	
	private void printHomologsInfo(PrintStream ps) {
		int numHoms1 = -1;
		int numHoms2 = -1;
		if (scoringType==ScoringType.ENTROPY) {
			if (isProtein(FIRST)) numHoms1 = getFirstChainEvolContext().getNumHomologs();
			if (isProtein(SECOND)) numHoms2 = getSecondChainEvolContext().getNumHomologs();
		} else if (scoringType==ScoringType.KAKS) {
			if (isProtein(FIRST)) numHoms1 = getFirstChainEvolContext().getNumHomologsWithValidCDS();
			if (isProtein(SECOND)) numHoms2 = getSecondChainEvolContext().getNumHomologsWithValidCDS();
		}
		ps.printf("%2d\t%2d\t",numHoms1,numHoms2);
	}
	
	private void printScores(PrintStream ps, CallType call) {
		ps.printf("%5.2f\t%5.2f\t%5.2f",
				this.getCoreScore(FIRST), this.getRimScore(FIRST), this.getCoreScore(FIRST)/this.getRimScore(FIRST));
		ps.print("\t");
		ps.printf("%5.2f\t%5.2f\t%5.2f",
				this.getCoreScore(SECOND), this.getRimScore(SECOND), this.getCoreScore(SECOND)/this.getRimScore(SECOND));
		ps.print("\t");
		// call type, score, voters
		ps.printf("%6s\t%5.2f", call.getName(),	this.getFinalScore());
		ps.print(this.getVotersString());
		ps.print("\t"+callReason);
	}
	
	public void printScoresLine(PrintStream ps) {
		CallType call = getCall();
		getInterface().printRimCoreInfo(ps);
		printHomologsInfo(ps);
		printScores(ps, call);
		ps.println();
		if (!warnings.isEmpty()){
			ps.println("  Warnings: ");
			for (String warning:getWarnings()) {
				ps.println("     "+warning);
			}
		}
	}
	
	public void resetCall() {
		this.call = null;
		this.scoringType = null;
		this.warnings = new ArrayList<String>();
		this.callReason = null;
		this.bioCalls = new ArrayList<Integer>();
		this.xtalCalls = new ArrayList<Integer>();
		this.grayCalls = new ArrayList<Integer>();
		this.noPredictCalls = new ArrayList<Integer>();
		this.rimScores = null;
		this.coreScores = null;
		this.finalScore = -1;
		this.isScoreWeighted = false;
	}
	
}
