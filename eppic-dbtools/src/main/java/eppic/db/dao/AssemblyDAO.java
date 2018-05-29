package eppic.db.dao;

import java.util.List;

import eppic.dtomodel.Assembly;

public interface AssemblyDAO {

	/**
	 * Gets the list of Assemblies for a given pdbInfoUid
	 * @param pdbInfoUid pdbInfo uid (db wide identifier)
	 * @param withScores include assembly scores
	 * @return the list of assemblies
	 */
	List<Assembly> getAssemblies(int pdbInfoUid, boolean withScores) throws DaoException;
}
