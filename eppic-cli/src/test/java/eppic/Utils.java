package eppic;

import java.io.File;

public class Utils {
	
	public static EppicParams generateEppicParams(String pdbId, File outDir) {
		EppicParams params = new EppicParams();
		params.setInputStr(pdbId);
		params.setInput();
		params.setOutDir(outDir);
		params.setTempCoordFilesDir(outDir);
		params.setAlphabet(EppicParams.DEF_ENTROPY_ALPHABET);
		params.setnSpherePointsASAcalc(100);
		return params;
	}

}
