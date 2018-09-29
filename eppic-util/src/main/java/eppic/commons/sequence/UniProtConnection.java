package eppic.commons.sequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.kraken.interfaces.uniparc.UniParcEntry;
import uk.ac.ebi.kraken.interfaces.uniprot.NcbiTaxon;
import uk.ac.ebi.kraken.interfaces.uniprot.NcbiTaxonomyId;
import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntry;
import uk.ac.ebi.uniprot.dataservice.client.Client;
import uk.ac.ebi.uniprot.dataservice.client.QueryResult;
import uk.ac.ebi.uniprot.dataservice.client.ServiceFactory;
import uk.ac.ebi.uniprot.dataservice.client.exception.ServiceException;
import uk.ac.ebi.uniprot.dataservice.client.uniparc.UniParcService;
import uk.ac.ebi.uniprot.dataservice.client.uniprot.UniProtQueryBuilder;
import uk.ac.ebi.uniprot.dataservice.client.uniprot.UniProtService;
import uk.ac.ebi.uniprot.dataservice.query.Query;

/**
 * Our interface to the Uniprot Java API.
 * 
 * @author Henning Stehr
 * @author Jose Duarte
 */
public class UniProtConnection {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UniProtConnection.class);
	
	/**
	 * The maximum number of UniProt entries to fetch in one request, see {@link #getMultipleUnirefEntries(List)}
	 */
	private static final int MAX_ENTRIES_PER_REQUEST = 100;
	
	/*--------------------------- member variables --------------------------*/
	private UniProtService uniProtService;
	private UniParcService uniparcService;
	
	private HashSet<String> nonReturnedIdsLastMultipleRequest;
	
	/*----------------------------- constructors ----------------------------*/
	
	public UniProtConnection() {
		ServiceFactory serviceFactoryInstance = Client.getServiceFactoryInstance();
		

	    // Create UniProt query service
	    uniProtService = serviceFactoryInstance.getUniProtQueryService();
	    uniProtService.start();
	    uniparcService = serviceFactoryInstance.getUniParcQueryService();
	    uniparcService.start();
	    	    
	}
	
	/*---------------------------- public methods ---------------------------*/
	
	/**
	 * Primary method to retrieve a Uniprot entry by its ID. The entry object
	 * can be used for subsequent method calls or for functions provided by the
	 * Uniprot API for which we do not have our own implementation.
	 * @param uniProtId
	 * @return 
	 * @throws NoMatchFoundException if no match returned by UniProt JAPI
	 * @throws ServiceException if problems getting the entry
	 */
	public UniProtEntry getEntry(String uniProtId) throws NoMatchFoundException, ServiceException {
		UniProtEntry entry = uniProtService.getEntry(uniProtId);
		if (entry==null) throw new NoMatchFoundException("No UniProt entry found for UniProt id "+uniProtId);
		return entry;
	}
	
	/**
	 * Retrieve a single Uniparc entry from UniProt JAPI. 
	 * @param uniparcId
	 * @return
	 * @throws NoMatchFoundException
	 * @throws ServiceException
	 */
	public UniParcEntry getUniparcEntry(String uniparcId) throws NoMatchFoundException, ServiceException {
		UniParcEntry entry = uniparcService.getEntry(uniparcId);
		if (entry==null) throw new NoMatchFoundException("No Uniparc entry found for Uniparc id "+uniparcId);
		return entry;
	}

	/**
	 * Convenience method to get a {@link UnirefEntry} object from UniProt JAPI 
	 * given a uniprot id.
	 * The UnirefEntry object returned contains the uniprot id, tax id, taxons and sequence.
	 * The information returned with this object can be obtained by calling {@link #getEntry(String)} and
	 * then extracting the different parts of the info from the returned JAPI's UniProtEntry
	 * @param uniProtId
	 * @return
	 * @throws NoMatchFoundException if no match returned by UniProt JAPI
	 * @throws ServiceException if problems getting the entry
	 */
	public UnirefEntry getUnirefEntry(String uniProtId) throws NoMatchFoundException, ServiceException {
		List<String> taxons = new ArrayList<>();
		
		UniProtEntry entry = getEntry(uniProtId);
		String sequence = entry.getSequence().getValue();
		
		List<NcbiTaxonomyId> ncbiTaxIds = entry.getNcbiTaxonomyIds();
		if (ncbiTaxIds.size()>1) {
			LOGGER.warn("More than one taxonomy id for uniprot entry {}", uniProtId);
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
	 * Note the JAPI has some limit of number of entries per request, use a max of {@value #MAX_ENTRIES_PER_REQUEST}
	 * in the input list or things can go wrong.
	 * @param idsList a list of uniprot ids
	 * @return
	 * @throws ServiceException if problems getting the entries
	 */
	public QueryResult<UniProtEntry> getMultipleEntries(List<String> idsList) throws ServiceException {
	    Query query = UniProtQueryBuilder.accessions(new HashSet<>(idsList));
	    return uniProtService.getEntries(query); 
	}
	
	/**
	 * Convenience method to get a List of {@link UnirefEntry}s given a List of uniprot ids.
	 * Analogous to {@link #getUnirefEntry(String)} but for multiple entries.
	 * If the JAPI does not return all requested ids a warning is logged and the list of non-returned 
	 * ids can be retrieved through {@link #getNonReturnedIdsLastMultipleRequest()}
	 * @param uniprotIds
	 * @return
	 * @throws IOException
	 * @throws ServiceException if problems getting the entries 
	 */
	public List<UnirefEntry> getMultipleUnirefEntries(List<String> uniprotIds) throws IOException, ServiceException {
		
		List<UnirefEntry> unirefEntries = new ArrayList<UnirefEntry>();
		
		for (int i=0;i<uniprotIds.size();i+=MAX_ENTRIES_PER_REQUEST) {
			
			List<String> uniprotIdsChunk = new ArrayList<>();
			for (int c=i;c<i+MAX_ENTRIES_PER_REQUEST && c<uniprotIds.size();c++) {				
				uniprotIdsChunk.add(uniprotIds.get(c));
			}
		
			QueryResult<UniProtEntry> entries = getMultipleEntries(uniprotIdsChunk);
			
			while (entries.hasNext()) {
				
				UniProtEntry entry = entries.next();
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
		}

		
		
		// now we check if the query to uniprot JAPI did really return all requested uniprot ids
	    HashSet<String> returnedUniIds = new HashSet<>();
	    for (UnirefEntry uniref:unirefEntries) {
			returnedUniIds.add(uniref.getUniprotId());
	    }
	    nonReturnedIdsLastMultipleRequest = new HashSet<>();
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
	 * @throws ServiceException
	 */
	public String getVersion() throws ServiceException {
		return uniProtService.getServiceInfo().getReleaseNumber();
	}
	
	/**
	 * Stops the connection
	 */
	public void close() {
		if (uniProtService!=null) {
			uniProtService.stop();
		}
		if (uniparcService!=null) {
			uniparcService.stop();
		}
	}

}
