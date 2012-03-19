package ch.systemsx.sybit.crkwebui.server.db.model;

import ch.systemsx.sybit.crkwebui.shared.CrkWebException;

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
	 * @throws CrkWebException
	 */
	public boolean isIPForbidden(String ip) throws CrkWebException;
}
