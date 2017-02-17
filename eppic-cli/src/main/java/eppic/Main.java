package eppic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.zip.GZIPOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.biojava.nbio.core.sequence.io.util.IOUtils;
import org.biojava.nbio.structure.Compound;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureIO;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.biojava.nbio.structure.io.PDBFileParser;
import org.biojava.nbio.structure.io.mmcif.ChemCompGroupFactory;
import org.biojava.nbio.structure.io.mmcif.DownloadChemCompProvider;
import org.biojava.nbio.structure.io.mmcif.MMcifParser;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifConsumer;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifParser;
import org.biojava.nbio.structure.xtal.CrystalBuilder;
import org.biojava.nbio.structure.xtal.SpaceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import eppic.assembly.Assembly;
import eppic.assembly.CrystalAssemblies;
import eppic.assembly.GraphUtils;
import eppic.assembly.LatticeGraph3D;
import eppic.assembly.gui.LatticeGUIMustache;
import eppic.commons.util.FileTypeGuesser;
import eppic.commons.util.StructureUtils;
import eppic.predictors.CombinedClusterPredictor;
import eppic.predictors.CombinedPredictor;
import eppic.predictors.GeometryClusterPredictor;
import eppic.predictors.GeometryPredictor;

/**
 * The eppic main class to execute the CLI workflow.
 * 
 * 
 * @author Jose Duarte
 *
 */
public class Main {
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	
	private static final int STEPS_TOTAL = 4;
	
	// fields
	private EppicParams params;
	
	private Structure pdb;
	private StructureInterfaceList interfaces;
	private ChainEvolContextList cecs;
	private InterfaceEvolContextList iecList;
	private List<GeometryPredictor> gps;
	private List<GeometryClusterPredictor> gcps;
	
	private CrystalAssemblies validAssemblies;
	
	private File stepsLogFile;
	private int stepCount;
	
	private DataModelAdaptor modelAdaptor;
		
	public Main() {
		this.params = new EppicParams();
		this.stepCount = 1;
	}
	
	protected Structure getStructure() {
		return pdb;
	}

		
	public void setUpLogging() {
		
		// the log4j2 log file configuration at runtime, note that elsewhere we use the slf4j interface only
		// see http://stackoverflow.com/questions/14862770/log4j2-assigning-file-appender-filename-at-runtime
		System.setProperty("logFilename", new File(params.getOutDir(),params.getBaseName()+".log").toString());
		
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();

		// TODO for some reason (bug?) log4j2 2.1 produces a file named with the log4j2.xml $pointer, the only fix I know for now is to remove it manually
		new File("${sys:logFilename}").deleteOnExit();
		// some program that we run (which one???) produces an empty error.log file, let's also remove it here
		new File("error.log").deleteOnExit();

		if (params.getProgressLogFile()!=null) {
			// the steps log file needed for the server, we only initialise it if a -L progress log file was passed (as that is only used by server)
			stepsLogFile = new File(params.getOutDir(),params.getBaseName()+EppicParams.STEPS_LOG_FILE_SUFFIX);
		}
		
		// TODO what about the debug logging? we used to do it from command line param -u, should we do it from xml file?
//		if (params.getDebug())
//			ROOTLOGGER.addAppender(outAppender);


	}
	
	public void loadConfigFile() {
		// loading settings from config file
		File userConfigFile = new File(System.getProperty("user.home"),EppicParams.CONFIG_FILE_NAME);  
		try {
			if (params.getConfigFile()!=null) {
				LOGGER.info("Loading user configuration file given in command line " + params.getConfigFile());
				params.readConfigFile(params.getConfigFile());
				params.checkConfigFileInput();
			} else if (userConfigFile.exists()) {
				LOGGER.info("Loading user configuration file " + userConfigFile);
				params.readConfigFile(userConfigFile);
				params.checkConfigFileInput();
			} else if (!params.isInputAFile() || params.isDoEvolScoring()) {
				LOGGER.error("No config file could be read at "+userConfigFile+
						". Please set one if you want to run the program using PDB codes as input with -i or if you want to run evolutionary predictions (-s).");
				System.exit(1);
			}
		} catch (IOException e) {
			LOGGER.error("Error while reading from config file: " + e.getMessage());
			System.exit(1);
		} catch (EppicException e) {
			LOGGER.error(e.getMessage());
			System.exit(1);
		}

	}

