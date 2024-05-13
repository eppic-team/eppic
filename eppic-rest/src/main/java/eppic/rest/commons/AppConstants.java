package eppic.rest.commons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AppConstants {

    private static final Logger logger = LoggerFactory.getLogger(AppConstants.class);


    public static final String ENDPOINTS_COMMON_PREFIX = "rest/api/";

    /**
     * The name of the parameter to be passed to JVM as system property with -D
     */
    public static final String DB_PROPERTIES_PARAM =  "eppicDbProperties";

    /**
     * The file where the progress log of the eppic CLI program is written to (using -L option)
     */
    public static final String PROGRESS_LOG_FILE_NAME 	= "eppic_wui_progress.log";

    /**
     * The file that the eppic CLI writes upon successful completion, used to signal that the queuing job finished successfully
     * Must coincide with EppicParams.FINISHED_FILE_NAME (from eppic-clie module, which is not a dependency for this module)
     */
    public static final String FINISHED_FILE_NAME 		= "finished";

    /**
     * The file to signal a killed job
     */
    public static final String KILLED_FILE_NAME 		= "killed";

    public static final String CONFIG_FILE_URL_SYS_PROPERTY = "eppicServerConfFile";


}