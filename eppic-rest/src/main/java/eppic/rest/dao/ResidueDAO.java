package eppic.rest.dao;

import ch.systemsx.sybit.crkwebui.shared.model.Residue;
import ch.systemsx.sybit.crkwebui.shared.model.ResiduesList;

import java.util.List;

/**
 * DAO for InterfaceResidue item.
 * @author AS
 *
 */
public interface ResidueDAO {

	/**
	 * Retrieves list of interface residue items for specified interface.
	 * @param interfaceUid uid of interface item
	 * @return list of interface residue items for specified interface
	 * @throws DaoException when can not retrieve list of residue items
	 */
	public List<Residue> getResiduesForInterface(int interfaceUid) throws DaoException;
	
	/**
	 * Retrieves list of interface residue items for all interfaces.
	 * @param jobId identifier of the job
	 * @return list of interface residue items for all interfaces
	 * @throws DaoException when can not retrieve list of residue items
	 */
	public ResiduesList getResiduesForAllInterfaces(String jobId) throws DaoException;
}
