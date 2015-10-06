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
	 * @return list of interface items with scores for pdb info item
	 * @throws DaoException when can not retrieve interface items
	 */
	public List<Interface> getInterfacesWithScores(int interfaceClusterUid) throws DaoException;
	
	/**
	 * Retrieves a specific list of interface items with scores and residues for a particular interface cluster item,
	 * only those interfaces that are in the given list of interfaceIds will be returned.
	 * @param interfaceClusterUid uid of interface cluster item
	 * @param interfaceIds list of ids of the interfaces to be returned
	 * @return list of interface items with residues and scores for pdb info item
	 * @throws DaoException when can not retrieve interface items
	 */
	public List<Interface> getInterfacesWithResidues(int interfaceClusterUid, List<Integer> interfaceIds) throws DaoException;
	
	/**
	 * Retrieves list of interface items with scores and residues for a particular interface cluster item.
	 * @param interfaceClusterUid uid of interface cluster item
	 * @return list of interface items with residues and scores for pdb info item
	 * @throws DaoException when can not retrieve interface items
	 */
	public List<Interface> getInterfacesWithResidues(int interfaceClusterUid) throws DaoException;
	
	public Interface getInterfaceWithResidues(int pdbInfoUid, int interfaceId)throws DaoException;

	/**
	 * Get a list of all interfaces (without residues or score) for a particular PDB
	 * @param pdbInfoUid
	 * @return
	 * @throws DaoException 
	 */
	public List<Interface> getAllInterfaces(int pdbInfoUid) throws DaoException;
}
