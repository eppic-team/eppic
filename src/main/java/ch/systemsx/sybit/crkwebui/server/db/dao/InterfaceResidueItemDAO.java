package ch.systemsx.sybit.crkwebui.server.db.dao;

import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResidueItem;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceResiduesItemsList;

/**
 * DAO for InterfaceResidue item.
 * @author AS
 *
 */
public interface InterfaceResidueItemDAO {

	/**
	 * Retrieves list of interface residue items for specified interface.
	 * @param interfaceUid uid of interface item
	 * @return list of interface residue items for specified interface
	 * @throws DaoException when can not retrieve list of residue items
	 */
	public List<InterfaceResidueItem> getResiduesForInterface(int interfaceUid) throws DaoException;
	
	/**
	 * Retrieves list of interface residue items for all interfaces.
	 * @param jobId identifier of the job
	 * @return list of interface residue items for all interfaces
	 * @throws DaoException when can not retrieve list of residue items
	 */
	public InterfaceResiduesItemsList getResiduesForAllInterfaces(String jobId) throws DaoException;
}
