package eppic;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eppic.db.dao.DaoException;
import eppic.db.dao.HitHspDAO;
import eppic.db.dao.UniProtInfoDAO;
import eppic.db.dao.UniProtMetadataDAO;
import eppic.db.dao.mongo.HitHspDAOMongo;
import eppic.db.dao.mongo.UniProtInfoDAOMongo;
import eppic.db.dao.mongo.UniProtMetadataDAOMongo;
import eppic.model.db.HitHspDB;
import eppic.model.db.UniProtInfoDB;
import eppic.model.db.UniProtMetadataDB;
import eppic.model.db.UniProtRefWarningDB;
import eppic.model.db.UniProtRefWarningType;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.structure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.commons.blast.BlastException;
import eppic.commons.blast.BlastHit;
import eppic.commons.blast.BlastHitList;
import eppic.commons.blast.BlastHsp;
import eppic.commons.sequence.AAAlphabet;
import eppic.commons.sequence.MultipleSequenceAlignment;
import eppic.commons.sequence.NoMatchFoundException;
import eppic.commons.sequence.SiftsConnection;
import eppic.commons.sequence.SiftsFeature;
import eppic.commons.sequence.UnirefEntry;
import eppic.commons.util.Interval;


public class ChainEvolContext implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ChainEvolContext.class);
	
	private static final double ROUNDING_MARGIN = 0.0001;
	
	private static final Pattern ALLX_SEQ_PATTERN = Pattern.compile("^X+$");
	
	/**
	 * Prefix of the id given to the query when there is no UniProt reference and the chain's sequence is used as is
	 */
	public static final String QUERY_ID_PREFIX = "query_";

	
	// members 
	
	/**
	 * An identifier for the sequence, usually the chain id of the representative Chain
	 * It must uniquely identify the ChainEvolContext in a ChainEvolContextList
	 */
	private final String sequenceId;
	private final String sequence;
	
	private UnirefEntry query;							// the query (id,seq) for this chain: the UniProt reference or the chain's sequence itself
	private boolean hasQueryMatch;						// whether we have a query to do the evolutionary analysis with
	private boolean queryIsUniProt;						// whether the query is a UniProt reference (true) or the chain's own sequence (false)
	
	private PdbToUniProtMapper pdbToUniProtMapper;
	
	private double idCutoff;
	private double queryCov;
	
	private HomologList homologs;	// the homologs of this chain's sequence
		
	private boolean searchWithFullUniprot;  // mode of searching, true=we search with full uniprot seq, false=we search with PDB matching part only
	
	private final List<UniProtRefWarningDB> queryWarnings;
	
	private final ChainEvolContextList parent;
	
	private final boolean isProtein;
	
	private final EntityInfo entity;
	

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
		this.queryWarnings = new ArrayList<>();
		this.isProtein = true;
		this.entity = null;
	}
	
	/**
	 * Construct a ChainEvolContext from a Chain
	 * @param parent
	 * @param entityInfo
	 */
	public ChainEvolContext(ChainEvolContextList parent, EntityInfo entityInfo) {
		this.parent = parent;
		Chain chain = entityInfo.getRepresentative();
		
		this.sequenceId = chain.getName();
		this.hasQueryMatch = false;
		this.searchWithFullUniprot = true;
		this.queryWarnings = new ArrayList<>();
		
		this.isProtein = chain.isProtein();
		
		this.entity = entityInfo;
		
		this.pdbToUniProtMapper = new PdbToUniProtMapper(entityInfo);
		
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
	 * Finds the query sequence to use for the evolutionary analysis of this chain.
	 * For PDB entries whose SIFTS mapping can be used, the query is the mapped UniProt reference sequence.
	 * Otherwise (input from file, no SIFTS mapping found or SIFTS disabled) the chain's own sequence
	 * is taken at face value as the query, with no UniProt reference: see {@link #hasUniProtRef()}
	 * @throws IOException
	 */
	public void retrieveQueryData(EppicParams params) throws IOException {
		
		String siftsLocation = params.getSiftsFile();
		boolean useSifts = params.isUseSifts();
		
		String queryUniprotId = null;
		query = null;
		
		
		if (!isProtein) {
			// nothing to do if it is not protein: query will remain null and hasQueryMatch will remain false
			LOGGER.info("Chain {} is not a protein, will not do evolutionary analysis for it", sequenceId);
			queryWarnings.add(new UniProtRefWarningDB(UniProtRefWarningType.NOT_PROTEIN, "Chain is not a protein, no evolutionary analysis is performed for this chain"));
			return;
		}
		
		if (sequence.length()<=EppicParams.PEPTIDE_LENGTH_CUTOFF) {
			LOGGER.info("Chain {} is a peptide (length {}, below cutoff {}), will not do evolutionary analysis for it.", sequenceId, sequence.length(), EppicParams.PEPTIDE_LENGTH_CUTOFF);
			queryWarnings.add(new UniProtRefWarningDB(UniProtRefWarningType.PEPTIDE, "Chain is a peptide ("+sequence.length()+" residues)"));
			// since 3.2.0 we don't do evol analysis of peptides for several reasons
			// a) it's difficult to find sequence homologs, due to high evalues and needing to use special
			//    substitution matrices (see https://github.com/soedinglab/MMseqs2/issues/125)
			// b) eppic evol scoring doesn't make much sense for peptides, see https://github.com/eppic-team/eppic/issues/5
			//    See also discussion here: https://github.com/eppic-team/eppic-science/issues/120
			return;
		}
		
		// two possible cases: 
		// 1) PDB code known and so SiftsFeatures can be taken from SiftsConnection (unless useSifts is set to false)
		SiftsFeature siftsMappings = null;
		boolean useSequenceAsQuery = false;

		Interval pdbInterv = null;
		Interval uniProtInterv = null;
		
		if (!params.isInputAFile() || params.isUsePdbCodeFromFile()) {
			PdbId pdbId = parent.getPdb().getPdbId();
			if (useSifts) {
				if (pdbId == null) {
					LOGGER.info("Could not read a PDB code in file.");
					useSequenceAsQuery = true;
				} else {
					String pdbCode = pdbId.getId().toLowerCase();
					SiftsConnection siftsConn = parent.getSiftsConn(siftsLocation);
					try {
						siftsMappings = siftsConn.getMappings(pdbCode, sequenceId);

						Integer chosenSiftsFeatureIndex = checkSiftsMappings(siftsMappings, params.isAllowChimeras());
						if (chosenSiftsFeatureIndex != null) {
							queryUniprotId = siftsMappings.getUniprotIds().get(chosenSiftsFeatureIndex);
							pdbInterv = siftsMappings.getCifIntervalSet().get(chosenSiftsFeatureIndex);
							uniProtInterv = siftsMappings.getUniprotIntervalSet().get(chosenSiftsFeatureIndex);
						}

					} catch (NoMatchFoundException e) {
						LOGGER.warn("No SIFTS mapping could be found for "+pdbCode+sequenceId);
						useSequenceAsQuery = true;
					}
				}
				
			} else {
				LOGGER.info("Using SIFTS feature is turned off.");
				useSequenceAsQuery = true;
			}
		// 2) PDB code not known: there is no UniProt reference, the sequence is used as is
		} else {
			LOGGER.info("Input is from file, won't use SIFTS. "
					+ "Set the property USE_PDB_CODE_FROM_FILE to true if you want to change this behavior.");
			useSequenceAsQuery = true;
		}
		
		if (useSequenceAsQuery) {
			if (params.isNoBlast()) {
				// without blast the homologs can only come from the sequence search cache, which is keyed by UniProt reference
				LOGGER.info("No UniProt reference available for chain {} and blasting is disabled (-B option). "
						+ "Will not do evolutionary analysis for it.", sequenceId);
			} else {
				initQueryFromSequence(params);
			}
			return;
		}
		
		if (queryUniprotId!=null) hasQueryMatch = true;
		
		if (hasQueryMatch) {

            LOGGER.info("UniProt id for chain {}: {}", sequenceId, queryUniprotId);

			// once we have the identifier we get the data from uniprot
			try {
				if (parent.isUseLocalUniprot()) {
					UniProtInfoDAO dao = new UniProtInfoDAOMongo(MongoSettingsStore.getMongoSettings().getMongoDatabase());
					UniProtInfoDB uniProtInfo = dao.getUniProtInfo(queryUniprotId);
					if (uniProtInfo == null) {
						LOGGER.warn("No UniProt info could be found in local db for id {}. Will not be able to do evolutionary analysis for chain {}", queryUniprotId, sequenceId);
						query = null;
						hasQueryMatch = false;
					} else {
						query = new UnirefEntry();
						query.setUniprotId(queryUniprotId);
						query.setId(queryUniprotId); // TODO check if this is needed
						query.setSequence(uniProtInfo.getSequence());
						List<String> taxons = new ArrayList<>();
						taxons.add(uniProtInfo.getFirstTaxon());
						taxons.add(uniProtInfo.getLastTaxon());
						query.setTaxons(taxons);
					}
				} else {
					query = parent.getUniProtConnection().getUnirefEntryWithRetry(queryUniprotId);
				}
				
				if (query!=null && query.replaceNonStandardByX()) {
					// TODO for jaligner we needed to replace 'O' by 'X', with Biojava and I'm not sure if this is needed anymore...
                    LOGGER.warn("Replacing 'O' by 'X' in UniProt reference {}", query.getUniId());
				}

				if (query != null) {
					queryIsUniProt = true;
					// this initialises the alignment that maps PDB-to-UniProt,
					// if SIFTS mapping was found then intervals will be used (https://github.com/eppic-team/eppic/issues/188),
					// otherwise intervals will be false
					initQueryMapping(uniProtInterv, pdbInterv, params);
				}
				// the back-mapping to pdb is not working well yet, commented out
				//LOGGER.info("Our mapping in SIFTS tab format: "+pdbToUniProtMapper.getMappingSiftsFormat(sequenceId));
				//LOGGER.info("Our mapping in DBREF PDB format: "+pdbToUniProtMapper.getMappingDbrefFormat(sequenceId));

			}  catch (NoMatchFoundException e) {
				if (parent.isUseLocalUniprot()) {
                    LOGGER.warn("Couldn't find UniProt id {} (reference for chain {}) in local database. Obsolete?", queryUniprotId, sequenceId);
				} else {
                    LOGGER.warn("Couldn't find UniProt id {} (reference for chain {}) through UniProt REST API. Obsolete?", queryUniprotId, sequenceId);
				}				
				LOGGER.warn("Won't do evolution analysis for chain "+sequenceId);
				query = null;
				hasQueryMatch = false;
			} catch (IOException e) {
                LOGGER.warn("Could not retrieve the UniProt data for UniProt id {} from UniProt REST API, error: {}", queryUniprotId, e.getMessage());
				LOGGER.warn("Won't do evolution analysis for chain "+sequenceId);
				query = null;
				hasQueryMatch = false;								
			} catch (DaoException e) {
                LOGGER.warn("Could not retrieve the UniProt data for UniProt id {} from local database, error: {}", queryUniprotId, e.getMessage());
				LOGGER.warn("Won't do evolution analysis for chain "+sequenceId);
				query = null;
				hasQueryMatch = false;
			}

		}
		
	}

	/**
	 * Sets the query to the chain's own sequence, taken at face value, with no UniProt reference.
	 * The homologs are then searched for with that sequence and the evolutionary analysis is done on it.
	 * The PDB-to-reference mapping is then a mapping of the PDB sequence to itself.
	 * @param params
	 * @since 3.6.0
	 */
	private void initQueryFromSequence(EppicParams params) {
		// we have to skip sequences composed of all unknown/non-standard (all Xs), blast fails otherwise
		Matcher m = ALLX_SEQ_PATTERN.matcher(this.sequence);
		if (m.matches()) {
			LOGGER.info("Chain {} is composed only of unknown or non-standard aminoacids. Won't do evolutionary analysis for it.", sequenceId);
			queryWarnings.add(new UniProtRefWarningDB(UniProtRefWarningType.ALL_UNKNOWN_RESIDUES, "Sequence is composed only of unknown or non-standard aminoacids, no evolutionary analysis is performed for this chain"));
			return;
		}

		LOGGER.info("No UniProt reference for chain {}. Will use its sequence as is to search for homologs", sequenceId);

		query = new UnirefEntry();
		// the id serves as the query's tag in the sequence search and the alignment, so it must not clash with UniProt/UniParc ids
		String queryId = QUERY_ID_PREFIX + sequenceId;
		query.setId(queryId);
		query.setUniprotId(queryId);
		query.setSequence(this.sequence);
		if (query.replaceNonStandardByX()) {
			LOGGER.warn("Replacing 'O' by 'X' in query sequence of chain {}", sequenceId);
		}

		hasQueryMatch = true;
		queryIsUniProt = false;

		initQueryMapping(null, null, params);
	}

	/**
	 * Initialises the mapping of the PDB sequence(s) to the query sequence and checks that it can be used.
	 * If it can't, the query is discarded and hasQueryMatch set to false.
	 * @param queryInterv the interval in the query sequence of the SIFTS mapping, or null
	 * @param pdbInterv the interval in the PDB sequence of the SIFTS mapping, or null
	 * @param params
	 */
	private void initQueryMapping(Interval queryInterv, Interval pdbInterv, EppicParams params) {
		try {
			pdbToUniProtMapper.setUniProtReference(query, queryInterv, pdbInterv);
		} catch (CompoundNotFoundException e) {
			LOGGER.warn("Some unknown compounds in PDB or UniProt protein sequences, error: {}", e.getMessage());
			LOGGER.warn("Sequences: ");
			LOGGER.warn("PDB: {}",sequence);
			LOGGER.warn("Query ({}): {}",query.getUniId(), query.getSequence());
			query = null;
			hasQueryMatch = false;
			return;
		}

		String warning = pdbToUniProtMapper.checkAlignments();
		if (warning != null) {
			UniProtRefWarningDB warningDB = new UniProtRefWarningDB(UniProtRefWarningType.LOW_REFERENCE_IDENTITY, warning);
			if (queryIsUniProt) {
				warningDB.setUniProtIds(List.of(query.getUniId()));
			}
			queryWarnings.add(warningDB);
			query = null;
			hasQueryMatch = false;
			return;
		}

		int length = pdbToUniProtMapper.getHomologsSearchInterval(params.getHomologsSearchMode()).getLength();
		if (length <= EppicParams.PEPTIDE_LENGTH_CUTOFF) {
			// it can happen when the pdb length is above cutoff but the search length is below
			// e.g. 2mre_B, 3gj5_B
			LOGGER.warn("The search interval length {} is below the peptide length cutoff ({}). Will not do evolutionary analysis for chain {}", length, EppicParams.PEPTIDE_LENGTH_CUTOFF, sequenceId);
			queryWarnings.add(new UniProtRefWarningDB(UniProtRefWarningType.SEARCH_INTERVAL_TOO_SHORT,
					"Search interval length " + length + " for " + query.getId() +
					" is below the peptide length cutoff (" + EppicParams.PEPTIDE_LENGTH_CUTOFF + ")"));
			hasQueryMatch = false;
			query = null;
		}
	}
	
	/**
	 * Checks the PDB to UniProt mapping obtained from the SIFTS resource and 
	 * returns a single index of input siftsMapping corresponding to a single UniProt segment
	 * decided to be the valid mapping.
	 * If any of the intervals is of 0 or negative length the mapping is rejected (null returned)
	 * 
	 * Subcases:
	 *  - more than one UniProt id: chimera case. 
	 *    If allow chimeras is true then the longest length match will be used
	 *  - one UniProt id but several segments: deletion or insertion case. Mapping rejected since 3.2.0
	 *     
	 * @param siftsMapping a SiftsFeature containing all UniProt segments that map to a particular PDB chain
	 * @param allowChimeras if true and mapping chimeric, the largest coverage segment will be chosen as the mapping
	 * @return the index (in {@link SiftsFeature#getUniprotIds()}) in input SiftsFeature corresponding to
	 * the chosen segment mapping or null if the mapping is rejected.
	 */
	private Integer checkSiftsMappings(SiftsFeature siftsMapping, boolean allowChimeras) {
		
		//we do a sanity check on the SIFTS data: negative intervals are surely errors, we need to reject
		if (siftsMapping.hasNegativeLength()) {
            LOGGER.warn("Negative intervals present in SIFTS mapping for chain {}. Will not use the SIFTS mapping.", sequenceId);
			queryWarnings.add(new UniProtRefWarningDB(UniProtRefWarningType.SIFTS_NEGATIVE_INTERVALS, "Problem with SIFTS PDB to UniProt mapping: negative intervals present in mapping"));
			// We continue with a null query, i.e. no match, of course we could try to still find a mapping ourselves
			// by calling findUniprotMapping if we return a null, but for the chimera case below there's also null returned
			// and we don't really want to search again in that case
			// As of Oct 2013 there is a bug in the SIFTS pipeline that produces some negative mappings, e.g. 4iar 
			return null;
		}
		// second sanity check: negative or 0 coordinates are surely errors: reject
		if (siftsMapping.hasNegatives()) {
            LOGGER.warn("0 or negative coordinates present in SIFTS mapping for chain {}. Will not use the SIFTS mapping.", sequenceId);
			queryWarnings.add(new UniProtRefWarningDB(UniProtRefWarningType.SIFTS_NON_POSITIVE_COORDINATES, "Problem with SIFTS PDB to UniProt mapping: 0 or negative coordinates present in mapping"));
			// We continue with a null query, i.e. no match, of course we could try to still find a mapping ourselves
			// by calling findUniprotMapping if we return a null, but for the chimera case below there's also null returned
			// and we don't really want to search again in that case
			// As of Oct 2013 there is a bug in the SIFTS pipeline that produces some negative coordinates, e.g. 1s70 
			return null;			
		}

		Integer chosenSiftsFeature = null;
		
		// a) more than 1 uniprot id: chimeras
		if (siftsMapping.isChimeric()) {

			// since 3.2.0 we select the largest coverage segment instead of the largest total coverage over all
			// segments of the same uniprot id
			int largestCoverageUniprotIdSegmentIndex = siftsMapping.getLargestSegmentCoverageIndex();

            LOGGER.warn("More than one UniProt SIFTS mapping for chain {}", sequenceId);
			StringBuilder msg = new StringBuilder("UniProt IDs are: ");
			for (int i=0;i<siftsMapping.getUniprotIds().size();i++){
				msg.append(siftsMapping.getUniprotIds().get(i))
						.append(" (segment coverage ")
						.append(siftsMapping.getUniprotIntervalSet().get(i).getLength())
						.append(") ");
			}
			LOGGER.warn(msg.toString());
			
			if (allowChimeras) {
				LOGGER.warn("ALLOW_CHIMERAS mode is on. Will use longest segment coverage UniProt id as reference: {} (coverage: {})",
						siftsMapping.getUniprotIds().get(largestCoverageUniprotIdSegmentIndex),
						siftsMapping.getUniprotIntervalSet().get(largestCoverageUniprotIdSegmentIndex).getLength());
				chosenSiftsFeature = largestCoverageUniprotIdSegmentIndex;
			} else {
				LOGGER.warn("Chain {} is likely to be a chimeric chain. Won't do evolution analysis on this chain.", sequenceId);
				StringBuilder warning = new StringBuilder("More than one UniProt id correspond to chain " + sequenceId + ": ");
				for (String uniId:siftsMapping.getUniqueUniprotIds()){
					warning.append(uniId).append(" (total coverage ").append(siftsMapping.getUniProtLengthCoverage(uniId)).append(") ");
				}
				warning.append(". This is most likely a chimeric chain");
				UniProtRefWarningDB warningDB = new UniProtRefWarningDB(UniProtRefWarningType.CHIMERIC, warning.toString());
				warningDB.setUniProtIds(new ArrayList<>(siftsMapping.getUniqueUniprotIds()));
				queryWarnings.add(warningDB);

				return null;
			}

		// b) 1 uniprot id but with more than 1 mapping: deletions or insertions in the construct 
		} else if (siftsMapping.getUniprotIds().size()>1) {

			StringBuilder msg = new StringBuilder();
			
			// common logging for all deletion/insertion cases
            LOGGER.warn("The SIFTS PDB-to-UniProt mapping of chain {} is composed of several segments of a single UniProt sequence ({})", sequenceId, siftsMapping.getUniprotIds().get(0));

			for (int i=0 ; i<siftsMapping.getUniprotIds().size(); i++) {
				Interval pdbInterv = siftsMapping.getCifIntervalSet().get(i);
				Interval interv = siftsMapping.getUniprotIntervalSet().get(i);
				// putting in the same loop the different mapping coordinates in the msg string for reporting below
				msg.append(" ")
						.append(pdbInterv.beg).append("-").append(pdbInterv.end)
						.append(",")
						.append(interv.beg).append("-").append(interv.end);
			}

			// common logging for all deletion/insertion cases
            LOGGER.warn("PDB chain {} vs UniProt ({}) segments:{}", sequenceId, siftsMapping.getUniprotIds().get(0), msg);


			// since 3.2.0 we reject any insertion/deletion
			LOGGER.warn("Check if the PDB entry is biologically reasonable (likely to be an engineered entry). Won't do evolution analysis on this chain.");

			String warning = "More than one UniProt segment of id " + siftsMapping.getUniprotIds().get(0) +
					" correspond to chain " + sequenceId + ":" + msg +
					". This is most likely an engineered chain";
			UniProtRefWarningDB warningDB = new UniProtRefWarningDB(UniProtRefWarningType.MULTIPLE_UNIPROT_SEGMENTS, warning);
			warningDB.setUniProtIds(List.of(siftsMapping.getUniprotIds().get(0)));
			queryWarnings.add(warningDB);

			return null;

		// c) 1 uniprot id and 1 mapping: vanilla case
		} else {
            LOGGER.info("Getting UniProt reference mapping for chain {} from SIFTS", sequenceId);
			chosenSiftsFeature = 0; // only one index in SiftsFeature
		}
		
		return chosenSiftsFeature;
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

        LOGGER.info("Homologs search mode: {}", searchMode.getName());
		if (searchMode==HomologsSearchMode.GLOBAL) {
						
			LOGGER.info("Using full query sequence {} {}-{} for blast search (entity {})",
					query.getUniId(), queryInterv.beg, queryInterv.end, entity.getMolId());

			searchWithFullUniprot = true;
		} else if (searchMode==HomologsSearchMode.LOCAL) {
						
			LOGGER.info("Using query {} subsequence {}-{} for blast search (entity {})",
					query.getUniId(), queryInterv.beg, queryInterv.end, entity.getMolId());
			searchWithFullUniprot = false;			
		} 
		
		
		homologs = new HomologList(query, queryInterv);
		
		if (useUniparc) LOGGER.info("UniParc hits will be used");
		else LOGGER.info("UniParc hits won't be used");
		homologs.setUseUniparc(useUniparc);

		BlastHitList cachedHitList;
		if (!queryIsUniProt) {
			// the cache is keyed by UniProt reference, it can't have anything for a query without one
			cachedHitList = null;
		} else if (params.getDbConfigFile()!=null) {
			cachedHitList = getCachedHspsForQuery(getQuery(), queryInterv);
		} else {
			LOGGER.info("Not using sequence search cache from db because the db config file was not supplied.");
			cachedHitList = null;
		}

		homologs.searchWithBlast(blastPlusBlastp, blastDbDir, blastDb, blastNumThreads, maxNumSeqs, cachedHitList, params.isNoBlast());
        LOGGER.info("{} homologs found by blast (chain {})", homologs.getSizeFullList(), getRepresentativeChainCode());
		
		if (homologs.getSizeFullList()>0) {
			BlastHit lastBlastHit = homologs.getLast().getBlastHsp().getParent();
            LOGGER.info("Last blast hit (lowest score hit): {}, identity of its best HSP: {}", lastBlastHit.getSubjectId(), String.format("%5.2f%%", lastBlastHit.getMaxScoringHsp().getPercentIdentity()));
		}
		 
	}
	
	public void filterToSameDomainOfLife() {
		if (query.hasTaxons()) {
            LOGGER.info("Filtering to domain: {}", query.getFirstTaxon());
			homologs.filterToSameDomainOfLife();
            LOGGER.info("{} homologs after filtering to {} (chain {})", homologs.getSizeFilteredSubset(), query.getFirstTaxon(), getRepresentativeChainCode());
		} else {
            LOGGER.info("Taxons of chain {} are unknown, can't filter to same domain of life.", getRepresentativeChainCode());
		}
	}
	
	public void removeIdenticalToQuery(double minQueryCov) {
		homologs.removeIdenticalToQuery(minQueryCov);
	}
	
	/**
	 * Retrieves the Uniprot data and metadata
	 * @throws IOException
	 */
	public void retrieveHomologsData() throws DaoException, IOException, IOException {
		if (parent.isUseLocalUniprot()) {
			homologs.retrieveUniprotKBData();
		} else {
			homologs.retrieveUniprotKBData(parent.getUniProtConnection());
		}
	}
	
	public void applyHardIdentityCutoff(double homHardIdCutoff, double queryCovCutoff) {
		// note that we subtract just a tiny rounding margin (0.0001) in order to be sure that we do take anything
		// that's just at the hard cutoff
		// Otherwise in rare cases (e.g. 2os7 with hard cutoff of 0.4 in uniprot_2012_05, blast hit G5S5I8 is exactly at 40%)
		// we would not include an entry in this procedure but it would be included in the applyIdentityCutoff method below (which would 
		// then crash because there wouldn't be sequence data for it)
		homologs.filterToMinIdAndCoverage(homHardIdCutoff-ROUNDING_MARGIN, queryCovCutoff);
        LOGGER.info("{} homologs within the hard identity cutoff {} (chain {})", homologs.getSizeFilteredSubset(), String.format("%4.2f", homHardIdCutoff), getRepresentativeChainCode());
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

            LOGGER.info("{} homologs after applying {} identity cutoff and {} query coverage cutoff and before redundancy elimination (chain {})", homologs.getSizeFilteredSubset(), String.format("%4.2f", currentIdCutoff), String.format("%4.2f", queryCovCutoff), getRepresentativeChainCode());
			
			homologs.reduceRedundancy(maxNumSeqs, params.getMmseqsBin(), params.getTempCoordFilesDir(), params.getNumThreads());

			this.idCutoff = currentIdCutoff;
			
			if (homologs.getSizeFilteredSubset()>=minNumSeqs) break;

            LOGGER.info("Tried {} identity cutoff, only {} non-redundant homologs found ({} required) (chain {})", String.format("%4.2f", currentIdCutoff), homologs.getSizeFilteredSubset(), minNumSeqs, getRepresentativeChainCode());
			
			currentIdCutoff -= homIdStep;
		}
				
	}

	public void align(EppicParams params) throws IOException, InterruptedException { 
		File clustaloBin = params.getClustaloBin();
		int nThreads = params.getNumThreads();
		
		// 3) alignment of the protein sequences
		File alnCacheFile = null;
		// beware we only try to use cache file if we are in default mode: uniparc=true, filter by domain=false
		// and only for UniProt references: cache files are named after the query id
		if (params.getAlnCacheDir()!=null && queryIsUniProt && params.isUseUniparc() && !params.isFilterByDomain()) {

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
			} // else ignore it

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
	

	
	/**
	 * The query: the UniProt reference or, if there is none, the chain's own sequence (see {@link #hasUniProtRef()}).
	 * Use {@link #hasQueryMatch()} to check whether this can be called.
	 * @return
	 */
	public UnirefEntry getQuery() {
		return query;
	}
	
	public PdbToUniProtMapper getPdbToUniProtMapper() {
		return pdbToUniProtMapper;
	}
	
	/**
	 * Whether there is a query to do the evolutionary analysis with, either a UniProt reference
	 * or the chain's own sequence. See {@link #hasUniProtRef()}
	 * @return
	 */
	public boolean hasQueryMatch() {
		return hasQueryMatch;
	}

	/**
	 * Whether the query is a UniProt reference sequence. If false and {@link #hasQueryMatch()} is true,
	 * the query is the chain's own sequence, used as is.
	 * @return
	 */
	public boolean hasUniProtRef() {
		return hasQueryMatch && queryIsUniProt;
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
		return HomologList.CLUSTERING_ID;
	}
	
	/**
	 * Returns the warnings related to the query sequence matching. The chain cluster parent is not set.
	 * @return
	 */
	public List<UniProtRefWarningDB> getQueryWarnings() {
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
	private BlastHitList getCachedHspsForQuery(UnirefEntry query, Interval queryInterv) throws DaoException {

		String queryId = query.getUniId() + "_" + queryInterv.beg + "-" + queryInterv.end;

		HitHspDAO dao = new HitHspDAOMongo(MongoSettingsStore.getMongoSettings().getMongoDatabase());

		List<HitHspDB> hitHsps = dao.getHitHspsForQueryId(queryId);

		UniProtMetadataDAO uniDao = new UniProtMetadataDAOMongo(MongoSettingsStore.getMongoSettings().getMongoDatabase());

		UniProtMetadataDB uniProtMetadata = uniDao.getUniProtMetadata();

		if (hitHsps.isEmpty()) {
			LOGGER.warn("Nothing found in sequence search cache for query {}", queryId);
		}

		BlastHitList hitList = new BlastHitList();

		int i = 0;
		for (HitHspDB hsp : hitHsps) {

			if (i==0) {
				hitList.setDb(uniProtMetadata.getVersion());
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

				// note that subject length and subjectDef are not needed

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
