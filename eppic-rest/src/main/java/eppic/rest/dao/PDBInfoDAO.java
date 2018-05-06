package eppic.rest.dao;

import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;
import eppic.model.PdbInfoDB;

/**
 * DAO for PDBScore item.
 * @author AS
 *
 */
public interface PDBInfoDAO 
{
	/**
	 * Retrieves pdb info item by job identifier.
	 * @param jobId identifier of the job
	 * @return pdb info item
	 * @throws DaoException when can not retrieve pdb info item for job
	 */
	PdbInfo getPDBInfo(String jobId) throws DaoException;

}
