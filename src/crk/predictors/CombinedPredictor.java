package crk.predictors;

import java.util.List;

import crk.CallType;
import crk.InterfaceEvolContext;

public class CombinedPredictor implements InterfaceTypePredictor {

	private String callReason;
	private List<String> warnings;

	private InterfaceEvolContext iec;
	private GeometryPredictor gp;
	private EvolRimCorePredictor rp;
	private EvolInterfZPredictor zp;
	
	private CallType call;
	
	public CombinedPredictor(InterfaceEvolContext iec, GeometryPredictor gp, EvolRimCorePredictor rp, EvolInterfZPredictor zp) {
		this.iec=iec;
		this.gp=gp;
		this.rp=rp;
		this.zp=zp;
	}
	
	@Override
	public CallType getCall() {
		
		// 1st the hard area limits
		if (iec.getInterface().getInterfaceArea()<GeometryPredictor.MIN_AREA_BIOCALL) {
			callReason = "Area below hard limit "+String.format("%4.0f", GeometryPredictor.MIN_AREA_BIOCALL);
			call = CallType.CRYSTAL;
		} 
		else if (iec.getInterface().getInterfaceArea()>GeometryPredictor.MAX_AREA_XTALCALL) {
			callReason = "Area above hard limit "+String.format("%4.0f", GeometryPredictor.MAX_AREA_XTALCALL);
			call = CallType.BIO;
		}
		else {
			// STRATEGY 1: consensus, when no evolution take geometry, when no consensus take evol
			int[] counts = countCalls();
			// 1) 2 bio calls
			if (counts[0]>=2) {
				callReason = "BIO consensus ("+counts[0]+" votes)";
				call = CallType.BIO;
			} 
			// 2) 2 xtal calls
			else if (counts[1]>=2) {
				callReason = "XTAL consensus ("+counts[1]+" votes)";
				call = CallType.CRYSTAL;
			}
			// 3) 2 nopreds (necessarily from the evol methods): we take geometry as the call
			else if (counts[2]==2) {
				callReason = "Prediction purely geometrical (no evolutionary prediction could be made): "+gp.getCallReason();
				call = gp.getCall();
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
				callReason = "No consensus. Z-score "+zp.getCall().getName()+", core/rim "+rp.getCall().getName()+". Taking evol call as final: "+validPred.getCallReason();
				call = validPred.getCall();
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
}
