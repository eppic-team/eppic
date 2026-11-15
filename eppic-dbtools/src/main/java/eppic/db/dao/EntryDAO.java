package eppic.db.dao;

import eppic.db.loaders.EntryData;
import eppic.model.db.InterfaceResidueFeaturesDB;
import eppic.model.db.PdbInfoDB;

import java.util.List;

/**
 * DAO for writing and removing entries as a whole: an entry is its {@link PdbInfoDB}, with
 * everything that hangs from it, and its {@link InterfaceResidueFeaturesDB}s. These are several
 * documents in the db, in several collections, but they are only ever written and removed
 * together, through here.
 * <p>
 * For reading see {@link PDBInfoDAO} and {@link InterfaceResidueFeaturesDAO}.
 */
public interface EntryDAO {

	/**
	 * Writes the given entry, replacing whatever the db has for its entry id. Either all of the
	 * entry is written or none of it is.
	 * @param pdbInfo the entry
	 * @param interfResFeatures the residue features of the interfaces of the entry
	 * @throws DaoException if the entry could not be written
	 */
	void write(PdbInfoDB pdbInfo, List<InterfaceResidueFeaturesDB> interfResFeatures) throws DaoException;

	/**
	 * Inserts the given entries, none of which must be in the db. This is the fast way of
	 * loading many entries into a db that does not have them. If it fails some of the entries
	 * can have been inserted, but none of them partially.
	 * @param entries the entries
	 * @throws DaoException if not all entries could be inserted
	 */
	void insertAll(List<EntryData> entries) throws DaoException;

	/**
	 * Removes all the data of the given entry
	 * @param entryId the identifier of the entry
	 * @return true if the entry was in the db, false if it was not
	 * @throws DaoException if the entry could not be removed
	 */
	boolean remove(String entryId) throws DaoException;
}
