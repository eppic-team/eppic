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

import org.xml.sax.SAXException;

import eppic.EppicParams;
import owl.core.connections.pisa.PisaConnection;
import owl.core.connections.pisa.PisaInterfaceList;
import owl.core.structure.AsaCalculator;
import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbChain;
import owl.core.structure.PdbLoadException;
import owl.core.structure.SpaceGroup;
import owl.core.util.FileFormatException;

/**
 * Script to print side by side a comparison of the interfaces calculated 
 * by PISA (automatically downloaded from the PISA web site as xml files) and
 * the interfaces calculated with owl's implementation.
 * 
 * @author duarte_j
 *
 */
public class CompToPisaInterf {
	
	private static final String   LOCAL_CIF_DIR = "/nfs/data/dbs/pdb/data/structures/all/mmCIF";

	private static final int NTHREADS = Runtime.getRuntime().availableProcessors(); // number of threads for ASA calculation

	private static final String TMPDIR = System.getProperty("java.io.tmpdir");

	
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
		
		for (String pdbCode: pdbCodes) {
		
			
			PdbAsymUnit pdb = null;
			try {
				File cifFile = new File(TMPDIR,pdbCode+".cif");
				PdbAsymUnit.grabCifFile(LOCAL_CIF_DIR, null, pdbCode, cifFile, false);
				pdb = new PdbAsymUnit(cifFile);
				cifFile.delete();
			} catch (IOException e) {
				System.err.println("Problems reading cif file for "+pdbCode+". Error: "+e.getMessage());
				continue;
			} catch (PdbLoadException e) {
				System.err.println("PDB load error, cause: "+e.getMessage());
				continue;
			} catch (FileFormatException e) {
				System.err.println("File format error: "+e.getMessage());
				continue;
			}

			
			System.out.println("##"+pdbCode);

			ChainInterfaceList pisaInterfaces = allPisaInterfaces.get(pdbCode).convertToChainInterfaceList(pdb);
			// we sort them on interface area because pisa doesn't always sort them like that (it does some kind of grouping)
			pisaInterfaces.sort();
			
			ChainInterfaceList interfaces = null;

			// not we use 0 as min interface area so that we compare to pisa better (since they keep all interfaces)
			interfaces = pdb.getAllInterfaces(EppicParams.INTERFACE_DIST_CUTOFF, AsaCalculator.DEFAULT_N_SPHERE_POINTS, NTHREADS, true, true, -1, 0);

			System.out.print("Number of interfaces: ours "+interfaces.size()+", pisa "+pisaInterfaces.getNumInterfaces());
			if (pisaInterfaces.getNumInterfaces()!=interfaces.size()) {
				System.out.print("\t disagreement");
			}
			System.out.println();
			for (PdbChain chain:pdb.getAllChains()) {
				System.out.print(chain.getChainCode()+": "+chain.getPdbChainCode());
				if (chain.isNonPolyChain()) {
					System.out.print("("+chain.getFirstResidue().getLongCode()+"-"+chain.getFirstResidue().getSerial()+")");
				}
				System.out.print(", ");
			}
			System.out.println();
			
			for (int i=0;i<Math.max(interfaces.size(), pisaInterfaces.size());i++) {
				if (i<pisaInterfaces.size()) {
					ChainInterface pisaInterf = pisaInterfaces.get(i+1);
					String name = pisaInterf.getFirstMolecule().getChainCode()+"+"+pisaInterf.getSecondMolecule().getChainCode();
					System.out.printf("%2d\t%8.2f\t%20s\t%20s",i+1,pisaInterf.getInterfaceArea(),name,SpaceGroup.getAlgebraicFromMatrix(pisaInterf.getSecondTransf().getMatTransform()));
				} else {
					System.out.printf("%2s\t%8s\t%20s\t%20s","","","","");
				}
				if (i<interfaces.size()) {
					ChainInterface ourInterf = interfaces.get(i+1);
					String name = ourInterf.getFirstMolecule().getChainCode()+"+"+ourInterf.getSecondMolecule().getChainCode();
					System.out.printf("\t%8.2f\t%20s\t%20s\n",ourInterf.getInterfaceArea(),name,SpaceGroup.getAlgebraicFromMatrix(ourInterf.getSecondTransf().getMatTransform()));
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
