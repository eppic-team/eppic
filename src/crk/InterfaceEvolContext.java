package crk;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import owl.core.structure.ChainInterface;
import owl.core.structure.PdbChain;
import owl.core.structure.Residue;
import owl.core.structure.AaResidue;

public class InterfaceEvolContext implements Serializable {

	private static final long serialVersionUID = 1L;

	protected static final int FIRST  = 0;
	protected static final int SECOND = 1;

	private InterfaceEvolContextList parent;
	
	private ChainInterface interf;

	private ChainEvolContextList cecs;
		
	private int minNumSeqs;
	

	
	
	public InterfaceEvolContext(ChainInterface interf, ChainEvolContextList cecs, InterfaceEvolContextList parent) {
		this.interf = interf;
		this.cecs = cecs;
		this.parent = parent;
	}

	public ChainInterface getInterface() {
		return interf;
	}
	
	public ChainEvolContext getFirstChainEvolContext() {
		return cecs.getChainEvolContext(interf.getFirstMolecule().getPdbChainCode());
	}
	
	public ChainEvolContext getSecondChainEvolContext() {
		return cecs.getChainEvolContext(interf.getSecondMolecule().getPdbChainCode());
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
	
	public ChainEvolContextList getChainEvolContextList() {
		return cecs;
	}
	
	public int getMinNumSeqs() {
		return minNumSeqs;
	}
	
	public void setMinNumSeqs(int minNumSeqs) {
		this.minNumSeqs = minNumSeqs;
	}
	
	/**
	 * Finds all unreliable core residues and returns them in a list.
	 * Unreliable are all residues for which the alignment from Uniprot to PDB doesn't match
	 * @return
	 */
	public List<Residue> getUnreliableCoreRes(int molecId) {
		List<Residue> coreResidues = getInterface().getRimCore(molecId).getCoreResidues();
		return getReferenceMismatchResidues(coreResidues, molecId);
	}
	
	/**
	 * Finds all unreliable rim residues and returns them in a list.
	 * Unreliable are all residues for which the alignment from Uniprot to PDB doesn't match
	 * @return
	 */
	public List<Residue> getUnreliableRimRes(int molecId) {
		List<Residue> rimResidues = getInterface().getRimCore(molecId).getRimResidues();
		return getReferenceMismatchResidues(rimResidues, molecId);
	}
	
	/**
	 * Given a list of residues returns the subset of those that are unreliable 
	 * because of mismatch of PDB sequence to Uniprot reference matching (thus indicating
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
				msg+= unreliableResidues.get(i).getSerial()+
					  "("+unreliableResidues.get(i).getLongCode()+")";
				
				if (i!=unreliableResidues.size()-1) msg+=",";				
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
	 * @return
	 */
	public double[] getSurfaceScoreDist(int molecId, double minInterfArea, int numSamples, int sampleSize, ScoringType scoType) {		
		return parent.getSurfaceScoreDist(getMolecule(molecId).getPdbChainCode(), minInterfArea, numSamples, sampleSize, scoType); 
	}
	
	/**
	 * Returns the number of residues that belong to the surface of the protein but 
	 * are not member of any interface above minInterfArea 
	 * @param molecId the molecule id: either {@link #FIRST} or {@link #SECOND}
	 * @param minInterfArea the residues considered will be those that are not in interfaces above this area value
	 * @return
	 */
	public int getNumResiduesNotInInterfaces(int molecId, double minInterfArea) {
		return parent.getResiduesNotInInterfaces(getMolecule(molecId).getPdbChainCode(), minInterfArea).size();
	}
	
	/**
	 * Writes out a PDB file with the 2 chains of this interface with evolutionary scores 
	 * as b-factors. 
	 * In order for the file to be handled properly by molecular viewers whenever the two
	 * chains have the same code we rename the second one to the next letter in alphabet.
	 * PDB chain codes are used for the output, not cif codes.  
	 * @param file
	 * @param scoringType
	 * @throws IOException
	 */
	public void writePdbFile(File file, ScoringType scoringType) throws IOException {

		if (interf.isFirstProtein() && interf.isSecondProtein()) {

			setConservationScoresAsBfactors(FIRST,scoringType);
			setConservationScoresAsBfactors(SECOND,scoringType);
			
			this.interf.writeToPdbFile(file);
		}
	}
	
	/**
	 * Set the b-factors of the given molecId (FIRST or SECOND) to conservation score values (entropy or ka/ks).
	 * @param molecId
	 * @param scoType
	 * @throws NullPointerException if ka/ks ratios are not calculated yet by calling {@link #computeKaKsRatiosSelecton(File)}
	 */
	private void setConservationScoresAsBfactors(int molecId, ScoringType scoType) {
		
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
			if (!(residue instanceof AaResidue)) continue;
			int resser = residue.getSerial();
			int queryPos = -2;
			if (scoType==ScoringType.ENTROPY) {
				queryPos = getChainEvolContext(molecId).getQueryUniprotPosForPDBPos(resser); 
			} 
//			else if (scoType==ScoringType.KAKS) {
//				queryPos = getChainEvolContext(molecId).getQueryCDSPosForPDBPos(resser);
//			}
			if (queryPos!=-1) {   
				map.put(resser, conservationScores.get(queryPos));	
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

//	/**
//	 * Tells whether Ka/KS analysis is possible for this interface.
//	 * It will not be possible when there is no sufficient data from either chain.
//	 * @return
//	 */
//	public boolean canDoKaks() {
//		boolean canDoCRK = true;
//		if ((this.interf.isFirstProtein() && !getFirstChainEvolContext().canDoKaks()) || 
//			(this.interf.isSecondProtein() && !getSecondChainEvolContext().canDoKaks()) ) {
//			canDoCRK = false;
//		}
//		return canDoCRK;
//	}

}
