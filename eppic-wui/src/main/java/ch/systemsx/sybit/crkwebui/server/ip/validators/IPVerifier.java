package ch.systemsx.sybit.crkwebui.server.ip.validators;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.db.dao.DaoException;
import ch.systemsx.sybit.crkwebui.server.db.dao.DataDownloadTrackingDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.IPAllowedDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.IPForbiddenDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.JobDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.DataDownloadTrackingDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.IPAllowedDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.IPForbiddenDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.JobDAOJpa;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

/**
 * This class is used to verify whether job can be submitted from specified IP. 
 * @author AS
 */
public class IPVerifier 
{
	
	private static final Logger log = LoggerFactory.getLogger(IPVerifier.class);
	
	/**
	 * Checks whether job can be submitted from specified IP address
	 * @param ip
	 * @param defaultNrOfAllowedSubmissionsForIP
	 * @throws ValidationException
	 * @throws DaoException
	 */
	public static void verifyIfCanBeSubmitted(String ip,
			  int defaultNrOfAllowedSubmissionsForIP) throws ValidationException, DaoException
	{
		verifyIfCanBeSubmitted(ip, defaultNrOfAllowedSubmissionsForIP, false);
	}
	
	/**
	 * Checks whether job can be submitted from specified IP address
	 * @param ip ip address
	 * @param defaultNrOfAllowedSubmissionsForIP default number of allowed submissions for IP address 	 
	 * @param verifyFromDownloads if the verification is to be done for datadownload servlet
	 * @throws ValidationException when job can not be submitted
	 * @throws DaoException when can not retrieve information from data storage
	 */
	public static void verifyIfCanBeSubmitted(String ip,
											  int defaultNrOfAllowedSubmissionsForIP,
											  boolean verifyFromDownloads) throws ValidationException, DaoException
	{
		IPForbiddenDAO ipForbiddenDAO = new IPForbiddenDAOJpa();
		boolean isIpForbidden = ipForbiddenDAO.isIPForbidden(ip);
		
		if(isIpForbidden)
		{
			throw new ValidationException("Submitting jobs from IP: " + ip + " is not allowed. Please contact the administrator");
		}
		else
		{
			IPAllowedDAO ipAllowedDAO = new IPAllowedDAOJpa();
			
			int nrOfAllowedSubmissionsForIPDuringOneDay  = ipAllowedDAO.getNrOfAllowedSubmissionsForIP(ip);
			
			if(nrOfAllowedSubmissionsForIPDuringOneDay <= 0)
			{
				nrOfAllowedSubmissionsForIPDuringOneDay = defaultNrOfAllowedSubmissionsForIP;
			}
			
			long nrOfJobsForIPDuringLastDay;
			Date date;
			if(!verifyFromDownloads){
				JobDAO jobDAO = new JobDAOJpa();
				nrOfJobsForIPDuringLastDay = jobDAO.getNrOfJobsForIPDuringLastDay(ip);
				date = jobDAO.getOldestJobSubmissionDateDuringLastDay(ip);
			}
			else{
				DataDownloadTrackingDAO downloadIPDAO = new DataDownloadTrackingDAOJpa();
				nrOfJobsForIPDuringLastDay = downloadIPDAO.getNrOfDownloadsForIPDuringLastDay(ip);
				date = downloadIPDAO.getOldestJobDownloadDateDuringLastDay(ip);
			}
			
			if(nrOfJobsForIPDuringLastDay >= nrOfAllowedSubmissionsForIPDuringOneDay)
			{
				Calendar calendar = Calendar.getInstance(); 
				
				calendar.setTime(date);
				calendar.add(Calendar.DATE, 1);
				
				String formattedDate = (DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(calendar.getTime()));
				
				log.info("Number of submissions per IP exceeded for IP="+ip+
						" (max allowed per IP and day is "+nrOfAllowedSubmissionsForIPDuringOneDay+")");
				
				throw new ValidationException(
						"Too many submissions for IP=" + ip + 
						" - current number of submissions: " + nrOfJobsForIPDuringLastDay + 
						" equals allowed submissions per 24h: " + nrOfAllowedSubmissionsForIPDuringOneDay + 
						" - Please contact the administrator if you want to increase number of allowed submissions or try after: " +
						formattedDate);
			}
		}
	}
}
