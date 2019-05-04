package ch.systemsx.sybit.crkwebui.server.jobs.managers;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.jobs.managers.commons.JobManager;
import ch.systemsx.sybit.crkwebui.shared.exceptions.JobManagerException;

/**
 * Factory used to select appropriate job manager.
 * @author AS
 */
public class JobManagerFactory
{
	private static final Logger LOGGER = LoggerFactory.getLogger(JobManagerFactory.class);
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
			
				LOGGER.info("Initialising DrmaaJobManager for queuing system {} with jobsDirectory {}", queuingSystemName, jobsDirectory);
				jobManager = new NativeJobManager(jobsDirectory);
			}
		}

		return jobManager;
	}
}
