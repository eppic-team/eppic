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
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			printUsageAndExit();
		}
	}

	private static void printUsageAndExit() {
		System.err
				.println("The first parameter must be the name of a tool.\n Available tools: ClusterCrystalForms, ClusterSequences, CompareLattices, UploadToDb, UserJobDBHandler");
		System.exit(1);
	}

}
