package eppic.analysis.pisa;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.vecmath.Matrix4d;

import owl.core.structure.SpaceGroup;
import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceDB;

public class InterfaceMatcher {
	
	public static final int MIN_CRYSTAL_TRANSLATION_FOR_WARNING = 4;
	
	private List<InterfaceClusterDB> ourInterfaceClusters;
	private List<SimpleInterface> theirInterfaces;
	
	private Map<Integer,SimpleInterface> ours2theirs;
	private Map<Integer,InterfaceDB> theirs2ours;
	
	public InterfaceMatcher(List<InterfaceClusterDB> ourInterfaceClusters, List<SimpleInterface> theirInterfaces)
	 throws OneToManyMatchException {
		
		this.ourInterfaceClusters = ourInterfaceClusters;
		this.theirInterfaces = theirInterfaces;
		
		matchThem();
	}
	
	public Collection<Integer> getOurIds() {
		Collection<Integer> ourIds = new TreeSet<Integer>();
		for (InterfaceClusterDB ic:ourInterfaceClusters) {
			for (InterfaceDB ourI:ic.getInterfaces()) {
				ourIds.add(ourI.getInterfaceId());
			}
		}
		return ourIds;
	}
	
	public SimpleInterface getTheirs(int ourId) {
		return ours2theirs.get(ourId);
	}
	
	public InterfaceDB getOurs(int theirId) {
		return theirs2ours.get(theirId);
	}
	
	public boolean hasMatch(int ourId) {
		return (ours2theirs.get(ourId) != null);
	}
	
	public boolean checkAllMatch(boolean verbose) {
		
		// 1) all ours are matched to one of theirs
		for (InterfaceClusterDB ic:ourInterfaceClusters) {
			for (InterfaceDB ourI:ic.getInterfaces()) {
				SimpleInterface theirI = ours2theirs.get(ourI.getInterfaceId());
				if (theirI==null) {
					// if not we check if the contacts are too far away and warn that it's impossible
					// to match their interfaces in these cases (PISA only calculates up to 3 neighbors)
					int xTrans = ourI.getXtalTrans_x();
					int yTrans = ourI.getXtalTrans_y();
					int zTrans = ourI.getXtalTrans_z();
					int maxTrans = Math.max(Math.max(xTrans, yTrans), zTrans);
					
					if(maxTrans >= MIN_CRYSTAL_TRANSLATION_FOR_WARNING){
						if (verbose)
							System.err.println("Warning: our interface id "+ourI.getInterfaceId()+" has a maximum translation of "+maxTrans+" cells. No matching 'their' interface should be expected");
					}
					else {
						if (verbose)
							System.err.println("Warning: no matching 'their' interface found for 'our' interface id "+ourI.getInterfaceId());
					}
					
					return false;
				}
			}
		}
		
		// 2) all theirs are matched to one of ours	
		for (SimpleInterface theirI: theirInterfaces) {
			if (!ours2theirs.values().contains(theirI)) {
				if (verbose)
					System.err.println("Warning: no matching 'our' interface found for 'their' interface id "+theirI.getId());
				return false;
			}
		} 
		
		return true;
	}
	
	private void matchThem() throws OneToManyMatchException {
		
		ours2theirs = new TreeMap<Integer, SimpleInterface>();
		theirs2ours = new TreeMap<Integer, InterfaceDB>();
				
		for (InterfaceClusterDB ic:ourInterfaceClusters) {
			for (InterfaceDB ourI:ic.getInterfaces()) {
				
				SimpleInterface theirI = getMatchingInterface(ourI);
				
				if (theirI!=null) {
				
					ours2theirs.put(ourI.getInterfaceId(), theirI);
					theirs2ours.put(theirI.getId(), ourI);

				} 
			}
		}
		
	}
	
	/**
	 * Returns the corresponding "theirs" matching interface to "ours" interface, or null if no match found
	 * @param ourI
	 * @return
	 * @throws OneToManyMatchException
	 */
	private SimpleInterface getMatchingInterface(InterfaceDB ourI) throws OneToManyMatchException {

		SimpleInterface match = null;
		
		for(SimpleInterface theirI:theirInterfaces){
			
			if (areMatching(theirI,ourI)) {
				if (match!=null) {
					throw new OneToManyMatchException("More than one matching interface (id "+theirI.getId()+", "+match.getId()+
							") found for interface id "+ourI.getInterfaceId());
					
				} else {
					match = theirI;
				}
			}
			
		}

		return match;
	}
	
	private boolean areMatching(SimpleInterface theirI, InterfaceDB ourI) {
		String ourChain1 = ourI.getChain1();
		String ourChain2 = ourI.getChain2();
		
		String theirChain1 = theirI.getChain1();
		String theirChain2 = theirI.getChain2();
		
		boolean invertedChains = false;
		if (theirChain1.equals(ourChain1) && theirChain2.equals(ourChain2)) {
			invertedChains = false;
		}
		else if (theirChain1.equals(ourChain2) && theirChain2.equals(ourChain1)) {
			invertedChains = true;
		}
		else {
			// chains don't match: this can't be the same interface
			return false;
		}
		
		// eppic always has 1st chain with identity, thus the transf12 coincides with transf2
		Matrix4d ourTransf12 = SpaceGroup.getMatrixFromAlgebraic(ourI.getOperator());

		Matrix4d theirTransf1 = theirI.getOperator1();
		Matrix4d theirTransf2 = theirI.getOperator2();
		// in case they don't have the first chain on identity, we first find the transf12
		// T12 = T02 * T01_inv
		Matrix4d theirTransf1inv = new Matrix4d();
		theirTransf1inv.invert(theirTransf1);
		Matrix4d theirTransf12 = new Matrix4d();
		theirTransf12.mul(theirTransf2, theirTransf1inv);

		if (invertedChains) {
			theirTransf12.invert();
		}

		
		// now we can compare the two transf12
		// a) first direct
		if (ourTransf12.epsilonEquals(theirTransf12, 0.00001)) {
			return true;
		}
		// b) then we need to check the inverse if the two chains are the same
		if (ourChain1.equals(ourChain2)) {
			theirTransf12.invert();
			if (ourTransf12.epsilonEquals(theirTransf12, 0.00001)) {
				//System.err.println("Warning: inverse transform matches (EPPIC interface id "+eppicI.getInterfaceId()+", PISA interface id "+pisaI.getId()+", pdb "+pdbCode+")");
				return true;
			}
		}
		return false;
	}
}
