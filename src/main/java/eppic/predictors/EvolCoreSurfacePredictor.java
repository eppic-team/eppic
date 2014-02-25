package eppic.predictors;

import java.util.ArrayList;
import java.util.List;

import owl.core.structure.InterfaceRimCore;
import owl.core.structure.Residue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.descriptive.UnivariateStatistic;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;

import eppic.EppicParams;
import eppic.CallType;
import eppic.InterfaceEvolContext;

public class EvolCoreSurfacePredictor implements InterfaceTypePredictor {

	private static final double  MIN_INTERF_FOR_RES_NOT_IN_INTERFACES = 500;
	private static final double  NUM_RESIDUES_NOT_IN_INTERFACES_TOLERANCE = 1.20; // we require 20% more residues in surface than required sample size
	
	// we used to do only 100, but that was too unstable: we want a predictor as deterministic as possible! 10000 seems a good compromise
	// Tested on DCbio/DCxtal datasets: cs-scores have a mean standard deviation on 10 runs of:
	//   <0.05 using 100, <0.01 using 10000, and <0.005 using 100000
	private static final int     NUM_SAMPLES_SCORE_DIST = 10000; 

	
	private static final Log LOGGER = LogFactory.getLog(EvolCoreSurfacePredictor.class);

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
		
		iec.getInterface().calcRimAndCore(bsaToAsaCutoff, minAsaForSurface);

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

			String reason = "Score "+String.format("%4.2f", score)+" is below cutoff ("+String.format("%4.2f", callCutoff)+")";
			if (check1 && !check2) reason += ". Based on side 1 only";
			else if (!check1 && check2) reason += ". Based on side 2 only";

			callReason = reason;

			if (score<=callCutoff) {
				call = CallType.BIO;
			} else if (score>callCutoff) {
				call = CallType.CRYSTAL;
			} else if (Double.isNaN(score)) {
				call = CallType.NO_PREDICTION;
			} 		

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
	
