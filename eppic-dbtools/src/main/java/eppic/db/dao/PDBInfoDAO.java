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
	 * Retrieves pdb info full object by job identifier.
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
