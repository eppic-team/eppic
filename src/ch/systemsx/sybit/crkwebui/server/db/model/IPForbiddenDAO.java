package ch.systemsx.sybit.crkwebui.server.db.model;

import ch.systemsx.sybit.crkwebui.shared.CrkWebException;

public interface IPForbiddenDAO 
{
	public boolean isIPForbidden(String ip) throws CrkWebException;
}
