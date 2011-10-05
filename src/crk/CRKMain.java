package crk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.xml.sax.SAXException;

import crk.predictors.GeometryPredictor;

import owl.core.connections.pisa.PisaConnection;
import owl.core.runners.PymolRunner;
import owl.core.runners.TcoffeeException;
import owl.core.runners.blast.BlastException;
import owl.core.sequence.UniprotVerMisMatchException;
import owl.core.structure.ChainInterface;
import owl.core.structure.ChainInterfaceList;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbLoadException;
import owl.core.structure.SpaceGroup;
import owl.core.structure.graphs.AICGraph;
import owl.core.util.FileFormatException;
import owl.core.util.Goodies;

public class CRKMain {
	
	
	// THE ROOT LOGGER (log4j)
	private static final Logger ROOTLOGGER = Logger.getRootLogger();
	private static final Log LOGGER = LogFactory.getLog(CRKMain.class);
	
	// fields
	private CRKParams params;
	
	private PdbAsymUnit pdb;
	private ChainInterfaceList interfaces;
	private ChainEvolContextList cecs;
	private InterfaceEvolContextList iecList;
	
	private WebUIDataAdaptor wuiAdaptor;
		
	public CRKMain() {
		this.params = new CRKParams();
		
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
				FileAppender fileErrorAppender = new FileAppender(new PatternLayout("%5p - %m%n"),params.getProgressLogFile().getAbsolutePath(),true);
				fileErrorAppender.setThreshold(Level.ERROR);
				ROOTLOGGER.addAppender(fileErrorAppender);
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
			if (userConfigFile.exists()) {
				LOGGER.info("Loading user configuration file " + userConfigFile);
				params.readConfigFile(userConfigFile);
			}
		} catch (IOException e) {
			LOGGER.fatal("Error while reading from config file " + userConfigFile + ": " + e.getMessage());
			System.exit(1);
		}

	}

	public void doLoadPdb() throws CRKException {
		params.getProgressLog().println("Loading PDB data: "+(params.getInFile()==null?params.getPdbCode():params.getInFile().getName()));
		pdb = null;
		File cifFile = null; // if given a pdb code in command line we will store the cif file here
		try {
			if (!params.isInputAFile()) {
				cifFile = new File(params.getOutDir(),params.getPdbCode() + ".cif");
				try {
					PdbAsymUnit.grabCifFile(params.getLocalCifDir(), params.getPdbFtpCifUrl(), params.getPdbCode(), cifFile, params.isUseOnlinePdb());
				} catch(IOException e) {
					throw new CRKException(e,"Couldn't get cif file for code "+params.getPdbCode()+" from ftp or couldn't uncompress it. Error: "+e.getMessage(),true);
				}
					
			} else {
				cifFile = params.getInFile();
			}
			pdb = new PdbAsymUnit(cifFile);
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
		wuiAdaptor.setTitle(pdb.getTitle());
		wuiAdaptor.setSpaceGroup(pdb.getSpaceGroup());
		
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
			PisaConnection pc = new PisaConnection(params.getPisaInterfacesUrl(), null, null);
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
			try {
				if (params.isUseNaccess()) {
					interfaces = pdb.getAllInterfaces(CRKParams.INTERFACE_DIST_CUTOFF, params.getNaccessExe(), 0, 0, true, false, false);
					LOGGER.info("Interfaces calculated with NACCESS.");
				} else {
					interfaces = pdb.getAllInterfaces(CRKParams.INTERFACE_DIST_CUTOFF, null, params.getnSpherePointsASAcalc(), params.getNumThreads(), true, false, false);
					LOGGER.info("Interfaces calculated with "+params.getnSpherePointsASAcalc()+" sphere points.");
				}
			} catch (IOException e) {
				throw new CRKException(e,"Couldn't run NACCESS for BSA calculation. Error: "+e.getMessage(),true);
			}
		}

		params.getProgressLog().println("Done");


		// checking for clashes
		if (!params.isUsePisa() && interfaces.hasInterfacesWithClashes()) {
			String msg = "Clashes found in some of the interfaces (atoms distance below "+AICGraph.CLASH_DISTANCE+"):";
			List<ChainInterface> clashyInterfs = interfaces.getInterfacesWithClashes();
			for (ChainInterface clashyInterf:clashyInterfs) {
				msg+=("\nInterface: "+clashyInterf.getFirstMolecule().getPdbChainCode()+"+"
						+clashyInterf.getSecondMolecule().getPdbChainCode()+" ("+
						SpaceGroup.getAlgebraicFromMatrix(clashyInterf.getSecondTransf())+
						") Clashes: "+clashyInterf.getNumClashes());
			}
			msg+=("\nThis is most likely an error in the structure. If you think the structure is correct, please report a bug.");
			// we used to throw a fatal exception and exit here, but we decided to simply warn and go ahead 
			LOGGER.warn(msg);
			System.err.println(msg);
			//throw new CRKException(null, msg, true);
		}

		interfaces.calcRimAndCores(params.getCAcutoffForGeom());
		
		try {
			PrintStream interfLogPS = new PrintStream(params.getOutputFile(".interfaces"));
			interfaces.printTabular(interfLogPS, params.getJobName());
			interfLogPS.close();
		} catch(IOException	e) {
			throw new CRKException(e,"Couldn't log interfaces description to file: "+e.getMessage(),false);
		}

		try {
			Goodies.serialize(params.getOutputFile(".interfaces.dat"),interfaces);
		} catch (IOException e) {
			throw new CRKException(e,"Couldn't write serialized ChainInterfaceList object to file: "+e.getMessage(),false);
		}
		
	}
	
	public void doGeomScoring() throws CRKException {

		try {
			List<GeometryPredictor> gps = new ArrayList<GeometryPredictor>();
			PrintStream scoreGeomPS = new PrintStream(params.getOutputFile(CRKParams.GEOMETRY_FILE_SUFFIX+".scores"));
			GeometryPredictor.printScoringHeaders(scoreGeomPS);
			for (ChainInterface interf:interfaces) {
				GeometryPredictor gp = new GeometryPredictor(interf);
				gps.add(gp);
				gp.setBsaToAsaCutoff(params.getCAcutoffForGeom());
				gp.setMinCoreSizeForBio(params.getMinCoreSizeForBio());
				gp.printScores(scoreGeomPS);
				gp.writePdbFile(params.getOutputFile("."+interf.getId()+".rimcore.pdb"));
			}
			scoreGeomPS.close();
			// for the webui
			wuiAdaptor.setInterfaces(interfaces);
			wuiAdaptor.setGeometryScores(gps);
		} catch (IOException e) {
			throw new CRKException(e, "Couldn't write interface geometry scores or related pdb files. "+e.getMessage(),true);
		}
	}
	
	public void doWritePymolFiles() throws CRKException {
		PymolRunner pr = null;
		if (params.isGenerateThumbnails()) {
			try {
				pr = new PymolRunner(params.getPymolExe());
				pr.readColorsFromPropertiesFile(CRKParams.COLORS_PROPERTIES_IS);
				pr.readColorMappingsFromResourceFile(CRKParams.PYMOL_COLOR_MAPPINGS_IS);
				
			} catch (IOException e) {
				LOGGER.error("Couldn't read colors file. Won't generate thumbnails or pse/pml files");
				pr = null;
			}
		}
		if (params.isGenerateThumbnails() && pr!=null) {
			try {
				for (ChainInterface interf:interfaces) {
					pr.generateInterfPngPsePml(interf, 
							params.getOutputFile("."+interf.getId()+".rimcore.pdb"), 
							params.getOutputFile("."+interf.getId()+".pse"),
							params.getOutputFile("."+interf.getId()+".pml"),
							params.getBaseName()+"."+interf.getId());
				}
			} catch (IOException e) {
				throw new CRKException(e, "Couldn't write thumbnails or pymol pse/pml files. "+e.getMessage(),true);
			} catch (InterruptedException e) {
				throw new CRKException(e, "Couldn't generate thumbnails or pse/pml files, pymol thread interrupted: "+e.getMessage(),false);
			}
			wuiAdaptor.setJmolScripts(interfaces, pr);
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
		
		findUniqueChains();
		
		try {
			params.getProgressLog().println("Loading chain evolutionary scores from file "+params.getChainEvContextSerFile());
			LOGGER.info("Loading chain evolutionary scores from file "+params.getChainEvContextSerFile());
			cecs = (ChainEvolContextList)Goodies.readFromFile(params.getChainEvContextSerFile());
		} catch (ClassNotFoundException e) {
			throw new CRKException(e,"Couldn't load interface enumeration binary file: "+e.getMessage(),true);
		} catch(IOException e) {
			throw new CRKException(e,"Couldn't load interface enumeration binary file: "+e.getMessage(),true);
		}

		// TODO check whether this looks compatible with the interfaces that we have
	}
	
	public void doFindEvolContext() throws CRKException {
		
		findUniqueChains();
		
		cecs = new ChainEvolContextList(pdb,params.getJobName());

		// a) getting the uniprot ids corresponding to the query (the pdb sequence)
		for (ChainEvolContext chainEvCont:cecs.getAllChainEvolContext()) {
			File emblQueryCacheFile = null;
			if (params.getEmblCdsCacheDir()!=null) {
				emblQueryCacheFile = new File(params.getEmblCdsCacheDir(),params.getBaseName()+"."+chainEvCont.getRepresentativeChainCode()+".query.emblcds.fa");
			}
			params.getProgressLog().println("Finding query's uniprot mapping (through SIFTS or blasting)");
			try {
				chainEvCont.retrieveQueryData(params.getSiftsFile(), emblQueryCacheFile, params.getBlastBinDir(), params.getBlastDbDir(), params.getBlastDb(), params.getNumThreads(),params.isDoScoreKaks(),params.getPdb2uniprotIdThreshold(),params.getPdb2uniprotQcovThreshold());
			} catch (BlastException e) {
				throw new CRKException(e,"Couldn't run blast to retrieve query's uniprot mapping: "+e.getMessage(),true);
			} catch (IOException e) {
				throw new CRKException(e,"Problems while retrieving query data: "+e.getMessage(),true);
			} catch (InterruptedException e) {
				throw new CRKException(e,"Thread interrupted while running blast for retrieving query data: "+e.getMessage(),true);
			}
			if (params.isDoScoreKaks() && chainEvCont.getQueryRepCDS()==null) {
				// note calling chainEvCont.canDoCRK() will also check for this condition (here we only want to log it once)
				LOGGER.error("No CDS good match for query sequence! can't do Ka/Ks analysis on it.");
			}

			// b) getting the homologs and sequence data 
			params.getProgressLog().println("Blasting for homologues...");
			File blastCacheFile = null;
			if (params.getBlastCacheDir()!=null) {
				blastCacheFile = new File(params.getBlastCacheDir(),params.getBaseName()+"."+chainEvCont.getRepresentativeChainCode()+".blast.xml"); 
			}
			try {
				chainEvCont.retrieveHomologs(params.getBlastBinDir(), params.getBlastDbDir(), params.getBlastDb(), params.getNumThreads(), params.getIdCutoff(), params.getQueryCoverageCutoff(), blastCacheFile);
				LOGGER.info("Uniprot version used: "+chainEvCont.getUniprotVer());
			} catch (UniprotVerMisMatchException e) {
				throw new CRKException(e, "Mismatch of Uniprot versions! "+e.getMessage(), true);
			} catch (BlastException e) {
				throw new CRKException(e,"Couldn't run blast to retrieve homologs: "+e.getMessage() ,true);
			} catch (IOException e) {
				throw new CRKException(e,"Problem while blasting for sequence homologs: "+e.getMessage(),true);
			} catch (InterruptedException e) {
				throw new CRKException(e,"Thread interrupted while blasting for sequence homologs: "+e.getMessage(),true);
			}

			params.getProgressLog().println("Retrieving UniprotKB data and EMBL CDS sequences");
			File emblHomsCacheFile = null;
			if (params.getEmblCdsCacheDir()!=null) {
				emblHomsCacheFile = new File(params.getEmblCdsCacheDir(),params.getBaseName()+"."+chainEvCont.getRepresentativeChainCode()+".homologs.emblcds.fa");
			}
			try {
				chainEvCont.retrieveHomologsData(emblHomsCacheFile, params.isDoScoreKaks());
			} catch (UniprotVerMisMatchException e) {
				throw new CRKException(e, "Mismatch of Uniprot versions! "+e.getMessage(), true);
			} catch (IOException e) {
				throw new CRKException(e, "Problem while fetching CDS data: "+e.getMessage(),true);
			}
			if (params.isDoScoreKaks() && !chainEvCont.isConsistentGeneticCodeType()){
				// note calling chainEvCont.canDoCRK() will also check for this condition (here we only want to log it once)
				LOGGER.error("The list of homologs does not have a single genetic code type, can't do CRK analysis on it.");
			}
			
			// remove redundancy
			chainEvCont.removeRedundancy();

			// skimming so that there's not too many sequences for selecton
			chainEvCont.skimList(params.getMaxNumSeqs());
			
			// check the back-translation of CDS to uniprot
			// check whether we have a good enough CDS for the chain
			if (params.isDoScoreKaks() && chainEvCont.canDoKaks()) {
				LOGGER.info("Number of homologs with at least one uniprot CDS mapping: "+chainEvCont.getNumHomologsWithCDS());
				LOGGER.info("Number of homologs with valid CDS: "+chainEvCont.getNumHomologsWithValidCDS());
			}

			// c) align
			params.getProgressLog().println("Aligning protein sequences with t_coffee...");
			try {
				chainEvCont.align(params.getTcoffeeBin(), params.isUseTcoffeeVeryFastMode());
			} catch (TcoffeeException e) {
				throw new CRKException(e, "Couldn't run t_coffee to align protein sequences: "+e.getMessage(), true);
			} catch (IOException e) {
				throw new CRKException(e, "Problems while running t_coffee to align protein sequences: "+e.getMessage(),true);
			} catch (InterruptedException e) {
				throw new CRKException(e, "Thread interrupted while running t_coffee to align protein sequences: "+e.getMessage(),true);
			}

			File outFile = null;
			try {
				// writing homolog sequences to file
				outFile = params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+".fa");
				chainEvCont.writeHomologSeqsToFile(outFile);

				// printing summary to file
				outFile = params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+".log");
				PrintStream log = new PrintStream(outFile);
				chainEvCont.printSummary(log);
				log.close();
				// writing the alignment to file
				outFile = params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+".aln");
				chainEvCont.writeAlignmentToFile(outFile);
				// writing the nucleotides alignment to file
				if (params.isDoScoreKaks() && chainEvCont.canDoKaks()) {
					outFile = params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+".cds.aln");
					chainEvCont.writeNucleotideAlignmentToFile(outFile);
				}
			} catch(FileNotFoundException e){
				LOGGER.error("Couldn't write file "+outFile);
				LOGGER.error(e.getMessage());
			}

			// d) computing entropies
			chainEvCont.computeEntropies(params.getReducedAlphabet());

			// e) compute ka/ks ratios
			if (params.isDoScoreKaks() && chainEvCont.canDoKaks()) {
				params.getProgressLog().println("Running selecton (this will take long)...");
				try {
				chainEvCont.computeKaKsRatiosSelecton(params.getSelectonBin(), 
						params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+".selecton.res"),
						params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+".selecton.log"), 
						params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+".selecton.tree"),
						params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+".selecton.global"),
						params.getSelectonEpsilon());
				} catch (IOException e) {
					throw new CRKException(e, "Problems while running selecton: "+e.getMessage(), true);
				} catch (InterruptedException e) {
					throw new CRKException(e, "Thread interrupted while running selecton: "+e.getMessage(), true);
				}
			}

			try {
				// writing the conservation scores (entropies/kaks) log file
				outFile = params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+CRKParams.ENTROPIES_FILE_SUFFIX);
				PrintStream conservScoLog = new PrintStream(outFile);
				chainEvCont.printConservationScores(conservScoLog, ScoringType.ENTROPY);
				conservScoLog.close();
				if (params.isDoScoreKaks() && chainEvCont.canDoKaks()) {
					outFile = params.getOutputFile("."+chainEvCont.getRepresentativeChainCode()+CRKParams.KAKS_FILE_SUFFIX);
					conservScoLog = new PrintStream(outFile);
					chainEvCont.printConservationScores(conservScoLog, ScoringType.KAKS);
					conservScoLog.close();				
				}
			} catch (FileNotFoundException e) {
				LOGGER.error("Could not write the scores log file "+outFile);
				LOGGER.error(e.getMessage());
			}
		}
		
		try {
			Goodies.serialize(params.getOutputFile(".chainevolcontext.dat"),cecs);
		} catch (IOException e) {
			throw new CRKException(e,"Couldn't write serialized ChainEvolContextList object to file: "+e.getMessage(),false);
		}
		
	}
	
	public void doEvolScoring() throws CRKException {

		interfaces.calcRimAndCores(params.getCAcutoffForRimCore());
		
		iecList = new InterfaceEvolContextList(params.getJobName(), params.getMinHomologsCutoff(),  
				params.getIdCutoff(), params.getQueryCoverageCutoff(), params.getMaxNumSeqs());
		iecList.addAll(interfaces,cecs);

		if (params.isDoScoreEntropies()) {
			try {
				interfaces.calcRimAndCores(params.getCAcutoffForRimCore());
				iecList.setBioCutoff(params.getEntrCallCutoff()-params.getGrayZoneWidth());
				iecList.setXtalCutoff(params.getEntrCallCutoff()+params.getGrayZoneWidth());
				iecList.setZscoreCutoff(params.getZscoreCutoff());
				PrintStream scoreEntrPS = new PrintStream(params.getOutputFile(CRKParams.ENTROPIES_FILE_SUFFIX+".scores"));
				// entropy nw
				iecList.scoreEntropy(false);
				iecList.printScoresTable(scoreEntrPS);
				wuiAdaptor.add(iecList);
				iecList.resetCalls();
				// entropy w
				iecList.scoreEntropy(true);
				iecList.printScoresTable(scoreEntrPS);
				iecList.writeScoresPDBFiles(params,CRKParams.ENTROPIES_FILE_SUFFIX+".pdb");
				wuiAdaptor.add(iecList);
				scoreEntrPS.close();
				iecList.resetCalls();
				// z-scores
				interfaces.calcRimAndCores(params.getCAcutoffForZscore());
				PrintStream scoreZscorePS = new PrintStream(params.getOutputFile(CRKParams.ZSCORES_FILE_SUFFIX+".scores"));
				iecList.scoreZscore();
				iecList.printZscoresTable(scoreZscorePS);
				iecList.resetCalls();
				scoreZscorePS.close();

			} catch (IOException e) {
				throw new CRKException(e, "Couldn't write final interface entropy scores or related PDB files. "+e.getMessage(),true);
			} 
		}
		if (params.isDoScoreKaks()) {
			try {
				interfaces.calcRimAndCores(params.getCAcutoffForRimCore());
				iecList.setBioCutoff(params.getKaksCallCutoff()-params.getGrayZoneWidth());
				iecList.setXtalCutoff(params.getKaksCallCutoff()+params.getGrayZoneWidth());
				// ka/ks scoring			
				PrintStream scoreKaksPS = new PrintStream(params.getOutputFile(CRKParams.KAKS_FILE_SUFFIX+".scores"));
				// kaks nw
				iecList.scoreKaKs(false);
				iecList.printScoresTable(scoreKaksPS);
				wuiAdaptor.add(iecList);
				iecList.resetCalls();
				// kaks w
				iecList.scoreKaKs(true);
				iecList.printScoresTable(scoreKaksPS);
				iecList.writeScoresPDBFiles(params, CRKParams.KAKS_FILE_SUFFIX+".pdb");
				wuiAdaptor.add(iecList);
				scoreKaksPS.close();
				iecList.resetCalls();

			} catch (IOException e) {
				throw new CRKException(e,"Couldn't write final interface Ka/Ks scores or related PDB files. "+e.getMessage(),true);
			}
		}

		params.getProgressLog().println("Done scoring");
	}
	
	/**
	 * The main of CRK 
	 */
	public static void main(String[] args) {
		
		CRKMain crkMain = new CRKMain();

		try {
			crkMain.params.parseCommandLine(args);
		
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
			}
			
			crkMain.doWritePymolFiles();
			
			// writing out the serialized file for web ui
			if (crkMain.params.isGenerateThumbnails()) {
				crkMain.wuiAdaptor.writePdbScoreItemFile(crkMain.params.getOutputFile(".webui.dat"));
				crkMain.wuiAdaptor.writeResidueDetailsFiles(crkMain.params.isDoScoreEntropies(),crkMain.params.isDoScoreKaks(),"resDetails.dat");
			}


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
