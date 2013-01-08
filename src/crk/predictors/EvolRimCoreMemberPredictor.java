package crk.predictors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import owl.core.structure.InterfaceRimCore;
import owl.core.structure.Residue;

import crk.CRKParams;
import crk.CallType;
import crk.ScoringType;

public class EvolRimCoreMemberPredictor implements InterfaceTypePredictor {

	private static final Log LOGGER = LogFactory.getLog(EvolRimCoreMemberPredictor.class);
		
	private String callReason;
	private List<String> warnings;

	private EvolRimCorePredictor parent;
	
	private int molecId;
	
	private double coreScore;
	private double rimScore;
	private double scoreRatio;
	
	private CallType call;
	
	public EvolRimCoreMemberPredictor(EvolRimCorePredictor parent, int molecId) {
		this.parent = parent;
		this.molecId = molecId;
		this.warnings = new ArrayList<String>();
	}
	
	@Override
	public CallType getCall() {
		
		int memberSerial = molecId+1;
		
		parent.getInterfaceEvolContext().getInterface().calcRimAndCore(parent.getBsaToAsaCutoff(), parent.getMinAsaForSurface());
		InterfaceRimCore rimCore = parent.getInterfaceEvolContext().getInterface().getRimCore(molecId);
		
		int countsUnrelCoreRes = -1;
		int countsUnrelRimRes = -1;
		if (parent.canDoEntropyScoring(molecId)) {
			List<Residue> unreliableCoreRes = parent.getInterfaceEvolContext().getUnreliableCoreRes(molecId);
			List<Residue> unreliableRimRes = parent.getInterfaceEvolContext().getUnreliableRimRes(molecId);
			countsUnrelCoreRes = unreliableCoreRes.size();
			countsUnrelRimRes = unreliableRimRes.size();
			String msg = parent.getInterfaceEvolContext().getReferenceMismatchWarningMsg(unreliableCoreRes,"core");
			if (msg!=null) {
				LOGGER.warn(msg);
				warnings.add(msg);
			}
			msg = parent.getInterfaceEvolContext().getReferenceMismatchWarningMsg(unreliableRimRes,"rim");
			if (msg!=null) {
				LOGGER.warn(msg);
				warnings.add(msg);
			}
		}
		
		call = null;

		if (!parent.getInterfaceEvolContext().isProtein(molecId)) {
			call = CallType.NO_PREDICTION;
			LOGGER.info("Interface "+parent.getInterfaceEvolContext().getInterface().getId()+", member "+memberSerial+" calls NOPRED because it is not a protein");
			callReason = memberSerial+": is not a protein";
		}
		else if (!parent.canDoEntropyScoring(molecId)) {
			call = CallType.NO_PREDICTION;
			callReason = memberSerial+": could not calculate evolutionary scores (no UniProt reference found for query)";
		}
		else if (!parent.getInterfaceEvolContext().hasEnoughHomologs(molecId)) {
			call = CallType.NO_PREDICTION;
			LOGGER.info("Interface "+parent.getInterfaceEvolContext().getInterface().getId()+", member "+memberSerial+" calls NOPRED because there are not enough homologs to calculate evolutionary scores");
			callReason = memberSerial+": there are only "+parent.getInterfaceEvolContext().getChainEvolContext(molecId).getNumHomologs()+
					" homologs to calculate evolutionary scores (at least "+parent.getInterfaceEvolContext().getMinNumSeqs()+" required)";
		} 
		else if (scoreRatio==-1) {
			// this happens whenever the value wasn't initialized, in practice it will 
			// happen when doing Ka/Ks scoring and for some reason (e.g. no CDS match for query) it can't be done 
			call = CallType.NO_PREDICTION;
			callReason = memberSerial+": could not calculate evolutionary scores";
		}
		else if (rimCore.getCoreSize()<CRKParams.MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE) {
			call = CallType.NO_PREDICTION;
			callReason = memberSerial+": not enough core residues to calculate evolutionary score (at least "+CRKParams.MIN_NUMBER_CORE_RESIDUES_EVOL_SCORE+" needed)";
		}
		else if (((double)countsUnrelCoreRes/(double)rimCore.getCoreSize())>CRKParams.MAX_ALLOWED_UNREL_RES) {
			call = CallType.NO_PREDICTION;
			LOGGER.info("Interface "+parent.getInterfaceEvolContext().getInterface().getId()+", member "+memberSerial+" calls NOPRED because there are not enough reliable core residues ("+
					countsUnrelCoreRes+" unreliable residues out of "+rimCore.getCoreSize()+" residues in core)");
			callReason = memberSerial+": there are not enough reliable core residues: "+
					countsUnrelCoreRes+" unreliable out of "+rimCore.getCoreSize()+" in core";
		}
		else if (((double)countsUnrelRimRes/(double)rimCore.getRimSize())>CRKParams.MAX_ALLOWED_UNREL_RES) {
			call = CallType.NO_PREDICTION;
			LOGGER.info("Interface "+parent.getInterfaceEvolContext().getInterface().getId()+", member "+memberSerial+" calls NOPRED because there are not enough reliable rim residues ("+
					countsUnrelRimRes+" unreliable residues out of "+rimCore.getRimSize()+" residues in rim)");
			callReason = memberSerial+": there are not enough reliable rim residues: "+
					countsUnrelRimRes+" unreliable out of "+rimCore.getRimSize()+" in rim";
		}
		else {
			if (scoreRatio<=parent.getCallCutoff()) {
				call = CallType.BIO;
				callReason = memberSerial+": score "+
						String.format("%4.2f",scoreRatio)+" is below cutoff ("+String.format("%4.2f", parent.getCallCutoff())+")";
			} else if (scoreRatio>parent.getCallCutoff()) {
				call = CallType.CRYSTAL;
				callReason = memberSerial+": score "+
						String.format("%4.2f",scoreRatio)+" is above cutoff ("+String.format("%4.2f", parent.getCallCutoff())+")";
			} else if (Double.isNaN(scoreRatio)) {
				call = CallType.NO_PREDICTION;
				callReason = memberSerial+": score is NaN";
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
	 * Calculates the entropy scores for this interface member.
	 * Subsequently use {@link #getCall()} and {@link #getScore()} to get the call and score
	 * @param weighted
	 */
	public void scoreEntropy(boolean weighted) {
		scoreInterfaceMember(weighted, ScoringType.ENTROPY);
	}
	
	private void scoreInterfaceMember(boolean weighted, ScoringType scoType) {	
		if (!parent.canDoEntropyScoring(molecId)) {
			scoreRatio = Double.NaN;
			return;
		}
		parent.getInterfaceEvolContext().getInterface().calcRimAndCore(parent.getBsaToAsaCutoff(), parent.getMinAsaForSurface());
		InterfaceRimCore rimCore = parent.getInterfaceEvolContext().getInterface().getRimCore(molecId);
		rimScore  = parent.getInterfaceEvolContext().calcScore(rimCore.getRimResidues(), molecId, scoType, weighted);
		coreScore = parent.getInterfaceEvolContext().calcScore(rimCore.getCoreResidues(),molecId, scoType, weighted);
		if (rimScore==0) {
			scoreRatio = CRKParams.SCORERATIO_INFINITY_VALUE;
		} else {
			scoreRatio = coreScore/rimScore;
		}
	}

	/**
	 * Gets the ratio score core over rim
	 * @return
	 */
	@Override
	public double getScore() {
		return scoreRatio;
	}
	
	public double getCoreScore() {
		return coreScore;
	}
	
	public double getRimScore() {
		return rimScore;
	}
	
	public void resetCall() {
		this.call = null;
		//this.scoringType = null;
		this.warnings = new ArrayList<String>();
		this.callReason = null;
		this.coreScore = -1;
		this.rimScore = -1;
		this.scoreRatio = -1;
	}
}
