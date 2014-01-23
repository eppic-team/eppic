package ch.systemsx.sybit.crkwebui.server.runners;

import java.io.File;
import java.util.List;

import ch.systemsx.sybit.crkwebui.server.commons.util.log.LogHandler;
import ch.systemsx.sybit.crkwebui.server.commons.validators.RunJobDataValidator;
import ch.systemsx.sybit.crkwebui.server.jobs.managers.commons.JobManager;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;

/**
 * This class is used to start crk application.
 * @author srebniak_a
 *
 */
public class CrkRunner
{
	private JobManager jobManager;
	private String crkApplicationLocation;
	private int nrOfThreadsForSubmission;
	private int assignedMemory;

	public CrkRunner(JobManager jobManager,
					 String crkApplicationLocation,
					 int nrOfThreadsForSubmission,
					 int assignedMemory)
	{
		this.jobManager = jobManager;
		this.crkApplicationLocation = crkApplicationLocation;
		this.nrOfThreadsForSubmission = nrOfThreadsForSubmission;
		this.assignedMemory = assignedMemory;
	}

	/**
	 * Starts job.
	 * @return submission id
	 * @throws Exception when can not start job
	 */
	public String run(RunJobData runJobData,
					  String destinationDirectoryName,
					  int inputType) throws Exception
	{
		File logFile = new File(destinationDirectoryName, "crklog");
		LogHandler.writeToLogFile(logFile, "Job submitted - please wait\n");

		File runFile = new File(destinationDirectoryName, "crkrun");
		runFile.createNewFile();

		RunJobDataValidator.validateJobId(runJobData.getJobId());
		RunJobDataValidator.validateInput(runJobData.getInput());
		RunJobDataValidator.validateInputParameters(runJobData.getInputParameters());

		List<String> crkCommand = CrkCommandGenerator.createCrkCommand(crkApplicationLocation,
															  runJobData.getInput(),
															  inputType,
															  runJobData.getInputParameters(),
															  destinationDirectoryName,
															  nrOfThreadsForSubmission,
															  assignedMemory);

	    return jobManager.startJob(runJobData.getJobId(), 
	    						   crkCommand, 
	    						   destinationDirectoryName,
	    						   nrOfThreadsForSubmission);
	}
}
