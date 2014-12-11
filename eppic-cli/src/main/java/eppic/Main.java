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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Compound;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.contact.StructureInterface;
import org.biojava.bio.structure.contact.StructureInterfaceCluster;
import org.biojava.bio.structure.contact.StructureInterfaceList;
import org.biojava.bio.structure.io.FileParsingParameters;
import org.biojava.bio.structure.io.PDBFileParser;
import org.biojava.bio.structure.io.mmcif.MMcifParser;
import org.biojava.bio.structure.io.mmcif.SimpleMMcifConsumer;
import org.biojava.bio.structure.io.mmcif.SimpleMMcifParser;
import org.biojava.bio.structure.xtal.CrystalBuilder;
import org.biojava.bio.structure.xtal.SpaceGroup;
import org.biojava3.structure.StructureIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.commons.util.FileTypeGuesser;
import eppic.commons.util.Goodies;
import eppic.predictors.CombinedClusterPredictor;
import eppic.predictors.CombinedPredictor;
import eppic.predictors.GeometryClusterPredictor;
import eppic.predictors.GeometryPredictor;

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
	
	private File stepsLogFile;
	private int stepCount;
	
	private DataModelAdaptor modelAdaptor;
		
	public Main() {
		this.params = new EppicParams();
		this.stepCount = 1;
	}
	

		
	public void setUpLogging() {
		
		// the log4j2 log file configuration at runtime, note that elsewhere we use the slf4j interface only
		// see http://stackoverflow.com/questions/14862770/log4j2-assigning-file-appender-filename-at-runtime
		System.setProperty("logFilename", new File(params.getOutDir(),params.getBaseName()+".log").toString());
		
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		ctx.reconfigure();


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
		
		params.getProgressLog().println("Loading PDB data: "+(params.getInFile()==null?params.getPdbCode():params.getInFile().getName()));
		writeStep("Calculating Interfaces");
		pdb = null;
		try {
			if (!params.isInputAFile()) {
				
				AtomCache cache = new AtomCache();
								
				cache.setUseMmCif(true);
				FileParsingParameters fileParsingParams = new FileParsingParameters();
				fileParsingParams.setAlignSeqRes(true);
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

					parser.parse(new BufferedReader(new InputStreamReader(new FileInputStream(params.getInFile())))); 

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
	
	public void doLoadInterfacesFromFile() throws EppicException {
		try {
			params.getProgressLog().println("Loading interfaces enumeration from file "+params.getInterfSerFile());
			LOGGER.info("Loading interfaces enumeration from file "+params.getInterfSerFile());
			interfaces = (StructureInterfaceList)Goodies.readFromFile(params.getInterfSerFile());
		} catch (ClassNotFoundException e) {
			throw new EppicException(e,"Couldn't load interface enumeration binary file: "+e.getMessage(),true);
		} catch (IOException e) {
			throw new EppicException(e,"Couldn't load interface enumeration binary file: "+e.getMessage(),true);
		}
		// TODO check that the input from serialized file matches the PDB input 
		
	}

	public void doFindInterfaces() throws EppicException {

		params.getProgressLog().println("Calculating possible interfaces...");
		LOGGER.info("Calculating possible interfaces");
		CrystalBuilder interfFinder = new CrystalBuilder(pdb);
		interfaces = interfFinder.getUniqueInterfaces(EppicParams.INTERFACE_DIST_CUTOFF);
		interfaces.calcAsas(params.getnSpherePointsASAcalc(), params.getNumThreads(), params.getMinSizeCofactorForAsa());
		interfaces.removeInterfacesBelowArea(EppicParams.MIN_INTERFACE_AREA_TO_KEEP);
		
		LOGGER.info("Interfaces calculated with "+params.getnSpherePointsASAcalc()+" sphere points.");

		LOGGER.info("Calculating interface clusters");

		int clustersSize = interfaces.getClusters().size();
		int numInterfaces = interfaces.size();
		LOGGER.info("Interface clustering done: "+numInterfaces+" interfaces - "+clustersSize+" clusters");
		String msg = "Interface clusters: ";
		for (int i=0; i<clustersSize;i++) {
			StructureInterfaceCluster cluster = interfaces.getClusters().get(i);
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

		if (!params.isGenerateModelSerializedFile()) {
			// we only produce the interfaces.dat file if not in -w mode (for WUI not to produce so many files)
			// TODO need everything in Biojava to be serializable for this to work
//			try {
//				Goodies.serialize(params.getOutputFile(EppicParams.INTERFACESDAT_FILE_SUFFIX),interfaces);
//			} catch (IOException e) {
//				throw new EppicException(e,"Couldn't write serialized ChainInterfaceList object to file: "+e.getMessage(),false);
//			}
		}
		
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
		for (StructureInterfaceCluster interfaceCluster:interfaces.getClusters()) {
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
		// TODO pass the biounit annotation to the model adaptor
		//modelAdaptor.setPdbBioUnits(this.pdb.getPdbBioUnitList());
		modelAdaptor.setGeometryScores(gps, gcps);
		modelAdaptor.setResidueDetails(interfaces);
		
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
		
		// 3 write pdb biounit list file
		try {
			toW.writePdbAssignments();
		} catch (IOException e) {
			throw new EppicException(e, "Could not write the PDB bio-unit assignments file: "+e.getMessage(), false);
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
	
	public void doWritePdbFiles() throws EppicException {

		if (interfaces.size() == 0) return;
		
		if (!params.isGenerateInterfacesPdbFiles()) return;
		
		try {
			if (!params.isDoEvolScoring()) {
				// no evol scoring: plain PDB files without altered bfactors
				for (StructureInterface interf : interfaces) {
					File pdbFile = params.getOutputFile("." + interf.getId() + ".pdb.gz");
					PrintStream ps = new PrintStream(new GZIPOutputStream(new FileOutputStream(pdbFile)));
					ps.print(interf.toPDB());
					ps.close();
				}
			} else {
				// writing PDB files with entropies as bfactors
				for (InterfaceEvolContext iec:iecList) {
					File pdbFile = params.getOutputFile("." + iec.getInterface().getId() + ".pdb.gz");
					iec.writePdbFile(pdbFile);
				}
			}
			
		} catch (IOException e) {
			throw new EppicException(e, "Couldn't write interface PDB files. " + e.getMessage(), true);
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
		
		if (interfaces.size() == 0) return;
		
		PymolRunner pr = null;
		params.getProgressLog().println("Writing PyMOL files");
		writeStep("Generating Thumbnails and PyMOL Files");
		LOGGER.info("Generating PyMOL files");
		try {
			pr = new PymolRunner(params.getPymolExe());
			pr.readColorsFromPropertiesFile(EppicParams.COLORS_PROPERTIES_IS);
			pr.readColorMappingsFromResourceFile(EppicParams.PYMOL_COLOR_MAPPINGS_IS);

		} catch (IOException e) {
			throw new EppicException(e,"Couldn't read colors file. Won't generate thumbnails or pse/pml files. " + e.getMessage(),true);
		}		

		try {
			for (StructureInterface interf:interfaces) {
				pr.generateInterfPngPsePml(interf, 
						params.getCAcutoffForGeom(), params.getMinAsaForSurface(), 
						params.getOutputFile("."+interf.getId()+".pdb.gz"), 
						params.getOutputFile("."+interf.getId()+".pse"),
						params.getOutputFile("."+interf.getId()+".pml"),
						params.getBaseName()+"."+interf.getId()	);
				LOGGER.info("Generated PyMOL files for interface "+interf.getId());
				MolViewersAdaptor.writeJmolScriptFile(interf, params.getCAcutoffForGeom(), params.getMinAsaForSurface(), pr, 
						params.getOutDir(), params.getBaseName());
			}

			if (params.isDoEvolScoring()) {
				for (ChainEvolContext cec:cecs.getAllChainEvolContext()) {
					Chain chain = pdb.getChainByPDB(cec.getRepresentativeChainCode());
					cec.setConservationScoresAsBfactors(chain);
					File chainPdbFile = params.getOutputFile("."+cec.getRepresentativeChainCode()+EppicParams.ENTROPIES_FILE_SUFFIX+".pdb");
					File chainPseFile = params.getOutputFile("."+cec.getRepresentativeChainCode()+EppicParams.ENTROPIES_FILE_SUFFIX+".pse");
					File chainPmlFile = params.getOutputFile("."+cec.getRepresentativeChainCode()+EppicParams.ENTROPIES_FILE_SUFFIX+".pml");
					File chainIconPngFile = params.getOutputFile("."+cec.getRepresentativeChainCode()+EppicParams.ENTROPIES_FILE_SUFFIX+".png");
					// TODO check that the file is written correctly like this with biojava
					Structure s = new StructureImpl();
					s.addChain(chain);
					PrintWriter pw = new PrintWriter(chainPdbFile);
					pw.write(s.toPDB());
					pw.close();
					pr.generateChainPse(chain, interfaces, 
							params.getCAcutoffForGeom(), params.getCAcutoffForZscore(), params.getMinAsaForSurface(),
							chainPdbFile, 
							chainPseFile, 
							chainPmlFile,
							chainIconPngFile,
							EppicParams.COLOR_ENTROPIES_ICON_WIDTH,
							EppicParams.COLOR_ENTROPIES_ICON_HEIGHT,
							0,params.getMaxEntropy() );
				}
			}
			
		} catch (IOException e) {
			throw new EppicException(e, "Couldn't write thumbnails, PyMOL pse/pml files or jmol files. "+e.getMessage(),true);
		} catch (InterruptedException e) {
			throw new EppicException(e, "Couldn't generate thumbnails, PyMOL pse/pml files or jmol files, PyMOL thread interrupted: "+e.getMessage(),true);
		} catch (StructureException e) {
			throw new EppicException(e, "Couldn't find chain id in input structure, something is wrong! "+e.getMessage(), true);
		}

	}

	public void doCompressFiles() throws EppicException {
		
		if (interfaces.size()==0) return;
		
		if (!params.isGenerateThumbnails()) return;
		// from here only if in -l mode: compress pse, chain pses and pdbs, create zip
		
		params.getProgressLog().println("Compressing files");
		LOGGER.info("Compressing files");
		
		try {
			for (StructureInterface interf:interfaces) {
				File pseFile = params.getOutputFile("."+interf.getId()+".pse");
				File gzipPseFile = params.getOutputFile("."+interf.getId()+".pse.gz");

				// pse (only if in -l mode)
				Goodies.gzipFile(pseFile, gzipPseFile);
				pseFile.delete();				
			}
			
			if (params.isDoEvolScoring()) {
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
			sb.append(DataModelAdaptor.getChainClusterString(chainCluster));
			sb.append(" ");
		}
		
		LOGGER.info(sb.toString());
	}
	
	public void doLoadEvolContextFromFile() throws EppicException {
		if (interfaces.size()==0) return;
		
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
		
		if (!params.isGenerateModelSerializedFile()) {
			// TODO to write the serialized file with Biojava we need to make everything Serializable
			// we only produce the chainevolcontext.dat file if not in -w mode (for WUI not to produce so many files)
			//try {
			//	Goodies.serialize(params.getOutputFile(EppicParams.CHAINEVCONTEXTDAT_FILE_SUFFIX),cecs);
			//} catch (IOException e) {
			//	throw new EppicException(e,"Couldn't write serialized ChainEvolContextList object to file: "+e.getMessage(),false);
			//}
		}
		
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
			iecList.setCoreSurfaceScoreStrategy(params.getCoreSurfaceScoreStrategy());
			iecList.scoreCoreSurface();

			// note this adds also the entropies to the residue details
			modelAdaptor.setEvolScores(iecList);
				
		}

		
	}
	
	public void doCombinedScoring() throws EppicException {
		if (interfaces.size()==0) return;
		
		List<CombinedPredictor> cps = new ArrayList<CombinedPredictor>();

		for (int i=0;i<iecList.size();i++) {
			CombinedPredictor cp = 
					new CombinedPredictor(iecList.get(i), gps.get(i), iecList.get(i).getEvolCoreRimPredictor(), iecList.get(i).getEvolCoreSurfacePredictor());
			cp.computeScores();
			cps.add(cp);
		}
		
		List<CombinedClusterPredictor> ccps = new ArrayList<CombinedClusterPredictor>();		
		int i = 0;
		for (StructureInterfaceCluster ic:interfaces.getClusters()) {
			int clusterId = ic.getId();
			CombinedClusterPredictor ccp = 
					new CombinedClusterPredictor(ic,iecList,
							gcps.get(i),
							iecList.getEvolCoreRimClusterPredictor(clusterId),
							iecList.getEvolCoreSurfaceClusterPredictor(clusterId));
			ccp.computeScores();
			ccps.add(ccp);
			i++;
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
	
	public void run(String[] args) {

		
		
		long start = System.nanoTime();

		try {
						
			params.parseCommandLine(args);

			// this has to come after getting the command line args, since it reads the location and name of log file from those
			setUpLogging();

			
			LOGGER.info(EppicParams.PROGRAM_NAME+" version "+EppicParams.PROGRAM_VERSION);
			
			loadConfigFile();
			
			try {
				LOGGER.info("Running in host "+InetAddress.getLocalHost().getHostName());
			} catch (UnknownHostException e) {
				LOGGER.warn("Could not determine host where we are running.");
			}
			
			// 0 load pdb
			doLoadPdb();

			// 1 finding interfaces
			if (params.getInterfSerFile()!=null) {
				doLoadInterfacesFromFile();
			} else {
				doFindInterfaces();
			}
			
			// TODO call doHBPlus when fixed
			// try hbplus if executable is set, writes pdb files needed for it (which then are overwritten in doWritePdbFiles)
			//doHBPlus();
			
			doGeomScoring();
			
			if (params.isDoEvolScoring()) {
				// 2 finding evolutionary context
				if (params.getChainEvContextSerFile()!=null) {
					doLoadEvolContextFromFile();
				} else {
					doFindEvolContext();
				}

				// 3 scoring
				doEvolScoring();
				
				// 4 combined scoring
				doCombinedScoring();
			}
			
			// 5 write TSV files (only if not in -w) 	
			doWriteTextOutputFiles();
			
			// 6 write pdb files (only if in -l)
			doWritePdbFiles();
						
			// 7 writing pymol files (only if in -l)
			doWritePymolFiles();
			
			// 8 compressing files (only if in -l)
			doCompressFiles();
			
			// 9 writing out the model serialized file and "finish" file for web ui (only if in -w)
			doWriteFinalFiles();

			

			long end = System.nanoTime();
			LOGGER.info("Finished successfully (total runtime "+((end-start)/1000000000L)+"s)");

		} catch (EppicException e) {
			LOGGER.error(e.getMessage());
			e.exitIfFatal(1);
		} 
//		catch (Exception e) {
//			//e.printStackTrace();
//
//			String stack = "";
//			for (StackTraceElement el:e.getStackTrace()) {
//				stack+="\tat "+el.toString()+"\n";				
//			}
//			LOGGER.error("Unexpected error. Stack trace:\n"+e+"\n"+stack+
//					"\nPlease report a bug to "+EppicParams.CONTACT_EMAIL);
//			System.exit(1);
//		}
		
		
	}
	

}
