package ch.systemsx.sybit.crkwebui.server.util;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import ch.systemsx.sybit.crkwebui.server.db.model.IPAllowedDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.IPAllowedDAOImpl;
import ch.systemsx.sybit.crkwebui.server.db.model.IPForbiddenDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.IPForbiddenDAOImpl;
import ch.systemsx.sybit.crkwebui.server.db.model.JobDAO;
import ch.systemsx.sybit.crkwebui.server.db.model.JobDAOImpl;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;

public class IPVerifier 
{
	public static void verifyIfCanBeSubmitted(String ip,
											  int defaultNrOfAllowedSubmissionsForIP) throws CrkWebException
	{
		IPForbiddenDAO ipForbiddenDAO = new IPForbiddenDAOImpl();
		boolean isIpForbidden = ipForbiddenDAO.isIPForbidden(ip);
		
		if(isIpForbidden)
		{
			throw new CrkWebException("Submitting jobs from IP: " + ip + " is not allowed. Please contact with the administrator");
		}
		else
		{
			IPAllowedDAO ipAllowedDAO = new IPAllowedDAOImpl();
			
			int nrOfAllowedSubmissionsForIPDuringOneDay  = ipAllowedDAO.getNrOfAllowedSubmissionsForIP(ip);
			
			if(nrOfAllowedSubmissionsForIPDuringOneDay <= 0)
			{
				nrOfAllowedSubmissionsForIPDuringOneDay = defaultNrOfAllowedSubmissionsForIP;
			}
			
			JobDAO jobDAO = new JobDAOImpl();
			long nrOfJobsForIPDuringLastDay = jobDAO.getNrOfJobsForIPDuringLastDay(ip);
			
			if(nrOfJobsForIPDuringLastDay >= nrOfAllowedSubmissionsForIPDuringOneDay)
			{
				Calendar calendar = Calendar.getInstance(); 
				Date date = jobDAO.getOldestJobSubmissionDateDuringLastDay(ip);
				calendar.setTime(date);
				calendar.add(Calendar.DATE, 1);
				
				String formattedDate = (DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(calendar.getTime()));
				
				throw new CrkWebException("Submitting jobs from IP: " + ip + " is not allowed - too many submissions " +
									" - current nr of submissions: " + nrOfJobsForIPDuringLastDay + 
									" equals allowed nr of allowed submissions per 24h: " + nrOfAllowedSubmissionsForIPDuringOneDay + 
									" - Please contact with the administrator if you want to increase number of allowed submissions or try after: " +
									formattedDate);
			}
		}
	}
}
