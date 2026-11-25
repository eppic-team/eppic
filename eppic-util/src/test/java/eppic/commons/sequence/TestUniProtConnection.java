package eppic.commons.sequence;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.*;

public class TestUniProtConnection {

	@Test
	public void testGetUniProt() throws NoMatchFoundException, IOException {
		String upid = "P30340";
		UniProtConnection upc = new UniProtConnection();
		UniprotEntry entry = upc.getEntry(upid);
		assertEquals(122, entry.getUniprotSeq().getLength());
		assertEquals(upid, entry.getUniId());
		assertEquals(1140, entry.getTaxId());
		assertEquals("Synechococcus", entry.getLastTaxon());
		assertEquals("Bacteria", entry.getFirstTaxon());
	}

	@Test
	public void testGetUniparc() throws NoMatchFoundException, IOException {
		String upid = "UPI00000217E5";
		UniProtConnection upc = new UniProtConnection();
		UniprotEntry entry = upc.getUniparcEntry(upid);
		assertEquals(184, entry.getUniprotSeq().getLength());
		assertEquals(upid, entry.getUniId());
		// TODO when tax id and taxons lineage implemented, then implement a test here
	}

	@Test
	public void testGetUniref() throws NoMatchFoundException, IOException {
		String upid = "P69905";
		UniProtConnection upc = new UniProtConnection();
		UnirefEntry entry = upc.getUnirefEntry(upid);
		assertEquals(142, entry.getSeq().getLength());
		assertEquals(upid, entry.getUniId());
	}

	@Test(expected = NoMatchFoundException.class)
	public void testGetObsoleteUniProt() throws NoMatchFoundException, IOException {
		String upid = "L8NZJ8";
		UniProtConnection upc = new UniProtConnection();
		upc.getEntry(upid);
	}

	@Test
	public void testGetEntriesBatch() throws IOException {
		UniProtConnection upc = new UniProtConnection();
		// L8NZJ8 is inactive (deleted): left out of the answer, like unknown ids
		Map<String, UniprotEntry> entries = upc.getEntries(Arrays.asList("P30340", "P69905", "L8NZJ8"));
		assertEquals(2, entries.size());
		assertFalse(entries.containsKey("L8NZJ8"));
		UniprotEntry entry = entries.get("P30340");
		assertEquals(122, entry.getUniprotSeq().getLength());
		assertEquals(1140, entry.getTaxId());
		assertEquals("Bacteria", entry.getFirstTaxon());
		assertEquals("Synechococcus", entry.getLastTaxon());
		assertEquals(142, entries.get("P69905").getUniprotSeq().getLength());
	}

	@Test
	public void testBatchWithMalformedIdFallsBackToOneByOne() throws IOException {
		UniProtConnection upc = new UniProtConnection();
		// a malformed accession makes the whole batch request fail with a 400. The good
		// ids must still come back, through the one by one fallback
		Map<String, UnirefEntry> entries = upc.getUnirefEntriesWithRetry(Arrays.asList("P30340", "NOT_AN_ID", "P69905"));
		assertEquals(2, entries.size());
		assertEquals(1140, entries.get("P30340").getNcbiTaxId());
		assertEquals(142, entries.get("P69905").getSeq().getLength());
	}

	@Test
	public void testGetUniProtVersion() throws IOException, NumberFormatException {
		UniProtConnection upc = new UniProtConnection();
		String ver = upc.getVersion();
		assertEquals(7, ver.length());
		assertTrue(ver.contains("_"));
		String[] tokens = ver.split("_");
		assertEquals(4, tokens[0].length());
		assertEquals(2, tokens[1].length());
		Integer.parseInt(tokens[0]);
		Integer.parseInt(tokens[1]);
		//assertEquals("2022_03", ver);
	}
}
