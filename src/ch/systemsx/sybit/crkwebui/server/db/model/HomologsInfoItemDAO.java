package ch.systemsx.sybit.crkwebui.server.db.model;

import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;

public interface HomologsInfoItemDAO 
{
	public List<HomologsInfoItem> getHomologsInfoItems(int pdbScoreUid) throws CrkWebException;
}
