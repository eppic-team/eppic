package crk;

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
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.xml.sax.SAXException;

import crk.predictors.CombinedPredictor;
import crk.predictors.GeometryPredictor;

import owl.core.connections.pisa.PisaConnection;
import owl.core.runners.PymolRunner;
import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.InterfacesFinder;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbChain;
import owl.core.structure.PdbLoadException;
import owl.core.structure.SpaceGroup;
import owl.core.structure.graphs.AICGraph;
import owl.core.util.FileFormatException;
import owl.core.util.Goodies;
import owl.core.util.RegexFileFilter;

public class CRKMain {
	
	
	// THE ROOT LOGGER (log4j)
	private static final Logger ROOTLOGGER = Logger.getRootLogger();
	private static final Log LOGGER = LogFactory.getLog(CRKMain.class);
	
	private static final int STEPS_TOTAL = 4;
	
	// fields
	private CRKParams params;
	
	private PdbAsymUnit pdb;
	private ChainInterfaceList interfaces;
	private ChainEvolContextList cecs;
	private InterfaceEvolContextList iecList;
	private List<GeometryPredictor> gps;
	
	private File stepsLogFile;
	private int stepCount;
	
	private WebUIDataAdaptor wuiAdaptor;
		
	public CRKMain() {
		this.params = new CRKParams();
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
				stepsLogFile = new File(params.getOutDir(),params.getBaseName()+CRKParams.STEPS_LOG_FILE_SUFFIX);
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
		File userConfigFile = new File(System.getProperty("user.home"),CRKParams.CONFIG_FILE_NAME);  
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
		} catch (CRKException e) {
			LOGGER.error(e.getMessage());
			System.exit(1);
		}

	}

	public void doLoadPdb() throws CRKException {
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
					throw new CRKException(e,"Couldn't get cif file for code "+params.getPdbCode()+" from ftp or couldn't uncompress it. Error: "+e.getMessage(),true);
				}
					
			} else {
				cifFile = params.getInFile();
			}
			// we parse PDB files with no X padding if no SEQRES is found. Otherwise matching to uniprot doesn't work in many cases
			pdb = new PdbAsymUnit(cifFile, PdbAsymUnit.DEFAULT_MODEL, false);
		} catch (FileFormatException e) {
			throw new CRKException(e,"File format error: "+e.getMessage(),true);
		} catch (PdbLoadException e) {
			throw new CRKException(e,"Couldn't load file "+cifFile+". Error: "+e.getMessage(),true);
		} catch (IOException e) {
			throw new CRKException(e,"Problems reading PDB data from "+cifFile+". Error: "+e.getMessage(),true);
		}
		
		if (pdb.getCrystalCell()==null) {
			LOGGER.warn("No crystal information found in source "+params.getPdbCode()+". Only asymmetric unit interfaces will be calculated.");
		}
		
		// we strip the H atoms: surface calculations should not have them (otherwise comparisons of structures with/without H arn't good)
		pdb.removeHatoms();
		
		// for the webui
		wuiAdaptor = new WebUIDataAdaptor();
		wuiAdaptor.setParams(params);
		wuiAdaptor.setPdbMetadata(pdb);
		
	}
	
	public void doLoadInterfacesFromFile() throws CRKException {
		try {
			params.getProgressLog().println("Loading interfaces enumeration from file "+params.getInterfSerFile());
			LOGGER.info("Loading interfaces enumeration from file "+params.getInterfSerFile());
			interfaces = (ChainInterfaceList)Goodies.readFromFile(params.getInterfSerFile());
		} catch (ClassNotFoundException e) {
			throw new CRKException(e,"Couldn't load interface enumeration binary file: "+e.getMessage(),true);
		} catch (IOException e) {
			throw new CRKException(e,"Couldn't load interface enumeration binary file: "+e.getMessage(),true);
		}
		
		if (!pdb.getPdbCode().equals(interfaces.get(1).getFirstMolecule().getPdbCode())) {
			throw new CRKException(null,"PDB codes of given PDB entry/file and given interface enumeration binary file don't match.",true);
		}
		
	}

	public void doFindInterfaces() throws CRKException {

		if (params.isUsePisa()) {
			params.getProgressLog().println("Getting PISA interfaces...");
			LOGGER.info("Interfaces from PISA.");
			PisaConnection pc = new PisaConnection();
			List<String> pdbCodes = new ArrayList<String>();
			pdbCodes.add(pdb.getPdbCode());
			try {
				interfaces = pc.getInterfacesDescription(pdbCodes).get(pdb.getPdbCode()).convertToChainInterfaceList(pdb);
			} catch(SAXException e) {
				throw new CRKException(e,"Error while reading PISA xml file"+e.getMessage(),true);
			} catch(IOException e) {
				throw new CRKException(e,"Error while retrieving PISA xml file: "+e.getMessage(),true);
			}
		} else {
			params.getProgressLog().println("Calculating possible interfaces...");
			InterfacesFinder interfFinder = new InterfacesFinder(pdb);
			interfaces = interfFinder.getAllInterfaces(CRKParams.INTERFACE_DIST_CUTOFF, params.getnSpherePointsASAcalc(), params.getNumThreads(), true, false, params.getMinSizeCofactorForAsa());
			LOGGER.info("Interfaces calculated with "+params.getnSpherePointsASAcalc()+" sphere points.");
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

		}

		LOGGER.info("Minimum ASA for a residue to be considered surface: "+String.format("%2.0f",params.getMinAsaForSurface()));
		
		params.getProgressLog().println("Done");


		// checking for clashes
		if (!params.isUsePisa() && interfaces.hasInterfacesWithClashes()) {
			String msg = "Clashes (atoms distance below "+AICGraph.CLASH_DISTANCE+") found in:";
			List<ChainInterface> clashyInterfs = interfaces.getInterfacesWithClashes();
			boolean tooManyClashes = false;
			for (ChainInterface clashyInterf:clashyInterfs) {
				int numClashes = clashyInterf.getNumClashes();
				if (numClashes>CRKParams.NUM_CLASHES_FOR_ERROR) tooManyClashes = true;
				msg+=("\nInterface: "+clashyInterf.getFirstMolecule().getPdbChainCode()+"+"
						+clashyInterf.getSecondMolecule().getPdbChainCode()+" ("+
						SpaceGroup.getAlgebraicFromMatrix(clashyInterf.getSecondTransf().getMatTransform())+
						") Clashes: "+numClashes); 
			}
			
			if (tooManyClashes) {
				throw new CRKException(null, "Too many clashes in at least one interface, most likely there is an error in this structure. "+msg , true);				
			} else { 
				LOGGER.warn(msg);
			}

		}

		interfaces.calcRimAndCores(params.getCAcutoffForGeom(), params.getMinAsaForSurface());
		
		try {
			PrintStream interfLogPS = new PrintStream(params.getOutputFile(".interfaces"));
			interfaces.printTabular(interfLogPS, params.getJobName());
			interfLogPS.close();
		} catch(IOException	e) {
			throw new CRKException(e,"Couldn't log interfaces description to file: "+e.getMessage(),false);
		}

		if (!params.isGenerateThumbnails()) {
			// we only produce the interfaces.dat file if not in -l mode (for WUI not to produce so many files)
			try {
				Goodies.serialize(params.getOutputFile(".interfaces.dat"),interfaces);
			} catch (IOException e) {
				throw new CRKException(e,"Couldn't write serialized ChainInterfaceList object to file: "+e.getMessage(),false);
			}
		}
		
	}
	
	public void doGeomScoring() throws CRKException {
		if (interfaces.getNumInterfaces()==0) {
			// no interfaces found at all, can happen e.g. in NMR structure with 1 chain, e.g. 1nmr
			LOGGER.info("No interfaces found, nothing to analyse.");
			params.getProgressLog().println("No interfaces found, nothing to analyse.");
			// we still continue so that the web interface can pick it up too
			return;
		}
		
		try {
			gps = new ArrayList<GeometryPredictor>();
			PrintStream scoreGeomPS = new PrintStream(params.getOutputFile(CRKParams.GEOMETRY_FILE_SUFFIX));
			GeometryPredictor.printScoringHeaders(scoreGeomPS);
			for (ChainInterface interf:interfaces) {
				GeometryPredictor gp = new GeometryPredictor(interf);
				gps.add(gp);
				gp.setBsaToAsaCutoff(params.getCAcutoffForGeom());
				gp.setMinAsaForSurface(params.getMinAsaForSurface());
				gp.setMinCoreSizeForBio(params.getMinCoreSizeForBio());
				gp.printScores(scoreGeomPS);
				// we write a PDB file only if no evol scoring is to be done, if evol scoring is done then 
				// the PDB files are written later with entropy values encoded as bfactors
				if (!params.isDoScoreEntropies()) {
					gp.writePdbFile(params.getOutputFile("."+interf.getId()+".pdb"));
				}
			}
			scoreGeomPS.close();
						
			// for the webui
			wuiAdaptor.setInterfaces(interfaces);
			wuiAdaptor.setGeometryScores(gps);
			wuiAdaptor.addResidueDetails(interfaces, params.getCAcutoffForRimCore(), params.getMinAsaForSurface());
		} catch (IOException e) {
			throw new CRKException(e, "Couldn't write interface geometry scores or related pdb files. "+e.getMessage(),true);
		}
	}
	
	public void doWritePymolFiles() throws CRKException {
		if (interfaces.getNumInterfaces()==0) return;
		
		PymolRunner pr = null;
		params.getProgressLog().println("Writing PyMOL files");
		writeStep("Generating Thumbnails and PyMOL Files");
		LOGGER.info("Generating PyMOL files");
		try {
			pr = new PymolRunner(params.getPymolExe());
			pr.readColorsFromPropertiesFile(CRKParams.COLORS_PROPERTIES_IS);
			pr.readColorMappingsFromResourceFile(CRKParams.PYMOL_COLOR_MAPPINGS_IS);

		} catch (IOException e) {
			throw new CRKException(e,"Couldn't read colors file. Won't generate thumbnails or pse/pml files. "+e.getMessage(),true);
		}		

		try {
			for (ChainInterface interf:interfaces) {
				pr.generateInterfPngPsePml(interf, 
						params.getCAcutoffForGeom(), params.getMinAsaForSurface(), 
						params.getOutputFile("."+interf.getId()+".pdb"), 
						params.getOutputFile("."+interf.getId()+".pse"),
						params.getOutputFile("."+interf.getId()+".pml"),
						params.getBaseName()+"."+interf.getId());
				LOGGER.info("Generated PyMOL files for interface "+interf.getId());
				wuiAdaptor.writeJmolScriptFile(interf, params.getCAcutoffForGeom(), params.getMinAsaForSurface(), pr, params.getOutDir(), params.getBaseName());
			}

			if (params.isDoScoreEntropies()) {
				for (ChainEvolContext cec:cecs.getAllChainEvolContext()) {
					PdbChain chain = pdb.getChain(cec.getRepresentativeChainCode());
					cec.setConservationScoresAsBfactors(chain);
					File chainPdbFile = params.getOutputFile("."+cec.getRepresentativeChainCode()+".pdb");
					chain.writeToPDBFile(chainPdbFile);
					pr.generateChainPse(chain, interfaces, 
							params.getCAcutoffForGeom(), params.getCAcutoffForZscore(), params.getMinAsaForSurface(),
							chainPdbFile, 
							params.getOutputFile("."+cec.getRepresentativeChainCode()+".pse"), 
							params.getOutputFile("."+cec.getRepresentativeChainCode()+".pml"),
							0,params.getMaxEntropy());
				}
			}
			
		} catch (IOException e) {
			throw new CRKException(e, "Couldn't write thumbnails, PyMOL pse/pml files or jmol files. "+e.getMessage(),true);
		} catch (InterruptedException e) {
			throw new CRKException(e, "Couldn't generate thumbnails, PyMOL pse/pml files or jmol files, pymol thread interrupted: "+e.getMessage(),true);
		}

		compressFiles();
				
		
	}

	private void compressFiles() throws CRKException {
		params.getProgressLog().println("Compressing files");
		LOGGER.info("Compressing files");
		try {
			for (ChainInterface interf:interfaces) {
				File pseFile = params.getOutputFile("."+interf.getId()+".pse");
				File gzipPseFile = params.getOutputFile("."+interf.getId()+".pse.gz");
				File pdbFile = params.getOutputFile("."+interf.getId()+".pdb");
				File gzipPdbFile = params.getOutputFile("."+interf.getId()+".pdb.gz");
				// pse
				gzipFile(pseFile, gzipPseFile);
				pseFile.delete();
				// pdb
				gzipFile(pdbFile, gzipPdbFile);
				pdbFile.delete();
				
			}
			
			if (params.isDoScoreEntropies()) {
				for (ChainEvolContext cec:cecs.getAllChainEvolContext()) {
					File pseFile = params.getOutputFile("."+cec.getRepresentativeChainCode()+".pse");
					File gzipPseFile = params.getOutputFile("."+cec.getRepresentativeChainCode()+".pse.gz");
					File pdbFile = params.getOutputFile("."+cec.getRepresentativeChainCode()+".pdb");
					File gzipPdbFile = params.getOutputFile("."+cec.getRepresentativeChainCode()+".pdb.gz");
					// pse
					gzipFile(pseFile, gzipPseFile);
					pseFile.delete();
					// pdb
					gzipFile(pdbFile, gzipPdbFile);
					pdbFile.delete();

				}
			}
		} catch (IOException e) {
			throw new CRKException(e, "PSE or PDB files could not be gzipped. "+e.getMessage(),true);
		}
		
		try {
			LOGGER.info("Generating zip results file");
			File zipFile = params.getOutputFile(".zip");
			File[] directoryContent = params.getOutDir().listFiles(new RegexFileFilter("^"+params.getBaseName()+"\\..*")); 

			if (directoryContent==null) 
				throw new CRKException(null, "Couldn't list files in dir "+params.getOutDir()+" for creating final .zip file", true);
			
			byte[] buffer = new byte[1024];
			ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));

			for (int i=0; i<directoryContent.length; i++) {
				File source = new File(params.getOutDir(), directoryContent[i].getName());
				
				// we exclude the pse, pngs, jmols, dats and zip file (if there was one from an old run it would go into a self-reference and grow forever)
				if (source.getName().endsWith(".pse.gz") || 
						source.getName().endsWith(".png") || 
						source.getName().endsWith(".jmol") || 
						source.getName().endsWith(".dat") || // this includes chainevolcontext.dat and interfaces.dat
						source.getName().endsWith(".zip")) 
					continue;

				if(source.isFile())	{
					FileInputStream in = new FileInputStream(source);
					zipOutputStream.putNextEntry(new ZipEntry(directoryContent[i].getName()));

					int length;
					while ((length = in.read(buffer)) > 0) {						
						zipOutputStream.write(buffer, 0, length);
					}
					zipOutputStream.closeEntry();
					in.close();
				}
			}
 
			zipOutputStream.close();
			

		} catch (IOException e) {
			throw new CRKException(e, "Final .zip file couldn't be created. "+e.getMessage(),true);
		}
	}
	
	private void gzipFile(File inFile, File outFile) throws IOException {
		GZIPOutputStream zos = new GZIPOutputStream(new FileOutputStream(outFile));
		FileInputStream is = new FileInputStream(inFile);

		int b;
		while ( (b=is.read())!=-1) {
			zos.write(b);
		}
		zos.close();
		is.close();
	}
	
	public void writeFinishedFile() throws CRKException {
		try {
			FileWriter fw = new FileWriter(new File(params.getOutDir(), "finished"));
			fw.close();
		} catch (IOException e) {
			throw new CRKException(e, "Couldn't write the finished file", true);
		}
	}
	
	private void findUniqueChains() {
		String msg = "Unique sequences for "+params.getJobName()+":";
		int i = 1;
		for (String representativeChain:pdb.getAllRepChains()) {
			List<String> entity = pdb.getSeqIdenticalGroup(representativeChain);
			msg+=" "+i+":";
			for (String chain:entity) {
				msg+=" "+chain;
			}
			i++;
		}
		LOGGER.info(msg);
	}
	
	public void doLoadEvolContextFromFile() throws CRKException {
		if (interfaces.getNumInterfaces()==0) return;
		
		findUniqueChains();
		
		try {
			params.getProgressLog().println("Loading chain evolutionary scores from file "+params.getChainEvContextSerFile());
			LOGGER.info("Loading chain evolutionary scores from file "+params.getChainEvContextSerFile());
			cecs = (ChainEvolContextList)Goodies.readFromFile(params.getChainEvContextSerFile());
		} catch (ClassNotFoundException e) {
			throw new CRKException(e,"Couldn't load interface evolutionary context binary file: "+e.getMessage(),true);
		} catch(IOException e) {
			throw new CRKException(e,"Couldn't load interface evolutionary context binary file: "+e.getMessage(),true);
		}

		// TODO check whether this looks compatible with the interfaces that we have
	}
	
	public void doFindEvolContext() throws CRKException {
		if (interfaces.getNumInterfaces()==0) return;
		
		findUniqueChains();
		
		try {
			cecs = new ChainEvolContextList(pdb,params);
		} catch (SQLException e) {
			throw new CRKException(e,"Could not connect to local uniprot database server: "+e.getMessage(),true);
		}
		
		// a) getting the uniprot ids corresponding to the query (the pdb sequence)
		writeStep("Finding Homologs and Calculating Entropies");		
		cecs.retrieveQueryData(params);
		wuiAdaptor.getRunParametersItem().setUniprotVer(cecs.getUniprotVer());
		
		// b) getting the homologs and sequence data and filtering it
		cecs.retrieveHomologs(params);

		// c) align
		cecs.align(params);
		
		cecs.writeSeqInfoToFiles(params);

		// d) computing entropies
		cecs.computeEntropies(params);
		cecs.writeEntropiesToFile(params, pdb);
		
		if (!params.isGenerateThumbnails()) {
			// we only produce the chainevolcontext.dat file if not in -l mode (for WUI not to produce so many files)
			try {
				Goodies.serialize(params.getOutputFile(".chainevolcontext.dat"),cecs);
			} catch (IOException e) {
				throw new CRKException(e,"Couldn't write serialized ChainEvolContextList object to file: "+e.getMessage(),false);
			}
		}
		
	}
	
	public void doEvolScoring() throws CRKException {
		if (interfaces.getNumInterfaces()==0) return;

		iecList = new InterfaceEvolContextList(params.getJobName(), interfaces, cecs);

		writeStep("Scoring Interfaces");
		
		if (params.isDoScoreEntropies()) {
			try {
				iecList.setRimCorePredBsaToAsaCutoff(params.getCAcutoffForRimCore(), params.getMinAsaForSurface()); // calls calcRimAndCores as well
				iecList.setCallCutoff(params.getEntrCallCutoff());
				iecList.setZscoreCutoff(params.getZscoreCutoff());
				PrintStream scoreEntrPS = new PrintStream(params.getOutputFile(CRKParams.CRSCORES_FILE_SUFFIX));
				// entropy nw
				iecList.scoreEntropy(false);
				iecList.printScoresTable(scoreEntrPS);
				
				// writing PDB files with entropies as bfactors (if no evol scoring performed, then these files are written at geom scoring of course without entropies)
				iecList.writeScoresPDBFiles(params,".pdb");

				scoreEntrPS.close();
				// z-scores
				PrintStream scoreZscorePS = new PrintStream(params.getOutputFile(CRKParams.CSSCORES_FILE_SUFFIX));
				iecList.setZPredBsaToAsaCutoff(params.getCAcutoffForZscore(), params.getMinAsaForSurface()); // calls calcRimAndCores as well
				iecList.scoreZscore();
				iecList.printZscoresTable(scoreZscorePS);
				scoreZscorePS.close();
				
				// note this adds also the entropies to the residue details
				wuiAdaptor.add(iecList);
				
			} catch (IOException e) {
				throw new CRKException(e, "Couldn't write final interface entropy scores or related PDB files. "+e.getMessage(),true);
			} 
		}

		
	}
	
	public void doCombinedScoring() throws CRKException {
		if (interfaces.getNumInterfaces()==0) return;
		
		try {
		
			// commented out because it was an issue to calculate twice especially for z-scores that are non-deterministic: 
			// in some cases (e.g. 1bos-20, 3ewe-5) it could happen that in first caculation a bio was called and in second a xtal was called 
			//iecList.setCallCutoff(params.getEntrCallCutoff());
			//iecList.setRimCorePredBsaToAsaCutoff(params.getCAcutoffForRimCore());
			//iecList.scoreEntropy(false);

			//iecList.setZscoreCutoff(params.getZscoreCutoff());
			//iecList.setZPredBsaToAsaCutoff(params.getCAcutoffForZscore());			
			//iecList.scoreZscore();

			List<CombinedPredictor> cps = new ArrayList<CombinedPredictor>();

			PrintStream scoreCombPS = new PrintStream(params.getOutputFile(CRKParams.COMBINED_FILE_SUFFIX));
			CombinedPredictor.printScoringHeaders(scoreCombPS);
			for (int i=0;i<iecList.size();i++) {
				CombinedPredictor cp = 
						new CombinedPredictor(iecList.get(i), gps.get(i), iecList.getEvolRimCorePredictor(i), iecList.getEvolInterfZPredictor(i));
				cps.add(cp);
				cp.printScoresLine(scoreCombPS);
			}
			scoreCombPS.close();

			wuiAdaptor.setCombinedPredictors(cps);

		} catch (IOException e) {
			throw new CRKException(e,"Couldn't write final combined scores file. "+e.getMessage(),true);
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
		} catch(FileNotFoundException e) {
			LOGGER.error("Couldn't write to steps log file "+stepsLogFile);
		}
	}
	
	/**
	 * The main of EPPIC (formerly known as CRK) 
	 */
	public static void main(String[] args){
		
		long start = System.nanoTime();
		
		CRKMain crkMain = new CRKMain();

		// we first parse command line and print errors to stderr (logging is not set up yet)
		try {
			crkMain.params.parseCommandLine(args);
		} catch (CRKException e) {
			System.err.println(e.getMessage());
			e.exitIfFatal(1);
		}
		try {
			// turn off jaligner logging (we only use NeedlemanWunschGotoh from that package)
			// (for some reason this doesn't work if condensated into one line, it seems that one needs to instantiate the logger and then call setLevel)
			// (and even weirder, for some reason it doesn't work if you put the code in its own separate method!)
			java.util.logging.Logger jalLogger = java.util.logging.Logger.getLogger("NeedlemanWunschGotoh");
			jalLogger.setLevel(java.util.logging.Level.OFF);

			crkMain.setUpLogging();

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
			
			
			if (crkMain.params.isGenerateThumbnails()) {
				// 5 writing pymol files
				crkMain.doWritePymolFiles();
				
				// 6 writing out the serialized file for web ui
				crkMain.wuiAdaptor.addInterfaceWarnings(); // first we call this method to add all the cached warnings
				crkMain.wuiAdaptor.writePdbScoreItemFile(crkMain.params.getOutputFile(".webui.dat"));
				
				// finally we write a signal file for the wui to know that job is finished
				crkMain.writeFinishedFile();

			}

			long end = System.nanoTime();
			LOGGER.info("Finished successfully (total runtime "+((end-start)/1000000000L)+"s)");

		} catch (CRKException e) {
			e.log(LOGGER);
			e.exitIfFatal(1);
		} 
//		catch (Exception e) {
//			e.printStackTrace();
//
//			String stack = "";
//			for (StackTraceElement el:e.getStackTrace()) {
//				stack+="\tat "+el.toString()+"\n";				
//			}
//			LOGGER.fatal("Unexpected error. Exiting.\n"+e+"\n"+stack);
//			System.exit(1);
//		}
		
		
	}
	

}
