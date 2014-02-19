package eppic;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import owl.core.structure.ChainInterface;
import owl.core.structure.PdbChain;
import owl.core.structure.Residue;

public class InterfaceEvolContext implements Serializable {

	private static final long serialVersionUID = 1L;

	protected static final int FIRST  = 0;
	protected static final int SECOND = 1;

	private InterfaceEvolContextList parent;
	
	private ChainInterface interf;

	private int minNumSeqs;
	

	
	
	public InterfaceEvolContext(ChainInterface interf, InterfaceEvolContextList parent) {
		this.interf = interf;
		this.parent = parent;
	}

	public ChainInterface getInterface() {
		return interf;
	}
	
	public ChainEvolContext getFirstChainEvolContext() {
		return parent.getChainEvolContext(interf.getFirstMolecule().getPdbChainCode());
	}
	
	public ChainEvolContext getSecondChainEvolContext() {
		return parent.getChainEvolContext(interf.getSecondMolecule().getPdbChainCode());
	}
	
	public ChainEvolContext getChainEvolContext(int molecId) {
		if (molecId==FIRST) return getFirstChainEvolContext();
		if (molecId==SECOND) return getSecondChainEvolContext();
		return null;
	}
	
	private PdbChain getMolecule(int molecId) {
		if (molecId==FIRST) {
			return getInterface().getFirstMolecule();
		}
		if (molecId==SECOND) {
			return getInterface().getSecondMolecule();
		}
		return null;		
	}
	
	public int getMinNumSeqs() {
		return minNumSeqs;
	}
	
	public void setMinNumSeqs(int minNumSeqs) {
		this.minNumSeqs = minNumSeqs;
	}
	
	/**
	 * Finds all unreliable core residues and returns them in a list.
	 * Unreliable are all residues for which the alignment from reference UniProt to PDB doesn't match
	 * @param molecId
	 * @return
	 */
	public List<Residue> getUnreliableCoreRes(int molecId) {
		List<Residue> coreResidues = getInterface().getRimCore(molecId).getCoreResidues();
		return getReferenceMismatchResidues(coreResidues, molecId);
	}
	
	/**
	 * Finds all unreliable rim residues and returns them in a list.
	 * Unreliable are all residues for which the alignment from reference UniProt to PDB doesn't match
	 * @param molecId
	 * @return
	 */
	public List<Residue> getUnreliableRimRes(int molecId) {
		List<Residue> rimResidues = getInterface().getRimCore(molecId).getRimResidues();
		return getReferenceMismatchResidues(rimResidues, molecId);
	}

	/**
	 * Finds all unreliable residues that belong to no crystal interface above given minInterfArea and returns them in a list.
	 * Unreliable are all residues for which the alignment from reference UniProt to PDB doesn't match
	 * @param molecId
	 * @param minInterfArea
	 * @param minAsaForSurface
	 * @return
	 */
	public List<Residue> getUnreliableNotInInterfacesRes(int molecId, double minInterfArea, double minAsaForSurface) {
		List<Residue> notInInterfacesResidues = parent.getResiduesNotInInterfaces(getMolecule(molecId).getPdbChainCode(), minInterfArea, minAsaForSurface);
		return getReferenceMismatchResidues(notInInterfacesResidues, molecId);
	}
	
