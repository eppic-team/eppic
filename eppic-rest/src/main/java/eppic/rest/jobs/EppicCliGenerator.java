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
	 * @param destinationDirectoryName directory where results of the job are to be stored
	 * @param nrOfThreadsForSubmission nr of threads used to run command
	 * @param assignedMemory memory to be assigned to JVM for execution of the command (in Megabytes)
	 * @return crk command
	 */
	public static List<String> generateCommand(String javaVMExec, String eppicJarPath,
											   File inputFile,
											   String destinationDirectoryName,
											   int nrOfThreadsForSubmission,
											   int assignedMemory) {

		return Arrays.asList(
				javaVMExec,
				"-Xmx" + assignedMemory + "m",
				"-jar", eppicJarPath,
				"-i", inputFile.getAbsolutePath(),
				"-o", destinationDirectoryName,
				"-b", truncateFileName(inputFile.getName()),
				"-a", String.valueOf(nrOfThreadsForSubmission),
				"-s", // we always run evolutionary calculations (we used to be able to choose that from input, not anymore)
				"-L", destinationDirectoryName + File.separator + AppConstants.PROGRESS_LOG_FILE_NAME,
				"-l", // for thumbnails and mmcif files
				"-P", // for json files, assembly diagram thumbnails (requires dot)
				"-w"  // for serialized output files
		);
	}

	/**
	 * Truncates the given fileName by removing anything after the last dot.
	 * If no dot present in fileName then nothing is truncated.
	 * @param fileName
	 * @return
	 */
	private static String truncateFileName(String fileName) {
		if( fileName == null) return null;

		String newName = fileName;
		int lastPeriodPos = fileName.lastIndexOf('.');
		if (lastPeriodPos >= 0)
		{
			newName = fileName.substring(0, lastPeriodPos);
		}
		return newName;

	}
}
