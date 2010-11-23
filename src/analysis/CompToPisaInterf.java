package analysis;

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

import owl.core.connections.pisa.PisaConnection;
import owl.core.structure.Asa;
import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbLoadError;
import owl.core.structure.SpaceGroup;
import owl.core.util.FileFormatError;

public class CompToPisaInterf {
	
	private static final String   LOCAL_CIF_DIR = "/nfs/data/dbs/pdb/data/structures/all/mmCIF";

	private static final String PISA_INTERFACES_URL = "http://www.ebi.ac.uk/msd-srv/pisa/cgi-bin/interfaces.pisa?";
	
	private static final double CUTOFF = 5.9;
	
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
		PisaConnection pc = new PisaConnection(PISA_INTERFACES_URL, null, null);
		System.out.println("Downloading PISA interfaces");
		Map<String, ChainInterfaceList> all = null;
		try {
			all = pc.getInterfacesDescription(pdbCodes);
		} catch (IOException e1) {
			System.err.println(e1.getMessage());
			System.exit(1);
		} catch (SAXException e1) {
			System.err.println(e1.getMessage());
			System.exit(1);
		}

		System.out.println("Computing interfaces...");
		
		for (String pdbCode: pdbCodes) {
		
			ChainInterfaceList pisaInterfaces = all.get(pdbCode);
			// we sort them on interface area because pisa doesn't always sort them like that (it does some kind of grouping)
			pisaInterfaces.sort();
			
			PdbAsymUnit pdb = null;
			try {
				File cifFile = new File(TMPDIR,pdbCode+".cif");
				PdbAsymUnit.grabCifFile(LOCAL_CIF_DIR, null, pdbCode, cifFile, false);
				pdb = new PdbAsymUnit(cifFile);
			} catch (IOException e) {
				System.err.println("Problems reading cif file for "+pdbCode+". Error: "+e.getMessage());
				continue;
			} catch (PdbLoadError e) {
				System.err.println("PDB load error, cause: "+e.getMessage());
				continue;
			} catch (FileFormatError e) {
				System.err.println("File format error: "+e.getMessage());
				continue;
			}

			System.out.println("##"+pdbCode);

			ChainInterfaceList interfaces = null;
			try {
				interfaces = pdb.getAllInterfaces(CUTOFF, null, Asa.DEFAULT_N_SPHERE_POINTS, NTHREADS);
			} catch (IOException e) {
				// do nothing. Won't happen here as we don't use naccess
			}

			System.out.print("Number of interfaces: ours "+interfaces.size()+", pisa "+pisaInterfaces.getNumProtProtInterfaces());
			if (pisaInterfaces.getNumProtProtInterfaces()!=interfaces.size()) {
				System.out.print("\t disagreement");
			}
			System.out.println();

			for (int i=0;i<Math.max(interfaces.size(), pisaInterfaces.size());i++) {
				if (i<pisaInterfaces.size()) {
					ChainInterface pisaInterf = pisaInterfaces.get(i);
					System.out.printf("%2d\t%8.2f\t%20s\t%20s",i+1,pisaInterf.getInterfaceArea(),pisaInterf.getName(),SpaceGroup.getAlgebraicFromMatrix(pisaInterf.getSecondTransf()));
				} else {
					System.out.printf("%2s\t%8s\t%20s\t%20s","","","","");
				}
				if (i<interfaces.size()) {
					ChainInterface ourInterf = interfaces.get(i);
					System.out.printf("\t%8.2f\t%20s\t%20s\n",ourInterf.getInterfaceArea(),ourInterf.getName(),SpaceGroup.getAlgebraicFromMatrix(ourInterf.getSecondTransf()));
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
