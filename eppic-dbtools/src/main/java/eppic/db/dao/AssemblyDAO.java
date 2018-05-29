package eppic.db.dao;

import java.util.List;

import eppic.dtomodel.Assembly;

public interface AssemblyDAO {

	/**
	 * Gets the list of Assemblies for a given pdbInfoUid
	 * @param pdbInfoUid pdbInfo uid (db wide identifier)
	 * @param withScores include assembly scores
	 * @return the list of assemblies
	 * @throws DaoException when problems retrieving data from backend db
	 */
	List<Assembly> getAssemblies(int pdbInfoUid, boolean withScores) throws DaoException;

	/**
	 * Retrieves assembly data for the given pdbInfoUid and PDB assembly id
	 * @param pdbInfoUid pdbInfo uid (db wide identifier)
	 * @param pdbAssemblyId the PDB assembly id
	 * @return the assembly data or null if assembly with given PDB assembly id can't be found
	 * @throws DaoException when problems retrieving data from backend db
	 */
	Assembly getAssembly(int pdbInfoUid, int pdbAssemblyId) throws DaoException;
}
