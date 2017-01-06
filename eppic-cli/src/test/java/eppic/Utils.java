package eppic;

import java.io.File;

public class Utils {
	
	protected static EppicParams generateEppicParams(String pdbId, File outDir) {
		EppicParams params = new EppicParams();
		params.setInputStr(pdbId);
		params.setInput();
		params.setOutDir(outDir);
		params.setAlphabet(EppicParams.DEF_ENTROPY_ALPHABET);
		params.setnSpherePointsASAcalc(100);
		return params;
	}

}
