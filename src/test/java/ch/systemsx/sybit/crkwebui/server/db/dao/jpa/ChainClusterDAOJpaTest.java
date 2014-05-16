package ch.systemsx.sybit.crkwebui.server.db.dao.jpa;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.PDBSearchResult;


public class ChainClusterDAOJpaTest {

    @Test
    public void testGetPdbSearchItems() throws DaoException {
	ChainClusterDAOJpa dao = new ChainClusterDAOJpa();
	List<PDBSearchResult> results = dao.getPdbSearchItemsForUniProt("P30340");
	List<PDBSearchResult> results2 = dao.getPdbSearchItems("1smt", "A", 100);
	
	assertEquals("the length of the two result lists are not the same", results2.size(), results.size());
	for(int i = 0; i < results.size(); i++)
	    assertEquals("Wrong pdbCode", results2.get(i).getPdbCode(), results.get(i).getPdbCode());
	for(int i = 0; i < results.size(); i++)
	    assertEquals("Wrong rfree value", results2.get(i).getRfreeValue(), results.get(i).getRfreeValue(), 0.0);
    }
}
