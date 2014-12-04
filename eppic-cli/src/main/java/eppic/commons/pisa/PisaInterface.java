package eppic.commons.pisa;

import java.io.PrintStream;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.ResidueNumber;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureTools;
import org.biojava.bio.structure.asa.GroupAsa;
import org.biojava.bio.structure.contact.Pair;
import org.biojava.bio.structure.contact.StructureInterface;
import org.biojava.bio.structure.xtal.CrystalTransform;


public class PisaInterface implements Comparable<PisaInterface> {

	private int id;						// note this is call NN in the PISA web tables
	private int type;					// note this is call id in the PISA web tables
	private double interfaceArea;
	private double solvEnergy;
	private double solvEnergyPvalue;
	
	private PisaMolecule firstMolecule;
	private PisaMolecule secondMolecule;
	
	// our own fields
	private int protprotId; 		// the serial number of this interface if only protein-protein interfaces are counted
	
	public PisaInterface() {
		
	}

	public void printTabular(PrintStream ps) {
		ps.print("# ");
		ps.printf("%d\t%d\t%9.2f\t%5.2f\t%4.2f\n",this.getId(),this.getType(),this.getInterfaceArea(),this.getSolvEnergy(),this.getSolvEnergyPvalue());
		ps.print("## ");
		this.getFirstMolecule().printTabular(ps);
		ps.print("## ");
		this.getSecondMolecule().printTabular(ps);
	}	
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the interfaceArea
	 */
	public double getInterfaceArea() {
		return interfaceArea;
	}

	/**
	 * @param interfaceArea the interfaceArea to set
	 */
	public void setInterfaceArea(double interfaceArea) {
		this.interfaceArea = interfaceArea;
	}

	/**
	 * @return the solvEnergy
	 */
	public double getSolvEnergy() {
		return solvEnergy;
	}

	/**
	 * @param solvEnergy the solvEnergy to set
	 */
	public void setSolvEnergy(double solvEnergy) {
		this.solvEnergy = solvEnergy;
	}

	/**
	 * @return the solvEnergyPvalue
	 */
	public double getSolvEnergyPvalue() {
		return solvEnergyPvalue;
	}

	/**
	 * @param solvEnergyPvalue the solvEnergyPvalue to set
	 */
	public void setSolvEnergyPvalue(double solvEnergyPvalue) {
		this.solvEnergyPvalue = solvEnergyPvalue;
	}

	/**
	 * @return the firstMolecule
	 */
	public PisaMolecule getFirstMolecule() {
		return firstMolecule;
	}

	/**
	 * @param firstMolecule the firstMolecule to set
	 */
	public void setFirstMolecule(PisaMolecule firstMolecule) {
		this.firstMolecule = firstMolecule;
	}

	/**
	 * @return the secondMolecule
	 */
	public PisaMolecule getSecondMolecule() {
		return secondMolecule;
	}

	/**
	 * @param secondMolecule the secondMolecule to set
	 */
	public void setSecondMolecule(PisaMolecule secondMolecule) {
		this.secondMolecule = secondMolecule;
	}
	
	/**
	 * Returns true if both members of this interface are proteins.
	 * @return
	 */
	public boolean isProtein() {
		return (firstMolecule.isProtein() && secondMolecule.isProtein());
	}

	/**
	 * Return the serial number of this interface if only protein-protein interfaces are
	 * counted. 1 would be the top prot-prot interface in the PISA list (usually biggest prot-prot interface) 
	 * @return
	 */
	public int getProtProtId() {
		return protprotId;
	}
	
	/**
	 * Sets the protprotId
	 * @param protprotId
	 */
	public void setProtProtId(int protprotId) {
		this.protprotId = protprotId;
	}
	
	@Override
	public int compareTo(PisaInterface o) {
		return new Double(this.getInterfaceArea()).compareTo(o.getInterfaceArea());
	}
	
	/**
	 * Converts a PisaInterface into our own ChainInterface which contains the full coordinates
	 * of the PDB entry with properly transformed chains.
	 * The chains read from the given pdb are deep-copied and then transformed so that they stay
	 * independent from the input pdb. 
	 * @param pdb the externally read PDB entry (cif file, pdb file, pdbase)
	 * @return
	 */
	public StructureInterface convertToChainInterface(Structure pdb) {
		StructureInterface interf = new StructureInterface();
		interf.setTotalArea(this.interfaceArea);
		interf.setId(this.id);		
		interf.setTransforms(new Pair<CrystalTransform>(firstMolecule.getTransf(),secondMolecule.getTransf()));
		interf.setMoleculeIds(new Pair<String>(firstMolecule.getChainId(),secondMolecule.getChainId())); 
		
		Chain pdb1 = (Chain)findChainForPisaMolecule(this.firstMolecule, pdb).clone();
		Calc.transform(pdb1, firstMolecule.getTransfOrth());
		Chain pdb2 = (Chain)findChainForPisaMolecule(this.secondMolecule, pdb).clone();
		Calc.transform(pdb2, secondMolecule.getTransfOrth());
		
		setGroupAsas(firstMolecule, secondMolecule, interf, pdb1, pdb2);
		
		interf.setMolecules(new Pair<Atom[]>(StructureTools.getAllAtomArray(pdb1),StructureTools.getAllAtomArray(pdb2)));  
		
		return interf;
	}
	