	private boolean checkInterfaceSide(int molecId) {
	
		String scoreType = "core-surface";
		
		InterfaceRimCore rimCore = iec.getInterface().getRimCore(molecId);
		
		int[] unrelRes = generateInterfaceWarnings(molecId);		
		
		int numSurfResidues = iec.getNumResiduesNotInInterfaces(molecId, MIN_INTERF_FOR_RES_NOT_IN_INTERFACES, minAsaForSurface);
		
		int interfaceId = iec.getInterface().getId();
		int memberSerial = molecId + 1;

		if (!iec.isProtein(molecId)) {
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
		if (rimCore.getCoreSize()<EppicParams.MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE) {

			// a special condition for core size, we don't want that if one side has too few cores, 
			// then the prediction is based only on the other side. We veto the whole interface scoring in this case
			callReason = "Not enough core residues (in at least 1 side) to calculate "+scoreType+
					" score. At least "+EppicParams.MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE+" needed";
			veto = CallType.NO_PREDICTION;
			return false;
		}
		if (numSurfResidues<rimCore.getCoreSize()*NUM_RESIDUES_NOT_IN_INTERFACES_TOLERANCE) {
			callReasonSides[molecId] = "Side "+memberSerial+" has not enough residues in protein surface belonging to no interface, can't calculate "+scoreType+" score";
			return false;
		}
		if (((double)unrelRes[0]/(double)rimCore.getCoreSize())>EppicParams.MAX_ALLOWED_UNREL_RES) {
			LOGGER.info("Interface "+interfaceId+", member "+memberSerial+
					": there are not enough reliable core residues to calculate "+scoreType+" score ("+
					unrelRes[0]+" unreliable residues out of "+rimCore.getCoreSize()+" residues in core)");
			callReasonSides[molecId] = "Side "+memberSerial+" has not enough reliable core residues: "+
					unrelRes[0]+" unreliable out of "+rimCore.getCoreSize()+" in core";
			return false;
		}
		if (((double)unrelRes[1]/(double)numSurfResidues)>EppicParams.MAX_ALLOWED_UNREL_RES) {
			callReasonSides[molecId] = "Side "+memberSerial+" has not enough reliable residues in protein surface belonging to no interface: " +
					unrelRes[1]+" unreliable residues out of "+
					numSurfResidues+" residues in surface";			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Generates warnings (to LOGGER and member variable) for given side of interface.
	 * @param molecId
	 * @return an array of size 2 with counts of unreliable core (index 0) and surface (index 1) residues
	 */
	private int[] generateInterfaceWarnings(int molecId) {
		
		int countUnrelCoreRes = -1;
		int countUnrelNotInInterfacesRes = -1;
		
		if (canDoEntropyScoring(molecId)) {
			List<Residue> unreliableCoreRes = iec.getUnreliableCoreRes(molecId);
			countUnrelCoreRes = unreliableCoreRes.size();
			String msg = iec.getReferenceMismatchWarningMsg(unreliableCoreRes,"core");
			if (msg!=null) {
				LOGGER.warn(msg);
				warnings.add(msg);
			}			
			
			List<Residue> unreliableSurfaceRes = iec.getUnreliableNotInInterfacesRes(molecId, MIN_INTERF_FOR_RES_NOT_IN_INTERFACES, minAsaForSurface);
			countUnrelNotInInterfacesRes = unreliableSurfaceRes.size();
			msg = iec.getReferenceMismatchWarningMsg(unreliableSurfaceRes,"surface");
			if (msg!=null) {
				LOGGER.warn(msg);
				warnings.add(msg);
			}
		}
		
		int[] unrelRes = {countUnrelCoreRes, countUnrelNotInInterfacesRes};
		return unrelRes;
	}

	private double scoreInterfaceSide(int molecId) {
		if (!canDoEntropyScoring(molecId)) {
			return Double.NaN;
		}

		double mean = Double.NaN;
		double sd = Double.NaN;
		
		InterfaceRimCore rimCore = iec.getInterface().getRimCore(molecId);

		if (rimCore.getCoreSize()==0) {
			return Double.NaN;
		}

		double coreScore = iec.calcScore(rimCore.getCoreResidues(), molecId, false);
		// we need to check, before trying to sample residues in surface for getting 
		// the background distribution, whether there are enough residues at all for sampling
		// it can happen for small proteins that the number of residues in surface is really small (e.g. 3jsd with only 1)
		int numSurfResNotInInterfaces = iec.getNumResiduesNotInInterfaces(molecId, MIN_INTERF_FOR_RES_NOT_IN_INTERFACES, minAsaForSurface);

		LOGGER.info("Interface "+iec.getInterface().getId()+", member "+(molecId+1)+":");
		LOGGER.info("Residues on surface not belonging to any crystal interface (above "+
				String.format("%3.0f",MIN_INTERF_FOR_RES_NOT_IN_INTERFACES)+" A2): "+numSurfResNotInInterfaces);			
		if (numSurfResNotInInterfaces<rimCore.getCoreSize()*NUM_RESIDUES_NOT_IN_INTERFACES_TOLERANCE) {
			LOGGER.info("There are only "+numSurfResNotInInterfaces+
					" residues in surface not belonging to any crystal interface (above "+String.format("%3.0f",MIN_INTERF_FOR_RES_NOT_IN_INTERFACES)+
					" A2). Can't do core-surface scoring for interface "+iec.getInterface().getId()+", member "+(molecId+1));
			return Double.NaN;
		}
		double[] surfScoreDist = iec.getSurfaceScoreDist(molecId, MIN_INTERF_FOR_RES_NOT_IN_INTERFACES, NUM_SAMPLES_SCORE_DIST, rimCore.getCoreSize(), minAsaForSurface);		

		UnivariateStatistic stat = new Mean();		
		mean = stat.evaluate(surfScoreDist);
		stat = new StandardDeviation();
		sd = stat.evaluate(surfScoreDist);


		LOGGER.info("Sampled "+NUM_SAMPLES_SCORE_DIST+" surface evolutionary scores of size "+rimCore.getCoreSize()+": "+
				"mean= "+String.format("%5.2f",mean)+", sd= "+String.format("%5.2f",sd));
		
		if (Double.isNaN(mean)) {
			int countNaNs = 0;
			for (int i=0;i<surfScoreDist.length;i++) {
				if (Double.isNaN(surfScoreDist[i])) countNaNs++;
			}
			LOGGER.info("The samples contained "+countNaNs+" NaNs"); 
		}
		
		// logging the actual samples averages (now commented out)
		//StringBuffer sb = new StringBuffer();
		//for (double sample:surfScoreDist) {
		//	sb.append(String.format("%4.2f",sample)+" ");
		//}
		//LOGGER.info(sb.toString());


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
	
}
