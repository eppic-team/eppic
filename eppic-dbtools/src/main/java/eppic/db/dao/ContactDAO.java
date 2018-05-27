package eppic.db.dao;

import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.model.Contact;
import ch.systemsx.sybit.crkwebui.shared.model.ContactsList;

/**
 * DAO for Contact
 * 
 * @author duarte_j
 *
 */
public interface ContactDAO {

	/**
	 * Retrieves list of contacts for specified interface.
	 * @param interfaceUid uid of interface item
	 * @return list of contacts for specified interface
	 * @throws DaoException when can not retrieve list of contacts
	 */
	public List<Contact> getContactsForInterface(int interfaceUid) throws DaoException;
	
	/**
	 * Retrieves list of contacts for all interfaces.
	 * @param jobId identifier of the job
	 * @return list of contacts for all interfaces
	 * @throws DaoException when can not retrieve list of contacts
	 */
	public ContactsList getContactsForAllInterfaces(String jobId) throws DaoException;
}

