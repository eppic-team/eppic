package eppic.predictors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.stat.descriptive.UnivariateStatistic;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.biojava.nbio.structure.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.CallType;
import eppic.EppicParams;
import eppic.InterfaceEvolContext;

public class EvolCoreSurfacePredictor implements InterfaceTypePredictor {

	// we require twice as many residues in surface than required sample size 
	private static final double  NUM_RESIDUES_IN_SURFACE_TOLERANCE = 2.00;
	
	// we used to do only 100, but that was too unstable: we want a predictor as deterministic as possible! 10000 seems a good compromise
	// Tested on DCbio/DCxtal datasets: cs-scores have a mean standard deviation on 10 runs of:
	//   <0.05 using 100, <0.01 using 10000, and <0.005 using 100000
	private static final int     NUM_SAMPLES_SCORE_DIST = 10000; 

	private static final Logger LOGGER = LoggerFactory.getLogger(EvolCoreSurfacePredictor.class);

	private CallType call;
	private String callReason;
	private List<String> warnings;

	private String[] callReasonSides;
	
	private double score; 
	private double score1;
	private double score2;
	
	private boolean check1;
	private boolean check2;
	
	private CallType veto;
	
	private double bsaToAsaCutoff;
	private double minAsaForSurface;

	private InterfaceEvolContext iec;
	
	private double callCutoff;
	
	private int coreSurfaceScoreStrategy;
	

	
	public EvolCoreSurfacePredictor(InterfaceEvolContext iec) {
		this.iec = iec;
		this.warnings = new ArrayList<String>();
		this.callReasonSides = new String[2];
		this.veto = null;
	}
	
	private boolean canDoEntropyScoring(int molecId) {
		return iec.getChainEvolContext(molecId).hasQueryMatch();
	}
	
	@Override
	public CallType getCall() {
		
		return call;
		
	}

	@Override
	public String getCallReason() {
		return callReason;
	}

	@Override
	public List<String> getWarnings() {
		return warnings;
	}

	@Override
	public void computeScores() {
		
		// pre-check and calculating scores
		
		check1 = checkInterfaceSide(InterfaceEvolContext.FIRST);
		check2 = checkInterfaceSide(InterfaceEvolContext.SECOND);
		
		score1 = scoreInterfaceSide(InterfaceEvolContext.FIRST);
		score2 = scoreInterfaceSide(InterfaceEvolContext.SECOND);
		
		// if a veto is present score is set to NaN (i.e. score is not used for decision)
		if (veto!=null) {
			score = Double.NaN;
		}		
		// the final score is the average of both sides if both can be scored or just one side if only one side can be scored		
		else if (check1 && check2) {
			score = (score1+score2)/2.0;
		} else if (check1) {
			score = score1;
		} else if (check2) {
			score = score2;
		} else {
			// if both check1 and check2 are false then we assign NaN
			score = Double.NaN;
		}

		
		// assigning call

		if (veto!=null) {
			call = veto;
			// callReason has to be assigned when assigning veto
		}
		else if (!check1 && !check2) {
			call = CallType.NO_PREDICTION;
			callReason = callReasonSides[0]+"\n"+callReasonSides[1];
		}
		else {


			if (score<=callCutoff) {
				call = CallType.BIO;
			} else if (score>callCutoff) {
				call = CallType.CRYSTAL;
			} else if (Double.isNaN(score)) {
				call = CallType.NO_PREDICTION;
			} 		

			String belowAboveStr;
			if (call==CallType.BIO) belowAboveStr = "below";
			else belowAboveStr = "above";
			String reason = "Score "+String.format("%4.2f", score)+" is "+belowAboveStr+" cutoff ("+String.format("%4.2f", callCutoff)+")";
			if (check1 && !check2) reason += ". Based on side 1 only";
			else if (!check1 && check2) reason += ". Based on side 2 only";

			callReason = reason;
		}
		
		
	}
	
	@Override
	public double getScore() {
		return score;
	}
	
	@Override
	public double getScore1() {
		return score1;
	}
	
	@Override
	public double getScore2() {
		return score2;
	}
	
