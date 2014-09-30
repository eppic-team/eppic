package eppic.tools;
import gnu.getopt.Getopt;

import java.io.File;
import java.util.Set;

import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.InterfaceGraph;
import owl.core.structure.SpaceGroup;
import owl.core.util.Goodies;


public class EnumerateAssemblies {

	private static final double BSATOASA_CUTOFF = 0.95;
	private static final double MIN_ASA_FOR_SURFACE = 5;

	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		
		String help = 
			"Usage: \n" +
			"enumerateAssemblies \n" +
			" -i <file> : input interfaces.dat binary file containing the list of interfaces of a PDB structure\n\n";
		
		File inputFile = null;

		Getopt g = new Getopt("enumerateAssemblies", args, "i:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'i':
				inputFile = new File(g.getOptarg());
				break;
			case 'h':
				System.out.println(help);
				System.exit(0);
				break;
			case '?':
				System.err.println(help);
				System.exit(1);
				break; // getopt() already printed an error
			}
		}

		if (inputFile == null) {
			System.err.println("Missing input file (-i)");
			System.err.println(help);
			System.exit(1);
		}
		
		if (inputFile!=null && !inputFile.exists()){
			System.err.println("Given file "+inputFile+" does not exist!");
			System.exit(1);
		}
		
		ChainInterfaceList interfs = (ChainInterfaceList) Goodies.readFromFile(inputFile);
		
		SpaceGroup sg = interfs.get(1).getFirstMolecule().getParent().getSpaceGroup();
		System.out.println(sg.getShortSymbol()+"("+sg.getId()+")"+" - "+sg.getNumOperators()+" operators");
		
		// first we print out the interfaces
		for (int i=0;i<interfs.size();i++) {
			ChainInterface interf = interfs.get(i+1);
			interf.calcRimAndCore(BSATOASA_CUTOFF, MIN_ASA_FOR_SURFACE);
			String infiniteStr = "";
			if (interf.isInfinite()) infiniteStr = " -- INFINITE interface";
			System.out.println("\n##Interface "+(i+1)+" "+
					interf.getFirstSubunitId()+"-"+
					interf.getSecondSubunitId()+infiniteStr);
			if (interf.hasClashes()) System.out.println("CLASHES!!!");
			System.out.println("Transf1: "+SpaceGroup.getAlgebraicFromMatrix(interf.getFirstTransf().getMatTransform())+
					". Transf2: "+SpaceGroup.getAlgebraicFromMatrix(interf.getSecondTransf().getMatTransform()));
			System.out.println(interf.getFirstMolecule().getChainCode()+" - "+interf.getSecondMolecule().getChainCode());
			System.out.println("Number of contacts: "+interf.getNumContacts());
			System.out.println("Number of contacting atoms (from both molecules): "+interf.getNumAtomsInContact());
			System.out.println("Number of core residues at "+String.format("%4.2f", BSATOASA_CUTOFF)+
					" bsa to asa cutoff: "+interf.getFirstRimCore().getCoreSize()+" "+interf.getSecondRimCore().getCoreSize());
			System.out.printf("Interface area: %8.2f\n",interf.getInterfaceArea());
		}
		
		// then come the assemblies
		System.out.println();
		System.out.println("ASSEMBLIES\n");
		Set<InterfaceGraph> assemblies = interfs.getInterfacesGraph().getAllAssemblies();
		
		for (InterfaceGraph assembly:assemblies) {
			System.out.println("Assembly: members "+assembly+" - oligomeric state: "+assembly.getOligomericState()+" ");//+assembly.getSubunits());
			//System.out.println(" distinct nodes: "+ass.getSubunits());
		}
		
		
	}

}
