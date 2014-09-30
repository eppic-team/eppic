package ch.systemsx.sybit.crkwebui.server.files.uploader.validators;

import ch.systemsx.sybit.crkwebui.server.commons.validators.CaptchaValidator;
import ch.systemsx.sybit.crkwebui.server.db.dao.JobDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.JobDAOJpa;
import ch.systemsx.sybit.crkwebui.server.files.uploader.data.UploadingData;
import ch.systemsx.sybit.crkwebui.server.ip.validators.IPVerifier;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

public class UploadingValidator 
{
	private boolean useCaptcha;
	private CaptchaValidator captchaValidator;
	private int nrOfAllowedSubmissionsWithoutCaptcha = 1;
	
	private boolean doIPBasedVerification;
	private int defaultNrOfAllowedSubmissionsForIP;
	
	public UploadingValidator(boolean useCaptcha,
							  int nrOfAllowedSubmissionsWithoutCaptcha,
							  String captchaPublicKey,
							  String captchaPrivateKey,
							  boolean doIPBasedVerification,
							  int defaultNrOfAllowedSubmissionsForIP)
	{
		this.useCaptcha = useCaptcha;
		this.nrOfAllowedSubmissionsWithoutCaptcha = nrOfAllowedSubmissionsWithoutCaptcha;
		this.captchaValidator = new CaptchaValidator(captchaPublicKey, captchaPrivateKey);
		
		this.doIPBasedVerification = doIPBasedVerification;
		this.defaultNrOfAllowedSubmissionsForIP = defaultNrOfAllowedSubmissionsForIP;
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
			validateIP(ip);
			validateCaptcha(uploadingData, ip);
		}
		catch(Exception e)
		{
			throw new ValidationException(e);
		}
	}
	
	/**
	 * Validates whether user with specified ip is allowed to upload file.
	 * @param ip ip of the user
	 * @throws ValidationException when validation fails
	 * @throws DaoException when data can not be retrieved from db
	 */
	private void validateIP(String ip) throws ValidationException, DaoException
	{
		if(doIPBasedVerification)
		{
			IPVerifier.verifyIfCanBeSubmitted(ip, 
											  defaultNrOfAllowedSubmissionsForIP);
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
		JobDAO jobDAO = new JobDAOJpa();
		long nrOfSubmittedJobs = jobDAO.getNrOfJobsForIPDuringLastDay(ip);
		
		if((useCaptcha) && (nrOfSubmittedJobs > nrOfAllowedSubmissionsWithoutCaptcha))
		{
			captchaValidator.verifyChallenge(uploadingData.getCaptchaChallenge(), 
											 uploadingData.getCaptchaResponse(), 
											 ip);
		}
	}
}
