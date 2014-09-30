package ch.systemsx.sybit.crkwebui.server.db.dao;

import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceCluster;

/**
 * DAO for Interface clusters
 * @author biyani_n
 *
 */
public interface InterfaceClusterDAO 
{

	/**
	 * Retrieves list of interface clusters with scores  and withour interfaces for pdb score item.
	 * @param pdbInfoUid uid of pdb score item
	 * @return list of interface clusters without interfaces for pdb score item
	 * @throws DaoException when can not retrieve interface items
	 */
	public List<InterfaceCluster> getInterfaceClustersWithoutInterfaces(int pdbInfoUid) throws DaoException;
	
}
