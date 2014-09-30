package ch.systemsx.sybit.crkwebui.server.runners;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.systemsx.sybit.crkwebui.server.CrkWebServiceImpl;
import ch.systemsx.sybit.crkwebui.server.commons.util.log.LogHandler;
import ch.systemsx.sybit.crkwebui.server.commons.validators.RunJobDataValidator;
import ch.systemsx.sybit.crkwebui.server.jobs.managers.commons.JobManager;
import ch.systemsx.sybit.crkwebui.shared.model.RunJobData;

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

		File runFile = new File(destinationDirectoryName, CrkWebServiceImpl.RUN_FILE_NAME);
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

		// logging the command
		StringBuffer sb = new StringBuffer();
		for (String token:crkCommand) {
			sb.append(token+" ");
		}
		log.info("Running user job: "+sb.toString());
		
	    return jobManager.startJob(javaVMExec,
	    		                   runJobData.getJobId(), 
	    						   crkCommand, 
	    						   destinationDirectoryName,
	    						   nrOfThreadsForSubmission);
	}
}
