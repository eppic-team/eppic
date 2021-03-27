package eppic.db.dao;

import java.util.List;
import java.util.Set;

import eppic.model.db.InterfaceClusterDB;

/**
 * DAO for Interface clusters
 * @author Nikhil Biyani
 * @author Jose Duarte
 *
 */
public interface InterfaceClusterDAO 
{

	/**
	 * Retrieves list of interface clusters for a given pdb info item.
	 * @param pdbInfoUid uid of pdb info item
	 * @param withScores include interface cluster scores
	 * @param withInterfaces include interfaces
	 * @return list of interface clusters
	 * @throws DaoException when interface cluster items can't be retrieved
	 */
	List<InterfaceClusterDB> getInterfaceClusters(int pdbInfoUid, boolean withScores, boolean withInterfaces) throws DaoException;

	/**
	 * Retrieves list of interface clusters for a given
	 * pdb info item and a list of interface cluster ids
	 * @param pdbInfoUid uid of pdb info item
	 * @param interfaceClusterIds list of interface cluster ids to retrieve
	 * @param withScores include interface cluster scores
	 * @param withInterfaces include interfaces
	 * @return list of interface clusters
	 * @throws DaoException when interface cluster items can't be retrieved
	 */
	List<InterfaceClusterDB> getInterfaceClusters(int pdbInfoUid, Set<Integer> interfaceClusterIds, boolean withScores, boolean withInterfaces) throws DaoException;
}
