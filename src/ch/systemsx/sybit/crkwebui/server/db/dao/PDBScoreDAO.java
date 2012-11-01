package ch.systemsx.sybit.crkwebui.server.db.dao;

import model.PDBScoreItemDB;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

/**
 * DAO for PDBScore item.
 * @author AS
 *
 */
public interface PDBScoreDAO 
{
	/**
	 * Retrieves pdb score item by job identifier.
	 * @param jobId identifier of the job
	 * @return pdb score item
	 * @throws DaoException when can not retrieve pdb score item for job
	 */
	public PDBScoreItem getPDBScore(String jobId) throws DaoException;
	
	/**
	 * Persists pdb score item.
	 * @param pdbScoreItem pdb score item to persist
	 * @throws DaoException when can not insert pdb score item
	 */
	public void insertPDBScore(PDBScoreItemDB pdbScoreItem) throws DaoException;
}