	public void doLoadPdb() throws EppicException {	
		
		// Before loading anything we make sure that BioJava is set to DownloadChemCompProvider.
		// This is in case the default is ReducedChemCompProvider.
		// That has a huge effect in the understanding of the chemical components, for instance 
		// what residues are non-standard aminoacids
		
		// TODO if BioJava 4.2 changes the default or the behavior of DownloadChemCompProvider
		//      we will need to revise this
		
		
		if (params.getAtomCachePath()!=null)
			ChemCompGroupFactory.setChemCompProvider(new DownloadChemCompProvider(params.getAtomCachePath()));
		else 
			ChemCompGroupFactory.setChemCompProvider(new DownloadChemCompProvider());
		
		params.getProgressLog().println("Loading PDB data: "+(params.getInFile()==null?params.getPdbCode():params.getInFile().getName()));
		writeStep("Calculating Interfaces");
		pdb = null;
		try {
			if (!params.isInputAFile()) {
				
				AtomCache cache = null;
								
				if (params.getAtomCachePath()!=null) {
					LOGGER.info("Path given in ATOM_CACHE_PATH, setting AtomCache to {} and ignoring env variable PDB_DIR", params.getAtomCachePath());
					cache = new AtomCache(params.getAtomCachePath());
				} else {
					cache = new AtomCache();
				}
				cache.setUseMmCif(true);
				
				// we set default fetch behavior to FETCH_IF_OUTDATED which is the closest to rsync
				if (params.getFetchBehavior()!=null) {
					cache.setFetchBehavior(params.getFetchBehavior());
				} else {
					cache.setFetchBehavior(EppicParams.DEF_FETCH_BEHAVIOR);
				}
				FileParsingParameters fileParsingParams = new FileParsingParameters();
				fileParsingParams.setAlignSeqRes(true);
				fileParsingParams.setParseBioAssembly(true);
				cache.setFileParsingParams(fileParsingParams);
				
				StructureIO.setAtomCache(cache); 
				
				
				try {
					pdb = StructureIO.getStructure(params.getPdbCode());
				} catch(IOException e) {
					throw new EppicException(e,"Couldn't get cif file from AtomCache for code "+params.getPdbCode()+". Error: "+e.getMessage(),true);
				}
					
			} else {

				int fileType = FileTypeGuesser.guessFileType(params.getInFile());
				
				if (fileType==FileTypeGuesser.CIF_FILE) {

					MMcifParser parser = new SimpleMMcifParser();

					SimpleMMcifConsumer consumer = new SimpleMMcifConsumer();

					FileParsingParameters fileParsingParams = new FileParsingParameters();
					// TODO we should parse PDB files with no X padding if no SEQRES is found. Otherwise matching to uniprot doesn't work in many cases
					//fileParsingParams.set????					
					fileParsingParams.setAlignSeqRes(true);

					consumer.setFileParsingParameters(fileParsingParams);

					parser.addMMcifConsumer(consumer);

					parser.parse(new BufferedReader(new InputStreamReader(IOUtils.openFile(params.getInFile())))); 

					pdb = consumer.getStructure();
					
				} else if (fileType==FileTypeGuesser.PDB_FILE || fileType==FileTypeGuesser.RAW_PDB_FILE) {

					PDBFileParser parser = new PDBFileParser();
					
					FileParsingParameters fileParsingParams = new FileParsingParameters();
					// TODO we should parse PDB files with no X padding if no SEQRES is found. Otherwise matching to uniprot doesn't work in many cases
					//fileParsingParams.set????
					fileParsingParams.setAlignSeqRes(true);
					
					parser.setFileParsingParameters(fileParsingParams);
					
					pdb = parser.parsePDBFile(new FileInputStream(params.getInFile()));
					
				}


			}
		} catch (StructureException e) {
			throw new EppicException(e,"Couldn't load file PDB/mmCIF file. Error: "+e.getMessage(),true);
		} catch (IOException e) {
			throw new EppicException(e,"Problems reading PDB data from "+params.getInFile()+". Error: "+e.getMessage(),true);
		}
		
		if (pdb.getCrystallographicInfo()==null || pdb.getCrystallographicInfo().getCrystalCell()==null) {
			LOGGER.warn("No crystal information found in source "+params.getPdbCode()+". Only asymmetric unit interfaces will be calculated.");
		}
		
		
		// for the webui
		modelAdaptor = new DataModelAdaptor();
		modelAdaptor.setParams(params);
		modelAdaptor.setPdbMetadata(pdb);
		
		
	}

