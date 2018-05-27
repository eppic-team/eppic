package eppic.db.dao;

import java.util.List;

import eppic.dtomodel.Assembly;

public interface AssemblyDAO {

	/**
	 * Gets the list of Assemblies for a given pdbInfoUid
	 * @param pdbInfoUid
	 * @return
	 */
	public List<Assembly> getAssemblies(int pdbInfoUid) throws DaoException;
}
