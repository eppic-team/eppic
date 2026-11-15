package eppic;

import java.io.File;

import eppic.cli.CommonCliParams;
import picocli.CommandLine;

public class Utils {
	
	/**
	 * Generates the params for running the given PDB id with output in given dir. 
	 * The params are obtained by parsing a command line, so that all defaults are the same 
	 * as when running the CLI. 
	 * @param pdbId
	 * @param outDir
	 * @return
	 */
	public static EppicParams generateEppicParams(String pdbId, File outDir) {
		CommonCliParams cliParams = new CommonCliParams();
		new CommandLine(cliParams).parseArgs("-i", pdbId, "-o", outDir.toString(), "-t", outDir.toString());
		EppicParams params;
		try {
			params = cliParams.toEppicParams();
		} catch (EppicException e) {
			throw new RuntimeException(e);
		}
		params.setInput(pdbId);
		params.setAlphabet(EppicParams.DEF_ENTROPY_ALPHABET);
		params.setnSpherePointsASAcalc(100);
		return params;
	}

}
