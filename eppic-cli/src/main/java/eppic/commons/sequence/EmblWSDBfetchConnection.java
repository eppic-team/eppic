package eppic.commons.sequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Connection class with static methods to download EMBL data with EMBL's DB fetch
 * web service.
 * See http://www.ebi.ac.uk/Tools/webservices/services/dbfetch_rest
 * 
 * Entries are retrieved with URLs of the form:
 * 
 * http://www.ebi.ac.uk/Tools/webservices/rest/dbfetch/{db}/{id}/{format}
 * 
 * 
 * @author duarte_j
 *
 */
public class EmblWSDBfetchConnection {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EmblWSDBfetchConnection.class);

	
	private static final String BASE_URL = "http://www.ebi.ac.uk/Tools/webservices/rest/dbfetch";
	private static final int MAX_ENTRIES_PER_REQUEST = 200; // see the embl ws dbfetch docs (url above) 
	
	private static final Pattern FASTA_HEADER_REGEX = Pattern.compile("^>(.*)$");
	private static final long MAX_CACHE_AGE = 1000*60*60*24*7; // maximum allowable age for the cache file to be read (in milliseconds)
	//private static final double TOLERANCE_MISSING_IDS_FROM_CACHE = 0.05;// the fraction of missing ids tolerated in order to read 
																		// sequences from the cache file
	
	public enum Db {
		EMBLCDS("emblcds"),
		UNIPROTKB("uniprotkb"),
		UNIPARC("uniparc");
		
		private String dbfetchStr;
		private Db(String dbfetchStr) {
			this.dbfetchStr = dbfetchStr;
		}
		public String getDBfetchStr() {
			return dbfetchStr;
		}
	}
	
	public enum Format {
		FASTA("fasta");
		
		private String dbfetchStr;
		private Format(String dbfetchStr) {
			this.dbfetchStr = dbfetchStr;
		}
		public String getDBfetchStr() {
			return dbfetchStr;
		}
	}
	
	private class SequenceCache {
		List<Sequence> list;		// the list of sequences in the cache FASTA file
		HashSet<String> notFoundIds;// the list of identifiers in the comment line of the cache FASTA file (whenever there are missing identifiers in the embl dbfetch they are written to the comment line)
		public SequenceCache(List<Sequence> list, HashSet<String> notFoundIds){
			this.list = list;
			this.notFoundIds = notFoundIds;
		}
	}
	
	/**
	 * Fetches a list of entries from EMBL DB fetch web service of the given db type  
	 * in FASTA format.
	 * If sequences are retrieved online (not read from cacheFile) and cacheFile is not null
	 * then the sequences are written to the cache file.
	 * @param db
	 * @param ids
	 * @param cacheFile a FASTA file containing the sequences to retrieve. If present and if
	 * it contains ALL required sequences then they are read from cacheFile. If null or file
	 * does not exist or file older than {@value #MAX_CACHE_AGE} then the sequences are 
	 * retrieved from EMBL DB fetch
	 * @return
	 * @throws IOException
	 * @throws NoMatchFoundException
	 */
	private static List<Sequence> fetch(Db db, List<String> ids, File cacheFile) throws IOException, NoMatchFoundException {
		List<Sequence> allSeqs = new ArrayList<Sequence>();
		if (cacheFile!=null && cacheFile.exists() && cacheFile.lastModified()>System.currentTimeMillis()-MAX_CACHE_AGE) {
			try {
				SequenceCache seqCache = readSeqs(cacheFile, FASTA_HEADER_REGEX);
				List<String> notFoundIds = getNotFoundIds(db, seqCache.list, ids, seqCache.notFoundIds);
				if (notFoundIds.isEmpty()) {
					LOGGER.info("Using sequences from cache file "+cacheFile);
					return seqCache.list;
				} else {
					allSeqs = new ArrayList<Sequence>();
				}
			} catch (FileFormatException e) {
				allSeqs = new ArrayList<Sequence>();
			}
		}
		if (allSeqs.isEmpty()) { // nothing read from cache
			// we do batches of MAX_ENTRIES_PER_REQUEST as the EMBL web service has a limit of entries per request
			for (int i=0;i<ids.size();i+=MAX_ENTRIES_PER_REQUEST) {
				String commaSepList = "";
				for (int c=i;c<i+MAX_ENTRIES_PER_REQUEST && c<ids.size();c++) {
					if (c!=i) commaSepList+=",";
					commaSepList+=ids.get(c);
				}
				allSeqs.addAll(fetch(db, commaSepList));
			}
			
			// looking up if all our ids were actually returned by embl dbfetch
			List<String> notFoundIds = getNotFoundIds(db, allSeqs, ids, null);
			if (!notFoundIds.isEmpty()) {
				String msg = "Some ids weren't returned by EMBL DB fetch service:";
				for (String id:notFoundIds) {
					msg+=" "+id;
				}
				LOGGER.warn(msg);
			}
			
			// we did fetch the sequences from embl, let's write them out to the cache file
			if (cacheFile!=null) writeSeqs(new PrintStream(cacheFile), allSeqs, notFoundIds);

		}
		return allSeqs;
	}
	
	private static List<String> getNotFoundIds(Db db, List<Sequence> seqs, List<String> ids, HashSet<String> exclude) {
		List<String> notFoundIds = new ArrayList<String>();
		HashSet<String> idsLookup = new HashSet<String>();
		for (Sequence sequence:seqs) {
			// putting ids of the cache file in a lookup table
			if (db.equals(Db.EMBLCDS)) {
				idsLookup.add(sequence.getSecondaryAccession());
			} else if (db.equals(Db.UNIPROTKB)) {
				idsLookup.add(sequence.getPrimaryAccession());
			} else if (db.equals(Db.UNIPARC)) {
				idsLookup.add(sequence.getName().substring(0,sequence.getName().lastIndexOf(" ")));
			} else {
				System.err.println("Error! "+db+" is not a supported format for lookups! Please report the bug.");
				System.exit(1);
			}
		}
		for (String id:ids) {
			if (exclude==null) {
				if (!idsLookup.contains(id)){
					notFoundIds.add(id);
				}
			} else {
				if (!exclude.contains(id) && !idsLookup.contains(id)) {
					notFoundIds.add(id);
				}
			}
		}
		return notFoundIds;
	}
	
	/**
	 * Retrieves the sequence data in FASTA format from EMBL DB fetch web service
	 * given a comma separated list of entry identifiers.
	 * @param db
	 * @param commaSepList
	 * @return
	 * @throws IOException
	 * @throws NoMatchFoundException
	 */
	private static List<Sequence> fetch(Db db, String commaSepList) throws IOException, NoMatchFoundException {
		URL url = new URL(BASE_URL+"/"+db.getDBfetchStr()+"/"+commaSepList+"/"+Format.FASTA.getDBfetchStr());
		URLConnection urlc = url.openConnection();
		InputStream is = urlc.getInputStream();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));    
	    List<Sequence> out = new ArrayList<Sequence>();
		String 	nextLine = null,
		lastSeqTag = null;
		StringBuffer currentSeq = null;
	    while ((nextLine = reader.readLine())!=null) {
		    if (nextLine.startsWith("No entries found")) 
		    	throw new NoMatchFoundException("No "+db.getDBfetchStr()+" match found for ids "+commaSepList);
		    if (nextLine.startsWith("ERROR")) 
		    	throw new IOException("EMBL DB fetch server returned an error.");
			nextLine = nextLine.trim();					    // remove whitespace
			if(nextLine.length() > 0) {						// ignore empty lines
				if (nextLine.startsWith(">")){
					Matcher m = FASTA_HEADER_REGEX.matcher(nextLine);
					if (lastSeqTag!=null) {
						out.add(new Sequence(lastSeqTag,currentSeq.toString()));
					}
					currentSeq = new StringBuffer();
					if (m.matches()) {
						lastSeqTag=m.group(1);
					}
				} else {
					currentSeq.append(nextLine);
				}
			}

	    }
	    is.close();
	    reader.close();
	    // adding last sequence (missed by loop above)
	    out.add(new Sequence(lastSeqTag,currentSeq.toString()));
	    
	    return out;		
	}
	
	/**
	 * Fetches EMBLCDS entries from EMBL DB fetch web service in FASTA format
	 * If sequences are retrieved online (not read from cacheFile) and cacheFile is not null
	 * then the sequences are written to the cache file. 
	 * @param emblcdsIds
	 * @param cacheFile a FASTA file containing the sequences to retrieve. If present and if
	 * it contains ALL required sequences then they are read from cacheFile. If null or file
	 * does not exist or file older than {@value #MAX_CACHE_AGE} then the sequences are 
	 * retrieved from EMBL DB fetch
	 * @return
	 * @throws IOException
	 * @throws NoMatchFoundException
	 */
	public static List<Sequence> fetchEMBLCDS(List<String> emblcdsIds, File cacheFile) throws IOException, NoMatchFoundException {
		return fetch(Db.EMBLCDS, emblcdsIds, cacheFile);
	}
	
	/**
	 * Fetches UNIPROT entries from EMBL DB fetch web service in FASTA format
	 * If sequences are retrieved online (not read from cacheFile) and cacheFile is not null
	 * then the sequences are written to the cache file. 
	 * @param uniprotIds
	 * @param cacheFile a FASTA file containing the sequences to retrieve. If present and if
	 * it contains ALL required sequences then they are read from cacheFile. If null or file
	 * does not exist or file older than {@value #MAX_CACHE_AGE} then the sequences are 
	 * retrieved from EMBL DB fetch
	 * @return
	 * @throws IOException
	 * @throws NoMatchFoundException
	 */
	public static List<Sequence> fetchUniprot(List<String> uniprotIds, File cacheFile) throws IOException, NoMatchFoundException {
		return fetch(Db.UNIPROTKB, uniprotIds, cacheFile);
	}
	
	/**
	 * Fetches UNIPARC entries from EMBL DB fetch web service in FASTA format
	 * If sequences are retrieved online (not read from cacheFile) and cacheFile is not null
	 * then the sequences are written to the cache file.  
	 * @param uniparcIds
	 * @param cacheFile a FASTA file containing the sequences to retrieve. If present and if
	 * it contains ALL required sequences then they are read from cacheFile. If null or file
	 * does not exist or file older than {@value #MAX_CACHE_AGE} then the sequences are 
	 * retrieved from EMBL DB fetch
	 * @return
	 * @throws IOException
	 * @throws NoMatchFoundException
	 */
	public static List<Sequence> fetchUniparc(List<String> uniparcIds, File cacheFile) throws IOException, NoMatchFoundException {
		return fetch(Db.UNIPARC, uniparcIds, cacheFile);
	}
	
	/**
	 * Write given list of Sequence objects to given PrintStream in FASTA format
	 * Output line length is fixed at 80.
	 * An additional comment line (starting with #) with missing ids is written
	 * @param ps
	 * @param sequences
	 * @param notFoundIds the list of ids not found by embl dbfetch to be stored
	 * in the comment line
	 */
	private static void writeSeqs(PrintStream ps, List<Sequence> sequences, List<String> notFoundIds) {
		int len = 80;
		if (!notFoundIds.isEmpty()) {
			ps.print("#");
			for (String id:notFoundIds){
				ps.print(id+" ");
			}
			ps.println();
		}
		for (Sequence sequence:sequences) { 
			ps.println(">"+sequence.getName());
			for(int i=0; i<sequence.getSeq().length(); i+=len) {
				ps.println(sequence.getSeq().substring(i, Math.min(i+len,sequence.getSeq().length())));
			}		
		}		
	}
	
	/**
	 * Reads a fasta file containing multiple sequences returning a 
	 * list of Sequence objects and a set of missing identifiers read from a comment 
	 * line (starting with #)
	 * @param seqsFile
	 * @param fastaHeaderRegex a regex that specifies as its first capture group what
	 * will be read from the FASTA header as a sequence tag.
	 * @return
	 * @throws IOException
	 */
	private static SequenceCache readSeqs(File seqsFile, Pattern fastaHeaderRegex) throws IOException, FileFormatException {
		List<Sequence> list = new ArrayList<Sequence>();
		HashSet<String> notFoundIds = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(seqsFile));
		String line;
		String lastTag = null;
		String currentTag = null;
		StringBuffer seq = null;
		while ((line=br.readLine())!=null){
			if (line.isEmpty()) continue;
			if (line.startsWith("#")) {
				String[] tokens = line.substring(1,line.length()).split(" ");
				for (String token:tokens) {
					notFoundIds.add(token);
				}
				continue;
			}
			if (line.startsWith(">")) {
				Matcher m = fastaHeaderRegex.matcher(line);
				if (m.find()) {
					currentTag = m.group(1);
					if (lastTag!=null) {
						list.add(new Sequence(lastTag, seq.toString()));
					}
					seq = new StringBuffer();
					lastTag = currentTag;
				} else {
					br.close();
					throw new FileFormatException("FASTA file "+seqsFile+" does not seem to have proper FASTA headers");
				}
			} else {
				seq.append(line.trim());
			}
		}
		br.close();
		if (lastTag!=null) { // this happens if the file is empty
			list.add(new Sequence(lastTag, seq.toString())); // adding the last sequence
		}
		return new EmblWSDBfetchConnection().new SequenceCache(list, notFoundIds);
	}

	// testing
	public static void main(String[] args) throws Exception {
		System.out.println("Fetching EMBLCDS:");
		File cacheFile = new File("/tmp/emblcds.cache.fa");
		List<String> emblcdsids = new ArrayList<String>();
		emblcdsids.add("CAA84586");
		emblcdsids.add("ACB36185");
		emblcdsids.add("EDY54793"); // can't be fetched by EMBL fetch
		List<Sequence> emblcdsSeqs = fetchEMBLCDS(emblcdsids, cacheFile);
		for (Sequence seq:emblcdsSeqs) {
			seq.writeToPrintStream(System.out);
		}
		System.out.println();
		System.out.println("Fetching a Uniprot:");
		cacheFile = new File("/tmp/uniprots.cache.fa");
		List<String> uniIds = new ArrayList<String>();
		uniIds.add("P12830");
		List<Sequence> uniSeqs = fetchUniprot(uniIds,cacheFile);
		for (Sequence seq:uniSeqs) {
			seq.writeToPrintStream(System.out);
		}

	}
	
}
