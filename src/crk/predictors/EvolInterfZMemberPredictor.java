package crk.predictors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.descriptive.UnivariateStatistic;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;

import owl.core.structure.InterfaceRimCore;
import owl.core.structure.Residue;

import crk.CRKParams;
import crk.CallType;
import crk.ScoringType;

public class EvolInterfZMemberPredictor implements InterfaceTypePredictor {
	
	private static final double  MIN_INTERF_FOR_RES_NOT_IN_INTERFACES = 500;
	private static final double  NUM_RESIDUES_NOT_IN_INTERFACES_TOLERANCE = 1.20; // we require 20% more residues in surface than required sample size
	
	// we used to do only 100, but that was too unstable: we want a predictor as deterministic as possible! 10000 seems a good compromise
	// Tested on DCbio/DCxtal datasets: cs-scores have a mean standard deviation on 10 runs of:
	//   <0.05 using 100, <0.01 using 10000, and <0.005 using 100000
	private static final int     NUM_SAMPLES_SCORE_DIST = 10000; 
	
	private static final Log LOGGER = LogFactory.getLog(EvolInterfZMemberPredictor.class);
	
	private EvolInterfZPredictor parent;
	
	private CallType call;
	private String callReason;
	private List<String> warnings;

	private double zScore; // cache of the last run scoreEntropy/scoreKaKs 
	
	//private ScoringType scoringType; // the type of the last scoring run (either kaks or entropy)
	
	private int molecId;

	private double coreScore;
	private double mean;
	private double sd;
	
	public EvolInterfZMemberPredictor(EvolInterfZPredictor parent, int molecId) {
		this.parent = parent;
		this.warnings = new ArrayList<String>();
		this.molecId = molecId;
	}
	
