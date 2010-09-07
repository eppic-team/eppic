package crk;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import owl.core.structure.ChainInterface;
import owl.core.structure.InterfaceRimCore;
import owl.core.structure.Pdb;
import owl.core.structure.Residue;

public class InterfaceEvolContext {

	private static final Log LOGGER = LogFactory.getLog(InterfaceEvolContext.class);

	
	private ChainInterface pisaInterf;
	private List<ChainEvolContext> chains;  // At the moment strictly 2 members (matching the 2 PisaMolecules of pisaInterf). 
											// If either of the 2 molecules is not a protein then is null.
//	private List<List<Residue>> unreliablePDBPositions; // At the moment strictly 2 members (matching the 2 PisaMolecules of pisaInterf).
													 	// The lists contain all residues that are in an unreliable positions (because of PDB to uniprot mapping problems in the query)
//	private List<List<Residue>> unreliableCDSPositions; // At the moment strictly 2 members (matching the 2 PisaMolecules of pisaInterf).
 														// The lists contain all residues that are in an unreliable positions (because uniprot to CDS matching problems of the homologs)

	
	
	public InterfaceEvolContext(ChainInterface pisaInterf, List<ChainEvolContext> chains) {
		this.pisaInterf = pisaInterf;
		this.chains = chains;
		
//		unreliablePDBPositions = new ArrayList<List<Residue>>();
//		unreliableCDSPositions = new ArrayList<List<Residue>>();
//		
//		unreliablePDBPositions.add(checkResiduesForPDBReliability(chains.get(0), pisaInterf.getFirstMolecule()));
//		unreliablePDBPositions.add(checkResiduesForPDBReliability(chains.get(1), pisaInterf.getSecondMolecule()));
//		unreliableCDSPositions.add(checkResiduesForCDSReliability(chains.get(0), pisaInterf.getFirstMolecule()));
//		unreliableCDSPositions.add(checkResiduesForCDSReliability(chains.get(1), pisaInterf.getSecondMolecule()));
	}

	public InterfaceScore scoreEntropy(double bsaToAsaSoftCutoff, double bsaToAsaHardCutoff, double relaxationStep, 
			int minCoreSize, int minMemberCoreSize,
			int homologsCutoff,  
			boolean weighted) {
		return scoreInterface(bsaToAsaSoftCutoff, bsaToAsaHardCutoff, relaxationStep, 
				minCoreSize, minMemberCoreSize, 
				homologsCutoff,
				weighted,
				ScoringType.ENTROPY);
	}

	public InterfaceScore scoreKaKs(double bsaToAsaSoftCutoff, double bsaToAsaHardCutoff, double relaxationStep, 
			int minCoreSize, int minMemberCoreSize,
			int homologsCutoff,  
			boolean weighted) {
		return scoreInterface(bsaToAsaSoftCutoff, bsaToAsaHardCutoff, relaxationStep, 
				minCoreSize, minMemberCoreSize, 
				homologsCutoff,
				weighted,
				ScoringType.KAKS);
	}

