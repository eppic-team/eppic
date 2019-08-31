package ch.systemsx.sybit.crkwebui.server.jobs.managers;

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
	 * Retrieves instance of job manager.
	 * @param jobsDirectory directory where results of the job are stored
	 * @param numWorkers the number of worker slots for the job manager
	 * @return job manager instance
	 * @throws JobManagerException when can not create job manager
	 */
	public static JobManager getJobManager(String jobsDirectory, int numWorkers) throws JobManagerException
	{

		LOGGER.info("Initialising native job manager with jobsDirectory {}", jobsDirectory);

		return new NativeJobManager(jobsDirectory, numWorkers);
	}
}
