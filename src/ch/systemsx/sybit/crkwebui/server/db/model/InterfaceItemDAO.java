package ch.systemsx.sybit.crkwebui.server.db.model;

import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.InterfaceItem;

public interface InterfaceItemDAO 
{
	public List<InterfaceItem> getInterfacesWithScores(int pdbScoreUid) throws CrkWebException;
	
	public List<InterfaceItem> getInterfacesWithResidues(int pdbScoreUid) throws CrkWebException;
}
