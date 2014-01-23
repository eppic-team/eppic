package ch.systemsx.sybit.crkwebui.server.jobs.managers;

import java.util.Properties;

import ch.systemsx.sybit.crkwebui.server.jobs.managers.commons.JobManager;
import ch.systemsx.sybit.crkwebui.server.jobs.managers.drmaa.DrmaaJobManager;
import ch.systemsx.sybit.crkwebui.shared.exceptions.JobManagerException;

/**
 * Factory used to select appropriate job manager.
 * @author AS
 */
public class JobManagerFactory
{
	/**
	 * Retrieves instance of job manager by name.
	 * @param queuingSystemName name of the queuing system
	 * @param queuingSystemProperties native specification properties for queuing system
	 * @param jobsDirectory directory where results of the job are stored
	 * @return job manager instance
	 * @throws JobManagerException when can not create job manager
	 */
	public static JobManager getJobManager(String queuingSystemName,
										   Properties queuingSystemProperties,
										   String jobsDirectory) throws JobManagerException
	{
		JobManager jobManager = null;

		if(queuingSystemName != null)
		{
			if(queuingSystemName.equals("sge"))
			{
				jobManager = new DrmaaJobManager(queuingSystemName, 
												 queuingSystemProperties,
												 jobsDirectory);
			}
		}

		return jobManager;
	}
}