	/**
	 * Sets the asa/bsa values of the passed StructureInterface object to the ones 
	 * present in the passed PisaMolecules
	 * @param pdb
	 */
	private static void setGroupAsas (PisaMolecule firstMolecule, PisaMolecule secondMolecule, 
			StructureInterface interf, Chain chain1, Chain chain2) {
		for (PisaResidue pisaRes:firstMolecule) {
			String fullResNum = pisaRes.getPdbResSer();
			int num = -100000000;
			char insCode = 0;

			try {
				if (Character.isAlphabetic(fullResNum.charAt(fullResNum.length()-1))) {
					num = Integer.parseInt(fullResNum.substring(0, fullResNum.length())); 
					insCode = fullResNum.charAt(fullResNum.length()-1);
				} else {
					num = Integer.parseInt(fullResNum);
					insCode = 0;
				}
			} catch (NumberFormatException e) {
				System.err.println("Error! number format exception while converting PISA residue number "+fullResNum);
				continue;
			}
			// TODO check that insCode=0 is right as default insCode
			ResidueNumber resNumber = new ResidueNumber(chain1.getChainID(), num, insCode);
			Group g = null;
			try {
				g = chain1.getGroupByPDB(resNumber);
			} catch (StructureException e) {
				System.err.println("Could not find group for residue number "+resNumber.toString()+" (coming from PISA residue number "+fullResNum+")");
				continue;
			}
			GroupAsa groupAsa = new GroupAsa(g); 
			groupAsa.setAsaU(pisaRes.getAsa());
			groupAsa.setAsaC(pisaRes.getAsa()-pisaRes.getBsa()); 
			interf.setFirstGroupAsa(groupAsa);
		}
		
		for (PisaResidue pisaRes:secondMolecule) {
			String fullResNum = pisaRes.getPdbResSer();
			int num = -100000000;
			char insCode = 0;

			try {
				if (Character.isAlphabetic(fullResNum.charAt(fullResNum.length()-1))) {
					num = Integer.parseInt(fullResNum.substring(0, fullResNum.length())); 
					insCode = fullResNum.charAt(fullResNum.length()-1);
				} else {
					num = Integer.parseInt(fullResNum);
					insCode = 0;
				}
			} catch (NumberFormatException e) {
				System.err.println("Error! number format exception while converting PISA residue number "+fullResNum);
				continue;
			}
			// TODO check that insCode=0 is right as default insCode
			ResidueNumber resNumber = new ResidueNumber(chain2.getChainID(), num, insCode);
			Group g = null;
			try {
				g = chain2.getGroupByPDB(resNumber);
			} catch (StructureException e) {
				System.err.println("Could not find group for residue number "+resNumber.toString()+" (coming from PISA residue number "+fullResNum+")");
				continue;
			}
			GroupAsa groupAsa = new GroupAsa(g); 
			groupAsa.setAsaU(pisaRes.getAsa());
			groupAsa.setAsaC(pisaRes.getAsa()-pisaRes.getBsa()); 
			interf.setSecondGroupAsa(groupAsa);

		}
	}
	
	/**
	 * For a given PisaMolecule and a corresponding PdbAsymUnit it finds what is the 
	 * PdbChain corresponding to the PisaMolecule: if is a protein/nucleotide chain it is straight 
	 * forward from the PDB chain code, but if it is a non-polymer chain then it has to find the corresponding
	 * chain by matching the 3 parts of the PISA identifier (PDB chain code, residue type and PDB residue serial). 
	 * @param molecule
	 * @param pdb the chain or null if nothing found (a warning is printed as well)
	 * @return
	 */
	private Chain findChainForPisaMolecule(PisaMolecule molecule, Structure pdb) {
		if (molecule.isProtein()) {
			try {
				return pdb.getChainByPDB(molecule.getChainId());
			} catch (StructureException e) {
				System.err.println("Could not find chain for PISA chain "+molecule.getChainId());
				return null;
			}
		}
		// TODO what to do with the non-polymeric chains from PISA????
//		String pisaNonPolyChainId = molecule.getChainId();
//		Pattern p = Pattern.compile("^\\[(\\w+)\\](\\w):(\\d+)$");
//		Matcher m = p.matcher(pisaNonPolyChainId);
//		String resCode = null;
//		String chain = null;
//		String pdbResSerial = null;
//		if (m.matches()){
//			resCode = m.group(1).trim();
//			chain = m.group(2);
//			pdbResSerial = m.group(3);
//		}
//		Chain selectedChain = null; // here we put the chain that we think matches the pisa non-poly chain identifier 
//		for (Chain nonPolyChain: pdb.getNonPolyChains()){
//			if (nonPolyChain.getPdbChainCode().equals(chain)) {
//				for (Residue res:nonPolyChain) {
//					if (res.getLongCode().equals(resCode) && res.getPdbSerial().equals(pdbResSerial)) {
//						selectedChain = nonPolyChain;
//						break;
//					}
//				}
//			}
//		}
//		if (selectedChain==null) System.err.println("Warning! couldn't find a corresponding non-polymer chain for PISA identifier "+pisaNonPolyChainId);
//		return selectedChain;
		System.err.println("Encountered PISA non-polymer chain "+molecule.getChainId()+
				". Conversion of PISA non-polymer chains not supported yet!");
		return null;
	}
}
