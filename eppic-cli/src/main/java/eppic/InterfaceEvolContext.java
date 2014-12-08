package eppic;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math.random.RandomDataImpl;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.contact.StructureInterface;

import eppic.predictors.EvolCoreRimPredictor;
import eppic.predictors.EvolCoreSurfacePredictor;

public class InterfaceEvolContext implements Serializable {

	private static final long serialVersionUID = 1L;

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
	
	private PdbChain getMolecule(int molecId) {
		if (molecId==FIRST) {
			return getInterface().getFirstMolecule();
		}
		if (molecId==SECOND) {
			return getInterface().getSecondMolecule();
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
		int resSer = residue.getSerial();
		if (resSer!=-1 && !chain.isPdbSeqPositionMatchingUniprot(resSer)) {
			return true;
		}
		return false;
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
	 * @param weighted
	 * @return
	 */
	public double calcScore(List<Group> residues, int molecId, boolean weighted) {
		return getChainEvolContext(molecId).calcScoreForResidueSet(residues, weighted);
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
			dist[i] = cec.calcScoreForResidueSet(residues, false);
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
	 * Writes out a gzipped PDB file with the 2 chains of this interface with evolutionary scores 
	 * as b-factors. 
	 * In order for the file to be handled properly by molecular viewers whenever the two
	 * chains have the same code we rename the second one to the next letter in alphabet.
	 * PDB chain codes are used for the output, not CIF codes.  
	 * @param file
	 * @param usePdbResSer if true PDB residue serials are written, if false CIF residue 
	 * serials are written 
	 * @throws IOException
	 */
	public void writePdbFile(File file, boolean usePdbResSer) throws IOException {

		if (interf.isFirstProtein() && interf.isSecondProtein()) {

			setConservationScoresAsBfactors(FIRST);
			setConservationScoresAsBfactors(SECOND);
			
			this.interf.writeToPdbFile(file, usePdbResSer, true);
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
		PdbChain pdb = getMolecule(molecId);
		conservationScores = getChainEvolContext(molecId).getConservationScores();
		
		HashMap<Integer,Double> map = new HashMap<Integer, Double>();
		for (Residue residue:pdb) {
			// we don't need to take care of het residues, as we use the uniprot ref for the calc of entropies entropies will always be asigned even for hets
			//if (!(residue instanceof AaResidue)) continue;
			int resser = residue.getSerial();
			int queryPos = getChainEvolContext(molecId).getQueryUniprotPosForPDBPos(resser); 
			 
			if (queryPos!=-1) {   
				map.put(resser, conservationScores.get(queryPos-1));	
			} else {
				
				// when no entropy info is available for a residue we still want to assign a value for it
				// or otherwise the residue would keep its original real bfactor and then possibly screw up the
				// scaling of colors for the rest
				// The most sensible value we can use is the max entropy so that it looks like a poorly conserved residue
				double maxEntropy = Math.log(this.getChainEvolContext(molecId).getHomologs().getReducedAlphabet())/Math.log(2);
				map.put(resser, maxEntropy);
				
			}
		}
		pdb.setBFactorsPerResidue(map);		
	}
	
	public boolean hasEnoughHomologs(int molecId){
		return this.getChainEvolContext(molecId).getNumHomologs()>=parent.getMinNumSeqs(); 
	}

	public int getMinNumSeqs() {
		return this.parent.getMinNumSeqs();
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
