package eppic.predictors;

import java.util.List;

import org.biojava.bio.structure.contact.StructureInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.CallType;
import eppic.EppicParams;
import eppic.InterfaceEvolContextList;

public class CombinedClusterPredictor implements InterfaceTypePredictor {

	private static final Logger LOGGER = LoggerFactory.getLogger(CombinedClusterPredictor.class);
	
	private InterfaceEvolContextList iecl;
	
	private InterfaceCluster ic;
	private InterfaceTypePredictor gcp;
	private InterfaceTypePredictor ecrcp;
	private InterfaceTypePredictor ecscp;
	
	private CallType call;
	private String callReason;
	
	private int votes;
	
	private double confidence;
	
	private CallType veto;
	
	public CombinedClusterPredictor(InterfaceCluster interfaceCluster,
			InterfaceEvolContextList iecl,
			GeometryClusterPredictor gcp,
			EvolCoreRimClusterPredictor ecrcp,
			EvolCoreSurfaceClusterPredictor ecscp) {
		
		this.iecl = iecl;
		this.ic = interfaceCluster;
		this.gcp = gcp;
		this.ecrcp = ecrcp;
		this.ecscp = ecscp;
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
				call = gcp.getCall();
				votes = 1;
			}
			// 4) 1 nopred (an evol method), 1 xtal, 1 bio
			else {
				// take evol call
				if (ecrcp.getCall()!=CallType.NO_PREDICTION) call = ecrcp.getCall();
				else if (ecscp.getCall()!=CallType.NO_PREDICTION) call = ecscp.getCall();
				else System.err.println("Warning! both core-surface and core-rim called nopred. Something went wrong in vote counts");
				
				callReason = "No consensus. Taking evolutionary call as final";
				votes = 1;
			}
		}
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
		return null;
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
	
	private boolean checkInterface() {

		// veto from hard area limits

		// if peptide, we don't use minimum hard area limits
		// for some cases this works nicely (e.g. 1w9q interface 4)		
		boolean useHardLimits = true;
		for (StructureInterface interf:ic.getMembers()) {
			if (interf.getFirstMolecule().getFullLength()<=EppicParams.PEPTIDE_LENGTH_CUTOFF ||
				interf.getSecondMolecule().getFullLength()<=EppicParams.PEPTIDE_LENGTH_CUTOFF) {
				useHardLimits = false;				
			}
			// break after one iteration: all interfaces in the cluster should have the peptide
			break;
		}
		if (useHardLimits==false) {
			LOGGER.info("Interface cluster "+ic.getId()+": peptide-protein interface, not checking minimum area hard limit. ");
		}
		
		if (useHardLimits && ic.getMeanArea()<EppicParams.MIN_AREA_BIOCALL) {
			
			callReason = "Area below hard limit "+String.format("%4.0f", EppicParams.MIN_AREA_BIOCALL);
			
			veto = CallType.CRYSTAL;
		} 
		else if (ic.getMeanArea()>EppicParams.MAX_AREA_XTALCALL) {
			
			callReason = "Area above hard limit "+String.format("%4.0f", EppicParams.MAX_AREA_XTALCALL);
			
			veto = CallType.BIO;
		}
		
		
		return true; // for the moment there's no conditions to reject a score
	}
	
	private int[] countCalls() {
		int[] counts = new int[3]; // biocalls, xtalcalls, nopredcalls
		if (gcp.getCall()==CallType.BIO) counts[0]++;
		else if (gcp.getCall()==CallType.CRYSTAL) counts[1]++;
		else if (gcp.getCall()==CallType.NO_PREDICTION) counts[2]++; // this can't happen in principle, there's always a geom prediction

		if (ecrcp.getCall()==CallType.BIO) counts[0]++;
		else if (ecrcp.getCall()==CallType.CRYSTAL) counts[1]++;
		else if (ecrcp.getCall()==CallType.NO_PREDICTION) counts[2]++;

		if (ecscp.getCall()==CallType.BIO) counts[0]++;
		else if (ecscp.getCall()==CallType.CRYSTAL) counts[1]++;
		else if (ecscp.getCall()==CallType.NO_PREDICTION) counts[2]++;

		return counts;
	}
	
	private void calcConfidence() {
		
		// we simply take the first member of the cluster and check if there enough homologs for it, other members should have same composition
		
		String firstPdbChainCode = ic.getMembers().get(0).getFirstMolecule().getPdbChainCode();
		String secondPdbChainCode = ic.getMembers().get(0).getSecondMolecule().getPdbChainCode();
		
		int firstNumHomologs = iecl.getChainEvolContext(firstPdbChainCode).getNumHomologs();
		int secondNumHomologs = iecl.getChainEvolContext(secondPdbChainCode).getNumHomologs();
		
		
		if ( firstNumHomologs<iecl.getMinNumSeqs() && secondNumHomologs<iecl.getMinNumSeqs()) {
			confidence = CONFIDENCE_LOW;
		} else if (firstNumHomologs<iecl.getMinNumSeqs()) {
			confidence = CONFIDENCE_MEDIUM;
		} else if (secondNumHomologs<iecl.getMinNumSeqs()) {
			confidence = CONFIDENCE_MEDIUM;
		} else {
			confidence = CONFIDENCE_HIGH;
		}
	}

}
