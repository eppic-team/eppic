package eppic.db.tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Main {

	public static void main(String[] args) {
		if (args.length < 1)
			printUsageAndExit();
		else {
			callTheTool(args);
		}
	}

	private static void callTheTool(String[] args) {
		String toolName = args[0];
		String[] toolParams = Arrays.copyOfRange(args, 1, args.length);
		try {
			Class<?> toolClass = Class.forName("eppic.db.tools." + toolName);
			Method main = toolClass.getMethod("main", String[].class);
			main.invoke(null, new Object[] { toolParams });
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			System.err.println("Unexpected error: "+e.getMessage());
			e.printStackTrace();
			System.exit(1);
		} catch (ClassNotFoundException e) {
			System.err.println("The given tool '"+toolName+"' is not recognised as one of the supported tools");
			printUsageAndExit();
		}
	}

	private static void printUsageAndExit() {
		System.err
				.println("The first parameter must be the name of a tool.\n"
						+ " Available tools: ClusterCrystalForms, ClusterSequences, CompareLattices, UploadToDb, UserJobCopier, DumpToXml, FindBiomimics");
		System.exit(1);
	}

}
