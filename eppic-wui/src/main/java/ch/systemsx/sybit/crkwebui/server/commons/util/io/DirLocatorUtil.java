package ch.systemsx.sybit.crkwebui.server.commons.util.io;

import java.io.File;

import eppic.model.dto.ApplicationSettings;

public class DirLocatorUtil {
	
	/**
	 * Given a baseDir where jobs are stored in the file system, returns the directory where the job is located:
	 * <li>
	 * If jobId is a PDB id (4 characters): the directory will be: baseDir/divided/mid-2-letters-from-PDB-id/jobId
	 * </li>
	 * <li>
	 * If jobId is a long alphanumerical string (more than 4 characters): the directory is directly under baseDir: baseDir/jobId
	 * </li>
	 * @param baseDir
	 * @param jobId
	 * @return
	 */
	public static File getJobDir(File baseDir, String jobId) {
		
		// CASE 1: jobId is a PDB id (4 letters): we direct to divided layout
		if (jobId.length()==4) {
			
			String midPdbIndex = jobId.substring(1,3);
			
			return new File(baseDir, "divided" + File.separator + midPdbIndex + File.separator + jobId);
			
		// CASE 2: jobId is a long alphanumerical string (user job): directory directly under baseDir
		} else {
			
			return new File(baseDir, jobId);
		} 
		
	}
	
	/**
	 * Given a baseUrl where jobs are stored, returns the URL where the job is located:
	 * <li>
	 * If jobId is a PDB id (4 characters): the URL will be: baseUrl/divided/mid-2-letters-from-PDB-id/jobId
	 * </li>
	 * <li>
	 * If jobId is a long alphanumerical string (more than 4 characters): the URL is directly under baseUrl: baseUrl/jobId
	 * </li>
	 * See ApplicationSettings#getResultsLocationForJob(String) for the equivalent client side call
	 * @param baseUrl
	 * @param jobId
	 * @see ApplicationSettings#getResultsLocationForJob(String) 
	 * @return
	 */
	public static String getJobUrlPath(String baseUrl, String jobId) {
		
		if (baseUrl.endsWith("/")) 
			baseUrl = baseUrl.substring(0, baseUrl.length()-1);

		
		// CASE 1: jobId is a PDB id (4 letters): we direct to divided layout
		if (jobId.length()==4) {
			
			String midPdbIndex = jobId.substring(1,3);
						
			return baseUrl + "/" + "divided" + "/" + midPdbIndex + "/" + jobId;
			
		// CASE 2: jobId is a long alphanumerical string (user job): directory directly under baseDir
		} else {
			
			return baseUrl +"/" + jobId;
		} 
		
	}

}
