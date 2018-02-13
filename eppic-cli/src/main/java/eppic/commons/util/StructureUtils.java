package eppic.commons.util;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix4d;

import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.PDBCrystallographicInfo;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureTools;

/**
 * Some needed functions to expand ncs operators taken from biojava 5.0.0-SNAPSHOT
 * When eppic switches from biojava 4.2.1 to 5.0.0 we should remove this and use the biojava functions in 
 * {@link StructureTools}.
 * 
 * @author Jose Duarte
 */
public class StructureUtils {
	/**
	 * Expands the NCS operators in the given Structure adding new chains as needed.
	 * The new chains are assigned ids of the form: original_chain_id+ncs_operator_index+"n"
	 * @param structure
	 */
	public static void expandNcsOps(Structure structure) {
		PDBCrystallographicInfo xtalInfo = structure.getCrystallographicInfo();
		if (xtalInfo ==null) return;
		
		if (xtalInfo.getNcsOperators()==null || xtalInfo.getNcsOperators().length==0) return;
		
		List<Chain> chainsToAdd = new ArrayList<>();
		int i = 0;
		for (Matrix4d m:xtalInfo.getNcsOperators()) {
			i++;
			
			for (Chain c:structure.getChains()) {
				Chain clonedChain = (Chain)c.clone();
				String newChainId = c.getChainID()+i+"n";
				clonedChain.setChainID(newChainId);
				clonedChain.setInternalChainID(newChainId);
				setChainIdsInResidueNumbers(clonedChain, newChainId);
				Calc.transform(clonedChain, m);
				chainsToAdd.add(clonedChain);
				c.getEntityInfo().addChain(clonedChain);
			}
		}
		
		for (Chain c:chainsToAdd) {
			structure.addChain(c);
		}
	}
	
	/**
	 * Auxiliary method to reset chain ids of residue numbers in a chain.
	 * Used when cloning chains and resetting their ids: one needs to take care of 
	 * resetting the ids within residue numbers too.
	 * @param c
	 * @param newChainId
	 */
	private static void setChainIdsInResidueNumbers(Chain c, String newChainId) {
		for (Group g:c.getAtomGroups()) {
			g.setResidueNumber(newChainId, g.getResidueNumber().getSeqNum(), g.getResidueNumber().getInsCode());
		}
		for (Group g:c.getSeqResGroups()) {
			if (g.getResidueNumber()==null) continue;
			g.setResidueNumber(newChainId, g.getResidueNumber().getSeqNum(), g.getResidueNumber().getInsCode());
		}
	}
}