	private InterfaceScore scoreInterface(double bsaToAsaSoftCutoff, double bsaToAsaHardCutoff, double relaxationStep, 
			int minCoreSize, int minMemberCoreSize,
			int homologsCutoff,  
			boolean weighted, 
			ScoringType scoType) {

		double rimScore1 = Double.NaN;
		double coreScore1 = Double.NaN;
		double rimScore2 = Double.NaN;
		double coreScore2 = Double.NaN;
		int numHomologs1 = 0;
		int numHomologs2 = 0;
		
		Map<Integer,InterfaceRimCore> rimcores = this.pisaInterf.getRimAndCore(bsaToAsaSoftCutoff, bsaToAsaHardCutoff, relaxationStep, minCoreSize);
		InterfaceRimCore rimCore1 = rimcores.get(1);
		InterfaceRimCore rimCore2 = rimcores.get(2);
		// first checking residues for the consistency of the CDS data (alignments of translations) and PDB data (alignment of PDB to uniprot)
		List<Residue> unrelRimResidues1 = new ArrayList<Residue>();
		List<Residue> unrelCoreResidues1 = new ArrayList<Residue>();
		List<Residue> unrelRimResidues2 = new ArrayList<Residue>();
		List<Residue> unrelCoreResidues2 = new ArrayList<Residue>();
		// rimCore1/2 will be null when the molecule is not a protein
		if (rimCore1 != null) {
			ChainEvolContext chain = chains.get(0);
			rimScore1  = calcScore(rimCore1.getRimResidues(), chain, scoType, pisaInterf.getFirstMolecule(), weighted);
			coreScore1 = calcScore(rimCore1.getCoreResidues(),chain, scoType, pisaInterf.getFirstMolecule(), weighted);
			numHomologs1 = chain.getNumHomologs();
			unrelRimResidues1.addAll(checkResiduesForPDBReliability(rimCore1.getRimResidues(), chain, pisaInterf.getFirstMolecule()));
			unrelCoreResidues1.addAll(checkResiduesForPDBReliability(rimCore1.getCoreResidues(), chain, pisaInterf.getFirstMolecule()));
			if (scoType==ScoringType.KAKS) {
				unrelRimResidues1.addAll(checkResiduesForCDSReliability(rimCore1.getRimResidues(), chain, pisaInterf.getFirstMolecule()));	
				unrelCoreResidues1.addAll(checkResiduesForCDSReliability(rimCore1.getCoreResidues(), chain, pisaInterf.getFirstMolecule()));
			}
		}
		if (rimCore2 != null) {
			ChainEvolContext chain = chains.get(1);
			rimScore2  = calcScore(rimCore2.getRimResidues(), chain, scoType, pisaInterf.getSecondMolecule(), weighted);
			coreScore2 = calcScore(rimCore2.getCoreResidues(),chain, scoType, pisaInterf.getSecondMolecule(), weighted);
			numHomologs2 = chain.getNumHomologs();
			unrelRimResidues2.addAll(checkResiduesForPDBReliability(rimCore2.getRimResidues(), chain, pisaInterf.getSecondMolecule()));
			unrelCoreResidues2.addAll(checkResiduesForPDBReliability(rimCore2.getCoreResidues(), chain, pisaInterf.getSecondMolecule()));
			if (scoType==ScoringType.KAKS) {
				unrelRimResidues2.addAll(checkResiduesForCDSReliability(rimCore2.getRimResidues(), chain, pisaInterf.getSecondMolecule()));	
				unrelCoreResidues2.addAll(checkResiduesForCDSReliability(rimCore2.getCoreResidues(), chain, pisaInterf.getSecondMolecule()));
			}
		}
				
		InterfaceMemberScore ims1 = new InterfaceMemberScore(rimCore1, coreScore1, rimScore1, numHomologs1, homologsCutoff, minMemberCoreSize, unrelRimResidues1, unrelCoreResidues1, 1);
		InterfaceMemberScore ims2 = new InterfaceMemberScore(rimCore2, coreScore2, rimScore2, numHomologs2, homologsCutoff, minMemberCoreSize, unrelRimResidues2, unrelCoreResidues2, 2);
		
		return new InterfaceScore(ims1, ims2, minCoreSize);
	}
	
//	private List<Residue> checkResiduesForPDBReliability(ChainEvolContext chain, PisaMolecule pisaMol) {
//		List<Residue> unreliableResidues = new ArrayList<Residue>();
//		Pdb pdb = chain.getPdb(pisaMol.getChainId());
//		
//		for (PisaResidue res:pisaMol.getResidues()) {
//			int resSer = chain.getResSerFromPdbResSer(pisaMol.getChainId(), res.getPdbResSer());
//
//			if (resSer!=-1 && !chain.isPdbSeqPositionMatchingUniprot(resSer)) {
//				unreliableResidues.add(pdb.getResidue(resSer));
//			}
//		}
//		
//		return unreliableResidues;
//	}
//	
//	private List<Residue> checkResiduesForCDSReliability(ChainEvolContext chain, PisaMolecule pisaMol) {
//		List<Residue> unreliableResidues = new ArrayList<Residue>();
//		Pdb pdb = chain.getPdb(pisaMol.getChainId());
//		
//		for (PisaResidue res:pisaMol.getResidues()) {
//			int resSer = chain.getResSerFromPdbResSer(pisaMol.getChainId(), res.getPdbResSer());
//
//			if (resSer!=-1 && !chain.isPdbSeqPositionReliable(resSer)) {
//				unreliableResidues.add(pdb.getResidue(resSer));
//			}
//		}
//		
//		return unreliableResidues;
//	}
	
	private List<Residue> checkResiduesForPDBReliability(List<Residue> residues, ChainEvolContext chain, Pdb pisaMol) {
		List<Residue> unreliableResidues = new ArrayList<Residue>();
		for (Residue res:residues){
			int resSer = chain.getResSerFromPdbResSer(pisaMol.getPdbChainCode(), res.getPdbSerial());
			if (resSer!=-1 && !chain.isPdbSeqPositionMatchingUniprot(resSer)) {
				unreliableResidues.add(res);
			}
		}
		if (!unreliableResidues.isEmpty()) {
			String msg = "Interface residue serials ";
			for (int i=0;i<unreliableResidues.size();i++) {
				msg+=unreliableResidues.get(i).getAaType().getThreeLetterCode()+unreliableResidues.get(i).getPdbSerial();
				if (i!=unreliableResidues.size()-1) {
					msg+=",";
				}
			}
			msg+=" can't be evaluated because of PDB SEQRES not matching the Uniprot sequence at those positions.";
			LOGGER.warn(msg);
		}
		return unreliableResidues;
	}
	
