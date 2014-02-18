package ch.systemsx.sybit.crkwebui.server.db.dao;

import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.Interface;

/**
 * DAO for Interface item.
 * @author AS
 *
 */
public interface InterfaceDAO 
{
	/**
	 * Retrieves list of interface items with scores for a particular interface cluster item.
	 * @param interfaceClusterUid uid of interface cluster item
	 * @return list of interface items with scores for pdb score item
	 * @throws DaoException when can not retrieve interface items
	 */
	public List<Interface> getInterfacesWithScores(int interfaceClusterUid) throws DaoException;
	
	/**
	 * Retrieves a specific list of interface items with scores a particular interface cluster item.
	 * The returned interfaces would be strictly present in the list of interfaceIds provided
	 * @param interfaceIds list of ids of the interfaces. These are the only allowed values of the interface id that would be returned
	 * @param interfaceClusterUid uid of interface cluster item
	 * @return list of interface items with scores for pdb score item
	 * @throws DaoException when can not retrieve interface items
	 */
	public List<Interface> getInterfacesWithScores(int interfaceClusterUid, List<Integer> interfaceIds) throws DaoException;
	
	/**
	 * Retrieves list of interface items with residues for a particular interface cluster item.
	 * @param interfaceClusterUid uid of interface cluster item
	 * @return list of interface items with residues for pdb score item
	 * @throws DaoException when can not retrieve interface items
	 */
	public List<Interface> getInterfacesWithResidues(int interfaceClusterUid) throws DaoException;
}
