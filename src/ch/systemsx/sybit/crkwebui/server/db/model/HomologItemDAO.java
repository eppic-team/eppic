package ch.systemsx.sybit.crkwebui.server.db.model;

import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.HomologItem;

public interface HomologItemDAO {

	/**
	 * Retrieves list of homologs items for homolog info item.
	 * @param homologInfoItemUid uid of homolog info item
	 * @return list of homolog items for homolog info item
	 * @throws CrkWebException
	 */
	public List<HomologItem> getHomologsInfoItems(int homologInfoItemUid) throws CrkWebException;
	
}
