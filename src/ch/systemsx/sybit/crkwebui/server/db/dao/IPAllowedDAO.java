package ch.systemsx.sybit.crkwebui.server.db.dao;

import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;

/**
 * DAO interface for IPAllowed.
 * @author AS
 *
 */
public interface IPAllowedDAO 
{
	/**
	 * Retrieves number of allowed submissions for specified ip.
	 * @param ip ip address
	 * @return number of allowed submissions for specified ip
	 * @throws DaoException when can not retrieve information from data storage
	 */
	public int getNrOfAllowedSubmissionsForIP(String ip) throws DaoException;
}
