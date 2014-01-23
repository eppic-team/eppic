package ch.systemsx.sybit.crkwebui.server.jobs.managers.drmaa;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

import ch.systemsx.sybit.crkwebui.server.jobs.generators.queuing.NativeSpecificationGenerator;
import ch.systemsx.sybit.crkwebui.server.jobs.generators.queuing.NativeSpecificationGeneratorFactory;
import ch.systemsx.sybit.crkwebui.server.jobs.managers.commons.JobManager;
import ch.systemsx.sybit.crkwebui.shared.exceptions.JobHandlerException;
import ch.systemsx.sybit.crkwebui.shared.exceptions.JobManagerException;
import ch.systemsx.sybit.crkwebui.shared.model.StatusOfJob;

/**
 * Drmaa specific implementation of JobManager.
 * @author AS
 */
public class DrmaaJobManager implements JobManager
{
	private SessionFactory sessionFactory;
	private Session session;

	private String queuingSystemName;
	private Properties queuingSystemProperties;
	private String jobsDirectory;

	/**
	 * Creates instance of drmaa job manager.
	 * @param queuingSystemName name of the queuing system
	 * @param queuingSystemProperties native properties for queuing system
	 * @param jobsDirectory directory where results of jobs are stored
	 * @throws JobManagerException when session can not be initialized
	 */
	public DrmaaJobManager(String queuingSystemName,
						   Properties queuingSystemProperties, 
						   String jobsDirectory) throws JobManagerException
	{
		sessionFactory = SessionFactory.getFactory();
		session = sessionFactory.getSession();
		try
		{
			session.init("");
		}
		catch (Throwable e)
		{
			throw new JobManagerException("Can not initialize " + queuingSystemName + " session", e);
		}

		this.queuingSystemName = queuingSystemName;
		this.queuingSystemProperties = queuingSystemProperties;
		this.jobsDirectory = jobsDirectory;
	}

	@Override
	public String startJob(String jobId,
						   List<String> command,
						   String jobDirectory,
						   int nrOfThreadsForSubmission) throws JobHandlerException
	{
		try
		{
			JobTemplate jobTemplate = session.createJobTemplate();
			jobTemplate.setRemoteCommand("java");
			jobTemplate.setArgs(command);
			jobTemplate.setJobName(jobId);
			jobTemplate.setErrorPath(":" + jobDirectory);
			jobTemplate.setOutputPath(":" + jobDirectory);
			
			NativeSpecificationGenerator nativeSpecificationGenerator = NativeSpecificationGeneratorFactory.
						getNativeSpecificationGenerator(queuingSystemName, 
														queuingSystemProperties);
			
			String nativeSpecification = nativeSpecificationGenerator.
						generateNativeSpecificationForSubmission(nrOfThreadsForSubmission);
			
			if(nativeSpecification != null)
			{
				jobTemplate.setNativeSpecification(nativeSpecification);
			}

			String submissionId = session.runJob(jobTemplate);
			
	      	session.deleteJobTemplate(jobTemplate);
	      	
	      	return submissionId;
		}
		catch(Throwable e)
		{
			throw new JobHandlerException(e);
		}
	}

	@Override
	public StatusOfJob getStatusOfJob(String jobId, String submissionId) throws JobHandlerException
	{
		StatusOfJob statusOfJob = StatusOfJob.QUEUING;

		try
		{
			int jobStatus = 0;
			
			try
			{
				jobStatus = session.getJobProgramStatus(submissionId);
			}
			catch(Throwable t)
			{
				
			}
			
			switch(jobStatus)
			{
				case Session.RUNNING:
					statusOfJob = StatusOfJob.RUNNING;
					break;
					
				case Session.SYSTEM_SUSPENDED:
					statusOfJob = StatusOfJob.WAITING;
					break;
					
				case Session.SYSTEM_ON_HOLD:
					statusOfJob = StatusOfJob.WAITING;
					break;
					
				case Session.USER_SUSPENDED:
					statusOfJob = StatusOfJob.WAITING;
					break;
					
				case Session.USER_ON_HOLD:
					statusOfJob = StatusOfJob.WAITING;
					break;
					
				case Session.DONE:
					if(checkIfJobWasFinishedSuccessfully(jobId))
					{
						statusOfJob = StatusOfJob.FINISHED;
					}
					else
					{
						statusOfJob = StatusOfJob.ERROR;
					}
					break;
					
				case Session.FAILED:
					if(checkIfJobWasStopped(jobId))
					{
						statusOfJob = StatusOfJob.STOPPED;
					}
					else
					{
						statusOfJob = StatusOfJob.ERROR;
					}
					break;
					
				case Session.UNDETERMINED:
					if(checkIfJobWasSubmitted(jobId))
					{
						if(checkIfJobWasFinishedSuccessfully(jobId))
						{
							statusOfJob = StatusOfJob.FINISHED;
						}
						else if(checkIfJobWasStopped(jobId))
						{
							statusOfJob = StatusOfJob.STOPPED;
						}
						else
						{
							statusOfJob = StatusOfJob.ERROR;
						}
					}
					else
					{
						statusOfJob = StatusOfJob.NONEXISTING;
					}

					break;
			}
		}
		catch (Throwable e)
		{
			throw new JobHandlerException(e);
		}

		return statusOfJob;
	}

	@Override
	public void stopJob(String submissionId) throws JobHandlerException
	{
		try
		{
			if((submissionId != null) &&
				(session.getJobProgramStatus(submissionId) != Session.FAILED) &&
				(session.getJobProgramStatus(submissionId) != Session.DONE))
			{
				session.control(submissionId, Session.TERMINATE);
			}
		}
		catch (Throwable t)
		{
			throw new JobHandlerException(t);
		}
	}

	/**
	 * Checks whether directory for specified job has been generated.
	 * @param jobId identifier of the job
	 * @return information whether directory for specified jobid exists
	 */
	private boolean checkIfJobWasSubmitted(String jobId)
	{
		boolean wasJobSubmitted = false;

		File jobDirectory = new File(jobsDirectory, jobId);

		if(jobDirectory.exists())
		{
			wasJobSubmitted = true;
		}

		return wasJobSubmitted;
	}

	/**
	 * Checks if job was finished successfully. If job was finished successfully then finished file exists
	 * in job directory.
	 *
	 * @param jobId identifier of the job
	 * @return information whether job was finished successfully
	 */
	private boolean checkIfJobWasFinishedSuccessfully(String jobId)
	{
		boolean wasJobFinishedSuccessfully = false;

		File finishedJobDirectory = new File(jobsDirectory, jobId);
		File finishedJobFile = new File(finishedJobDirectory, "finished");

		if(finishedJobFile.exists())
		{
			wasJobFinishedSuccessfully = true;
		}

		return wasJobFinishedSuccessfully;
	}
	
	/**
	 * Checks if job was stopped. If job was stopped then killed file exists
	 * in job directory.
	 *
	 * @param jobId identifier of the job
	 * @return information whether job was stopped
	 */
	private boolean checkIfJobWasStopped(String jobId)
	{
		boolean wasStopped = false;

		File stoppedJobDirectory = new File(jobsDirectory, jobId);
		File stoppedJobFile = new File(stoppedJobDirectory, "killed");

		if(stoppedJobFile.exists())
		{
			wasStopped = true;
		}

		return wasStopped;
	}

	@Override
	public void finalize() throws JobHandlerException
	{
		try
		{
			session.exit();
		}
		catch (Throwable e)
		{
			throw new JobHandlerException(e);
		}
	}
}
