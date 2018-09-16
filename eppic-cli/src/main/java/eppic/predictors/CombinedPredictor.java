package eppic.predictors;

import java.util.ArrayList;
import java.util.List;

import org.biojava.nbio.structure.contact.AtomContact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.DataModelAdaptor;
import eppic.EppicParams;
import eppic.InterfaceEvolContext;
import eppic.commons.util.CallType;

/**
 * The CombinedPredictor takes as input all other predictors (scores) and
 * outputs the probability of the interface being BIO, P(BIO|scores), calculated
 * with a logistic regression classifier.
 * <p>
 * The model is P(BIO|scores) = 1 / (1 + exp(-x)), where x =
 * {@link EppicParams#LOGIT_INTERSECT} +
 * {@link EppicParams#LOGIT_GM_COEFFICIENT} * gm +
 * {@link EppicParams#LOGIT_CS_COEFFICIENT} * cs
 * <p>
 * The NOPRED value (most uncertain score) for cs score is
 * {@link EppicParams#CS_NOPRED}.
 * <p>
 * The confidence is set to be the probability of the final call to be true (BIO
 * or XTAL).
 * 
 * @author Jose Duarte
 * @author Aleix Lafita
 *
 */
public class CombinedPredictor implements InterfaceTypePredictor {

	private static final Logger LOGGER = LoggerFactory.getLogger(CombinedPredictor.class);
	
	private String callReason;
	private List<String> warnings;

	private InterfaceEvolContext iec;
	
	private InterfaceTypePredictor gp;
	private InterfaceTypePredictor ecrp;
	private InterfaceTypePredictor ecsp;
	
	private CallType call;
	
	private double probability = 0.5;
	private double confidence = 0.5;
	
