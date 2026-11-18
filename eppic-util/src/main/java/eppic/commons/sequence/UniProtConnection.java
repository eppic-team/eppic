package eppic.commons.sequence;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	 * Maximum number of attempts for a request that fails with an error (other than
	 * being rate limited)
	 */
	private static final int MAX_NUM_RETRIES = 5;

	/**
	 * Maximum number of times a single request waits and tries again after being rate
	 * limited (429). Waits double from {@value #RETRY_INTERVAL} s up to
	 * {@value #MAX_BACKOFF} s, unless the API says how long in Retry-After: about 20
	 * minutes in all before giving up.
	 */
	private static final int MAX_NUM_RATE_LIMITED_RETRIES = 8;

	/**
	 * Longest wait (in seconds) after a 429, also capping the API's own Retry-After
	 */
	private static final int MAX_BACKOFF = 300;

	/**
	 * Minimum time between the start of two requests, so that a tight loop of fast
	 * requests (e.g. the one by one fallback) does not run into the rate limit
	 */
	private static final long MIN_REQUEST_INTERVAL_MS = 200;

	/**
	 * Maximum number of accessions asked for in a single batch request. The API takes
	 * more, but it pages its answer at 500, and smaller batches keep the cost of a
	 * failed batch (see {@link #getUnirefEntriesWithRetry(List)}) low.
	 */
	private static final int MAX_ACCESSIONS_PER_REQUEST = 100;

	static final String DEFAULT_REST_BASE = "https://rest.uniprot.org";

	/**
	 * A single UniProtKB entry: https://rest.uniprot.org/uniprotkb/P12345
	 */
	private final String uniprotEndpoint;

	/**
	 * Several UniProtKB entries in one request:
	 * https://rest.uniprot.org/uniprotkb/accessions?accessions=P12345,P67890
	 * It answers with {"results": [...]}, leaving out the accessions it does not know
	 * about or that are inactive (deleted, merged, demerged).
	 */
	private final String uniprotBatchEndpoint;

	/**
	 * Only what we read in {@link #parseEntry(JsonNode)}. organism_name brings the whole
	 * organism object, which has taxonId and lineage (as a list of names, from the most
	 * general to the most specific). Without it the full entry (references, comments,
	 * features...) is sent, many times bigger.
	 */
	private static final String UNIPROT_FIELDS = "accession,organism_name,sequence";

	private final String uniparcEndpoint;

	private static final String UNIPARC_FIELDS = "upi,sequence";

	/**
	 * Any small entry will do: we only read the release from the response headers.
	 */
	private final String versionUrl;

	private static final String RELEASE_HEADER = "X-UniProt-Release";

	private static final String INACTIVE_ENTRY_TYPE = "Inactive";

	/*--------------------------- member variables --------------------------*/
	private final Client client;
	private final ObjectMapper objectMapper;
	private HashSet<String> nonReturnedIdsLastMultipleRequest;
	private HashSet<String> notRetrievedIdsLastBatch = new HashSet<>();
	private long lastRequestStartMs = 0;

	/*----------------------------- constructors ----------------------------*/

	public UniProtConnection() {
		this(DEFAULT_REST_BASE);
	}

	/**
	 * @param restBase the base url of the UniProt REST API, only ever changed by tests
	 */
	UniProtConnection(String restBase) {

		uniprotEndpoint = restBase + "/uniprotkb/";
		uniprotBatchEndpoint = restBase + "/uniprotkb/accessions";
		uniparcEndpoint = restBase + "/uniparc/";
		versionUrl = uniprotEndpoint + "P12345?fields=accession";

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

		String request = uniprotEndpoint + uniProtId + "?fields=" + UNIPROT_FIELDS;
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

		String request = uniprotBatchEndpoint + "?size=" + MAX_ACCESSIONS_PER_REQUEST +
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

		String request = uniparcEndpoint + uniparcId + "?fields=" + UNIPARC_FIELDS;
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
	 * it. Ids that fail on their own are left out of the returned map, logged, and listed
	 * by {@link #getNotRetrievedIdsLastBatch()}.
	 * <p>
	 * Being rate limited (429) is different: the request is retried after a growing wait
	 * (see {@link #withRetry}), and never falls back to one request per id.
	 * @param uniProtIds the UniProt ids, at most {@link #getMaxAccessionsPerRequest()}
	 * @return the entries found, keyed by UniProt id
	 * @throws IOException if the batch fails and so does every single id request, i.e.
	 * the API is down rather than choking on some ids, or if still rate limited after
	 * {@value #MAX_NUM_RATE_LIMITED_RETRIES} waits
	 */
	public Map<String, UnirefEntry> getUnirefEntriesWithRetry(List<String> uniProtIds) throws IOException {
		notRetrievedIdsLastBatch = new HashSet<>();
		String what = "a batch of " + uniProtIds.size() + " ids";
		Map<String, UniprotEntry> entries;
		try {
			entries = withRetry(() -> getEntries(uniProtIds), what, MAX_NUM_RETRIES);
		} catch (NoMatchFoundException e) {
			// getEntries answers an empty map instead, but the compiler doesn't know
			return new LinkedHashMap<>();
		} catch (RateLimitedException e) {
			// one request per id would only make the rate limiting worse
			notRetrievedIdsLastBatch.addAll(uniProtIds);
			throw e;
		} catch (IOException e) {
			LOGGER.warn("Could not retrieve {}. Will request them one by one. Error: {}", what, e.getMessage());
			return getUnirefEntriesOneByOne(uniProtIds);
		}
		Map<String, UnirefEntry> unirefEntries = new LinkedHashMap<>();
		for (Map.Entry<String, UniprotEntry> e : entries.entrySet()) {
			unirefEntries.put(e.getKey(), toUnirefEntry(e.getValue(), e.getKey()));
		}
		return unirefEntries;
	}

	/**
	 * The ids of the last {@link #getUnirefEntriesWithRetry(List)} call that are absent
	 * from its answer because they could not be retrieved (errors, rate limiting), as
	 * opposed to the API not knowing about them or them being inactive.
	 * @return the ids, empty if all could be retrieved
	 */
	public Set<String> getNotRetrievedIdsLastBatch() {
		return notRetrievedIdsLastBatch;
	}

	/**
	 * The fallback of {@link #getUnirefEntriesWithRetry(List)}: one request per id, a
	 * single attempt each, except for waiting out rate limiting.
	 * @param uniProtIds the UniProt ids
	 * @return the entries found, keyed by UniProt id
	 * @throws IOException if every id failed with an IOException, or if still rate
	 * limited after {@value #MAX_NUM_RATE_LIMITED_RETRIES} waits
	 */
	private Map<String, UnirefEntry> getUnirefEntriesOneByOne(List<String> uniProtIds) throws IOException {
		Map<String, UnirefEntry> unirefEntries = new LinkedHashMap<>();
		List<String> failed = new ArrayList<>();
		IOException lastError = null;
		for (int i = 0; i < uniProtIds.size(); i++) {
			String uniProtId = uniProtIds.get(i);
			try {
				unirefEntries.put(uniProtId, withRetry(() -> getUnirefEntry(uniProtId), uniProtId, 1));
			} catch (NoMatchFoundException e) {
				// unknown or inactive: absent from the map, like in the batch answer
				LOGGER.debug("{} not found: {}", uniProtId, e.getMessage());
			} catch (RateLimitedException e) {
				notRetrievedIdsLastBatch.addAll(failed);
				notRetrievedIdsLastBatch.addAll(uniProtIds.subList(i, uniProtIds.size()));
				throw e;
			} catch (IOException e) {
				failed.add(uniProtId);
				lastError = e;
			}
		}
		notRetrievedIdsLastBatch.addAll(failed);
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
	 * maximum of {@value #MAX_NUM_RETRIES} attempts, and waiting out rate limiting (429)
	 * @param uniProtId the uniprot identifier
	 * @return
	 * @throws NoMatchFoundException if no match returned by UniProt REST API
	 * @throws IOException if all attempts fail
	 */
	public UnirefEntry getUnirefEntryWithRetry(String uniProtId) throws NoMatchFoundException, IOException {
		return withRetry(() -> getUnirefEntry(uniProtId), uniProtId, MAX_NUM_RETRIES);
	}

	@FunctionalInterface
	private interface ApiCall<T> {
		T call() throws IOException, NoMatchFoundException;
	}

	/**
	 * Runs an API call, retrying it:
	 * <ul>
	 * <li>after a 429 (rate limited): waiting what the API asks for in Retry-After, or
	 * else doubling waits from {@value #RETRY_INTERVAL} s up to {@value #MAX_BACKOFF} s,
	 * at most {@value #MAX_NUM_RATE_LIMITED_RETRIES} times, then a
	 * {@link RateLimitedException} is thrown</li>
	 * <li>after any other IOException: every {@value #RETRY_INTERVAL} s, up to
	 * maxAttempts attempts in all</li>
	 * <li>never after a {@link ClientErrorException} (asking again won't change the
	 * answer) or a {@link NoMatchFoundException}</li>
	 * </ul>
	 * Being rate limited does not use up the error attempts.
	 */
	private <T> T withRetry(ApiCall<T> call, String what, int maxAttempts) throws IOException, NoMatchFoundException {
		int errors = 0;
		int rateLimited = 0;
		while (true) {
			try {
				return call.call();
			} catch (ClientErrorException e) {
				throw e;
			} catch (RateLimitedException e) {
				rateLimited++;
				if (rateLimited > MAX_NUM_RATE_LIMITED_RETRIES) {
					throw new RateLimitedException("Still rate limited (429) while retrieving " + what + " after waiting " +
							MAX_NUM_RATE_LIMITED_RETRIES + " times. Giving up", null);
				}
				long wait = e.getRetryAfterSeconds() != null ?
						Math.max(1, Math.min(e.getRetryAfterSeconds(), MAX_BACKOFF)) :
						Math.min((long) RETRY_INTERVAL << (rateLimited - 1), MAX_BACKOFF);
				LOGGER.warn("Rate limited (429) while retrieving {}, for the {} time. Waiting {} s{}", what, rateLimited, wait,
						e.getRetryAfterSeconds() != null ? " as asked by Retry-After" : "");
				sleep(wait * 1000, what);
			} catch (IOException e) {
				errors++;
				if (errors >= maxAttempts) {
					throw new IOException("Could not retrieve " + what + " from UniProt REST API after " + errors +
							" attempts. Giving up. Last error: " + e.getMessage(), e);
				}
				LOGGER.warn("Got IOException while retrieving {} on attempt {}. Error: {}", what, errors, e.getMessage());
				LOGGER.info("Waiting {} s before next retry", RETRY_INTERVAL);
				sleep(RETRY_INTERVAL * 1000L, what);
			}
		}
	}

	private static void sleep(long millis, String what) throws IOException {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Interrupted while retrieving " + what, e);
		}
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
			response = client.target(versionUrl)
					.request(MediaType.APPLICATION_JSON)
					.head();
		} catch (ProcessingException e) {
			throw new IOException("Could not connect to " + versionUrl + ": " + e.getMessage(), e);
		}
		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			throw new IOException("Could not get UniProt version from headers of url request: " + versionUrl +
					". Response status was " + response.getStatus());
		}
		String version = response.getHeaderString(RELEASE_HEADER);
		if (version == null) {
			throw new IOException("No " + RELEASE_HEADER + " header in the response of " + versionUrl);
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
		throttle();
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
		if (status == TOO_MANY_REQUESTS) {
			throw new RateLimitedException("Response status for " + uri + " was " + status,
					parseRetryAfter(response.getHeaderString("Retry-After")));
		}
		if (status >= 400 && status < 500) {
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
	 * Waits as needed so that requests start at least {@value #MIN_REQUEST_INTERVAL_MS} ms
	 * apart.
	 */
	private synchronized void throttle() throws IOException {
		long wait = lastRequestStartMs + MIN_REQUEST_INTERVAL_MS - System.currentTimeMillis();
		if (wait > 0) {
			sleep(wait, "the next request");
		}
		lastRequestStartMs = System.currentTimeMillis();
	}

	/**
	 * Parses a Retry-After header, which is either a number of seconds or an HTTP date.
	 * @return the seconds to wait, or null if absent or not understood
	 */
	static Long parseRetryAfter(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return Math.max(0, Long.parseLong(value.trim()));
		} catch (NumberFormatException e) {
			// not seconds, so it should be a date
		}
		try {
			ZonedDateTime when = ZonedDateTime.parse(value.trim(), DateTimeFormatter.RFC_1123_DATE_TIME);
			return Math.max(0, Duration.between(ZonedDateTime.now(when.getZone()), when).getSeconds());
		} catch (DateTimeParseException e) {
			LOGGER.debug("Could not understand Retry-After header '{}'", value);
			return null;
		}
	}

	/**
	 * A 429 answer: we are asking too fast. Worth retrying, but only after a wait.
	 */
	private static class RateLimitedException extends IOException {
		private static final long serialVersionUID = 1L;
		private final Long retryAfterSeconds;
		RateLimitedException(String message, Long retryAfterSeconds) {
			super(message);
			this.retryAfterSeconds = retryAfterSeconds;
		}
		/**
		 * @return what the API asked us to wait for in Retry-After, null if it didn't say
		 */
		Long getRetryAfterSeconds() {
			return retryAfterSeconds;
		}
	}

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
