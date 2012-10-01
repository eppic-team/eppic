package crk.predictors;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import owl.core.structure.Atom;
import owl.core.structure.ChainInterface;

import crk.CRKParams;
import crk.CallType;
import crk.InterfaceEvolContext;
import edu.uci.ics.jung.graph.util.Pair;

public class CombinedCSGeomPredictor implements InterfaceTypePredictor {
	
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
	
	public CombinedCSGeomPredictor(InterfaceEvolContext iec, GeometryPredictor gp, EvolRimCorePredictor rp, EvolInterfZPredictor zp) {
		this.iec=iec;
		this.gp=gp;
		this.rp=rp;
		this.zp=zp;
		this.warnings = new ArrayList<String>();
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
				for (Pair<Atom> pair:iec.getInterface().getAICGraph().getDisulfidePairs()) {		
					msg +=
							pair.getFirst().getParentResidue().getParent().getPdbChainCode()+"-"+
									pair.getFirst().getParentResSerial()+" and "+
									pair.getSecond().getParentResidue().getParent().getPdbChainCode()+"-"+
									pair.getSecond().getParentResSerial();
				}
				warnings.add(msg);
			}
		}
		// engineered disulfides: they are only warnings
		if (!engineeredDisulfides.isEmpty()) {
			String msg = engineeredDisulfides.size()+" engineered disulfide bridges present.";
			msg += " Between CYS residues: ";			
			for (Pair<Atom> pair:engineeredDisulfides) {		
				msg +=
						pair.getFirst().getParentResidue().getParent().getPdbChainCode()+"-"+
								pair.getFirst().getParentResSerial()+" and "+
								pair.getSecond().getParentResidue().getParent().getPdbChainCode()+"-"+
								pair.getSecond().getParentResSerial();
			}
			warnings.add(msg);
		}

		
		// THE CALL
		
		// 0 if peptide, we don't use hard area limits
		// for some cases this works nicely (e.g. 1w9q interface 4)
		boolean useHardLimits = true;
		if (iec.getInterface().getFirstMolecule().getFullLength()<=CRKParams.PEPTIDE_LENGTH_CUTOFF || 
			iec.getInterface().getSecondMolecule().getFullLength()<=CRKParams.PEPTIDE_LENGTH_CUTOFF){
			useHardLimits = false;
		}
		String reasonMsgPrefix = "";
		if (!useHardLimits) reasonMsgPrefix = "Peptide-protein interface, not checking minimum area hard limit. ";
				
		
		// 1st if wild-type disulfide bridges present we call bio
		if (!wildTypeDisulfides.isEmpty()) {
			callReason = wildTypeDisulfides.size()+" wild-type disulfide bridges present.";
			callReason += " Between CYS residues: ";			
			for (Pair<Atom> pair:wildTypeDisulfides) {		
				callReason+=
						pair.getFirst().getParentResidue().getParent().getPdbChainCode()+"-"+
								pair.getFirst().getParentResSerial()+" and "+
								pair.getSecond().getParentResidue().getParent().getPdbChainCode()+"-"+
								pair.getSecond().getParentResSerial();
			}
			call = CallType.BIO;
			votes = 0;
		}
		// 2nd the hard area limits
		else if (useHardLimits && iec.getInterface().getInterfaceArea()<CRKParams.MIN_AREA_BIOCALL) {
			callReason = "Area below hard limit "+String.format("%4.0f", CRKParams.MIN_AREA_BIOCALL);
			call = CallType.CRYSTAL;
			votes = 0;
		} 
		else if (iec.getInterface().getInterfaceArea()>CRKParams.MAX_AREA_XTALCALL) {
			callReason = "Area above hard limit "+String.format("%4.0f", CRKParams.MAX_AREA_XTALCALL);
			call = CallType.BIO;
			votes = 0;
		}
		else {
			// STRATEGY: use core-surface when available, otherwise use geometry
			
			if (zp.getCall()!=CallType.NO_PREDICTION) {
				callReason = reasonMsgPrefix+"Core-surface calls bio)";
				call = zp.getCall();
				if (gp.getCall()==zp.getCall()) votes = 2;
				else votes =1;
			} else {
				callReason = reasonMsgPrefix+"Prediction purely geometrical (no evolutionary prediction could be made): "+gp.getCallReason();
				call = gp.getCall();
				votes = 1;
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
	
	@Override
	public double getScore() {
		return (double)votes;
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
	
}
