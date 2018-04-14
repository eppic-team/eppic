package eppic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.random.RandomDataImpl;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.predictors.EvolCoreRimPredictor;
import eppic.predictors.EvolCoreSurfacePredictor;

public class InterfaceEvolContext implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(InterfaceEvolContext.class);
	
	// the 2 sides of the interface
	public static final int FIRST  = 0;
	public static final int SECOND = 1;

	private InterfaceEvolContextList parent;
	
	private StructureInterface interf;

	private EvolCoreRimPredictor evolCoreRimPredictor;
	private EvolCoreSurfacePredictor evolCoreSurfacePredictor;
	
	public InterfaceEvolContext(StructureInterface interf, InterfaceEvolContextList parent) {
		this.interf = interf;
		this.parent = parent;
	}

	public StructureInterface getInterface() {
		return interf;
	}
	
	public ChainEvolContext getFirstChainEvolContext() {
		return parent.getChainEvolContext(interf.getMoleculeIds().getFirst());
	}
	
	public ChainEvolContext getSecondChainEvolContext() {
		return parent.getChainEvolContext(interf.getMoleculeIds().getSecond());
	}
	
	public ChainEvolContext getChainEvolContext(int molecId) {
		if (molecId==FIRST) return getFirstChainEvolContext();
		if (molecId==SECOND) return getSecondChainEvolContext();
		return null;
	}
	
	public EvolCoreRimPredictor getEvolCoreRimPredictor() {
		return evolCoreRimPredictor;
	}
	
	public void setEvolCoreRimPredictor(EvolCoreRimPredictor evolCoreRimPredictor) {
		this.evolCoreRimPredictor = evolCoreRimPredictor;
	}
	
	public EvolCoreSurfacePredictor getEvolCoreSurfacePredictor() {
		return evolCoreSurfacePredictor;
	}
	
	public void setEvolCoreSurfacePredictor(EvolCoreSurfacePredictor evolCoreSurfacePredictor) {
		this.evolCoreSurfacePredictor = evolCoreSurfacePredictor;
	}
	
	private Chain getMolecule(int molecId) {
		if (molecId==FIRST) {
			return getInterface().getMolecules().getFirst()[0].getGroup().getChain();
		}
		if (molecId==SECOND) {
			return getInterface().getMolecules().getSecond()[0].getGroup().getChain();
		}
		return null;		
	}
	
	private String getChainId (int molecId) {
		if (molecId==FIRST) {
			return getInterface().getMoleculeIds().getFirst();
		}
		if (molecId==SECOND) {
			return getInterface().getMoleculeIds().getSecond();
		}
		return null;	
	}
	
	/**
	 * Finds all unreliable residues that belong to the surface and returns them in a list.
	 * Unreliable are all residues for which the alignment from reference UniProt to PDB doesn't match
	 * @param molecId
	 * @param minAsaForSurface
	 * @return
	 */
	public List<Group> getUnreliableSurfaceRes(int molecId, double minAsaForSurface) {
		List<Group> surfResidues = null;
		if (molecId==FIRST) {
			surfResidues = interf.getSurfaceResidues(minAsaForSurface).getFirst();
		} else if (molecId == SECOND) {
			surfResidues = interf.getSurfaceResidues(minAsaForSurface).getSecond();
		}
		return getReferenceMismatchResidues(surfResidues, molecId);
	}
	
	/**
	 * Given a list of residues returns the subset of those that are unreliable 
	 * because of mismatch of PDB sequence to UniProt reference matching (thus indicating
	 * engineered residues).
	 * @param residues
	 * @param molecId
	 * @return
	 */
	public List<Group> getReferenceMismatchResidues(List<Group> residues, int molecId) {
		List<Group> unreliableResidues = new ArrayList<Group>();
		for (Group res:residues){
			if (isReferenceMismatch(res, molecId)) {
				unreliableResidues.add(res);
			}
		}
		return unreliableResidues;
	}
	
	/**
	 * Given a residue and the molecId to which it belongs returns true if the position is 
	 * a UniProt reference mismatch (thus indicating engineered residue)
	 * @param residue
	 * @param molecId
	 * @return
	 */
	public boolean isReferenceMismatch(Group residue, int molecId) {
		ChainEvolContext chain = getChainEvolContext(molecId);

		return !chain.getPdbToUniProtMapper().isPdbGroupMatchingUniProt(residue); 
	}
	
	public String getReferenceMismatchWarningMsg(List<Group> unreliableResidues, String typeOfResidues) {
		String msg = null;
		if (!unreliableResidues.isEmpty()) {
			
			msg = unreliableResidues.size()+" "+typeOfResidues+
					" residues of chain "+unreliableResidues.get(0).getChainId()+
					" are unreliable because of mismatch of PDB sequence to UniProt reference: ";
			
			for (int i=0;i<unreliableResidues.size();i++) {
				String serial = unreliableResidues.get(i).getResidueNumber().toString();
				 
				msg+= serial +
					  "("+unreliableResidues.get(i).getPDBName()+")";
				
				if (i!=unreliableResidues.size()-1) msg+=", ";				
			}
			msg+=" ";
		}
		return msg;
	}
	
	/**
	 * Calculates the evolutionary score for the given list of residues by summing up evolutionary
	 * scores per residue and averaging (optionally weighted by BSA)
	 * @param residues
	 * @param molecId
	 * @return
	 */
	public double calcScore(List<Group> residues, int molecId) {
		return getChainEvolContext(molecId).calcScoreForResidueSet(residues);
	}
	
	/**
	 * Returns the distribution of evolutionary scores of random subsets of residues in the surface for
	 * given molecId
	 * @param molecId the molecule id: either {@link #FIRST} or {@link #SECOND}
	 * @param numSamples number of samples of size sampleSize to be taken from the surface
	 * @param sampleSize number of residues in each sample
	 * @param minAsaForSurface the minimum ASA for a residue to be considered surface
	 * @return
	 */
	public double[] getSurfaceScoreDist(int molecId, int numSamples, int sampleSize, double minAsaForSurface) {
		if (sampleSize==0) return new double[0];

		double[] dist = new double[numSamples];
		
		List<Group> surfResidues = null;
		if (molecId==FIRST) surfResidues = interf.getSurfaceResidues(minAsaForSurface).getFirst();
		if (molecId==SECOND) surfResidues = interf.getSurfaceResidues(minAsaForSurface).getSecond();
		
		RandomDataImpl rd = new RandomDataImpl();
		for (int i=0;i<numSamples;i++) {
			Object[] sample = rd.nextSample(surfResidues, sampleSize);
			List<Group> residues = new ArrayList<Group>(sample.length);
			for (int j=0;j<sample.length;j++){
				residues.add((Group)sample[j]);
			}
			ChainEvolContext cec = this.parent.getChainEvolContext(getChainId(molecId));
			dist[i] = cec.calcScoreForResidueSet(residues);
		}		

		return dist;
	}
	
	/**
	 * Returns the number of residues that belong to the surface of the protein
	 * @param molecId the molecule id: either {@link #FIRST} or {@link #SECOND}
	 * @param minAsaForSurface the minimum ASA for a residue to be considered surface
	 * @return
	 */
	public int getNumSurfaceResidues(int molecId, double minAsaForSurface) {
		if (molecId==FIRST) return interf.getSurfaceResidues(minAsaForSurface).getFirst().size();
		if (molecId==SECOND) return interf.getSurfaceResidues(minAsaForSurface).getSecond().size();
		return -1;
	}
	
	/**
	 * Set the b-factors of the interface to conservation score values (at the
	 * moment, only entropy supported). 
	 */
	public void setConservationScoresAsBfactors() {
		if (getChainEvolContext(FIRST)!=null && getChainEvolContext(FIRST).isProtein()) {
			setConservationScoresAsBfactors(FIRST);
		}
		if (getChainEvolContext(SECOND)!=null && getChainEvolContext(SECOND).isProtein()) {
			setConservationScoresAsBfactors(SECOND);
		}
	}
	
	/**
	 * Set the b-factors of the given molecId (FIRST or SECOND) to conservation score values (at the
	 * moment, only entropy supported).
	 * @param molecId
	 * @throws NullPointerException if evolutionary scores are not calculated yet
	 */
	private void setConservationScoresAsBfactors(int molecId) {
		
		// do nothing (i.e. keep original b-factors) if there's no query match for this sequence and thus no evol scores calculated 
		if (!getChainEvolContext(molecId).hasQueryMatch()) return;
		
		List<Double> conservationScores = null;
		Chain pdb = getMolecule(molecId);
		conservationScores = getChainEvolContext(molecId).getConservationScores();
		
		for (Group residue:pdb.getAtomGroups()) {
			
			if (residue.isWater()) continue;

			int queryPos = getChainEvolContext(molecId).getPdbToUniProtMapper().getUniProtIndexForPdbGroup(residue, !getChainEvolContext(molecId).isSearchWithFullUniprot());
			
			if (queryPos!=-1) {
				for (Atom atom:residue.getAtoms()) {
					atom.setTempFactor(conservationScores.get(queryPos-1).floatValue());
				}
			} else {
				
				// when no entropy info is available for a residue we still want to assign a value for it
				// or otherwise the residue would keep its original real bfactor and then possibly screw up the
				// scaling of colors for the rest
				// The most sensible value we can use is the max entropy so that it looks like a poorly conserved residue
				double maxEntropy = Math.log(this.getChainEvolContext(molecId).getHomologs().getReducedAlphabet().getNumLetters()) / Math.log(2);
				LOGGER.info("Residue {} ({}) of chain {} has no entropy value associated to it, will set its b-factor to max entropy ({})",
						residue.getResidueNumber().toString(), residue.getPDBName(), residue.getChainId(), maxEntropy);
				for (Atom atom:residue.getAtoms()) {
					atom.setTempFactor((float)maxEntropy);
				}
			}
		}
	}
	
	public boolean hasEnoughHomologs(int molecId){
		return this.getChainEvolContext(molecId).getNumHomologs()>=parent.getMinNumSeqs(); 
	}

	public int getMinNumSeqs() {
		return this.parent.getMinNumSeqs();
	}
	
	public static boolean isProtein(StructureInterface interf, int molecId) {
		if (molecId==FIRST) {
			return interf.getMolecules().getFirst()[0].getGroup().getChain().isProtein();
		} else if (molecId==SECOND) {
			return interf.getMolecules().getSecond()[0].getGroup().getChain().isProtein();
		} else {
			throw new IllegalArgumentException("Fatal error! Wrong molecId "+molecId);
		}
	}

	/**
	 * Tells whether the given interface is a protein-protein interface
	 * @param interf
	 * @return true if both partners of the interface are protein, false otherwise
	 */
	public static boolean isProtProt(StructureInterface interf) {
		return 
			isProtein(interf, FIRST) &&
			isProtein(interf, SECOND);
	}
}
