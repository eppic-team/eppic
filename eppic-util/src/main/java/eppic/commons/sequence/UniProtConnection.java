package eppic.commons.sequence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
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

	/**
	 * Maximum number of accessions that can be asked for in a single request to the
	 * Proteins API. This is the limit the API itself imposes on the accession parameter.
	 */
	private static final int MAX_ACCESSIONS_PER_REQUEST = 100;

	private static final String UNIPROT_ENDPOINT = "https://www.ebi.ac.uk/proteins/api/proteins/";
	/**
	 * The same resource as {@link #UNIPROT_ENDPOINT}, in its search form, which takes a
	 * comma separated list of accessions and answers with an array of entries.
	 */
	private static final String UNIPROT_BATCH_ENDPOINT = "https://www.ebi.ac.uk/proteins/api/proteins";
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
		UniprotEntry entry = parseEntry(node);
		if (!entry.getUniId().equals(uniProtId)) {
			throw new IOException("Returned id ("+entry.getUniId()+") is different from request id ("+uniProtId+") for request " + request);
		}

		return entry;
	}

	/**
	 * Parse one entry of the Proteins API JSON representation, be it the whole response
	 * of a single accession request or one element of a batch request's array.
	 * @param node the JSON node of a single entry
	 * @return the parsed entry
	 */
	private UniprotEntry parseEntry(JsonNode node) {
		JsonNode accession = node.get("accession");
		String uniId = accession.asText();
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
	 * Retrieve several entries in a single request to the Proteins API.
	 * <p>
	 * Accessions that the API does not know about are simply absent from the returned
	 * map: unlike the single accession request, a batch request does not answer 404 for
	 * them (unless none of them is found at all, which is handled here as an empty map).
	 * @param uniProtIds the UniProt ids, at most {@value #MAX_ACCESSIONS_PER_REQUEST}
	 * @return the entries found, keyed by accession
	 * @throws IOException if problems getting the entries
	 * @throws IllegalArgumentException if more than {@value #MAX_ACCESSIONS_PER_REQUEST} ids are given
	 */
	public Map<String, UniprotEntry> getEntries(List<String> uniProtIds) throws IOException {

		// Proteins API: https://www.ebi.ac.uk/proteins/api/proteins?accession=P12345,P67890

		if (uniProtIds.size() > MAX_ACCESSIONS_PER_REQUEST) {
			throw new IllegalArgumentException("Can't request more than " + MAX_ACCESSIONS_PER_REQUEST +
					" accessions in one request, " + uniProtIds.size() + " were given");
		}

		Map<String, UniprotEntry> entries = new LinkedHashMap<>();
		if (uniProtIds.isEmpty()) {
			return entries;
		}

		String request = UNIPROT_BATCH_ENDPOINT + "?size=" + MAX_ACCESSIONS_PER_REQUEST +
				"&accession=" + String.join(",", uniProtIds);
		Response response;
		try {
			response = getServiceResponse(request);
		} catch (NoMatchFoundException e) {
			// none of the accessions in this batch is known to the API
			LOGGER.warn("None of the {} accessions of the batch was found by the Proteins API", uniProtIds.size());
			return entries;
		}

		JsonNode node = objectMapper.readValue(response.readEntity(String.class), JsonNode.class);
		if (!node.isArray()) {
			throw new IOException("Expected an array in the response of request " + request);
		}
		for (JsonNode entryNode : node) {
			UniprotEntry entry = parseEntry(entryNode);
			entries.put(entry.getUniId(), entry);
		}

		return entries;
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

		return toUnirefEntry(getEntry(uniProtId), uniProtId);
	}

	private UnirefEntry toUnirefEntry(UniprotEntry entry, String uniProtId) {
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
	 * The maximum number of ids that {@link #getUnirefEntriesWithRetry(List)} accepts in
	 * one call, so that callers can chunk their input accordingly.
	 * @return the maximum batch size
	 */
	public static int getMaxAccessionsPerRequest() {
		return MAX_ACCESSIONS_PER_REQUEST;
	}

	/**
	 * As {@link #getUnirefEntry(String)} but for a batch of ids in a single request, and
	 * with retries every {@value #RETRY_INTERVAL} seconds up to a maximum of
	 * {@value #MAX_NUM_RETRIES}.
	 * <p>
	 * Ids that the API does not know about are absent from the returned map rather than
	 * signalled with an exception: with a batch there is no single answer to give.
	 * @param uniProtIds the UniProt ids, at most {@link #getMaxAccessionsPerRequest()}
	 * @return the entries found, keyed by UniProt id
	 * @throws IOException if all {@value #MAX_NUM_RETRIES} retries fail
	 */
	public Map<String, UnirefEntry> getUnirefEntriesWithRetry(List<String> uniProtIds) throws IOException {
		for (int i=1; i<=MAX_NUM_RETRIES; i++) {
			if (i!=1) {
				try {
					LOGGER.info("Waiting {} s before next retry", RETRY_INTERVAL);
					Thread.sleep(RETRY_INTERVAL * 1000);
				} catch (InterruptedException e1) {
					LOGGER.error("Got InterruptedException while retrying to retrieve a batch of {} ids. Will not retry more", uniProtIds.size());
					Thread.currentThread().interrupt();
					break;
				}
			}
			try {
				Map<String, UniprotEntry> entries = getEntries(uniProtIds);
				Map<String, UnirefEntry> unirefEntries = new LinkedHashMap<>();
				for (Map.Entry<String, UniprotEntry> e : entries.entrySet()) {
					unirefEntries.put(e.getKey(), toUnirefEntry(e.getValue(), e.getKey()));
				}
				return unirefEntries;
			} catch (IOException e) {
				LOGGER.warn("Got IOException while retrieving a batch of {} ids on attempt {}. Error: {}",
						uniProtIds.size(), i, e.getMessage());
			}
		}

		throw new IOException("Could not retrieve a batch of " + uniProtIds.size() +
				" ids from UniProt REST API after " + MAX_NUM_RETRIES + " attempts. Giving up");
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

		// in batches, because the API caps how many accessions one request can carry
		for (int i = 0; i < uniprotIds.size(); i += MAX_ACCESSIONS_PER_REQUEST) {
			List<String> batch = uniprotIds.subList(i, Math.min(i + MAX_ACCESSIONS_PER_REQUEST, uniprotIds.size()));
			// ids not known to the API are simply missing from the answer, and are
			// reported below as non returned ids
			unirefEntries.addAll(getUnirefEntriesWithRetry(batch).values());
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

		Response response;
		try {
			response = client.target(url)
					.request(MediaType.APPLICATION_JSON)
					.head();
		} catch (ProcessingException e) {
			throw new IOException("Could not connect to " + url + ": " + e.getMessage(), e);
		}
		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			throw new IOException("Could not get UniProt version from headers of url request: " + url);
		}
		return response.getHeaderString("X-UniProt-Release");

	}
	
	/**
	 * Stops the connection
	 */
	public void close() {
		client.close();
	}

	private Response getServiceResponse(String uri) throws IOException, NoMatchFoundException {
		Response response;
		try {
			response = client.target(uri)
					.request(MediaType.APPLICATION_JSON)
					.get();
		} catch (ProcessingException e) {
			// Jersey wraps transport level failures (connection reset, unexpected end of
			// file, timeouts) in this RuntimeException. Rethrowing as IOException is what
			// lets the retry loops and the callers' error counting see them at all.
			throw new IOException("Could not connect to " + uri + ": " + e.getMessage(), e);
		}
		if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			throw new NoMatchFoundException("Response status for " + uri + " was " + response.getStatus());
		}
		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			throw new IOException("Response status for " + uri + " was " + response.getStatus());
		}
		return response;
	}

}
