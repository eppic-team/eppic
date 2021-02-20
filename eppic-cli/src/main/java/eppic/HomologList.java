package eppic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eppic.commons.blast.*;
import eppic.db.dao.DaoException;
import eppic.db.dao.UniProtInfoDAO;
import eppic.db.dao.mongo.UniProtInfoDAOMongo;
import eppic.model.dto.UniProtInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import eppic.commons.sequence.AAAlphabet;
import eppic.commons.sequence.AlignmentConstructionException;
import eppic.commons.sequence.ClustaloRunner;
import eppic.commons.sequence.FileFormatException;
import eppic.commons.sequence.MultipleSequenceAlignment;
import eppic.commons.sequence.NoMatchFoundException;
import eppic.commons.sequence.Sequence;
import eppic.commons.sequence.UniProtConnection;
import eppic.commons.sequence.UnirefEntry;
import eppic.commons.util.Interval;
import uk.ac.ebi.kraken.interfaces.uniparc.UniParcEntry;
import uk.ac.ebi.uniprot.dataservice.client.exception.ServiceException;

/**
 * Class to store a set of homologs of a given sequence.
 * It contains methods to blast against a uniprot/uniref database to get the homolog
 * list and to retrieve data from Uniprot (taxonomy, dbrefs), embl cds sequences, 
 * Uniparc sequences...
 * 
 * @see Homolog
 * 
 * @author Jose Duarte
 *
 */
public class HomologList implements  Serializable {

	private static final long serialVersionUID = 1L;

	/*------------------------ constants --------------------------*/
	
	private static final String 	BLASTOUT_SUFFIX = "blast.out.xml";
	private static final String 	FASTA_SUFFIX = ".fa";
	private static final String 	BLAST_BASENAME = "homSearch";
	private static final String     CLUSTERING_BASENAME = "homClustering";

	private static final boolean 	BLAST_NO_FILTERING = true;
	private static final String 	UNIPROT_VER_FILE = "reldate.txt";
	
