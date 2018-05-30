package eppic.db.dao;

import java.util.List;

import eppic.model.dto.Contact;
import eppic.model.dto.ContactsList;

/**
 * DAO for Contact
 * 
 * @author duarte_j
 *
 */
public interface ContactDAO {

	/**
	 * Retrieves list of contacts for specified interface.
	 * @param pdbInfoUid uid of pdbInfo
	 * @param interfaceId interface id
	 * @return list of contacts for specified interface
	 * @throws DaoException when can not retrieve list of contacts
	 */
	List<Contact> getContactsForInterface(int pdbInfoUid, int interfaceId) throws DaoException;
	
	/**
	 * Retrieves list of contacts for all interfaces.
	 * @param jobId identifier of the job
	 * @return list of contacts for all interfaces
	 * @throws DaoException when can not retrieve list of contacts
	 */
	ContactsList getContactsForAllInterfaces(String jobId) throws DaoException;
}

