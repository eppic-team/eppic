package eppic.predictors;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import owl.core.structure.Atom;
import owl.core.structure.ChainInterface;
import edu.uci.ics.jung.graph.util.Pair;
import eppic.EppicParams;
import eppic.CallType;
import eppic.InterfaceEvolContext;

public class CombinedPredictor implements InterfaceTypePredictor {
	
	protected static final int FIRST  = 0;
	protected static final int SECOND = 1;
	
	private String callReason;
	private List<String> warnings;

	private InterfaceEvolContext iec;
	private GeometryPredictor gp;
	private EvolRimCorePredictor rp;
	private EvolInterfZPredictor zp;
	
	private CallType call;
	
	private int votes;
	
	private boolean usePdbResSer;
	
	public CombinedPredictor(InterfaceEvolContext iec, GeometryPredictor gp, EvolRimCorePredictor rp, EvolInterfZPredictor zp) {
		this.iec=iec;
		this.gp=gp;
		this.rp=rp;
		this.zp=zp;
		this.warnings = new ArrayList<String>();
	}
	
	public void setUsePdbResSer(boolean usePdbResSer) {
		this.usePdbResSer = usePdbResSer;
	}
	
	@Override
	public CallType getCall() {
		
		// before making the call we gather any possible wild-type disulfide bridges present
		// This is more of a geom prediction but as we need to check whether it's wild-type or artifactual it needs to be here
		List<Pair<Atom>> wildTypeDisulfides = new ArrayList<Pair<Atom>>();
		List<Pair<Atom>> engineeredDisulfides = new ArrayList<Pair<Atom>>();
		if (iec.getInterface().getAICGraph().hasDisulfideBridges()) {
			// we can only check whether they are not engineered if we have query matches for both sides
			if (iec.getChainEvolContext(FIRST).hasQueryMatch() && iec.getChainEvolContext(SECOND).hasQueryMatch()) {				
				for (Pair<Atom> pair:iec.getInterface().getAICGraph().getDisulfidePairs()) {	
					if (iec.isReferenceMismatch(pair.getFirst().getParentResidue(),FIRST) || 
						iec.isReferenceMismatch(pair.getSecond().getParentResidue(),SECOND)) {
						engineeredDisulfides.add(pair);
					} else {
						wildTypeDisulfides.add(pair);
					}
				}
			} else { // we can't tell whether they are engineered or not, we simply warn they are present
				String msg = iec.getInterface().getAICGraph().getDisulfidePairs().size()+" disulfide bridges present, can't determine whether they are wild-type or not.";
				msg += " Between CYS residues: ";
				msg += getPairInteractionsString(iec.getInterface().getAICGraph().getDisulfidePairs());
				warnings.add(msg);
			}
		}
		// engineered disulfides: they are only warnings
		if (!engineeredDisulfides.isEmpty()) {
			String msg = engineeredDisulfides.size()+" engineered disulfide bridges present.";
			msg += " Between CYS residues: ";
			msg += getPairInteractionsString(engineeredDisulfides);
			warnings.add(msg);
		}

		
		// THE CALL
		
		// 0 if peptide, we don't use hard area limits
		// for some cases this works nicely (e.g. 1w9q interface 4)
		boolean useHardLimits = true;
		if (iec.getInterface().getFirstMolecule().getFullLength()<=EppicParams.PEPTIDE_LENGTH_CUTOFF || 
			iec.getInterface().getSecondMolecule().getFullLength()<=EppicParams.PEPTIDE_LENGTH_CUTOFF){
			useHardLimits = false;
		}
		String reasonMsgPrefix = "";
		if (!useHardLimits) reasonMsgPrefix = "Peptide-protein interface, not checking minimum area hard limit. ";
				
		
		// 1st if wild-type disulfide bridges present we call bio
		if (!wildTypeDisulfides.isEmpty()) {
			callReason = wildTypeDisulfides.size()+" wild-type disulfide bridges present.";
			callReason += " Between CYS residues: ";
			callReason += getPairInteractionsString(wildTypeDisulfides);
			call = CallType.BIO;
			votes = 0;
		}
		// 2nd the hard area limits
		else if (useHardLimits && iec.getInterface().getInterfaceArea()<EppicParams.MIN_AREA_BIOCALL) {
			callReason = "Area below hard limit "+String.format("%4.0f", EppicParams.MIN_AREA_BIOCALL);
			call = CallType.CRYSTAL;
			votes = 0;
		} 
		else if (iec.getInterface().getInterfaceArea()>EppicParams.MAX_AREA_XTALCALL) {
			callReason = "Area above hard limit "+String.format("%4.0f", EppicParams.MAX_AREA_XTALCALL);
			call = CallType.BIO;
			votes = 0;
		}
		else {
			// STRATEGY 1: consensus, when no evolution take geometry, when no consensus take evol
			int[] counts = countCalls();
			// 1) 2 bio calls
			if (counts[0]>=2) {
				callReason = reasonMsgPrefix+"BIO consensus ("+counts[0]+" votes)";
				call = CallType.BIO;
				votes = counts[0];
			} 
			// 2) 2 xtal calls
			else if (counts[1]>=2) {
				callReason = reasonMsgPrefix+"XTAL consensus ("+counts[1]+" votes)";
				call = CallType.CRYSTAL;
				votes = counts[1];
			}
			// 3) 2 nopreds (necessarily from the evol methods): we take geometry as the call
			else if (counts[2]==2) {
				callReason = reasonMsgPrefix+"Prediction purely geometrical (no evolutionary prediction could be made): "+gp.getCallReason();
				call = gp.getCall();
				votes = 1;
			}
			// 4) 1 nopred (an evol method), 1 xtal, 1 bio
			else {
				// sub-strategy a) take geometry call
				//callReason ="No consensus. Z-score "+zp.getCall().getName()+", core/rim "+rp.getCall().getName()+". Taking geometrical call as final: "+gp.getCallReason();
				//call = gp.getCall();
				// sub-strategy b) take evol call
				InterfaceTypePredictor validPred = null;
				if (rp.getCall()!=CallType.NO_PREDICTION) validPred = rp;
				else validPred = zp;
				callReason = reasonMsgPrefix+"No consensus. Core-surface "+zp.getCall().getName()+", core-rim "+rp.getCall().getName()+". Taking evolutionary call as final: "+validPred.getCallReason();
				call = validPred.getCall();
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
//				else validPred = zp;
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

	private int[] countCalls() {
		int[] counts = new int[3]; // biocalls, xtalcalls, nopredcalls
		if (gp.getCall()==CallType.BIO) counts[0]++;
		else if (gp.getCall()==CallType.CRYSTAL) counts[1]++;
		else if (gp.getCall()==CallType.NO_PREDICTION) counts[2]++; // this can't happen in principle, there's always a geom prediction

		if (rp.getCall()==CallType.BIO) counts[0]++;
		else if (rp.getCall()==CallType.CRYSTAL) counts[1]++;
		else if (rp.getCall()==CallType.NO_PREDICTION) counts[2]++;

		if (zp.getCall()==CallType.BIO) counts[0]++;
		else if (zp.getCall()==CallType.CRYSTAL) counts[1]++;
		else if (zp.getCall()==CallType.NO_PREDICTION) counts[2]++;

		return counts;
	}
	
	public static void printScoringHeaders(PrintStream ps) {
		ps.printf("%15s\t%6s\t","interface","area");
		ps.printf("%6s\t%6s\t%6s\t%6s\t%6s","geom","c-r","c-s","call","reason");
		ps.println();
		
	}
	
	public void printScoresLine(PrintStream ps) {
		getCall();// in case it's not calculated yet
		ChainInterface interf = iec.getInterface();
		ps.printf("%15s\t%6.1f\t",
				interf.getId()+"("+interf.getFirstMolecule().getPdbChainCode()+"+"+interf.getSecondMolecule().getPdbChainCode()+")",
				interf.getInterfaceArea());
		ps.printf("%6s\t%6s\t%6s\t%6s\t%s", gp.getCall().getName(),rp.getCall().getName(),zp.getCall().getName(),call.getName(),getCallReason());
		
		ps.println();
		if (!warnings.isEmpty()){
			ps.println("  Warnings: ");
			for (String warning:getWarnings()) {
				ps.println("     "+warning);
			}
		}
	}
	
	private String getPairInteractionsString(List<Pair<Atom>> pairs) {
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
	
	private String getPairInteractionString(Pair<Atom> pair) {
		String firstResSer = null;
		String secondResSer = null;
		if (usePdbResSer) {
			firstResSer = pair.getFirst().getParentResidue().getPdbSerial();	
			secondResSer = pair.getSecond().getParentResidue().getPdbSerial();
		} else {
			firstResSer = ""+pair.getFirst().getParentResidue().getSerial();
			secondResSer = ""+pair.getSecond().getParentResidue().getSerial();
		}
		
		return
		pair.getFirst().getParentResidue().getParent().getPdbChainCode()+"-"+
		firstResSer+" and "+
		pair.getSecond().getParentResidue().getParent().getPdbChainCode()+"-"+
		secondResSer;
		
	}
	
}
