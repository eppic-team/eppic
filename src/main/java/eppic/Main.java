package eppic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import eppic.predictors.CombinedPredictor;
import eppic.predictors.GeometryPredictor;
import owl.core.structure.BioUnitAssignmentType;
import owl.core.structure.ChainCluster;
import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.InterfaceCluster;
import owl.core.structure.InterfacesFinder;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbBioUnitList;
import owl.core.structure.PdbChain;
import owl.core.structure.PdbLoadException;
import owl.core.structure.SpaceGroup;
import owl.core.structure.graphs.AICGraph;
import owl.core.util.FileFormatException;
import owl.core.util.Goodies;
import owl.core.util.RegexFileFilter;

public class Main {
	
	
	// THE ROOT LOGGER (log4j)
	private static final Logger ROOTLOGGER = Logger.getRootLogger();
	private static final Log LOGGER = LogFactory.getLog(Main.class);
	
	private static final int STEPS_TOTAL = 4;
	
	// fields
	private EppicParams params;
	
	private PdbAsymUnit pdb;
	private ChainInterfaceList interfaces;
	private ChainEvolContextList cecs;
	private InterfaceEvolContextList iecList;
	private List<GeometryPredictor> gps;
	
	private File stepsLogFile;
	private int stepCount;
	
	private DataModelAdaptor modelAdaptor;
		