	private List<Residue> checkResiduesForCDSReliability(List<Residue> residues, ChainEvolContext chain, Pdb pisaMol) {
		List<Residue> unreliableResidues = new ArrayList<Residue>();
		for (Residue res:residues){
			int resSer = chain.getResSerFromPdbResSer(pisaMol.getPdbChainCode(), res.getPdbSerial());
			if (resSer!=-1 && !chain.isPdbSeqPositionReliable(resSer)) {
				unreliableResidues.add(res);
			}				
		}
		if (!unreliableResidues.isEmpty()) {
			String msg = "Interface residue serials ";
			for (int i=0;i<unreliableResidues.size();i++) {
				msg+=unreliableResidues.get(i).getAaType().getThreeLetterCode()+unreliableResidues.get(i).getPdbSerial();
				if (i!=unreliableResidues.size()-1) {
					msg+=(",");
				}
			}
			msg+=" can't be evaluated because of unreliable CDS sequence information.";		
			LOGGER.warn(msg);
		}
		return unreliableResidues;
	}
	
	private double calcScore(List<Residue> residues, ChainEvolContext chain, ScoringType scoType, Pdb pisaMol, boolean weighted) {
		double totalScore = 0.0;
		double totalWeight = 0.0;
		List<Double> conservScores = chain.getConservationScores(scoType);
		for (Residue res:residues){
			int resSer = chain.getResSerFromPdbResSer(pisaMol.getPdbChainCode(), res.getPdbSerial());

			if (resSer!=-1) {
				int queryPos = -2;
				if (scoType==ScoringType.ENTROPY) {
					queryPos = chain.getQueryUniprotPosForPDBPos(resSer); 
				} else if (scoType==ScoringType.KAKS) {
					queryPos = chain.getQueryCDSPosForPDBPos(resSer);
				}
				if (queryPos!=-1) {   
					double weight = 1.0;
					if (weighted) {
						weight = res.getBsa();
					}
					totalScore += weight*(conservScores.get(queryPos));
					totalWeight += weight;
				} else {
					
				}
			} else {
				LOGGER.warn("Can't map PISA pdb residue serial "+res.getPdbSerial()+" (res type: "+res.getAaType().getThreeLetterCode()+", PISA serial: "+res.getSerial()+")");
				LOGGER.warn("The residue will not be used for scoring");
			}
		}
		return totalScore/totalWeight;
	}
	
	public void writePdbFile(File file, ScoringType scoType) throws IOException {
		PrintStream ps = new PrintStream(file);
		String chain1 = null;
		String chain2 = null;
		if (pisaInterf.isFirstProtein()) {
			chain1 = pisaInterf.getFirstMolecule().getPdbChainCode();
			chains.get(0).setConservationScoresAsBfactors(chain1,scoType);
			// we copy in order to leave the original Pdbs unaltered (essential to be able to apply transformations several times)
			Pdb pdb1 = chains.get(0).getPdb(chain1).copy();
			pdb1.transform(pisaInterf.getFirstTransfOrth());
			pdb1.writeAtomLines(ps);
		}
		if (pisaInterf.isSecondProtein()) {
			
			chain2 = pisaInterf.getSecondMolecule().getPdbChainCode();
			String chain2forOutput = chain2; // the name we will put to the chain2 in the output pdb file
			if (chain1!=null && chain1.equals(chain2)) {
				// if both chains are named equally we want to still named them differently in the output pdb file
				// so that molecular viewers can handle properly the 2 chains as separate entities 
				char letter = chain1.charAt(0);
				if (letter!='Z' && letter!='z') {
					chain2forOutput = Character.toString((char)(letter+1)); // i.e. next letter in alphabet
				} else {
					chain2forOutput = Character.toString((char)(letter-25)); //i.e. 'A' or 'a'
				}
				LOGGER.warn("Chain "+chain2+" renamed to "+chain2forOutput+" to write the output PDB file "+file);
			}
			chains.get(1).setConservationScoresAsBfactors(chain2,scoType);
			Pdb pdb2 = chains.get(1).getPdb(chain2).copy();
			pdb2.transform(pisaInterf.getSecondTransfOrth());
			pdb2.setChainCode(chain2forOutput);
			pdb2.writeAtomLines(ps);
		}
		ps.close();
	}
}
