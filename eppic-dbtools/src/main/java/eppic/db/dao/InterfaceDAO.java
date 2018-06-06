package eppic.db.dao;

import java.util.List;
import java.util.Set;

import eppic.model.dto.Interface;

/**
 * DAO for Interface item.
 * @author Adam Srebniak
 * @author Jose Duarte
 *
 */
public interface InterfaceDAO 
{
	/**
	 * Retrieves list of interfaces for a particular interface cluster.
	 * @param interfaceClusterUid uid of interface cluster
	 * @param withScores include interface scores
	 * @param withResidues include per-residue data
	 * @return list of interface items
	 * @throws DaoException when problems retrieving data from backend db
	 */
	List<Interface> getInterfacesForCluster(int interfaceClusterUid, boolean withScores, boolean withResidues) throws DaoException;

	/**
	 * Retrieves list of interface items for a particular interface cluster,
	 * only those interfaces that are in the given list of interfaceIds will be returned.
	 * @param interfaceClusterUid uid of interface cluster
	 * @param interfaceIds list of ids of the interfaces to be returned
	 * @param withScores include interface scores
	 * @param withResidues include per-residue data
	 * @return list of interface items
	 * @throws DaoException when problems retrieving data from backend db
	 */
	List<Interface> getInterfacesForCluster(int interfaceClusterUid, Set<Integer> interfaceIds, boolean withScores, boolean withResidues) throws DaoException;

	/**
	 * Retrieves a single interface for given pdbInfo uid and interface id
	 * @param pdbInfoUid pdbInfo uid (db wide identifier)
	 * @param interfaceId interface id
	 * @param withScores include interface scores
	 * @param withResidues include per-residue data
	 * @return the interface data
	 * @throws DaoException when problems retrieving data from backend db
	 */
	Interface getInterface(int pdbInfoUid, int interfaceId, boolean withScores, boolean withResidues)throws DaoException;

	/**
	 * Retrieves a list of all interfaces for given pdbInfo uid
	 * @param pdbInfoUid pdbInfo uid (db wide identifier)
	 * @param withScores include interface scores
	 * @param withResidues include per-residue data
	 * @return list of interface items
	 * @throws DaoException when problems retrieving data from backend db
	 */
	List<Interface> getInterfacesByPdbUid(int pdbInfoUid, boolean withScores, boolean withResidues) throws DaoException;
}
