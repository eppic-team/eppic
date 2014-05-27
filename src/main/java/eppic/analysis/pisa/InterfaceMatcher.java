package eppic.analysis.pisa;

import java.util.ArrayList;
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
		return ours2theirs.containsKey(ourId);
	}
	
	/**
	 * Returns true if all 'our' interfaces match 1 and only 1 'their' interfaces
	 * and if all 'their' interfaces match 1 and only 1 'our' interfaces
	 * @return
	 */
	public boolean checkAllMatch() {		
		return (checkOursMatch() && checkTheirsMatch());		
	}
	
	/**
	 * Returns true if all 'our' interfaces match 1 and only 1 'their' interfaces
	 * @return
	 */
	public boolean checkOursMatch() {
		// 1) all ours are matched to one of theirs
		for (InterfaceClusterDB ic:ourInterfaceClusters) {
			for (InterfaceDB ourI:ic.getInterfaces()) {
				if (! ours2theirs.containsKey(ourI.getInterfaceId())) {
					return false;
				}
			}
		}
		return true;
	}

	public List<InterfaceDB> getOursNotMatching() {
		List<InterfaceDB> nonmatching = new ArrayList<InterfaceDB>();
		// 1) all ours are matched to one of theirs
		for (InterfaceClusterDB ic:ourInterfaceClusters) {
			for (InterfaceDB ourI:ic.getInterfaces()) {
				if (! ours2theirs.containsKey(ourI.getInterfaceId())) {
					nonmatching.add(ourI);
				}
			}
		}
		return nonmatching;
	}
	
	/**
	 * Returns true if all 'their' interfaces match 1 and only 1 'our' interfaces
	 * @return
	 */
	public boolean checkTheirsMatch() {
		// 2) all theirs are matched to one of ours	
		for (SimpleInterface theirI: theirInterfaces) {
			if (! theirs2ours.containsKey(theirI.getId())) {
				return false;
			}
		} 

		return true;
	}
	
	public List<SimpleInterface> getTheirsNotMatching() {
		List<SimpleInterface> nonmatching = new ArrayList<SimpleInterface>();
		// 2) all theirs are matched to one of ours	
		for (SimpleInterface theirI: theirInterfaces) {
			if (! theirs2ours.containsKey(theirI.getId())) {
				nonmatching.add(theirI);
			}
		} 

		return nonmatching;
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
		// Following my understanding, it should be T02*T01_inv as indicated above, but that didn't work 
		// and for some reason inverting the order in mul does work. Most likely my understanding is 
		// wrong, but need to check this better at some point
		theirTransf12.mul(theirTransf1inv, theirTransf2);

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
