package eppic.rest.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import eppic.model.dto.InputParameters;
import eppic.model.shared.InputType;
import eppic.rest.commons.AppConstants;

/**
 * EPPIC command generator.
 * @author AS
 *
 */
public class EppicCliGenerator {
	/**
	 * Creates EPPIC command to execute.
	 * @param crkApplicationLocation location of EPPIC executable jar
	 * @param input input
	 * @param inputType type of the input
	 * @param inputParameters input parameters
	 * @param destinationDirectoryName directory where results of the job are to be stored
	 * @param nrOfThreadsForSubmission nr of threads used to run command
	 * @param assignedMemory memory assigned for execution of the command
	 * @return crk command
	 */
	public static List<String> createCrkCommand(String crkApplicationLocation,
												String input,
												int inputType,
												InputParameters inputParameters,
												String destinationDirectoryName,
												int nrOfThreadsForSubmission,
												int assignedMemory)
	{
		List<String> command = new ArrayList<>();
		command.add("-Xmx" + assignedMemory + "m");
		command.add("-jar");
		command.add(crkApplicationLocation);
		command.add("-i");

		String inputLocation = input;

		// TODO we use basename as the identifier for the job: make sure it is set correctly to the random alphanumeric string when the job is run
		String baseName = input;
		if(inputType == InputType.FILE.getIndex())
		{
			baseName = truncateFileName(input);
			inputLocation = destinationDirectoryName + File.separator + input;
		}

		command.add(inputLocation);
		command.add("-o");
		command.add(destinationDirectoryName);
		command.add("-b");
		command.add(baseName);
		command.add("-q");
		command.add(String.valueOf(inputParameters.getMaxNrOfSequences()));

		command.add("-d");
		command.add(String.valueOf(inputParameters.getSoftIdentityCutoff()));
		command.add("-D");
		command.add(String.valueOf(inputParameters.getHardIdentityCutoff()));
		command.add("-H");
		command.add(inputParameters.getSearchMode().toLowerCase());
		command.add("-a");
		command.add(String.valueOf(nrOfThreadsForSubmission));

		// we always run evolutionary calculations (we used to be able to choose that from input, not anymore)
		command.add("-s");

		command.add("-L");
		command.add(destinationDirectoryName + File.separator + AppConstants.PROGRESS_LOG_FILE_NAME);
		command.add("-l"); // for thumbnails and mmcif files
		command.add("-P"); // for json files, assembly diagram thumbnails (requires dot)
		command.add("-w"); // for webui.dat file

		return command;
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
