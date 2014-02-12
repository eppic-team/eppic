package ch.systemsx.sybit.crkwebui.server.db.dao;

import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceItem;

/**
 * DAO for Interface item.
 * @author AS
 *
 */
public interface InterfaceItemDAO 
{
	/**
	 * Retrieves list of interface items with scores for pdb score item.
	 * @param pdbScoreUid uid of pdb score item
	 * @return list of interface items with scores for pdb score item
	 * @throws DaoException when can not retrieve interface items
	 */
	public List<InterfaceItem> getInterfacesWithScores(int pdbScoreUid) throws DaoException;
	
	/**
	 * Retrieves a specific list of interface items with scores for the pdb score item.
	 * @param interfaceIds id of the interfaces in the pdb
	 * @param pdbScoreUid uid of pdb score item
	 * @return list of interface items with scores for pdb score item
	 * @throws DaoException when can not retrieve interface items
	 */
	public List<InterfaceItem> getInterfacesWithScores(int pdbScoreUid, List<Integer> interfaceIds) throws DaoException;
	
	/**
	 * Retrieves list of interface items with residues for pdb score item.
	 * @param pdbScoreUid uid of pdb score item
	 * @return list of interface items with residues for pdb score item
	 * @throws DaoException when can not retrieve interface items
	 */
	public List<InterfaceItem> getInterfacesWithResidues(int pdbScoreUid) throws DaoException;
}
