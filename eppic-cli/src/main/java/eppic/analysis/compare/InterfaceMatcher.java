package eppic.analysis.compare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.vecmath.Matrix4d;

import org.biojava.nbio.structure.xtal.SpaceGroup;

import eppic.model.db.InterfaceClusterDB;
import eppic.model.db.InterfaceDB;

public class InterfaceMatcher {

	private static final Pattern CHAIN_ID_REGEX = Pattern.compile("^(\\w+)\\d+n$");
	
	
	private List<InterfaceClusterDB> ourInterfaceClusters;
	private List<SimpleInterface> theirInterfaces;
	
	private Map<Integer,SimpleInterface> ours2theirs;
	private Map<Integer,InterfaceDB> theirs2ours;
	
	public InterfaceMatcher(List<InterfaceClusterDB> ourInterfaceClusters, List<SimpleInterface> theirInterfaces) {
		
		this.ourInterfaceClusters = ourInterfaceClusters;
		this.theirInterfaces = theirInterfaces;
		
		matchThem();
	}
	
	public int getNumOurInterfaces() {
		int count = 0;
		for (InterfaceClusterDB ic:ourInterfaceClusters) {
			count+=ic.getInterfaces().size();
		}
		return count;
	}
	
	public int getNumTheirInterfaces() {
		return this.theirInterfaces.size();
	}
	
	/**
	 * Returns the sorted set of 'our' interface ids
	 * @return
	 */
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
	
	public boolean oursMatch(int ourId) {
		return ours2theirs.containsKey(ourId);
	}
	
	public boolean theirsMatch(int theirId) {
		return theirs2ours.containsKey(theirId);
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

	/**
	 * Returns a list of all interfaces from 'our' list that don't match one of 'theirs'
	 * @return
	 */
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
	
	/**
	 * Returns a list of all interfaces from 'their' list that don't match one of 'ours'
	 * @return
	 */
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
	
	/**
	 * Returns true if and only if the mapping 'ours'<->'theirs' is one-to-one,
	 * false if one-to-many, many-to-one or many-to-many
	 * @return
	 */
	public boolean checkOneToOneMapping() {
		// a necessary condition is that both maps have the same size
		if (ours2theirs.size()!=theirs2ours.size()) return false;
		
		// and sufficient when neither side map to many on the other side
		Set<Integer> theirIds = new HashSet<Integer>();
		for (int ourId:ours2theirs.keySet()) {
			if (!theirIds.add(ours2theirs.get(ourId).getId())) {
				// the mapped element is repeated: not a one-to-one mapping
				return false;
			}
		}
		return true;
		
	}
	
	private void matchThem() {
		
		ours2theirs = new TreeMap<Integer, SimpleInterface>();
		theirs2ours = new TreeMap<Integer, InterfaceDB>();
				
		for (InterfaceClusterDB ic:ourInterfaceClusters) {
			for (InterfaceDB ourI:ic.getInterfaces()) {
				
				
				for(SimpleInterface theirI:theirInterfaces){
					
					if (areMatching(theirI,ourI)) {
						ours2theirs.put(ourI.getInterfaceId(), theirI);
						theirs2ours.put(theirI.getId(), ourI);
					}
					
				}
			}
		}
		
	}

	private String ncsChainToStandardChain(String chainId){
		if (chainId.length()>2 && chainId.endsWith("n")) {
			Matcher m = CHAIN_ID_REGEX.matcher(chainId);
			if (m.matches()) {
				return m.group(1);
			}
		}
		return chainId;
	}
	
	private boolean areMatching(SimpleInterface theirI, InterfaceDB ourI) {
		String ourChain1 = ourI.getChain1();
		String ourChain2 = ourI.getChain2();

		if (ourI.getInterfaceCluster().getPdbInfo().isNcsOpsPresent()) {
			// we have special chain ids for NCS case, e.g. A1n
			// see https://github.com/eppic-team/eppic/issues/141 , this is probably not solving the problem, but it helps
			ourChain1 = ncsChainToStandardChain(ourChain1);
			ourChain2 = ncsChainToStandardChain(ourChain2);
		}
		
		String theirChain1 = theirI.getChain1();
		String theirChain2 = theirI.getChain2();
		
		boolean invertedChains;
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
		
		// 'ours' always has 1st chain with identity, thus the transf12 coincides with transf2
		// of course we could also put this through findTransf12() as it would return the same matrix but there's no point
		Matrix4d ourTransf12 = SpaceGroup.getMatrixFromAlgebraic(ourI.getOperator());

		Matrix4d theirTransf1 = theirI.getOperator1();
		Matrix4d theirTransf2 = theirI.getOperator2();
		Matrix4d theirTransf12 = findTransf12(theirTransf1, theirTransf2); 

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
	
	public static Matrix4d findTransf12(Matrix4d transf1, Matrix4d transf2) {
		
		// if first chain is not on identity, we first need to find the transf12
		// T12 = T02 * T01_inv
		Matrix4d transf1inv = new Matrix4d();
		transf1inv.invert(transf1);
		Matrix4d transf12 = new Matrix4d();
		// Following my understanding, it should be T02*T01_inv as indicated above, but that didn't work 
		// and for some reason inverting the order in mul does work. Most likely my understanding is 
		// wrong, but need to check this better at some point
		// A possible explanation for this is that vecmath treats transform() as a pre-multiplication
		// rather than a post-multiplication as I was assuming, i.e. x1 = x0 * T01 instead of x1 = T01 * x0,
		// in that case then it is true that: T12 = T01_inv * T02, which would explain the expression below
		transf12.mul(transf1inv, transf2);
		
		return transf12;
	}
}