	public void doFindInterfaces() throws EppicException {

		params.getProgressLog().println("Calculating possible interfaces...");
		StructureUtils.expandNcsOps(pdb);
		LOGGER.info("Calculating possible interfaces");
		CrystalBuilder interfFinder = new CrystalBuilder(pdb);
		interfaces = interfFinder.getUniqueInterfaces(EppicParams.INTERFACE_DIST_CUTOFF);
		interfaces.calcAsas(params.getnSpherePointsASAcalc(), params.getNumThreads(), params.getMinSizeCofactorForAsa());
		interfaces.removeInterfacesBelowArea(EppicParams.MIN_INTERFACE_AREA_TO_KEEP);
		
		LOGGER.info("Interfaces calculated with "+params.getnSpherePointsASAcalc()+" sphere points.");

		LOGGER.info("Calculating interface clusters");

		int clustersSize = interfaces.getClusters(EppicParams.CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF).size();
		int numInterfaces = interfaces.size();
		LOGGER.info("Interface clustering done: "+numInterfaces+" interfaces - "+clustersSize+" clusters");
		String msg = "Interface clusters: ";
		for (int i=0; i<clustersSize;i++) {
			StructureInterfaceCluster cluster = interfaces.getClusters(EppicParams.CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF).get(i);
			msg += cluster.getId()+": ";
			for (StructureInterface interf:cluster.getMembers()) {
				msg += interf.getId()+" ";
			}
			if (i!=clustersSize-1) msg += ", ";
		}
		LOGGER.info(msg); 

		// TODO how to do the cofactors logging now?
		//if (interfFinder.hasCofactors()) {
		//	LOGGER.info("Cofactors of more than "+params.getMinSizeCofactorForAsa()+" atoms present: they will be taken into account for ASA calculations");
		//	for (String pdbChainCode:pdb.getPdbChainCodes()) {
		//		LOGGER.info("Chain "+pdbChainCode+": "+interfFinder.getNumCofactorsForPdbChainCode(pdbChainCode)+" cofactor(s) - "+
		//				interfFinder.getCofactorsInfoString(pdbChainCode));
		//	}
		//} else {
		//	if (params.getMinSizeCofactorForAsa()<0) {
		//		LOGGER.info("No minimum size for cofactors set. All cofactors will be ignored for ASA calculations");
		//	} else {
		//		LOGGER.info("No cofactors of more than "+params.getMinSizeCofactorForAsa()+" atoms present. Cofactors will be ignored for ASA calculations");
		//	}
		//}


		LOGGER.info("Minimum ASA for a residue to be considered surface: "+String.format("%2.0f",params.getMinAsaForSurface()));
		
		params.getProgressLog().println("Done");

		checkClashes();

		
		
	}
	
	private void checkClashes() throws EppicException {
		
		// getting the number of clashes per interface
		int[] numClashesPerInterface = new int[interfaces.size()];
		boolean hasInterfacesWithClashes = false;
		boolean tooManyClashes = false;
		int i = 0;
		for (StructureInterface interf:interfaces) {
			numClashesPerInterface[i] = interf.getContacts().getContactsWithinDistance(EppicParams.CLASH_DISTANCE).size();
			if (numClashesPerInterface[i]>0) hasInterfacesWithClashes = true;
			if (numClashesPerInterface[i]>EppicParams.NUM_CLASHES_FOR_ERROR) tooManyClashes = true;
			i++;
		}
		
		if (hasInterfacesWithClashes) {
		
			String msg = "Clashes (atoms distance below "+EppicParams.CLASH_DISTANCE+") found in:";			
			i = 0;
			for (StructureInterface interf:interfaces) {
				if (numClashesPerInterface[i]>0) {		
					msg+=("\nInterface "+interf.getId()+": "+interf.getMoleculeIds().getFirst()+"+"
							+interf.getMoleculeIds().getSecond()+" ("+
							SpaceGroup.getAlgebraicFromMatrix(interf.getTransforms().getSecond().getMatTransform())+
							") Clashes: "+numClashesPerInterface[i]);
				}
				i++;
			}
			
			if (tooManyClashes) {
				String excptionMsg = null;
				if (params.isInputAFile()) {
					excptionMsg = "Too many clashes in at least one interface, most likely there is an error in this structure. Please check that the CRYST1 record is correct. "+msg;
				} else {
					excptionMsg = "Too many clashes in at least one interface, most likely there is an error in this structure. "+msg; 
				}
				throw new EppicException(null, excptionMsg, true);				
			} else { 
				LOGGER.warn(msg);
			}

		}
		
	}
	
	public void doFindAssemblies() throws StructureException { 
		
		params.getProgressLog().println("Calculating possible assemblies...");
		validAssemblies = new CrystalAssemblies(pdb, interfaces, params.isForceContractedAssemblyEnumeration()); 

		StringBuilder sb = new StringBuilder();
		for (Assembly a: validAssemblies) {
			sb.append(a.toString()+" ");
		}
		LOGGER.info("There are {} topologically possible assemblies: {}", validAssemblies.size(), sb.toString());
					
		params.getProgressLog().println("Done");
	}
	
