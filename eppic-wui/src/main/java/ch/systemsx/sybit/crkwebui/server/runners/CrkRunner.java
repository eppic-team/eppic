package ch.systemsx.sybit.crkwebui.server.runners;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.CrkWebServiceImpl;
import ch.systemsx.sybit.crkwebui.server.commons.util.log.LogHandler;
import ch.systemsx.sybit.crkwebui.server.commons.validators.RunJobDataValidator;
import ch.systemsx.sybit.crkwebui.server.jobs.managers.commons.JobManager;
import eppic.model.dto.RunJobData;

/**
 * This class is used to start the EPPIC CLI application (formerly known as CRK).
 * @author srebniak_a
 *
 */
public class CrkRunner
{
	
	private static final Logger log = LoggerFactory.getLogger(CrkRunner.class);
	
	private JobManager jobManager;
	private String crkApplicationLocation;
	private int nrOfThreadsForSubmission;
	private int assignedMemory;
	private String javaVMExec;

	public CrkRunner(JobManager jobManager,
					 String crkApplicationLocation,
					 int nrOfThreadsForSubmission,
					 int assignedMemory,
					 String javaVMExec)
	{
		this.jobManager = jobManager;
		this.crkApplicationLocation = crkApplicationLocation;
		this.nrOfThreadsForSubmission = nrOfThreadsForSubmission;
		this.assignedMemory = assignedMemory;
		this.javaVMExec = javaVMExec;
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
		File logFile = new File(destinationDirectoryName, CrkWebServiceImpl.PROGRESS_LOG_FILE_NAME);
		LogHandler.writeToLogFile(logFile, "Job submitted - please wait\n");

		RunJobDataValidator.validateJobId(runJobData.getJobId());
		RunJobDataValidator.validateInput(runJobData.getInput());
		RunJobDataValidator.validateInputParameters(runJobData.getInputParameters());

		List<String> eppicCommand = new ArrayList<>();
		eppicCommand.add(javaVMExec);
		eppicCommand.addAll(CrkCommandGenerator.createCrkCommand(crkApplicationLocation,
															  runJobData.getInput(),
															  inputType,
															  runJobData.getInputParameters(),
															  destinationDirectoryName,
															  nrOfThreadsForSubmission,
															  assignedMemory));

		// logging the command
		log.info("Running user job: {}", String.join(" ", eppicCommand));
		
		if (jobManager==null) {
			log.error("jobManager is null! A NPE will follow!");
		}
		
	    return jobManager.startJob(runJobData.getJobId(),
	    						   eppicCommand,
	    						   destinationDirectoryName,
	    						   nrOfThreadsForSubmission);
	}
}
