package eppic.rest.jobs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eppic.rest.commons.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.model.dto.RunJobData;

/**
 * This class is used to start the EPPIC CLI application.
 * @author srebniak_a
 */
public class EppicCliRunner {

	private static final Logger log = LoggerFactory.getLogger(EppicCliRunner.class);

	private final JobManager jobManager;
	private final String crkApplicationLocation;
	private final int nrOfThreadsForSubmission;
	private final int assignedMemory;
	private final String javaVMExec;

	public EppicCliRunner(JobManager jobManager,
						  String crkApplicationLocation,
						  int nrOfThreadsForSubmission,
						  int assignedMemory,
						  String javaVMExec) {
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
					  int inputType) throws Exception {
		File logFile = new File(destinationDirectoryName, AppConstants.PROGRESS_LOG_FILE_NAME);
		writeToLogFile(logFile, "Job submitted - please wait\n");

		// TODO implement validation
		//RunJobDataValidator.validateJobId(runJobData.getJobId());
		//RunJobDataValidator.validateInput(runJobData.getInput());

		List<String> eppicCommand = new ArrayList<>();
		eppicCommand.add(javaVMExec);
		eppicCommand.addAll(EppicCliGenerator.createCrkCommand(crkApplicationLocation,
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

	private static void writeToLogFile(File logFile, String message) {
		try {
			FileOutputStream outputStream = new FileOutputStream(logFile, true);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
			bufferedOutputStream.write(message.getBytes());
		} catch (IOException e) {
			log.warn("Could not write message '{}' to log file {}", message, logFile);
		}

	}
}
