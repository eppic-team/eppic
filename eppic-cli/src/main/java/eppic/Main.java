package eppic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import eppic.assembly.*;
import eppic.model.db.InterfaceClusterDB;
import eppic.model.db.InterfaceDB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.biojava.nbio.core.sequence.io.util.IOUtils;
import org.biojava.nbio.structure.*;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.biojava.nbio.structure.io.PDBFileParser;
import org.biojava.nbio.structure.chem.ChemCompGroupFactory;
import org.biojava.nbio.structure.chem.DownloadChemCompProvider;
import org.biojava.nbio.structure.io.StructureFiletype;
import org.biojava.nbio.structure.io.cif.CifStructureConverter;
import org.biojava.nbio.structure.xtal.CrystalBuilder;
import org.biojava.nbio.structure.xtal.SpaceGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.assembly.gui.LatticeGUIMustache;
import eppic.assembly.layout.LayoutUtils;
import eppic.commons.util.FileTypeGuesser;
import eppic.predictors.CombinedClusterPredictor;
import eppic.predictors.CombinedPredictor;
import eppic.predictors.GeometryClusterPredictor;
import eppic.predictors.GeometryPredictor;
import picocli.CommandLine;

import javax.vecmath.Matrix4d;

/**
 * The eppic main class to execute the CLI workflow.
 * 
 * 
 * @author Jose Duarte
 *
 */
@CommandLine.Command(
        name = EppicParams.PROGRAM_NAME,
        mixinStandardHelpOptions = true, // adds -h, --help, -V, --version
        description = "EPPIC: Evolutionary Protein-Protein Interface Classifier")
