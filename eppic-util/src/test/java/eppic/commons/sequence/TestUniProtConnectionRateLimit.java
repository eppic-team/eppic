package eppic.commons.sequence;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * How {@link UniProtConnection} copes with being rate limited (429), against a local
 * fake of the UniProt REST API.
 */
public class TestUniProtConnectionRateLimit {

	private HttpServer server;
	private String base;

	/** how many of the next batch requests answer 429 */
	private final AtomicInteger batch429sLeft = new AtomicInteger();
	private final AtomicInteger batchRequests = new AtomicInteger();
	private final AtomicInteger singleRequests = new AtomicInteger();

	@Before
	public void setUp() throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/uniprotkb/accessions", this::handleBatch);
		server.createContext("/uniprotkb/", ex -> {
			singleRequests.incrementAndGet();
			String id = ex.getRequestURI().getPath().substring("/uniprotkb/".length());
			respond(ex, 200, entryJson(id), null);
		});
		server.start();
		base = "http://127.0.0.1:" + server.getAddress().getPort();
	}

	@After
	public void tearDown() {
		server.stop(0);
	}

	private void handleBatch(HttpExchange ex) throws IOException {
		batchRequests.incrementAndGet();
		if (batch429sLeft.getAndDecrement() > 0) {
			respond(ex, 429, "{\"messages\":[\"Too many requests\"]}", "0");
			return;
		}
		String query = URLDecoder.decode(ex.getRequestURI().getRawQuery(), StandardCharsets.UTF_8.name());
		String accessions = Arrays.stream(query.split("&"))
				.filter(p -> p.startsWith("accessions="))
				.findFirst().get().substring("accessions=".length());
		String results = Arrays.stream(accessions.split(","))
				.map(TestUniProtConnectionRateLimit::entryJson)
				.collect(Collectors.joining(","));
		respond(ex, 200, "{\"results\":[" + results + "]}", null);
	}

	private static String entryJson(String id) {
		return "{\"entryType\":\"UniProtKB reviewed (Swiss-Prot)\",\"primaryAccession\":\"" + id + "\"," +
				"\"organism\":{\"taxonId\":9606,\"lineage\":[\"Eukaryota\",\"Homo\"]}," +
				"\"sequence\":{\"value\":\"MKVLAAGIVG\"}}";
	}

	private static void respond(HttpExchange ex, int status, String body, String retryAfter) throws IOException {
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		ex.getResponseHeaders().add("Content-Type", "application/json");
		if (retryAfter != null) {
			ex.getResponseHeaders().add("Retry-After", retryAfter);
		}
		ex.sendResponseHeaders(status, bytes.length);
		try (OutputStream os = ex.getResponseBody()) {
			os.write(bytes);
		}
	}

	@Test
	public void testBatchWaitsOutRateLimiting() throws IOException {
		batch429sLeft.set(2);
		UniProtConnection upc = new UniProtConnection(base);
		List<String> ids = Arrays.asList("P11111", "P22222", "P33333");

		Map<String, UnirefEntry> entries = upc.getUnirefEntriesWithRetry(ids);

		assertEquals(3, entries.size());
		assertEquals(9606, entries.get("P22222").getNcbiTaxId());
		assertEquals("Homo", entries.get("P22222").getLastTaxon());
		assertTrue(upc.getNotRetrievedIdsLastBatch().isEmpty());
		// two 429s, then the answer
		assertEquals(3, batchRequests.get());
		// a 429 must never fall back to one request per id
		assertEquals(0, singleRequests.get());
	}

	@Test
	public void testBatchGivesUpWhenAlwaysRateLimited() {
		batch429sLeft.set(Integer.MAX_VALUE);
		UniProtConnection upc = new UniProtConnection(base);
		List<String> ids = Arrays.asList("P11111", "P22222");

		try {
			upc.getUnirefEntriesWithRetry(ids);
			fail("Expected an IOException when always rate limited");
		} catch (IOException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("rate limited"));
		}
		assertEquals(2, upc.getNotRetrievedIdsLastBatch().size());
		assertEquals(0, singleRequests.get());
	}

	@Test
	public void testParseRetryAfter() {
		assertNull(UniProtConnection.parseRetryAfter(null));
		assertNull(UniProtConnection.parseRetryAfter(""));
		assertNull(UniProtConnection.parseRetryAfter("soon"));
		assertEquals(Long.valueOf(30), UniProtConnection.parseRetryAfter("30"));
		assertEquals(Long.valueOf(0), UniProtConnection.parseRetryAfter("-5"));
		// a date in the past means no wait
		assertEquals(Long.valueOf(0), UniProtConnection.parseRetryAfter("Wed, 21 Oct 2015 07:28:00 GMT"));
	}
}
