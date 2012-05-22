package crk.predictors;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import crk.CRKParams;
import crk.CallType;

import edu.uci.ics.jung.graph.util.Pair;

import owl.core.structure.AaResidue;
import owl.core.structure.AminoAcid;
import owl.core.structure.Atom;
import owl.core.structure.ChainInterface;
import owl.core.structure.InterfaceRimCore;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbChain;
import owl.core.structure.Residue;
import owl.core.structure.graphs.AICGraph;

public class GeometryPredictor implements InterfaceTypePredictor {

	private static final double INTERF_DIST_CUTOFF = 5.9;
	
	private static final int FIRST = 0;
	private static final int SECOND = 1;
	
	private static final Log LOGGER = LogFactory.getLog(GeometryPredictor.class);
	
	private ChainInterface interf;
	private double bsaToAsaCutoff;
	private int minCoreSizeForBio;
	private CallType call;
	private String callReason;
	private List<String> warnings;
	
	public GeometryPredictor(ChainInterface interf) {
		this.interf = interf;
		warnings = new ArrayList<String>();
	}
	
	@Override
	public CallType getCall() {
		
		if (call!=null) return call;
		
		interf.calcRimAndCore(bsaToAsaCutoff);
		int size1 = interf.getFirstRimCore().getCoreSize();
		int size2 = interf.getSecondRimCore().getCoreSize();
		int size = size1+size2;				
		
		List<Pair<Atom>> interactingPairs = getNonpolyInteractingPairs();
		
		// this will happen when we read from PISA, beware that the cutoff is similar to PISA's but not necessarily the same
		if (interf.getAICGraph()==null) interf.calcAICGraph(INTERF_DIST_CUTOFF);

		// NOTE that we used to detect disulfide bridges here, but it is now moved to CombinedPredictor
		// as we also need to check in the reference alignment whether the bridge is wild-type or artifact
		
		// CALL
		if (size<minCoreSizeForBio) {
			callReason = "Total core size "+size+" below cutoff ("+minCoreSizeForBio+")";
			call = CallType.CRYSTAL;
		} 
		else {
			callReason = "Total core size "+size+" above cutoff ("+minCoreSizeForBio+")";
			call = CallType.BIO;
		}
		
		// WARNINGS
		// 1) clashes
		if (interf.hasClashes()) {
			List<Pair<Atom>> pairs = interf.getAICGraph().getClashingPairs();
			String warning = "";
			for (int i=0;i<pairs.size();i++) {
				Pair<Atom> pair = pairs.get(i);
				warning +=  getPairInteractionString(pair);
				if (i!=pairs.size()-1) warning+=", ";
			}
			
			warnings.add("Clashes found between: "+warning);
			LOGGER.warn("Interface "+interf.getId()+" has "+pairs.size()+" clashes: "+warning);
		} 
		// if no clashes then we report on any other kind of short distances
		// 2) hydrogen bonds -- commented out for now: 
		// our algorithm for finding them was depending on H atoms (which was bad anyway, because it was
		// ignoring H bonds in structures without H) and we now remove all Hs, in order to be consistent in surface calc, AICgraphs etc 
//		else if (interf.getAICGraph().hasHbonds()) {
//			List<Pair<Atom>> pairs = interf.getAICGraph().getHbondPairs();
//			String warning = pairs.size()+" Hydrogen bonds: ";
//			for (int i=0;i<pairs.size();i++) {
//				Pair<Atom> pair = pairs.get(i);
//				warning +=  getPairInteractionString(pair);
//				if (i!=pairs.size()-1) warning+=", ";
//			}
//			
//			warnings.add(warning);
//			LOGGER.warn("Interface "+interf.getId()+" has Hydrogen bonds: "+warning);			
//		}
		// 3) any other kind of close interaction
		else if (interf.getAICGraph().hasCloselyInteractingPairs()) {
			List<Pair<Atom>> pairs = interf.getAICGraph().getCloselyInteractingPairs();
			String warning = pairs.size()+" closely interacting atoms: ";
			for (int i=0;i<pairs.size();i++) {
				Pair<Atom> pair = pairs.get(i);
				warning +=  getPairInteractionString(pair);
				if (i!=pairs.size()-1) warning+=", ";
			}
			
			warnings.add(warning);
			LOGGER.warn("Interface "+interf.getId()+" has closely interacting atoms: "+warning);
		}
		// 4) checking whether either first or second member of interface are peptides
		checkForPeptides(FIRST);
		checkForPeptides(SECOND);
		// 5) if interactions mediated by a non-polymer are found we warn 
		// In some cases it can be a natural thing, we believe it is so for 2o3b (interface is small but strong because of the Mg2+)
		// but we think this are mostly artifacts of crystallization:
		// e.g. 1s1q interface 4: mediated by a Cu but it's a crystallization artifact. In this case area is very small and falls under hard limit
		// e.g. 2vis interfaces 5 and 8. Also very small area both		
		if (!interactingPairs.isEmpty()) {
			String warning = "Close interactions mediated by a non-polymer chain exist in interface. Between : ";
			for (int i=0;i<interactingPairs.size();i++) {
				Pair<Atom> pair = interactingPairs.get(i);
				// first atom is always from nonpoly chain, second atom from either poly chain of interface
				warning+=pair.getFirst().getParentResSerial()+
						"("+pair.getFirst().getCode()+ ") and "+
							pair.getSecond().getParentResidue().getParent().getPdbChainCode()+"-"+
							pair.getSecond().getParentResSerial()+"("+pair.getSecond().getParentResidue().getLongCode()+")-"+
							pair.getSecond().getCode()+
							" (dist: "+String.format("%3.1f",pair.getFirst().getCoords().distance(pair.getSecond().getCoords()))+")";
				if (i!=interactingPairs.size()-1) warning+=", ";
			}

			warnings.add(warning);
			LOGGER.warn("Interface "+interf.getId()+": "+warning);
		}
		
		return call;
	}
	