	@Override
	public double getConfidence() {
		return CONFIDENCE_UNASSIGNED;
	}
	
	private boolean checkInterfaceSide(int molecId) {
	
		String scoreType = "core-surface";
		
		List<Group> cores = null;
		if (molecId == InterfaceEvolContext.FIRST) {
			cores = iec.getInterface().getCoreResidues(bsaToAsaCutoff, minAsaForSurface).getFirst();
		} else if (molecId == InterfaceEvolContext.SECOND) {
			cores = iec.getInterface().getCoreResidues(bsaToAsaCutoff, minAsaForSurface).getSecond();
		}
		
		// we don't do nopreds anymore for cases with unreliable residues, instead 
		// we only warn (done in generateInterfaceWarnings)
		// See issue https://github.com/eppic-team/eppic/issues/34
		generateInterfaceWarnings(cores, molecId);		
		
		int numSurfResidues = iec.getNumSurfaceResidues(molecId, minAsaForSurface); 
		
		int interfaceId = iec.getInterface().getId();
		int memberSerial = molecId + 1;

		if (!InterfaceEvolContext.isProtein(iec.getInterface(), molecId)) {
			LOGGER.info("Interface "+interfaceId+", member "+memberSerial+": can't calculate "+scoreType+" score because it is not a protein");
			callReasonSides[molecId] = "Side "+ memberSerial+" is not a protein";
			return false;
		}
		if (!canDoEntropyScoring(molecId)) {
			LOGGER.info("Interface "+interfaceId+", member "+memberSerial+": can't calculate "+scoreType+" score because it has no UniProt reference");
			callReasonSides[molecId] = "Side "+memberSerial+" has no UniProt reference";
			return false;
		}
		if (!iec.hasEnoughHomologs(molecId)) {
			LOGGER.info("Interface "+interfaceId+", member "+memberSerial+": can't calculate "+scoreType+" score because there are not enough homologs");
			callReasonSides[molecId] = "Side "+memberSerial+" has only "+iec.getChainEvolContext(molecId).getNumHomologs()+
					" homologs (at least "+iec.getMinNumSeqs()+" required)";
			return false;
		} 
		if (cores.size()<EppicParams.MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE) {

			// a special condition for core size, we don't want that if one side has too few cores, 
			// then the prediction is based only on the other side. We veto the whole interface scoring in this case
			callReason = "Not enough core residues (in at least 1 side) to calculate "+scoreType+
					" score. At least "+EppicParams.MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE+" needed";
			veto = CallType.NO_PREDICTION;
			return false;
		}		
		if (numSurfResidues<cores.size()*NUM_RESIDUES_IN_SURFACE_TOLERANCE) {
			callReasonSides[molecId] = "Side "+memberSerial+" has not enough residues in protein surface, can't calculate "+scoreType+" score";
			return false;
		}		
		
		return true;
	}
	
	/**
	 * Generates warnings (to LOGGER and member variable) for given side of interface.
	 * @param cores
	 * @param molecId
	 * @return an array of size 2 with counts of unreliable core (index 0) and surface (index 1) residues
	 */
	private int[] generateInterfaceWarnings(List<Group> cores, int molecId) {
		
		int countUnrelCoreRes = -1;
		int countUnrelSurfaceRes = -1;
		
		if (canDoEntropyScoring(molecId)) {
			List<Group> unreliableCoreRes = iec.getReferenceMismatchResidues(cores, molecId);
			countUnrelCoreRes = unreliableCoreRes.size();
			String msg = iec.getReferenceMismatchWarningMsg(unreliableCoreRes,"core");
			if (msg!=null) {
				LOGGER.info(msg);
				warnings.add(msg);
			}			
			
			List<Group> unreliableSurfaceRes = new ArrayList<Group>();
			unreliableSurfaceRes = iec.getUnreliableSurfaceRes(molecId, minAsaForSurface);
			countUnrelSurfaceRes = unreliableSurfaceRes.size();
			
			msg = iec.getReferenceMismatchWarningMsg(unreliableSurfaceRes,"surface");
			if (msg!=null) {
				LOGGER.info(msg);
				warnings.add(msg);
			}
		}
		
		int[] unrelRes = {countUnrelCoreRes, countUnrelSurfaceRes};
		return unrelRes;
	}

