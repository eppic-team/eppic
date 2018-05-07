package eppic.rest.dao;

import eppic.model.PdbInfoDB;

/**
 * DAO for PDBInfoDB.
 * @author Jose Duarte
 *
 */
public interface PDBInfoDAO {

	/**
	 * Retrieves pdb info item by job identifier, without cascading to deeper tables.
	 * @param jobId job identifier
	 * @return pdb info item
	 * @throws DaoException when can not retrieve pdb info item for job
	 */
	PdbInfoDB getPDBInfoShallow(String jobId) throws DaoException;

	/**
	 * Retrieves full pdb info object including all children.
	 * @param jobId job identifier
	 * @return pdb info item
	 * @throws DaoException when can not retrieve pdb info item for job
	 */
	PdbInfoDB getPDBInfo(String jobId) throws DaoException;

	/**
	 * Retrieves full pdb info object including all children.
	 * @param pdbId pdb identifier
	 * @return pdb info item
	 * @throws DaoException when can not retrieve pdb info item for job
	 */
	PdbInfoDB getPDBInfoByPdbId(String pdbId) throws DaoException;

}
