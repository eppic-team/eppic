package ch.systemsx.sybit.crkwebui.server.db.model;

import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;

/**
 * DAO for HomologsInfo item.
 * @author AS
 *
 */
public interface HomologsInfoItemDAO 
{
	/**
	 * Retrieves list of homologs info items for pdb score item.
	 * @param pdbScoreUid uid of pdb score item
	 * @return list of homologs info items for pdb score item
	 * @throws CrkWebException
	 */
	public List<HomologsInfoItem> getHomologsInfoItems(int pdbScoreUid) throws CrkWebException;
}
