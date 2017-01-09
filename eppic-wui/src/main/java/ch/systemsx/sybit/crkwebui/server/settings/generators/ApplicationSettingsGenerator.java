package ch.systemsx.sybit.crkwebui.server.settings.generators;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.shared.exceptions.ParsingException;
import ch.systemsx.sybit.crkwebui.shared.model.ApplicationSettings;

/**
 * Application settings generator.
 */
public class ApplicationSettingsGenerator 
{
	
	private static final Logger logger = LoggerFactory.getLogger(ApplicationSettingsGenerator.class);
	
	
	public static final String DEVELOPMENT_MODE = "development_mode";
	/**
	 * General server settings.
	 */
	private Properties globalProperties;
	
	/**
	 * Creates instance of application settings generator with set general server properties.
	 * @param globalProperties general server properties
	 */
	public ApplicationSettingsGenerator(Properties globalProperties)
	{
		this.globalProperties = globalProperties;
	}
	
	/**
	 * Creates application settings using provided input streams.
	 * @param inputParametersStream input parameters input stream
	 * @param helpPageStream stream containing help page content
	 * @param downloadsPageStream stream containing downloads page content
	 * @param releasesPageStream stream containing releases page content
	 * @param gridPropertiesInputStream stream containing settings of the grids
	 * @return application settings
	 * @throws ParsingException when can not properly prepare application settings
	 */
	public ApplicationSettings generateApplicationSettings(InputStream inputParametersStream,
														   InputStream gridPropertiesInputStream) throws ParsingException
	{
		ApplicationSettings settings = initializeApplicationSettings(inputParametersStream);
		settings.setGridProperties(prepareGridProperties(gridPropertiesInputStream));

		boolean useCaptcha = Boolean.parseBoolean(globalProperties.getProperty("use_captcha","false"));
		String captchaPublicKey = globalProperties.getProperty("captcha_public_key");
		int nrOfAllowedSubmissionsWithoutCaptcha = Integer.parseInt(globalProperties.getProperty("nr_of_allowed_submissions_without_captcha"));
		String pdbLinkUrl = globalProperties.getProperty("pdb_link_url");
		String uniparcLinkUrl = globalProperties.getProperty("uniparc_link_url");
		String uniprotLinkUrl = globalProperties.getProperty("uniprot_link_url");
		String wikipediaLinkUrl = globalProperties.getProperty("wikipedia_link_url");
		String publicationLinkUrl = globalProperties.getProperty("publication_link_url");
		boolean usePrecompiledResults = Boolean.parseBoolean(globalProperties.getProperty("use_precompiled","true"));
		String uniprotVersion = globalProperties.getProperty("uniprot_version");
		String examplePdb = globalProperties.getProperty("example_pdb");
		String eppicExplorerUrl = globalProperties.getProperty("eppic_explorer_url");
		double resolutionCutOff;
		if(globalProperties.getProperty("resolution_cutoff")!=null)
			resolutionCutOff = Double.parseDouble(globalProperties.getProperty("resolution_cutoff"));
		else{
			logger.warn("Resolution cut off value not found from the server properties file.");
			resolutionCutOff = -2;
		}

		double rfreeCutOff;
		if(globalProperties.getProperty("rfree_cutoff")!=null)
			rfreeCutOff = Double.parseDouble(globalProperties.getProperty("rfree_cutoff"));
		else{
			logger.warn("R-Free cut off value not found from the server properties file.");
			rfreeCutOff = -2;
		}
		
		int maxXMLCalls;
		if(globalProperties.getProperty("max_jobs_in_one_call")!=null){
			maxXMLCalls = Integer.parseInt(globalProperties.getProperty("max_jobs_in_one_call"));
		}
		else{
			logger.warn("Warning: Max XML jobs value not found from the server properties file.");
			maxXMLCalls = 1;
		}
		
		String javaVM;
		if(globalProperties.getProperty("java_VM_exec")!=null && !globalProperties.getProperty("java_VM_exec").equals("")){
			javaVM = globalProperties.getProperty("java_VM_exec");
		}
		else{
			logger.info("java VM not found from the server properties file, using default value 'java'");
			javaVM = "java";
		}
		
		settings.setCaptchaPublicKey(captchaPublicKey);
		settings.setUseCaptcha(useCaptcha);
		settings.setNrOfAllowedSubmissionsWithoutCaptcha(nrOfAllowedSubmissionsWithoutCaptcha);
		settings.setPdbLinkUrl(pdbLinkUrl);
		settings.setEppicExplorerUrl(eppicExplorerUrl);
		settings.setUniparcLinkUrl(uniparcLinkUrl);
		settings.setUniprotLinkUrl(uniprotLinkUrl);
		settings.setWikipediaUrl(wikipediaLinkUrl);
		settings.setPublicationLinkUrl(publicationLinkUrl);
		settings.setUsePrecompiledResults(usePrecompiledResults);
		settings.setUniprotVersion(uniprotVersion);
		settings.setExamplePdb(examplePdb);
		boolean readOnlyMode = Boolean.parseBoolean(globalProperties.getProperty("read_only_mode","false"));
		settings.setReadOnlyMode(readOnlyMode);
		boolean devMode = Boolean.parseBoolean(globalProperties.getProperty(DEVELOPMENT_MODE,"false"));
		settings.setDevelopmentMode(devMode);
		settings.setResolutionCutOff(resolutionCutOff);
		settings.setRfreeCutOff(rfreeCutOff);
		settings.setMaxXMLCalls(maxXMLCalls);
		settings.setJavaVMExec(javaVM);

		settings.setResultsLocation(globalProperties.getProperty("results_location"));
		return settings;
	}
	
	/**
	 * Creates initial application settings.
	 * @param inputParametersStream stream containing input parameters
	 * @return initial application settings
	 * @throws ParsingException when can not properly initialize application settings
	 */
	private ApplicationSettings initializeApplicationSettings(InputStream inputParametersStream) throws ParsingException
	{
		try
		{
			ApplicationSettings settings = InputParametersGenerator.prepareApplicationSettings(inputParametersStream);
			return settings;
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			throw new ParsingException("Error during preparing input parameters");
		}
	}
	
	/**
	 * Retrieves settings of the grids.
	 * @param gridPropertiesInputStream stream containing settings of the grids
	 * @return settings of the grids
	 * @throws ParsingException when can not properly prepare settings of the grids
	 */
	private Map<String, String> prepareGridProperties(InputStream gridPropertiesInputStream) throws ParsingException
	{
		try
		{
			Map<String, String> gridProperties = GridPropertiesGenerator.prepareGridProperties(gridPropertiesInputStream);
			return gridProperties;
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			throw new ParsingException("Error during preparing grid properties");
		}
	}
}
