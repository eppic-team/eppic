package eppic.commons.sequence;

import org.junit.Test;

import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntry;
import uk.ac.ebi.uniprot.dataservice.client.exception.ServiceException;

import static org.junit.Assert.*;

public class TestUniProtConnection {

	@Test
	public void testGetUniProt() throws NoMatchFoundException, ServiceException {
		String upid = "P30340";
		UniProtConnection upc = new UniProtConnection();
		UniProtEntry entry = upc.getEntry(upid);
		assertEquals("SMTB_SYNE7", entry.getUniProtId().getValue());
		assertEquals(122, entry.getSequence().getLength());
	}
}
