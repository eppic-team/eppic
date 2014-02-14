package ch.systemsx.sybit.crkwebui.server.db.dao;

import java.util.List;

import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;
import ch.systemsx.sybit.crkwebui.shared.model.PDBSearchResult;

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
	
	/**
	 * Retrieves a list of pdb score items from homologsinfoItem table having a particular uniprot id
	 * @param uniProtId
	 * @return
	 * @throws DaoException
	 */
	public List<PDBSearchResult> getPdbSearchItemsForUniProt(String uniProtId) throws DaoException;
}
