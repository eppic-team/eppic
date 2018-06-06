package ch.systemsx.sybit.crkwebui.server.runners;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.server.CrkWebServiceImpl;
import eppic.model.dto.InputParameters;
import eppic.model.dto.PdbInfo;
import eppic.model.shared.InputType;

/**
 * EPPIC command generator.
 * @author AS
 *
 */
public class CrkCommandGenerator
{
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
		List<String> command = new ArrayList<String>();
		command.add("-Xmx" + assignedMemory + "m");
		command.add("-jar");
		command.add(crkApplicationLocation);
		command.add("-i");

		String inputLocation = input;

		String baseName = input;
		if(inputType == InputType.FILE.getIndex())
		{
			baseName = PdbInfo.truncateFileName(input);
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
		command.add(destinationDirectoryName + File.separator + CrkWebServiceImpl.PROGRESS_LOG_FILE_NAME);
		command.add("-l"); // for thumbnails and mmcif files
		command.add("-P"); // for json files, assembly diagram thumbnails (requires dot)
		command.add("-w"); // for webui.dat file

		return command;
	}
}
