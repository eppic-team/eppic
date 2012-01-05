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
import crk.InterfaceEvolContext;
import crk.ScoringType;

public class EvolInterfZMemberPredictor implements InterfaceTypePredictor {
	
	private static final double  MIN_INTERF_FOR_RES_NOT_IN_INTERFACES = 500;
	private static final double  NUM_RESIDUES_NOT_IN_INTERFACES_TOLERANCE = 1.20; // we require 20% more residues in surface than required sample size
	private static final int     NUM_SAMPLES_SCORE_DIST = 100;
	
	private static final Log LOGGER = LogFactory.getLog(EvolInterfZMemberPredictor.class);
	
	private InterfaceEvolContext iec;
	
	private CallType call;
	private String callReason;
	private List<String> warnings;

	private double zScore; // cache of the last run scoreEntropy/scoreKaKs 
	
	private ScoringType scoringType; // the type of the last scoring run (either kaks or entropy)
	
	private int molecId;

	private double coreScore;
	private double mean;
	private double sd;
	private double zScoreCutoff;
	
	private double bsaToAsaCutoff;
	
	public EvolInterfZMemberPredictor(InterfaceEvolContext iec, int molecId) {
		this.iec = iec;
		this.warnings = new ArrayList<String>();
		this.molecId = molecId;
	}
	
	private boolean canDoEntropyScoring() {
		return iec.getChainEvolContext(molecId).hasQueryMatch();
	}
	
