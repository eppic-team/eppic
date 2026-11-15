package eppic.db.dao;

import eppic.model.db.PdbInfoDB;

import java.util.List;

/**
 * DAO for reading PDBInfoDB. For writing see {@link EntryDAO}.
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
	 * Get all objects in PdbInfo, without their assemblies
	 * @return list of PdbInfoDB objects
	 */
	List<PdbInfoDB> getAll() throws DaoException;
}
