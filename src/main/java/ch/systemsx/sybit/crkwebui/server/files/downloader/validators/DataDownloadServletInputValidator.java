package ch.systemsx.sybit.crkwebui.server.files.downloader.validators;

import java.util.List;
import java.util.Map;

import ch.systemsx.sybit.crkwebui.server.db.dao.JobDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.JobDAOJpa;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

public class DataDownloadServletInputValidator {

	/**
	 * Validates correctness of input data necessary to produce xml file.
	 * @param type type of the file
	 * @param jobIdMap map of identifier of the job to the interfaceId's
	 * @param getSeqInfo string with t/f to provide seq info or not
	 * @param maxXMLCalls maximum number of Job Ids to be used in one call
	 * @throws ValidationException when validation fails
	 * @throws DaoException 
	 */
	public static void validateFileDownloadInput(String type,
											   Map<String, List<Integer>> jobIdMap,
											   String getSeqInfo,
											   int maxXMLJobs) throws ValidationException, DaoException
	{
		if(type == null || !type.equals("xml")){
			throw new ValidationException("Please provide a correct value of file type to be downloaded with &type=");
		}
		
		if(jobIdMap.size() > maxXMLJobs){
			throw new ValidationException("Exceeded maximum number of jobs allowed ("+maxXMLJobs+") to be retrieved in one call");
		}
		
		for(String jobId: jobIdMap.keySet()){
			checkIfResultsExist(jobId);
		}
		
		if(getSeqInfo != null){
			if(!( getSeqInfo.equals("t") || getSeqInfo.equals("f") )){
				throw new ValidationException("Please provide a correct value with &getSeqInfo=  ; (allowed: t/f)");
			}
		}
		
	}
	

	/**
	 * 
	 * @param jobId
	 * @throws DaoException 
	 * @throws Exception
	 */
	private static void checkIfResultsExist(String jobId) throws ValidationException, DaoException
	{
		String status = null;

		JobDAO jobDAO = new JobDAOJpa();
		status = jobDAO.getStatusForJob(jobId);

		if(status == null || !status.equals(StatusOfJob.FINISHED.getName()))
		{
				throw new ValidationException("Nothing found with the provided id:"+ jobId);
		}

	}
}
