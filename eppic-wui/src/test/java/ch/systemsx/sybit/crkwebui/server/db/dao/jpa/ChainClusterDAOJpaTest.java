package ch.systemsx.sybit.crkwebui.server.db.dao.jpa;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import ch.systemsx.sybit.crkwebui.shared.model.PDBSearchResult;
import ch.systemsx.sybit.crkwebui.shared.model.SequenceClusterType;
import eppic.db.dao.DaoException;
import eppic.db.dao.jpa.ChainClusterDAOJpa;


public class ChainClusterDAOJpaTest {

    @Test
    public void testGetPdbSearchItems() throws DaoException {
	ChainClusterDAOJpa dao = new ChainClusterDAOJpa();
	List<PDBSearchResult> results = dao.getPdbSearchItemsForUniProt("P30340");
	List<PDBSearchResult> results2 = dao.getPdbSearchItems("1smt", "A", SequenceClusterType.C95);
	
	assertEquals("the length of the two result lists are not the same", results2.size(), results.size());
	for(int i = 0; i < results.size(); i++)
	    assertEquals("Wrong pdbCode", results2.get(i).getPdbCode(), results.get(i).getPdbCode());
	for(int i = 0; i < results.size(); i++)
	    assertEquals("Wrong rfree value", results2.get(i).getRfreeValue(), results.get(i).getRfreeValue(), 0.0);
    }
    
    @Test
    public void testClusterLevels() throws DaoException {
    	ChainClusterDAOJpa dao = new ChainClusterDAOJpa();
    	//List<PDBSearchResult> results = dao.getPdbSearchItemsForUniProt("P30340");
    	String[] pdbCodes = {"2gs2","7aat", "3u7c"};
    	String[] pdbChainCodes = {"A","A", "A"};
    	
    	for (int i=0;i<pdbCodes.length;i++) {
    		String pdbCode = pdbCodes[i];
    		String pdbChainCode = pdbChainCodes[i];
    		List<PDBSearchResult> resultsc100 = dao.getPdbSearchItems(pdbCode, pdbChainCode, SequenceClusterType.C100);
    		List<PDBSearchResult> resultsc95 = dao.getPdbSearchItems(pdbCode, pdbChainCode, SequenceClusterType.C95);
    		List<PDBSearchResult> resultsc90 = dao.getPdbSearchItems(pdbCode, pdbChainCode, SequenceClusterType.C90);
    		List<PDBSearchResult> resultsc80 = dao.getPdbSearchItems(pdbCode, pdbChainCode, SequenceClusterType.C80);
    		List<PDBSearchResult> resultsc70 = dao.getPdbSearchItems(pdbCode, pdbChainCode, SequenceClusterType.C70);
    		List<PDBSearchResult> resultsc60 = dao.getPdbSearchItems(pdbCode, pdbChainCode, SequenceClusterType.C60);
    		List<PDBSearchResult> resultsc50 = dao.getPdbSearchItems(pdbCode, pdbChainCode, SequenceClusterType.C50);

    		System.out.println("Cluster sizes for "+pdbCode+pdbChainCode+": ");
    		System.out.println("C100: "+resultsc100.size());
    		System.out.println("C95: "+resultsc95.size());
    		System.out.println("C90: "+resultsc90.size());
    		System.out.println("C80: "+resultsc80.size());
    		System.out.println("C70: "+resultsc70.size());
    		System.out.println("C60: "+resultsc60.size());
    		System.out.println("C50: "+resultsc50.size());

    		assertTrue("size of c100 must be <= than c95",resultsc100.size()<=resultsc95.size());
    		assertTrue("size of c95 must be <= than c90",resultsc95.size()<=resultsc90.size());
    		assertTrue("size of c90 must be <= than c80",resultsc90.size()<=resultsc80.size());
    		assertTrue("size of c80 must be <= than c70",resultsc80.size()<=resultsc70.size());
    		assertTrue("size of c70 must be <= than c60",resultsc70.size()<=resultsc60.size());
    		assertTrue("size of c60 must be <= than c50",resultsc60.size()<=resultsc50.size());
    	}
    }
}
