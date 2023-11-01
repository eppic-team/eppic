package eppic.rest.jobs;

import java.io.File;
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
	 * @return the eppic CLI command
	 */
	public static List<String> generateCommand(String javaVMExec, String eppicJarPath,
											   File inputFile,
											   String entryId,
											   String baseNameForOutput,
											   String destinationDirectoryName,
											   int nrOfThreadsForSubmission,
											   int assignedMemory,
											   File cliConfigFile) {

		return Arrays.asList(
				javaVMExec,
				"-Xmx" + assignedMemory + "m",
				"-jar", eppicJarPath,
				"-i", inputFile.getAbsolutePath(),
				"-b", entryId,
				"-o", destinationDirectoryName,
				"-b", baseNameForOutput,
				"-a", String.valueOf(nrOfThreadsForSubmission),
				"-s", // we always run evolutionary calculations (we used to be able to choose that from input, not anymore)
				"-L", destinationDirectoryName + File.separator + AppConstants.PROGRESS_LOG_FILE_NAME,
				"-l", // for thumbnails and mmcif files
				"-P", // for json files, assembly diagram thumbnails (requires dot)
				"-w", // for serialized output files
				"-g", cliConfigFile.getAbsolutePath()
		);
	}
}