	private double scoreInterfaceSide(int molecId) {
		if (!canDoEntropyScoring(molecId)) {
			return Double.NaN;
		}

		double mean = Double.NaN;
		double sd = Double.NaN;
		
		List<Group> cores = null;
		if (molecId == InterfaceEvolContext.FIRST) {
			cores = iec.getInterface().getCoreResidues(bsaToAsaCutoff, minAsaForSurface).getFirst();
		} else if (molecId == InterfaceEvolContext.SECOND) {
			cores = iec.getInterface().getCoreResidues(bsaToAsaCutoff, minAsaForSurface).getSecond();
		}

		if (cores.size()==0) {
			return Double.NaN;
		}

		double coreScore = iec.calcScore(cores, molecId);
		
		double[] surfScoreDist = null;
		
		// STRATEGY: straight z-scores approach: we sample from the whole surface

		// we need to check, before trying to sample residues in surface for getting 
		// the background distribution, whether there are enough residues at all for sampling
		// it can happen for small proteins that the number of residues in surface is really small 
		int numSurfRes = iec.getNumSurfaceResidues(molecId, minAsaForSurface);

		LOGGER.info("Interface "+iec.getInterface().getId()+", member "+(molecId+1)+":");
		LOGGER.info("Residues on surface: "+numSurfRes);			
		if (numSurfRes<cores.size()*NUM_RESIDUES_IN_SURFACE_TOLERANCE) {
			LOGGER.info("There are only "+numSurfRes+
					" residues in surface. Can't do core-surface scoring for interface "+iec.getInterface().getId()+", member "+(molecId+1));
			return Double.NaN;
		}
		surfScoreDist = iec.getSurfaceScoreDist(molecId, NUM_SAMPLES_SCORE_DIST, cores.size(), minAsaForSurface);		



		
		UnivariateStatistic stat = new Mean();		
		mean = stat.evaluate(surfScoreDist);
		stat = new StandardDeviation();
		sd = stat.evaluate(surfScoreDist);


		LOGGER.info("Sampled "+NUM_SAMPLES_SCORE_DIST+" surface evolutionary scores of size "+cores.size()+": "+
				"mean= "+String.format("%5.2f",mean)+", sd= "+String.format("%5.2f",sd));
		
		if (Double.isNaN(mean)) {
			int countNaNs = 0;
			for (int i=0;i<surfScoreDist.length;i++) {
				if (Double.isNaN(surfScoreDist[i])) countNaNs++;
			}
			LOGGER.info("The samples contained "+countNaNs+" NaNs"); 
		}
		
		// logging the actual samples averages
		StringBuilder sb = new StringBuilder();
		sb.append("Samples averages: ");
		for (double sample:surfScoreDist) {
			sb.append(String.format("%4.2f",sample)+" ");
		}
		LOGGER.debug(sb.toString());


		double zScore = Double.NaN;
		
		if (sd!=0) {
			zScore = (coreScore-mean)/sd;
		} else {
			if ((coreScore-mean)>0) {
				zScore = EppicParams.SCORERATIO_INFINITY_VALUE;
			} else if ((coreScore-mean)<0) {
				zScore = -EppicParams.SCORERATIO_INFINITY_VALUE;
			} else {
				zScore = Double.NaN;
			}
		}
		return zScore;
	}
	
	public void setCallCutoff(double callCutoff) {
		this.callCutoff = callCutoff;
	}
	
	public void setBsaToAsaCutoff(double bsaToAsaCutoff, double minAsaForSurface) {
		this.bsaToAsaCutoff = bsaToAsaCutoff;
		this.minAsaForSurface = minAsaForSurface;
	}
	
	public void setCoreSurfaceScoreStrategy(int coreSurfaceScoreStrategy) {
		this.coreSurfaceScoreStrategy = coreSurfaceScoreStrategy;
	}
	
	public int getCoreSurfaceScoreStrategy() {
		return coreSurfaceScoreStrategy;
	}
	
}
