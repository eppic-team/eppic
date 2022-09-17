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
		assertEquals("SMTB_SYNE7", entry.getUniProtId().getValue());
		assertEquals(122, entry.getSequence().getLength());
	}
}
