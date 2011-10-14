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
		if (iec.getInterface().getInterfaceArea()<GeometryPredictor.MIN_AREA_BIOCALL) {
			callReason = "Area below hard limit "+String.format("%4.0f", GeometryPredictor.MIN_AREA_BIOCALL);
			call = CallType.CRYSTAL;
		} 
		else if (iec.getInterface().getInterfaceArea()>GeometryPredictor.MAX_AREA_XTALCALL) {
			callReason = "Area above hard limit "+String.format("%4.0f", GeometryPredictor.MAX_AREA_XTALCALL);
			call = CallType.BIO;
		}
		else {
			int[] counts = countCalls();
			if (counts[0]>=2) {
				callReason = "BIO consensus ("+counts[0]+" votes)";
				call = CallType.BIO;
			} 
			else if (counts[1]>=2) {
				callReason = "XTAL consensus ("+counts[1]+" votes)";
				call = CallType.CRYSTAL;
			}
			else if (counts[2]==2) {
				callReason = gp.getCallReason();
				call = gp.getCall();
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

	private int[] countCalls() {
		int[] counts = new int[3]; // biocalls, xtalcalls, nopredcalls
		if (gp.getCall()==CallType.BIO) counts[0]++;
		else if (gp.getCall()==CallType.CRYSTAL) counts[1]++;
		else if (gp.getCall()==CallType.NO_PREDICTION) counts[2]++;

		if (rp.getCall()==CallType.BIO) counts[0]++;
		else if (rp.getCall()==CallType.CRYSTAL) counts[1]++;
		else if (rp.getCall()==CallType.NO_PREDICTION) counts[2]++;

		if (zp.getCall()==CallType.BIO) counts[0]++;
		else if (zp.getCall()==CallType.CRYSTAL) counts[1]++;
		else if (zp.getCall()==CallType.NO_PREDICTION) counts[2]++;

		return counts;
	}
}
