package eppic.db.dao;

import java.util.List;

import eppic.model.dto.SequenceClusterType;
import eppic.model.dto.ChainCluster;
import eppic.model.dto.PDBSearchResult;

/**
 * DAO for HomologsInfo item.
 * @author AS
 *
 */
public interface ChainClusterDAO 
{
	/**
	 * Retrieves list of chain clusters for pdb score item.
	 * @param pdbInfoUid uid of pdb info item
	 * @return list of chain cluster for pdb score item
	 * @throws DaoException when can not retrieve chain clusters
	 */
	List<ChainCluster> getChainClusters(int pdbInfoUid) throws DaoException;
	
	/**
	 * Retrieves a list of pdb search items from ChainCluster table having a particular uniprot id
	 * @param uniProtId the unitprot id
	 * @return list of results
	 * @throws DaoException when can not retrieve items
	 */
	List<PDBSearchResult> getPdbSearchItemsForUniProt(String uniProtId) throws DaoException;
	
	/**
	 * Retrieves a list of pdb search items from ChainCluster table having a particular pdb id and cluster id
	 * @param pdbCode 
	 * @param repChain 
	 * @param c
	 * @return list of results
	 * @throws DaoException when can not retrieve items
	 */
	List<PDBSearchResult> getPdbSearchItems(String pdbCode, String repChain, SequenceClusterType c) throws DaoException;
}