public class Main implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
	
	private static final int STEPS_TOTAL = 4;
	
    // fields
    @CommandLine.Mixin
    private CliParams cliParams;

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

	// needed for testing
	protected CrystalAssemblies getCrystalAssemblies() {
	    return validAssemblies;
    }

	// needed for testing
    protected StructureInterfaceList getInterfaces() {
		return interfaces;
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

	private void logBuildAndHost() {
        LOGGER.info(EppicParams.PROGRAM_NAME + " version {}", EppicParams.PROGRAM_VERSION);
		LOGGER.info("Build git SHA: {}", EppicParams.BUILD_GIT_SHA);

		try {
            LOGGER.info("Running in host {}", InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			LOGGER.warn("Could not determine host where we are running.");
		}
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
            LOGGER.error("Error while reading from config file: {}", e.getMessage());
			System.exit(1);
		} catch (EppicException e) {
			LOGGER.error(e.getMessage());
			System.exit(1);
		}

	}

	public long doLoadPdb() throws EppicException {

		long start = System.currentTimeMillis();
		
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

		FileParsingParameters fileParsingParams = new FileParsingParameters();
		// TODO we should parse PDB files with no X padding if no SEQRES is found. Otherwise matching to uniprot doesn't work in many cases
		//fileParsingParams.set????
		fileParsingParams.setAlignSeqRes(true);
		fileParsingParams.setParseBioAssembly(true);

		pdb = null;
		long modDate = 0;
		try {
			if (!params.isInputAFile()) {

				try {
					pdb = readStructureFromPdbCode(fileParsingParams);
					if (params.getCifRepositoryTemplateUrl()!=null) {
						modDate = getLastModDateFromUrl(params.getPdbCode().toLowerCase());
					}

				} catch(IOException e) {
					throw new EppicException(e,"Couldn't get cif file from AtomCache for code "+params.getPdbCode()+". Error: "+e.getMessage(),true);
				}
					
			} else {

				int fileType = FileTypeGuesser.guessFileType(params.getInFile());
				fileParsingParams.setParseBioAssembly(false);

				if (fileType==FileTypeGuesser.CIF_FILE) {

					pdb = CifStructureConverter.fromInputStream(IOUtils.openFile(params.getInFile()), fileParsingParams);

				} else if (fileType==FileTypeGuesser.PDB_FILE || fileType==FileTypeGuesser.RAW_PDB_FILE) {

					PDBFileParser parser = new PDBFileParser();
					parser.setFileParsingParameters(fileParsingParams);

					pdb = parser.parsePDBFile(new FileInputStream(params.getInFile()));
					
				} else {
					throw new EppicException(null, "Could not guess if file is PDB or mmCIF. If PDB file please make sure your file's first line starts with one of the usual tokens, e.g. REMARK, ATOM, CRYST1", true);
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

		if (!params.isInputAFile() && params.getCifRepositoryTemplateUrl()!=null && params.isGenerateModelSerializedFile()) {
			// Only for weekly update pipeline: use mod date from URL. Because the holdings json file has literally the actual file timestamps. Which not always coincide
			// with the last mod date written in mmCIF. So if we don't use the actual timestamps for the db then the incremental update logic would be wrong
			modelAdaptor.setPdbModDate(modDate);
		}

		return System.currentTimeMillis() - start;
	}

	private Structure readStructureFromPdbCode(FileParsingParameters fileParsingParams) throws StructureException, IOException {

		if (params.getCifRepositoryTemplateUrl()!=null) {

			// 1. Use a different http repository than the official PDB one

			String pdbCode = params.getPdbCode().toLowerCase();
			String url = getPathUrl(params.getCifRepositoryTemplateUrl(), pdbCode);
			URL cifGzUrl = new URL(url);
			pdb = CifStructureConverter.fromInputStream(new GZIPInputStream(cifGzUrl.openStream()), fileParsingParams);

		} else {

			// 2. Use standard PDB http repository as available via BioJava

			AtomCache cache;

			if (params.getAtomCachePath() != null) {
				LOGGER.info("Path given in ATOM_CACHE_PATH, setting AtomCache to {} and ignoring env variable PDB_DIR", params.getAtomCachePath());
				cache = new AtomCache(params.getAtomCachePath());
			} else {
				cache = new AtomCache();
			}
			cache.setFiletype(StructureFiletype.CIF);

			// we set default fetch behavior to FETCH_IF_OUTDATED which is the closest to rsync
			if (params.getFetchBehavior() != null) {
				cache.setFetchBehavior(params.getFetchBehavior());
			} else {
				cache.setFetchBehavior(EppicParams.DEF_FETCH_BEHAVIOR);
			}
			cache.setFileParsingParams(fileParsingParams);

			StructureIO.setAtomCache(cache);

			pdb = StructureIO.getStructure(params.getPdbCode());

		}

		return pdb;
	}

	/**
	 * Returns the full path to the cif file given a base URL and PDB id
	 * @param filesFolderPath a URL string with placeholders {middle} (for middle 2 letters of PDB id) and {id} (for PDB id)
	 * @param pdbCode the PDB id
	 * @return the full URL to the cif file
	 */
	private static String getPathUrl(String filesFolderPath, String pdbCode) {
		return filesFolderPath.replaceAll("\\{id}", pdbCode.toLowerCase()).replaceAll("\\{middle}", pdbCode.substring(1, 3).toLowerCase());
	}

	private long getLastModDateFromUrl(String pdbCode) throws IOException {
		URL cifGzUrl = new URL(getPathUrl(params.getCifRepositoryTemplateUrl(), pdbCode));
		HttpURLConnection httpCon = (HttpURLConnection) cifGzUrl.openConnection();
		return httpCon.getLastModified();
	}

	public long doFindInterfaces() throws EppicException {

		long start = System.currentTimeMillis();

		params.getProgressLog().println("Calculating possible interfaces...");

		LOGGER.info("Calculating possible interfaces");
		CrystalBuilder interfFinder;
		Map<String,String> chainOrigNames = null;
		Map<String, Matrix4d > chainNcsOps = null;
		if (modelAdaptor.getPdbInfo().isNcsOpsPresent()) {
			chainOrigNames = new HashMap<>();
			chainNcsOps = new HashMap<>();
			CrystalBuilder.expandNcsOps(pdb,chainOrigNames,chainNcsOps);
			interfFinder = new CrystalBuilder(pdb,chainOrigNames,chainNcsOps);
		} else {
			interfFinder = new CrystalBuilder(pdb);
		}

		modelAdaptor.setChainOrigNames(chainOrigNames);
		modelAdaptor.setChainNcsOps(chainNcsOps);

		modelAdaptor.setChainClustersData(pdb);

		interfaces = interfFinder.getUniqueInterfaces(EppicParams.INTERFACE_DIST_CUTOFF);
		LOGGER.info("Calculating ASAs");
		interfaces.calcAsas(params.getnSpherePointsASAcalc(), params.getNumThreads(), params.getMinSizeCofactorForAsa());
		interfaces.removeInterfacesBelowArea(EppicParams.MIN_INTERFACE_AREA_TO_KEEP);
		
		LOGGER.info("Interfaces calculated with "+params.getnSpherePointsASAcalc()+" sphere points.");

		LOGGER.info("Calculating interface clusters");

		int clustersSize = interfaces.getClusters(EppicParams.CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF).size();
		int numInterfaces = interfaces.size();
        LOGGER.info("Interface clustering done: {} interfaces - {} clusters", numInterfaces, clustersSize);
		StringBuilder msg = new StringBuilder("Interface clusters: ");
		for (int i=0; i<clustersSize;i++) {
			StructureInterfaceCluster cluster = interfaces.getClusters(EppicParams.CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF).get(i);
			msg.append(cluster.getId()).append(": ");
			for (StructureInterface interf:cluster.getMembers()) {
				msg.append(interf.getId()).append(" ");
			}
			if (i!=clustersSize-1) msg.append(", ");
		}
		LOGGER.info(msg.toString());

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

		return System.currentTimeMillis() - start;
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
		
			StringBuilder msg = new StringBuilder("Clashes (atoms distance below " + EppicParams.CLASH_DISTANCE + ") found in:");
			i = 0;
			for (StructureInterface interf:interfaces) {
				if (numClashesPerInterface[i]>0) {		
					msg.append("\nInterface ").append(interf.getId())
							.append(": ").append(interf.getMoleculeIds().getFirst()).append("+")
							.append(interf.getMoleculeIds().getSecond()).append(" (")
							.append(SpaceGroup.getAlgebraicFromMatrix(interf.getTransforms().getSecond().getMatTransform()))
							.append(") Clashes: ").append(numClashesPerInterface[i]);
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
				// New from 3.0.4 : when generating data for WUI, we won't stop when too many clashes.
				// Like this, too many clashes don't look like an error for the precalculation pipeline and for user jobs.
				// For WUI, a warning on top (plus warnings per interface) should replace the effect of this.
				if (!params.isGenerateModelSerializedFile()) {
					throw new EppicException(null, excptionMsg, true);	
				} else {
					LOGGER.warn(excptionMsg);
				}
								
			} else { 
				LOGGER.warn(msg.toString());
			}

		}
		
	}
	
	public long doFindAssemblies() {

		long start = System.currentTimeMillis();

		params.getProgressLog().println("Calculating possible assemblies...");

		try {
			validAssemblies = new CrystalAssemblies(pdb, interfaces, params.isForceContractedAssemblyEnumeration());
		} catch (ConcurrentModificationException e) {
			LOGGER.error("Caught ConcurrentModificationException while finding assemblies. This is a known bug in the contraction of heteromeric assemblies graphs");
			if (params.isGenerateModelSerializedFile()) {
				// TODO fix the bug in eppic.assembly.GraphContractor.contract(). This is a workaround so that the precomp workflow
				//  doesn't report an error and can proceed
				LOGGER.error("Will exit now with success, though no output files will be produced. This is so that the precomputation workflow can run in full. Note that this is a bug.");
				System.exit(0);
			} else {
				throw e;
			}
		}

		StringBuilder sb = new StringBuilder();
		for (Assembly a: validAssemblies) {
			sb.append(a.toString()).append(" ");
		}
		LOGGER.info("There are {} topologically possible assemblies: {}", validAssemblies.size(), sb.toString());
					
		params.getProgressLog().println("Done");

		return System.currentTimeMillis() - start;
	}
	
	public long doAssemblyScoring() {

		long start = System.currentTimeMillis();

		if (params.isDoEvolScoring()) {
			validAssemblies.setInterfaceEvolContextList(iecList);

			validAssemblies.score();
		}

		modelAdaptor.setAssemblies(validAssemblies);

		modelAdaptor.setPdbBioUnits(pdb.getPDBHeader().getBioAssemblies(), validAssemblies, pdb);

		return System.currentTimeMillis() - start;
	}
	
	public long doGeomScoring() throws EppicException {

		long start = System.currentTimeMillis();

		if (interfaces.size()==0) {
			// no interfaces found at all, can happen e.g. in NMR structure with 1 chain, e.g. 1nmr
			LOGGER.info("No interfaces found, nothing to analyse.");
			params.getProgressLog().println("No interfaces found, nothing to analyse.");
			// we still continue so that the web interface can pick it up too
			return System.currentTimeMillis() - start;
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

		return System.currentTimeMillis() - start;
	}
	
	public long doWriteTextOutputFiles() throws EppicException {

		long start = System.currentTimeMillis();

		// we don't write text files if in -w
		if (params.isGenerateModelSerializedFile()) return System.currentTimeMillis() - start;

		TextOutputWriter toW = new TextOutputWriter(modelAdaptor.getPdbInfo(), modelAdaptor.getInterfFeatures(), params);

		// 0 write .A.aln file
		try {
			toW.writeAlnFiles();
		} catch (IOException e) {
			throw new EppicException(e, "Could not write the homologs alignment files: "+e.getMessage(), true);
		}

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

		return System.currentTimeMillis() - start;
	}
	
	public long doWriteCoordFiles() throws EppicException {

		long start = System.currentTimeMillis();

		if (!params.isGenerateOutputCoordFiles()) return System.currentTimeMillis() - start; // no -p or -l specified: nothing to do

		if (params.isGenerateThumbnails()) {
			params.getProgressLog().println("Writing PyMOL files");
			writeStep("Generating Thumbnails and PyMOL Files");
			LOGGER.info("Generating PyMOL files");
		}

		try {

			PymolRunner pr = new PymolRunner(params.getPymolExe());

			// since 3.1.0 there's no need to write the evol scores out to files
			
			// INTERFACE files
			for (StructureInterface interf : interfaces) {
				// a hack necessary to handle reduced redundancy in structures with NCS
				if (modelAdaptor.getPdbInfo().isNcsOpsPresent() && modelAdaptor.getPdbInfo().getInterface(interf.getId())==null) {
					LOGGER.info("Skipping generation of interface coordinate file for redundant NCS interface {}", interf.getId());
					continue;
				}
				// note that tempCoordsDir will only be different from outDir if -t option was specified (so that we can specify a fast in memory storage dir in precomputation)
				File outputFile = params.getOutputFile(params.getTempCoordFilesDir(), EppicParams.INTERFACES_COORD_FILES_SUFFIX + "." + interf.getId() + EppicParams.MMCIF_FILE_EXTENSION);
				PrintStream ps = new PrintStream(new GZIPOutputStream(new FileOutputStream(outputFile)));				
				ps.print(interf.toMMCIF());
				ps.close();

				if (params.isGenerateThumbnails()) {
					pr.generateInterfacePng(interf, outputFile, params.getOutDir(),
							params.getBaseName() + EppicParams.INTERFACES_COORD_FILES_SUFFIX + "." + interf.getId());
					LOGGER.info("Generated PyMOL files for interface "+interf.getId());
				}

				if (params.isGenerateModelSerializedFile()) {
					// since 3.1.0 we don't need to keep the precomputed files, because we can generate on the fly
					outputFile.delete();
				}

				if (params.isGeneratePdbFiles()) { 
					outputFile = params.getOutputFile(EppicParams.INTERFACES_COORD_FILES_SUFFIX + "." + interf.getId() + EppicParams.PDB_FILE_EXTENSION);
					ps = new PrintStream(new GZIPOutputStream(new FileOutputStream(outputFile)));
					ps.print(interf.toPDB());
					ps.close();
				}


			}
				
			// ASSEMBLY files
			for (Assembly a:validAssemblies) {

				// note that tempCoordsDir will only be different from outDir if -t option was specified (so that we can specify a fast in memory storage dir in precomputation)
				File outputFile= params.getOutputFile(params.getTempCoordFilesDir(), EppicParams.ASSEMBLIES_COORD_FILES_SUFFIX+"." + a.getId() + EppicParams.MMCIF_FILE_EXTENSION);
				
				LOGGER.info("Writing assembly {} to {}", a.getId(), outputFile);
				a.writeToMmCifFile(outputFile);

				if (params.isGenerateThumbnails()) {
					pr.generateAssemblyPng(a, outputFile, params.getOutDir(),
							params.getBaseName()+EppicParams.ASSEMBLIES_COORD_FILES_SUFFIX+"."+a.getId());
                    LOGGER.info("Generated PyMOL files for assembly {}", a.getId());
				}

				if (params.isGenerateModelSerializedFile()) {
					// since 3.1.0 we don't need to keep the precomputed files, because we can generate on the fly
					outputFile.delete();
				}

				if (params.isGeneratePdbFiles()) {
					outputFile = params.getOutputFile(EppicParams.ASSEMBLIES_COORD_FILES_SUFFIX + "." + a.getId() + EppicParams.PDB_FILE_EXTENSION);
					a.writeToPdbFile(outputFile);
				}

			}
			
		} catch (IOException e) {
			throw new EppicException(e, "Couldn't write interface coordinate files or PyMOL png files. " + e.getMessage(), true);
		} catch (InterruptedException e) {
			throw new EppicException(e, "Couldn't generate PyMOL png files, PyMOL thread interrupted: "+e.getMessage(),true);
		}

		return System.currentTimeMillis() - start;
	}

	public long doWriteAssemblyDiagrams() throws EppicException {

		long start = System.currentTimeMillis();

		// should not happen, there should always be 1 assembly (the trivial no-interfaces engaged one)
		if (validAssemblies.getUniqueAssemblies().size() == 0) return System.currentTimeMillis() - start;

		if (!params.isGenerateDiagrams()) return System.currentTimeMillis() - start;
		
		params.getProgressLog().println("Writing Assembly Diagram files");
		writeStep("Generating assembly diagram Thumbnails");
		LOGGER.info("Generating Assembly Diagram files");

		try {
			LatticeGraph3D latticeGraph = new LatticeGraph3D(validAssemblies.getLatticeGraph());
			GraphvizRunner runner = new GraphvizRunner(params.getGraphvizExe());
			String fileFormat = "png";
			

			for (Assembly a:validAssemblies) {
				
				// Generate the png with the assembly diagram via invoking the dot executable

				File pngFile= params.getOutputFile(EppicParams.ASSEMBLIES_DIAGRAM_FILES_SUFFIX+"." + a.getId() + "."+EppicParams.THUMBNAILS_SIZE+"x"+EppicParams.THUMBNAILS_SIZE+"."+fileFormat);

				LOGGER.info("Writing diagram for assembly {} to {}",a.getId(),pngFile);
					
				// Filter down to this assembly
				// TODO this is not going to work for contracted graphs: both clusterIds and interfaceids are wrong! see issue https://github.com/eppic-team/eppic/issues/148
				SortedSet<Integer> clusterIds = GraphUtils.getDistinctInterfaceClusters(a.getAssemblyGraph().getSubgraph());
				Set<Integer> interfaceIds = GraphUtils.getDistinctInterfaces(a.getAssemblyGraph().getSubgraph());
				if (modelAdaptor.getPdbInfo().isNcsOpsPresent()) {
					// we have to hack the interface list removing the redundant NCS interfaces. In model (and wui) they aren't present
					Set<Integer> nonRedundantSet = new HashSet<>();
					for (InterfaceClusterDB icdb : modelAdaptor.getPdbInfo().getInterfaceClusters()) {
						for (InterfaceDB idb : icdb.getInterfaces()) {
							nonRedundantSet.add(idb.getInterfaceId());
						}
					}
					interfaceIds.removeIf( (Integer interfId) -> !nonRedundantSet.contains(interfId));
				}
				latticeGraph.filterEngagedClusters(clusterIds);
					
				LatticeGUIMustache guiThumb = new LatticeGUIMustache(LatticeGUIMustache.TEMPLATE_ASSEMBLY_DIAGRAM_THUMB, latticeGraph);
				guiThumb.setLayout2D(LayoutUtils.getDefaultLayout2D(latticeGraph.getCrystalCell()));
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
				if (params.getGraphvizExe()==null) {
					LOGGER.warn("GRAPHVIZ_EXE was not specified in eppic.conf. Will not generate assembly {} png", a.getId());
				} else {
					runner.generateFromDot(guiThumb, pngFile, fileFormat);
				}

			}

		} catch( IOException|InterruptedException e) {
			throw new EppicException(e, "Couldn't write assembly diagrams. " + e.getMessage(), true);
		}

		return System.currentTimeMillis() - start;
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


	public long doWriteFinalFiles() throws EppicException {

		long start = System.currentTimeMillis();
		
		if (params.isGenerateModelSerializedFile()) {
			
			modelAdaptor.setInterfaceWarnings(); // first we call this method to add all the cached warnings
			modelAdaptor.writeSerializedModelFiles(
					params.getOutputFile(EppicParams.SERIALIZED_PDBINFO_FILE_SUFFIX),
					params.getOutputFile(EppicParams.SERIALIZED_INTERF_FEATURES_FILE_SUFFIX),
					params.getOutputFile(EppicParams.SERIALIZED_FILES_ZIP_SUFFIX)
					);

			// finally we write a signal file for the wui to know that job is finished
			try {
				FileWriter fw = new FileWriter(new File(params.getOutDir(), EppicParams.FINISHED_FILE_NAME));
				fw.close();
			} catch (IOException e) {
				throw new EppicException(e, "Couldn't write the finished file", true);
			}
		}

		return System.currentTimeMillis() - start;
	}
	
	private void findUniqueChains() {
		StringBuilder sb = new StringBuilder();
		sb.append("Unique sequences: ");
		
		for (EntityInfo chainCluster:pdb.getEntityInfos()) {

			if (chainCluster.getType() == EntityType.POLYMER) {
				// in mmCIF files some sugars are annotated as compounds with no chains linked to them, e.g. 3s26
				if (chainCluster.getChains().isEmpty()) continue;

				sb.append(DataModelAdaptor.getChainClusterString(chainCluster));
				sb.append(" ");
			}
		}
		
		LOGGER.info(sb.toString());
	}
	
	public long doFindEvolContext() throws EppicException {

		long start = System.currentTimeMillis();

		if (interfaces.size()==0) return System.currentTimeMillis() - start;
		
		findUniqueChains();

		cecs = new ChainEvolContextList(pdb, params);
		cecs.initUniProtVer(params);
		
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

		return System.currentTimeMillis() - start;
	}
	
	public long doEvolScoring() throws EppicException {
		long start = System.currentTimeMillis();

		if (interfaces.size()==0) return System.currentTimeMillis() - start;

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

		return System.currentTimeMillis() - start;
	}
	
	public long doCombinedScoring() throws EppicException {
		long start = System.currentTimeMillis();

		if (interfaces.size()==0) return System.currentTimeMillis() - start;
		
		// interface scoring
		List<CombinedPredictor> cps = new ArrayList<>();
		for (int i=0;i<iecList.size();i++) {
			CombinedPredictor cp = 
					new CombinedPredictor(iecList.get(i), gps.get(i), iecList.get(i).getEvolCoreRimPredictor(), iecList.get(i).getEvolCoreSurfacePredictor());
			cp.computeScores();
			cps.add(cp);
		}
		
		// interface cluster scoring
		List<CombinedClusterPredictor> ccps = new ArrayList<>();
		for (StructureInterfaceCluster interfaceCluster:interfaces.getClusters(EppicParams.CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF)) {
			List<CombinedPredictor> ccpsForCluster = new ArrayList<>();
			
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

		return System.currentTimeMillis() - start;
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

    @Override
    public void run() {
        // Here picocli has already populated cliParams (and any other @Option fields)
        try {
            cliParams.checkCommandLineInput();
        } catch (EppicException e) {
            LOGGER.error(e.getMessage());
            e.exitIfFatal(1);
        }
        // Convert cliParams -> EppicParams as needed
        this.params = cliParams.toEppicParams(); // or however you build EppicParams
        this.stepCount = 1;

        // reuse your existing workflow
        run(true);
    }

    /**
     * The main of EPPIC
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
	
	/**
	 * Run the full eppic analysis given a parameters object
	 * @param params the parameters
	 */
	public void run(EppicParams params) {
		this.params = params;
		run(false);
	}
	
	private void run(boolean loadConfigFile) {

		long start = System.currentTimeMillis();
		long loadPdbTime, findInterfTime, findAssembliesTime, geomScoringTime,
				findEvolContextTime, evolScoringTime, combinedScoringTime, assemblyScoringTime,
				writeTextOutputFilesTime, writeCoordFilesTime, writeAssemblyDiagramsTime, writeFinalFilesTime;
		findEvolContextTime = evolScoringTime = combinedScoringTime = 0;

		try {

			// this has to come after getting the command line args, since it reads the location and name of log file from those
			setUpLogging();
			
			logBuildAndHost();

			if (loadConfigFile) loadConfigFile();
			
			// 0 load pdb
			loadPdbTime = doLoadPdb();

			// 1 finding interfaces
			findInterfTime = doFindInterfaces();
					
			// 2 find the assemblies
			findAssembliesTime = doFindAssemblies();
			
			// TODO call doHBPlus when fixed
			// try hbplus if executable is set, writes pdb files needed for it (which then are overwritten in doWritePdbFiles)
			//doHBPlus();
			
			geomScoringTime = doGeomScoring();

			if (params.isDoEvolScoring()) {
				// 3 finding evolutionary context		
				findEvolContextTime = doFindEvolContext();

				// 4 scoring
				evolScoringTime = doEvolScoring();
				
				// 5 combined scoring
				combinedScoringTime = doCombinedScoring();
				
			}
			
			// 6 score assemblies and predict most likely assembly
			assemblyScoringTime = doAssemblyScoring();
			
			// 7 write TSV files (only if not in -w) 	
			writeTextOutputFilesTime = doWriteTextOutputFiles();
			
			// 8 write coordinate files (in -p or -l) and pymol png files (-l)
			writeCoordFilesTime = doWriteCoordFiles();
			
			// 9 write assembly diagrams (only if in -P)
			writeAssemblyDiagramsTime = doWriteAssemblyDiagrams();
			
			// 10 writing out the model serialized file and "finish" file for web ui (only if in -w)
			writeFinalFilesTime = doWriteFinalFiles();

			long end = System.currentTimeMillis();
			String partialTimingsStr = "[loadPdbTime, findInterfTime, findAssembliesTime, geomScoringTime, " +
					"findEvolContextTime, evolScoringTime, combinedScoringTime, assemblyScoringTime, " +
					"twriteTextOutputFilesTime, writeCoordFilesTime, writeAssemblyDiagramsTime, writeFinalFilesTime]";
			LOGGER.info("Finished successfully (total runtime {}s). Partial timings in ms {}: {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}",
					(end-start)/1000L, partialTimingsStr,
					loadPdbTime, findInterfTime, findAssembliesTime, geomScoringTime,
					findEvolContextTime, evolScoringTime, combinedScoringTime, assemblyScoringTime,
					writeTextOutputFilesTime, writeCoordFilesTime, writeAssemblyDiagramsTime, writeFinalFilesTime);

		} catch (EppicException e) {
			LOGGER.error(e.getMessage());
			e.exitIfFatal(1);
		} 
		
		catch (Exception e) {

			StringBuilder stack = new StringBuilder();
			for (StackTraceElement el:e.getStackTrace()) {
				stack.append("\tat ").append(el.toString()).append("\n");				
			}
            LOGGER.error("Unexpected error. Stack trace:\n{}\n{}\nPlease report a bug to " + EppicParams.CONTACT_EMAIL, e, stack.toString());
			System.exit(1);
		}

	}
	
	public DataModelAdaptor getDataModelAdaptor() {
		return modelAdaptor;
	}
	

}
