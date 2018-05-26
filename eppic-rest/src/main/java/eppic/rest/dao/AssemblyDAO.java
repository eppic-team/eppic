package eppic.rest.dao;

import ch.systemsx.sybit.crkwebui.shared.model.Assembly;

import java.util.List;

public interface AssemblyDAO {

	/**
	 * Gets the list of Assemblies for a given pdbInfoUid
	 * @param pdbInfoUid
	 * @return
	 */
	public List<Assembly> getAssemblies(int pdbInfoUid) throws DaoException;
}
