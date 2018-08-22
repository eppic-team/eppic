package eppic.db.dao;

import java.util.List;

import eppic.model.dto.Assembly;

public interface AssemblyDAO {

	/**
	 * Gets the list of Assemblies for a given pdbInfoUid
	 * @param pdbInfoUid pdbInfo uid (db wide identifier)
	 * @param withScores include assembly scores
	 * @param withGraph include graph nodes and edges
	 * @return the list of assemblies
	 * @throws DaoException when problems retrieving data from backend db
	 */
	List<Assembly> getAssemblies(int pdbInfoUid, boolean withScores, boolean withGraph) throws DaoException;

	/**
	 * Retrieves assembly data for the given pdbInfoUid and PDB assembly id
	 * @param pdbInfoUid pdbInfo uid (db wide identifier)
	 * @param pdbAssemblyId the PDB assembly id
	 * @param withGraph include graph nodes and edges
	 * @return the assembly data or null if assembly with given PDB assembly id can't be found
	 * @throws DaoException when problems retrieving data from backend db
	 */
	Assembly getAssemblyByPdbAssemblyId(int pdbInfoUid, int pdbAssemblyId, boolean withGraph) throws DaoException;

	/**
	 * Retrieves assembly data for the given pdbInfoUid and eppic assembly id
	 * @param pdbInfoUid pdbInfo uid (db wide identifier)
	 * @param assemblyId the eppic assembly id
	 * @param withGraph include graph nodes and edges
	 * @return the assembly data or null if assembly with given assembly id can't be found
	 * @throws DaoException when problems retrieving data from backend db
	 */
	Assembly getAssembly(int pdbInfoUid, int assemblyId, boolean withGraph) throws DaoException;
}
