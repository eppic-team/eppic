package eppic.db.dao;

import eppic.model.db.PdbInfoDB;

/**
 * DAO for PDBInfoDB.
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
	PdbInfoDB getPDBInfo(String jobId) throws DaoException;

	/**
	 * Persists pdb info item.
	 * @param pdbInfo pdb info item to persist
	 * @throws DaoException when can not insert pdb info item
	 */
	void insertPDBInfo(PdbInfoDB pdbInfo) throws DaoException;
}