	@Override
	public String getCallReason() {
		return callReason;
	}
	
	@Override
	public List<String> getWarnings() {
		return warnings;
	}
	
	private String getPairInteractionString(Pair<Atom> pair) {
		return
		pair.getFirst().getParentResidue().getParent().getPdbChainCode()+"-"+
		pair.getFirst().getParentResSerial()+"("+pair.getFirst().getParentResidue().getLongCode()+")-"+
		pair.getFirst().getCode()+" and "+ 	
		pair.getSecond().getParentResidue().getParent().getPdbChainCode()+"-"+
		pair.getSecond().getParentResSerial()+"("+pair.getSecond().getParentResidue().getLongCode()+")-"+
		pair.getSecond().getCode()+
		" (dist: "+String.format("%3.1f",pair.getFirst().getCoords().distance(pair.getSecond().getCoords()))+")";
	}

	public void printScores(PrintStream ps) {
		
		CallType call = getCall();
		interf.printRimCoreInfo(ps);
		
		ps.println(call.getName()+"\t"+getCallReason());
		if (!getWarnings().isEmpty()) {
			ps.println("  Warnings: ");
			for (String warning:getWarnings()) {
				ps.println("     "+warning);
			}
		}
	}
	
	public static void printScoringHeaders(PrintStream ps) {
		ps.printf("%15s\t%6s\t","interface","area");
		ps.printf("%5s\t%5s\t%5s","size1", "size2","CA");
		ps.print("\t");
		ps.printf("%6s","call");
		ps.print("\t");
		ps.printf("%6s","reason");
		ps.println();
	}
	
	/**
	 * Writes out a PDB file with the 2 chains of this interface
	 * In order for the file to be handled properly by molecular viewers whenever the two
	 * chains have the same code we rename the second one to the next letter in alphabet.
	 * PDB chain codes are used for the output, not cif codes.  
	 * @param file
	 * @throws IOException
	 */
	public void writePdbFile(File file) throws IOException {
 
		if (interf.isFirstProtein() && interf.isSecondProtein()) {
			this.interf.writeToPdbFile(file);
		}
	}
	
