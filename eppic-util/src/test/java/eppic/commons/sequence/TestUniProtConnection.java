package eppic.commons.sequence;

import org.junit.Test;

import java.io.IOException;

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
		String upid = "A0A8C0T962";
		UniProtConnection upc = new UniProtConnection();
		UnirefEntry entry = upc.getUnirefEntry(upid);
		assertEquals(443, entry.getSeq().getLength());
		assertEquals(upid, entry.getUniId());
	}
}
