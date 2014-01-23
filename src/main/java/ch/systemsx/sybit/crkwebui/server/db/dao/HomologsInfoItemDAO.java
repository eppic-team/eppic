package ch.systemsx.sybit.crkwebui.server.db.dao;

import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
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
	 * @throws DaoException when can not retrieve homologs info items
	 */
	public List<HomologsInfoItem> getHomologsInfoItems(int pdbScoreUid) throws DaoException;
}
