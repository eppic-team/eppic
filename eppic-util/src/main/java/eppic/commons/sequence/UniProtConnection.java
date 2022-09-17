package eppic.commons.sequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

	/**
	 * Waiting time (in seconds) between retries
	 */
	private static final int RETRY_INTERVAL = 30;

	/**
	 * Maximum number of retries
	 */
	private static final int MAX_NUM_RETRIES = 5;

	private static final String UNIPROT_ENDPOINT = "https://www.ebi.ac.uk/proteins/api/proteins/";
	private static final String UNIPARC_ENDPOINT = "https://www.ebi.ac.uk/proteins/api/uniparc/upi/";
	
	/*--------------------------- member variables --------------------------*/
	private final Client client;
	private final ObjectMapper objectMapper;
	private HashSet<String> nonReturnedIdsLastMultipleRequest;
	
	/*----------------------------- constructors ----------------------------*/
	
	public UniProtConnection() {

			ClientConfig clientConfig = new ClientConfig();
			//clientConfig.register(MultiPartFeature.class);

			client = ClientBuilder.newClient(clientConfig);

			objectMapper = new ObjectMapper();

	}
	
	/*---------------------------- public methods ---------------------------*/
	
	/**
	 * Primary method to retrieve a Uniprot entry by its ID. The entry object
	 * can be used for subsequent method calls or for functions provided by the
	 * Uniprot API for which we do not have our own implementation.
	 * @param uniProtId
	 * @return 
	 * @throws NoMatchFoundException if no match returned by UniProt JAPI
	 * @throws IOException if problems getting the entry
	 */
	public UniprotEntry getEntry(String uniProtId) throws NoMatchFoundException, IOException {

		// Proteins API: https://www.ebi.ac.uk/proteins/api/proteins/A0A0G2ZPK8

		String request = UNIPROT_ENDPOINT + uniProtId;
		Response response = getServiceResponse(request);

		JsonNode node = objectMapper.readValue(response.readEntity(String.class), JsonNode.class);
		JsonNode accession = node.get("accession");
		String uniId = accession.asText();
		if (!uniId.equals(uniProtId)) {
			throw new IOException("Returned id ("+uniId+") is different from request id ("+uniProtId+") for request " + request);
		}
		JsonNode organism = node.get("organism");
		JsonNode taxonomy = organism.get("taxonomy");
		JsonNode seqOuterNode = node.get("sequence");
		JsonNode seqInnerNode = seqOuterNode.get("sequence");
		String seq = seqInnerNode.asText();
		Sequence seqObj = new Sequence();
		seqObj.setSeq(seq);
		seqObj.setName(uniId);
		seqObj.setType(true);
		UniprotEntry entry = new UniprotEntry(uniId);
		entry.setUniprotSeq(seqObj);
		entry.setTaxId(taxonomy.asInt());
		// TODO set taxons

		if (entry==null) throw new NoMatchFoundException("No UniProt entry found for UniProt id "+uniProtId);
		return entry;
	}
	
	/**
	 * Retrieve a single Uniparc entry from UniProt JAPI. 
	 * @param uniparcId
	 * @return
	 * @throws NoMatchFoundException
	 * @throws IOException
	 */
	public UniParcEntry getUniparcEntry(String uniparcId) throws NoMatchFoundException, IOException {

		// Proteins API: https://www.ebi.ac.uk/proteins/api/uniparc/upi/UPI00000217E5

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
	 * @throws IOException if problems getting the entry
	 */
	public UnirefEntry getUnirefEntry(String uniProtId) throws NoMatchFoundException, IOException {
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
	 * As {@link #getUnirefEntry(String)} but with retries every {@value #RETRY_INTERVAL} seconds, up to a
	 * maximum of {@value #MAX_NUM_RETRIES}
	 * @param uniProtId the uniprot identifier
	 * @return
	 * @throws NoMatchFoundException if no match returned by UniProt JAPI
	 * @throws IOException if all {@value #MAX_NUM_RETRIES} retries result in ServiceExceptions
	 */
	public UnirefEntry getUnirefEntryWithRetry(String uniProtId) throws NoMatchFoundException, IOException {
		for (int i=1; i<=MAX_NUM_RETRIES; i++) {
			if (i!=1) {
				try {
					LOGGER.info("Waiting {} s before next retry", RETRY_INTERVAL);
					Thread.sleep(RETRY_INTERVAL * 1000);
				} catch (InterruptedException e1) {
					LOGGER.error("Got InterruptedException while retrying to retrieve {}. Will not retry more", uniProtId);
					break;
				}
			}
			try {
				UnirefEntry unirefEntry = getUnirefEntry(uniProtId);
				return unirefEntry;
			} catch (ServiceException e) {
				LOGGER.warn("Got ServiceException while retrieving {} on attempt {}.", uniProtId, i);

			}
		}

		// after MAX_NUM_RETRIES, we got exceptions in all, give up and throw exception
		throw new ServiceException("Could not retrieve "+uniProtId+" from UniProt JAPI after "+MAX_NUM_RETRIES+" attempts. Giving up");
	}
	
	/**
	 * Gets a list of Uniprot entries as an Iterator given a list of Uniprot identifiers.
	 * If any of the input entries can not be retrieved through JAPI then they will be
	 * missing in the returned iterator. The user must check for those.  
	 * Note the JAPI has some limit of number of entries per request, use a max of {@value #MAX_ENTRIES_PER_REQUEST}
	 * in the input list or things can go wrong.
	 * @param idsList a list of uniprot ids
	 * @return
	 * @throws IOException if problems getting the entries
	 */
	public QueryResult<UniProtEntry> getMultipleEntries(List<String> idsList) throws IOException {
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
	 * @throws IOException if problems getting the entries
	 */
	public List<UnirefEntry> getMultipleUnirefEntries(List<String> uniprotIds) throws IOException, IOException {
		
		List<UnirefEntry> unirefEntries = new ArrayList<>();
		
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
	 * @throws IOException
	 */
	public String getVersion() throws IOException {
		return uniProtService.getServiceInfo().getReleaseNumber();
	}
	
	/**
	 * Stops the connection
	 */
	public void close() {
		client.close();
	}

	private Response getServiceResponse(String uri) throws IOException {
		Response response = client.target(uri)
				.request(MediaType.APPLICATION_JSON)
				.get();
		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			throw new IOException("Response status for " + uri + " was " + response.getStatus());
		}
		return response;
	}

}
