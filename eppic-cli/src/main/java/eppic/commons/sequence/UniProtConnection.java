package eppic.commons.sequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.kraken.interfaces.uniprot.NcbiTaxon;
import uk.ac.ebi.kraken.interfaces.uniprot.NcbiTaxonomyId;
import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntry;
import uk.ac.ebi.kraken.interfaces.uniprot.features.BindingFeature;
import uk.ac.ebi.kraken.interfaces.uniprot.features.Feature;
import uk.ac.ebi.kraken.interfaces.uniprot.features.FeatureType;
import uk.ac.ebi.kraken.model.blast.JobStatus;
import uk.ac.ebi.kraken.model.blast.parameters.DatabaseOptions;
import uk.ac.ebi.kraken.uuw.services.remoting.EntryIterator;
import uk.ac.ebi.kraken.uuw.services.remoting.EntryRetrievalService;
import uk.ac.ebi.kraken.uuw.services.remoting.Query;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtJAPI;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtQueryBuilder;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtQueryService;
import uk.ac.ebi.kraken.uuw.services.remoting.blast.BlastData;
import uk.ac.ebi.kraken.uuw.services.remoting.blast.BlastInput;

/**
 * Our interface to the Uniprot Java API.
 * 
 * @author stehr
 */
public class UniProtConnection {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UniProtConnection.class);
	
	/*--------------------------- member variables --------------------------*/
	private EntryRetrievalService entryRetrievalService;
	private UniProtQueryService uniProtQueryService;
	
	private HashSet<String> nonReturnedIdsLastMultipleRequest;
	
	/*----------------------------- constructors ----------------------------*/
	
	public UniProtConnection() {
		// Create entry retrieval service
		entryRetrievalService = UniProtJAPI.factory.getEntryRetrievalService();
	    // Create UniProt query service
	    uniProtQueryService = UniProtJAPI.factory.getUniProtQueryService();
	}
	
	/*---------------------------- public methods ---------------------------*/
	
	/**
	 * Primary method to retrieve a Uniprot entry by its ID. The entry object
	 * can be used for subsequent method calls or for functions provided by the
	 * Uniprot API for which we do not have our own implementation.
	 * @param uniProtId
	 * @return 
	 * @throws NoMatchFoundException if no match returned by UniProt JAPI
	 */
	public UniProtEntry getEntry(String uniProtId) throws NoMatchFoundException {
		UniProtEntry entry = (UniProtEntry) entryRetrievalService.getUniProtEntry(uniProtId);
		if (entry==null) throw new NoMatchFoundException("No Uniprot entry found for uniprot id "+uniProtId);
		return entry;
	}

	/**
	 * Convenience method to get a owl.core.sequence.UnirefEntry object from UniProt JAPI 
	 * given a uniprot id.
	 * The UnirefEntry object returned contains the uniprot id, tax id, taxons and sequence.
	 * The information returned with this object can be obtained by calling {@link #getEntry(String)} and
	 * then extracting the different parts of the info from the returned JAPI's UniProtEntry
	 * @param uniProtId
	 * @return
	 * @throws NoMatchFoundException if no match returned by UniProt JAPI
	 */
	public UnirefEntry getUnirefEntry(String uniProtId) throws NoMatchFoundException {
		List<String> taxons = new ArrayList<String>();
		
		UniProtEntry entry = getEntry(uniProtId);
		String sequence = entry.getSequence().getValue();
		
		List<NcbiTaxonomyId> ncbiTaxIds = entry.getNcbiTaxonomyIds();
		if (ncbiTaxIds.size()>1) {
			LOGGER.warn("More than one taxonomy id for uniprot entry "+uniProtId);
		}
		int ncbiTaxId = Integer.parseInt(ncbiTaxIds.get(0).getValue());
		for (NcbiTaxon ncbiTax:entry.getTaxonomy()) {
			taxons.add(ncbiTax.getValue());
		}
		UnirefEntry uniref = new UnirefEntry();
		uniref.setUniprotId(uniProtId);
		uniref.setNcbiTaxId(ncbiTaxId);
		uniref.setTaxons(taxons);
		uniref.setSequence(sequence);
		return uniref;
	}
	
	/**
	 * Gets a list of Uniprot entries as an Iterator given a list of Uniprot identifiers.
	 * If any of the input entries can not be retrieved through JAPI then they will be
	 * missing in the returned iterator. The user must check for those.  
	 * @param idsList a list of uniprot ids
	 * @return
	 */
	public EntryIterator<UniProtEntry> getMultipleEntries(List<String> idsList) {
	    Query query = UniProtQueryBuilder.buildIDListQuery(idsList);
	    return uniProtQueryService.getEntryIterator(query);
	}
	
	/**
	 * Convenience method to get a List of owl.core.sequence.UnirefEntry given a List of uniprot ids.
	 * Analogous to {@link #getUnirefEntry(String)} but for multiple entries.
	 * If the JAPI does not return all requested ids a warning is logged and the list of non-returned 
	 * ids can be retrieved through {@link #getNonReturnedIdsLastMultipleRequest()}
	 * @param uniprotIds
	 * @return
	 * @throws IOException
	 */
	public List<UnirefEntry> getMultipleUnirefEntries(List<String> uniprotIds) throws IOException {
		
		List<UnirefEntry> unirefEntries = new ArrayList<UnirefEntry>();
		
		EntryIterator<UniProtEntry> entries = getMultipleEntries(uniprotIds);

		for (UniProtEntry entry:entries) {
			String uniId = entry.getPrimaryUniProtAccession().getValue();
			if (!uniprotIds.contains(uniId)) { // TODO this could be more efficient by using a Map, is it necessary?
				// this happens if the JAPI/server are really broken and return records that we didn't ask for (actually happened on the 09.02.2011!!!)
				throw new IOException("Uniprot JAPI server returned an unexpected record: "+uniId);
			}
			String sequence = entry.getSequence().getValue();

			List<NcbiTaxonomyId> ncbiTaxIds = entry.getNcbiTaxonomyIds();
			if (ncbiTaxIds.size()>1) {
				LOGGER.warn("More than one taxonomy id for uniprot entry "+uniId);
			}
			int ncbiTaxId = Integer.parseInt(ncbiTaxIds.get(0).getValue());
			
			List<String> taxons = new ArrayList<String>();
			for(NcbiTaxon ncbiTaxon:entry.getTaxonomy()) {
				taxons.add(ncbiTaxon.getValue());
			}
			UnirefEntry uniref = new UnirefEntry();
			uniref.setUniprotId(uniId);
			uniref.setNcbiTaxId(ncbiTaxId);
			uniref.setTaxons(taxons);
			uniref.setSequence(sequence);
			unirefEntries.add(uniref);
		}
		
		// now we check if the query to uniprot JAPI did really return all requested uniprot ids
	    HashSet<String> returnedUniIds = new HashSet<String>();
	    for (UnirefEntry uniref:unirefEntries) {
			returnedUniIds.add(uniref.getUniprotId());
	    }
	    nonReturnedIdsLastMultipleRequest = new HashSet<String>();
	    for (String uniprotId:uniprotIds){
	    	if (!returnedUniIds.contains(uniprotId)) {
	    		nonReturnedIdsLastMultipleRequest.add(uniprotId);
	    		LOGGER.warn("Information for uniprot ID "+uniprotId+" could not be retrieved with the Uniprot JAPI.");
	    	}
	    }
		
		return unirefEntries;
	}
	
	public HashSet<String> getNonReturnedIdsLastMultipleRequest() {
		return nonReturnedIdsLastMultipleRequest;
	}
	
	/**
	 * Return the UniProt version this connection is connected to
	 * @return a Uniprot version string
	 */
	public String getVersion() {
		return UniProtJAPI.factory.getVersion();
	}
	
	/**
	 * Use the Uniprot blast interface to find an entry by sequence.
	 * @param sequence the query sequence
	 * @return the uniprot entry with the best match to the query sequence
	 */
	public UniProtEntry getHumanEntryBySequence(String sequence) {
	    //Create a blast input with a Database and sequence
	    BlastInput input = new BlastInput(DatabaseOptions.UNIPROT_HUMAN, sequence);
	    //Submitting the input to the service will return a job id
	    String jobid = uniProtQueryService.submitBlast(input);
	    
	    System.out.print("Waiting for Blast result...");
	    
	    //Use this jobid to check the service to see if the job is complete
	    while (!(uniProtQueryService.checkStatus(jobid) == JobStatus.FINISHED)) {
		    try {
		      //Sleep a bit before the next request
		          System.out.print(".");
		          Thread.sleep(5000);
		    } catch (InterruptedException e) {
		          e.printStackTrace();
		    }
	    }
	    //The blast data contains the job information and the hits with entries
	    BlastData<UniProtEntry> blastResult = uniProtQueryService.getResults(jobid);
	    UniProtEntry bestHit = blastResult.getBlastHits().get(0).getEntry();
		String description = blastResult.getBlastHits().get(0).getHit().getDescription();
		long length = blastResult.getBlastHits().get(0).getHit().getLength();
		float seqId = blastResult.getBlastHits().get(0).getHit().getAlignments().get(0).getIdentity();
	    if(length != sequence.length()) {
	    	System.err.println("Warning: Blast hit is not full length");
	    }
	    if(seqId < 100) {
	    	System.err.println("Warning: Blast hit has sequence identity < 100%");
	    }
	    if(!sequence.equals(bestHit.getSequence().getValue())) {
	    	System.err.println("Warning: Retrieved sequence is not identical to query sequence");
	    }
	    System.out.println();
	    System.out.print("Found: ");
	    System.out.println(description);
	    //System.out.println("length = " + length);
	    //System.out.println("seqid = " + seqId);	    
		return bestHit;
	}
	
	/*---------------------------- static methods ---------------------------*/
	

	/**
	 * Returns the start location of an ATP binding site, or 0 if no such site is annotated in the entry.
	 */
	public static int getAtpBindingSite(UniProtEntry entry) {
		for(Feature f:entry.getFeatures(FeatureType.BINDING)) {
			BindingFeature f2 = (BindingFeature) f;
			if(f2.getFeatureDescription().getValue().startsWith("ATP")) {
				System.out.println("Found binding feature: " + f2.getFeatureDescription().getValue());
				System.out.println("Location: " + f2.getFeatureLocation().getStart() + "-" + f2.getFeatureLocation().getEnd());
				return f2.getFeatureLocation().getStart();
			}
		}
		return 0;
	}
	

}
