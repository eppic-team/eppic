package eppic.db.dao;

import eppic.model.db.InterfaceResidueFeaturesDB;


/**
 * DAO for reading InterfaceResidueFeaturesDB. For writing see {@link EntryDAO}.
 */
public interface InterfaceResidueFeaturesDAO {

    InterfaceResidueFeaturesDB getInterfResFeatures(String entryId, int interfId) throws DaoException;
}
