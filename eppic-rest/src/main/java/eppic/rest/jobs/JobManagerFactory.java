package eppic.rest.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 */
	public static JobManager getJobManager(String jobsDirectory, int numWorkers) {

		LOGGER.info("Initialising native job manager with jobsDirectory {} and num workers {}", jobsDirectory, numWorkers);

		return new NativeJobManager(jobsDirectory, numWorkers);
	}
}
