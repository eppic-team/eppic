package crk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import owl.core.connections.NoMatchFoundException;
import owl.core.connections.SiftsConnection;
import owl.core.features.SiftsFeature;
import owl.core.runners.blast.BlastException;
import owl.core.runners.blast.BlastHit;
import owl.core.runners.blast.BlastHitList;
import owl.core.runners.blast.BlastHsp;
import owl.core.runners.blast.BlastRunner;
import owl.core.runners.blast.BlastXMLParser;
import owl.core.sequence.Sequence;
import owl.core.sequence.Homolog;
import owl.core.sequence.HomologList;
import owl.core.sequence.UniprotVerMisMatchException;
import owl.core.sequence.UnirefEntry;
import owl.core.sequence.alignment.MultipleSequenceAlignment;
import owl.core.sequence.alignment.PairwiseSequenceAlignment;
import owl.core.sequence.alignment.PairwiseSequenceAlignment.PairwiseSequenceAlignmentException;
import owl.core.structure.AminoAcid;
import owl.core.structure.PdbAsymUnit;
import owl.core.structure.PdbChain;
import owl.core.structure.Residue;
import owl.core.util.Interval;

public class ChainEvolContext implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private static final Log LOGGER = LogFactory.getLog(ChainEvolContext.class);

	private static final double ROUNDING_MARGIN = 0.0001;
	
	private static final double MAX_QUERY_TO_UNIPROT_DISAGREEMENT = 0.1;
	
	private static final Pattern ALLX_SEQ_PATTERN = Pattern.compile("^X+$");
	
	// blast constants
	private static final String BLASTOUT_SUFFIX = "blast.out.xml";
	private static final String BLAST_BASENAME = "pdb2unimapping";
	private static final String FASTA_SUFFIX = ".fa";	
	private static final boolean BLAST_NO_FILTERING = true;
	private static final boolean DEBUG = false;
	
	// private classes
	private class MappingCoverage implements Comparable<MappingCoverage>{
		public String uniprotId;
		public int totalCoverage;
		public MappingCoverage(String uniprotId, int coverage) {
			this.uniprotId = uniprotId;
			this.totalCoverage = coverage;			
		}
		public void add(int coverage) {
			this.totalCoverage += coverage;
		}
		@Override
		public int compareTo(MappingCoverage o) {
			return new Integer(totalCoverage).compareTo(o.totalCoverage);
		}
		public String toString() {
			return uniprotId+"-"+totalCoverage;
		}
	}

	
	// members 
	private String representativeChain;		// the pdb chain code of the representative chain
	private String sequence;
	
	private UnirefEntry query;							// the uniprot info (id,seq) corresponding to this chain's sequence
	private Interval queryInterv;						// the interval of the query uniprot reference sequence that is actually used in the blast search
	private boolean hasQueryMatch;						// whether we could find the query's uniprot match or not
	private PairwiseSequenceAlignment alnPdb2Uniprot; 	// the alignment between the pdb sequence and the uniprot sequence (query)
	
	private double idCutoff;
	private double queryCov;
	
	private HomologList homologs;	// the homologs of this chain's sequence
		
	private boolean searchWithFullUniprot;  // mode of searching, true=we search with full uniprot seq, false=we search with PDB matching part only
	
	private List<String> queryWarnings;
	
	private ChainEvolContextList parent;

	public ChainEvolContext(ChainEvolContextList parent, String sequence, String representativeChain) {
		this.parent = parent;		
		this.sequence = sequence;
		this.representativeChain = representativeChain;
		this.hasQueryMatch = false;
		this.searchWithFullUniprot = true;
		this.queryWarnings = new ArrayList<String>();
		
	}
	
	/**
	 * Retrieves the UniProt mapping corresponding to the query PDB sequence 
	 * @throws IOException
	 * @throws BlastException
	 * @throws InterruptedException
	 */
	public void retrieveQueryData(CRKParams params) throws IOException, BlastException, InterruptedException {
		
		String siftsLocation = params.getSiftsFile();
		boolean useSifts = params.isUseSifts();
		File blastPlusBlastp = params.getBlastpBin();
		String blastDbDir = params.getBlastDbDir();
		String blastDb = params.getBlastDb();
		int blastNumThreads = params.getNumThreads();
		double pdb2uniprotIdThreshold = params.getPdb2uniprotIdThreshold();
		double pdb2uniprotQcovThreshold = params.getPdb2uniprotQcovThreshold();
		boolean useUniparc = params.isUseUniparc();
		
		String queryUniprotId = null;
		query = null;
		
		if (sequence.length()<=CRKParams.PEPTIDE_LENGTH_CUTOFF) {
			queryWarnings.add("Chain is a peptide ("+sequence.length()+" residues)");
		}
		
		// two possible cases: 
		// 1) PDB code known and so SiftsFeatures can be taken from SiftsConnection (unless useSifts is set to false)
		List<SiftsFeature> mappings = null;
		if (!params.isInputAFile()) {
			String pdbCode = parent.getPdb().getPdbCode();
			if (useSifts) {
				SiftsConnection siftsConn = parent.getSiftsConn(siftsLocation);
				try {
					mappings = siftsConn.getMappings(pdbCode, representativeChain);
					
					queryUniprotId = checkSiftsMappings(mappings, params.isAllowChimeras());
					 
				} catch (NoMatchFoundException e) {
					LOGGER.warn("No SIFTS mapping could be found for "+pdbCode+representativeChain);
					LOGGER.info("Trying blasting to find one.");
					queryUniprotId = findUniprotMapping(blastPlusBlastp, blastDbDir, blastDb, blastNumThreads, pdb2uniprotIdThreshold, pdb2uniprotQcovThreshold, useUniparc);  
				}
			} else {
				LOGGER.info("Using SIFTS feature is turned off. Will blast to find query-to-uniprot mapping");
				queryUniprotId = findUniprotMapping(blastPlusBlastp, blastDbDir, blastDb, blastNumThreads, pdb2uniprotIdThreshold, pdb2uniprotQcovThreshold, useUniparc);
			}
		// 2) PDB code not known and so SiftsFeatures have to be found by blasting, aligning etc.
		} else {
			LOGGER.info("No PDB code available. Can't use SIFTS. Blasting to find the query's Uniprot mapping.");
			queryUniprotId = findUniprotMapping(blastPlusBlastp, blastDbDir, blastDb, blastNumThreads, pdb2uniprotIdThreshold, pdb2uniprotQcovThreshold, useUniparc);
		}
		
		if (queryUniprotId!=null) hasQueryMatch = true;
		
		if (hasQueryMatch) {

			LOGGER.info("UniProt id for chain "+representativeChain+": "+queryUniprotId);

			// once we have the identifier we get the data from uniprot
			try {
				if (parent.isUseLocalUniprot()) {
					query = parent.getUniProtLocalConnection().getUnirefEntry(queryUniprotId);
				} else {
					query = parent.getUniProtJapiConnection().getUnirefEntry(queryUniprotId);
				}

				// and finally we align the 2 sequences (in case of mapping from SIFTS we rather do this than trusting the SIFTS alignment info)
				alnPdb2Uniprot = new PairwiseSequenceAlignment(sequence, query.getSequence(), parent.getPdbName()+representativeChain, query.getUniId());
				
				LOGGER.info("Chain "+representativeChain+" PDB SEQRES to UniProt alignmnent:\n"+getQueryPdbToUniprotAlnString());
				LOGGER.info("Query ("+parent.getPdbName()+representativeChain+") length: "+sequence.length());
				LOGGER.info("UniProt ("+query.getUniId()+") length: "+query.getLength());
				LOGGER.info("Alignment length: "+alnPdb2Uniprot.getLength());
				int shortestSeqLength = Math.min(query.getLength(), sequence.length());
				double id = (double)alnPdb2Uniprot.getIdentity()/(double)shortestSeqLength;
				LOGGER.info("Query to reference UniProt percent identity: "+String.format("%6.2f%%",id*100.0));
				LOGGER.info("UniProt reference coverage of query's sequence: "+
						String.format("%6.2f%%",100.0*(double)sequence.length()/(double)query.getLength()));
				// in strange cases like 3try (a racemic mixture with a chain composed of L and D aminoacids) the SIFTS-mapped
				// UniProt entry does not align at all to the PDB SEQRES (mostly 'X'), we have to check for this
				if (id<MAX_QUERY_TO_UNIPROT_DISAGREEMENT) {
					String msg = "Identity of PDB to UniProt reference alignment is below maximum allowed threshold ("+
								String.format("%2.0f%%", 100.0*MAX_QUERY_TO_UNIPROT_DISAGREEMENT)+"). " +
								"Will not use the UniProt reference "+query.getUniId();
					LOGGER.warn(msg);
					queryWarnings.add(msg);
					query = null;
					hasQueryMatch = false;
				}
			} catch (PairwiseSequenceAlignmentException e1) {
				LOGGER.fatal("Problem aligning PDB sequence for chain "+representativeChain+" to its UniProt match "+query.getUniId());
				LOGGER.fatal(e1.getMessage());
				LOGGER.fatal("Can't continue");
				System.exit(1);
			}  catch (NoMatchFoundException e) {
				LOGGER.error("Couldn't find UniProt id "+queryUniprotId+" through UniProt JAPI. Obsolete?");
				LOGGER.error("Won't do evolution analysis for chain "+representativeChain);
				query = null;
				hasQueryMatch = false;
			} catch (SQLException e) {
				LOGGER.error("Could not retrieve the UniProt data for UniProt id "+queryUniprotId+" from local database "+params.getLocalUniprotDbName()+", error: "+e.getMessage());
				LOGGER.error("Won't do evolution analysis for chain "+representativeChain);
				query = null;
				hasQueryMatch = false;
			}
		}
		
		// In case we did get the mappings from SIFTS and that we could align properly 
		// we need to check our alignment against SIFTS mappings
		if (hasQueryMatch && mappings!=null) {

			// we only do the check for uniprot mappings of size 1 and not for multi-segment mappings 
			// TODO could we do this check also for multi-segment mapping?
			if (mappings.size()==1) {
				// if we are here it means we have a single uniprot match with only one interval mapping
				Interval interv = mappings.iterator().next().getIntervalSet().iterator().next();

				if (interv.end>query.getSequence().length() || interv.beg>query.getSequence().length()) {
					// This can happen if Uniref100 got the clustering wrong and provide a 
					// representative whose sequence is shorter (but 100% identical) than the member
					// e.g. in UniProt 2013_01 PDB 1bo4 maps to Q53396 (seq length 177) and its representative 
					// in Uniref100 is D3VY99 (seq length 154). The SIFTS mapping then refers to the longer sequence
					// This is a problem only if we use our local UniProt database (which contains the Uniref100 clustering)
					// but not if we use the UniProt JAPI
					LOGGER.warn("The retrieved sequence for UniProt "+query.getUniId()+
							" seems to be shorter than the mapping coordinates in SIFTS (for chain "+representativeChain+")." +
							" This is likely to be a problem with Uniref100 clustering. Sequence length is "+query.getSequence().length()+
							", SIFTS mapping is "+interv.beg+"-"+interv.end);

					// For any other case we check that our mappings coincide and warn if not
				} else if ( (alnPdb2Uniprot.getFirstMatchingPos(false)+1)!=interv.beg ||
						(alnPdb2Uniprot.getLastMatchingPos(false)+1)!=interv.end) {
					LOGGER.warn("The SIFTS mapping does not coincide with our mapping for chain "+representativeChain+", UniProt id "+query.getUniId()+
							": ours is "+
							(alnPdb2Uniprot.getFirstMatchingPos(false)+1)+"-"+(alnPdb2Uniprot.getLastMatchingPos(false)+1)+
							", SIFTS is "+interv.beg+"-"+interv.end);
				}
			}
		}
		
	}
	
	/**
	 * Checks the PDB to UniProt mapping parsed out from the SIFTS resource and 
	 * returns a single UniProt id decided to be the valid mapping. 
	 * If any of the intervals is of 0 or negative length the mapping is rejected.
	 * 
	 * Subcases:
	 *  - more than one UniProt id: chimera case. 
	 *    If allow chimeras is true then the longest length match will be used
	 *  - one UniProt id but several segments: deletion or insertion case.
	 *    If the gaps are above CRKParams.NUM_GAP_RES_FOR_CHIMERIC_FUSION then the 
	 *    mapping is rejected
	 *     
	 * @param mappings
	 * @param allowChimeras
	 * @return
	 */
	private String checkSiftsMappings(List<SiftsFeature> mappings, boolean allowChimeras) {
		
		String queryUniprotId = null;
						
		// first we group the unique uniprot ids in a map also storing their coverage
		HashMap<String,MappingCoverage> uniqUniIds = new HashMap<String,MappingCoverage>();
		boolean hasNegativeLength = false;
		boolean hasNegatives = false;
		for (SiftsFeature sifts:mappings) {
			Interval interv = sifts.getUniprotIntervalSet().iterator().next(); // there's always only 1 interval per SiftsFeature
						
			if (interv.getLength()<=0) hasNegativeLength = true;
			if (interv.beg<=0 || interv.end<=0) hasNegatives = true;
			
			if (!uniqUniIds.containsKey(sifts.getUniprotId())) {
				uniqUniIds.put(sifts.getUniprotId(), new MappingCoverage(sifts.getUniprotId(),interv.getLength()));
			} else {
				uniqUniIds.get(sifts.getUniprotId()).add(interv.getLength());
			}
		}
		
		//we do a sanity check on the SIFTS data: negative intervals are surely errors, we need to reject
		if (hasNegativeLength) {
			LOGGER.warn("Negative intervals present in SIFTS mapping for chain "+representativeChain+". Will not use the SIFTS mapping.");
			queryWarnings.add("Problem with SIFTS PDB to UniProt mapping: negative intervals present in mapping");
			// We continue with a null query, i.e. no match, of course we could try to still find a mapping ourselves
			// by calling findUniprotMapping if we return a null, but for the chimera case below there's also null returned
			// and we don't really want to search again in that case
			// As of Oct 2013 there is a bug in the SIFTS pipeline that produces some negative mappings, e.g. 4iar 
			return null;
		}
		// second sanity check: negative or 0 coordinates are surely errors: reject
		if (hasNegatives) {
			LOGGER.warn("0 or negative coordinates present in SIFTS mapping for chain "+representativeChain+". Will not use the SIFTS mapping.");
			queryWarnings.add("Problem with SIFTS PDB to UniProt mapping: 0 or negative coordinates present in mapping");
			// We continue with a null query, i.e. no match, of course we could try to still find a mapping ourselves
			// by calling findUniprotMapping if we return a null, but for the chimera case below there's also null returned
			// and we don't really want to search again in that case
			// As of Oct 2013 there is a bug in the SIFTS pipeline that produces some negative coordinates, e.g. 1s70 
			return null;			
		}
		
		// a) more than 1 uniprot id: chimeras
		if (uniqUniIds.size()>1) {
						
			String largestCoverageUniprotId = Collections.max(uniqUniIds.values()).uniprotId;
			
			LOGGER.warn("More than one UniProt SIFTS mapping for chain "+representativeChain);
			String msg = "UniProt IDs are: ";
			for (String uniId:uniqUniIds.keySet()){
				msg+=(uniId+" (total coverage "+uniqUniIds.get(uniId).totalCoverage+") ");
			}
			LOGGER.warn(msg);
			
			if (allowChimeras) {
				LOGGER.warn("ALLOW_CHIMERAS mode is on. Will use longest coverage UniProt id as reference "+largestCoverageUniprotId);
				queryUniprotId = largestCoverageUniprotId;
			} else {
				LOGGER.warn("This is likely to be a chimeric chain. Won't do evolution analysis on this chain.");
				String warning = "More than one UniProt id correspond to chain "+representativeChain+": ";
				for (String uniId:uniqUniIds.keySet()){
					warning+=(uniId+" (total coverage "+uniqUniIds.get(uniId).totalCoverage+") ");
				}
				warning+=". This is most likely a chimeric chain";
				queryWarnings.add(warning);
				// we continue with a null query, i.e. we treat it in the same way as no match
				queryUniprotId = null;
			}

		// b) 1 uniprot id but with more than 1 mapping: deletions or insertions in the construct 
		} else if (mappings.size()>1) {			
			boolean unacceptableGaps = false;
			String msg = "";
			
			int lastUniprotEnd = -1;
			int lastPdbEnd = -1;
			
			for (SiftsFeature sifts:mappings) {
				// checking for the gaps between the PDB segments: insertions in PDB sequence with respect to UniProt
				Interval pdbInterv = sifts.getCifIntervalSet().iterator().next();
				if (lastPdbEnd!=-1 && 
					( (pdbInterv.beg-lastPdbEnd)>CRKParams.NUM_GAP_RES_FOR_CHIMERIC_FUSION ||
					  (pdbInterv.beg-lastPdbEnd)<0	)) {
					// if at least one of the gaps is long or an inversion then we consider the mapping unacceptable
					unacceptableGaps = true;
				}
				lastPdbEnd = pdbInterv.end;
				
				// checking for the gaps between the uniprot segments: deletions in PDB sequence with respect to UniProt
				Interval interv = sifts.getUniprotIntervalSet().iterator().next(); // there's always only 1 interval per SiftsFeature
				if (lastUniprotEnd!=-1 && 
					( (interv.beg-lastUniprotEnd)>CRKParams.NUM_GAP_RES_FOR_CHIMERIC_FUSION ||
					  (interv.beg-lastUniprotEnd)<0 )) {
					// if at least 1 of the gaps is long or an inversion (e.g. circular permutants) 
					// then we consider the mappings unacceptable
					// Note: for this to work properly the intervals have to be sorted as they appear in the PDB chain (getMappings above sorts them)
					unacceptableGaps = true;
				}
				lastUniprotEnd = interv.end;
				
				// putting in the same loop the different mapping coordinates in the msg string for reporting below
				msg+=" "+pdbInterv.beg+"-"+pdbInterv.end+","+interv.beg+"-"+interv.end;
				
			}
			if (unacceptableGaps) {
				LOGGER.warn("More than one UniProt segment SIFTS mapping for chain "+representativeChain);
				LOGGER.warn("PDB chain "+representativeChain+" vs UniProt ("+
							mappings.iterator().next().getUniprotId()+") segments:"+msg);
				LOGGER.warn("Check if the PDB entry is biologically reasonable (likely to be an engineered entry). Won't do evolution analysis on this chain.");

				String warning = "More than one UniProt segment of id "+mappings.iterator().next().getUniprotId()+
						" correspond to chain "+representativeChain+":"+msg+
						". This is most likely an engineered chain";
				queryWarnings.add(warning);

				// we continue with a null query, i.e. we treat it in the same way as no match
				queryUniprotId = null;
			} else {
				// this we need to do because sometimes the gaps are of only 2-3 residues and 
				// it is a bit over the top to reject it plainly
				// for some cases the gaps are even 0 length! (I guess a SIFTS error) e.g. 2jdi chain D
				LOGGER.warn("More than one UniProt segment SIFTS mapping for chain "+representativeChain);
				LOGGER.warn("PDB chain "+representativeChain+" vs UniProt ("+
						mappings.iterator().next().getUniprotId()+") segments:"+msg);
				LOGGER.warn("Gaps between segments are all below "+CRKParams.NUM_GAP_RES_FOR_CHIMERIC_FUSION+" residues. " +
						"Will anyway proceed with this UniProt reference");
				queryUniprotId = uniqUniIds.keySet().iterator().next();
			}
		// c) 1 uniprot id and 1 mapping: vanilla case
		} else {
			LOGGER.info("Getting UniProt reference mapping for chain "+representativeChain+" from SIFTS");
			queryUniprotId = uniqUniIds.keySet().iterator().next();
		}
		
		return queryUniprotId;
	}
	
	
	public void blastForHomologs(CRKParams params) 
	throws IOException, BlastException, UniprotVerMisMatchException, InterruptedException {
		
		
		File blastPlusBlastp = params.getBlastpBin();
		String blastDbDir = params.getBlastDbDir();
		String blastDb = params.getBlastDb();
		int blastNumThreads = params.getNumThreads(); 
		int maxNumSeqs = params.getMaxNumSeqs();
		HomologsSearchMode searchMode = params.getHomologsSearchMode();
		boolean useUniparc = params.isUseUniparc();			
		
		queryInterv = new Interval(1,query.getLength());
		
		LOGGER.info("Homologs search mode: "+searchMode.getName());
		if (searchMode==HomologsSearchMode.GLOBAL) {
			searchWithFullUniprot = true;
			LOGGER.info("Using full UniProt sequence "+query.getUniId()+" for blast search"+" (chain "+getRepresentativeChainCode()+")");
		} else if (searchMode==HomologsSearchMode.LOCAL) {
			searchWithFullUniprot = false;
			queryInterv = new Interval(alnPdb2Uniprot.getFirstMatchingPos(false)+1,alnPdb2Uniprot.getLastMatchingPos(false)+1);
			LOGGER.info("Using UniProt "+query.getUniId()+" subsequence "+queryInterv.beg+"-"+queryInterv.end+" for blast search"+" (chain "+getRepresentativeChainCode()+")");
		} 
		
		homologs = new HomologList(query,queryInterv);
		
		if (useUniparc) LOGGER.info("UniParc hits will be used");
		else LOGGER.info("UniParc hits won't be used");
		homologs.setUseUniparc(useUniparc);
		
		File blastCacheFile = null;
		if (params.getBlastCacheDir()!=null) {
			blastCacheFile = new File(params.getBlastCacheDir(),getQuery().getUniId()+
					"."+queryInterv.beg+"-"+queryInterv.end+CRKParams.BLAST_CACHE_FILE_SUFFIX);
		}
		
		homologs.searchWithBlast(blastPlusBlastp, blastDbDir, blastDb, blastNumThreads, maxNumSeqs, blastCacheFile);
		LOGGER.info(homologs.getSizeFullList()+" homologs found by blast (chain "+getRepresentativeChainCode()+")");
		
		if (homologs.getSizeFullList()>0) {
			BlastHit lastBlastHit = homologs.getLast().getBlastHsp().getParent();		
			LOGGER.info("Last blast hit (lowest score hit): "+lastBlastHit.getSubjectId()+", identity of its best HSP: "+
					String.format("%5.2f%%",lastBlastHit.getMaxScoringHsp().getPercentIdentity()) );
		}
		 
	}
	
	public void filterToSameDomainOfLife() {
		if (query.hasTaxons()) {
			LOGGER.info("Filtering to domain: "+query.getFirstTaxon());
			homologs.filterToSameDomainOfLife();
			LOGGER.info(homologs.getSizeFilteredSubset()+" homologs after filtering to "+query.getFirstTaxon()+" (chain "+getRepresentativeChainCode()+")");
		} else {
			LOGGER.info("Taxons of chain "+getRepresentativeChainCode()+" are unknown, can't filter to same domain of life.");
		}
	}
	
	public void removeIdenticalToQuery(double minQueryCov) {
		homologs.removeIdenticalToQuery(minQueryCov);
	}
	
	/**
	 * Retrieves the Uniprot data and metadata
	 * @throws IOException
	 * @throws UniprotVerMisMatchException
	 * @throws SQLException 
	 */
	public void retrieveHomologsData() throws IOException, UniprotVerMisMatchException, SQLException {
		if (parent.isUseLocalUniprot()) {
			homologs.retrieveUniprotKBData(parent.getUniProtLocalConnection());
		} else {
			homologs.retrieveUniprotKBData(parent.getUniProtJapiConnection());
			homologs.retrieveUniparcData(null);
		}
	}
	
	public void applyHardIdentityCutoff(double homHardIdCutoff, double queryCovCutoff) {
		// note that we subtract just a tiny rounding margin (0.0001) in order to be sure that we do take anything
		// that's just at the hard cutoff
		// Otherwise in rare cases (e.g. 2os7 with hard cutoff of 0.4 in uniprot_2012_05, blast hit G5S5I8 is exactly at 40%)
		// we would not include an entry in this procedure but it would be included in the applyIdentityCutoff method below (which would 
		// then crash because there wouldn't be sequence data for it)
		homologs.filterToMinIdAndCoverage(homHardIdCutoff-ROUNDING_MARGIN, queryCovCutoff);		
		LOGGER.info(homologs.getSizeFilteredSubset()+" homologs within the hard identity cutoff "+String.format("%4.2f",homHardIdCutoff)
				+" (chain "+getRepresentativeChainCode()+")");
	}
	
	public void applyIdentityCutoff(CRKParams params) throws IOException, InterruptedException, BlastException {

		double homSoftIdCutoff = params.getHomSoftIdCutoff();
		double homHardIdCutoff = params.getHomHardIdCutoff();
		double homIdStep = params.getHomIdStep();
		double queryCovCutoff = params.getQueryCoverageCutoff();
		int minNumSeqs = params.getMinNumSeqs();
		int maxNumSeqs = params.getMaxNumSeqs();
		
		
		this.queryCov = queryCovCutoff;
		
		double currentIdCutoff = homSoftIdCutoff;
		
		while (currentIdCutoff>homHardIdCutoff-ROUNDING_MARGIN) { 
						
			homologs.filterToMinIdAndCoverage(currentIdCutoff, queryCovCutoff);
						
			LOGGER.info(homologs.getSizeFilteredSubset()+" homologs after applying "+
					String.format("%4.2f",currentIdCutoff)+" identity cutoff and "+
					String.format("%4.2f",queryCovCutoff)+" query coverage cutoff and before redundancy elimination"
					+" (chain "+getRepresentativeChainCode()+")");
			
			homologs.reduceRedundancy(maxNumSeqs, params.getBlastclustBin(), params.getBlastDataDir(), params.getNumThreads());

			this.idCutoff = currentIdCutoff;
			
			if (homologs.getSizeFilteredSubset()>=minNumSeqs) break;

			LOGGER.info("Tried "+String.format("%4.2f",currentIdCutoff)+" identity cutoff, only "+
					homologs.getSizeFilteredSubset()+" non-redundant homologs found ("+minNumSeqs+" required)"
					+" (chain "+getRepresentativeChainCode()+")");
			
			currentIdCutoff -= homIdStep;
		}
				
	}

	public void align(CRKParams params) throws IOException, InterruptedException, UniprotVerMisMatchException { 
		File tcoffeeBin = params.getTcoffeeBin();
		File clustaloBin = params.getClustaloBin();
		int nThreads = params.getNumThreads();
		
		// 3) alignment of the protein sequences
		File alnCacheFile = null;
		// beware we only try to use cache file if we are in default mode: uniparc=true, filter by domain=false
		if (params.getAlnCacheDir()!=null && params.isUseUniparc() && !params.isFilterByDomain()) {
			String intervStr = "";
			if (!isSearchWithFullUniprot()) {
				intervStr = "."+getQueryInterval().beg+"-"+getQueryInterval().end;
			}
			// a cache file will look like  Q9UKX7.i60.c85.m60.1-109.aln (that corresponds to 3tj3C) 
			//                              P52294.i60.c85.m60.aln       (that corresponds to 3tj3A)
			// i=identity threshold used, c=query coverage, m=max num seqs, optional two last numbers are the subinterval
			alnCacheFile = new File(params.getAlnCacheDir(),getQuery().getUniId()+
					".i"+String.format("%2.0f", idCutoff*100.0)+
					".c"+String.format("%2.0f", queryCov*100.0)+
					".m"+params.getMaxNumSeqs()+
					intervStr+
					".aln"); 
		}
		
		homologs.computeAlignment(tcoffeeBin, clustaloBin, nThreads, alnCacheFile);
	}
	
	public void writeAlignmentToFile(File alnFile) throws FileNotFoundException {
		homologs.writeAlignmentToFile(alnFile); 
	}
	
	public void writeHomologSeqsToFile(File outFile, CRKParams params) throws FileNotFoundException {
		homologs.writeToFasta(outFile, false);
	}
	
	public MultipleSequenceAlignment getAlignment() {
		return homologs.getAlignment();
	}
	
	/**
	 * Returns the query's PDB SEQRES to uniprot alignment as a nicely formatted
	 * aligmnent string in several lines with a middle line of matching characters,
	 * e.g. 
	 * 1abcA   AAAA--BCDEFGICCC
	 *         ||.|  ||.|||:|||
	 * QABCD1  AABALCBCJEFGLCCC
	 * @return
	 */
	public String getQueryPdbToUniprotAlnString() {
		return alnPdb2Uniprot.getFormattedAlignmentString();
	}
	
	public PairwiseSequenceAlignment getPdb2uniprotAln() {
		return alnPdb2Uniprot;
	}
	
	/**
	 * Returns a list of the conservation scores values, i.e. entropy or ka/ks ratios 
	 * depending on scoType.
	 * The sequence to which the list refers is the query Uniprot/CDS translated sequence, 
	 * not the PDB SEQRES.   
	 * @param scoType
	 * @return
	 */
	public List<Double> getConservationScores(ScoringType scoType) {
		if (scoType.equals(ScoringType.ENTROPY)) {
			return homologs.getEntropies();
		}
		throw new IllegalArgumentException("Given scoring type "+scoType+" is not recognized ");

	}
	
	/**
	 * Compute the sequence entropies for all reference sequence (uniprot) positions
	 * @param reducedAlphabet
	 */
	public void computeEntropies(int reducedAlphabet) {
		homologs.computeEntropies(reducedAlphabet);
	}
	
	/**
	 * Calculates the evolutionary score for the given list of residues by summing up evolutionary
	 * scores per residue and averaging (optionally weighted by BSA).
	 * If a residue does not have a mapping to the reference UniProt (i.e. aligns to a gap) 
	 * then it is ignored and not counted in the average. 
	 * @param residues
	 * @param scoType whether the evolutionary score should be entropy or Ka/Ks
	 * @param weighted whether the scores should be weighted by BSA of each residue
	 * @return the average (optionally BSA weighted) evolutionary score for the given set 
	 * of residues or NaN if all residues do not have a mapping to the reference UniProt or 
	 * if the input residue list is empty.
	 */
	public double calcScoreForResidueSet(List<Residue> residues, ScoringType scoType, boolean weighted) {
		double totalScore = 0.0;
		double totalWeight = 0.0;
		List<Double> conservScores = getConservationScores(scoType);
		for (Residue res:residues){
			int resSer = res.getSerial(); 

			int queryPos = -2;
			if (scoType==ScoringType.ENTROPY) {
				queryPos = getQueryUniprotPosForPDBPos(resSer); 
			} 
			if (queryPos!=-1) {   
				double weight = 1.0;
				if (weighted) {
					weight = res.getBsa();
				}
				totalScore += weight*(conservScores.get(queryPos));
				totalWeight += weight;
			} else {
				// ignore it
			}

		}
		return totalScore/totalWeight;
	}
	
	/**
	 * Gets the size of the reduced alphabet used for calculating entropies
	 * @return the size of the reduced alphabet or 0 if no entropies have been computed
	 */
	public int getReducedAlphabet() {
		return homologs.getReducedAlphabet();
	}
	
	/**
	 * Prints a summary of the query and uniprot/cds identifiers and homologs with 
	 * their uniprot/cds identifiers
	 * @param ps
	 */
	public void printSummary(PrintStream ps) {
		
		ps.println("Query: "+parent.getPdbName()+representativeChain);
		ps.println("UniProt id for query:");
		ps.print(this.query.getUniId());
		if (this.query.hasTaxons()) ps.println("\t"+this.query.getFirstTaxon()+"\t"+this.query.getLastTaxon());
		else ps.println("\tunknown taxonomy");
		ps.println();
		
		ps.println("UniProt version: "+getUniprotVer());
		ps.println("Homologs: "+homologs.getSizeFilteredSubset()+" with minimum "+
				String.format("%4.2f",homologs.getIdCutoff())+" identity and "+
				String.format("%4.2f",homologs.getQCovCutoff())+" query coverage");

		int clusteringPercentId = homologs.getUsedClusteringPercentId();		
		if (clusteringPercentId>0) 
			ps.println("List is redundancy reduced through clustering on "+clusteringPercentId+"% sequence identity");
		
		for (Homolog hom:homologs.getFilteredSubset()) {
			ps.printf("%-13s",hom.getUniId());
			ps.printf("\t%5.1f",hom.getPercentIdentity());
			ps.printf("\t%5.1f",hom.getQueryCoverage()*100.0);
			if (hom.getUnirefEntry().hasTaxons()) 
				ps.print("\t"+hom.getUnirefEntry().getFirstTaxon()+"\t"+hom.getUnirefEntry().getLastTaxon());
			ps.println();
		}
	}
	
	public void printConservationScores(PrintStream ps, ScoringType scoType, PdbAsymUnit pdb) {
		if (scoType.equals(ScoringType.ENTROPY)) {
			ps.println("# Entropies for all query sequence positions (reference UniProt: "+query.getUniId()+") based on a "+homologs.getReducedAlphabet()+" letters alphabet.");
			ps.println("# seqres\tpdb\tuniprot\tuniprot_res\tentropy");
		} 
		PdbChain chain = null;
		if (pdb!=null) chain = pdb.getChain(representativeChain);
		List<Double> conservationScores = getConservationScores(scoType);		
		for (int i=0;i<conservationScores.size();i++) {
			int resser = 0;
			if (scoType.equals(ScoringType.ENTROPY)) {
				resser = getPDBPosForQueryUniprotPos(i+queryInterv.beg-1);
			}
			String pdbresser = "NA";
			if (chain!=null) pdbresser = chain.getPdbResSerFromResSer(resser);
			ps.printf("%4d\t%4s\t%4d\t%3s\t%5.2f\n",
					resser, 
					pdbresser, 
					i+queryInterv.beg, 
					AminoAcid.one2three(query.getSequence().charAt(i+queryInterv.beg-1)), 
					conservationScores.get(i));
		}
	}
	
	/**
	 * Set the b-factors of the given pdb chain to conservation score values.
	 * @param chain
	 */
	protected void setConservationScoresAsBfactors(PdbChain chain) {
		
		// do nothing (i.e. keep original b-factors) if there's no query match for this sequence and thus no evol scores calculated 
		if (!hasQueryMatch()) return;
		
		List<Double> conservationScores = getConservationScores(ScoringType.ENTROPY);
		
		HashMap<Integer,Double> map = new HashMap<Integer, Double>();
		for (Residue residue:chain) {
			// we don't need to take care of het residues, as we use the uniprot ref for the calc of entropies entropies will always be asigned even for hets
			//if (!(residue instanceof AaResidue)) continue;
			int resser = residue.getSerial();
			int queryPos = getQueryUniprotPosForPDBPos(resser); 
 
			if (queryPos!=-1) {   
				map.put(resser, conservationScores.get(queryPos));	
			} else {
				// when no entropy info is available for a residue we still want to assign a value for it
				// or otherwise the residue would keep its original real bfactor and then possibly screw up the
				// scaling of colors for the rest
				// The most sensible value we can use is the max entropy so that it looks like a poorly conserved residue
				double maxEntropy = Math.log(this.homologs.getReducedAlphabet())/Math.log(2);
				map.put(resser, maxEntropy);
			}
		}
		chain.setBFactorsPerResidue(map);		
	}
	
	public int getNumHomologs() {
		if (homologs==null) return 0;
		return homologs.getSizeFilteredSubset();
	}
	
	public String getRepresentativeChainCode() {
		return representativeChain;
	}
	
	public String getPdbSequence() {
		return sequence;
	}
	
	/**
	 * Tells whether a given position of the reference PDB sequence (starting at 1)
	 * is reliable with respect to:
	 *  the PDB to uniprot matching (mismatches occur mostly because of engineered residues in PDB)
	 * @param resser the (cif) PDB residue serial corresponding to the SEQRES record, starting at 1
	 * @return
	 */
	public boolean isPdbSeqPositionMatchingUniprot(int resser) {
		// we check if the PDB to uniprot mapping is reliable (identity) at the resser position
		if (!alnPdb2Uniprot.isMatchingTo2(resser-1)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Given a residue serial of the reference PDB SEQRES sequence (starting at 1), returns
	 * its corresponding Uniprot's sequence index within subinterval (starting at 0)
	 * @param resser
	 * @return the mapped uniprot sequence position or -1 if it maps to a gap
	 */
	public int getQueryUniprotPosForPDBPos(int resser) {
		int uniprotPos = alnPdb2Uniprot.getMapping1To2(resser-1);
		if (uniprotPos==-1) return -1; // maps to a gap in alignment: return -1
		// the position in the subsequence that was used for blasting
		int pos = uniprotPos - (queryInterv.beg-1); 
		// check if it is out of the subsequence: can happen when his tags or other engineered residues at termini 
		// in n-terminal (pos<0), e.g. 3n1e
		// or c-terminal (pos>=subsequence length) e.g. 2eyi
		if (pos<0 || pos>=queryInterv.getLength()) return -1;  
		return pos;
	}
	
	/**
	 * Given a sequence index of the query Uniprot sequence (starting at 0), returns its
	 * corresponding PDB SEQRES position (starting at 1)
	 * @param queryPos
	 * @return the mapped PDB SEQRES sequence position or -1 if it maps to a gap
	 */
	public int getPDBPosForQueryUniprotPos(int queryPos) {
		return alnPdb2Uniprot.getMapping2To1(queryPos)+1;
	}
	
	private String findUniprotMapping(File blastPlusBlastp, String blastDbDir, String blastDb, int blastNumThreads, double pdb2uniprotIdThreshold, double pdb2uniprotQcovThreshold, boolean useUniparc) 
			throws IOException, BlastException, InterruptedException {
		// for too short sequence it doesn't make sense to blast
		if (this.sequence.length()<CRKParams.MIN_SEQ_LENGTH_FOR_BLASTING) {
			LOGGER.info("Chain "+representativeChain+" too short for blasting. Won't try to find a UniProt reference for it.");
			// query warnings for peptides (with a higher cutoff than this) are already logged before, see above
			// note that then as no reference is found, there won't be blasting for homologs either
			return null;
		}
		// we have to skip sequences composed of all unknown/non-standard (all Xs), blast fails otherwise
		Matcher m = ALLX_SEQ_PATTERN.matcher(this.sequence);
		if (m.matches()) {
			LOGGER.info("Chain "+representativeChain+" is composed only of unknown or non-standard aminoacids. Won't try to find a UniProt reference for it.");
			queryWarnings.add("Sequence is composed only of unknown or non-standard aminoacids, cannot find a UniProt reference for it");
			return null;
		}
		
		File outBlast = File.createTempFile(BLAST_BASENAME,BLASTOUT_SUFFIX);
		File inputSeqFile = File.createTempFile(BLAST_BASENAME,FASTA_SUFFIX);
		if (!DEBUG) {
			outBlast.deleteOnExit();
			inputSeqFile.deleteOnExit();
		}
		
		new Sequence(parent.getPdbName(),this.sequence).writeToFastaFile(inputSeqFile);

		BlastRunner blastRunner = new BlastRunner(blastDbDir);
		blastRunner.runBlastp(blastPlusBlastp, inputSeqFile, blastDb, outBlast, BlastRunner.BLASTPLUS_XML_OUTPUT_TYPE, BLAST_NO_FILTERING, blastNumThreads, 500);

		BlastHitList blastList = null;
		try {
			// note that by setting second parameter to true we are ignoring the DTD url to avoid unnecessary network connections
			BlastXMLParser blastParser = new BlastXMLParser(outBlast, true);
			blastList = blastParser.getHits();
		} catch (SAXException e) {
			// if this happens it means that blast doesn't format correctly its XML, i.e. has a bug
			LOGGER.fatal("Unexpected error: "+e.getMessage());
			System.exit(1);
		}

		String uniprotMapping = null;

		if (!blastList.isEmpty()) {
			BlastHit best = null;
			blastList.sort();
			for (int i=0;i<blastList.size();i++) {
				best = blastList.get(i);
				if (isHitUniprot(best)) break;
				if (i==blastList.size()-1) { // if not a single hit is uniprot then we get to here and we have to catch it
					LOGGER.warn("No UniProt match could be found for the query "+parent.getPdbName()+representativeChain+
							": no blast hit was a UniProt match (total "+blastList.size()+" blast hits)");
					queryWarnings.add("Blast didn't find a UniProt match for the chain. All blast hits were non-UniProt matches (total "+blastList.size()+" blast hits).");
					return null;
				}
			}
			BlastHsp bestHsp = best.getMaxScoringHsp();
			if ((bestHsp.getPercentIdentity()/100.0)>pdb2uniprotIdThreshold && bestHsp.getQueryCoverage()>pdb2uniprotQcovThreshold) {
				uniprotMapping = getDeflineAccession(best);
				LOGGER.info("Blast found UniProt id "+uniprotMapping+" as best hit with "+
						String.format("%5.2f%% id and %4.2f coverage",bestHsp.getPercentIdentity(),bestHsp.getQueryCoverage()));
			} else {
				LOGGER.warn("No UniProt match could be found for the query "+parent.getPdbName()+representativeChain+" within cutoffs "+
						String.format("%5.2f%% id and %4.2f coverage",pdb2uniprotIdThreshold,pdb2uniprotQcovThreshold));
				LOGGER.warn("Best match was "+best.getSubjectId()+", with "+
						String.format("%5.2f%% id and %4.2f coverage",bestHsp.getPercentIdentity(),bestHsp.getQueryCoverage()));
				LOGGER.warn("Alignment: ");
				LOGGER.warn(bestHsp.getAlignment().getFastaString(null, true));
				queryWarnings.add("Blast didn't find a UniProt match for the chain. Best match was "+getDeflineAccession(best)+", with "+
						String.format("%5.2f%% id and %4.2f coverage",bestHsp.getPercentIdentity(),bestHsp.getQueryCoverage()));
			}			
		} else {
			LOGGER.warn("No UniProt match could be found for the query "+parent.getPdbName()+representativeChain+". Blast returned no hits.");
			queryWarnings.add("Blast didn't find a UniProt match for the chain (no hits returned by blast)");
		}

		return uniprotMapping;
	}

	/**
	 * Gets the UniProt id, UniProt isoform id or UniParc id given a blast hit
	 * @param hit
	 * @return the id or null if the defline doesn't match the regexes for UniProt, UniProt isoform or UniParc ids
	 */
	private String getDeflineAccession(BlastHit hit) {
		String sid = hit.getSubjectId();
		Matcher m = Sequence.DEFLINE_PRIM_ACCESSION_REGEX.matcher(sid);
		if (m.matches()) {
			return m.group(1); 
		} else {
			Matcher m2 = Sequence.DEFLINE_PRIM_ACCESSION_UNIREF_REGEX.matcher(sid);
			if (m2.matches()) {
				return m2.group(1);
			} else {
				return null;
			}
		}
	}
	
	/**
	 * Tells whether given BlastHit's defline matches a UniProt id
	 * @param hit
	 * @return
	 */
	private boolean isHitUniprot(BlastHit hit) {
		String sid = hit.getSubjectId();
		Matcher m = Sequence.DEFLINE_PRIM_ACCESSION_REGEX.matcher(sid);
		if (m.matches()) {
			return true;
		} else {
			Matcher m2 = Sequence.DEFLINE_PRIM_ACCESSION_UNIREF_REGEX.matcher(sid);
			if (m2.matches()) {
				String uniId = m2.group(1);
				if (uniId.contains("-")) {
					LOGGER.warn("Blast hit "+uniId+" is a UniProt isoform id. Skipping it");
					return false;
				} else if (uniId.startsWith("UPI")) {
					LOGGER.warn("Blast hit "+uniId+" is a UniParc id. Skipping it");
					return false;
				} else {
					return true;
				}
			} else {
				LOGGER.warn("Could not find UniProt id in blast subject id "+sid+". Skipping it");
				return false;
			}
		}
	}
	
	public String getUniprotVer() {
		return parent.getUniprotVer();
	}
	
	/**
	 * The uniprot match for the query, use {@link #hasQueryMatch()} to check whether this can be called. 
	 * @return
	 */
	public UnirefEntry getQuery() {
		return query;
	}
	
	/**
	 * The interval in the UniProt match (query) that is considered for the blast search
	 * @return
	 */
	public Interval getQueryInterval() {
		return queryInterv;
	}
	
	/**
	 * Whether the query's UniProt match could be found or not.
	 * @return
	 */
	public boolean hasQueryMatch() {
		return hasQueryMatch;
	}
	
	/**
	 * Whether the search for homologs is based on full Uniprot or on a sub-interval, 
	 * get the interval with {@link #getQueryInterval()} 
	 * @return
	 */
	public boolean isSearchWithFullUniprot() {
		return searchWithFullUniprot;
	}
	
	/**
	 * Returns the actual identity cut-off applied after calling {@link #applyIdentityCutoff(double, double, double, double, int)}
	 * @return
	 */
	public double getIdCutoff() {
		return idCutoff;
	}
	
	/**
	 * Returns the actual percent identity cut-off used for the final list of homologs
	 * @return
	 */
	public int getUsedClusteringPercentId() {
		return homologs.getUsedClusteringPercentId();
	}
	
	/**
	 * Returns a list of strings containing warnings related to the query sequence matching
	 * @return
	 */
	public List<String> getQueryWarnings() {
		return queryWarnings;
	}
	
	/**
	 * Returns the HomologList
	 * @return
	 */
	public HomologList getHomologs() {
		return homologs;
	}
}
