package eppic.db.dao;

import eppic.model.db.UniProtInfoDB;

import java.util.List;

public interface UniProtInfoDAO {

    void insertUniProtInfo(String uniId, String sequence, String firstTaxon, String lastTaxon) throws DaoException;

    void insertUniProtInfos(List<UniProtInfoDB> uniProtInfoDBList) throws DaoException;

    /**
     * Retrieve the UniProt info object or null if no such uniId found.
     * @param uniId the UniProt or UniParc id
     * @return
     * @throws DaoException
     */
    UniProtInfoDB getUniProtInfo(String uniId) throws DaoException;
}
