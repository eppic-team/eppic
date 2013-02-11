package ch.systemsx.sybit.crkwebui.server.ip.validators;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import ch.systemsx.sybit.crkwebui.server.db.dao.IPAllowedDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.IPForbiddenDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.JobDAO;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.IPAllowedDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.IPForbiddenDAOJpa;
import ch.systemsx.sybit.crkwebui.server.db.dao.jpa.JobDAOJpa;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

/**
 * This class is used to verify whether job can be submitted from specified IP. 
 * @author AS
 */
public class IPVerifier 
{
	/**
	 * Checks whether job can be submitted from specified IP address
	 * @param ip ip address
	 * @param defaultNrOfAllowedSubmissionsForIP default number of allowed submissions for IP address 
	 * @throws ValidationException when job can not be submitted
	 * @throws DaoException when can not retrieve information from data storage
	 */
	public static void verifyIfCanBeSubmitted(String ip,
											  int defaultNrOfAllowedSubmissionsForIP) throws ValidationException, DaoException
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
			
			JobDAO jobDAO = new JobDAOJpa();
			long nrOfJobsForIPDuringLastDay = jobDAO.getNrOfJobsForIPDuringLastDay(ip);
			
			if(nrOfJobsForIPDuringLastDay >= nrOfAllowedSubmissionsForIPDuringOneDay)
			{
				Calendar calendar = Calendar.getInstance(); 
				Date date = jobDAO.getOldestJobSubmissionDateDuringLastDay(ip);
				calendar.setTime(date);
				calendar.add(Calendar.DATE, 1);
				
				String formattedDate = (DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(calendar.getTime()));
				
				throw new ValidationException("Submitting jobs from IP: " + ip + " is not allowed - too many submissions " +
									" - current nr of submissions: " + nrOfJobsForIPDuringLastDay + 
									" equals allowed nr of allowed submissions per 24h: " + nrOfAllowedSubmissionsForIPDuringOneDay + 
									" - Please contact the administrator if you want to increase number of allowed submissions or try after: " +
									formattedDate);
			}
		}
	}
}