	public Main() {
		this.params = new EppicParams();
		this.stepCount = 1;
	}
	

		
	public void setUpLogging() {
		
		// setting up the file logger for log4j
		try {
			FileAppender fullLogAppender = new FileAppender(new PatternLayout("%d{ABSOLUTE} %5p - %m%n"),params.getOutDir()+"/"+params.getBaseName()+".log",false);
			fullLogAppender.setThreshold(Level.INFO);
			ConsoleAppender errorAppender = new ConsoleAppender(new PatternLayout("%5p - %m%n"),ConsoleAppender.SYSTEM_ERR);
			errorAppender.setThreshold(Level.ERROR);
			ConsoleAppender outAppender = new ConsoleAppender(new PatternLayout("%d{ABSOLUTE} %5p - %m%n"),ConsoleAppender.SYSTEM_OUT);
			outAppender.setThreshold(Level.DEBUG);
			ROOTLOGGER.addAppender(fullLogAppender);
			ROOTLOGGER.addAppender(errorAppender);
			if (params.getProgressLogFile()!=null) {
				// commenting out the error logging to progress file (was needed for server, but now it will read stderr from sge file)
				//FileAppender fileErrorAppender = new FileAppender(new PatternLayout("%5p - %m%n"),params.getProgressLogFile().getAbsolutePath(),true);
				//fileErrorAppender.setThreshold(Level.ERROR);
				//ROOTLOGGER.addAppender(fileErrorAppender);
				// the steps log file needed for the server, we only initialise it if a -L progress log file was passed (as that is only used by server)
				stepsLogFile = new File(params.getOutDir(),params.getBaseName()+EppicParams.STEPS_LOG_FILE_SUFFIX);
			}
			if (params.getDebug())
				ROOTLOGGER.addAppender(outAppender);
		} catch (IOException e) {
			System.err.println("Couldn't open log file "+params.getOutDir()+"/"+params.getBaseName()+".log"+" for writing.");
			System.err.println(e.getMessage());
			System.exit(1);
		}
	    ROOTLOGGER.setLevel(Level.INFO);

	    // TODO now using the apache common logging framework which acts as a meta framework for other frameworks such
	    //      as log4j or java logging. We always should instantiate loggers from apache commons (LogFactory.getLog()).
	    //      Left to do is still configure the logging in CRK properly by using the apache commons mechanism. At the moment
	    //      the logging configuration is done using log4j/java logger mechanisms.

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
			} else if (!params.isInputAFile() || params.isDoScoreEntropies()) {
				LOGGER.error("No config file could be read at "+userConfigFile+
						". Please set one if you want to run the program using PDB codes as input with -i or if you want to run evolutionary predictions (-s).");
				System.exit(1);
			}
		} catch (IOException e) {
			LOGGER.fatal("Error while reading from config file: " + e.getMessage());
			System.exit(1);
		} catch (EppicException e) {
			LOGGER.error(e.getMessage());
			System.exit(1);
		}

	}

	public void doLoadPdb() throws EppicException {
		params.getProgressLog().println("Loading PDB data: "+(params.getInFile()==null?params.getPdbCode():params.getInFile().getName()));
		writeStep("Calculating Interfaces");
		pdb = null;
		File cifFile = null; // if given a pdb code in command line we will store the cif file here
		try {
			if (!params.isInputAFile()) {
				cifFile = new File(params.getOutDir(),params.getPdbCode() + ".cif");
				cifFile.deleteOnExit();
				try {
					PdbAsymUnit.grabCifFile(params.getLocalCifDir(), params.getPdbFtpCifUrl(), params.getPdbCode(), cifFile, params.isUseOnlinePdb());
				} catch(IOException e) {
					throw new EppicException(e,"Couldn't get cif file for code "+params.getPdbCode()+" from ftp or couldn't uncompress it. Error: "+e.getMessage(),true);
				}
					
			} else {
				cifFile = params.getInFile();
			}
			// we parse PDB files with no X padding if no SEQRES is found. Otherwise matching to uniprot doesn't work in many cases
			pdb = new PdbAsymUnit(cifFile, PdbAsymUnit.DEFAULT_MODEL, false);
		} catch (FileFormatException e) {
			throw new EppicException(e,"File format error: "+e.getMessage(),true);
		} catch (PdbLoadException e) {
			throw new EppicException(e,"Couldn't load file "+cifFile.getName()+". Error: "+e.getMessage(),true);
		} catch (IOException e) {
			throw new EppicException(e,"Problems reading PDB data from "+cifFile+". Error: "+e.getMessage(),true);
		}
		
		if (pdb.getCrystalCell()==null) {
			LOGGER.warn("No crystal information found in source "+params.getPdbCode()+". Only asymmetric unit interfaces will be calculated.");
		}
		
		// we strip the H atoms: surface calculations should not have them (otherwise comparisons of structures with/without H arn't good)
		pdb.removeHatoms();
		
		// for the webui
		modelAdaptor = new DataModelAdaptor();
		modelAdaptor.setParams(params);
		modelAdaptor.setPdbMetadata(pdb);
		
		
	}
	
	public void doLoadInterfacesFromFile() throws EppicException {
		try {
			params.getProgressLog().println("Loading interfaces enumeration from file "+params.getInterfSerFile());
			LOGGER.info("Loading interfaces enumeration from file "+params.getInterfSerFile());
			interfaces = (ChainInterfaceList)Goodies.readFromFile(params.getInterfSerFile());
		} catch (ClassNotFoundException e) {
			throw new EppicException(e,"Couldn't load interface enumeration binary file: "+e.getMessage(),true);
		} catch (IOException e) {
			throw new EppicException(e,"Couldn't load interface enumeration binary file: "+e.getMessage(),true);
		}
		
		if (!pdb.getPdbCode().equals(interfaces.get(1).getFirstMolecule().getPdbCode())) {
			throw new EppicException(null,"PDB codes of given PDB entry/file and given interface enumeration binary file don't match.",true);
		}
		
	}

	public void doFindInterfaces() throws EppicException {

		params.getProgressLog().println("Calculating possible interfaces...");
		LOGGER.info("Calculating possible interfaces");
		InterfacesFinder interfFinder = new InterfacesFinder(pdb);
		interfaces = interfFinder.getAllInterfaces(EppicParams.INTERFACE_DIST_CUTOFF, 
				params.getnSpherePointsASAcalc(), params.getNumThreads(), true, false, 
				params.getMinSizeCofactorForAsa(),
				EppicParams.MIN_INTERFACE_AREA_TO_KEEP);		
		LOGGER.info("Interfaces calculated with "+params.getnSpherePointsASAcalc()+" sphere points.");

		LOGGER.info("Calculating interface clusters");
		interfaces.initialiseClusters(pdb, EppicParams.CLUSTERING_RMSD_CUTOFF, EppicParams.CLUSTERING_MINATOMS, EppicParams.CLUSTERING_ATOM_TYPE);

		int clustersSize = interfaces.getClusters().size();
		int numInterfaces = interfaces.size();
		LOGGER.info("Interface clustering done: "+numInterfaces+" interfaces - "+clustersSize+" clusters");
		String msg = "Interface clusters: ";
		for (int i=0; i<clustersSize;i++) {
			InterfaceCluster cluster = interfaces.getClusters().get(i);
			msg += cluster.toString();
			if (i!=clustersSize-1) msg += ", "; 
		}
		LOGGER.info(msg); 

		if (interfFinder.hasCofactors()) {
			LOGGER.info("Cofactors of more than "+params.getMinSizeCofactorForAsa()+" atoms present: they will be taken into account for ASA calculations");
			for (String pdbChainCode:pdb.getPdbChainCodes()) {
				LOGGER.info("Chain "+pdbChainCode+": "+interfFinder.getNumCofactorsForPdbChainCode(pdbChainCode)+" cofactor(s) - "+
						interfFinder.getCofactorsInfoString(pdbChainCode));
			}
		} else {
			if (params.getMinSizeCofactorForAsa()<0) {
				LOGGER.info("No minimum size for cofactors set. All cofactors will be ignored for ASA calculations");
			} else {
				LOGGER.info("No cofactors of more than "+params.getMinSizeCofactorForAsa()+" atoms present. Cofactors will be ignored for ASA calculations");
			}
		}


		LOGGER.info("Minimum ASA for a residue to be considered surface: "+String.format("%2.0f",params.getMinAsaForSurface()));
		
		params.getProgressLog().println("Done");


		// checking for clashes
		if (interfaces.hasInterfacesWithClashes()) {
			msg = "Clashes (atoms distance below "+AICGraph.CLASH_DISTANCE+") found in:";
			List<ChainInterface> clashyInterfs = interfaces.getInterfacesWithClashes();
			boolean tooManyClashes = false;
			for (ChainInterface clashyInterf:clashyInterfs) {
				int numClashes = clashyInterf.getNumClashes();
				if (numClashes>EppicParams.NUM_CLASHES_FOR_ERROR) tooManyClashes = true;
				msg+=("\nInterface: "+clashyInterf.getFirstMolecule().getPdbChainCode()+"+"
						+clashyInterf.getSecondMolecule().getPdbChainCode()+" ("+
						SpaceGroup.getAlgebraicFromMatrix(clashyInterf.getSecondTransf().getMatTransform())+
						") Clashes: "+numClashes); 
			}
			
			if (tooManyClashes) {
				throw new EppicException(null, "Too many clashes in at least one interface, most likely there is an error in this structure. "+msg , true);				
			} else { 
				LOGGER.warn(msg);
			}

		}

		interfaces.calcRimAndCores(params.getCAcutoffForGeom(), params.getMinAsaForSurface());
		
		try {
			// if no interfaces found (e.g. NMR) we don't want to write the file
			if (interfaces.getNumInterfaces()>0) {
				PrintStream interfLogPS = new PrintStream(params.getOutputFile(".interfaces"));
				interfaces.printTabular(interfLogPS, params.isUsePdbResSer());
				interfLogPS.close();
			}
		} catch(IOException	e) {
			throw new EppicException(e,"Couldn't log interfaces description to file: "+e.getMessage(),false);
		}

		if (!params.isGenerateWuiSerializedFile()) {
			// we only produce the interfaces.dat file if not in -w mode (for WUI not to produce so many files)
			try {
				Goodies.serialize(params.getOutputFile(".interfaces.dat"),interfaces);
			} catch (IOException e) {
				throw new EppicException(e,"Couldn't write serialized ChainInterfaceList object to file: "+e.getMessage(),false);
			}
		}
		
	}
	
	public void doGeomScoring() throws EppicException {
		if (interfaces.getNumInterfaces()==0) {
			// no interfaces found at all, can happen e.g. in NMR structure with 1 chain, e.g. 1nmr
			LOGGER.info("No interfaces found, nothing to analyse.");
			params.getProgressLog().println("No interfaces found, nothing to analyse.");
			// we still continue so that the web interface can pick it up too
			return;
		}
		
		try {
			gps = new ArrayList<GeometryPredictor>();
			PrintStream scoreGeomPS = new PrintStream(params.getOutputFile(EppicParams.GEOMETRY_FILE_SUFFIX));
			GeometryPredictor.printScoringHeaders(scoreGeomPS);
			for (ChainInterface interf:interfaces) {
				GeometryPredictor gp = new GeometryPredictor(interf);
				gp.setUsePdbResSer(params.isUsePdbResSer());
				gps.add(gp);
				gp.setBsaToAsaCutoff(params.getCAcutoffForGeom());
				gp.setMinAsaForSurface(params.getMinAsaForSurface());
				gp.setMinCoreSizeForBio(params.getMinCoreSizeForBio());
				gp.printScores(scoreGeomPS);
				
			}
			scoreGeomPS.close();
						
			// for the webui
			modelAdaptor.setInterfaces(interfaces, this.pdb.getPdbBioUnitList());
			modelAdaptor.setGeometryScores(gps);
			modelAdaptor.setResidueDetails(interfaces);
		} catch (IOException e) {
			throw new EppicException(e, "Couldn't write interface geometry scores file. "+e.getMessage(),true);
		}
	}
	
	private void writePdbAssignments() throws EppicException{
		if(this.interfaces.size()==0) return;
		
		PrintStream ps = null;
		try {
			ps = new PrintStream(params.getOutputFile(EppicParams.PDB_BIOUNIT_ASSIGN_FILE_SUFFIX));
		} catch (FileNotFoundException e) {
			throw new EppicException(e,"Could not write the PDB bio-unit assignments file: "+e.getMessage(),true);
		}
		PdbBioUnitList bioUnitList = this.pdb.getPdbBioUnitList();
		
		//Get the full details on biounits
		TreeMap<Integer, List<Integer>> matchIds = bioUnitList.getInterfaceMatches(this.interfaces);
		
		
		//Get the summary
		TreeMap<BioUnitAssignmentType, int[]> matchSummary = bioUnitList.gatherBioUnits(this.interfaces);
		
		//Add eppic results to the matchSummary
		int[] eppicResults = new int[this.interfaces.size()];

		if(params.isDoScoreEntropies()){
			for (int i=0; i<iecList.size(); i++) {
				CombinedPredictor cp = 
						new CombinedPredictor(iecList.get(i), gps.get(i), iecList.get(i).getEvolCoreRimPredictor(), iecList.get(i).getEvolCoreSurfacePredictor());
				cp.setUsePdbResSer(params.isUsePdbResSer());
				int callIdx = cp.getCall().getIndex();
				if(callIdx == 0) eppicResults[i] = 0;
				else if(callIdx == 1) eppicResults[i] = 1;
				else eppicResults[i] = -1;
			}
		} else{
			for (int i=0; i<this.interfaces.size(); i++) {
				GeometryPredictor gp = new GeometryPredictor(this.interfaces.get(i+1));
				gp.setUsePdbResSer(params.isUsePdbResSer());
				gp.setBsaToAsaCutoff(params.getCAcutoffForGeom());
				gp.setMinAsaForSurface(params.getMinAsaForSurface());
				gp.setMinCoreSizeForBio(params.getMinCoreSizeForBio());
				int callIdx = gp.getCall().getIndex();
				if(callIdx == 0) eppicResults[i] = 0;
				else if(callIdx == 1) eppicResults[i] = 1;
				else eppicResults[i] = -1;
			}
		}
	
		matchSummary.put(BioUnitAssignmentType.eppic, eppicResults);
		
		//Print Headers--------
		ps.printf("#%15s\t%36s\t\t\t%12s\n","Interfaces","Gathered BioUnits","Details");
		
		ps.printf("%15s\t","");
				
		for(BioUnitAssignmentType type:matchSummary.keySet()){
			ps.printf("%12s\t",type.getType()+"(Final)");
		}
		
		//ps.printf("%1s", "|");
		
		for(int iUnit:matchIds.keySet()) 
			ps.printf("%12s\t",bioUnitList.get(iUnit).getType().getType()+"("+bioUnitList.get(iUnit).getSize()+")");
		
		ps.print("\n");
		
		//Print rest------
		for (int i=1; i<=this.interfaces.getNumInterfaces(); i++) {
			ChainInterface interf = this.interfaces.get(i);
			ps.printf("%15s\t", interf.getId()+"("+interf.getFirstMolecule().getPdbChainCode()+"+"+interf.getSecondMolecule().getPdbChainCode()+")");
			
			for(BioUnitAssignmentType type:matchSummary.keySet()){
				if(matchSummary.get(type)[i-1] == 0) ps.printf("%12s\t","bio");
				else if(matchSummary.get(type)[i-1] == 1) ps.printf("%12s\t","xtal");
				else ps.printf("%12s\t","nopred");
			}
			//ps.printf("%1s", "|");
			
			for(int iUnit:matchIds.keySet()){
				if(matchIds.get(iUnit).contains(i)) ps.printf("%12s\t","bio");
				else ps.printf("%12s\t","xtal");
			}
			ps.print("\n");
		}
		ps.close();
	}
	
	public void doWritePdbFiles() throws EppicException {

		if (interfaces.getNumInterfaces()==0) return;
		
		try {
			if (!params.isDoScoreEntropies()) {
				// no evol scoring: plain PDB files without altered bfactors
				for (ChainInterface interf:interfaces) {
					if (interf.isFirstProtein() && interf.isSecondProtein()) {
						interf.writeToPdbFile(params.getOutputFile("."+interf.getId()+".pdb.gz"), params.isUsePdbResSer(), true);
					}

				}
			} else {
				// writing PDB files with entropies as bfactors
				for (InterfaceEvolContext iec:iecList) {
					iec.writePdbFile(params.getOutputFile("."+iec.getInterface().getId()+".pdb.gz"),ScoringType.CORERIM, params.isUsePdbResSer());			
				}
			}
			
		} catch (IOException e) {
			throw new EppicException(e, "Couldn't write interfaces PDB files. "+e.getMessage(), true);
		}


	}
	
	public void doWritePymolFiles() throws EppicException {
		
		if (!params.isGenerateThumbnails()) return;
		
		if (interfaces.getNumInterfaces()==0) return;
		
		PymolRunner pr = null;
		params.getProgressLog().println("Writing PyMOL files");
		writeStep("Generating Thumbnails and PyMOL Files");
		LOGGER.info("Generating PyMOL files");
		try {
			pr = new PymolRunner(params.getPymolExe());
			pr.readColorsFromPropertiesFile(EppicParams.COLORS_PROPERTIES_IS);
			pr.readColorMappingsFromResourceFile(EppicParams.PYMOL_COLOR_MAPPINGS_IS);

		} catch (IOException e) {
			throw new EppicException(e,"Couldn't read colors file. Won't generate thumbnails or pse/pml files. "+e.getMessage(),true);
		}		

		try {
			for (ChainInterface interf:interfaces) {
				pr.generateInterfPngPsePml(interf, 
						params.getCAcutoffForGeom(), params.getMinAsaForSurface(), 
						params.getOutputFile("."+interf.getId()+".pdb.gz"), 
						params.getOutputFile("."+interf.getId()+".pse"),
						params.getOutputFile("."+interf.getId()+".pml"),
						params.getBaseName()+"."+interf.getId(), 
						params.isUsePdbResSer());
				LOGGER.info("Generated PyMOL files for interface "+interf.getId());
				MolViewersAdaptor.writeJmolScriptFile(interf, params.getCAcutoffForGeom(), params.getMinAsaForSurface(), pr, 
						params.getOutDir(), params.getBaseName(), params.isUsePdbResSer());
			}

			if (params.isDoScoreEntropies()) {
				for (ChainEvolContext cec:cecs.getAllChainEvolContext()) {
					PdbChain chain = pdb.getChain(cec.getRepresentativeChainCode());
					cec.setConservationScoresAsBfactors(chain);
					File chainPdbFile = params.getOutputFile("."+cec.getRepresentativeChainCode()+EppicParams.ENTROPIES_FILE_SUFFIX+".pdb");
					File chainPseFile = params.getOutputFile("."+cec.getRepresentativeChainCode()+EppicParams.ENTROPIES_FILE_SUFFIX+".pse");
					File chainPmlFile = params.getOutputFile("."+cec.getRepresentativeChainCode()+EppicParams.ENTROPIES_FILE_SUFFIX+".pml");
					File chainIconPngFile = params.getOutputFile("."+cec.getRepresentativeChainCode()+EppicParams.ENTROPIES_FILE_SUFFIX+".png");
					chain.writeToPDBFileWithPdbChainCodes(chainPdbFile, params.isUsePdbResSer());
					pr.generateChainPse(chain, interfaces, 
							params.getCAcutoffForGeom(), params.getCAcutoffForZscore(), params.getMinAsaForSurface(),
							chainPdbFile, 
							chainPseFile, 
							chainPmlFile,
							chainIconPngFile,
							EppicParams.COLOR_ENTROPIES_ICON_WIDTH,
							EppicParams.COLOR_ENTROPIES_ICON_HEIGHT,
							0,params.getMaxEntropy(),
							params.isUsePdbResSer());
				}
			}
			
		} catch (IOException e) {
			throw new EppicException(e, "Couldn't write thumbnails, PyMOL pse/pml files or jmol files. "+e.getMessage(),true);
		} catch (InterruptedException e) {
			throw new EppicException(e, "Couldn't generate thumbnails, PyMOL pse/pml files or jmol files, pymol thread interrupted: "+e.getMessage(),true);
		}

	}

	public void doCompressFiles() throws EppicException {
		
		if (interfaces.getNumInterfaces()==0) return;
		
		if (!params.isGenerateThumbnails()) return;
		// from here only if in -l mode: compress pse, chain pses and pdbs, create zip
		
		params.getProgressLog().println("Compressing files");
		LOGGER.info("Compressing files");
		
		try {
			for (ChainInterface interf:interfaces) {
				File pseFile = params.getOutputFile("."+interf.getId()+".pse");
				File gzipPseFile = params.getOutputFile("."+interf.getId()+".pse.gz");

				// pse (only if in -l mode)
				Goodies.gzipFile(pseFile, gzipPseFile);
				pseFile.delete();				
			}
			
			if (params.isDoScoreEntropies()) {
				for (ChainEvolContext cec:cecs.getAllChainEvolContext()) {
					File pseFile = 
							params.getOutputFile("."+cec.getRepresentativeChainCode()+EppicParams.ENTROPIES_FILE_SUFFIX+".pse");
					File gzipPseFile = 
							params.getOutputFile("."+cec.getRepresentativeChainCode()+EppicParams.ENTROPIES_FILE_SUFFIX+".pse.gz");
					File pdbFile = 
							params.getOutputFile("."+cec.getRepresentativeChainCode()+EppicParams.ENTROPIES_FILE_SUFFIX+".pdb");
					File gzipPdbFile = 
							params.getOutputFile("."+cec.getRepresentativeChainCode()+EppicParams.ENTROPIES_FILE_SUFFIX+".pdb.gz");
					// pse
					Goodies.gzipFile(pseFile, gzipPseFile);
					pseFile.delete();
					// pdb
					Goodies.gzipFile(pdbFile, gzipPdbFile);
					pdbFile.delete();

				}
			}
		} catch (IOException e) {
			throw new EppicException(e, "PSE or PDB files could not be gzipped. "+e.getMessage(),true);
		}
		
		try {
			LOGGER.info("Generating zip results file");
			File zipFile = params.getOutputFile(".zip");
			File[] directoryContent = params.getOutDir().listFiles(new RegexFileFilter("^"+params.getBaseName()+"\\..*")); 

			if (directoryContent==null) 
				throw new EppicException(null, "Couldn't list files in dir "+params.getOutDir()+" for creating final .zip file", true);
			
			byte[] buffer = new byte[1024];
			ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));

			for (int i=0; i<directoryContent.length; i++) {
				File file = new File(params.getOutDir(), directoryContent[i].getName());
				
				// we exclude the pse, pngs, jmols, dats and zip file (if there was one from an old run it would go into a self-reference and grow forever)
				if (file.getName().endsWith(".pse.gz") || 
						file.getName().endsWith(".png") || 
						file.getName().endsWith(".jmol") || 
						file.getName().endsWith(".dat") || // this includes chainevolcontext.dat and interfaces.dat
						file.getName().endsWith(".zip")) 
					continue;
				
				if (!params.isInputAFile()) { // i.e. PDB code from local cif repo
					// we don't want to store the cif file in zip
					if (file.getName().endsWith(".cif")) continue;
				}

				if(file.isFile())	{
					FileInputStream in = new FileInputStream(file);
					zipOutputStream.putNextEntry(new ZipEntry(directoryContent[i].getName()));

					int length;
					while ((length = in.read(buffer)) > 0) {						
						zipOutputStream.write(buffer, 0, length);
					}
					zipOutputStream.closeEntry();
					in.close();
				}
				
				// finally we mark for removal files that we've zipped and that we don't need explicitely for the WUI
				// in order to save space and increase transfer times between servers for PDB-wide precomputation
				// Files needed by WUI explicitly are: 
				// png, jmol, <interface_id>.pdb.gz, <interface_id>.pse.gz, entropies.pse.gz, aln and of course zip
				// also the main log file is needed to keep writing logs to it after compression and to check for errors in precomputing
				if ( file.getName().endsWith(".pml") ||
					 file.getName().endsWith(".interfaces") ||
					 file.getName().endsWith(EppicParams.GEOMETRY_FILE_SUFFIX) ||
					 file.getName().endsWith(EppicParams.CRSCORES_FILE_SUFFIX) ||
					 file.getName().endsWith(EppicParams.CSSCORES_FILE_SUFFIX) ||
					 file.getName().endsWith(EppicParams.COMBINED_FILE_SUFFIX) ||
					 file.getName().endsWith(EppicParams.ENTROPIES_FILE_SUFFIX) ||
					 file.getName().endsWith(EppicParams.PDB_BIOUNIT_ASSIGN_FILE_SUFFIX) ||
					 file.getName().endsWith(".fa") ||
					 file.getName().endsWith(EppicParams.ENTROPIES_FILE_SUFFIX+".pdb.gz") ||
					 ( file.getName().endsWith(".log") && 
					   !file.getName().substring(0, file.getName().lastIndexOf('.')).equals(params.getBaseName()) ) // i.e. .A.log but not main .log 
						) {
					
					file.deleteOnExit();
				}
			}
 
			zipOutputStream.close();
			

		} catch (IOException e) {
			throw new EppicException(e, "Final .zip file couldn't be created. "+e.getMessage(),true);
		}
	}
	
	private void writeFinishedFile() throws EppicException {
		try {
			FileWriter fw = new FileWriter(new File(params.getOutDir(), "finished"));
			fw.close();
		} catch (IOException e) {
			throw new EppicException(e, "Couldn't write the finished file", true);
		}
	}
	
	private void findUniqueChains() {
		String msg = "Unique sequences: ";
		
		for (ChainCluster chainCluster:pdb.getProtChainClusters()) {
			msg += chainCluster.getClusterString()+" ";
		}
		
		LOGGER.info(msg);
	}
	
	public void doLoadEvolContextFromFile() throws EppicException {
		if (interfaces.getNumInterfaces()==0) return;
		
		findUniqueChains();
		
		try {
			params.getProgressLog().println("Loading chain evolutionary scores from file "+params.getChainEvContextSerFile());
			LOGGER.info("Loading chain evolutionary scores from file "+params.getChainEvContextSerFile());
			cecs = (ChainEvolContextList)Goodies.readFromFile(params.getChainEvContextSerFile());
		} catch (ClassNotFoundException e) {
			throw new EppicException(e,"Couldn't load interface evolutionary context binary file: "+e.getMessage(),true);
		} catch(IOException e) {
			throw new EppicException(e,"Couldn't load interface evolutionary context binary file: "+e.getMessage(),true);
		}

		// TODO check whether this looks compatible with the interfaces that we have
	}
	
	public void doFindEvolContext() throws EppicException {
		if (interfaces.getNumInterfaces()==0) return;
		
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
		
		cecs.writeSeqInfoToFiles(params);

		// d) computing entropies
		cecs.computeEntropies(params);
		cecs.writeEntropiesToFile(params, pdb);
		
		if (!params.isGenerateWuiSerializedFile()) {
			// we only produce the chainevolcontext.dat file if not in -w mode (for WUI not to produce so many files)
			try {
				Goodies.serialize(params.getOutputFile(".chainevolcontext.dat"),cecs);
			} catch (IOException e) {
				throw new EppicException(e,"Couldn't write serialized ChainEvolContextList object to file: "+e.getMessage(),false);
			}
		}
		
	}
	
	public void doEvolScoring() throws EppicException {
		if (interfaces.getNumInterfaces()==0) return;

		iecList = new InterfaceEvolContextList(interfaces, cecs);
		iecList.setUsePdbResSer(params.isUsePdbResSer());

		writeStep("Scoring Interfaces");
		
		if (params.isDoScoreEntropies()) {
			
			iecList.setCoreRimPredBsaToAsaCutoff(params.getCAcutoffForRimCore(), params.getMinAsaForSurface()); // calls calcRimAndCores as well
			iecList.setCoreRimScoreCutoff(params.getCoreRimScoreCutoff());
			iecList.setCoreSurfScoreCutoff(params.getCoreSurfScoreCutoff());
			iecList.scoreCoreRim();

			// z-scores
			iecList.setCoreSurfacePredBsaToAsaCutoff(params.getCAcutoffForZscore(), params.getMinAsaForSurface()); // calls calcRimAndCores as well
			iecList.scoreCoreSurface();

			// note this adds also the entropies to the residue details
			modelAdaptor.setEvolScores(iecList);
				
		}

		
	}
	
	public void doCombinedScoring() throws EppicException {
		if (interfaces.getNumInterfaces()==0) return;
		
		try {
		

			List<CombinedPredictor> cps = new ArrayList<CombinedPredictor>();

			PrintStream scoreCombPS = new PrintStream(params.getOutputFile(EppicParams.COMBINED_FILE_SUFFIX));
			CombinedPredictor.printScoringHeaders(scoreCombPS);
			for (int i=0;i<iecList.size();i++) {
				CombinedPredictor cp = 
						new CombinedPredictor(iecList.get(i), gps.get(i), iecList.get(i).getEvolCoreRimPredictor(), iecList.get(i).getEvolCoreSurfacePredictor());
				cp.setUsePdbResSer(params.isUsePdbResSer());
				cps.add(cp);
				cp.printScoresLine(scoreCombPS);
			}
			scoreCombPS.close();

			modelAdaptor.setCombinedPredictors(cps);

		} catch (IOException e) {
			throw new EppicException(e,"Couldn't write final combined scores file. "+e.getMessage(),true);
		}
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
	 * The main of EPPIC (formerly known as CRK) 
	 */
	public static void main(String[] args){
		
		long start = System.nanoTime();
		
		Main crkMain = new Main();

		// we first parse command line and print errors to stderr (logging is not set up yet)
		try {
			crkMain.params.parseCommandLine(args);
		} catch (EppicException e) {
			System.err.println(e.getMessage());
			e.exitIfFatal(1);
		}
		try {

			// turn off jaligner logging (we only use NeedlemanWunschGotoh from that package)
			// (for some reason this doesn't work if condensated into one line, it seems that one needs to instantiate the logger and then call setLevel)
			// (and even weirder, for some reason it doesn't work if you put the code in its own separate method!)
			// NOTE: as of owl revision 1647 the embedded jaligner jar contains a modified NeedlemanWunschGotoh class
			//       that doesn't have logging at all (we fixed a bug in the code and took the opportunity to remove
			//       the logging). The official jaligner has still logging (and the bug). If we go back to using the official 
			//       jaligner we need to put this logging-turning-off code back.
			//java.util.logging.Logger jalLogger = java.util.logging.Logger.getLogger("NeedlemanWunschGotoh");
			//jalLogger.setLevel(java.util.logging.Level.OFF);
			
			crkMain.setUpLogging();

			LOGGER.info(EppicParams.PROGRAM_NAME+" version "+EppicParams.PROGRAM_VERSION);
			
			crkMain.loadConfigFile();
			
			try {
				LOGGER.info("Running in host "+InetAddress.getLocalHost().getHostName());
			} catch (UnknownHostException e) {
				LOGGER.warn("Could not determine host where we are running.");
			}
			
			// 0 load pdb
			crkMain.doLoadPdb();

			// 1 finding interfaces
			if (crkMain.params.getInterfSerFile()!=null) {
				crkMain.doLoadInterfacesFromFile();
			} else {
				crkMain.doFindInterfaces();
			}
			crkMain.doGeomScoring(); 
			
			if (crkMain.params.isDoScoreEntropies()) {
				// 2 finding evolutionary context
				if (crkMain.params.getChainEvContextSerFile()!=null) {
					crkMain.doLoadEvolContextFromFile();
				} else {
					crkMain.doFindEvolContext();
				}

				// 3 scoring
				crkMain.doEvolScoring();
				
				// 4 combined scoring
				crkMain.doCombinedScoring();
			}
			
			//4.5 Write the pdb BioUnits file
			crkMain.writePdbAssignments();
			
			crkMain.doWritePdbFiles();
						
			// 5 writing pymol files
			crkMain.doWritePymolFiles();
			
			// 6 compressing files
			crkMain.doCompressFiles();

			if (crkMain.params.isGenerateWuiSerializedFile()) {
				// 7 writing out the serialized file for web ui
				crkMain.modelAdaptor.setInterfaceWarnings(); // first we call this method to add all the cached warnings
				crkMain.modelAdaptor.writeSerializedModelFile(crkMain.params.getOutputFile(".webui.dat"));

				// finally we write a signal file for the wui to know that job is finished
				crkMain.writeFinishedFile();
			}


			long end = System.nanoTime();
			LOGGER.info("Finished successfully (total runtime "+((end-start)/1000000000L)+"s)");

		} catch (EppicException e) {
			e.log(LOGGER);
			e.exitIfFatal(1);
		} 
		catch (Exception e) {
			//e.printStackTrace();

			String stack = "";
			for (StackTraceElement el:e.getStackTrace()) {
				stack+="\tat "+el.toString()+"\n";				
			}
			LOGGER.fatal("Unexpected error. Stack trace:\n"+e+"\n"+stack+
					"\nPlease report a bug to "+EppicParams.CONTACT_EMAIL);
			System.exit(1);
		}
		
		
	}
	

}
