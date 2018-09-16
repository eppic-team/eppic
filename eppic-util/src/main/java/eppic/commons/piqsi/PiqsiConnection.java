package eppic.commons.piqsi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class PiqsiConnection {
	
	private File piqsiFile;
	
	private HashMap<String,PiqsiAnnotation> annotations;
	
	public PiqsiConnection(File piqsiFile) throws IOException {
		this.piqsiFile = piqsiFile;
		this.annotations = new HashMap<String, PiqsiAnnotation>();
		parsePiqsi();
	}
	
	private void parsePiqsi() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(piqsiFile));
		
		String line;
		while ((line=br.readLine())!=null) {
			if (line.startsWith("PDB code")) continue;
			
			String[] fields = line.split("\t");
			
			String pdbCode = fields[0];
			int pdbSubunits = Integer.parseInt(fields[2]);
			int piqsiSubunits = Integer.parseInt(fields[3]);
			String pdbSymmetry = fields[4];
			String piqsiSymmetry = fields[5];
			
			PiqsiAnnotation an = new PiqsiAnnotation(pdbCode, pdbSubunits, piqsiSubunits, pdbSymmetry, piqsiSymmetry);
			annotations.put(pdbCode,an);
			
		}
		
		
		br.close();
	}
	
	/**
	 * Returns the PiQSi annotation or null if there's no annotation in PiQSi for the given PDB code
	 * @param pdbCode
	 * @return
	 */
	public PiqsiAnnotation getAnnotation(String pdbCode) {
		return annotations.get(pdbCode);
	}

}
