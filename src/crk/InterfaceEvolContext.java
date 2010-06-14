package crk;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import owl.core.connections.pisa.PisaInterface;
import owl.core.connections.pisa.PisaMolecule;
import owl.core.connections.pisa.PisaResidue;
import owl.core.connections.pisa.PisaRimCore;
import owl.core.sequence.alignment.MultipleSequenceAlignment;
import owl.core.structure.Pdb;

public class InterfaceEvolContext {

	private PisaInterface pisaInterf;
	private List<ChainEvolContext> chains;  // At the moment strictly 2 members (matching the 2 PisaMolecules of pisaInterf). 
											// If either of the 2 molecules is not a protein then is null.
	
	public InterfaceEvolContext(PisaInterface pisaInterf, List<ChainEvolContext> chains) {
		this.pisaInterf = pisaInterf;
		this.chains = chains;
	}

	public InterfaceScore scoreEntropy(double bsaToAsaSoftCutoff, double bsaToAsaHardCutoff, double relaxationStep, int minCoreSize, int minMemberCoreSize,
			int homologsCutoff,  
			boolean weighted, 
			int reducedAlphabet) {

		double rimEnt1 = Double.NaN;
		double coreEnt1 = Double.NaN;
		double rimEnt2 = Double.NaN;
		double coreEnt2 = Double.NaN;
		int numHomologs1 = 0;
		int numHomologs2 = 0;
		
		Map<Integer,PisaRimCore> rimcores = this.pisaInterf.getRimAndCore(bsaToAsaSoftCutoff, bsaToAsaHardCutoff, relaxationStep, minCoreSize);
		PisaRimCore rimCore1 = rimcores.get(1);
		PisaRimCore rimCore2 = rimcores.get(2);
		// rimCore1/2 will be null when the molecule is not a protein
		if (rimCore1 != null) {
			rimEnt1 = getEntropy(rimCore1.getRimResidues(), chains.get(0), pisaInterf.getFirstMolecule(), weighted, reducedAlphabet);
			coreEnt1 = getEntropy(rimCore1.getCoreResidues(), chains.get(0), pisaInterf.getFirstMolecule(), weighted, reducedAlphabet);
			numHomologs1 = chains.get(0).getNumHomologs();
		}
		if (rimCore2 != null) {
			rimEnt2 = getEntropy(rimCore2.getRimResidues(), chains.get(1), pisaInterf.getSecondMolecule(), weighted, reducedAlphabet);
			coreEnt2 = getEntropy(rimCore2.getCoreResidues(), chains.get(1), pisaInterf.getSecondMolecule(), weighted, reducedAlphabet);
			numHomologs2 = chains.get(1).getNumHomologs();
		}
				
		InterfaceMemberScore ims1 = new InterfaceMemberScore(rimCore1, coreEnt1, rimEnt1, numHomologs1, homologsCutoff, minMemberCoreSize, 1);
		InterfaceMemberScore ims2 = new InterfaceMemberScore(rimCore2, coreEnt2, rimEnt2, numHomologs2, homologsCutoff, minMemberCoreSize, 2);
		
		return new InterfaceScore(ims1, ims2, minCoreSize);
	}
	
	public double scoreCRK() {
		return 0;
	}
	
	private double getEntropy(List<PisaResidue> residues, ChainEvolContext chain, PisaMolecule pisaMol, boolean weighted, int reducedAlphabet) {
		MultipleSequenceAlignment aln = chain.getAlignment();
		Pdb pdb = chain.getPdb(pisaMol.getChainId());

		double totalEnt = 0.0;
		double totalWeight = 0.0;
		for (PisaResidue res:residues){
			int resSer = pdb.getResSerFromPdbResSer(res.getPdbResSer());
			if (resSer!=-1) {
				double weight = 1.0;
				if (weighted) {
					weight = res.getBsa();
				}
				totalEnt += weight*(aln.getColumnEntropy(aln.seq2al(pdb.getPdbCode()+chain.getRepresentativeChainCode(), resSer),reducedAlphabet));
				totalWeight += weight;
			} else {
				System.err.println("Can't map PISA pdb residue serial "+res.getPdbResSer()+" (res type:"+res.getResType()+", PISA serial: "+res.getResSerial()+")");
				System.err.println("The residue will not be used for scoring");
			}
		}
		return totalEnt/totalWeight;
	}
	
	public void writePdbFile(File file, int reducedAlphabet) throws IOException {
		PrintStream ps = new PrintStream(file);
		if (pisaInterf.getFirstMolecule().isProtein()) {
			chains.get(0).setEntropiesAsBfactors(pisaInterf.getFirstMolecule().getChainId(), reducedAlphabet);
			// we copy in order to leave the original Pdbs unaltered (essential to be able to apply transformations several times)
			Pdb pdb1 = chains.get(0).getPdb(pisaInterf.getFirstMolecule().getChainId()).copy();
			pdb1.transform(pisaInterf.getFirstMolecule().getSymOp());
			pdb1.writeAtomLines(ps);
		}
		if (pisaInterf.getSecondMolecule().isProtein()) {
			chains.get(1).setEntropiesAsBfactors(pisaInterf.getSecondMolecule().getChainId(), reducedAlphabet);
			Pdb pdb2 = chains.get(1).getPdb(pisaInterf.getSecondMolecule().getChainId()).copy();
			pdb2.transform(pisaInterf.getSecondMolecule().getSymOp());
			pdb2.writeAtomLines(ps);
		}
		ps.close();
	}
}
