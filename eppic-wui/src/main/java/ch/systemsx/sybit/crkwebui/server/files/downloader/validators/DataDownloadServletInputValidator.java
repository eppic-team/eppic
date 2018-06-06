package ch.systemsx.sybit.crkwebui.server.files.downloader.validators;

import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;
import eppic.model.shared.StatusOfJob;
import eppic.db.dao.DaoException;
import eppic.db.dao.JobDAO;
import eppic.db.dao.jpa.JobDAOJpa;

public class DataDownloadServletInputValidator {
	
	/**
	 * Validates correctness of input data necessary to produce xml/json file.
	 * @param type type of the file
	 * @param jobId the jobId (pdb id if precalculater result)
	 * @param getSeqInfo string with t/f to provide seq info or not
	 * @param getResInfo string with t/f to provide res info or not
	 * @throws ValidationException when validation fails
	 * @throws DaoException 
	 */
	public static void validateFileDownloadInput(String type,
												 String jobId,
												 String getSeqInfo,
												 String getResInfo) throws ValidationException, DaoException
	{
		if(type == null || type.trim().isEmpty()){
			throw new ValidationException("Please provide a correct value of file type to be downloaded with &type=");
		}

		if (!type.equals("xml") && !type.equals("json")) {
			throw new ValidationException("Please provide a correct value of file type to be downloaded with &type= (either 'xml' or 'json')");
		}

		checkIfResultsExist(jobId);
		
		if(getSeqInfo != null){
			if(!( getSeqInfo.equals("t") || getSeqInfo.equals("f") )){
				throw new ValidationException("Please provide a correct value with &getSeqInfo=  ; (allowed: t/f)");
			}
		}

		if (getResInfo !=null ) {
			if(!( getResInfo.equals("t") || getResInfo.equals("f") )){
				throw new ValidationException("Please provide a correct value with &getResInfo=  ; (allowed: t/f)");
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
		StatusOfJob status = null;

		JobDAO jobDAO = new JobDAOJpa();
		status = jobDAO.getStatusForJob(jobId);

		if(status == null || !status.equals(StatusOfJob.FINISHED))
		{
				throw new ValidationException("Nothing found with the provided id:"+ jobId);
		}

	}
}
