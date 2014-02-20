package eppic.tools;
import eppic.PymolRunner;
import gnu.getopt.Getopt;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import owl.core.structure.ChainCluster;
import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.InterfaceCluster;
import owl.core.structure.InterfacesFinder;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbChain;
import owl.core.structure.SpaceGroup;
import owl.core.util.GeometryTools;
import owl.core.util.Goodies;
import owl.core.util.OptSuperposition;


public class EnumerateInterfaces {

	private static final String LOCAL_CIF_DIR = "/nfs/data/dbs/pdb/data/structures/all/mmCIF";
	private static final String BASENAME = "interf_enum";
	private static final String TMPDIR = System.getProperty("java.io.tmpdir");
	private static final File   PYMOL_EXE = new File("/usr/bin/pymol");
	
	private static final double BSATOASA_CUTOFF = 0.95;
	private static final double MIN_ASA_FOR_SURFACE = 5;
	private static final int CONSIDER_COFACTORS = 40; // minimum number of atoms for a cofactor to be considered, if -1 all ignored
	
	private static final Pattern  PDBCODE_PATTERN = Pattern.compile("^\\d\\w\\w\\w$");
	
	
	
	// 6.0 seems to be PISA's cutoff, found for structure 1pmm where with 5.5 there is one interface (tiny, 1 atom contacting) missing
	// 5.0  gives 25 for 1pmo (right number) but 6.0 gives one more (26) for which NACCESS measures a negative area...
	// what's the cutoff then? I'm trying a value in between but it seems strange to choose such fractional values
	// 5.75 gives 25 for 1pmo (right) but 26 for 1pmm (instead of 27)
	// 5.90 gives 25 for 1pmo (right)  and 27 for 1pmm (right)
	// Beware in any case that the new default in eppic is 5.5
	private static final double CUTOFF = 5.5; 
	
	private static final int N_SPHERE_POINTS = 3000;
	
	private static final double MIN_AREA_TO_KEEP = 35;
	
	private static final double CLUSTERING_CUTOFF = 2.0;
	private static final int MINATOMS_CLUSTERING = 10;
	
	private static final int NTHREADS = Runtime.getRuntime().availableProcessors();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		
		String help = 
			"Usage: \n" +
			"enumerateInterfaces \n" +
			" -i <string> : input pdb code\n" +
			" [-t <int>]  : number of threads for calculating ASAs. Default: "+NTHREADS + "\n"+
			" [-w <dir>]  : output dir to write PDB files for each interface \n" +
			" [-l]        : cartoon PNG images of each interface will be written to\n" +
			"               output dir given in -w \n" +
			" [-s]        : write a serialized interfaces.dat file to output dir given\n" +
			"               in -w\n" +
			" [-n]        : non-polymer chains will also be considered\n" +
			" [-r]        : don't use redundancy elimination\n" +
			" [-d]        : more verbose output for debugging\n\n";
		
		String pdbStr = null;
		File writeDir = null;
		int nThreads = NTHREADS;
		boolean generatePngs = false;
		boolean debug = false;
		boolean serialize = false;
		boolean nonPoly = false;
		boolean withRedundancyElimination = true;