	public void doAssemblyScoring() {


		if (params.isDoEvolScoring()) {
			validAssemblies.setInterfaceEvolContextList(iecList);

			validAssemblies.score();
		}


		modelAdaptor.setAssemblies(validAssemblies);

		modelAdaptor.setPdbBioUnits(pdb.getPDBHeader().getBioAssemblies(), validAssemblies, pdb);

	}
	
	public void doGeomScoring() throws EppicException {
		if (interfaces.size()==0) {
			// no interfaces found at all, can happen e.g. in NMR structure with 1 chain, e.g. 1nmr
			LOGGER.info("No interfaces found, nothing to analyse.");
			params.getProgressLog().println("No interfaces found, nothing to analyse.");
			// we still continue so that the web interface can pick it up too
			return;
		}

		// interface scoring
		gps = new ArrayList<GeometryPredictor>();
		for (StructureInterface interf:interfaces) {
			GeometryPredictor gp = new GeometryPredictor(interf);
			gps.add(gp);
			gp.setBsaToAsaCutoff(params.getCAcutoffForGeom());
			gp.setMinAsaForSurface(params.getMinAsaForSurface());
			gp.setMinCoreSizeForBio(params.getMinCoreSizeForBio());
			gp.computeScores();
		}
		
		// interface cluster scoring
		gcps = new ArrayList<GeometryClusterPredictor>();
		for (StructureInterfaceCluster interfaceCluster:interfaces.getClusters(EppicParams.CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF)) {
			List<GeometryPredictor> gpsForCluster = new ArrayList<GeometryPredictor>();
			
			for (int i=0;i<interfaces.size();i++) {
				if ( interfaces.get(i+1).getCluster().getId()==interfaceCluster.getId()) {
					gpsForCluster.add(gps.get(i));
				}
			}
			
			GeometryClusterPredictor gcp = new GeometryClusterPredictor(gpsForCluster);

			gcp.setMinCoreSizeForBio(params.getMinCoreSizeForBio());
			gcp.computeScores();			
			gcps.add(gcp);
		}

		// writing to the model: for the webui and csv output files
		modelAdaptor.setInterfaces(interfaces); // this writes all interface geom info, including h-bonds, disulfides etc
		modelAdaptor.setGeometryScores(gps, gcps);
		modelAdaptor.setResidueBurialDetails(interfaces);

	}
	
	public void doWriteTextOutputFiles() throws EppicException {
		
		TextOutputWriter toW = new TextOutputWriter(modelAdaptor.getPdbInfo(), params);
		
		// 0 write .A.aln file : always write it, with our without -w 
		try {
			toW.writeAlnFiles();
		} catch (IOException e) {
			throw new EppicException(e, "Could not write the homologs alignment files: "+e.getMessage(), true);
		}
		
		
		// we don't write text files if in -w
		if (params.isGenerateModelSerializedFile()) return;
		

		
		// 1 interfaces info file and contacts info file
		try {
			// if no interfaces found (e.g. NMR) we don't want to write the file
			if (interfaces.size()>0) {
				toW.writeInterfacesInfoFile();
				toW.writeContactsInfoFile();
			}
		} catch(IOException	e) {
			throw new EppicException(e,"Could not write interfaces description or contact list to file: "+e.getMessage(),false);
		}

		// 2 scores file: geom, core-rim, core-surface and combine all in one file
		try {
			toW.writeScoresFile();
		} catch (IOException e) {
			throw new EppicException(e, "Could not write interface scores file. "+e.getMessage(),true);
		}
		
		// 3 write assemblies file
		try {
			toW.writeAssembliesFile();
		} catch (IOException e) {
			throw new EppicException(e, "Could not write the assemblies file: "+e.getMessage(), false);
		}
		
		// 4 write .A.log file
		try {
			toW.writeHomologsSummaries();
		} catch (IOException e) {
			throw new EppicException(e, "Could not write the homologs summaries files: "+e.getMessage(), false);
		}
		
		// 5 write .A.entropies file  
		try {
			toW.writeEntropyFiles();
		} catch (IOException e) {
			throw new EppicException(e, "Could not write the homologs entropies files: "+e.getMessage(), false);
		}
	
		
	}
	