	@Override
	public CallType getCall() {
		
		int memberSerial = molecId+1;
		
		iec.getInterface().calcRimAndCore(bsaToAsaCutoff);
		InterfaceRimCore rimCore = iec.getInterface().getRimCore(molecId);
		
		int countsUnrelCoreRes = -1;
		if (canDoEntropyScoring()) {
			countsUnrelCoreRes = getUnreliableCoreRes().size();
		}

		if (!canDoEntropyScoring()) {
			call = CallType.NO_PREDICTION;
			callReason = memberSerial+": no evol z-scores calculation could be performed (no uniprot query match)";
		}
		else if (!iec.hasEnoughHomologs(molecId)) {
			call = CallType.NO_PREDICTION;
			LOGGER.info("Interface "+iec.getInterface().getId()+", member "+memberSerial+" calls NOPRED because there are not enough homologs to calculate evolutionary scores");
			callReason = memberSerial+": there are only "+iec.getChainEvolContext(molecId).getNumHomologs()+
					" homologs to calculate evolutionary scores (at least "+iec.getMinNumSeqs()+" required)";
		}
		else if (rimCore.getCoreSize()<CRKParams.MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE) {
			call = CallType.NO_PREDICTION;
			callReason = memberSerial+": not enough core residues to calculate evolutionary score (at least "+CRKParams.MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE+" needed)";
		} 
		else if (iec.getNumResiduesNotInInterfaces(molecId, MIN_INTERF_FOR_RES_NOT_IN_INTERFACES)<rimCore.getCoreSize()*NUM_RESIDUES_NOT_IN_INTERFACES_TOLERANCE) {
			call = CallType.NO_PREDICTION;
			callReason = memberSerial+": not enough residues in protein surface belonging to no interface, can't calculate the surface score distribution";
		}
		else if (((double)countsUnrelCoreRes/(double)rimCore.getCoreSize())>CRKParams.MAX_ALLOWED_UNREL_RES) {
			call = CallType.NO_PREDICTION;
			LOGGER.info("Interface "+iec.getInterface().getId()+", member "+memberSerial+" calls NOPRED because there are not enough reliable core residues ("+
					countsUnrelCoreRes+" unreliable residues out of "+rimCore.getCoreSize()+" residues in core)");
			callReason = memberSerial+": there are not enough reliable core residues: "+
					countsUnrelCoreRes+" unreliable out of "+rimCore.getCoreSize()+" in core";
		}
		else {
			if (zScore<zScoreCutoff) {
				call = CallType.BIO;
				callReason = memberSerial+": score "+
						String.format("%4.2f",getScore())+" is below BIO cutoff ("+String.format("%4.2f", zScoreCutoff)+")";
			} else if (zScore>zScoreCutoff) {
				call = CallType.CRYSTAL;
				callReason = memberSerial+": score "+
						String.format("%4.2f",zScore)+" is above XTAL cutoff ("+String.format("%4.2f", zScoreCutoff)+")";
			} else if (Double.isNaN(zScore)) {
				call = CallType.NO_PREDICTION;
				callReason = memberSerial+": score is NaN";
			} else {
				// note: this is useless, just kept here as a placeholder in case we want to introduce gray zone
				call = CallType.GRAY;
				callReason = memberSerial+": score "+String.format("%4.2f",zScore)+" falls in gray area ("+
				String.format("%4.2f", zScoreCutoff)+" - "+String.format("%4.2f", zScoreCutoff)+")"; 
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
		scoringType = ScoringType.ENTROPY;
	}

	/**
	 * Calculates the ka/ks score for this interface member.
	 * Subsequently use {@link #getCall()} and {@link #getScore()} to get the call and score
	 */
	public void scoreKaKs() {
		scoreInterfaceMember(ScoringType.KAKS);
		scoringType = ScoringType.KAKS;
	}
	
	public ScoringType getScoringType() {
		return this.scoringType;
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
	public double getScore() {
		return zScore;
	}
	
	private double scoreInterfaceMember(ScoringType scoType) {
		if (!canDoEntropyScoring()) {
			zScore = Double.NaN;
			return zScore;
		}
		
		iec.getInterface().calcRimAndCore(bsaToAsaCutoff);
		InterfaceRimCore rimCore = iec.getInterface().getRimCore(molecId);

		coreScore = iec.calcScore(rimCore.getCoreResidues(),molecId, scoType, false);
		// we need to check, before trying to sample residues in surface for getting 
		// the background distribution, whether there are enough residues at all for sampling
		// it can happen for small proteins that the number of residues in surface is really small (e.g. 3jsd with only 1)
		if (iec.getNumResiduesNotInInterfaces(molecId, MIN_INTERF_FOR_RES_NOT_IN_INTERFACES)<rimCore.getCoreSize()*NUM_RESIDUES_NOT_IN_INTERFACES_TOLERANCE) {
			zScore = Double.NaN;
			return Double.NaN;
		}
		double[] surfScoreDist = iec.getSurfaceScoreDist(molecId, MIN_INTERF_FOR_RES_NOT_IN_INTERFACES, NUM_SAMPLES_SCORE_DIST, rimCore.getCoreSize(), scoType);
		
		if (rimCore.getCoreSize()!=0) {
			LOGGER.info("Interface "+iec.getInterface().getId()+", member "+(molecId+1)+": sampled "+NUM_SAMPLES_SCORE_DIST+" surface evolutionary scores of size "+rimCore.getCoreSize()+": ");
			StringBuffer sb = new StringBuffer();
			for (double sample:surfScoreDist) {
				sb.append(String.format("%4.2f",sample)+" ");
			}
			LOGGER.info(sb.toString());
		}
		
		UnivariateStatistic stat = new Mean();		
		mean = stat.evaluate(surfScoreDist);
		stat = new StandardDeviation();
		sd = stat.evaluate(surfScoreDist);
		
		if (rimCore.getCoreSize()!=0) LOGGER.info("Mean= "+String.format("%5.2f",mean)+", sd= "+String.format("%5.2f",sd));
		
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
	
	public void setZscoreCutoff(double zScoreCutoff) {
		this.zScoreCutoff = zScoreCutoff;
	}
	
	public void setBsaToAsaCutoff(double bsaToAsaCutoff) { 
		this.bsaToAsaCutoff = bsaToAsaCutoff;
	}
	
	/**
	 * Finds all unreliable core residues and returns them in a list.
	 * Unreliable are all residues that:
	 * - if scoringType entropy: the alignment from Uniprot to PDB doesn't match
	 * - if scoringType ka/ks: as entropy + the alignment of CDS translation to protein doesn't match
	 * @return
	 */
	private List<Residue> getUnreliableCoreRes() {
		List<Residue> coreResidues = iec.getInterface().getRimCore(molecId).getCoreResidues();

		List<Residue> unreliableCoreResidues = new ArrayList<Residue>();
		List<Residue> unreliableForPdb = iec.getUnreliableResiduesForPDB(coreResidues, molecId);
		String msg = iec.getUnreliableForPdbWarningMsg(unreliableForPdb);
		if (msg!=null) {
			LOGGER.warn(msg);
			warnings.add(msg);
		}	
		unreliableCoreResidues.addAll(unreliableForPdb);
		

		return unreliableCoreResidues;
	}
	
	public void resetCall() {
		this.call = null;
		this.scoringType = null;
		this.warnings = new ArrayList<String>();
		this.callReason = null;
		this.coreScore = -1;
		this.mean = -1;
		this.sd = -1;
		this.zScore = -1;
	}
	
}
