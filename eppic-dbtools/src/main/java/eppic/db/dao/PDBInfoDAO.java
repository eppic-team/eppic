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
	 * Retrieves pdb info object by job identifier, optionally without its assemblies. The
	 * assemblies are most of the data of an entry, so this is for what does not need them.
	 * @param entryId identifier of the job
	 * @param withAssemblies if false the pdb info item has no assemblies
	 * @return pdb info item
	 * @throws DaoException when can not retrieve pdb info item for job
	 */
	PdbInfoDB getPDBInfo(String entryId, boolean withAssemblies) throws DaoException;

	/**
	 * Get all objects in PdbInfo, without their assemblies
	 * @return list of PdbInfoDB objects
	 */
	List<PdbInfoDB> getAll() throws DaoException;
}
