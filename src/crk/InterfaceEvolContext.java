package crk;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import owl.core.connections.pisa.PisaInterface;
import owl.core.connections.pisa.PisaMolecule;
import owl.core.connections.pisa.PisaResidue;
import owl.core.connections.pisa.PisaRimCore;
import owl.core.structure.Pdb;

public class InterfaceEvolContext {

	private static final Logger LOGGER = Logger.getLogger(InterfaceEvolContext.class);
	
	private PisaInterface pisaInterf;
	private List<ChainEvolContext> chains;  // At the moment strictly 2 members (matching the 2 PisaMolecules of pisaInterf). 
											// If either of the 2 molecules is not a protein then is null.
//	private List<List<Residue>> unreliablePDBPositions; // At the moment strictly 2 members (matching the 2 PisaMolecules of pisaInterf).
													 	// The lists contain all residues that are in an unreliable positions (because of PDB to uniprot mapping problems in the query)
//	private List<List<Residue>> unreliableCDSPositions; // At the moment strictly 2 members (matching the 2 PisaMolecules of pisaInterf).
 														// The lists contain all residues that are in an unreliable positions (because uniprot to CDS matching problems of the homologs)

	
	
	public InterfaceEvolContext(PisaInterface pisaInterf, List<ChainEvolContext> chains) {
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

		double rimEnt1 = Double.NaN;
		double coreEnt1 = Double.NaN;
		double rimEnt2 = Double.NaN;
		double coreEnt2 = Double.NaN;
		int numHomologs1 = 0;
		int numHomologs2 = 0;
		
		Map<Integer,PisaRimCore> rimcores = this.pisaInterf.getRimAndCore(bsaToAsaSoftCutoff, bsaToAsaHardCutoff, relaxationStep, minCoreSize);
		PisaRimCore rimCore1 = rimcores.get(1);
		PisaRimCore rimCore2 = rimcores.get(2);
		// first checking residues for the consistency of the CDS data (alignments of translations) and PDB data (alignment of PDB to uniprot)
		List<PisaResidue> unrelRimResidues1 = new ArrayList<PisaResidue>();
		List<PisaResidue> unrelCoreResidues1 = new ArrayList<PisaResidue>();
		List<PisaResidue> unrelRimResidues2 = new ArrayList<PisaResidue>();
		List<PisaResidue> unrelCoreResidues2 = new ArrayList<PisaResidue>();
		// rimCore1/2 will be null when the molecule is not a protein
		if (rimCore1 != null) {
			ChainEvolContext chain = chains.get(0);
			rimEnt1  = calcScore(rimCore1.getRimResidues(), chain, scoType, pisaInterf.getFirstMolecule(), weighted);
			coreEnt1 = calcScore(rimCore1.getCoreResidues(),chain, scoType, pisaInterf.getFirstMolecule(), weighted);
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
			rimEnt2  = calcScore(rimCore2.getRimResidues(), chain, scoType, pisaInterf.getSecondMolecule(), weighted);
			coreEnt2 = calcScore(rimCore2.getCoreResidues(),chain, scoType, pisaInterf.getSecondMolecule(), weighted);
			numHomologs2 = chain.getNumHomologs();
			unrelRimResidues2.addAll(checkResiduesForPDBReliability(rimCore2.getRimResidues(), chain, pisaInterf.getSecondMolecule()));
			unrelCoreResidues2.addAll(checkResiduesForPDBReliability(rimCore2.getCoreResidues(), chain, pisaInterf.getSecondMolecule()));
			if (scoType==ScoringType.KAKS) {
				unrelRimResidues2.addAll(checkResiduesForCDSReliability(rimCore2.getRimResidues(), chain, pisaInterf.getSecondMolecule()));	
				unrelCoreResidues2.addAll(checkResiduesForCDSReliability(rimCore2.getCoreResidues(), chain, pisaInterf.getSecondMolecule()));
			}
		}
				
		InterfaceMemberScore ims1 = new InterfaceMemberScore(rimCore1, coreEnt1, rimEnt1, numHomologs1, homologsCutoff, minMemberCoreSize, unrelRimResidues1, unrelCoreResidues1, 1);
		InterfaceMemberScore ims2 = new InterfaceMemberScore(rimCore2, coreEnt2, rimEnt2, numHomologs2, homologsCutoff, minMemberCoreSize, unrelRimResidues2, unrelCoreResidues2, 2);
		
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
	
	private List<PisaResidue> checkResiduesForPDBReliability(List<PisaResidue> residues, ChainEvolContext chain, PisaMolecule pisaMol) {
		List<PisaResidue> unreliableResidues = new ArrayList<PisaResidue>();
		for (PisaResidue res:residues){
			int resSer = chain.getResSerFromPdbResSer(pisaMol.getChainId(), res.getPdbResSer());
			if (resSer!=-1 && !chain.isPdbSeqPositionMatchingUniprot(resSer)) {
				unreliableResidues.add(res);
			}
		}
		if (!unreliableResidues.isEmpty()) {
			String msg = "Interface residue serials ";
			for (int i=0;i<unreliableResidues.size();i++) {
				msg+=unreliableResidues.get(i).getResType()+unreliableResidues.get(i).getPdbResSer();
				if (i!=unreliableResidues.size()-1) {
					msg+=",";
				}
			}
			msg+=" can't be evaluated because of PDB SEQRES not matching the Uniprot sequence at those positions.";
			LOGGER.warn(msg);
		}
		return unreliableResidues;
	}
	
	private List<PisaResidue> checkResiduesForCDSReliability(List<PisaResidue> residues, ChainEvolContext chain, PisaMolecule pisaMol) {
		List<PisaResidue> unreliableResidues = new ArrayList<PisaResidue>();
		for (PisaResidue res:residues){
			int resSer = chain.getResSerFromPdbResSer(pisaMol.getChainId(), res.getPdbResSer());
			if (resSer!=-1 && !chain.isPdbSeqPositionReliable(resSer)) {
				unreliableResidues.add(res);
			}				
		}
		if (!unreliableResidues.isEmpty()) {
			String msg = "Interface residue serials ";
			for (int i=0;i<unreliableResidues.size();i++) {
				msg+=unreliableResidues.get(i).getResType()+unreliableResidues.get(i).getPdbResSer();
				if (i!=unreliableResidues.size()-1) {
					msg+=(",");
				}
			}
			msg+=" can't be evaluated because of unreliable CDS sequence information.";		
			LOGGER.warn(msg);
		}
		return unreliableResidues;
	}
	
	private double calcScore(List<PisaResidue> residues, ChainEvolContext chain, ScoringType scoType, PisaMolecule pisaMol, boolean weighted) {
		double totalScore = 0.0;
		double totalWeight = 0.0;
		List<Double> conservScores = chain.getConservationScores(scoType);
		for (PisaResidue res:residues){
			int resSer = chain.getResSerFromPdbResSer(pisaMol.getChainId(), res.getPdbResSer());

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
				LOGGER.warn("Can't map PISA pdb residue serial "+res.getPdbResSer()+" (res type:"+res.getResType()+", PISA serial: "+res.getResSerial()+")");
				LOGGER.warn("The residue will not be used for scoring");
			}
		}
		return totalScore/totalWeight;
	}
	
	public void writePdbFile(File file, ScoringType scoType) throws IOException {
		PrintStream ps = new PrintStream(file);
		if (pisaInterf.getFirstMolecule().isProtein()) {
			chains.get(0).setConservationScoresAsBfactors(pisaInterf.getFirstMolecule().getChainId(),scoType);
			// we copy in order to leave the original Pdbs unaltered (essential to be able to apply transformations several times)
			Pdb pdb1 = chains.get(0).getPdb(pisaInterf.getFirstMolecule().getChainId()).copy();
			pdb1.transform(pisaInterf.getFirstMolecule().getSymOp());
			pdb1.writeAtomLines(ps);
		}
		if (pisaInterf.getSecondMolecule().isProtein()) {
			chains.get(1).setConservationScoresAsBfactors(pisaInterf.getSecondMolecule().getChainId(),scoType);
			Pdb pdb2 = chains.get(1).getPdb(pisaInterf.getSecondMolecule().getChainId()).copy();
			pdb2.transform(pisaInterf.getSecondMolecule().getSymOp());
			pdb2.writeAtomLines(ps);
		}
		ps.close();
	}
}