		Getopt g = new Getopt("enumerateInterfaces", args, "i:w:t:lsnrdh?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 'i':
				pdbStr = g.getOptarg();
				break;
			case 'w':
				writeDir = new File(g.getOptarg());
				break;
			case 't':
				nThreads = Integer.parseInt(g.getOptarg());
				break;
			case 'l':
				generatePngs = true;
				break;
			case 's':
				serialize = true;
				break;
			case 'n':
				nonPoly = true;
				break;
			case 'r':
				withRedundancyElimination = false;
				break;
			case 'd':
				debug = true;
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

		if (pdbStr == null) {
			System.err.println("Missing input PDB code/file (-i)");
			System.err.println(help);
			System.exit(1);
		}
		
		File inputFile = new File(pdbStr);
		Matcher m = PDBCODE_PATTERN.matcher(pdbStr);
		if (m.matches()) {
			inputFile = null;
		}
		
		if (inputFile!=null && !inputFile.exists()){
			System.err.println("Given file "+inputFile+" does not exist!");
			System.exit(1);
		}
		
		if (generatePngs==true && writeDir==null) {
			System.err.println("Can't generate images if a write directory not specified (use -w)");
			System.exit(1);
		}

		String outBaseName = pdbStr;
		
		PdbAsymUnit pdb = null;
		if (inputFile==null) {
			inputFile = new File(TMPDIR,BASENAME+"_"+pdbStr+".cif");
			PdbAsymUnit.grabCifFile(LOCAL_CIF_DIR, null, pdbStr, inputFile, false);
			pdb = new PdbAsymUnit(inputFile);
		} else {
			pdb = new PdbAsymUnit(inputFile);
			outBaseName = inputFile.getName().substring(0, inputFile.getName().lastIndexOf("."));
		}

		// we remove H atoms
		pdb.removeHatoms();
		
		System.out.println(pdb.getPdbCode()+" - "+pdb.getNumPolyChains()+" polymer chains ("+pdb.getProtChainClusters().size()+" sequence unique), " +
				pdb.getNumNonPolyChains()+" non-polymer chains.");

		for (ChainCluster chainCluster:pdb.getProtChainClusters()) {
			System.out.println(chainCluster.getClusterString());
		}
		System.out.println("Polymer chains: ");
		for (PdbChain chain:pdb.getPolyChains()) {
			System.out.println(chain.getChainCode()+"("+chain.getPdbChainCode()+")");
		}
		System.out.println("Non-polymer chains: ");
		for (PdbChain chain:pdb.getNonPolyChains()) {
			System.out.println(chain.getChainCode()+"("+chain.getPdbChainCode()+") "+" residues: "+chain.getObsLength()+
					" ("+chain.getFirstResidue().getLongCode()+"-"+chain.getFirstResidue().getSerial()+")");
		}
		
		System.out.println(pdb.getSpaceGroup().getShortSymbol()+" ("+pdb.getSpaceGroup().getId()+")");
		if (debug) System.out.println("Symmetry operators: "+pdb.getSpaceGroup().getNumOperators());
		
		System.out.println("Calculating possible interfaces... (using "+nThreads+" CPUs for ASA calculation)");
		long start = System.currentTimeMillis();
		InterfacesFinder interfFinder = new InterfacesFinder(pdb);
		interfFinder.setDebug(debug);
		interfFinder.setWithRedundancyElimination(withRedundancyElimination);

		ChainInterfaceList interfaces = interfFinder.getAllInterfaces(CUTOFF, N_SPHERE_POINTS, nThreads, true, nonPoly, CONSIDER_COFACTORS, MIN_AREA_TO_KEEP);
		
		interfaces.initialiseClusters(pdb, CLUSTERING_CUTOFF, MINATOMS_CLUSTERING, "CA");
		
		long end = System.currentTimeMillis();
		long total = (end-start)/1000;
		System.out.println("Total time for interface calculation: "+total+"s");
		
		System.out.println("Total number of interfaces found: "+interfaces.size());

		PymolRunner pr = new PymolRunner(PYMOL_EXE);
		File[] interfPdbFiles = new File[interfaces.size()];
					
		for (int i=0;i<interfaces.size();i++) {
			ChainInterface interf = interfaces.get(i+1);
			interf.calcRimAndCore(BSATOASA_CUTOFF, MIN_ASA_FOR_SURFACE);
			
			String infiniteStr = "";
			if (interf.isInfinite()) infiniteStr = " -- INFINITE interface";
			System.out.println("\n##Interface "+(i+1)+" "+
					interf.getFirstSubunitId()+"-"+
					interf.getSecondSubunitId()+infiniteStr);
			if (interf.hasClashes()) System.out.println("CLASHES!!!");
			
			
			System.out.println("Transf1: "+SpaceGroup.getAlgebraicFromMatrix(interf.getFirstTransf().getMatTransform())+
					". Transf2: "+SpaceGroup.getAlgebraicFromMatrix(interf.getSecondTransf().getMatTransform()));
	 		
			int foldType = pdb.getSpaceGroup().getAxisFoldType(interf.getSecondTransf().getTransformId());
			AxisAngle4d axisAngle = pdb.getSpaceGroup().getRotAxisAngle(interf.getSecondTransf().getTransformId());
			
			String screwStr = "";
			if (interf.getSecondTransf().getTransformType().isScrew()) {
				Vector3d screwTransl = 
						interf.getSecondTransf().getTranslScrewComponent();
				screwStr = " -- "+interf.getSecondTransf().getTransformType().getShortName()+" with translation "+
				String.format("(%5.2f,%5.2f,%5.2f)",screwTransl.x,screwTransl.y,screwTransl.z);

			}
			//if (pdb.getSpaceGroup().isScrewRotation(interf.getSecondTransf().getTransformId())) {
			//	Vector3d screwTransl = pdb.getSpaceGroup().getTranslScrewComponent(interf.getSecondTransf().getTransformId());
			//	screwStr = " -- "+interf.getSecondTransf().getTransformType().getShortName()+" screw with translation "+
			//			String.format("(%5.2f,%5.2f,%5.2f)",screwTransl.x,screwTransl.y,screwTransl.z);
			//}
			
			System.out.println(" "+foldType+"-fold on axis "+String.format("(%5.2f,%5.2f,%5.2f)",axisAngle.x,axisAngle.y,axisAngle.z)+screwStr);
			
			System.out.println("Number of contacts: "+interf.getNumContacts());
			System.out.println("Number of contacting atoms (from both molecules): "+interf.getNumAtomsInContact());
			System.out.println("Number of core residues at "+String.format("%4.2f", BSATOASA_CUTOFF)+
					" bsa to asa cutoff: "+interf.getFirstRimCore().getCoreSize()+" "+interf.getSecondRimCore().getCoreSize());
			System.out.printf("Interface area: %8.2f\n",interf.getInterfaceArea());
			
			if (!interf.getFirstMolecule().getPdbChainCode().equals(interf.getSecondMolecule().getPdbChainCode()) && 
					pdb.areChainsInSameCluster(interf.getFirstMolecule().getPdbChainCode(), 
										interf.getSecondMolecule().getPdbChainCode())){

				System.out.println("Chains are NCS related, trying to find pseudo-symmetric relationship: ");
				//System.out.println("Superposition matrix in orthonormal: ");

				OptSuperposition os = interf.getOptimalSuperposition();
				
				Matrix3d optSuperposition = os.getSupMatrix();
				System.out.printf("%5.2f %5.2f %5.2f\n%5.2f %5.2f %5.2f\n%5.2f %5.2f %5.2f\n",
						optSuperposition.m00, optSuperposition.m01, optSuperposition.m12,
						optSuperposition.m10, optSuperposition.m11, optSuperposition.m12,
						optSuperposition.m20, optSuperposition.m21, optSuperposition.m22);
				
				Vector3d transl = os.getCentroidsTranslation();
				System.out.printf("translation: (%5.2f, %5.2f, %5.2f)\n",transl.x,transl.y,transl.z);
				Vector3d translXtal = new Vector3d(transl);
				pdb.getCrystalCell().transfToCrystal(translXtal);
				System.out.printf("translation (xtal): (%5.2f, %5.2f, %5.2f)\n",translXtal.x,translXtal.y,translXtal.z);
								
				System.out.printf("rmsd: %7.4f\n",os.getRmsd());

				double trace = optSuperposition.m00+optSuperposition.m11+optSuperposition.m22; 
				System.out.printf("Trace: %5.2f\n",trace);

				AxisAngle4d angleAndAxis = GeometryTools.getRotAxisAndAngle(optSuperposition);
				System.out.printf("Angle: %5.2f (%6.2f deg)\n",angleAndAxis.angle,angleAndAxis.angle*180.0/Math.PI);
				System.out.printf("Axis: %5.2f, %5.2f, %5.2f \n",angleAndAxis.x,angleAndAxis.y,angleAndAxis.z);

				Matrix4d optSuperPosWithTransl = os.getTransformMatrix();
				
				Matrix4d optSuperPosWithTranslXtal = pdb.getCrystalCell().transfToCrystal(optSuperPosWithTransl);
				
				Vector3d screwComp = GeometryTools.getTranslScrewComponent(optSuperPosWithTranslXtal);
				System.out.printf("screw comp: (%5.2f, %5.2f, %5.2f)\n",screwComp.x,screwComp.y,screwComp.z);
			}
			
			
			
			
			if (writeDir!=null) {
				File pdbFile = new File(writeDir,outBaseName+"."+(i+1)+".interface.pdb");
				interf.writeToPdbFile(pdbFile, true, false);
				interfPdbFiles[i] = pdbFile; 
				if (generatePngs) {
					pr.generateInterfPngPsePml(interf, BSATOASA_CUTOFF, MIN_ASA_FOR_SURFACE, pdbFile, 
							new File(writeDir,outBaseName+"."+(i+1)+".pse"), 
							new File(writeDir,outBaseName+"."+(i+1)+".pml"), outBaseName, true);
				}
			}
		}
		
				
		List<InterfaceCluster> clusters = interfaces.getClusters();
		System.out.println("\nClusters: ");
		for (InterfaceCluster cluster:clusters) {
			System.out.print(cluster.getId()+":");
			for (ChainInterface member:cluster.getMembers()) {
				System.out.print(" "+member.getId());
			}
			System.out.println();
		}
		System.out.println();
		
		if (writeDir!=null) {
			pr.generateInterfacesPse(inputFile, pdb.getPdbChainCodes(),
					new File(writeDir,outBaseName+".allinterfaces.pml"), 
					new File(writeDir,outBaseName+".allinterfaces.pse"), 
					interfPdbFiles,interfaces);
		}
		
		if (serialize && writeDir!=null) {
			Goodies.serialize(new File(writeDir,outBaseName+".interfaces.dat"), interfaces);
		}
		
//		System.out.println();
//		System.out.println("#### RECALCULATING with redundancy elimination");
//		System.out.println();
//		
//		start = System.currentTimeMillis();
//		interfFinder.setDebug(debug);
//		interfFinder.setWithRedundancyElimination(true);
//		ChainInterfaceList interfacesWithRE = interfFinder.getAllInterfaces(CUTOFF, null, Asa.DEFAULT_N_SPHERE_POINTS, nThreads, true, nonPoly);
//		end = System.currentTimeMillis();
//		long totalWithRE = (end-start)/1000;
//		
//		System.out.println();
//		System.out.println();
//		System.out.println("Total time for interface calculation ");
//		System.out.println(" with redundancy: "+total+"s");
//		System.out.println(" no redundancy  : "+totalWithRE+"s");
//		System.out.println("Total number of interfaces found: ");
//		System.out.println(" with redundancy: "+interfaces.size());
//		System.out.println(" no redundancy  : "+interfacesWithRE.size());
//		if (interfaces.size()!=interfacesWithRE.size()) {
//			System.out.println("#### FAILURE!");
//		}


	}

}
