package eppic.db.dao;

import java.util.List;

import eppic.dtomodel.InterfaceCluster;

/**
 * DAO for Interface clusters
 * @author biyani_n
 *
 */
public interface InterfaceClusterDAO 
{

	/**
	 * Retrieves list of interface clusters with scores and without interfaces for a given pdb info item.
	 * @param pdbInfoUid uid of pdb info item
	 * @return list of interface clusters without interfaces for pdb info item
	 * @throws DaoException when interface cluster items can't be retrieved
	 */
	List<InterfaceCluster> getInterfaceClustersWithoutInterfaces(int pdbInfoUid) throws DaoException;

	/**
	 * Retrieves list of interface clusters with scores and without interfaces for a given 
	 * pdb info item and a list of interface cluster ids
	 * @param pdbInfoUid uid of pdb info item
	 * @param interfaceClusterIds
	 * @return
	 * @throws DaoException
	 */
	List<InterfaceCluster> getInterfaceClustersWithoutInterfaces(int pdbInfoUid, List<Integer> interfaceClusterIds) throws DaoException;
}
