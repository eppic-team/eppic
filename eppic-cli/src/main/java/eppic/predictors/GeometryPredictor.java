package eppic.predictors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.asa.GroupAsa;
import org.biojava.nbio.structure.contact.AtomContact;
import org.biojava.nbio.structure.contact.Pair;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.CallType;
import eppic.EppicParams;
import eppic.InterfaceEvolContext;

public class GeometryPredictor implements InterfaceTypePredictor {

	private static final Logger LOGGER = LoggerFactory.getLogger(GeometryPredictor.class);
	
	private StructureInterface interf;
	private double bsaToAsaCutoff;
	private double minAsaForSurface;
	private int minCoreSizeForBio;
	private CallType call;
	private String callReason;
	private List<String> warnings;
	
	private int size;
	
	private int size1;
	private int size2;
		
	public GeometryPredictor(StructureInterface interf) {
		this.interf = interf;
		this.warnings = new ArrayList<String>();
	}
	
	@Override
	public void computeScores() {
		
		generateWarnings();
		
		Pair<List<Group>> cores = interf.getCoreResidues(bsaToAsaCutoff, minAsaForSurface);
		size1 = cores.getFirst().size();
		size2 = cores.getSecond().size();
		size = size1+size2;
		
		
		// CALL
		if (size<minCoreSizeForBio) {
			callReason = "Total core size "+size+" below cutoff ("+minCoreSizeForBio+")";
			call = CallType.CRYSTAL;
		} 
		else {
			callReason = "Total core size "+size+" above cutoff ("+minCoreSizeForBio+")";
			call = CallType.BIO;
		}
		

	}
	
