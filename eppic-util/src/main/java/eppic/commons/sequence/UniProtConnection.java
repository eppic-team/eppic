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
 * Our interface to the Uniprot REST API.
 * 
 * @author Henning Stehr
 * @author Jose Duarte
 */
public class UniProtConnection {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UniProtConnection.class);
	
	/**
	 * Waiting time (in seconds) between retries
	 */
	private static final int RETRY_INTERVAL = 10;

	/**
	 * Maximum number of retries
	 */
	private static final int MAX_NUM_RETRIES = 5;

	private static final String UNIPROT_ENDPOINT = "https://www.ebi.ac.uk/proteins/api/proteins/";
	private static final String UNIPARC_ENDPOINT = "https://www.ebi.ac.uk/proteins/api/uniparc/upi/";

	private static final String UNIPROT_WEB_API_ENDPOINT = "https://www.uniprot.org/uniprot/";
	
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
	 * @throws NoMatchFoundException if no match returned by UniProt REST API
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
		JsonNode lineage = organism.get("lineage");
		List<String> taxons = new ArrayList<>();
		entry.setTaxons(taxons);
		for (JsonNode oneLineage : lineage) {
			taxons.add(oneLineage.asText());
		}

		return entry;
	}
	
	/**
	 * Retrieve a single Uniparc entry from UniProt REST API.
	 * @param uniparcId the uniparc id (starts with "UPI")
	 * @return
	 * @throws NoMatchFoundException
	 * @throws IOException
	 */
	public UniprotEntry getUniparcEntry(String uniparcId) throws NoMatchFoundException, IOException {

		// Proteins API: https://www.ebi.ac.uk/proteins/api/uniparc/upi/UPI00000217E5

		String request = UNIPARC_ENDPOINT + uniparcId;
		Response response = getServiceResponse(request);

		JsonNode node = objectMapper.readValue(response.readEntity(String.class), JsonNode.class);
		JsonNode accession = node.get("accession");
		String uniId = accession.asText();

		if (!uniId.equals(uniparcId)) {
			throw new IOException("Returned id ("+uniId+") is different from request id ("+uniparcId+") for request " + request);
		}

		JsonNode seqNode = node.get("sequence").get("content");
		UniprotEntry entry = new UniprotEntry(uniparcId);
		entry.setUniprotSeq(new Sequence(uniparcId, seqNode.asText()));

		return entry;
	}

	/**
	 * Convenience method to get a {@link UnirefEntry} object from UniProt REST API
	 * given a UniProt id.
	 * Essentially this is the same as {@link #getEntry(String)}, just differing in the type of
	 * the returned object (keeping it like that for backwards compatibility only. In theory we only need getEntry)
	 * @param uniProtId the UniProt id, it must not be prefixed by a Uniref prefix, e.g. "UniRef100_"
	 * @return
	 * @throws NoMatchFoundException if no match returned by UniProt REST API
	 * @throws IOException if problems getting the entry
	 */
	public UnirefEntry getUnirefEntry(String uniProtId) throws NoMatchFoundException, IOException {

		UniprotEntry entry = getEntry(uniProtId);
		String sequence = entry.getUniprotSeq().getSeq();

		int ncbiTaxId = entry.getTaxId();
		List<String> taxons = new ArrayList<>(entry.getTaxons());
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
	 * @throws NoMatchFoundException if no match returned by UniProt REST API
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
				return getUnirefEntry(uniProtId);
			} catch (IOException e) {
				LOGGER.warn("Got IOException while retrieving {} on attempt {}.", uniProtId, i);

			}
		}

		// after MAX_NUM_RETRIES, we got exceptions in all, give up and throw exception
		throw new IOException("Could not retrieve "+uniProtId+" from UniProt REST API after "+MAX_NUM_RETRIES+" attempts. Giving up");
	}
	
	/**
	 * Convenience method to get a List of {@link UnirefEntry}s given a List of uniprot ids.
	 * Analogous to {@link #getUnirefEntry(String)} but for multiple entries.
	 * If the REST API does not return all requested ids a warning is logged and the list of non-returned
	 * ids can be retrieved through {@link #getNonReturnedIdsLastMultipleRequest()}
	 * @param uniprotIds
	 * @return
	 * @throws IOException
	 * @throws IOException if problems getting the entries
	 */
	public List<UnirefEntry> getMultipleUnirefEntries(List<String> uniprotIds) throws IOException {
		
		List<UnirefEntry> unirefEntries = new ArrayList<>();

		for (String uniprotId:uniprotIds) {
			try {
				UnirefEntry entry = getUnirefEntry(uniprotId);
				unirefEntries.add(entry);
			} catch (NoMatchFoundException e) {
				// nothing to do here... below we check the ones that fail
			}
		}
		
		// now we check if the query to uniprot API did really return all requested uniprot ids
	    HashSet<String> returnedUniIds = new HashSet<>();
	    for (UnirefEntry uniref:unirefEntries) {
			returnedUniIds.add(uniref.getUniprotId());
	    }
	    nonReturnedIdsLastMultipleRequest = new HashSet<>();
	    for (String uniprotId:uniprotIds){
	    	if (!returnedUniIds.contains(uniprotId)) {
	    		nonReturnedIdsLastMultipleRequest.add(uniprotId);
	    		LOGGER.warn("Information for uniprot ID "+uniprotId+" could not be retrieved with the Uniprot REST API.");
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
		String url = UNIPROT_WEB_API_ENDPOINT + "P12345.fasta";
		try {
			Response response = getServiceResponse(url);
			return response.getHeaderString("X-UniProt-Release");
		} catch (NoMatchFoundException e) {
			throw new IOException("Could not find entry P12345 at URL '" + url + "' in order to find UniProt version");
		}
	}
	
	/**
	 * Stops the connection
	 */
	public void close() {
		client.close();
	}

	private Response getServiceResponse(String uri) throws IOException, NoMatchFoundException {
		Response response = client.target(uri)
				.request(MediaType.APPLICATION_JSON)
				.get();
		if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			throw new NoMatchFoundException("Response status for " + uri + " was " + response.getStatus());
		}
		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			throw new IOException("Response status for " + uri + " was " + response.getStatus());
		}
		return response;
	}

}
