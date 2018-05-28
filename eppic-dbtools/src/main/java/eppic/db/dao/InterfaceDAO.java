package eppic.db.dao;

import java.util.List;
import java.util.Set;

import eppic.dtomodel.Interface;

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
	 * @param interfaceClusterUid uid of interface cluster item
	 * @param withScores include interface scores
	 * @param withResidues include per-residue data
	 * @return list of interface items with scores for pdb info item
	 * @throws DaoException when can not retrieve interface items
	 */
	List<Interface> getInterfacesForCluster(int interfaceClusterUid, boolean withScores, boolean withResidues) throws DaoException;

	/**
	 * Retrieves list of interface items with scores for a particular interface cluster item,
	 * only those interfaces that are in the given list of interfaceIds will be returned.
	 * @param interfaceClusterUid uid of interface cluster item
	 * @param interfaceIds list of ids of the interfaces to be returned
	 * @return list of interface items with scores for pdb info item
	 * @throws DaoException when can not retrieve interface items
	 */
	List<Interface> getInterfacesWithScores(int interfaceClusterUid, Set<Integer> interfaceIds) throws DaoException;
	
	/**
	 * Retrieves a specific list of interface items with scores and residues for a particular interface cluster item,
	 * only those interfaces that are in the given list of interfaceIds will be returned.
	 * @param interfaceClusterUid uid of interface cluster item
	 * @param interfaceIds list of ids of the interfaces to be returned
	 * @return list of interface items with residues and scores for pdb info item
	 * @throws DaoException when problems retrieving data from backend db
	 */
	List<Interface> getInterfacesWithResidues(int interfaceClusterUid, Set<Integer> interfaceIds) throws DaoException;

	/**
	 * Retrieves a single interface for given interface id
	 * @param pdbInfoUid pdbInfo uid (db wide identifier)
	 * @param interfaceId interface id
	 * @param withScores include interface scores
	 * @param withResidues include per-residue data
	 * @return
	 * @throws DaoException when problems retrieving data from backend db
	 */
	Interface getInterface(int pdbInfoUid, int interfaceId, boolean withScores, boolean withResidues)throws DaoException;

	/**
	 * Retrieves a list of all interfaces for a particular PDB
	 * @param pdbInfoUid pdbInfo uid (db wide identifier)
	 * @param withScores include interface scores
	 * @param withResidues include per-residue data
	 * @return
	 * @throws DaoException when problems retrieving data from backend db
	 */
	List<Interface> getInterfacesByPdbUid(int pdbInfoUid, boolean withScores, boolean withResidues) throws DaoException;
}
