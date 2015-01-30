package eppic.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureIO;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.asa.AsaCalculator;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.xtal.CrystalBuilder;
import org.biojava.nbio.structure.xtal.SpaceGroup;
import org.xml.sax.SAXException;

import eppic.EppicParams;
import eppic.commons.pisa.PisaConnection;
import eppic.commons.pisa.PisaInterfaceList;

/**
 * Script to print side by side a comparison of the interfaces calculated 
 * by PISA (automatically downloaded from the PISA web site as xml files) and
 * the interfaces calculated with Biojava's implementation.
 * 
 * @author duarte_j
 *
 */
public class CompToPisaInterf {
	
	private static final int NTHREADS = Runtime.getRuntime().availableProcessors(); // number of threads for ASA calculation

	
	public static void main (String[] args) {
		
		File listFile = new File(args[0]);
		
		Set<String> pdbCodesSet = new HashSet<String>();
		try {
		BufferedReader flist = new BufferedReader(new FileReader(listFile));
		String line;
		while ((line = flist.readLine() ) != null ) {
			if (line.startsWith("#")) continue;
			if (line.isEmpty()) break;
			String pdbCode = line.split("\\s+")[0].toLowerCase();
			pdbCodesSet.add(pdbCode);
		}
		flist.close();
		} catch(IOException e) {
			System.err.println("Couldn't read list file "+listFile);
			System.exit(1);
		}

		// we use the set just to eliminate duplicates, now we put it into a list
		List<String> pdbCodes = new ArrayList<String>();
		pdbCodes.addAll(pdbCodesSet);
		
		// getting PISA interfaces
		PisaConnection pc = new PisaConnection();
		System.out.println("Downloading PISA interfaces");
		Map<String, PisaInterfaceList> allPisaInterfaces = null;
		try {
			allPisaInterfaces = pc.getInterfacesDescription(pdbCodes);
		} catch (IOException e1) {
			System.err.println(e1.getMessage());
			System.exit(1);
		} catch (SAXException e1) {
			System.err.println(e1.getMessage());
			System.exit(1);
		}

		System.out.println("Computing interfaces...");

		AtomCache cache = new AtomCache();
		cache.setUseMmCif(true);
		StructureIO.setAtomCache(cache); 
		
		for (String pdbCode: pdbCodes) {
			
			Structure pdb = null;
			try {
				pdb = StructureIO.getStructure(pdbCode);
			} catch (IOException|StructureException e) {
				System.out.println("\nError. Couldn't load PDB "+pdbCode+". Error: "+e.getMessage());
				continue;
			} 
			
			
			System.out.println("##"+pdbCode);

			StructureInterfaceList pisaInterfaces = allPisaInterfaces.get(pdbCode).convertToChainInterfaceList(pdb);
			// we sort them on interface area because pisa doesn't always sort them like that (it does some kind of grouping)
			pisaInterfaces.sort();
			
			
			CrystalBuilder cb = new CrystalBuilder(pdb);
			
			StructureInterfaceList interfaces = cb.getUniqueInterfaces(EppicParams.INTERFACE_DIST_CUTOFF);
			
			interfaces.calcAsas(AsaCalculator.DEFAULT_N_SPHERE_POINTS, NTHREADS, -1);
			// not we use 0 as min interface area so that we compare to pisa better (since they keep all interfaces)
			interfaces.removeInterfacesBelowArea(0);

			
			System.out.print("Number of interfaces: ours "+interfaces.size()+", pisa "+pisaInterfaces.size());
			if (pisaInterfaces.size()!=interfaces.size()) {
				System.out.print("\t disagreement");
			}
			System.out.println();
			// TODO since the move to Biojava, we don't have poly/non-poly chains anymore so we can't do the following anymore:
			//for (Chain chain:pdb.getChains()) {
			//	System.out.print(chain.getChainCode()+": "+chain.getPdbChainCode());
			//	if (chain.isNonPolyChain()) {
			//		System.out.print("("+chain.getFirstResidue().getLongCode()+"-"+chain.getFirstResidue().getSerial()+")");
			//	}
			//	System.out.print(", ");
			//}
			System.out.println();
			
			for (int i=0;i<Math.max(interfaces.size(), pisaInterfaces.size());i++) {
				if (i<pisaInterfaces.size()) {
					StructureInterface pisaInterf = pisaInterfaces.get(i+1);
					String name = pisaInterf.getMoleculeIds().getFirst()+"+"+pisaInterf.getMoleculeIds().getSecond();
					System.out.printf("%2d\t%8.2f\t%20s\t%20s",i+1,pisaInterf.getTotalArea(),name,SpaceGroup.getAlgebraicFromMatrix(pisaInterf.getTransforms().getSecond().getMatTransform()));
				} else {
					System.out.printf("%2s\t%8s\t%20s\t%20s","","","","");
				}
				if (i<interfaces.size()) {
					StructureInterface ourInterf = interfaces.get(i+1);
					String name = ourInterf.getMoleculeIds().getFirst()+"+"+ourInterf.getMoleculeIds().getSecond();
					System.out.printf("\t%8.2f\t%20s\t%20s\n",ourInterf.getTotalArea(),name,SpaceGroup.getAlgebraicFromMatrix(ourInterf.getTransforms().getSecond().getMatTransform()));
				} else {
					System.out.printf("\t%8s\t%20s\t%20s\n","","","");
				}
				
			}

//			int i = 0;
//			for (int p=0;p<pisaInterfaces.size();p++) {
//				ChainInterface pisaInterf = pisaInterfaces.get(p);
//				if (!pisaInterf.isProtein()) continue;
//				System.out.println("\nInterface "+(i+1));
//				ChainInterface myInterf = interfaces.get(i);
//				
//				
//				System.out.printf("Areas, pisa: %8.2f\tmy: %8.2f\tdiff: %4.1f%%\n",pisaInterf.getInterfaceArea(),myInterf.getInterfaceArea(),
//						(pisaInterf.getInterfaceArea()-myInterf.getInterfaceArea())*100.0/pisaInterf.getInterfaceArea());
//			}
			
		}
	}
}
