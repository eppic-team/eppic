package crk;

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
	private List<ChainEvolContext> chains;
	private PisaMolecule firstMolecule;
	private PisaMolecule secondMolecule;
	
	public InterfaceEvolContext(PisaInterface pisaInterf, List<ChainEvolContext> chains) {
		this.pisaInterf = pisaInterf;
		this.chains = chains;
		this.firstMolecule = pisaInterf.getFirstMolecule();
		this.secondMolecule = pisaInterf.getSecondMolecule();
	}

	public InterfaceScore scoreEntropy(double bsaToAsaSoftCutoff, double bsaToAsaHardCutoff, double relaxationStep, int minNumResidues, boolean weighted, int reducedAlphabet) {
		double rimEnt1 = Double.NaN;
		double coreEnt1 = Double.NaN;
		double rimEnt2 = Double.NaN;
		double coreEnt2 = Double.NaN;
		PisaRimCore rimCore1 = null;
		PisaRimCore rimCore2 = null;
		
		Map<Integer,PisaRimCore> rimcores = this.pisaInterf.getRimAndCore(bsaToAsaSoftCutoff, bsaToAsaHardCutoff, relaxationStep, minNumResidues);
		rimCore1 = rimcores.get(1);
		rimCore2 = rimcores.get(2);
		if (rimCore1 != null) {
			rimEnt1 = getEntropy(rimCore1.getRimResidues(), chains.get(0).getAlignment(), chains.get(0).getPdb(),weighted, reducedAlphabet);
			coreEnt1 = getEntropy(rimCore1.getCoreResidues(), chains.get(0).getAlignment(), chains.get(0).getPdb(),weighted, reducedAlphabet);
		}
		if (rimCore2 != null) {
			rimEnt2 = getEntropy(rimCore2.getRimResidues(), chains.get(1).getAlignment(), chains.get(1).getPdb(),weighted, reducedAlphabet);
			coreEnt2 = getEntropy(rimCore2.getCoreResidues(), chains.get(1).getAlignment(), chains.get(1).getPdb(),weighted, reducedAlphabet);
		}

		int totalCoreResidues = 0;
		if (firstMolecule.isProtein()) totalCoreResidues+=rimCore1.getCoreSize();
		if (secondMolecule.isProtein()) totalCoreResidues+=rimCore2.getCoreSize();
		if (totalCoreResidues<minNumResidues) {
			rimEnt1 = Double.NaN;
			coreEnt1 = Double.NaN;
			rimEnt2 = Double.NaN;
			coreEnt2 = Double.NaN;
		}
		
		return new InterfaceScore(rimCore1, rimCore2, coreEnt1, rimEnt1, coreEnt2, rimEnt2);
	}
	
	public double scoreCRK() {
		return 0;
	}
	
	private double getEntropy(List<PisaResidue> residues, MultipleSequenceAlignment aln, Pdb pdb, boolean weighted, int reducedAlphabet) {
		double totalEnt = 0.0;
		double totalWeight = 0.0;
		for (PisaResidue res:residues){
			int resSer = pdb.getResSerFromPdbResSer(res.getPdbResSer());
			double weight = 1.0;
			if (weighted) {
				weight = res.getBsa();
			}
			totalEnt += weight*(aln.getColumnEntropy(aln.seq2al(pdb.getPdbCode()+pdb.getPdbChainCode(), resSer),reducedAlphabet));
			totalWeight += weight;
		}
		return totalEnt/totalWeight;
	}
}
