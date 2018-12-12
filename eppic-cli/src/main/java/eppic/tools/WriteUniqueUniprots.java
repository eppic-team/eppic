package eppic.tools;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eppic.commons.sequence.*;
import eppic.commons.util.Interval;
import gnu.getopt.Getopt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.uniprot.dataservice.client.exception.ServiceException;


/**
 *
 * Script to write out all unique uniprot sequence segments present in the SIFTS file into FASTA file/s.
 * The sequences are pulled from UniProt JAPI remotely (since 3.2.0, before it was from local UniProt db).
 *
 * @author Nikhil Biyani
 * @author Jose Duarte
 */
public class WriteUniqueUniprots {

	private static final Logger logger = LoggerFactory.getLogger(WriteUniqueUniprots.class);
	
	private SiftsConnection sc;
	private UniProtConnection uc;

	public WriteUniqueUniprots(String scPath) throws IOException {
		sc = new SiftsConnection(scPath);
		uc = new UniProtConnection();
	}

	public static void main(String[] args) throws IOException {
		
		String help =
				"Usage: WriteUniqueUnirots\n" +
						"Creates fasta (.fa) file/s of unique mappings of PDB to Uniprot\n" +
						" -s <file>  	  : SIFTS file path \n" +
						" [-f <file>]     : a file with a list of PDB ids. Only sequences corresponding\n" +
						"                   to these PDB ids will be written out. If not provided, then\n" +
						"                   all sequences in SIFTS files are written out\n" +
						" -o <dir/file>   : If directory, sequences are written to individual FASTA \n"+
						"                   files in the directory.\n" +
						"                   If file all sequences are written to it as a single FASTA\n";

		Getopt g = new Getopt("UploadToDB", args, "s:f:o:h?");
		
		String scFilePath = null;
		File outPath = null;
		File listFile = null;

		int c;
		while ((c = g.getopt()) != -1) {
			switch(c){
			case 's':
				scFilePath = g.getOptarg();
				break;
			case 'f':
				listFile = new File(g.getOptarg());
				break;
			case 'o':
				outPath = new File(g.getOptarg());
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
			System.err.println("Hey Jim, Can you check if the SIFTS file really exists! " +
					"I am having trouble in reading it.");
			System.exit(1);
		}

		if (outPath == null) {
			System.err.println("Output file or dir must be provided with -o");
			System.err.println(help);
			System.exit(1);
		}

		boolean singleFastaFile;
		if (outPath.isDirectory()) {
			singleFastaFile = false;
		} else {
			singleFastaFile = true;
		}


		WriteUniqueUniprots wuni = new WriteUniqueUniprots(scFilePath);
		Set<String> pdbIds = null;
		if (listFile!=null) {
			pdbIds = readListFile(listFile);
		}
		Map<String, List<Interval>> uniqueMap = wuni.sc.getUniqueMappings(pdbIds);

		int countFishy = 0;
		int countErrLength = 0;

		int countNotFound = 0;
		int countCantRetrieve = 0;

		PrintWriter out = null;

		if (singleFastaFile) {
			out = new PrintWriter(outPath);
		}

		int countMappings = 0;
		for (List<Interval> list : uniqueMap.values()) {
			countMappings += list.size();
		}

		logger.info("Total of {} unique UniProt ids and {} unique segment mappings.",
				uniqueMap.size(), countMappings);

		if (pdbIds!=null) {
			logger.info("Mappings correspond to {} PDB ids", pdbIds.size());
		} else {
			logger.info("Mappings correspond to ALL current PDB ids as present in SIFTS file {}", scFilePath);
		}

		for (String uniprotid : uniqueMap.keySet()) {

			try {
				UnirefEntry uniEntry = wuni.uc.getUnirefEntry(uniprotid);
				String uniSeq = uniEntry.getSequence();
				for (Interval interv : uniqueMap.get(uniprotid)) {
					//Create fasta files
					int maxLen = 60;

					if (interv.beg >= interv.end) {
						logger.warn("Inverted or 0-size interval in uniprot mapping {}_{}-{}", uniprotid, interv.beg, interv.end);
						countFishy++;
						continue;
					}
					if (interv.beg <= 0) {
						logger.warn("Negative starting position in uniprot mapping {}_{}-{}", uniprotid, interv.beg, interv.end);
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
				logger.warn("Could not find {} from JAPI. Skipping", uniprotid);
				countNotFound++;
			} catch (ServiceException e) {
				logger.warn("ServiceException while retrieving UniProt {} from JAPI. Error: {}", uniprotid, e.getMessage());
				countCantRetrieve++;
			}
		}

		if (singleFastaFile) {
			out.close();
		}

		if (countFishy > 0)
			logger.warn("Total encountered fishy mappings (inverted/0-size intervals and negative starting positions): {}", countFishy);

		if (countErrLength > 0)
			logger.warn("Total encountered problems in the length: {}", countErrLength);

		if (countNotFound > 0) {
			logger.warn("Could not find {} ids via JAPI", countNotFound);
		}

		if (countCantRetrieve > 0) {
			logger.warn("Could not retrieve {} ids via JAPI", countNotFound);
		}

		if ( (countNotFound + countCantRetrieve) > 0.20 * uniqueMap.size()) {
			logger.error("More than 20% of ids could not be found or retrieved. Dumping sequences was unsuccessful.");
			System.exit(1);
		}

		logger.info("Finished dumping sequences successfully");
	}

	private static Set<String> readListFile(File listFile) throws IOException {
		Set<String> set = new HashSet<>();
		try (BufferedReader br = new BufferedReader(new FileReader(listFile))) {
			String line;
			while ((line = br.readLine())!=null) {
				if (line.trim().isEmpty()) continue;
				if (line.startsWith("#")) continue;

				String pdbId = line.trim();
				if (pdbId.length()!=4) {
					logger.warn("Found string of length !=4 in file {}: {}. Skipping", listFile.toString(), pdbId);
					continue;
				}
				set.add(pdbId);
			}
		}
		return set;
	}
}
