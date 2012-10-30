package ch.systemsx.sybit.crkwebui.server;

import java.io.File;
import java.util.List;

import ch.systemsx.sybit.crkwebui.server.generators.CrkCommandGenerator;
import ch.systemsx.sybit.crkwebui.server.managers.JobManager;
import ch.systemsx.sybit.crkwebui.server.util.LogHandler;
import ch.systemsx.sybit.crkwebui.server.validators.RunJobDataValidator;
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

	public CrkRunner(JobManager jobManager,
					 String crkApplicationLocation,
					 int nrOfThreadsForSubmission)
	{
		this.jobManager = jobManager;
		this.crkApplicationLocation = crkApplicationLocation;
		this.nrOfThreadsForSubmission = nrOfThreadsForSubmission;
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
															  nrOfThreadsForSubmission);

	    return jobManager.startJob(runJobData.getJobId(), 
	    						   crkCommand, 
	    						   destinationDirectoryName,
	    						   nrOfThreadsForSubmission);
	}
}
