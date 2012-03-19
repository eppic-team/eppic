package ch.systemsx.sybit.crkwebui.server.db.model;

import model.PDBScoreItemDB;
import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
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
	 * @throws CrkWebException
	 */
	public PDBScoreItem getPDBScore(String jobId) throws CrkWebException;
	
	/**
	 * Persists pdb score item.
	 * @param pdbScoreItem pdb score item to persist
	 * @throws CrkWebException
	 */
	public void insertPDBScore(PDBScoreItemDB pdbScoreItem) throws CrkWebException;
}