	public void doWriteCoordFiles() throws EppicException {

		if (!params.isGenerateOutputCoordFiles()) return;
		
		
		
		try {
			if (params.isDoEvolScoring() && iecList!=null) { //iecList can be null if there are no interfaces (e.g. NMR monomers)
				// we set the entropies as bfactors in case we are in evol scoring (-s)
				// this will reset the bfactors in the Chain objects of the StructureInterface objects
				// so both interfaces and assembly files will be written with reset bfactors
				for (InterfaceEvolContext iec:iecList) {				
					iec.setConservationScoresAsBfactors();
				}
			}
			
			// INTERFACE files
			for (StructureInterface interf : interfaces) {
				File outputFile = params.getOutputFile(EppicParams.INTERFACES_COORD_FILES_SUFFIX + "." + interf.getId() + EppicParams.MMCIF_FILE_EXTENSION);
				PrintStream ps = new PrintStream(new GZIPOutputStream(new FileOutputStream(outputFile)));				
				ps.print(interf.toMMCIF());
				ps.close();
				if (params.isGeneratePdbFiles()) { 
					outputFile = params.getOutputFile(EppicParams.INTERFACES_COORD_FILES_SUFFIX + "." + interf.getId() + EppicParams.PDB_FILE_EXTENSION);
					ps = new PrintStream(new GZIPOutputStream(new FileOutputStream(outputFile)));
					ps.print(interf.toPDB());
					ps.close();
				}				
			}
				
			// ASSEMBLY files
			for (Assembly a:validAssemblies) {

				File outputFile= params.getOutputFile(EppicParams.ASSEMBLIES_COORD_FILES_SUFFIX+"." + a.getId() + EppicParams.MMCIF_FILE_EXTENSION);

				try {
					LOGGER.info("Writing assembly {} to {}",a.getId(),outputFile);
					a.writeToMmCifFile(outputFile);
					if (params.isGeneratePdbFiles()) {
						outputFile= params.getOutputFile(EppicParams.ASSEMBLIES_COORD_FILES_SUFFIX+"." + a.getId() +  EppicParams.PDB_FILE_EXTENSION);
						a.writeToPdbFile(outputFile);
					}

				} catch (StructureException e) {
					LOGGER.error("Could not write assembly coordinates file {}: {}",a.getId(),e.getMessage());
					continue;
				}

			}
			
		} catch (IOException e) {
			throw new EppicException(e, "Couldn't write interface PDB files. " + e.getMessage(), true);
		} 
	}

	public void doWriteAssemblyDiagrams() throws EppicException {

		// should not happen, there should always be 1 assembly (the trivial no-interfaces engaged one)
		if (validAssemblies.getUniqueAssemblies().size() == 0) return; 

		if (!params.isGenerateDiagrams()) return;
		
		params.getProgressLog().println("Writing Assembly Diagram files");
		writeStep("Generating assembly diagram Thumbnails");
		LOGGER.info("Generating Assembly Diagram files");

		try {
			LatticeGraph3D latticeGraph = new LatticeGraph3D(validAssemblies.getLatticeGraph());
			GraphvizRunner runner = new GraphvizRunner(params.getGraphvizExe());
			String fileFormat = "png";
			
			// the gson object needed in step 3 below
			Gson gson = LatticeGraph3D.createGson();

			for (Assembly a:validAssemblies) {
				
				// 1. Generate the png with the assembly diagram via invoking the dot executable

				File pngFile= params.getOutputFile(EppicParams.ASSEMBLIES_DIAGRAM_FILES_SUFFIX+"." + a.getId() + "."+EppicParams.THUMBNAILS_SIZE+"x"+EppicParams.THUMBNAILS_SIZE+"."+fileFormat);

				LOGGER.info("Writing diagram for assembly {} to {}",a.getId(),pngFile);
					
				// Filter down to this assembly
				// TODO this is not going to work for contracted graphs: both clusterIds and interfaceids are wrong! see issue https://github.com/eppic-team/eppic/issues/148
				SortedSet<Integer> clusterIds = GraphUtils.getDistinctInterfaceClusters(a.getAssemblyGraph().getSubgraph());
				Set<Integer> interfaceIds = GraphUtils.getDistinctInterfaces(a.getAssemblyGraph().getSubgraph());
				latticeGraph.filterEngagedClusters(clusterIds);
					
				LatticeGUIMustache guiThumb = new LatticeGUIMustache(LatticeGUIMustache.TEMPLATE_ASSEMBLY_DIAGRAM_THUMB, latticeGraph);
				guiThumb.setLayout2D(LatticeGUIMustache.getDefaultLayout2D(pdb));
				guiThumb.setTitle("Assembly "+a.getId());
				guiThumb.setPdbId(pdb.getPDBCode());
				int dpi = 72; // 72 dots per inch for output
				// size is in inches
				guiThumb.setSize(String.valueOf((double)EppicParams.THUMBNAILS_SIZE/(double)dpi));
				guiThumb.setDpi(String.valueOf(dpi));

				// Generate thumbs via dot file
				//File dotFile= params.getOutputFile(EppicParams.ASSEMBLIES_DIAGRAM_FILES_SUFFIX+"." + a.getId() + ".dot");
				//try (PrintWriter out = new PrintWriter(dotFile)) {
				//	guiThumb.execute(out);
				//}
				//runner.generateFromDot(dotFile, pngFile, fileFormat);

				// Generate thumbs via pipe
				runner.generateFromDot(guiThumb, pngFile, fileFormat);
				
				
				// 2. Generate the json file for the dynamic js graph in the wui
				
				guiThumb = new LatticeGUIMustache(LatticeGUIMustache.TEMPLATE_ASSEMBLY_DIAGRAM_JSON, latticeGraph);
				guiThumb.setLayout2D(LatticeGUIMustache.getDefaultLayout2D(pdb));
				String json;
				// Hack to work around Mustache limitations which prevent generating valid JSON
				try(StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						) {

					// Construct page
					guiThumb.execute(pw);

					pw.flush();
					sw.flush();
					json = sw.toString();
					// Remove all trailing commas from lists (invalid JSON)
					json = json.replaceAll(",(?=\\s*[}\\]])","");
											
				}
				File jsonAssemblyDiagramFile = params.getOutputFile(EppicParams.get2dDiagramJsonFilenameSuffix(interfaceIds));

				PrintWriter pw = new PrintWriter(new FileWriter(jsonAssemblyDiagramFile));
				pw.println(json);
				pw.close();
				
				
				// 3. Generate the json file for the 3d lattice graph in the wui (ngl based)
								
				latticeGraph.setHexColors();

				json = gson.toJson(latticeGraph);

				File jsonLatticeGraphFile = params.getOutputFile(EppicParams.get3dLatticeGraphJsonFilenameSuffix(interfaceIds));

				pw = new PrintWriter(new FileWriter(jsonLatticeGraphFile));
				pw.println(json);
				pw.close();
				
				
			}
			
			// additionally for the lattice graph 3d we need the full graph "*" file, i.e. all interfaces engaged
			// used in "view unit cell"
			
			latticeGraph.filterEngagedClusters(null);
			latticeGraph.setHexColors();

			String json = gson.toJson(latticeGraph);

			File jsonLatticeGraphFile = params.getOutputFile(EppicParams.get3dLatticeGraphJsonFilenameSuffix(null));

			PrintWriter pw = new PrintWriter(new FileWriter(jsonLatticeGraphFile));
			pw.println(json);
			pw.close();
			
		} catch( IOException|StructureException|InterruptedException e) {
			throw new EppicException(e, "Couldn't write assembly diagrams. " + e.getMessage(), true);
		}
	}