	public CombinedPredictor(InterfaceEvolContext iec, 
			GeometryPredictor gp, EvolCoreRimPredictor ecrp, EvolCoreSurfacePredictor ecsp) {
		this.iec=iec;
		this.gp=gp;
		this.ecrp=ecrp;
		this.ecsp=ecsp;
		this.warnings = new ArrayList<String>();
		
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
	public double getScore() {
		return probability;
	}
	
	@Override
	public double getScore1() {
		return SCORE_UNASSIGNED;
	}
	
	@Override
	public double getScore2() {
		return SCORE_UNASSIGNED;
	}
	
	@Override
	public double getConfidence() {
		return confidence;
	}
	
	@Override
	public void computeScores() {
			
		checkInterface();

		if (!InterfaceEvolContext.isProtein(iec.getInterface(), InterfaceEvolContext.FIRST) &&
			!InterfaceEvolContext.isProtein(iec.getInterface(), InterfaceEvolContext.SECOND)  ) {
			LOGGER.info("Interface {} is not protein in either side, can't score it",iec.getInterface().getId());
			callReason = "Both sides are not protein, can't score";
			call = CallType.NO_PREDICTION;
			probability = 0.5;
			return;
		}
		
		// STRATEGY: if the probability is over 0.5 call BIO, XTAL otherwise
		probability = calcProbability();
		
		// 1) BIO call
		if (probability > 0.5) {
			callReason = String.format(
					"Probability of the interface being biologically relevant is %.2f.",
					probability);
			call = CallType.BIO;
		} 
		// 2) XTAL call
		else if (probability <= 0.5) {
			callReason = String.format(
					"Probability of the interface being biologically relevant is %.2f.",
					probability);
			call = CallType.CRYSTAL;
		}
		
		calcConfidence();
		
	}
	
	private boolean checkInterface() {
		
		// first we gather any possible wild-type disulfide bridges present
		// This is more of a geom feature but as we need to check whether it's wild-type or artifactual it needs to be here
		List<AtomContact> wildTypeDisulfides = new ArrayList<AtomContact>();
		List<AtomContact> engineeredDisulfides = new ArrayList<AtomContact>();
		List<AtomContact> disulfides = getDisulfidePairs();
		if (!disulfides.isEmpty()) {
			// we can only check whether they are not engineered if we have query matches for both sides
			if (iec.getChainEvolContext(InterfaceEvolContext.FIRST).hasQueryMatch() && iec.getChainEvolContext(InterfaceEvolContext.SECOND).hasQueryMatch()) {				
				for (AtomContact pair:disulfides) {	
					if (iec.isReferenceMismatch(pair.getPair().getFirst().getGroup(),InterfaceEvolContext.FIRST) || 
						iec.isReferenceMismatch(pair.getPair().getSecond().getGroup(),InterfaceEvolContext.SECOND)) {
						engineeredDisulfides.add(pair);
					} else {
						wildTypeDisulfides.add(pair);
					}
				}
			} else { // we can't tell whether they are engineered or not, we simply warn they are present
				String msg = disulfides.size()+" disulfide bridges present, can't determine whether they are wild-type or not.";
				msg += " Between CYS residues: ";
				msg += getPairInteractionsString(disulfides);
				warnings.add(msg);
			}
		}
		// disulfides: they are only warnings
		if (!engineeredDisulfides.isEmpty()) {
			String msg = engineeredDisulfides.size()+" engineered disulfide bridges present.";
			msg += " Between CYS residues: ";
			msg += getPairInteractionsString(engineeredDisulfides);
			warnings.add(msg);
		}
		
		if (!wildTypeDisulfides.isEmpty()) {
			String msg = wildTypeDisulfides.size()+" wild-type disulfide bridges present.";
			msg += " Between CYS residues: ";
			msg += getPairInteractionsString(wildTypeDisulfides);			
			warnings.add(msg);
		}

		
		return true; // for the moment there's no conditions to reject a score
	}
	
	/**
	 * The confidence of the call, as the probability that the call is the true one.
	 * The same as the probability for BIO, 1-p for CRYSTAL.
	 */
	private void calcConfidence() {
		
		switch (call) {
		case BIO: 
			confidence = probability;
			break;
		case CRYSTAL: 
			confidence = 1 - probability;
			break;
		case NO_PREDICTION:
			confidence = 0.5;
		
		}
		
	}
	
	@SuppressWarnings("unused")
	private int[] countCalls() {
		int[] counts = new int[3]; // biocalls, xtalcalls, nopredcalls
		if (gp.getCall()==CallType.BIO) counts[0]++;
		else if (gp.getCall()==CallType.CRYSTAL) counts[1]++;
		else if (gp.getCall()==CallType.NO_PREDICTION) counts[2]++; // this can't happen in principle, there's always a geom prediction

		if (ecrp.getCall()==CallType.BIO) counts[0]++;
		else if (ecrp.getCall()==CallType.CRYSTAL) counts[1]++;
		else if (ecrp.getCall()==CallType.NO_PREDICTION) counts[2]++;

		if (ecsp.getCall()==CallType.BIO) counts[0]++;
		else if (ecsp.getCall()==CallType.CRYSTAL) counts[1]++;
		else if (ecsp.getCall()==CallType.NO_PREDICTION) counts[2]++;

		return counts;
	}
	
	/**
	 * Calculate the probability of the interface being BIO with the logisitic
	 * function, and accounting for the NOPRED special cases.
	 */
	private double calcProbability() {
		
		if (ecsp.getCall() == CallType.NO_PREDICTION) {
			// The NOPRED is due to the lack of homologs
			if (gp.getScore() > 0) {
				return 1 / (1 + Math.exp(-(EppicParams.LOGIT_INTERSECT + 
						EppicParams.LOGIT_GM_COEFFICIENT * gp.getScore() + 
						EppicParams.LOGIT_CS_COEFFICIENT * EppicParams.LOGIT_CS_NOPRED)));
			} 
			// The NOPRED is due to the lack of core residues
			else {
				return 0;
			}
		} else {
			return 1 / (1 + Math.exp(-(EppicParams.LOGIT_INTERSECT + 
					EppicParams.LOGIT_GM_COEFFICIENT * gp.getScore() + 
					EppicParams.LOGIT_CS_COEFFICIENT *  ecsp.getScore())));
		}
	}
	
	private String getPairInteractionsString(List<AtomContact> pairs) {
		String msg = "";
		
		for (int i=0;i<pairs.size();i++) {
			if (i<pairs.size()-1) {
				msg += getPairInteractionString(pairs.get(i))+", ";
			} else {
				msg += getPairInteractionString(pairs.get(i));
			}
		}
		return msg;
	}	
	
	private String getPairInteractionString(AtomContact pair) {
		String firstResSer = null;
		String secondResSer = null;
		firstResSer = pair.getPair().getFirst().getGroup().getResidueNumber().toString();	
		secondResSer = pair.getPair().getSecond().getGroup().getResidueNumber().toString();
		
		return
		pair.getPair().getFirst().getGroup().getChainId()+"-"+
		firstResSer+" and "+
		pair.getPair().getSecond().getGroup().getChainId()+"-"+
		secondResSer;
		
	}
	
	private List<AtomContact> getDisulfidePairs() {
		List<AtomContact> list = new ArrayList<AtomContact>();
		for (AtomContact contact:iec.getInterface().getContacts()) {
			if (DataModelAdaptor.isDisulfideInteraction(contact)) 
				list.add(contact);
		}
		return list;
	}
	
}
