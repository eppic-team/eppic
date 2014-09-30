package ch.systemsx.sybit.crkwebui.server.db.dao;

import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;

/**
 * DAO for IPForbidden.
 * @author AS
 *
 */
public interface IPForbiddenDAO 
{
	/**
	 * Retrieves information whether user with specified ip address can not submit job.
	 * @param ip ip address
	 * @return information whether user with specified ip address can not submit job
	 * @throws DaoException when can not retrieve information from data storage
	 */
	public boolean isIPForbidden(String ip) throws DaoException;
}