	public void setBsaToAsaCutoff(double bsaToAsaCutoff) {
		this.bsaToAsaCutoff = bsaToAsaCutoff;
	}
	
	public void setMinCoreSizeForBio(int minCoreSizeForBio) {
		this.minCoreSizeForBio = minCoreSizeForBio;
	}
	
	public int countGlycines(InterfaceRimCore rimcore) {
		int count = 0;
		for (Residue res:rimcore.getCoreResidues()) {
			if (res instanceof AaResidue && (((AaResidue)res).getAaType()==AminoAcid.GLY)) {
				count++;
			}
		}
		return count;
	}

	private List<Pair<Atom>> getNonpolyInteractingPairs() {
		PdbChain firstChain = this.interf.getFirstMolecule();
		PdbChain secondChain = this.interf.getSecondMolecule();
		PdbAsymUnit firstAU = firstChain.getParent();
		PdbAsymUnit secondAU = secondChain.getParent();
		
		List<Pair<Atom>> interactingPairs = new ArrayList<Pair<Atom>>();
		interactingPairs.addAll(getNonPolyContacts(firstChain, secondChain, firstAU));
		if (firstAU!=secondAU) {
			// in the case they are identical, then both chains come from same AU, there's no transformation between them
			// we don't need to check the poly chains of the second one
			interactingPairs.addAll(getNonPolyContacts(firstChain, secondChain, secondAU));
		}
		return interactingPairs;
	}
	
	private List<Pair<Atom>> getNonPolyContacts(PdbChain firstChain, PdbChain secondChain, PdbAsymUnit au) {
		List<Pair<Atom>> pairs = new ArrayList<Pair<Atom>>();
		for (PdbChain nonpoly:au.getNonPolyChains()) {
			AICGraph graph1 = nonpoly.getAICGraph(firstChain, INTERF_DIST_CUTOFF);
			if (graph1.getEdgeCount()>0) {
				AICGraph graph2 = nonpoly.getAICGraph(secondChain, INTERF_DIST_CUTOFF);
				if (graph2.getEdgeCount()>0) {
					// the nonpoly chain is in contact with both firstChain and secondChain
					// we check for atoms within a narrow cutoff in both sides -> electrostatic/covalent interactions
					if (graph1.hasCloselyInteractingPairs() && graph2.hasCloselyInteractingPairs()) {
						pairs.addAll(graph1.getCloselyInteractingPairs());
						pairs.addAll(graph2.getCloselyInteractingPairs());
					}
				}
			}
		}
		return pairs;
	}

	private void checkForPeptides(int molecId) {
		// In most cases our predictions for peptides will be bad because not only the peptide but also the protein partner
		// will have too small an interface core and we'd call crystal based on core size
		// But still for some cases (e.g. 3bfw) the prediction is correct (the core size is big enough) 
		PdbChain molec = null;
		if (molecId==FIRST) {
			molec = interf.getFirstMolecule();
		} else if (molecId==SECOND) {
			molec = interf.getSecondMolecule();
		}
		if (molec.getFullLength()<=CRKParams.PEPTIDE_LENGTH_CUTOFF) {
			double bsa = interf.getInterfaceArea();
			String msg = "Ratio of interface area to ASA: "+
					String.format("%4.2f", bsa/molec.getASA())+". "+
					"Ratio of buried residues to total residues: "+
					String.format("%4.2f",(double)molec.getNumResiduesWithBsaAbove(0)/(double)molec.getObsLength());
			LOGGER.info("Chain "+molec.getPdbChainCode()+" of interface "+interf.getId()+" is a peptide. "+msg);
			warnings.add("Chain "+molec.getPdbChainCode()+" is a peptide.");
		}

	}

	public ChainInterface getInterface() {
		return this.interf;
	}
}
