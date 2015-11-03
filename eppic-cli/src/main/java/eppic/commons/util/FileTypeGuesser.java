package eppic.commons.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.biojava.nbio.core.sequence.io.util.IOUtils;

/** 
 * Class: 		FileTypeGuesser
 * Author:		Henning Stehr, stehr@molgen.mpg.de
 * 
 * Provides static methods for guessing the type of several protein related file formats.
 * File type determination is based on matching a RegEx signature in the first line of the file.
 * Provides a main method for file type guessing from the command line.
 * 
 * Supported file types
 * - PDB files
 * - PDB atom line files
 * - Casp TS (3D prediction) files
 * - Casp RR (contact prediction) files
 * - mmCIF files
 * - CMView contact map files
 */
public class FileTypeGuesser {

	/*------------------------------ constants ------------------------------*/
	// file types
	public static final int UNKNOWN_FILE	= 0;	// unknown file type
	public static final int PDB_FILE		= 1;	// PDB file with header
	public static final int RAW_PDB_FILE 	= 2;	// file with PDB atom lines
	public static final int CASP_TS_FILE 	= 3;	// CASP 3D prediction file
	public static final int CASP_RR_FILE 	= 4;	// CASP contact prediction file
	public static final int OWL_CM_FILE 	= 5;	// contact map file
	public static final int CIF_FILE 		= 6;	// mmCIF file from PDB
	
	// names of the file types as above
	private static final String[] FILE_TYPE_NAMES  = {
		"Unknown file",
		"PDB file",
		"File with PDB atom lines",
		"Casp 3D prediction file",
		"Casp contact prediction file",
		"Contact map file",
		"PDB mmCIF file"
	};
	
	// signatures for the files as above
	private static final String[] FILE_SIGNATURES  = {
		"(?:HEADER|TITLE|COMPND|SOURCE|EXPDTA|REMARK|DBREF|SEQRES|CRYST1|MODEL|HELIX|SHEET).*",
		"ATOM\\s+.*",
		"PFRMAT\\s+TS.*",
		"PFRMAT\\s+RR.*",
		"#(?:AGLAPPE|CMVIEW|OWL) GRAPH FILE.*",
		// mmCIF files from PDB always start with "data_PDBcode". 
		// Phenix's cif files do start with data_ but then with no PDB code 
		"data_\\w+.*"
	};
	
	/*---------------------------- public methods ---------------------------*/
	
	/**
	 * Attempts to determine the type of the given file. If file type could be
	 * identified, returns the file type constant, otherwise UNKNOWN_FILE_TYPE.
	 * 
	 * File type constant can be:
	 * PDB_FILE		 	PDB file with header
	 * RAW_PDB_FILE 	File with PDB atom lines
	 * CASP_TS_FILE 	CASP 3D prediction file
	 * CASP_RR_FILE 	CASP contact prediction file
	 * OWL_CM_FILE 		Contact map file
	 * CIF_FILE 		mmCIF file from PDB	
	 * 
	 * @param file the file whose type to guess
	 * @throws FileNotFoundException if file could not be found
	 * @throws IOException if an error occured while reading the file
	 * @return the determined file type or UNKNOWN_FILE_TYPE
	 */
	public static int guessFileType(File file) throws FileNotFoundException, IOException {
		
		BufferedReader in = new BufferedReader(new InputStreamReader(IOUtils.openFile(file)));
		String firstLine = in.readLine();
		if(firstLine != null) {
			// be lenient here and allow one empty line at the beginning
			if(firstLine.trim().length() == 0) firstLine = in.readLine();
			for(int i=1; i <= FILE_SIGNATURES.length; i++) {
				if(lineMatches(firstLine, FILE_SIGNATURES[i-1])) {
					in.close();
					return i;
				}
			}
		}
		in.close();
		return UNKNOWN_FILE;
	}
	
	/**
	 * Returns a string with the name of the given file type constant or null if no name is defined.
	 * The file types are defined as public constants in this class.
	 * @param fileType the file type number
	 * @return The name of the file type or null if no name is defined for the given constant
	 */
	public static String getFileTypeName(int fileType) {
		if(fileType > FILE_TYPE_NAMES.length) return null;
		return FILE_TYPE_NAMES[fileType];
	}
	
	/*---------------------------- private methods --------------------------*/
	
	/**
	 * Returns true if the line matches the regular expression, false
	 * if it does not match or line is null.
	 * @param line the line of text
	 * @param regex the regular expression to match
	 * @return true if regex matches the line, false otherwise
	 */
	private static boolean lineMatches(String line, String regex) {
			if(line == null) return false;
			Pattern pattern = 
	            Pattern.compile(regex);
			Matcher matcher = 
	            pattern.matcher(line.trim());
			return matcher.matches();
	}
	
	
	/**
	 * Tries to guess the type of the given file and prints out the results.
	 * @param args the file name
	 */
	public static void main(String[] args) {
		
		if(args.length != 1) {
			System.out.println("Usage: FileTypeGuesser.java file");
			System.exit(1);
		}
			
		String fileName = args[0];
		
		try {
			int fileType = guessFileType(new File(fileName));
			String fileTypeName = getFileTypeName(fileType);
			if(fileType != UNKNOWN_FILE && fileTypeName != null) {
				System.out.println("I believe " + fileName + " is a " + fileTypeName);
			} else {
				System.out.println("I really can't figure out the file type of " + fileName);
			}
		} catch(IOException e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
}
