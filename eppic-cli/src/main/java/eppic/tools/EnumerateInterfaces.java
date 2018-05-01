package eppic.tools;
import eppic.DataModelAdaptor;
import eppic.EppicParams;
import eppic.PymolRunner;
import eppic.commons.util.FileTypeGuesser;
import eppic.commons.util.Goodies;
import gnu.getopt.Getopt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Vector3d;

import org.biojava.nbio.structure.*;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.contact.Pair;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.biojava.nbio.structure.io.PDBFileParser;
import org.biojava.nbio.structure.io.mmcif.MMcifParser;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifConsumer;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifParser;
import org.biojava.nbio.structure.xtal.CrystalBuilder;
import org.biojava.nbio.structure.xtal.SpaceGroup;



public class EnumerateInterfaces {

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
	
	private static final double MIN_AREA_TO_KEEP = 0;
	
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
			" [-d]        : more verbose output for debugging\n\n";
		
		String pdbStr = null;
		File writeDir = null;
		int nThreads = NTHREADS;
		boolean generatePngs = false;
		boolean debug = false;
		boolean serialize = false;
		//boolean nonPoly = false;
		
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
				//nonPoly = true;
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

		
		Structure pdb = null;

		if (inputFile == null) {

			AtomCache cache = new AtomCache();		
			cache.setUseMmCif(true);		
			StructureIO.setAtomCache(cache); 

			try {
				pdb = StructureIO.getStructure(pdbStr);
			} catch (IOException|StructureException e) {
				System.out.println("Error. Couldn't load PDB "+pdbStr+". Error: "+e.getMessage());
				System.exit(0);
			} 		
		} else {
			
			int fileType = FileTypeGuesser.guessFileType(inputFile);
			
			if (fileType==FileTypeGuesser.CIF_FILE) {

				MMcifParser parser = new SimpleMMcifParser();

				SimpleMMcifConsumer consumer = new SimpleMMcifConsumer();

				FileParsingParameters fileParsingParams = new FileParsingParameters();
				// TODO we should parse PDB files with no X padding if no SEQRES is found. Otherwise matching to uniprot doesn't work in many cases
				//fileParsingParams.set????

				consumer.setFileParsingParameters(fileParsingParams);

				parser.addMMcifConsumer(consumer);

				parser.parse(new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)))); 

				pdb = consumer.getStructure();
				
			} else if (fileType==FileTypeGuesser.PDB_FILE || fileType==FileTypeGuesser.RAW_PDB_FILE) {

				PDBFileParser parser = new PDBFileParser();
				
				FileParsingParameters fileParsingParams = new FileParsingParameters();
				// TODO we should parse PDB files with no X padding if no SEQRES is found. Otherwise matching to uniprot doesn't work in many cases
				//fileParsingParams.set????
				parser.setFileParsingParameters(fileParsingParams);
				
				pdb = parser.parsePDBFile(new FileInputStream(inputFile));
				
			}
			
			outBaseName = inputFile.getName().substring(0, inputFile.getName().lastIndexOf("."));
		}

		int countPolyEntities = 0;
		for (EntityInfo chainCluster:pdb.getEntityInfos()) {
			if (chainCluster.getType() == EntityType.POLYMER) countPolyEntities++;
		}
		System.out.println(pdb.getPDBCode()+" - "+pdb.getPolyChains().size()+" poly chains ("+countPolyEntities+" sequence unique) ");

		for (EntityInfo chainCluster:pdb.getEntityInfos()) {
			if (chainCluster.getType() == EntityType.POLYMER) {
				// in mmCIF files some sugars are annotated as compounds with no chains linked to them, e.g. 3s26
				if (chainCluster.getChains().isEmpty()) continue;
				System.out.println(DataModelAdaptor.getChainClusterString(chainCluster));
			}
		}
		System.out.println("Chains: ");
		for (Chain chain:pdb.getPolyChains()) {
			System.out.println(chain.getId()+"("+chain.getName()+")");
		}
		
		PDBCrystallographicInfo xtalInfo = pdb.getCrystallographicInfo();
		SpaceGroup sg = (xtalInfo==null?null:xtalInfo.getSpaceGroup());
		if (sg!=null) {
			System.out.println(sg.getShortSymbol()+" ("+sg.getId()+")");
			if (debug) System.out.println("Symmetry operators: "+sg.getNumOperators());
		}
		
		System.out.println("Calculating possible interfaces... (using "+nThreads+" CPUs for ASA calculation)");
		long start = System.currentTimeMillis();
		
		CrystalBuilder interfFinder = new CrystalBuilder(pdb);
		StructureInterfaceList interfaces = interfFinder.getUniqueInterfaces(CUTOFF);
		interfaces.calcAsas(N_SPHERE_POINTS, nThreads, CONSIDER_COFACTORS);
		interfaces.removeInterfacesBelowArea(MIN_AREA_TO_KEEP);
		

		long end = System.currentTimeMillis();
		long total = (end-start)/1000;
		System.out.println("Total time for interface calculation: "+total+"s");
		
		System.out.println("Total number of interfaces found: "+interfaces.size());

		PymolRunner pr = new PymolRunner(PYMOL_EXE);
		File[] interfPdbFiles = new File[interfaces.size()];
					
		for (int i=0;i<interfaces.size();i++) {
			StructureInterface interf = interfaces.get(i+1);			
			
			String infiniteStr = "";
			if (interf.isInfinite()) infiniteStr = " -- INFINITE interface";
			System.out.println("\n##Interface "+(i+1)+" "+
					getFirstSubunitId(interf)+"-"+
					getSecondSubunitId(interf)+infiniteStr);
			if (interf.getContacts().getContactsWithinDistance(EppicParams.CLASH_DISTANCE).size()>0) 
				System.out.println("CLASHES!!!");
			
			
			System.out.println("Transf1: "+SpaceGroup.getAlgebraicFromMatrix(interf.getTransforms().getFirst().getMatTransform())+
					". Transf2: "+SpaceGroup.getAlgebraicFromMatrix(interf.getTransforms().getSecond().getMatTransform()));
	 		
			int foldType = sg.getAxisFoldType(interf.getTransforms().getSecond().getTransformId());
			AxisAngle4d axisAngle = sg.getRotAxisAngle(interf.getTransforms().getSecond().getTransformId());
			
			String screwStr = "";
			if (interf.getTransforms().getSecond().getTransformType().isScrew()) {
				Vector3d screwTransl = 
						interf.getTransforms().getSecond().getTranslScrewComponent();
				screwStr = " -- "+interf.getTransforms().getSecond().getTransformType().getShortName()+" with translation "+
				String.format("(%5.2f,%5.2f,%5.2f)",screwTransl.x,screwTransl.y,screwTransl.z);

			}
			//if (pdb.getSpaceGroup().isScrewRotation(interf.getSecondTransf().getTransformId())) {
			//	Vector3d screwTransl = pdb.getSpaceGroup().getTranslScrewComponent(interf.getSecondTransf().getTransformId());
			//	screwStr = " -- "+interf.getSecondTransf().getTransformType().getShortName()+" screw with translation "+
			//			String.format("(%5.2f,%5.2f,%5.2f)",screwTransl.x,screwTransl.y,screwTransl.z);
			//}
			
			System.out.println(" "+foldType+"-fold on axis "+String.format("(%5.2f,%5.2f,%5.2f)",axisAngle.x,axisAngle.y,axisAngle.z)+screwStr);
			
			System.out.println("Number of atom contacts: "+interf.getContacts().size());
			Pair<List<Group>> cores = interf.getCoreResidues(BSATOASA_CUTOFF, MIN_ASA_FOR_SURFACE);
			System.out.println("Number of core residues at "+String.format("%4.2f", BSATOASA_CUTOFF)+
					" bsa to asa cutoff: "+
					cores.getFirst().size()+" "+
					cores.getSecond().size());
			
			System.out.printf("Interface area: %8.2f\n",interf.getTotalArea());
			
			// TODO following code to report on rmsds needs to be rewritten for biojava at some point
//			if (!interf.getMoleculeIds().getFirst().equals(interf.getMoleculeIds().getSecond()) && 
//					pdb.areChainsInSameCluster(interf.getMoleculeIds().getFirst(), 
//										interf.getMoleculeIds().getSecond())){
//
//				System.out.println("Chains are NCS related, trying to find pseudo-symmetric relationship: ");
//				//System.out.println("Superposition matrix in orthonormal: ");
//
//				OptSuperposition os = interf.getOptimalSuperposition();
//				
//				Matrix3d optSuperposition = os.getSupMatrix();
//				System.out.printf("%5.2f %5.2f %5.2f\n%5.2f %5.2f %5.2f\n%5.2f %5.2f %5.2f\n",
//						optSuperposition.m00, optSuperposition.m01, optSuperposition.m12,
//						optSuperposition.m10, optSuperposition.m11, optSuperposition.m12,
//						optSuperposition.m20, optSuperposition.m21, optSuperposition.m22);
//				
//				Vector3d transl = os.getCentroidsTranslation();
//				System.out.printf("translation: (%5.2f, %5.2f, %5.2f)\n",transl.x,transl.y,transl.z);
//				Vector3d translXtal = new Vector3d(transl);
//				xtalInfo.getCrystalCell().transfToCrystal(translXtal);
//				System.out.printf("translation (xtal): (%5.2f, %5.2f, %5.2f)\n",translXtal.x,translXtal.y,translXtal.z);
//								
//				System.out.printf("rmsd: %7.4f\n",os.getRmsd());
//
//				double trace = optSuperposition.m00+optSuperposition.m11+optSuperposition.m22; 
//				System.out.printf("Trace: %5.2f\n",trace);
//
//				AxisAngle4d angleAndAxis = GeometryTools.getRotAxisAndAngle(optSuperposition);
//				System.out.printf("Angle: %5.2f (%6.2f deg)\n",angleAndAxis.angle,angleAndAxis.angle*180.0/Math.PI);
//				System.out.printf("Axis: %5.2f, %5.2f, %5.2f \n",angleAndAxis.x,angleAndAxis.y,angleAndAxis.z);
//
//				Matrix4d optSuperPosWithTransl = os.getTransformMatrix();
//				
//				Matrix4d optSuperPosWithTranslXtal = xtalInfo.getCrystalCell().transfToCrystal(optSuperPosWithTransl);
//				
//				Vector3d screwComp = GeometryTools.getTranslScrewComponent(optSuperPosWithTranslXtal);
//				System.out.printf("screw comp: (%5.2f, %5.2f, %5.2f)\n",screwComp.x,screwComp.y,screwComp.z);
//			}
			
			
			
			
			if (writeDir!=null) {
				File pdbFile = new File(writeDir,outBaseName+"."+(i+1)+".interface.pdb");
				PrintWriter pw = new PrintWriter(pdbFile);
				pw.print(interf.toPDB());
				pw.close();
				interfPdbFiles[i] = pdbFile; 
				if (generatePngs) {
					// TODO this expects now a mmcif file...
					pr.generateInterfacePng(interf, pdbFile, outBaseName);
				}
			}
		}
		
				
		List<StructureInterfaceCluster> clusters = interfaces.getClusters(EppicParams.CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF);
		System.out.println("\nClusters: ");
		for (StructureInterfaceCluster cluster:clusters) {
			System.out.print(cluster.getId()+":");
			for (StructureInterface member:cluster.getMembers()) {
				System.out.print(" "+member.getId());
			}
			System.out.println();
		}
		System.out.println();
		
		if (writeDir!=null) {
			Set<String> chainIds = new TreeSet<String>();
			for (Chain chain:pdb.getPolyChains()) {
				chainIds.add(chain.getName());
			}
			pr.generateInterfacesPse(inputFile, chainIds,
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

	private static String getFirstSubunitId(StructureInterface interf) {
		return 
				interf.getMoleculeIds().getFirst()+""+
				interf.getTransforms().getFirst().getTransformId()+
				interf.getTransforms().getFirst().getCrystalTranslation().toString();
	}
	
	private static String getSecondSubunitId(StructureInterface interf) {
		return 
				interf.getMoleculeIds().getSecond()+""+
				interf.getTransforms().getSecond().getTransformId()+
				interf.getTransforms().getSecond().getCrystalTranslation().toString();
	}

}
