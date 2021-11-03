package ch.systemsx.sybit.crkwebui.server;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import eppic.EppicParams;

/**
 * The server side implementation of the RPC service.
 *
 * @author srebniak_a
 */
public class CrkWebServiceImpl
{

	private static final Logger logger = LoggerFactory.getLogger(CrkWebServiceImpl.class);

	// NOTE for the unaware reader: the EPPIC program used to be call CRK, thus the prevalence of that name in the code

	// config file locations
	
	// the within-war location for server config files, read with getServletContext().getResourceAsStream()
	private static final String CONFIG_FILES_RESOURCE_LOCATION = "/WEB-INF/classes/META-INF";

	/**
	 * The property that needs to be passed to JVM with a -D to specify the 
	 * directory where all config files are located (the "config profile").
	 */
	public static final String CONFIG_DIR_PROPERTY	  = "eppicWuiConfigDir";
	
	// the off-war location for server config files
	private static final String CONFIG_FILES_LOCATION = System.getProperty(CONFIG_DIR_PROPERTY);
	
	static {
		if (CONFIG_FILES_LOCATION==null || CONFIG_FILES_LOCATION.isEmpty()) {
			logger.error("Property {} wasn't specified correctly. The property must be passed to the JVM with a -D parameter", CONFIG_DIR_PROPERTY);
			throw new RuntimeException("Can't continue without a valid config file dir");
		} else if (! new File(CONFIG_FILES_LOCATION).isDirectory()) {
			logger.error("The value '{}' specified for the config files directory with system property {} is not a directory!", CONFIG_FILES_LOCATION , CONFIG_DIR_PROPERTY);
			throw new RuntimeException("Can't continue without a valid config file dir");
		} else {
			logger.info("Reading configuration files from dir {}, passed through property {}", CONFIG_FILES_LOCATION, CONFIG_DIR_PROPERTY);
		}
	}

	public static final String SERVER_PROPERTIES_FILE 	= CONFIG_FILES_LOCATION+"/server.properties";
	private static final String EMAIL_PROPERTIES_FILE   = CONFIG_FILES_LOCATION + "/email.properties";
	private static final String INPUT_PARAMS_FILE 		= CONFIG_FILES_LOCATION+"/input_parameters.xml";

	// note: grid.properties we read from within-war file, then from server-config dir if there is one
	// that way we can still externally configure, whilst keeping a default config in the packed war file
	private static final String GRID_PROPERTIES_FILE_RESOURCE 	= CONFIG_FILES_RESOURCE_LOCATION+"/grid.properties";
	private static final String GRID_PROPERTIES_FILE 	= CONFIG_FILES_LOCATION+"/grid.properties";


	// the file where the progress log of the eppic CLI program is written to (using -L option), used to be called 'crklog'
	public static final String PROGRESS_LOG_FILE_NAME 	= "eppic_wui_progress.log";

	// the file to signal a killed job
	public static final String KILLED_FILE_NAME 		= "killed";

	// the file that the eppic CLI writes upon successful completion, used to signal that the queuing job finished successfully
	public static final String FINISHED_FILE_NAME 		= EppicParams.FINISHED_FILE_NAME;

	// the suffix of the filename used for writing the steps for the progress animation
	public static final String STEPS_FILE_NAME_SUFFIX 	= ".steps.log";

}