	public static final int         CLUSTERING_ID = 90;
	private static final double     CLUSTERING_COVERAGE = 0.99;
	
	
	private static final boolean 	DEBUG = false;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HomologList.class);
	
	/*-------------------------- members --------------------------*/
	
	private UnirefEntry ref;						// the uniref entry (uniprot/uniparc) to which the homologs refer
	private Interval refInterval;
	private boolean isSubInterval;
	private List<Homolog> list; 					// the list of homologs
	private List<Homolog> subList;			 		// the filtered list of homologs after calling filterToMinIdAndCoverage
	
	private double idCutoff; 						// the identity cutoff (see filterToMinIdAndCoverage() )
	private double qCoverageCutoff;					// the query coverage cutoff (see filterToMinIdAndCoverage() )
	private String uniprotVer;						// the version of uniprot used in blasting, read from the reldate.txt uniprot file

	private MultipleSequenceAlignment aln;	  		// the protein sequences alignment

	private AAAlphabet alphabet;					// the reduced alphabet used to calculate entropies
	private List<Double> entropies;					// entropies for each uniprot reference sequence position
	
	private boolean useUniparc;
	
	
	public HomologList(UnirefEntry ref) {
		this(ref,null);
	}
	
	/**
	 * Create a new UniprotHomologList
	 * @param ref the uniprot entry whose sequence is the reference for this homolog list
	 * @param interv the interval in the uniprot sequence that we actually use (with 
	 * numbering of uniprot seq from 1 to length-1), if null the whole sequence is use 
	 */
	public HomologList(UnirefEntry ref, Interval interv) {
		this.ref = ref;
		if (interv!=null) {
			this.refInterval = interv;
		} else {
			this.refInterval = new Interval(1,ref.getLength());
		}
		if (refInterval.beg==1 && refInterval.end==ref.getLength()) {
			isSubInterval = false;
		} else {
			isSubInterval = true;
		}
		
		this.idCutoff = 0.0; // i.e. no filter

	}
	
	public List<Homolog> getFilteredSubset() {
		return subList;
	}
	
	/**
	 * Performs a blast search based on the reference UniprotEntry to populate this list of homologs.
	 * All blast output files will be removed on exit.
	 * If a gzipped blast xml cacheFile is passed and it exists, blast is not run but instead results 
	 * are read from cache file. If cacheFile passed but does not exist the blast xml file will be 
	 * gzipped to cacheFile
	 * @param blastPlusBlastp
	 * @param blastDbDir
	 * @param blastDb
	 * @param blastNumThreads
	 * @param cachedHitList a BlastHitList coming from a cache of precomputed sequence search run
	 * @param noBlast whether the "no blast" mode is enabled: if true no blast is performed at all, if false blast
	 * is attempted if needed
	 * @throws IOException
	 * @throws BlastException
	 * @throws InterruptedException
	 */
	public void searchWithBlast(File blastPlusBlastp, String blastDbDir, String blastDb, int blastNumThreads, int maxNumSeqs, BlastHitList cachedHitList, boolean noBlast)
			throws IOException, BlastException, InterruptedException {

		BlastHitList blastList = null;
		
		this.uniprotVer = readUniprotVer(blastDbDir);

		if (cachedHitList!=null && cachedHitList.size()>0) {

			LOGGER.info("Using sequence search results from cache.");

			blastList = cachedHitList;

			// if we do take the cache, we have to do some sanity checks
			String blastqueryid = blastList.getQueryId();
			blastqueryid = blastqueryid.replaceAll("_.*", "");
			if (!blastqueryid.equals(this.ref.getUniId())) {
				throw new IOException("Query id " + blastqueryid + " from cache " +
						" does not match the id from the sequence: " + this.ref.getUniId());
			}

		} else if (noBlast) {
				throw new IOException("The \"no blast\" mode was specified (-B option) and entry does not exist in sequence search cache ");

		} else {

			// perform the sequence search
			// TODO rewrite by running mmseqs or calling the mmseqs search microservice

			File outBlast = File.createTempFile(BLAST_BASENAME,BLASTOUT_SUFFIX);
			File inputSeqFile = File.createTempFile(BLAST_BASENAME,FASTA_SUFFIX);
			// NOTE: we blast the reference uniprot sequence using only the interval specified
			this.ref.getSeq().getInterval(this.refInterval).writeToFastaFile(inputSeqFile);
			
			BlastRunner blastRunner = new BlastRunner(blastDbDir);
			blastRunner.runBlastp(blastPlusBlastp, inputSeqFile, blastDb, outBlast, BlastRunner.BLASTPLUS_XML_OUTPUT_TYPE, BLAST_NO_FILTERING, blastNumThreads, maxNumSeqs);

			if (!DEBUG) {
				// note that if blast throws an exception, the files won't be deleted on exit, good for debugging a crash
				outBlast.deleteOnExit();
				inputSeqFile.deleteOnExit();
			}

			LOGGER.info("Run blast: "+blastRunner.getLastBlastCommand());
			
			LOGGER.info("Blasted against "+blastDbDir+"/"+blastDb);

			try {
				// note that by setting second parameter to true we are ignoring the DTD url to avoid unnecessary network connections
				BlastXMLParser blastParser = new BlastXMLParser(outBlast, true);
				blastList = blastParser.getHits();
			} catch (SAXException e) {
				// if this happens it means that blast doesn't format correctly its XML, i.e. has a bug
				LOGGER.error("Unexpected error: "+e.getMessage());
				System.exit(1);
			}
		}

		if (blastList.size()==0) {
			LOGGER.warn("The size of the sequence search hit list is 0 for {}.", this.ref.getUniId());
		}

		this.list = new ArrayList<>();
		for (BlastHit hit:blastList) {
			for (BlastHsp hsp:hit) {
				String sid = hit.getSubjectId();
				Matcher m = Sequence.DEFLINE_PRIM_ACCESSION_REGEX.matcher(sid);
				if (m.matches()) {
					String uniId = m.group(1);
					UnirefEntry uniref = new UnirefEntry();
					uniref.setUniprotId(uniId);
					list.add(new Homolog(hsp,uniref));
				} else {
					Matcher m2 = Sequence.DEFLINE_PRIM_ACCESSION_UNIREF_REGEX.matcher(sid);
					if (m2.matches()) {					
						String uniId = m2.group(1);
						if (uniId.startsWith("UPI")){
							if (useUniparc) {
								UnirefEntry uniref = new UnirefEntry();
								uniref.setUniparcId(uniId);
								list.add(new Homolog(hsp,uniref));
							} else {
								LOGGER.info("Ignoring blast hit "+uniId+" because it is a UniParc id.");
							}
						}
						else if (uniId.contains("-")) {
							LOGGER.info("Ignoring blast hit "+uniId+" because it is a UniProt isoform id.");
						}
						else {	
							UnirefEntry uniref = new UnirefEntry();
							uniref.setUniprotId(uniId);
							list.add(new Homolog(hsp,uniref));
						}
					} else {
						LOGGER.error("Could not find UniProt id in subject id "+sid);
					}
				}
			}
		}
		this.subList = list; // initially the subList is the same as the list until filterToMinIdAndCoverage is called
	}
	
	public static String readUniprotVer(String blastDbDir) {
		String ver = null;
		File uniprotVerFile = new File(blastDbDir,UNIPROT_VER_FILE);
		try (
			BufferedReader br = new BufferedReader(new FileReader(uniprotVerFile));) {
			String line;
			Pattern p = Pattern.compile("^UniProt.*Release\\s([\\d._]+).*");
			while ((line=br.readLine())!=null){
				Matcher m = p.matcher(line);
				if (m.matches()) {
					ver = m.group(1);
					break;
				}
			}
			if (ver == null) {
				LOGGER.warn("Couldn't find a UniProt version in file {}", uniprotVerFile);
				ver = "unknown";
			}
		} catch(IOException e) {
			LOGGER.warn("Couldn't read UniProt release file {} to find UniProt version", uniprotVerFile);
			ver = "unknown";
		}
		return ver;
	}
	
	/**
	 * Retrieves from UniprotKB the sequence and taxonomy info,
	 * by using the remote UniProt API, for both UniProt entries and UniParc entries.
	 * @param uniprotConn
	 * @throws IOException
	 * @throws ServiceException 
	 */
	public void retrieveUniprotKBData(UniProtConnection uniprotConn) throws IOException, ServiceException {
		String japiVer = null;
		try {
			japiVer = uniprotConn.getVersion();
		} catch (ServiceException e) {
			LOGGER.warn("Could not get UniProt release version from UniProt JAPI. Will not check if version used for blast coincides with version queried through JAPI. Error: "+e.getMessage());
		}

		if (japiVer!=null && !japiVer.equals(this.uniprotVer)){
			LOGGER.warn("UniProt version used for blast ("+uniprotVer+") and UniProt version being queried with JAPI ("+japiVer+") don't match!");
		}
		
		List<String> uniprotIds = new ArrayList<>();
		
		for (Homolog hom:subList) {
			
			if (hom.isUniprot()) 
				uniprotIds.add(hom.getUniId());
			
		}
		
		List<UnirefEntry> unirefs = uniprotConn.getMultipleUnirefEntries(uniprotIds);
		
		HashMap<String,UnirefEntry> unirefsmap = new HashMap<>();
		for (UnirefEntry uniref:unirefs) {
			unirefsmap.put(uniref.getUniId(), uniref);
		}

		Iterator<Homolog> it = subList.iterator();
		while (it.hasNext()) {
			Homolog hom = it.next();
			if (unirefsmap.containsKey(hom.getUniId())) {
				UnirefEntry uniref = unirefsmap.get(hom.getUniId());
				hom.getUnirefEntry().setUniprotId(uniref.getUniprotId());
				hom.getUnirefEntry().setNcbiTaxId(uniref.getNcbiTaxId());
				hom.getUnirefEntry().setSequence(uniref.getSequence());
				hom.getUnirefEntry().setTaxons(uniref.getTaxons());
			} else {
				if (hom.isUniprot()) {				
					LOGGER.info("Removing UniProt id "+hom.getUniId()+" from homologs because it wasn't returned by the UniProt connection.");
					it.remove();
					removeFromMainList(hom);
				}
			}
		}
		
		// now retrieve uniparc ids
		
		it = subList.iterator();
		while (it.hasNext()) {
			Homolog hom = it.next();
			if (hom.isUniprot()) continue;			
			// ok this is a uniparc, let's get it from japi
			
			try {
				UniParcEntry uniparcEntry = uniprotConn.getUniparcEntry(hom.getUniId());
				String seq = uniparcEntry.getSequence().getValue();
				hom.getUnirefEntry().setSequence(seq);
				
			} catch (NoMatchFoundException e) {
				LOGGER.warn("Could not find UniParc id {} through UniProt JAPI. Will not use this homolog.", hom.getUniId());
				it.remove();
				removeFromMainList(hom);

			} catch (ServiceException e) {
				LOGGER.warn("Problems retrieving UniParc id {} through UniProt JAPI. Will not use this homolog. Error: {}", hom.getUniId(), e.getMessage());
				it.remove();
				removeFromMainList(hom);

			}
		}
		
		checkRetrievedLengthsVsBlastIntervals();
	}
	
	/**
	 * Retrieves both uniprot and uniparc data from local db
	 */
	public void retrieveUniprotKBData() throws DaoException {

		UniProtInfoDAO uniProtInfoDAO = new UniProtInfoDAOMongo(MongoSettingsStore.getMongoSettings().getMongoDatabase());

		List<String> uniIds = new ArrayList<>();
		for (Homolog hom:subList) {
			uniIds.add(hom.getUniId());
		}

		List<UnirefEntry> unirefs = new ArrayList<>();
		for (String uniId : uniIds) {
			UniProtInfo uniProtInfo = uniProtInfoDAO.getUniProtInfo(uniId);
			if (uniProtInfo!=null) {
				UnirefEntry entry = new UnirefEntry();
				entry.setUniprotId(uniId);
				entry.setId(uniId); // TODO check if this is needed
				entry.setSequence(uniProtInfo.getSequence());
				List<String> taxons = new ArrayList<>();
				taxons.add(uniProtInfo.getFirstTaxon());
				taxons.add(uniProtInfo.getLastTaxon());
				entry.setTaxons(taxons);
				unirefs.add(entry);
			} else {
				LOGGER.warn("No UniProt info found in db for {}", uniId);
			}
		}

		HashMap<String,UnirefEntry> unirefsmap = new HashMap<>();
		for (UnirefEntry uniref:unirefs) {
			unirefsmap.put(uniref.getUniId(), uniref);
		}
		
		Iterator<Homolog> it = subList.iterator();
		while (it.hasNext()) {
			Homolog hom = it.next();
			if (unirefsmap.containsKey(hom.getUniId())) {
				UnirefEntry uniref = unirefsmap.get(hom.getUniId());
				hom.getUnirefEntry().setId(uniref.getId());
				hom.getUnirefEntry().setUniprotId(uniref.getUniprotId());
				hom.getUnirefEntry().setUniparcId(uniref.getUniparcId());
				hom.getUnirefEntry().setNcbiTaxId(uniref.getNcbiTaxId());
				hom.getUnirefEntry().setSequence(uniref.getSequence());
				hom.getUnirefEntry().setTaxons(uniref.getTaxons());				
			} else { // the query might have not found some uniprot ids
				LOGGER.info("Removing UniProt/UniParc id "+hom.getUniId()+" from homologs because it wasn't returned by the UniProt connection.");
				it.remove();
				removeFromMainList(hom);

			}
		}
		
		checkRetrievedLengthsVsBlastIntervals();
	}
	
	/**
	 * Removes a homolog from the main list.
	 * Note this will remove based on the memory reference, because {@link Homolog} does not have equals implemented. 
	 * If we implement equals in {@link Homolog} this will most likely break!
	 * @param hom
	 */
	private void removeFromMainList(Homolog hom) {
		boolean gotit = list.remove(hom);
		if (!gotit) {
			LOGGER.warn("Could not find homlog {} in HomologList, can't remove it! ", hom.getIdentifier());
		}
	}
	
	/**
	 * Checks for sequence intervals read from blast to be within the bounds of the actual sequence length retrieved from 
	 * the UniProt connection (local or JAPI). If they aren't the entry is removed from the homolog list and a warning printed.
	 * This can happen for instance if the UniProt version in the blast database is not the same than the version where sequences 
	 * are retrieved from. 
	 */
	private void checkRetrievedLengthsVsBlastIntervals() {
		
		Iterator<Homolog> it = subList.iterator(); 
		while (it.hasNext()) {
			Homolog hom = it.next();
			Interval interv = new Interval(hom.getBlastHsp().getSubjectStart(),
					 hom.getBlastHsp().getSubjectEnd());
			Sequence seq = hom.getUnirefEntry().getSeq();
			
			try {
				// this subsets the sequence given the interval, if interval off bounds it produces the exception
				seq.getInterval(interv);
				
			} catch (StringIndexOutOfBoundsException e) {
				
				LOGGER.warn("The interval found from blast is off the bounds of the sequence retrieved from UniProt for entry {}. Mismatch of UniProt versions perhaps? Removing entry from list", hom.getIdentifier());
				it.remove();
				removeFromMainList(hom);
				
			}
		}

	}
	
	/**
	 * Write to the given file the query protein sequence and all the homolog protein 
	 * sequences in FASTA format. Only the subset of entries result of filtering with {@link #filterToMinIdAndCoverage(double, double)}
	 * will be used. If no filtered applied yet then all entries are used.
	 * @param outFile
	 * @param writeQuery if true the query sequence is written as well, if false only 
	 * homologs 
	 * @throws FileNotFoundException
	 */
	public void writeToFasta(File outFile, boolean writeQuery) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(outFile);
		
		int len = 80;

		if (writeQuery) {
			pw.print(MultipleSequenceAlignment.FASTAHEADER_CHAR + this.ref.getUniId());
			if (isSubInterval) {
				pw.print(" "+refInterval.beg+"-"+refInterval.end);
			}
			pw.println();
			Sequence refSequence = ref.getSeq().getInterval(refInterval);
			for(int i=0; i<refSequence.getLength(); i+=len) {
				pw.println(refSequence.getSeq().substring(i, Math.min(i+len,refSequence.getLength())));
			}
		}
		
		for(Homolog hom:subList) {
			
			String sequence = 
					hom.getUnirefEntry().getSeq().
						getInterval(new Interval(hom.getBlastHsp().getSubjectStart(),
												 hom.getBlastHsp().getSubjectEnd())).getSeq();
			
			pw.print(MultipleSequenceAlignment.FASTAHEADER_CHAR + hom.getIdentifier());
			 
			pw.println();
			
			for(int i=0; i<sequence.length(); i+=len) {
				pw.println(sequence.substring(i, Math.min(i+len,sequence.length())));
			}
		}
		pw.println();
		pw.close();
	}
	
	/**
	 * Runs external program to align all protein sequences of homologs and the query sequence
	 * returning a MultipleSequenceAlignment object
	 * Two external programs are supported: t_coffee or clustalo. Only one of the two can be passed, 
	 * the other one must be null, if both are null or both are set then an IllegalArgumentException is thrown
	 * @param clustaloBin
	 * @param nThreads number of CPU cores external program should use
	 * @param alnCacheFile
	 * @throws IOException 
	 */
	public void computeAlignment(File clustaloBin, int nThreads, File alnCacheFile) 
			throws IOException, InterruptedException {
				
		// we have to catch the special case when there are no homologs at all, we then will set an "alignment" that contains just the query sequence
		if (getSizeFilteredSubset()==0) {

			String[] tags = { this.ref.getUniId() };
			String[] seqs = { ref.getSeq().getInterval(refInterval).getSeq() };
			try {
				this.aln = new MultipleSequenceAlignment(tags,seqs);
			} catch (AlignmentConstructionException e) {
				throw new IOException(e);
			}
			LOGGER.info("No homologs to align: no need to compute alignment");
			return;
		}
		
		File alnFile = null;
		boolean alnFromCache = false;
		
		if (alnCacheFile!=null && alnCacheFile.exists()) {
			String uniprotVerFromCacheDir = readUniprotVer(alnCacheFile.getParent());
			String uniprotVerFromBlast = this.uniprotVer; // this can be either actually from blast db dir (if blast was run) or read from blast cache dir
			if (!uniprotVerFromBlast.equals(uniprotVerFromCacheDir)) {
				LOGGER.warn("Uniprot version used for blast "+
						" ("+uniprotVerFromBlast+") does not match version in alignment cache dir "+
						alnCacheFile.getParent()+" ("+uniprotVerFromCacheDir+")");
			}
			
			alnFile = alnCacheFile;
			
			
			LOGGER.warn("Reading alignment from cache file " + alnCacheFile);
			try {
				this.aln = new MultipleSequenceAlignment(alnFile.getAbsolutePath(), MultipleSequenceAlignment.FASTAFORMAT);
				
				if (!checkAlnFromCache(true)) { 
					LOGGER.warn("Alignment from cache file does not coindice with computed filtered list of homologs");
					LOGGER.info("Will compute alignment");
					alnFile = runAlignmentProgram(clustaloBin, nThreads);

				} else {
					alnFromCache = true;
					LOGGER.info("Alignment from cache file coincides with computed filtered list of homologs. Won't recompute alignment");
				}
				
				
			} catch (FileFormatException|AlignmentConstructionException e) {
				throw new IOException(e);
			}
			
			
		} else {
			// no cache: we compute with external program
			alnFile = runAlignmentProgram(clustaloBin, nThreads); 			
			
		}
		
		if (!alnFromCache) { 
			// this can happen if 
			// a) no cache file given, 
			// b) cache file given but doesn't exist, 
			// c) existing cache file given but with wrong content 
			try {
				this.aln = new MultipleSequenceAlignment(alnFile.getAbsolutePath(), MultipleSequenceAlignment.FASTAFORMAT);
			} catch (FileFormatException|AlignmentConstructionException e) {
				throw new IOException(e);
			}
		}
		

		// if we did pass a cache file but it doesn't exist yet, we have computed the alignment. Now we need to write it to the given cache file
		if (alnCacheFile!=null && !alnCacheFile.exists()) { 
			try {
				writeAlignmentToFile(alnCacheFile);
				LOGGER.info("Writing alignment cache file "+alnCacheFile);
			} catch(FileNotFoundException e) {
				LOGGER.warn("Couldn't write alignment cache file "+alnCacheFile+". Error: "+e.getMessage());
			}
		}

	}
	
	private File runAlignmentProgram(File clustaloBin, int nThreads) 
			throws InterruptedException, IOException {
		
		return runClustalo(clustaloBin, nThreads);
		
	}
	
	private File runClustalo(File clustaloBin, int nThreads) 
			throws InterruptedException, IOException {
		
		File alnFile = File.createTempFile("homologs.",".aln");
		File homologSeqsFile = File.createTempFile("homologs.", ".fa");

		this.writeToFasta(homologSeqsFile, true);
		
		ClustaloRunner cor = new ClustaloRunner(clustaloBin);
		cor.buildCmdLine(homologSeqsFile, alnFile, nThreads);
		LOGGER.info("Running clustalo command: " + cor.getCmdLine());
		long start = System.nanoTime();		
		cor.runClustalo();		
		long end = System.nanoTime();
		LOGGER.info("clustalo ran in "+((end-start)/1000000000L)+"s ("+nThreads+" threads)");
		if (!DEBUG) { 
			// note that if the run of tcoffee throws an exception, files are not marked for deletion
			homologSeqsFile.deleteOnExit();
			alnFile.deleteOnExit();
		}
		return alnFile;
	}
	
	private boolean checkAlnFromCache(boolean checkQuery) {
		
		int size = this.subList.size();
		if (checkQuery) size+=1;
		
		// first we check the size
		if (this.aln.getNumberOfSequences()!=size) {
			LOGGER.info("Number of sequences in alignment from cache file is "+this.aln.getNumberOfSequences()+" but the size of the computed filtered list of homologs is "+size);
			return false;
		}
		
		// then the query tag is present
		if (checkQuery) {
			if (!this.aln.hasTag(this.ref.getUniId())) {
				LOGGER.info("Query tag "+this.ref.getUniId()+" not present in alignment cache file");
				return false;
			}
		}		
		
		// and finally that all other tags are present
		for (Homolog hom:subList){
			
			String tag = hom.getIdentifier();
			
			if (!this.aln.hasTag(tag)) {
				LOGGER.info("Tag "+tag+" not present in alignment cached file");
				return false;
			}

		}
		
		return true;
	}
	
	/**
	 * Returns the protein sequence alignment of query sequence and all homologs
	 * @return
	 */
	public MultipleSequenceAlignment getAlignment() {
		return aln;
	}
	
	public void writeAlignmentToFile(File alnFile) throws FileNotFoundException {
		aln.writeFasta(new PrintStream(alnFile), 80, true);
	}
 
	/**
	 * Creates a subset list of Homologs that have at least the required identities
	 * and query coverage.
	 * Subsequently all methods in this class will refer to the sublist rather than to original unfiltered list.
	 * Upon a subsequent call to this method a different id/coverage can be chosen and other methods used with those.
	 * @param idCutoff
	 * @param queryCovCutoff
	 */
	public void filterToMinIdAndCoverage(double idCutoff, double queryCovCutoff) {
		this.idCutoff = idCutoff;
		this.qCoverageCutoff = queryCovCutoff;

		this.subList = new ArrayList<Homolog>();

		for (Homolog hom:list) {
			if ((hom.getPercentIdentity()/100.0)>idCutoff && hom.getQueryCoverage()>queryCovCutoff) {
				subList.add(hom);			
				//System.out.println(hom+" "+hom.getPercentIdentity()+" "+hom.getBlastHsp().getParent().getTotalPercentIdentity());
				//System.out.println(hom.getQueryCoverage()+" "+hom.getBlastHsp().getParent().getQueryCoverage());
			}
		}
		
	}
	
	/**
	 * Filters the existing subset to the same domain of life (Bacteria, Archaea, Eukaryota) as the reference sequence
	 */
	public void filterToSameDomainOfLife() {
		Iterator<Homolog> it = subList.iterator();
		while (it.hasNext()) {
			Homolog hom = it.next();
			if (!hom.getUnirefEntry().hasTaxons()) {
				LOGGER.info("Removing homolog "+hom.getUniId()+" as no taxonomy info available for it");
				it.remove();
				continue;
			}
			if (!hom.getUnirefEntry().isInSameDomainOfLife(this.ref)) {
				it.remove();
			}
		}
		
	}
	
	/**
	 * Removes from the original set of homologs returned by blast  
	 * those ones that are 100% identical to query while covering at least the given minQueryCov. 
	 * The query itself will usually be in this group and removed with this procedure.
	 */
	public void removeIdenticalToQuery(double minQueryCov) {
		Iterator<Homolog> it = list.iterator();
		while (it.hasNext()) {
			Homolog hom = it.next();
			if (hom.getPercentIdentity()>99.99999 && hom.getQueryCoverage()>minQueryCov) {
				it.remove();
				LOGGER.info("Removing "+hom.getUniId()+" because it is 100% identical and covers "+
						String.format("%5.1f%%",hom.getQueryCoverage()*100.0)+" of the query.");				
			}
		}
		
		LOGGER.info(getSizeFullList()+" homologs after removing identicals to query");
	}
	
	/**
	 * Reduces the size of the subset of homologs by reducing the sequence redundancy in it.
	 * The procedure is based on clustering the sequences at {@link #CLUSTERING_ID} sequence identity.
	 * This procedure will thus always remove duplicates (100% identical pairs).
	 * Note that the sequences used for clustering are the HSP matching regions only.
	 * One representative is chosen from each cluster and the other
	 * cluster members discarded.
	 * @param maxDesiredHomologs the maximum number of desired homologs
	 * @param mmseqsBin the mmseqs2 executable
	 * @param outDir dir where mmseqs2 will write its output to
	 * @param numThreads the number of threads for mmseqs
	 * @throws IOException 
	 * @throws BlastException 
	 * @throws InterruptedException 
	 */
	public void reduceRedundancy(int maxDesiredHomologs, File mmseqsBin, File outDir, int numThreads)
			throws IOException, InterruptedException, BlastException {

		if (getSizeFilteredSubset()<=1) {
			LOGGER.info("1 or fewer homologs in the filtered list, no need to cluster them for sequence redundancy elimination");
			return;
		}
		
		LOGGER.info("Proceeding to perform redundancy reduction for homologs of {} by clustering of sequences", ref.getUniId());
				
		File inputSeqFile = File.createTempFile(CLUSTERING_BASENAME,FASTA_SUFFIX);

		writeToFasta(inputSeqFile, false);

		// mmseqs by default does 504 hits per query. Thus 504 is the max possible number of sequences to cluster
		// and max number of possible sequences if nothing is clustered

		long start = System.currentTimeMillis();
		List<List<String>> clusters = MmseqsRunner.runMmseqsEasyCluster(mmseqsBin, inputSeqFile, new File(outDir, "seqClustering"), CLUSTERING_ID, CLUSTERING_COVERAGE, numThreads);
		long end = System.currentTimeMillis();
		LOGGER.info("Ran mmseqs2 sequence clustering for redundancy reduction in {}s", ((end-start)/1000));

		if (!DEBUG) {
			// note that if mmseqs throws an exception then this is not reached and thus files not removed on exit
			inputSeqFile.deleteOnExit();
		}

		LOGGER.info("Redundancy elimination at {}% identity resulted in {} clusters", CLUSTERING_ID, clusters.size());
		
		HashSet<String> membersToRemove = new HashSet<>();
		int i = 0;
		for (List<String> cluster:clusters) {
			i++;
			if (cluster.size()>1) {
				StringBuilder msg = new StringBuilder("Cluster "+i+" representative: "+cluster.get(0)+". Removed: ");
				// we then proceed to remove all but first
				for (int j=1;j<cluster.size();j++) {
					membersToRemove.add(cluster.get(j));
					//Homolog toRemove = getHomolog(cluster.get(j));
					//subList.remove(toRemove);
					msg.append(cluster.get(j)).append(" ");
				}								
				LOGGER.info(msg.toString());
			}
		}

		subList.removeIf(hom -> membersToRemove.contains(hom.getIdentifier()));
		
		LOGGER.info("Size of homolog list after redundancy reduction: "+subList.size());
		
		if (subList.size()>maxDesiredHomologs*2.00) {
			LOGGER.info("Size of final homologs list ({}) is larger than 2x of the max number of sequences required ({})", subList.size(), maxDesiredHomologs);
		}

	}
	
	/**
	 * Returns the number of homologs in the unfiltered list (not filtered)
	 * @return
	 */
	public int getSizeFullList() {
		return list.size();
	}
	
	/**
	 * Returns the last homolog (as sorted by blast, i.e. worst evalue) present 
	 * in the full unfiltered list of homologs
	 * @return
	 * @throws ArrayIndexOutOfBoundsException if list is of 0 size
	 */
	public Homolog getLast() {
		return list.get(list.size()-1);
	}
	
	/**
	 * Returns the number of homologs in the filtered subset i.e. the one 
	 * after calling {@link #filterToMinIdAndCoverage(double, double)}
	 * @return
	 */
	public int getSizeFilteredSubset() {
		return subList.size();
	}
	
	/**
	 * Returns the sequence identity cutoff, see {@link #filterToMinIdAndCoverage(double, double)}
	 * @return
	 */
	public double getIdCutoff() {
		return idCutoff;
	}
	
	/**
	 * Returns the query coverage cutoff, see {@link #filterToMinIdAndCoverage(double, double)}
	 * @return
	 */
	public double getQCovCutoff() {
		return qCoverageCutoff;
	}
	
	/**
	 * Gets the uniprot version used for blasting
	 * @return
	 */
	public String getUniprotVer() {
		return uniprotVer;
	}
	
	/**
	 * Compute the sequence entropies for all reference sequence (uniprot) positions
	 * @param alphabet the AAAlphabet
	 */
	public void computeEntropies(AAAlphabet alphabet) {
		this.alphabet = alphabet;
		this.entropies = new ArrayList<Double>(); 
		for (int i=0;i<refInterval.getLength();i++){
			entropies.add(this.aln.getColumnEntropy(this.aln.seq2al(ref.getUniId(),i+1), alphabet));
		}
	}
	
	public List<Double> getEntropies() {
		return entropies;
	}
	
	public AAAlphabet getReducedAlphabet() {
		return alphabet;
	}
	
	public void setUseUniparc(boolean useUniparc) {
		this.useUniparc = useUniparc;
	}
	
	/**
	 * Returns a string containing information about the distribution of the sequence entropies for
	 * the alignment of this homolog list, including an entropy value for the distribution.
	 * It divides the sequence entropy values into 6 bins from 0 to max(s), using that distribution 
	 * to calculate an entropy value of it. 
	 * @return
	 */
	public String getAlnVariability() {
		int numBins = 6;
		// the max value for entropy given a reducedAlphabet
		double maxs = Math.log(alphabet.getNumLetters())/Math.log(2);
		double max = Math.max(1,Collections.max(entropies));
		// the bin step given the max and numBins
		double binStep = max/(double)numBins;
		int[] binCounts = new int[numBins];
		int totalLength = entropies.size();
		for (double s:entropies) {
			int i=0;
			for (double binBoundary=binStep;binBoundary<=max;binBoundary+=binStep) {
				if (s>=binBoundary-binStep && s<binBoundary) binCounts[i]++;
				i++;
			}
		}
		
		StringBuffer bf = new StringBuffer();
		
		bf.append("Distribution of entropies: \n");
		for (double binBoundary=binStep;binBoundary<=max;binBoundary+=binStep) {
			bf.append(String.format("%4.2f ", binBoundary));
		}
		bf.append("\n");
		double sumplogp=0.0;
		for (int i=0;i<numBins;i++){
			bf.append(String.format("%4s ",binCounts[i]));
			double prob = (double)binCounts[i]/(double)totalLength; 
			if (prob!=0){ // plogp is defined to be 0 when p=0 (because of limit). If we let java calculate it, it gives NaN (-infinite) because it tries to compute log(0) 
				sumplogp += prob*(Math.log(prob)/Math.log(2));
			}
		}
		double alnVariability = (-1.0)*sumplogp;
		
		bf.append("\n");
		bf.append(String.format("min: %4.2f max: %4.2f max s possible: %4.2f\n",Collections.min(entropies),Collections.max(entropies),maxs));
		bf.append(String.format("Alignment information content: %4.2f\n",alnVariability));
		return bf.toString();
	}
}
