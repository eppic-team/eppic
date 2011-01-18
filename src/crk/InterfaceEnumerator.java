package crk;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import owl.core.connections.pisa.PisaConnection;
import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.SpaceGroup;

public class InterfaceEnumerator {
	
	private static final Log LOGGER = LogFactory.getLog(InterfaceEnumerator.class);
	
	private ChainInterfaceList interfaces; //cached interfaceList
	
	public InterfaceEnumerator() {
		
	}

	public ChainInterfaceList enumerate(PdbAsymUnit pdb, boolean usePisa, boolean useNaccess, String pisaInterfacesUrl, 
			double interfaceDistCutoff, File naccessExe, int nSpherePointsASAcalc, int numThreads) {
		interfaces = null;
		if (usePisa) {
			System.out.println("Getting PISA interfaces...");
			LOGGER.info("Interfaces from PISA.");
			PisaConnection pc = new PisaConnection(pisaInterfacesUrl, null, null);
			List<String> pdbCodes = new ArrayList<String>();
			pdbCodes.add(pdb.getPdbCode());
			try {
				interfaces = pc.getInterfacesDescription(pdbCodes).get(pdb.getPdbCode()).convertToChainInterfaceList(pdb);
			} catch (SAXException e) {
				LOGGER.fatal("Error while reading PISA xml file");
				LOGGER.fatal(e.getMessage());
				System.err.println("Error while reading PISA xml file");
				System.exit(1);
			} catch (IOException e) {
				LOGGER.fatal("Error while retrieving PISA xml file: "+e.getMessage());
				System.exit(1);
			}
		} else {
			System.out.println("Calculating possible interfaces...");
			if (useNaccess) {
				try {
					interfaces = pdb.getAllInterfaces(interfaceDistCutoff, naccessExe, 0, 0);
				} catch(IOException e) {
					LOGGER.fatal("Couldn't run NACCESS: "+e.getMessage());
					System.exit(1);
				}
				LOGGER.info("Interfaces calculated with NACCESS.");
			} else {
				try {
					interfaces = pdb.getAllInterfaces(interfaceDistCutoff, null, nSpherePointsASAcalc, numThreads);
				} catch(IOException e) {
					// do nothing, no IOException thrown when naccess not used
				}
				LOGGER.info("Interfaces calculated with "+nSpherePointsASAcalc+" sphere points.");
			}
		}
		
		System.out.println("Done");
		
		
		return interfaces;

	}
	
	public void checkForClashes(boolean usePisa, double interChainAtomClashDistance) {
		// checking for clashes
		if (!usePisa && interfaces.hasInterfacesWithClashes(interChainAtomClashDistance)) {				
			LOGGER.error("Clashes found in some of the interfaces (atoms distance below "+interChainAtomClashDistance+"):");
			List<ChainInterface> clashyInterfs = interfaces.getInterfacesWithClashes(interChainAtomClashDistance);
			for (ChainInterface clashyInterf:clashyInterfs) {
				LOGGER.error("Interface: "+clashyInterf.getFirstMolecule().getPdbChainCode()+"+"
						+clashyInterf.getSecondMolecule().getPdbChainCode()+" ("+
						SpaceGroup.getAlgebraicFromMatrix(clashyInterf.getSecondTransf())+
						") Clashes: "+clashyInterf.getNumClashes(interChainAtomClashDistance));
			}
			LOGGER.error("Clashes found in some of the interfaces. This is most likely an error in the structure. If you think the structure is correct, please report a bug.");
			System.err.println("Clashes found in some of the interfaces. This is most likely an error in the structure. If you think the structure is correct, please report a bug.");
			System.exit(1);
		}
	}
	
	public void calcRimAndCores(boolean zooming, double bsaToAsaSoftCutoff, double bsaToAsaHardCutoff, double relaxationStep, int minNumResCA, double[] cutoffsCA) {
		if (zooming) {
			interfaces.calcRimAndCores(bsaToAsaSoftCutoff, bsaToAsaHardCutoff, relaxationStep, minNumResCA);
		} else {
			interfaces.calcRimAndCores(cutoffsCA);
		}

	}
	
	public void logToFile(File outDir, String baseName,String pdbName) {
		try {
			PrintStream interfLogPS = new PrintStream(new File(outDir,baseName+".interfaces"));
			interfaces.printTabular(interfLogPS, pdbName);
			interfLogPS.close();
		} catch (IOException e) {
			LOGGER.error("Couldn't log interfaces description to file: "+e.getMessage());
		}
	}

	public void checkNotEmptyListAboveArea (double minInterfAreaReporting) {
		if (interfaces.getNumInterfacesAboveArea(minInterfAreaReporting)==0) {
			LOGGER.info(String.format("No interfaces with area above %4.0f. Nothing to score.\n",minInterfAreaReporting));
			System.out.printf("No interfaces with area above %4.0f. Nothing to score.\n",minInterfAreaReporting);
			System.exit(0);
		}

	}
	
}
