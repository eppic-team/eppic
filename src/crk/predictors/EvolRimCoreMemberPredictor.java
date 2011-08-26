package crk.predictors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import owl.core.structure.InterfaceRimCore;

import crk.CallType;
import crk.InterfaceEvolContext;

public class EvolRimCoreMemberPredictor implements InterfaceTypePredictor {

	private static final Log LOGGER = LogFactory.getLog(EvolRimCoreMemberPredictor.class);
	
	private static final double MAX_ALLOWED_UNREL_RES = 0.05; // 5% maximum allowed unreliable residues for core or rim
	
	private String callReason;
	private List<String> warnings;
	
	private EvolRimCorePredictor ercp;
	private int molecId;
	
	public EvolRimCoreMemberPredictor(EvolRimCorePredictor ercp, int molecId) {
		this.ercp = ercp;
		this.molecId = molecId;
		this.warnings = new ArrayList<String>();
	}
	
	@Override
	public CallType getCall() {
		
		int memberSerial = molecId+1;
		InterfaceEvolContext iec = ercp.getInterfaceEvolContext();
		
		InterfaceRimCore rimCore = iec.getRimCore(molecId);
		
		int countsUnrelCoreRes = ercp.getUnreliableCoreRes(molecId, ercp.getScoringType()).size();
		int countsUnrelRimRes = ercp.getUnreliableRimRes(molecId, ercp.getScoringType()).size();
		
		
		double ratio = ercp.getScoreRatio(molecId);
		
		CallType call = null;

		if (!iec.isProtein(molecId)) {
			call = CallType.NO_PREDICTION;
			LOGGER.info("Interface "+iec.getInterface().getId()+", member "+memberSerial+" calls NOPRED because it is not a protein");
			callReason = memberSerial+": is not a protein";
		}
		else if (!iec.hasEnoughHomologs(molecId)) {
			call = CallType.NO_PREDICTION;
			LOGGER.info("Interface "+iec.getInterface().getId()+", member "+memberSerial+" calls NOPRED because there are not enough homologs to calculate evolutionary scores");
			callReason = memberSerial+": there are only "+iec.getChainEvolContext(molecId).getNumHomologs()+
					" homologs to calculate evolutionary scores (at least "+iec.getHomologsCutoff()+" required)";
		}
		else if (((double)countsUnrelCoreRes/(double)rimCore.getCoreSize())>MAX_ALLOWED_UNREL_RES) {
			call = CallType.NO_PREDICTION;
			LOGGER.info("Interface "+iec.getInterface().getId()+", member "+memberSerial+" calls NOPRED because there are not enough reliable core residues ("+
					countsUnrelCoreRes+" unreliable residues out of "+rimCore.getCoreSize()+" residues in core)");
			callReason = memberSerial+": there are not enough reliable core residues: "+
					countsUnrelCoreRes+" unreliable out of "+rimCore.getCoreSize()+" in core";
		}
		else if (((double)countsUnrelRimRes/(double)rimCore.getRimSize())>MAX_ALLOWED_UNREL_RES) {
			call = CallType.NO_PREDICTION;
			LOGGER.info("Interface "+iec.getInterface().getId()+", member "+memberSerial+" calls NOPRED because there are not enough reliable rim residues ("+
					countsUnrelRimRes+" unreliable residues out of "+rimCore.getRimSize()+" residues in rim)");
			callReason = memberSerial+": there are not enough reliable rim residues: "+
					countsUnrelRimRes+" unreliable out of "+rimCore.getRimSize()+" in rim";
		}
		else {
			if (ratio<ercp.getBioCutoff()) {
				call = CallType.BIO;
				callReason = memberSerial+": score "+
						String.format("%4.2f",ratio)+" is below BIO cutoff ("+String.format("%4.2f", ercp.getBioCutoff())+")";
			} else if (ratio>ercp.getXtalCutoff()) {
				call = CallType.CRYSTAL;
				callReason = memberSerial+": score "+
						String.format("%4.2f",ratio)+" is above XTAL cutoff ("+String.format("%4.2f", ercp.getXtalCutoff())+")";
			} else if (Double.isNaN(ratio)) {
				call = CallType.NO_PREDICTION;
				callReason = memberSerial+": score is NaN";
			} else {
				call = CallType.GRAY;
				callReason = memberSerial+": score "+String.format("%4.2f",ratio)+" falls in gray area ("+
				String.format("%4.2f", ercp.getBioCutoff())+" - "+String.format("%4.2f", ercp.getXtalCutoff())+")"; 
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

}