	// TODO implement the HBplus stuff
//	public void doHBPlus() throws EppicException {
//
//		if (interfaces.size() == 0) return;
//		
//		if (params.getHbplusExe() != null && params.getHbplusExe().exists()) {
//
//			if (!params.isGenerateInterfacesPdbFiles()) {
//				LOGGER.info("HBPlus is set in config file but -l was not used, will not do H-bond calculation with HBPlus");
//				return;
//			}
//
//			try {
//
//				for (StructureInterface interf : interfaces) {
//					if (interf.isFirstProtein() && interf.isSecondProtein()) {
//						// note this file will be overwritten later by doWritePdbFiles()
//						File pdbFile = params.getOutputFile("." + interf.getId() + ".pdb.gz");
//						try {
//							interf.writeToPdbFile(pdbFile, true, true);
//						} catch (IOException e) {
//							throw new EppicException(e, "Couldn't write interface PDB files. " + e.getMessage(), true);
//						}
//						LOGGER.info("Running HBPlus for interface "+interf.getId());
//						interf.runHBPlus(params.getHbplusExe(), pdbFile);						
//					}
//				}
//
//			} catch (IOException e) {
//				throw new EppicException(e, "Couldn't run HBPlus. Error: " + e.getMessage(), true);
//			} catch (InterruptedException e) {
//				throw new EppicException(e, "Problems while running HBPlus. " + e.getMessage(), true);
//			}
//		} else {
//			LOGGER.info("HBPlus is not set or set to an invalid path. Will do H-bond calculation with internal algorithm.");
//		}
//	}
	

	public void doWritePymolFiles() throws EppicException {
		
		if (!params.isGenerateThumbnails()) return;
		
		params.getProgressLog().println("Writing PyMOL files");
		writeStep("Generating Thumbnails and PyMOL Files");
		LOGGER.info("Generating PyMOL files");

		PymolRunner pr = new PymolRunner(params.getPymolExe());

		try {
			for (StructureInterface interf:interfaces) {
				File cifFile = params.getOutputFile(EppicParams.INTERFACES_COORD_FILES_SUFFIX+"."+interf.getId()+ EppicParams.MMCIF_FILE_EXTENSION);
				pr.generateInterfacePng(interf, 
						cifFile, 
						params.getBaseName()+EppicParams.INTERFACES_COORD_FILES_SUFFIX+"."+interf.getId()	);
				LOGGER.info("Generated PyMOL files for interface "+interf.getId());
				
			}

			for (Assembly a:validAssemblies) {
				File cifFile = params.getOutputFile(EppicParams.ASSEMBLIES_COORD_FILES_SUFFIX+"."+a.getId()+ EppicParams.MMCIF_FILE_EXTENSION);

				pr.generateAssemblyPng(a, cifFile,  
						params.getBaseName()+EppicParams.ASSEMBLIES_COORD_FILES_SUFFIX+"."+a.getId());
			}
			
		} catch (IOException e) {
			throw new EppicException(e, "Couldn't write thumbnails, PyMOL pse/pml files. "+e.getMessage(),true);
		} catch (InterruptedException e) {
			throw new EppicException(e, "Couldn't generate thumbnails, PyMOL pse/pml files, PyMOL thread interrupted: "+e.getMessage(),true);
		}

	}

