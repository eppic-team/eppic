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
	 * Maximum number of accessions asked for in a single batch request. The API takes
	 * more, but it pages its answer at 500, and smaller batches keep the cost of a
	 * failed batch (see {@link #getUnirefEntriesWithRetry(List)}) low.
	 */
	private static final int MAX_ACCESSIONS_PER_REQUEST = 100;

	private static final String REST_BASE = "https://rest.uniprot.org";

	/**
	 * A single UniProtKB entry: https://rest.uniprot.org/uniprotkb/P12345
	 */
	private static final String UNIPROT_ENDPOINT = REST_BASE + "/uniprotkb/";

	/**
	 * Several UniProtKB entries in one request:
	 * https://rest.uniprot.org/uniprotkb/accessions?accessions=P12345,P67890
	 * It answers with {"results": [...]}, leaving out the accessions it does not know
	 * about or that are inactive (deleted, merged, demerged).
	 */
	private static final String UNIPROT_BATCH_ENDPOINT = REST_BASE + "/uniprotkb/accessions";

	/**
	 * Only what we read in {@link #parseEntry(JsonNode)}. organism_name brings the whole
	 * organism object, which has taxonId and lineage (as a list of names, from the most
	 * general to the most specific). Without it the full entry (references, comments,
	 * features...) is sent, many times bigger.
	 */
	private static final String UNIPROT_FIELDS = "accession,organism_name,sequence";

	private static final String UNIPARC_ENDPOINT = REST_BASE + "/uniparc/";

	private static final String UNIPARC_FIELDS = "upi,sequence";

	/**
	 * Any small entry will do: we only read the release from the response headers.
	 */
	private static final String VERSION_URL = UNIPROT_ENDPOINT + "P12345?fields=accession";

	private static final String RELEASE_HEADER = "X-UniProt-Release";

	private static final String INACTIVE_ENTRY_TYPE = "Inactive";

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

		String request = UNIPROT_ENDPOINT + uniProtId + "?fields=" + UNIPROT_FIELDS;
		Response response = getServiceResponse(request);

		JsonNode node = objectMapper.readValue(response.readEntity(String.class), JsonNode.class);
		// unlike the Proteins API, which answered 404, inactive entries (deleted, merged,
		// demerged) come back as a stub with no sequence
		if (INACTIVE_ENTRY_TYPE.equals(node.path("entryType").asText())) {
			throw new NoMatchFoundException("UniProt entry " + uniProtId + " is inactive: " + node.path("inactiveReason"));
		}
		UniprotEntry entry = parseEntry(node);
		if (!entry.getUniId().equals(uniProtId)) {
			throw new IOException("Returned id ("+entry.getUniId()+") is different from request id ("+uniProtId+") for request " + request);
		}

		return entry;
	}

	/**
	 * Parse one entry of the UniProt REST API JSON representation, be it the whole
	 * response of a single accession request or one element of a batch request's results.
	 * @param node the JSON node of a single entry
	 * @return the parsed entry
	 * @throws IOException if the node lacks any of the fields we need
	 */
	private UniprotEntry parseEntry(JsonNode node) throws IOException {
		JsonNode accession = node.get("primaryAccession");
		JsonNode organism = node.get("organism");
		JsonNode seqNode = node.path("sequence").get("value");
		if (accession == null || organism == null || organism.get("taxonId") == null || seqNode == null) {
			throw new IOException("Unexpected UniProt entry JSON, missing accession, organism or sequence: " +
					node.toString().substring(0, Math.min(200, node.toString().length())));
		}
		String uniId = accession.asText();
		Sequence seqObj = new Sequence();
		seqObj.setSeq(seqNode.asText());
		seqObj.setName(uniId);
		seqObj.setType(true);
		UniprotEntry entry = new UniprotEntry(uniId);
		entry.setUniprotSeq(seqObj);
		entry.setTaxId(organism.get("taxonId").asInt());
		List<String> taxons = new ArrayList<>();
		entry.setTaxons(taxons);
		for (JsonNode oneLineage : organism.path("lineage")) {
			taxons.add(oneLineage.asText());
		}

		return entry;
	}

	/**
	 * Retrieve several entries in a single request to the UniProt REST API.
	 * <p>
	 * Accessions that the API does not know about, or that are inactive, are simply
	 * absent from the returned map.
	 * @param uniProtIds the UniProt ids, at most {@value #MAX_ACCESSIONS_PER_REQUEST}
	 * @return the entries found, keyed by accession
	 * @throws IOException if problems getting the entries. A malformed accession fails
	 * the whole batch with a 400
	 * @throws IllegalArgumentException if more than {@value #MAX_ACCESSIONS_PER_REQUEST} ids are given
	 */
	public Map<String, UniprotEntry> getEntries(List<String> uniProtIds) throws IOException {

		if (uniProtIds.size() > MAX_ACCESSIONS_PER_REQUEST) {
			throw new IllegalArgumentException("Can't request more than " + MAX_ACCESSIONS_PER_REQUEST +
					" accessions in one request, " + uniProtIds.size() + " were given");
		}

		Map<String, UniprotEntry> entries = new LinkedHashMap<>();
		if (uniProtIds.isEmpty()) {
			return entries;
		}

		String request = UNIPROT_BATCH_ENDPOINT + "?size=" + MAX_ACCESSIONS_PER_REQUEST +
				"&fields=" + UNIPROT_FIELDS +
				"&accessions=" + String.join(",", uniProtIds);
		Response response;
		try {
			response = getServiceResponse(request);
		} catch (NoMatchFoundException e) {
			// not expected (unknown accessions are left out of a 200), but harmless
			LOGGER.warn("None of the {} accessions of the batch was found by the UniProt REST API", uniProtIds.size());
			return entries;
		}

		JsonNode node = objectMapper.readValue(response.readEntity(String.class), JsonNode.class);
		JsonNode results = node.get("results");
		if (results == null || !results.isArray()) {
			throw new IOException("Expected a results array in the response of request " + request);
		}
		for (JsonNode entryNode : results) {
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

		String request = UNIPARC_ENDPOINT + uniparcId + "?fields=" + UNIPARC_FIELDS;
		Response response = getServiceResponse(request);

		JsonNode node = objectMapper.readValue(response.readEntity(String.class), JsonNode.class);
		String uniId = node.path("uniParcId").asText();

		if (!uniId.equals(uniparcId)) {
			throw new IOException("Returned id ("+uniId+") is different from request id ("+uniparcId+") for request " + request);
		}

		JsonNode seqNode = node.path("sequence").get("value");
		if (seqNode == null) {
			throw new IOException("No sequence in the response of request " + request);
		}
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
	 * <p>
	 * If the batch request keeps failing (or fails with a client error, e.g. a 400 for a
	 * malformed accession, which is not retried), the ids are requested one by one, once
	 * each, so that a single id the API chokes on does not take the other 99 down with
	 * it. Ids that fail on their own are left out of the returned map and logged.
	 * @param uniProtIds the UniProt ids, at most {@link #getMaxAccessionsPerRequest()}
	 * @return the entries found, keyed by UniProt id
	 * @throws IOException if the batch fails and so does every single id request, i.e.
	 * the API is down rather than choking on some ids
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
					throw new IOException("Interrupted while retrieving a batch of " + uniProtIds.size() + " ids", e1);
				}
			}
			try {
				Map<String, UniprotEntry> entries = getEntries(uniProtIds);
				Map<String, UnirefEntry> unirefEntries = new LinkedHashMap<>();
				for (Map.Entry<String, UniprotEntry> e : entries.entrySet()) {
					unirefEntries.put(e.getKey(), toUnirefEntry(e.getValue(), e.getKey()));
				}
				return unirefEntries;
			} catch (ClientErrorException e) {
				LOGGER.warn("Client error while retrieving a batch of {} ids, not retrying it. Error: {}",
						uniProtIds.size(), e.getMessage());
				break;
			} catch (IOException e) {
				LOGGER.warn("Got IOException while retrieving a batch of {} ids on attempt {}. Error: {}",
						uniProtIds.size(), i, e.getMessage());
			}
		}

		LOGGER.warn("Could not retrieve a batch of {} ids. Will request them one by one", uniProtIds.size());
		return getUnirefEntriesOneByOne(uniProtIds);
	}

	/**
	 * The fallback of {@link #getUnirefEntriesWithRetry(List)}: one request per id, a
	 * single attempt each.
	 * @param uniProtIds the UniProt ids
	 * @return the entries found, keyed by UniProt id
	 * @throws IOException if every id failed with an IOException
	 */
	private Map<String, UnirefEntry> getUnirefEntriesOneByOne(List<String> uniProtIds) throws IOException {
		Map<String, UnirefEntry> unirefEntries = new LinkedHashMap<>();
		List<String> failed = new ArrayList<>();
		IOException lastError = null;
		for (String uniProtId : uniProtIds) {
			try {
				unirefEntries.put(uniProtId, getUnirefEntry(uniProtId));
			} catch (NoMatchFoundException e) {
				// unknown or inactive: absent from the map, like in the batch answer
				LOGGER.debug("{} not found: {}", uniProtId, e.getMessage());
			} catch (IOException e) {
				failed.add(uniProtId);
				lastError = e;
			}
		}
		if (!failed.isEmpty() && failed.size() == uniProtIds.size()) {
			throw new IOException("Could not retrieve any of the " + uniProtIds.size() +
					" ids of the batch, neither as a batch nor one by one. Last error: " + lastError.getMessage(), lastError);
		}
		if (!failed.isEmpty()) {
			LOGGER.warn("Could not retrieve {} of the {} ids of the batch, one by one either: {}. Last error: {}",
					failed.size(), uniProtIds.size(), failed, lastError.getMessage());
		}
		return unirefEntries;
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
			} catch (ClientErrorException e) {
				LOGGER.warn("Client error while retrieving {}, not retrying it. Error: {}", uniProtId, e.getMessage());
				break;
			} catch (IOException e) {
				LOGGER.warn("Got IOException while retrieving {} on attempt {}. Error: {}", uniProtId, i, e.getMessage());

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
		Response response;
		try {
			response = client.target(VERSION_URL)
					.request(MediaType.APPLICATION_JSON)
					.head();
		} catch (ProcessingException e) {
			throw new IOException("Could not connect to " + VERSION_URL + ": " + e.getMessage(), e);
		}
		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			throw new IOException("Could not get UniProt version from headers of url request: " + VERSION_URL +
					". Response status was " + response.getStatus());
		}
		String version = response.getHeaderString(RELEASE_HEADER);
		if (version == null) {
			throw new IOException("No " + RELEASE_HEADER + " header in the response of " + VERSION_URL);
		}
		return version;

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
		int status = response.getStatus();
		if (status == Response.Status.NOT_FOUND.getStatusCode()) {
			throw new NoMatchFoundException("Response status for " + uri + " was " + status);
		}
		if (status >= 400 && status < 500 && status != TOO_MANY_REQUESTS) {
			// e.g. a 400 for a malformed accession: asking again won't change the answer
			throw new ClientErrorException("Response status for " + uri + " was " + status +
					": " + response.readEntity(String.class));
		}
		if (status != Response.Status.OK.getStatusCode()) {
			throw new IOException("Response status for " + uri + " was " + status);
		}
		return response;
	}

	private static final int TOO_MANY_REQUESTS = 429;

	/**
	 * A 4xx answer other than 404 and 429: the request itself is wrong, so it is not
	 * worth retrying.
	 */
	private static class ClientErrorException extends IOException {
		private static final long serialVersionUID = 1L;
		ClientErrorException(String message) {
			super(message);
		}
	}

}
