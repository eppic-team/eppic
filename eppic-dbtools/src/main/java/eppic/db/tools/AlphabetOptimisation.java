package eppic.db.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eppic.model.PdbInfoDB;
import gnu.getopt.Getopt;

public class AlphabetOptimisation {

	

	public static void main(String[] args) throws Exception {

		String help = 
				"Usage: AlphabetOptimisation\n" +
				" -D : the database name to use\n"+
				" -i : a list file of PDB codes\n"+
				"The database access must be set in file " + DBHandler.CONFIG_FILE_NAME + " in home dir\n";

		String dbName = null;
		String listFileName = null;
		
		Getopt g = new Getopt("AlphabetOptimisation", args, "D:i:h?");
		int c;
		while ((c = g.getopt()) != -1) {
			switch (c) {
			case 'D':
				dbName = g.getOptarg();
				break;
			case 'i':
				listFileName = g.getOptarg();
				break;
			case 'h':
				System.out.println(help);
				System.exit(0);
				break;
			case '?':
				System.err.println(help);
				System.exit(1);
				break; // getopt() already printed an error
			}
		}
		
		
		if (listFileName == null) {
			System.err.println("A file with a list of PDB codes have to be provided with -i");
			System.exit(1);
		}
		
		File listFile = new File(listFileName);
		if (!listFile.exists()) {
			System.err.println("Input string "+listFileName+" does not seem to be a file");
			System.exit(1);
		}
		
		DBHandler dbh = new DBHandler(dbName);
		
		List<String> pdbCodes = readList(listFile);

		List<PdbInfoDB> pdbInfos = dbh.deserializePdbList(pdbCodes);
		
		for (PdbInfoDB pdbInfo : pdbInfos) {
			System.out.println(pdbInfo.getPdbCode() + " " + pdbInfo.getChainClusters().size() + " chains");
		}
		
		
	}
	
	private static List<String> readList(File file) throws IOException {
		List<String> pdbs = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("#")) continue;
			if (line.trim().isEmpty()) continue;
			String pdbCode = line.trim(); 
			if (pdbCode.length() == 4) pdbs.add(pdbCode);
		}
		br.close();
		return pdbs;
	}
	
}
