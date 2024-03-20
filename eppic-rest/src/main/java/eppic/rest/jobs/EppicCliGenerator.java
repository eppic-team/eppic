package eppic.rest.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eppic.rest.commons.AppConstants;

/**
 * EPPIC command generator.
 * @author AS
 *
 */
public class EppicCliGenerator {
	/**
	 * Creates EPPIC command to execute.
	 * @param eppicJarPath location of EPPIC executable jar
	 * @param inputFile input
	 * @param entryId an identifier to write out to json serialized file and db as the entryId
	 * @param destinationDirectoryName directory where results of the job are to be stored
	 * @param nrOfThreadsForSubmission nr of threads used to run command
	 * @param assignedMemory memory to be assigned to JVM for execution of the command (in Megabytes)
	 * @param skipEvolAnalysis if true, no evolutionary analysis will be performed (much faster, because it doesn't require sequence search)
	 * @return the eppic CLI command
	 */
	public static List<String> generateCommand(String javaVMExec, String eppicJarPath,
											   File inputFile,
											   String entryId,
											   String destinationDirectoryName,
											   int nrOfThreadsForSubmission,
											   int assignedMemory,
											   File cliConfigFile,
											   boolean skipEvolAnalysis) {

		List<String> cmd = new ArrayList<>(Arrays.asList(
				javaVMExec,
				"-Xmx" + assignedMemory + "m",
				"-jar", eppicJarPath,
				"-i", inputFile.getAbsolutePath(),
				"-b", entryId,
				"-o", destinationDirectoryName,
				"-a", String.valueOf(nrOfThreadsForSubmission),
				"-L", destinationDirectoryName + File.separator + AppConstants.PROGRESS_LOG_FILE_NAME,
				"-l", // for thumbnails and mmcif files
				"-P", // for json files, assembly diagram thumbnails (requires dot)
				"-w", // for serialized output files
				"-g", cliConfigFile.getAbsolutePath()
		));
		if (!skipEvolAnalysis)
			cmd.add("-s");
		return cmd;
	}
}