	/**
	 * Given a list of residues returns the subset of those that are unreliable 
	 * because of mismatch of PDB sequence to UniProt reference matching (thus indicating
	 * engineered residues).
	 * @param residues
	 * @param molecId
	 * @return
	 */
	public List<Residue> getReferenceMismatchResidues(List<Residue> residues, int molecId) {
		List<Residue> unreliableResidues = new ArrayList<Residue>();
		for (Residue res:residues){
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
	public boolean isReferenceMismatch(Residue residue, int molecId) {
		ChainEvolContext chain = getChainEvolContext(molecId);
		int resSer = residue.getSerial();
		if (resSer!=-1 && !chain.isPdbSeqPositionMatchingUniprot(resSer)) {
			return true;
		}
		return false;
	}
	
	public String getReferenceMismatchWarningMsg(List<Residue> unreliableResidues, String typeOfResidues) {
		String msg = null;
		if (!unreliableResidues.isEmpty()) {
			
			msg = unreliableResidues.size()+" "+typeOfResidues+
					" residues of chain "+unreliableResidues.get(0).getParent().getPdbChainCode()+
					" are unreliable because of mismatch of PDB sequence to UniProt reference: ";
			
			for (int i=0;i<unreliableResidues.size();i++) {
				String serial = null;
				if (parent.isUsePdbResSer()) {
					serial = unreliableResidues.get(i).getPdbSerial();
				} else {
					serial = "" + unreliableResidues.get(i).getSerial();
				}
				msg+= serial +
					  "("+unreliableResidues.get(i).getLongCode()+")";
				
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
	 * @param scoType
	 * @param weighted
	 * @return
	 */
	public double calcScore(List<Residue> residues, int molecId, ScoringType scoType, boolean weighted) {
		return getChainEvolContext(molecId).calcScoreForResidueSet(residues, scoType, weighted);
	}
	
	/**
	 * Returns the distribution of evolutionary scores of random subsets of residues in the surface (not belonging 
	 * to any interface above minInterfArea) for given pdbChainCode and scoType.
	 * The result is cached in a map and taken from there upon subsequent call.
	 * @param molecId the molecule id: either {@link #FIRST} or {@link #SECOND}
	 * @param minInterfArea the residues considered will be those that are not in interfaces above this area value
	 * @param numSamples number of samples of size sampleSize to be taken from the surface
	 * @param sampleSize number of residues in each sample
	 * @param scoType
	 * @param minAsaForSurface the minimum ASA for a residue to be considered surface
	 * @return
	 */
	public double[] getSurfaceScoreDist(int molecId, double minInterfArea, int numSamples, int sampleSize, ScoringType scoType, double minAsaForSurface) {		
		return parent.getSurfaceScoreDist(getMolecule(molecId).getPdbChainCode(), minInterfArea, numSamples, sampleSize, scoType, minAsaForSurface); 
	}
	
	/**
	 * Returns the number of residues that belong to the surface of the protein but 
	 * are not member of any interface above minInterfArea 
	 * @param molecId the molecule id: either {@link #FIRST} or {@link #SECOND}
	 * @param minInterfArea the residues considered will be those that are not in interfaces above this area value
	 * @param minAsaForSurface the minimum ASA for a residue to be considered surface
	 * @return
	 */
	public int getNumResiduesNotInInterfaces(int molecId, double minInterfArea, double minAsaForSurface) {
		return parent.getResiduesNotInInterfaces(getMolecule(molecId).getPdbChainCode(), minInterfArea, minAsaForSurface).size();
	}
	
	/**
	 * Writes out a gzipped PDB file with the 2 chains of this interface with evolutionary scores 
	 * as b-factors. 
	 * In order for the file to be handled properly by molecular viewers whenever the two
	 * chains have the same code we rename the second one to the next letter in alphabet.
	 * PDB chain codes are used for the output, not CIF codes.  
	 * @param file
	 * @param scoringType
	 * @param usePdbResSer if true PDB residue serials are written, if false CIF residue 
	 * serials are written 
	 * @throws IOException
	 */
	public void writePdbFile(File file, ScoringType scoringType, boolean usePdbResSer) throws IOException {

		if (interf.isFirstProtein() && interf.isSecondProtein()) {

			setConservationScoresAsBfactors(FIRST,scoringType);
			setConservationScoresAsBfactors(SECOND,scoringType);
			
			this.interf.writeToPdbFile(file, usePdbResSer, true);
		}
	}
	
	/**
	 * Set the b-factors of the given molecId (FIRST or SECOND) to conservation score values (at the
	 * moment, only entropy supported).
	 * @param molecId
	 * @throws NullPointerException if evolutionary scores are not calculated yet
	 */
	private void setConservationScoresAsBfactors(int molecId, ScoringType scoType) {
		if (scoType!=ScoringType.CORERIM) 
			throw new IllegalArgumentException("Only entropy scoring supported at the moment as evolutionary scoring");
		
		// do nothing (i.e. keep original b-factors) if there's no query match for this sequence and thus no evol scores calculated 
		if (!getChainEvolContext(molecId).hasQueryMatch()) return;
		
		List<Double> conservationScores = null;
		PdbChain pdb = null;
		if (molecId==FIRST) {
			pdb = interf.getFirstMolecule();
		} else if (molecId==SECOND) {
			pdb = interf.getSecondMolecule();
		}
		conservationScores = getChainEvolContext(molecId).getConservationScores(scoType);
		
		HashMap<Integer,Double> map = new HashMap<Integer, Double>();
		for (Residue residue:pdb) {
			// we don't need to take care of het residues, as we use the uniprot ref for the calc of entropies entropies will always be asigned even for hets
			//if (!(residue instanceof AaResidue)) continue;
			int resser = residue.getSerial();
			int queryPos = -2;
			if (scoType==ScoringType.CORERIM) {
				queryPos = getChainEvolContext(molecId).getQueryUniprotPosForPDBPos(resser); 
			} 
			if (queryPos!=-1) {   
				map.put(resser, conservationScores.get(queryPos));	
			} else {
				
				if (scoType==ScoringType.CORERIM) {
					// when no entropy info is available for a residue we still want to assign a value for it
					// or otherwise the residue would keep its original real bfactor and then possibly screw up the
					// scaling of colors for the rest
					// The most sensible value we can use is the max entropy so that it looks like a poorly conserved residue
					double maxEntropy = Math.log(this.getChainEvolContext(molecId).getHomologs().getReducedAlphabet())/Math.log(2);
					map.put(resser, maxEntropy);
				} else {
					// if not in ENTROPY, we assign 0 (just as placeholder, only scoring supported at the moment is entropy)
					map.put(resser, 0.0);
				}
			}
		}
		pdb.setBFactorsPerResidue(map);		
	}
	
	public boolean hasEnoughHomologs(int molecId){
		return this.getChainEvolContext(molecId).getNumHomologs()>=minNumSeqs;
	}

	public boolean isProtein(int molecId) {
		if (molecId==FIRST) {
			return this.interf.isFirstProtein();
		} else if (molecId==SECOND) {
			return this.interf.isSecondProtein();
		} else {
			throw new IllegalArgumentException("Fatal error! Wrong molecId "+molecId);
		}
	}

}
