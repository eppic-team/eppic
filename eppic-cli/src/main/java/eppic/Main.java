package eppic;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import eppic.assembly.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.biojava.nbio.structure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The eppic main class to execute the CLI workflow.
 * 
 * 
 * @author Jose Duarte
 *
 */
public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	// fields
	private EppicParams params;
	

	public Main() {
		this.params = new EppicParams();
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

	/**
	 * The main of EPPIC  
	 */
	public static void main(String[] args){
		
		Main eppicMain = new Main();
		
		eppicMain.run(args);
	}
	
	/**
	 * Run the full eppic analysis given a parameters object
	 * @param params the parameters
	 */
	public FullAnalysis run(EppicParams params) {
		this.params = params;
		return run(false);
	}
	
	/**
	 * Run the full eppic analysis given the command line arguments (which are then converted into an {@link EppicParams} object)
	 * @param args the CLI arguments
	 */
	public FullAnalysis run(String[] args) {
		
		try {
			params.parseCommandLine(args);
			
		} catch (EppicException e) {
			LOGGER.error(e.getMessage());
			e.exitIfFatal(1);
		}
		
		return run(true);
	}
	
	private FullAnalysis run(boolean loadConfigFile) {

		try {

			// this has to come after getting the command line args, since it reads the location and name of log file from those
			setUpLogging();
			
			logBuildAndHost();

			if (loadConfigFile) loadConfigFile();

            FullAnalysis fa = new FullAnalysis(params, params.getPdbCode(), params.isInputAFile(), params.getInFile());
            fa.run();
            return fa;

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
        return null;
	}

}
