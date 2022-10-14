package eppic.db.dao;

import eppic.model.db.PdbInfoDB;

import java.util.List;

/**
 * DAO for PDBInfoDB.
 * @author AS
 *
 */
public interface PDBInfoDAO 
{
	/**
	 * Retrieves pdb info full object by job identifier.
	 * @param entryId identifier of the job
	 * @return pdb info item
	 * @throws DaoException when can not retrieve pdb info item for job
	 */
	PdbInfoDB getPDBInfo(String entryId) throws DaoException;

	/**
	 * Persists pdb info object.
	 * @param pdbInfo pdb info item to persist
	 * @throws DaoException when can not insert pdb info item
	 */
	void insertPDBInfo(PdbInfoDB pdbInfo) throws DaoException;

	/**
	 * Persists a list of PdbInfoDB objects
	 * @param pdbInfos the list
	 * @throws DaoException
	 */
	void insertPDBInfos(List<PdbInfoDB> pdbInfos) throws DaoException;
}
