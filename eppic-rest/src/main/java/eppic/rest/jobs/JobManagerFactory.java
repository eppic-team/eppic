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
		return getJobManager(jobsDirectory, numWorkers, null);
	}

	/**
	 * Retrieves instance of job manager.
	 * @param jobsDirectory directory where results of the job are stored
	 * @param numWorkers the number of worker slots for the job manager
	 * @param jobStatusStore the store of job statuses shared by all replicas, or null if there is only one replica
	 * @return job manager instance
	 */
	public static JobManager getJobManager(String jobsDirectory, int numWorkers, JobStatusStore jobStatusStore) {

		LOGGER.info("Initialising native job manager with jobsDirectory {}, num workers {} and job status store {}",
				jobsDirectory, numWorkers, jobStatusStore == null ? "none" : jobStatusStore.getClass().getSimpleName());

		return new NativeJobManager(jobsDirectory, numWorkers, jobStatusStore);
	}
}