	public void doWriteFinalFiles() throws EppicException {
		
		if (params.isGenerateModelSerializedFile()) {
			
			modelAdaptor.setInterfaceWarnings(); // first we call this method to add all the cached warnings
			modelAdaptor.writeSerializedModelFile(params.getOutputFile(EppicParams.SERIALIZED_MODEL_FILE_SUFFIX));

			// finally we write a signal file for the wui to know that job is finished
			try {
				FileWriter fw = new FileWriter(new File(params.getOutDir(), EppicParams.FINISHED_FILE_NAME));
				fw.close();
			} catch (IOException e) {
				throw new EppicException(e, "Couldn't write the finished file", true);
			}
		}

	}
	
	private void findUniqueChains() {
		StringBuilder sb = new StringBuilder();
		sb.append("Unique sequences: ");
		
		for (Compound chainCluster:pdb.getCompounds()) {
			// in mmCIF files some sugars are annotated as compounds with no chains linked to them, e.g. 3s26
			if (chainCluster.getChains().isEmpty()) continue;

			sb.append(DataModelAdaptor.getChainClusterString(chainCluster));
			sb.append(" ");
		}
		
		LOGGER.info(sb.toString());
	}
	
	public void doFindEvolContext() throws EppicException {
		if (interfaces.size()==0) return;
		
		findUniqueChains();
		
		try {
			cecs = new ChainEvolContextList(pdb,params);
		} catch (SQLException e) {
			throw new EppicException(e,"Could not connect to local UniProt database server: "+e.getMessage(),true);
		}
		
		// a) getting the uniprot ids corresponding to the query (the pdb sequence)
		writeStep("Finding Homologs and Calculating Entropies");		
		cecs.retrieveQueryData(params);
		modelAdaptor.setUniProtVersion(cecs.getUniprotVer());
		
		// b) getting the homologs and sequence data and filtering it
		cecs.retrieveHomologs(params);

		// c) align
		cecs.align(params);
		
		// d) computing entropies
		cecs.computeEntropies(params);
		
	}
	
	public void doEvolScoring() throws EppicException {
		if (interfaces.size()==0) return;

		iecList = new InterfaceEvolContextList(interfaces, cecs);
		iecList.setUsePdbResSer(params.isUsePdbResSer());

		writeStep("Scoring Interfaces");
		
		if (params.isDoEvolScoring()) {

			// setting cut-offs
			iecList.setCoreRimScoreCutoff(params.getCoreRimScoreCutoff());
			iecList.setCoreSurfScoreCutoff(params.getCoreSurfScoreCutoff());
			iecList.setMinNumSeqs(params.getMinNumSeqs());
			
			// core-rim
			iecList.setCoreRimPredBsaToAsaCutoff(params.getCAcutoffForRimCore(), params.getMinAsaForSurface()); // calls calcRimAndCores as well
			iecList.scoreCoreRim();

			// core-surface
			iecList.setCoreSurfacePredBsaToAsaCutoff(params.getCAcutoffForZscore(), params.getMinAsaForSurface()); // calls calcRimAndCores as well
			iecList.scoreCoreSurface();

			// note this adds also the entropies to the residue details
			modelAdaptor.setEvolScores(iecList);
				
		}

		
	}
	
