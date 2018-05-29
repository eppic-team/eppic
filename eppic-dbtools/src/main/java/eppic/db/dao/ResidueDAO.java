package eppic.db.dao;

import java.util.List;

import eppic.model.dto.Residue;
import eppic.model.dto.ResiduesList;

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
	List<Residue> getResiduesForInterface(int interfaceUid) throws DaoException;
	
	/**
	 * Retrieves list of interface residue items for all interfaces.
	 * @param jobId identifier of the job
	 * @return list of interface residue items for all interfaces
	 * @throws DaoException when can not retrieve list of residue items
	 */
	ResiduesList getResiduesForAllInterfaces(String jobId) throws DaoException;
}
