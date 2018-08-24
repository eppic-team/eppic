package eppic.db.dao;

import eppic.model.dto.PdbInfo;
import eppic.model.db.PdbInfoDB;

/**
 * DAO for PDBScore item.
 * @author AS
 *
 */
public interface PDBInfoDAO 
{
	/**
	 * Retrieves pdb info item by job identifier, with only a restricted list of selected fields (light weight data).
	 * @param jobId identifier of the job
	 * @return pdb info item
	 * @throws DaoException when can not retrieve pdb info item for job
	 */
	PdbInfo getPDBInfo(String jobId) throws DaoException;

	/**
	 * Retrieves pdb info item by job identifier with all data. Optionally retrieving clusters and residue data too (lots of data, heavy).
	 * @param jobId identifier of the job
	 * @param withChainClustersAndResidues whether to retrieve chain clusters and residues (lots of data, heavy query) or not
	 * @return pdb info item
	 * @throws DaoException when can not retrieve pdb info item for job
	 */
	PdbInfo getPDBInfo(String jobId, boolean withChainClustersAndResidues) throws DaoException;

	/**
	 * Persists pdb info item.
	 * @param pdbInfo pdb info item to persist
	 * @throws DaoException when can not insert pdb info item
	 */
	void insertPDBInfo(PdbInfoDB pdbInfo) throws DaoException;
}
