package eppic;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eppic.db.dao.DaoException;
import eppic.db.dao.HitHspDAO;
import eppic.db.dao.jpa.HitHspDAOJpa;
import eppic.model.dto.HitHsp;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.EntityInfo;
import org.biojava.nbio.structure.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import eppic.commons.blast.BlastException;
import eppic.commons.blast.BlastHit;
import eppic.commons.blast.BlastHitList;
import eppic.commons.blast.BlastHsp;
import eppic.commons.blast.BlastRunner;
import eppic.commons.blast.BlastXMLParser;
import eppic.commons.sequence.AAAlphabet;
import eppic.commons.sequence.HomologList;
import eppic.commons.sequence.MultipleSequenceAlignment;
import eppic.commons.sequence.NoMatchFoundException;
import eppic.commons.sequence.Sequence;
import eppic.commons.sequence.SiftsConnection;
import eppic.commons.sequence.SiftsFeature;
import eppic.commons.sequence.UnirefEntry;
import eppic.commons.util.Interval;
import uk.ac.ebi.uniprot.dataservice.client.exception.ServiceException;


public class ChainEvolContext implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ChainEvolContext.class);
	
	private static final double ROUNDING_MARGIN = 0.0001;
	
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
		@Override
		public String toString() {
			return uniprotId+"-"+totalCoverage;
		}
	}

	
	// members 
	
	/**
	 * An identifier for the sequence, usually the chain id of the representative Chain
	 * It must uniquely identify the ChainEvolContext in a ChainEvolContextList
	 */
	private String sequenceId;
	private String sequence;
	
	private UnirefEntry query;							// the uniprot info (id,seq) corresponding to this chain's sequence
	private boolean hasQueryMatch;						// whether we could find the query's uniprot match or not
	
	private PdbToUniProtMapper pdbToUniProtMapper;
	
	private double idCutoff;
	private double queryCov;
	
	private HomologList homologs;	// the homologs of this chain's sequence
		
	private boolean searchWithFullUniprot;  // mode of searching, true=we search with full uniprot seq, false=we search with PDB matching part only
	
	private List<String> queryWarnings;
	
	private ChainEvolContextList parent;
	
	private boolean isProtein;
	
	private EntityInfo entity;
	

	/**
	 * Construct a ChainEvolContext from a Sequence. 
	 * The sequence is assumed to be a protein sequence, it is not checked.
	 * @param parent
	 * @param sequence
	 */
	public ChainEvolContext(ChainEvolContextList parent, String sequence, String sequenceName) {
		this.parent = parent;		
		this.sequence = sequence;
		this.sequenceId = sequenceName;
		this.hasQueryMatch = false;
		this.searchWithFullUniprot = true;
		this.queryWarnings = new ArrayList<String>();
		this.isProtein = true;
		this.entity = null;
	}
	
	/**
	 * Construct a ChainEvolContext from a Chain
	 * @param parent
	 * @param compound
	 */
	public ChainEvolContext(ChainEvolContextList parent, EntityInfo compound) {
		this.parent = parent;
		Chain chain = compound.getRepresentative();
		
		this.sequenceId = chain.getName();
		this.hasQueryMatch = false;
		this.searchWithFullUniprot = true;
		this.queryWarnings = new ArrayList<String>();
		
		this.isProtein = chain.isProtein();
		
		this.entity = compound;
		
		this.pdbToUniProtMapper = new PdbToUniProtMapper(compound);
		
		this.sequence = pdbToUniProtMapper.getPdbSequence();
	}
	
	public String getSequenceId() {
		return sequenceId;
	}
	
	public boolean isProtein() {
		return isProtein;
	}
	
	public EntityInfo getCompound() { 
		return entity;
	}
	
	/**
	 * Retrieves the UniProt mapping corresponding to the query PDB sequence 
	 * @throws IOException
	 * @throws BlastException
	 * @throws InterruptedException
	 */
	public void retrieveQueryData(EppicParams params) throws IOException, BlastException, InterruptedException {
		
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
		
		
		if (!isProtein) {
			// nothing to do if it is not protein: query will remain null and hasQueryMatch will remain false
			LOGGER.info("Chain {} is not a protein, will not do evolutionary analysis for it", sequenceId);
			queryWarnings.add("Chain is not a protein, no evolutionary analysis is performed for this chain");
			return;
		}
		
		if (isProtein && sequence.length()<=EppicParams.PEPTIDE_LENGTH_CUTOFF) {
			queryWarnings.add("Chain is a peptide ("+sequence.length()+" residues)");
		}
		
		// two possible cases: 
		// 1) PDB code known and so SiftsFeatures can be taken from SiftsConnection (unless useSifts is set to false)
		List<SiftsFeature> mappings = null;
		boolean findMappingWithBlast = false;
		
		if (!params.isInputAFile() || params.isUsePdbCodeFromFile()) {
			String pdbCode = parent.getPdb().getPDBCode().toLowerCase();
			if (useSifts) {
				if (pdbCode==null || !pdbCode.matches("\\d\\w\\w\\w")) {
					LOGGER.info("Could not read a PDB code in file.");
					findMappingWithBlast = true;
				} else {

					SiftsConnection siftsConn = parent.getSiftsConn(siftsLocation);
					try {
						mappings = siftsConn.getMappings(pdbCode, sequenceId);

						queryUniprotId = checkSiftsMappings(mappings, params.isAllowChimeras());

					} catch (NoMatchFoundException e) {
						LOGGER.warn("No SIFTS mapping could be found for "+pdbCode+sequenceId);
						findMappingWithBlast = true;
					}
				}
				
			} else {
				LOGGER.info("Using SIFTS feature is turned off.");
				findMappingWithBlast = true;
			}
		// 2) PDB code not known and so SiftsFeatures have to be found by blasting, aligning etc.
		} else {
			LOGGER.info("Input is from file, won't use SIFTS. "
					+ "Set the property USE_PDB_CODE_FROM_FILE to true if you want to change this behavior.");
			findMappingWithBlast = true;
		}
		
		if (findMappingWithBlast) {
			if (params.isNoBlast()) {
				LOGGER.info("Will not blast to find query to UniProt mapping. Blasting is disabled (-B option)."
						+ " No UniProt reference mapping will be available and thus no homologs alignment.");
			} else {
				LOGGER.info("Will blast to find query to UniProt mapping.");
				queryUniprotId = findUniprotMapping(blastPlusBlastp, blastDbDir, blastDb, blastNumThreads, pdb2uniprotIdThreshold, pdb2uniprotQcovThreshold, useUniparc);
			}
		}
		
		if (queryUniprotId!=null) hasQueryMatch = true;
		
		if (hasQueryMatch) {

			LOGGER.info("UniProt id for chain "+sequenceId+": "+queryUniprotId);			

			// once we have the identifier we get the data from uniprot
			try {
				if (parent.isUseLocalUniprot()) {
					query = parent.getUniProtLocalConnection().getUnirefEntry(queryUniprotId);
				} else {
					query = parent.getUniProtJapiConnection().getUnirefEntry(queryUniprotId);
				}
				
				if (query.replaceNonStandardByX()) {
					// TODO for jaligner we needed to replace 'O' by 'X', with Biojava and I'm not sure if this is needed anymore...
					LOGGER.warn("Replacing 'O' by 'X' in UniProt reference "+query.getUniId());
				}

				// this initialises the alignment that maps PDB-to-UniProt
				pdbToUniProtMapper.setUniProtReference(query);
				
				String warning = pdbToUniProtMapper.checkAlignments();
				if (warning!=null) {
					queryWarnings.add(warning);
					query = null;
					hasQueryMatch = false;
				}
				
				// the back-mapping to pdb is not working well yet, commented out
				//LOGGER.info("Our mapping in SIFTS tab format: "+pdbToUniProtMapper.getMappingSiftsFormat(sequenceId));
				//LOGGER.info("Our mapping in DBREF PDB format: "+pdbToUniProtMapper.getMappingDbrefFormat(sequenceId));

			}  catch (NoMatchFoundException e) {
				if (parent.isUseLocalUniprot()) {
					LOGGER.warn("Couldn't find UniProt id "+queryUniprotId+" (reference for chain "+sequenceId+") in local database. Obsolete?");
				} else {
					LOGGER.warn("Couldn't find UniProt id "+queryUniprotId+" (reference for chain "+sequenceId+") through UniProt JAPI. Obsolete?");	
				}				
				LOGGER.warn("Won't do evolution analysis for chain "+sequenceId);
				query = null;
				hasQueryMatch = false;
			} catch (ServiceException e) {
				LOGGER.warn("Could not retrieve the UniProt data for UniProt id "+queryUniprotId+" from UniProt JAPI, error: "+e.getMessage());
				LOGGER.warn("Won't do evolution analysis for chain "+sequenceId);
				query = null;
				hasQueryMatch = false;								
			} catch (SQLException e) {
				LOGGER.warn("Could not retrieve the UniProt data for UniProt id "+queryUniprotId+" from local database "+params.getLocalUniprotDbName()+", error: "+e.getMessage());
				LOGGER.warn("Won't do evolution analysis for chain "+sequenceId);
				query = null;
				hasQueryMatch = false;
			} catch (CompoundNotFoundException e) {
				LOGGER.warn("Some unknown compounds in PDB or UniProt protein sequences, error: "+e.getMessage());
				LOGGER.warn("Sequences: ");
				LOGGER.warn("PDB: {}",sequence);
				LOGGER.warn("UniProt ({}): {}",query.getUniId(), query.getSequence());
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
							" seems to be shorter than the mapping coordinates in SIFTS (for chain "+sequenceId+")." +
							" This is likely to be a problem with Uniref100 clustering. Sequence length is "+query.getSequence().length()+
							", SIFTS mapping is "+interv.beg+"-"+interv.end);

					// For any other case we check that our mappings coincide and warn if not
				} else {
					
					Interval ourInterv = pdbToUniProtMapper.getMatchingIntervalUniProtCoords();
					
					if (ourInterv.beg!=interv.beg || ourInterv.end!=interv.end ) {
						
						LOGGER.warn("The SIFTS mapping does not coincide with our mapping for chain "+sequenceId+", UniProt id "+query.getUniId()+
							": ours is "+
							ourInterv.beg+"-"+ourInterv.end+
							", SIFTS is "+interv.beg+"-"+interv.end);
					}
				}
			}
		}
		
	}
	
	/**
	 * Checks the PDB to UniProt mapping obtained from the SIFTS resource and 
	 * returns a single UniProt id decided to be the valid mapping. 
	 * If any of the intervals is of 0 or negative length the mapping is rejected.
	 * 
	 * Subcases:
	 *  - more than one UniProt id: chimera case. 
	 *    If allow chimeras is true then the longest length match will be used
	 *  - one UniProt id but several segments: deletion or insertion case.
	 *    All deletions are allowed, insertions are only allowed if below
	 *    EppicParams.NUM_GAP_RES_FOR_CHIMERIC_FUSION 
	 *     
	 * @param mappings
	 * @param allowChimeras
	 * @return the UniProt id decided to be the valid mapping or null if mapping is rejected
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
			LOGGER.warn("Negative intervals present in SIFTS mapping for chain "+sequenceId+". Will not use the SIFTS mapping.");
			queryWarnings.add("Problem with SIFTS PDB to UniProt mapping: negative intervals present in mapping");
			// We continue with a null query, i.e. no match, of course we could try to still find a mapping ourselves
			// by calling findUniprotMapping if we return a null, but for the chimera case below there's also null returned
			// and we don't really want to search again in that case
			// As of Oct 2013 there is a bug in the SIFTS pipeline that produces some negative mappings, e.g. 4iar 
			return null;
		}
		// second sanity check: negative or 0 coordinates are surely errors: reject
		if (hasNegatives) {
			LOGGER.warn("0 or negative coordinates present in SIFTS mapping for chain "+sequenceId+". Will not use the SIFTS mapping.");
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
			
			LOGGER.warn("More than one UniProt SIFTS mapping for chain "+sequenceId);
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
				String warning = "More than one UniProt id correspond to chain "+sequenceId+": ";
				for (String uniId:uniqUniIds.keySet()){
					warning+=(uniId+" (total coverage "+uniqUniIds.get(uniId).totalCoverage+") ");
				}
				warning+=". This is most likely a chimeric chain";
				queryWarnings.add(warning);

				return null;
			}

		// b) 1 uniprot id but with more than 1 mapping: deletions or insertions in the construct 
		} else if (mappings.size()>1) {			
			boolean unacceptableGaps = false;
			String msg = "";
			
			// common logging for all deletion/insertion cases
			LOGGER.warn("The SIFTS PDB-to-UniProt mapping of chain "+sequenceId+
					" is composed of several segments of a single UniProt sequence ("+mappings.iterator().next().getUniprotId()+")");
			
			int lastUniprotEnd = -1;
			int lastPdbEnd = -1;			
			
			for (SiftsFeature sifts:mappings) {
				// checking for the gaps between the PDB segments: insertions in PDB sequence with respect to UniProt
				Interval pdbInterv = sifts.getCifIntervalSet().iterator().next();
				if (lastPdbEnd!=-1 && 
					( (pdbInterv.beg-lastPdbEnd)>EppicParams.NUM_GAP_RES_FOR_CHIMERIC_FUSION ||
					  (pdbInterv.beg-lastPdbEnd)<0	)) {
					// if at least one of the gaps is long or an inversion then we consider the mappings unacceptable
					unacceptableGaps = true;
				}
				lastPdbEnd = pdbInterv.end;
				
				// checking for the gaps between the uniprot segments: deletions in PDB sequence with respect to UniProt
				Interval interv = sifts.getUniprotIntervalSet().iterator().next(); // there's always only 1 interval per SiftsFeature
				if ( lastUniprotEnd!=-1 && 
					 (interv.beg-lastUniprotEnd)<0 ) {
					// if at least 1 of the gaps is an inversion (e.g. circular permutants) then we consider the mappings unacceptable
					// Note: for this to work properly the intervals have to be sorted as they appear in the PDB chain (getMappings above sorts them)
					unacceptableGaps = true;
				} else if (lastUniprotEnd!=-1 && 
						   (interv.beg-lastUniprotEnd)>1) {
					// we only warn that deletion exists, we then proceed with the mapping unless unacceptableGaps goes to true elsewhere
					// esentially a deletion is like removing a domain from a terminal, so as we do allow removing domains from terminals
					// we should also allow this (see JIRA issue CRK-139)
					// e.g. 4cofA: a whole loop is deleted (and a few residues inserted to replace it too) 
					LOGGER.warn("Deletion of length "+(interv.beg-lastUniprotEnd)+" in sequence of PDB chain "+sequenceId+" with respect to its UniProt reference");
				}
				lastUniprotEnd = interv.end;
				
				// putting in the same loop the different mapping coordinates in the msg string for reporting below
				msg+=" "+pdbInterv.beg+"-"+pdbInterv.end+","+interv.beg+"-"+interv.end;
				
			}

			// common logging for all deletion/insertion cases
			LOGGER.warn("PDB chain "+sequenceId+" vs UniProt ("+
					mappings.iterator().next().getUniprotId()+") segments:"+msg);

			
			if (unacceptableGaps) {				
				// insertions exist and they are too long: reject mapping
				LOGGER.warn("Check if the PDB entry is biologically reasonable (likely to be an engineered entry). Won't do evolution analysis on this chain.");

				String warning = "More than one UniProt segment of id "+mappings.iterator().next().getUniprotId()+
						" correspond to chain "+sequenceId+":"+msg+
						". This is most likely an engineered chain";
				queryWarnings.add(warning);

				return null;
				
			} else {
				// two cases here: 
				// a) all deletions 
				// b) insertions exist but they are within the allowed margins: warn only and continue using the mapping
				//    (sometimes the gaps are of only 2-3 residues and it is a bit over the top to reject it plainly)
				//    for some cases the gaps are even 0 length! (I guess a SIFTS error) e.g. 2jdi chain D				
				LOGGER.warn("All segments are deletions or insertions are all below "+EppicParams.NUM_GAP_RES_FOR_CHIMERIC_FUSION+" residues. " +
						"Will anyway proceed with this UniProt reference");
				queryUniprotId = uniqUniIds.keySet().iterator().next();
			}
		// c) 1 uniprot id and 1 mapping: vanilla case
		} else {
			LOGGER.info("Getting UniProt reference mapping for chain "+sequenceId+" from SIFTS");
			queryUniprotId = uniqUniIds.keySet().iterator().next();
		}
		
		return queryUniprotId;
	}
	
	
	public void blastForHomologs(EppicParams params) 
			throws IOException, BlastException, InterruptedException, DaoException {
		
		
		File blastPlusBlastp = params.getBlastpBin();
		String blastDbDir = params.getBlastDbDir();
		String blastDb = params.getBlastDb();
		int blastNumThreads = params.getNumThreads(); 
		int maxNumSeqs = params.getMaxNumSeqs();
		HomologsSearchMode searchMode = params.getHomologsSearchMode();
		boolean useUniparc = params.isUseUniparc();			
		
		Interval queryInterv = pdbToUniProtMapper.getHomologsSearchInterval(searchMode);
		
		LOGGER.info("Homologs search mode: "+searchMode.getName());
		if (searchMode==HomologsSearchMode.GLOBAL) {
						
			LOGGER.info("Using full UniProt sequence {} {}-{} for blast search (entity {})",
					query.getUniId(), queryInterv.beg, queryInterv.end, entity.getMolId());

			searchWithFullUniprot = true;
		} else if (searchMode==HomologsSearchMode.LOCAL) {
						
			LOGGER.info("Using UniProt {} subsequence {}-{} for blast search (entity {})",
					query.getUniId(), queryInterv.beg, queryInterv.end, entity.getMolId());
			searchWithFullUniprot = false;			
		} 
		
		
		homologs = new HomologList(query, queryInterv);
		
		if (useUniparc) LOGGER.info("UniParc hits will be used");
		else LOGGER.info("UniParc hits won't be used");
		homologs.setUseUniparc(useUniparc);

		BlastHitList cachedHitList;
		if (params.getDbConfigFile()!=null) {
			cachedHitList = getCachedHspsForQuery(getQuery(), queryInterv);
		} else {
			LOGGER.info("Not using sequence search cache from db because the db config file was not supplied.");
			cachedHitList = null;
		}

		homologs.searchWithBlast(blastPlusBlastp, blastDbDir, blastDb, blastNumThreads, maxNumSeqs, cachedHitList, params.isNoBlast());
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
	 * @throws SQLException 
	 * @throws ServiceException
	 */
	public void retrieveHomologsData() throws IOException, SQLException, ServiceException {
		if (parent.isUseLocalUniprot()) {
			homologs.retrieveUniprotKBData(parent.getUniProtLocalConnection());
		} else {
			homologs.retrieveUniprotKBData(parent.getUniProtJapiConnection());
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
	
	public void applyIdentityCutoff(EppicParams params) throws IOException, InterruptedException, BlastException {

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

	public void align(EppicParams params) throws IOException, InterruptedException { 
		File clustaloBin = params.getClustaloBin();
		int nThreads = params.getNumThreads();
		
		// 3) alignment of the protein sequences
		File alnCacheFile = null;
		// beware we only try to use cache file if we are in default mode: uniparc=true, filter by domain=false
		if (params.getAlnCacheDir()!=null && params.isUseUniparc() && !params.isFilterByDomain()) {

			Interval interv = pdbToUniProtMapper.getHomologsSearchInterval(params.getHomologsSearchMode());
			String intervStr = "."+interv.beg+"-"+interv.end;
			
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
		
		homologs.computeAlignment(clustaloBin, nThreads, alnCacheFile);
	}
	
	public MultipleSequenceAlignment getAlignment() {
		return homologs.getAlignment();
	}
	
	/**
	 * Returns a list of the conservation scores values (only entropy in current implementation)
	 * The sequence to which the list refers is the reference UniProt one, 
	 * not the PDB SEQRES.   
	 * @return
	 */
	public List<Double> getConservationScores() {

		return homologs.getEntropies();

	}
	
	/**
	 * Compute the sequence entropies for all reference sequence (uniprot) positions
	 * @param alphabet
	 */
	public void computeEntropies(AAAlphabet alphabet) {
		homologs.computeEntropies(alphabet);
	}
	
	/**
	 * Calculates the evolutionary score for the given list of residues by summing up evolutionary
	 * scores per residue and averaging.
	 * If a residue does not have a mapping to the reference UniProt (i.e. aligns to a gap) 
	 * then it is ignored and not counted in the average. 
	 * @param residues
	 * @return the average evolutionary score for the given set 
	 * of residues or NaN if all residues do not have a mapping to the reference UniProt or 
	 * if the input residue list is empty.
	 */
	public double calcScoreForResidueSet(List<Group> residues) {
		double totalScore = 0.0;
		double totalWeight = 0.0;
		List<Double> conservScores = getConservationScores();
		for (Group res:residues){

			int queryPos = pdbToUniProtMapper.getUniProtIndexForPdbGroup(res, !isSearchWithFullUniprot()); 
			 
			if (queryPos!=-1) {   
				double weight = 1.0;
				totalScore += weight*(conservScores.get(queryPos-1));
				totalWeight += weight;
			} else {
				// ignore it
			}

		}
		return totalScore/totalWeight;
	}
	
	/**
	 * Set the b-factors of the given pdb chain to conservation score values.
	 * @param chain
	 */
	protected void setConservationScoresAsBfactors(Chain chain) {
		
		// do nothing (i.e. keep original b-factors) if there's no query match for this sequence and thus no evol scores calculated 
		if (!hasQueryMatch()) return;
		
		List<Double> conservationScores = getConservationScores();
		
		for (Group residue:chain.getAtomGroups()) {
			
			if (residue.isWater()) continue;
			
			int queryPos = pdbToUniProtMapper.getUniProtIndexForPdbGroup(residue, !isSearchWithFullUniprot());
			
			if (queryPos!=-1) { 
				for (Atom atom:residue.getAtoms()) {
					atom.setTempFactor(conservationScores.get(queryPos-1).floatValue());
				}	
			} else {
				// when no entropy info is available for a residue we still want to assign a value for it
				// or otherwise the residue would keep its original real bfactor and then possibly screw up the
				// scaling of colors for the rest
				// The most sensible value we can use is the max entropy so that it looks like a poorly conserved residue
				double maxEntropy = Math.log(this.homologs.getReducedAlphabet().getNumLetters())/Math.log(2);
				LOGGER.info("Residue {} ({}) of chain {} has no entropy value associated to it, will set its b-factor to max entropy ({})",
						residue.getResidueNumber().toString(), residue.getPDBName(), residue.getChainId(), maxEntropy);
				for (Atom atom:residue.getAtoms()) {
					atom.setTempFactor((float)maxEntropy);
				}	
			}
		}
	}
	
	public int getNumHomologs() {
		if (homologs==null) return 0;
		return homologs.getSizeFilteredSubset();
	}
	
	public String getRepresentativeChainCode() {
		return sequenceId;
	}
	
	public String getPdbSequence() {
		return sequence;
	}
	

	
	private String findUniprotMapping(File blastPlusBlastp, String blastDbDir, String blastDb, int blastNumThreads, double pdb2uniprotIdThreshold, double pdb2uniprotQcovThreshold, boolean useUniparc) 
			throws IOException, BlastException, InterruptedException {
		// for too short sequence it doesn't make sense to blast
		if (this.sequence.length()<EppicParams.MIN_SEQ_LENGTH_FOR_BLASTING) {
			LOGGER.info("Chain "+sequenceId+" too short for blasting. Won't try to find a UniProt reference for it.");
			// query warnings for peptides (with a higher cutoff than this) are already logged before, see above
			// note that then as no reference is found, there won't be blasting for homologs either
			return null;
		}
		// we have to skip sequences composed of all unknown/non-standard (all Xs), blast fails otherwise
		Matcher m = ALLX_SEQ_PATTERN.matcher(this.sequence);
		if (m.matches()) {
			LOGGER.info("Chain "+sequenceId+" is composed only of unknown or non-standard aminoacids. Won't try to find a UniProt reference for it.");
			queryWarnings.add("Sequence is composed only of unknown or non-standard aminoacids, cannot find a UniProt reference for it");
			return null;
		}
		
		File outBlast = File.createTempFile(BLAST_BASENAME,BLASTOUT_SUFFIX);
		File inputSeqFile = File.createTempFile(BLAST_BASENAME,FASTA_SUFFIX);
		if (!DEBUG) {
			outBlast.deleteOnExit();
			inputSeqFile.deleteOnExit();
		}
		
		new Sequence("chain"+sequenceId,this.sequence).writeToFastaFile(inputSeqFile);

		BlastRunner blastRunner = new BlastRunner(blastDbDir);
		blastRunner.runBlastp(blastPlusBlastp, inputSeqFile, blastDb, outBlast, BlastRunner.BLASTPLUS_XML_OUTPUT_TYPE, BLAST_NO_FILTERING, blastNumThreads, 500);

		BlastHitList blastList = null;
		try {
			// note that by setting second parameter to true we are ignoring the DTD url to avoid unnecessary network connections
			BlastXMLParser blastParser = new BlastXMLParser(outBlast, true);
			blastList = blastParser.getHits();
		} catch (SAXException e) {
			// if this happens it means that blast doesn't format correctly its XML, i.e. has a bug
			LOGGER.error("Unexpected error while parsing blast XML output: ",e);
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
					LOGGER.warn("No UniProt match could be found for the query chain "+sequenceId+
							": no blast hit was a UniProt match (total "+blastList.size()+" blast hits)");
					queryWarnings.add("Blast didn't find a UniProt match for the chain. All blast hits were non-UniProt matches (total "+blastList.size()+" blast hits).");
					return null;
				}
			}
			BlastHsp bestHsp = best.getMaxScoringHsp();
			if ((bestHsp.getQueryPercentIdentity()/100.0)>pdb2uniprotIdThreshold && bestHsp.getQueryCoverage()>pdb2uniprotQcovThreshold) {
				uniprotMapping = getDeflineAccession(best);
				LOGGER.info("Blast found UniProt id "+uniprotMapping+" as best hit with "+
						String.format("%5.2f%% id and %4.2f coverage",bestHsp.getQueryPercentIdentity(),bestHsp.getQueryCoverage()));
			} else {
				LOGGER.warn("No UniProt match could be found with blast for the query chain "+sequenceId+" within cutoffs "+
						String.format("%5.2f%% id and %4.2f coverage",pdb2uniprotIdThreshold,pdb2uniprotQcovThreshold));
				LOGGER.warn("Best match was "+best.getSubjectId()+", with "+
						String.format("%5.2f%% id and %4.2f coverage",bestHsp.getQueryPercentIdentity(),bestHsp.getQueryCoverage()));
				LOGGER.info("Alignment of best UniProt match found by blast: \n"+bestHsp.getAlignment().getFastaString(null, true));
				queryWarnings.add("Blast didn't find a UniProt match for the chain. Best match was "+getDeflineAccession(best)+", with "+
						String.format("%5.2f%% id and %4.2f coverage",bestHsp.getQueryPercentIdentity(),bestHsp.getQueryCoverage()));
			}			
		} else {
			LOGGER.warn("No UniProt match could be found for the query chain "+sequenceId+". Blast returned no hits.");
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
					LOGGER.info("Blast hit "+uniId+" is a UniProt isoform id. Skipping it");
					return false;
				} else if (uniId.startsWith("UPI")) {
					LOGGER.info("Blast hit "+uniId+" is a UniParc id. Skipping it");
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
	
	public PdbToUniProtMapper getPdbToUniProtMapper() {
		return pdbToUniProtMapper;
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
	 * get the interval with {@link PdbToUniProtMapper#getHomologsSearchInterval(HomologsSearchMode)}
	 * @return
	 */
	public boolean isSearchWithFullUniprot() {
		return searchWithFullUniprot;
	}
	
	/**
	 * Returns the actual identity cut-off applied after calling {@link #applyIdentityCutoff(EppicParams)}
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

	/**
	 * Retrieve the BlastHitList from the sequence search cache in db.
	 * @param query the query
	 * @param queryInterv the query interval in uniprot 1-based coordinates
	 * @return
	 * @throws DaoException if something goes wrong when getting data from db
	 * @since 3.2.0
	 */
	private static BlastHitList getCachedHspsForQuery(UnirefEntry query, Interval queryInterv) throws DaoException {

		String queryId = query.getUniId() + "_" + queryInterv.beg + "-" + queryInterv.end;

		HitHspDAO dao = new HitHspDAOJpa();

		List<HitHsp> hitHsps = dao.getHitHspsForQueryId(queryId);

		if (hitHsps.isEmpty()) {
			LOGGER.warn("Nothing found in sequence search cache for query {}", queryId);
		}

		BlastHitList hitList = new BlastHitList();

		int i = 0;
		for (HitHsp hsp : hitHsps) {

			if (i==0) {
				hitList.setDb(hsp.getDb());
				hitList.setQueryId(queryId);
				hitList.setQueryLength(queryInterv.end - queryInterv.beg + 1);
			}

			BlastHit hit;
			if (hitList.contains(hsp.getSubjectId())) {
				hit = hitList.getHit(hsp.getSubjectId());
			} else {
				hit = new BlastHit();
				hit.setQueryId(queryId);
				hit.setSubjectId(hsp.getSubjectId());
				// query length is needed for query coverage
				hit.setQueryLength(hitList.getQueryLength());

				// TODO review if subject length and subject def are needed

				hitList.add(hit);
			}

			BlastHsp blastHsp = new BlastHsp(hit);
			blastHsp.setAliLength(hsp.getAliLength());
			blastHsp.setEValue(hsp.geteValue());
			blastHsp.setQueryStart(hsp.getQueryStart());
			blastHsp.setQueryEnd(hsp.getQueryEnd());
			blastHsp.setSubjectStart(hsp.getSubjectStart());
			blastHsp.setSubjectEnd(hsp.getSubjectEnd());
			blastHsp.setScore(hsp.getBitScore());
			blastHsp.setIdentities((int)(hsp.getPercentIdentity()*hsp.getAliLength()));

			hit.addHsp(blastHsp);

			i++;
		}

		return hitList;
	}
	

	
}
