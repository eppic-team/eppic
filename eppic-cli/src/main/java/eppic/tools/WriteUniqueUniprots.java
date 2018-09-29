package eppic.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eppic.EppicParams;
import eppic.commons.sequence.NoMatchFoundException;
import eppic.commons.sequence.SiftsConnection;
import eppic.commons.sequence.UniprotLocalConnection;
import eppic.commons.sequence.UnirefEntry;
import eppic.commons.util.Interval;
import gnu.getopt.Getopt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * Script to write out all unique uniprot sequence segments present in the SIFTS file into FASTA file/s.
 * The sequences are pulled from the local uniprot database.
 *
 * @author biyani_n
 *
 */
public class WriteUniqueUniprots {

	private static final Logger logger = LoggerFactory.getLogger(WriteUniqueUniprots.class);
	
	public SiftsConnection sc;
	public UniprotLocalConnection uniLC;
	
	public WriteUniqueUniprots(String scPath, String uniprotdb, String dbHost, String dbPort, String dbUser, String dbPwd) throws IOException, SQLException{
			sc = new SiftsConnection(scPath);
			uniLC = new UniprotLocalConnection(uniprotdb, dbHost, dbPort, dbUser, dbPwd);
	}

	public static void main(String[] args) throws IOException, SQLException {
		
		String help =
				"Usage: WriteUniqueUnirots\n" +
						"Creates fasta (.fa) file/s of unique mappings of PDB to Uniprot\n" +
						" -s <file>  	  : SIFTS file path \n" +
						" -u <string>     : Uniprot database name \n" +
						" [-o <dir/file>] : If directory, sequences are written to individual FASTA \n"+
						"                   files in the directory.\n" +
						"                   If file all sequences are written to it as a single FASTA\n"+
						" [-g <file>]     : an eppic-cli configuration file with config parameters \n" +
						"                   for uniprot local db connection. If not provided it will \n" +
						"                   be read from ~/.eppic.conf \n";
		
		Getopt g = new Getopt("UploadToDB", args, "s:u:o:g:h?");
		
		String scFilePath = null;
		String uniprotdbName = null;
		File outPath = null;
		File configFile = null;
		
		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 's':
				scFilePath = g.getOptarg();
				break;
			case 'u':
				uniprotdbName = g.getOptarg();
				break;
			case 'o':
				outPath = new File(g.getOptarg());
				break;
			case 'g':
				configFile = new File(g.getOptarg());
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
		
		//Check if the file for sifts connection exits!
		if( scFilePath == null || !(new File(scFilePath)).isFile()) {
			System.err.println("Hey Jim, Can you check if the SIFTS Connection file really exists! " +
					"I am having trouble in reading it.");
			System.err.println(help);
			System.exit(1);
		}
		
		//Check if the uniprotdbName is provided by the user
		if( uniprotdbName == null){
			System.err.println("Hey Jim, Could you please provide me with a unipotDB Name with option -u");
			System.err.println(help);
			System.exit(1);
		}

		if (outPath == null) {
			System.err.println("Output file or dir must be provided with -o");
			System.err.println(help);
			System.exit(1);
		}

		EppicParams params = new EppicParams();
		if (configFile == null) {
			configFile = new File(System.getProperty("user.home"), EppicParams.CONFIG_FILE_NAME);
		}
		params.readConfigFile(configFile);

		boolean singleFastaFile;
		if (outPath.isDirectory()) {
			singleFastaFile = false;
		} else {
			singleFastaFile = true;
		}


		WriteUniqueUniprots wuni = new WriteUniqueUniprots(scFilePath, uniprotdbName,
				params.getLocalUniprotDbHost(), params.getLocalUniprotDbPort(),
				params.getLocalUniprotDbUser(), params.getLocalUniprotDbPwd());
		Map<String, List<Interval>> uniqueMap = wuni.sc.getUniqueMappings();

		int countFishy = 0;
		int countErrLength = 0;

		PrintWriter out = null;

		if (singleFastaFile) {
			out = new PrintWriter(outPath);
		}

		for (String uniprotid : uniqueMap.keySet()) {
			try {
				UnirefEntry uniEntry = wuni.uniLC.getUnirefEntry(uniprotid);
				String uniSeq = uniEntry.getSequence();
				for (Interval interv : uniqueMap.get(uniprotid)) {
					//Create fasta files
					int maxLen = 60;

					if (interv.beg >= interv.end) {
						logger.warn("Inverted or 0-size interval in uniprot mapping for {}_{}-{}", uniprotid, interv.beg, interv.end);
						countFishy++;
						continue;
					}
					if (interv.beg <= 0) {
						logger.warn("Negative starting position in uniprot mapping for {}_{}-{}", uniprotid, interv.beg, interv.end);
						countFishy++;
						continue;
					}
					if (uniSeq.length() >= interv.end) {
						if (!singleFastaFile) {
							File outFile = new File(outPath, uniprotid + "." + interv.beg + "-" + interv.end + ".fa");
							out = new PrintWriter(outFile);
						}
						out.println(">" + uniprotid + "_" + interv.beg + "-" + interv.end);
						String uniSubSeq = uniSeq.substring(interv.beg - 1, interv.end);
						for (int i = 0; i < uniSubSeq.length(); i += maxLen) {
							out.println(uniSubSeq.substring(i, Math.min(i + maxLen, uniSubSeq.length())));
						}
						if (!singleFastaFile) {
							out.close();
						}
					} else {
						logger.warn("Length of query seq ({}) smaller than interval end of uniprot seq for {}_{}-{}", uniSeq.length(), uniprotid, interv.beg, interv.end);
						countErrLength++;
					}

				}
			} catch (NoMatchFoundException er) {
				logger.warn("Could not find {} in db. Skipping");
			}
		}

		if (singleFastaFile) {
			out.close();
		}

		if (countFishy > 0)
			logger.warn("Total encountered fishy mappings (inverted/0-size intervals and negative starting positions): {}", countFishy);

		if (countErrLength > 0)
			logger.warn("Total encountered problems in the length: {}", countErrLength);

	}
}