	@Override
	public CallType getCall() {
		
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
	
	@Override
	public double getScore() {
		return (double)size;
	}
	
	@Override
	public double getScore1() {
		return (double)size1;
	}
	
	@Override
	public double getScore2() {
		return (double)size2;
	}
	
	@Override
	public double getConfidence() {
		return CONFIDENCE_UNASSIGNED;
	}
	
	private void generateWarnings() {
		
		// TODO non-poly interacting stuff needs to be rewritten using Biojava
		//List<AtomContact> interactingPairs = getNonpolyInteractingPairs();
		
		// NOTE that we used to detect disulfide bridges here, but it is now moved to CombinedPredictor
		// as we also need to check in the reference alignment whether the bridge is wild-type or artifact

		List<AtomContact> clashes = interf.getContacts().getContactsWithinDistance(EppicParams.CLASH_DISTANCE);
		List<AtomContact> closeInteracting = 
				interf.getContacts().getContactsWithinDistance(EppicParams.CLOSE_INTERACTION_DIST); 

		
		// WARNINGS
		// 1) clashes
		if (!clashes.isEmpty()) {			
			StringBuilder warning = new StringBuilder();
			if (clashes.size()>EppicParams.MAX_NUM_CLASHES_TO_REPORT_WUI) {
				warning.append(clashes.size() + " clashes found");
			} else {
				for (int i=0;i<clashes.size();i++) {
					AtomContact pair = clashes.get(i);
					warning.append(getPairInteractionString(pair));
					if (i!=clashes.size()-1) warning.append(", ");
				}
			}
			
			warnings.add("Clashes found between: "+warning.toString());
			LOGGER.warn("Interface "+interf.getId()+" has "+clashes.size()+" clashes: "+warning.toString());
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
		else if (!closeInteracting.isEmpty()) {
			StringBuilder warning = new StringBuilder(closeInteracting.size()+" closely interacting atoms: ");
			for (int i=0;i<closeInteracting.size();i++) {
				AtomContact pair = closeInteracting.get(i);
				warning.append(getPairInteractionString(pair));
				if (i!=closeInteracting.size()-1) warning.append(", ");
			}
			
			warnings.add(warning.toString());
			LOGGER.warn("Interface "+interf.getId()+" has closely interacting atoms: "+warning.toString());
		}
		// 4) checking whether either first or second member of interface are peptides
		checkForPeptides(InterfaceEvolContext.FIRST);
		checkForPeptides(InterfaceEvolContext.SECOND);
		// 5) if interactions mediated by a non-polymer are found we warn 
		// In some cases it can be a natural thing, we believe it is so for 2o3b (interface is small but strong because of the Mg2+)
		// but we think this are mostly artifacts of crystallization:
		// e.g. 1s1q interface 4: mediated by a Cu but it's a crystallization artifact. In this case area is very small and falls under hard limit
		// e.g. 2vis interfaces 5 and 8. Also very small area both		
//		if (!interactingPairs.isEmpty()) {
//			String warning = "Close interactions mediated by a non-polymer chain exist in interface. Between : ";
//			for (int i=0;i<interactingPairs.size();i++) {
//				AtomContact pair = interactingPairs.get(i);
//				String firstResSer = null;
//				String secondResSer = null;
//				
//				firstResSer = pair.getPair().getFirst().getGroup().getResidueNumber().toString();	
//				secondResSer = pair.getPair().getSecond().getGroup().getResidueNumber().toString();
//				
//				// first atom is always from nonpoly chain, second atom from either poly chain of interface
//				warning+=firstResSer+
//						"("+pair.getPair().getFirst().getName()+ ") and "+
//							pair.getPair().getSecond().getGroup().getChainId()+"-"+
//							secondResSer+"("+pair.getPair().getSecond().getGroup().getPDBName()+")-"+
//							pair.getPair().getSecond().getName()+
//							" (distance: "+String.format("%3.1f A",Calc.getDistance(pair.getPair().getFirst(),pair.getPair().getSecond()) );
//				if (i!=interactingPairs.size()-1) warning+=", ";
//			}
//
//			warnings.add(warning);
//			LOGGER.warn("Interface "+interf.getId()+": "+warning);
//		}

	}
	
	private String getPairInteractionString(AtomContact pair) {
		String firstResSer = null;
		String secondResSer = null;
		
		firstResSer = pair.getPair().getFirst().getGroup().getResidueNumber().toString();	
		secondResSer = pair.getPair().getSecond().getGroup().getResidueNumber().toString();
		
		
		return
		pair.getPair().getFirst().getGroup().getChainId()+"-"+
		firstResSer+"("+pair.getPair().getFirst().getGroup().getPDBName()+")-"+
		pair.getPair().getFirst().getName()+" and "+ 	
		pair.getPair().getSecond().getGroup().getChainId()+"-"+
		secondResSer+"("+pair.getPair().getSecond().getGroup().getPDBName()+")-"+
		pair.getPair().getSecond().getName()+
		" (distance: "+String.format("%3.1f A)",Calc.getDistance(pair.getPair().getFirst(),pair.getPair().getSecond()) );
	}
	
	public void setBsaToAsaCutoff(double bsaToAsaCutoff) {
		this.bsaToAsaCutoff = bsaToAsaCutoff;
	}
	
	public void setMinAsaForSurface(double minAsaForSurface) {
		this.minAsaForSurface = minAsaForSurface;
	}
	
	public void setMinCoreSizeForBio(int minCoreSizeForBio) {
		this.minCoreSizeForBio = minCoreSizeForBio;
	}
		
//	private List<AtomContact> getNonpolyInteractingPairs() {
//		PdbChain firstChain = this.interf.getFirstMolecule();
//		PdbChain secondChain = this.interf.getSecondMolecule();
//		PdbAsymUnit firstAU = firstChain.getParent();
//		PdbAsymUnit secondAU = secondChain.getParent();
//		
//		List<Pair<Atom>> interactingPairs = new ArrayList<Pair<Atom>>();
//		interactingPairs.addAll(getNonPolyContacts(firstChain, secondChain, firstAU));
//		if (firstAU!=secondAU) {
//			// in the case they are identical, then both chains come from same AU, there's no transformation between them
//			// we don't need to check the poly chains of the second one
//			interactingPairs.addAll(getNonPolyContacts(firstChain, secondChain, secondAU));
//		}
//		return interactingPairs;
//	}
//	
//	private List<Pair<Atom>> getNonPolyContacts(PdbChain firstChain, PdbChain secondChain, PdbAsymUnit au) {
//		List<Pair<Atom>> pairs = new ArrayList<Pair<Atom>>();
//		for (PdbChain nonpoly:au.getNonPolyChains()) {
//			AICGraph graph1 = nonpoly.getAICGraph(firstChain, EppicParams.INTERFACE_DIST_CUTOFF);
//			if (graph1.getEdgeCount()>0) {
//				AICGraph graph2 = nonpoly.getAICGraph(secondChain, EppicParams.INTERFACE_DIST_CUTOFF);
//				if (graph2.getEdgeCount()>0) {
//					// the nonpoly chain is in contact with both firstChain and secondChain
//					// we check for atoms within a narrow cutoff in both sides -> electrostatic/covalent interactions
//					if (graph1.hasCloselyInteractingPairs() && graph2.hasCloselyInteractingPairs()) {
//						pairs.addAll(graph1.getCloselyInteractingPairs());
//						pairs.addAll(graph2.getCloselyInteractingPairs());
//					}
//				}
//			}
//		}
//		return pairs;
//	}

	private void checkForPeptides(int molecId) {
		// In most cases our predictions for peptides will be bad because not only the peptide but also the protein partner
		// will have too small an interface core and we'd call crystal based on core size
		// But still for some cases (e.g. 3bfw) the prediction is correct (the core size is big enough) 
		Collection<GroupAsa> groupAsas = null;
		String chainId = null;
		boolean isProt = InterfaceEvolContext.isProtein(interf, molecId);
		if (molecId==InterfaceEvolContext.FIRST) {
			groupAsas = interf.getFirstGroupAsas().values();
			chainId = interf.getMoleculeIds().getFirst();			
			
		} else if (molecId==InterfaceEvolContext.SECOND) {
			groupAsas = interf.getSecondGroupAsas().values();
			chainId = interf.getMoleculeIds().getSecond();
		}
		double asa = 0;
		int numResWitBsaAbove0 = 0;
		for (GroupAsa gAsa:groupAsas) {
			asa += gAsa.getAsaU();
			if (gAsa.getBsa()>0) numResWitBsaAbove0++;
		}
		
		
		// if the chain is not a protein we don't want to warna about peptides
		if (!isProt) return;
		
		
		if (groupAsas.size()<=EppicParams.PEPTIDE_LENGTH_CUTOFF) {
			double bsa = interf.getTotalArea();
			String msg = "Ratio of interface area to ASA: "+
					String.format("%4.2f", bsa/asa)+". "+
					"Ratio of buried residues to total residues: "+
					String.format("%4.2f",(double)numResWitBsaAbove0/(double)groupAsas.size());			
			LOGGER.info("Chain "+chainId+" of interface "+interf.getId()+" is a peptide. "+msg);
			warnings.add("Chain "+chainId+" is a peptide.");
		}

	}

}
