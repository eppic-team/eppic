package eppic.db.dao;

import eppic.model.db.ResidueInfoDB;

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
	List<ResidueInfoDB> getResiduesForInterface(int interfaceUid) throws DaoException;
	
}