	public void doCombinedScoring() throws EppicException {
		if (interfaces.size()==0) return;
		
		// interface scoring
		List<CombinedPredictor> cps = new ArrayList<CombinedPredictor>();
		for (int i=0;i<iecList.size();i++) {
			CombinedPredictor cp = 
					new CombinedPredictor(iecList.get(i), gps.get(i), iecList.get(i).getEvolCoreRimPredictor(), iecList.get(i).getEvolCoreSurfacePredictor());
			cp.computeScores();
			cps.add(cp);
		}
		
		// interface cluster scoring
		List<CombinedClusterPredictor> ccps = new ArrayList<CombinedClusterPredictor>();		
		for (StructureInterfaceCluster interfaceCluster:interfaces.getClusters(EppicParams.CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF)) {
			List<CombinedPredictor> ccpsForCluster = new ArrayList<CombinedPredictor>();
			
			for (int i=0;i<interfaces.size();i++) {
				if ( interfaces.get(i+1).getCluster().getId()==interfaceCluster.getId()) {
					ccpsForCluster.add(cps.get(i));
				}
			}
			
			CombinedClusterPredictor ccp = new CombinedClusterPredictor(ccpsForCluster);

			ccp.computeScores();
			ccps.add(ccp);
			iecList.setCombinedClusterPredictor(interfaceCluster.getId(), ccp);
		}			

		modelAdaptor.setCombinedPredictors(cps, ccps);

		params.getProgressLog().println("Done scoring");
	}
	
	private void writeStep(String text) {
		if (stepsLogFile==null) return;
		try {
			PrintStream stepsLog = new PrintStream(stepsLogFile);
			stepsLog.println("step_num="+stepCount);
			stepsLog.println("step="+text);
			stepsLog.println("step_total="+STEPS_TOTAL);
			stepCount++;
			stepsLog.close();
		} catch(FileNotFoundException e) {
			LOGGER.error("Couldn't write to steps log file "+stepsLogFile);
		}
	}
	
	/**
	 * The main of EPPIC  
	 */
	public static void main(String[] args){
		
		Main eppicMain = new Main();
		
		eppicMain.run(args);
	}
	
	/**
	 * Run the full eppic analysis given a parameters object
	 * @param params
	 */
	public void run(EppicParams params) {
		this.params = params;
		run(false);
	}
	
	/**
	 * Run the full eppic analysis given the command line arguments (which are then converted into an {@link EppicParams} object)
	 * @param args
	 */
	public void run(String[] args) {
		
		try {
			params.parseCommandLine(args);
			
		} catch (EppicException e) {
			LOGGER.error(e.getMessage());
			e.exitIfFatal(1);
		}
		
		run(true);
	}
	
	private void run(boolean loadConfigFile) {

				
		long start = System.nanoTime();

		try {
									

			// this has to come after getting the command line args, since it reads the location and name of log file from those
			setUpLogging();

			
			LOGGER.info(EppicParams.PROGRAM_NAME+" version "+EppicParams.PROGRAM_VERSION);
			LOGGER.info("Build git SHA: {}", EppicParams.BUILD_GIT_SHA);
			
			if (loadConfigFile)
				loadConfigFile();
			
			try {
				LOGGER.info("Running in host "+InetAddress.getLocalHost().getHostName());
			} catch (UnknownHostException e) {
				LOGGER.warn("Could not determine host where we are running.");
			}
			
			// 0 load pdb
			doLoadPdb();

			// 1 finding interfaces
			doFindInterfaces();
			
					
			// 2 find the assemblies
			doFindAssemblies();

			
			// TODO call doHBPlus when fixed
			// try hbplus if executable is set, writes pdb files needed for it (which then are overwritten in doWritePdbFiles)
			//doHBPlus();
			
			doGeomScoring();
			
			if (params.isDoEvolScoring()) {
				// 3 finding evolutionary context		
				doFindEvolContext();

				// 4 scoring
				doEvolScoring();
				
				// 5 combined scoring
				doCombinedScoring();
				
			}
			
			// 6 score assemblies and predict most likely assembly
			doAssemblyScoring();
			
			// 7 write TSV files (only if not in -w) 	
			doWriteTextOutputFiles();
			
			// 8 write coordinate files (only if in -l)
			doWriteCoordFiles();
			
			// 9 write assembly diagrams (only if in -P)
			doWriteAssemblyDiagrams();
			
			// 10 writing pymol files (only if in -l)
			doWritePymolFiles();
			
			// 11 writing out the model serialized file and "finish" file for web ui (only if in -w)
			doWriteFinalFiles();

			

			long end = System.nanoTime();
			LOGGER.info("Finished successfully (total runtime "+((end-start)/1000000000L)+"s)");

		} catch (EppicException e) {
			LOGGER.error(e.getMessage());
			e.exitIfFatal(1);
		} 
		
		catch (Exception e) {

			StringBuilder stack = new StringBuilder();
			for (StackTraceElement el:e.getStackTrace()) {
				stack.append("\tat ").append(el.toString()).append("\n");				
			}
			LOGGER.error("Unexpected error. Stack trace:\n"+e+"\n"+stack.toString()+
					"\nPlease report a bug to "+EppicParams.CONTACT_EMAIL);
			System.exit(1);
		}
		
		
	}
	
	public DataModelAdaptor getDataModelAdaptor() {
		return modelAdaptor;
	}
	

}
