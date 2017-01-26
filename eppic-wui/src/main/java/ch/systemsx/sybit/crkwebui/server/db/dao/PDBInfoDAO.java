package ch.systemsx.sybit.crkwebui.server.db.dao;

import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;
import eppic.model.PdbInfoDB;

/**
 * DAO for PDBScore item.
 * @author AS
 *
 */
public interface PDBInfoDAO 
{
	/**
	 * Retrieves pdb info item by job identifier.
	 * @param jobId identifier of the job
	 * @return pdb info item
	 * @throws DaoException when can not retrieve pdb info item for job
	 */
	public PdbInfo getPDBInfo(String jobId) throws DaoException;
	
	/**
	 * Persists pdb info item.
	 * @param pdbInfo pdb info item to persist
	 * @throws DaoException when can not insert pdb info item
	 */
	public void insertPDBInfo(PdbInfoDB pdbInfo) throws DaoException;
}
