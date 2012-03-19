package ch.systemsx.sybit.crkwebui.server.db.model;

import ch.systemsx.sybit.crkwebui.shared.CrkWebException;

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
	 * @throws CrkWebException
	 */
	public int getNrOfAllowedSubmissionsForIP(String ip) throws CrkWebException;
}
