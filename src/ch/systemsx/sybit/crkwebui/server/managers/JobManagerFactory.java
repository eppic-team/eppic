package ch.systemsx.sybit.crkwebui.server.managers;

import ch.systemsx.sybit.crkwebui.shared.exceptions.JobManagerException;

/**
 * Factory used to select appropriate job manager.
 * @author AS
 */
public class JobManagerFactory
{
	/**
	 * Retrieves instance of job manager by name.
	 * @param jobManagerName name of the job manager
	 * @param jobsDirectory directory where results of the job are stored
	 * @return job manager instance
	 * @throws JobManagerException when can not create job manager
	 */
	public static JobManager getJobManager(String jobManagerName,
										   String jobsDirectory) throws JobManagerException
	{
		JobManager jobManager = null;

		if(jobManagerName != null)
		{
			if(jobManagerName.equals("drmaa"))
			{
				jobManager = new DrmaaJobManager(jobsDirectory);
			}
		}

		return jobManager;
	}
}
