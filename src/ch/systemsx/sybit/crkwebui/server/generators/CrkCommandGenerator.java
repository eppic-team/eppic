package ch.systemsx.sybit.crkwebui.server.generators;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.model.InputParameters;
import ch.systemsx.sybit.crkwebui.shared.model.InputType;

/**
 * Crk command generator.
 * @author AS
 *
 */
public class CrkCommandGenerator
{
	/**
	 * Creates crk command to execute.
	 * @param crkApplicationLocation location of crk executable
	 * @param input input
	 * @param inputType type of the input
	 * @param inputParameters input parameters
	 * @param destinationDirectoryName directory where results of the job are to be stored
	 * @return crk command
	 */
	public static List<String> createCrkCommand(String crkApplicationLocation,
												String input,
												int inputType,
												InputParameters inputParameters,
												String destinationDirectoryName)
	{
		List<String> command = new ArrayList<String>();
		command.add("-jar");
		command.add(crkApplicationLocation);
		command.add("-i");

		String inputLocation = input;

		if(inputType == InputType.FILE.getIndex())
		{
			inputLocation = destinationDirectoryName + File.separator + input;
		}

		command.add(inputLocation);
		command.add("-o");
		command.add(destinationDirectoryName);
		command.add("-q");
		command.add(String.valueOf(inputParameters.getMaxNrOfSequences()));

		command.add("-d");
		command.add(String.valueOf(inputParameters.getSoftIdentityCutoff()));
		command.add("-D");
		command.add(String.valueOf(inputParameters.getHardIdentityCutoff()));
		command.add("-r");
		command.add(String.valueOf(inputParameters.getReducedAlphabet()));
		command.add("-H");
		command.add(inputParameters.getSearchMode().toLowerCase());
		command.add("-a");
		command.add(String.valueOf("1"));

		if(inputParameters.getMethods() != null)
		{
			for(String method : inputParameters.getMethods())
			{
				if(method.equals("Entropy"))
				{
					command.add("-s");
				}
			}
		}

		command.add("-L");
		command.add(destinationDirectoryName + File.separator + "crklog");
		command.add("-l");

		return command;
	}
}
