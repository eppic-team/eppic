package ch.systemsx.sybit.crkwebui.server.db.model;

import ch.systemsx.sybit.crkwebui.shared.CrkWebException;

public interface IPAllowedDAO 
{
	public int getNrOfAllowedSubmissionsForIP(String ip) throws CrkWebException;
}
