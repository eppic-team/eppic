package eppic.predictors;

import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.contact.AtomContact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.EppicParams;
import eppic.CallType;
import eppic.InterfaceEvolContext;

public class CombinedPredictor implements InterfaceTypePredictor {

	private static final Logger LOGGER = LoggerFactory.getLogger(CombinedPredictor.class);
	
	private String callReason;
	private List<String> warnings;

	private InterfaceEvolContext iec;
	
	private InterfaceTypePredictor gp;
	private InterfaceTypePredictor ecrp;
	private InterfaceTypePredictor ecsp;
	
	private CallType call;
	
	private int votes;
	
	private CallType veto;
	
	private double confidence;
	
	public CombinedPredictor(InterfaceEvolContext iec, 
			GeometryPredictor gp, EvolCoreRimPredictor ecrp, EvolCoreSurfacePredictor ecsp) {
		this.iec=iec;
		this.gp=gp;
		this.ecrp=ecrp;
		this.ecsp=ecsp;
		this.warnings = new ArrayList<String>();
		
		this.veto = null;
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
		return (double)votes;
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
		
		calcConfidence();
		
		if (veto!=null) {
			call = veto;
			votes = -1;
			// callReason has to be assigned when assigning the veto
		} else {
			// STRATEGY 1: consensus, when no evolution take geometry, when no consensus take evol
			int[] counts = countCalls();
			// 1) 2 bio calls
			if (counts[0]>=2) {
				callReason = "BIO consensus ("+counts[0]+" votes)";
				call = CallType.BIO;
				votes = counts[0];
			} 
			// 2) 2 xtal calls
			else if (counts[1]>=2) {
				callReason = "XTAL consensus ("+counts[1]+" votes)";
				call = CallType.CRYSTAL;
				votes = counts[1];
			}
			// 3) 2 nopreds (necessarily from the evol methods): we take geometry as the call
			else if (counts[2]==2) {
				callReason = "Prediction purely geometrical (no evolutionary prediction could be made)";
				call = gp.getCall();
				votes = 1;
			}
			// 4) 1 nopred (an evol method), 1 xtal, 1 bio
			else {
				// take evol call
				if (ecrp.getCall()!=CallType.NO_PREDICTION) call = ecrp.getCall();
				else if (ecsp.getCall()!=CallType.NO_PREDICTION) call = ecsp.getCall();
				else System.err.println("Warning! both core-surface and core-rim called nopred. Something went wrong in vote counts");
				
				callReason = "No consensus. Taking evolutionary call as final";
				votes = 1;
			}
//			// STRATEGY 2: trust more evolution when we can
//			// 1) there is evolutionary prediction from both methods
//			if (rp.getCall()!=CallType.NO_PREDICTION && zp.getCall()!=CallType.NO_PREDICTION) {
//				if (rp.getCall()==zp.getCall()) {
//					call = rp.getCall();
//					callReason = "Consensus of evol predictions";
//				} else {
//					// each evol prediction votes in a different direction, geometry has to give the last word (so in total 2 votes go for it)
//					call = gp.getCall();
//					callReason = "One evolutionary measure and geometry agree";
//				}
//			// 2) there's no evol predictions at all
//			} else if (rp.getCall()==CallType.NO_PREDICTION && zp.getCall()==CallType.NO_PREDICTION) {
//				call = gp.getCall();
//				callReason = "Couldn't do evol predictions. Taking geometry as final prediction";				
//			}
//			// 3) only one of the evol predicts
//			else if (rp.getCall()==CallType.NO_PREDICTION || zp.getCall()==CallType.NO_PREDICTION) {
//				InterfaceTypePredictor validPred = null;
//				if (rp.getCall()!=CallType.NO_PREDICTION) validPred = rp;
//					else validPred = zp;
//						
//				if (gp.getCall()==validPred.getCall()) {
//					call = validPred.getCall();
//					callReason = "One evolutionary could not predict and the other evol measure and geometry agree";
//				} else {
//					// no agreement: we trust evolution
//					call = validPred.getCall();
//					callReason = "Only one evol measure predicts and disagrees with geometry: taking evol prediction";
//				}
//			}
		}
		
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

		// veto from the hard area limits
		
		// if peptide, we don't use minimum hard area limits
		// for some cases this works nicely (e.g. 1w9q interface 4)
		boolean useHardLimits = true;
		if (iec.getInterface().getFirstGroupAsas().size()<=EppicParams.PEPTIDE_LENGTH_CUTOFF || 
			iec.getInterface().getSecondGroupAsas().size()<=EppicParams.PEPTIDE_LENGTH_CUTOFF){
			useHardLimits = false;
			LOGGER.info("Interface "+iec.getInterface().getId()+": peptide-protein interface, not checking minimum area hard limit. ");
		}

		if (useHardLimits && iec.getInterface().getTotalArea()<EppicParams.MIN_AREA_BIOCALL) {
			
			callReason = "Area below hard limit "+String.format("%4.0f", EppicParams.MIN_AREA_BIOCALL);
			
			veto = CallType.CRYSTAL;
		} 
		else if (iec.getInterface().getTotalArea()>EppicParams.MAX_AREA_XTALCALL) {
			
			callReason = "Area above hard limit "+String.format("%4.0f", EppicParams.MAX_AREA_XTALCALL);
			
			veto = CallType.BIO;
		}
		
		
		return true; // for the moment there's no conditions to reject a score
	}
	
	private void calcConfidence() {
		if (!iec.hasEnoughHomologs(InterfaceEvolContext.FIRST) && !iec.hasEnoughHomologs(InterfaceEvolContext.SECOND)) {
			confidence = CONFIDENCE_LOW;
		} else if (!iec.hasEnoughHomologs(InterfaceEvolContext.FIRST)) {
			confidence = CONFIDENCE_MEDIUM;
		} else if (!iec.hasEnoughHomologs(InterfaceEvolContext.SECOND)) {
			confidence = CONFIDENCE_MEDIUM;
		} else {
			confidence = CONFIDENCE_HIGH;
		}
	}
	
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
			if (isDisulfideInteraction(contact)) 
				list.add(contact);
		}
		return list;
	}
	
	private static boolean isDisulfideInteraction(AtomContact contact) {
		Atom atomi = contact.getPair().getFirst();
		Atom atomj = contact.getPair().getSecond();
		if (atomi.getGroup().getPDBName().equals("CYS") &&
			atomj.getGroup().getPDBName().equals("CYS") &&
			atomi.getName().equals("SG") &&
			atomj.getName().equals("SG") &&
			contact.getDistance()<(EppicParams.DISULFIDE_BRIDGE_DIST+EppicParams.DISULFIDE_BRIDGE_DIST_SIGMA) && 
			contact.getDistance()>(EppicParams.DISULFIDE_BRIDGE_DIST-EppicParams.DISULFIDE_BRIDGE_DIST_SIGMA)) {
				return true;
		}
		return false;
	}
	
}
