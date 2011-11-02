package ch.systemsx.sybit.crkwebui.server.db.model;

import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.NumHomologsStringItem;

public interface NumHomologsStringsDAO 
{
	public List<NumHomologsStringItem> getNumHomologsStrings(int pdbScoreUid) throws CrkWebException;
}
