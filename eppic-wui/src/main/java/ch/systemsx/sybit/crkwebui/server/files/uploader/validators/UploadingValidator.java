package ch.systemsx.sybit.crkwebui.server.files.uploader.validators;

import ch.systemsx.sybit.crkwebui.server.commons.validators.CaptchaValidator;
import ch.systemsx.sybit.crkwebui.server.files.uploader.data.UploadingData;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;
import eppic.db.dao.DaoException;
import eppic.db.dao.JobDAO;
import eppic.db.dao.mongo.JobDAOMongo;

public class UploadingValidator 
{
	private boolean useCaptcha;
	private CaptchaValidator captchaValidator;
	private int nrOfAllowedSubmissionsWithoutCaptcha = 1;
	

	public UploadingValidator(boolean useCaptcha,
							  int nrOfAllowedSubmissionsWithoutCaptcha,
							  String captchaPublicKey,
							  String captchaPrivateKey)
	{
		this.useCaptcha = useCaptcha;
		this.nrOfAllowedSubmissionsWithoutCaptcha = nrOfAllowedSubmissionsWithoutCaptcha;
		this.captchaValidator = new CaptchaValidator(captchaPublicKey, captchaPrivateKey);
		
	}
	
	/**
	 * Validates right to upload file.
	 * @param uploadingData data to upload
	 * @param ip ip of the current user
	 * @throws ValidationException when validation fails
	 */
	public void validate(UploadingData uploadingData,
						 String ip) throws ValidationException
	{
		try
		{
			validateCaptcha(uploadingData, ip);
		}
		catch(Exception e)
		{
			throw new ValidationException(e);
		}
	}
	
	/**
	 * Validates captcha challenge when one is to be used.
	 * @param uploadingData data to upload
	 * @param ip ip of the user
	 * @throws ValidationException when validation fails
	 * @throws DaoException when data can not be retrieved from db
	 */
	private void validateCaptcha(UploadingData uploadingData,
								 String ip) throws ValidationException, DaoException
	{
		JobDAO jobDAO = new JobDAOMongo();
		long nrOfSubmittedJobs = jobDAO.getNrOfJobsForIPDuringLastDay(ip);
		
		if((useCaptcha) && (nrOfSubmittedJobs > nrOfAllowedSubmissionsWithoutCaptcha))
		{
			captchaValidator.verifyChallenge(uploadingData.getCaptchaChallenge(), 
											 uploadingData.getCaptchaResponse(), 
											 ip);
		}
	}
}