	@Override
	public CallType getCall() {
		
		int memberSerial = molecId+1;
		
		parent.getInterfaceEvolContext().getInterface().calcRimAndCore(parent.getBsaToAsaCutoff(), parent.getMinAsaForSurface());
		InterfaceRimCore rimCore = parent.getInterfaceEvolContext().getInterface().getRimCore(molecId);
		
		int countsUnrelCoreRes = -1;
		if (parent.canDoEntropyScoring(molecId)) {
			List<Residue> unreliableCoreRes = parent.getInterfaceEvolContext().getUnreliableCoreRes(molecId);
			countsUnrelCoreRes = unreliableCoreRes.size();
			String msg = parent.getInterfaceEvolContext().getReferenceMismatchWarningMsg(unreliableCoreRes,"core");
			if (msg!=null) {
				LOGGER.warn(msg);
				warnings.add(msg);
			}			
		}

		if (!parent.canDoEntropyScoring(molecId)) {
			call = CallType.NO_PREDICTION;
			callReason = memberSerial+": no evol z-scores calculation could be performed (no uniprot query match)";
		}
		else if (!parent.getInterfaceEvolContext().hasEnoughHomologs(molecId)) {
			call = CallType.NO_PREDICTION;
			LOGGER.info("Interface "+parent.getInterfaceEvolContext().getInterface().getId()+", member "+memberSerial+" calls NOPRED because there are not enough homologs to calculate evolutionary scores");
			callReason = memberSerial+": there are only "+parent.getInterfaceEvolContext().getChainEvolContext(molecId).getNumHomologs()+
					" homologs to calculate evolutionary scores (at least "+parent.getInterfaceEvolContext().getMinNumSeqs()+" required)";
		}
		else if (rimCore.getCoreSize()<CRKParams.MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE) {
			call = CallType.NO_PREDICTION;
			callReason = memberSerial+": not enough core residues to calculate evolutionary score (at least "+CRKParams.MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE+" needed)";
		} 
		else if (parent.getInterfaceEvolContext().getNumResiduesNotInInterfaces(molecId, MIN_INTERF_FOR_RES_NOT_IN_INTERFACES, parent.getMinAsaForSurface())<rimCore.getCoreSize()*NUM_RESIDUES_NOT_IN_INTERFACES_TOLERANCE) {
			call = CallType.NO_PREDICTION;
			callReason = memberSerial+": not enough residues in protein surface belonging to no interface, can't calculate the surface score distribution";
		}
		else if (((double)countsUnrelCoreRes/(double)rimCore.getCoreSize())>CRKParams.MAX_ALLOWED_UNREL_RES) {
			call = CallType.NO_PREDICTION;
			LOGGER.info("Interface "+parent.getInterfaceEvolContext().getInterface().getId()+", member "+memberSerial+" calls NOPRED because there are not enough reliable core residues ("+
					countsUnrelCoreRes+" unreliable residues out of "+rimCore.getCoreSize()+" residues in core)");
			callReason = memberSerial+": there are not enough reliable core residues: "+
					countsUnrelCoreRes+" unreliable out of "+rimCore.getCoreSize()+" in core";
		}
		else {
			if (zScore<parent.getCallCutoff()) {
				call = CallType.BIO;
				callReason = memberSerial+": score "+
						String.format("%4.2f",getScore())+" is below BIO cutoff ("+String.format("%4.2f", parent.getCallCutoff())+")";
			} else if (zScore>parent.getCallCutoff()) {
				call = CallType.CRYSTAL;
				callReason = memberSerial+": score "+
						String.format("%4.2f",zScore)+" is above XTAL cutoff ("+String.format("%4.2f", parent.getCallCutoff())+")";
			} else if (Double.isNaN(zScore)) {
				call = CallType.NO_PREDICTION;
				callReason = memberSerial+": score is NaN";
			} else {
				// note: this is useless, just kept here as a placeholder in case we want to introduce gray zone
				call = CallType.GRAY;
				callReason = memberSerial+": score "+String.format("%4.2f",zScore)+" falls in gray area ("+
				String.format("%4.2f", parent.getCallCutoff())+" - "+String.format("%4.2f", parent.getCallCutoff())+")"; 
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
		return warnings;
	}
	
	/**
	 * Calculates the entropy score for this interface member.
	 * Subsequently use {@link #getCall()} and {@link #getScore()} to get the call and score
	 */
	public void scoreEntropy() {
		scoreInterfaceMember(ScoringType.ENTROPY);
		//scoringType = ScoringType.ENTROPY;
	}

	public double getCoreScore() {
		return coreScore;
	}
	
	public double getMean() {
		return mean;
	}
	
	public double getSd() {
		return sd;
	}
	
	/**
	 * Gets the z-score for this interface member
	 * @return
	 */
	@Override
	public double getScore() {
		return zScore;
	}
	
	private double scoreInterfaceMember(ScoringType scoType) {
		if (!parent.canDoEntropyScoring(molecId)) {
			zScore = Double.NaN;
			return zScore;
		}

		mean = Double.NaN;
		sd = Double.NaN;
		
		parent.getInterfaceEvolContext().getInterface().calcRimAndCore(parent.getBsaToAsaCutoff(), parent.getMinAsaForSurface());
		InterfaceRimCore rimCore = parent.getInterfaceEvolContext().getInterface().getRimCore(molecId);

		if (rimCore.getCoreSize()==0) {
			zScore = Double.NaN;
			return Double.NaN;
		}

		coreScore = parent.getInterfaceEvolContext().calcScore(rimCore.getCoreResidues(), molecId, scoType, false);
		// we need to check, before trying to sample residues in surface for getting 
		// the background distribution, whether there are enough residues at all for sampling
		// it can happen for small proteins that the number of residues in surface is really small (e.g. 3jsd with only 1)
		int numSurfResNotInInterfaces = parent.getInterfaceEvolContext().getNumResiduesNotInInterfaces(molecId, MIN_INTERF_FOR_RES_NOT_IN_INTERFACES, parent.getMinAsaForSurface());

		LOGGER.info("Interface "+parent.getInterfaceEvolContext().getInterface().getId()+", member "+(molecId+1)+":");
		LOGGER.info("Residues on surface not belonging to any crystal interface (above "+
				String.format("%3.0f",MIN_INTERF_FOR_RES_NOT_IN_INTERFACES)+" A2): "+numSurfResNotInInterfaces);			
		if (numSurfResNotInInterfaces<rimCore.getCoreSize()*NUM_RESIDUES_NOT_IN_INTERFACES_TOLERANCE) {
			zScore = Double.NaN;
			LOGGER.info("There are only "+numSurfResNotInInterfaces+
					" residues in surface not belonging to any crystal interface (above "+String.format("%3.0f",MIN_INTERF_FOR_RES_NOT_IN_INTERFACES)+
					" A2). Can't do core-surface scoring for interface "+parent.getInterfaceEvolContext().getInterface().getId()+", member "+(molecId+1));
			return Double.NaN;
		}
		double[] surfScoreDist = parent.getInterfaceEvolContext().getSurfaceScoreDist(molecId, MIN_INTERF_FOR_RES_NOT_IN_INTERFACES, NUM_SAMPLES_SCORE_DIST, rimCore.getCoreSize(), scoType, parent.getMinAsaForSurface());



		UnivariateStatistic stat = new Mean();		
		mean = stat.evaluate(surfScoreDist);
		stat = new StandardDeviation();
		sd = stat.evaluate(surfScoreDist);


		LOGGER.info("Sampled "+NUM_SAMPLES_SCORE_DIST+" surface evolutionary scores of size "+rimCore.getCoreSize()+": "+
				"mean= "+String.format("%5.2f",mean)+", sd= "+String.format("%5.2f",sd));
		// logging the actual samples averages (now commented out)
		//StringBuffer sb = new StringBuffer();
		//for (double sample:surfScoreDist) {
		//	sb.append(String.format("%4.2f",sample)+" ");
		//}
		//LOGGER.info(sb.toString());


		
		if (sd!=0) {
			zScore = (coreScore-mean)/sd;
		} else {
			if ((coreScore-mean)>0) {
				zScore = CRKParams.SCORERATIO_INFINITY_VALUE;
			} else if ((coreScore-mean)<0) {
				zScore = -CRKParams.SCORERATIO_INFINITY_VALUE;
			} else {
				zScore = Double.NaN;
			}
		}
		return zScore;
	}
	
	public void resetCall() {
		this.call = null;
		this.warnings = new ArrayList<String>();
		this.callReason = null;
		this.coreScore = -1;
		this.mean = -1;
		this.sd = -1;
		this.zScore = -1;
	}
	
}
