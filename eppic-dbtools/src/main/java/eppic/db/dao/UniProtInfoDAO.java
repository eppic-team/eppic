package eppic.db.dao;

import eppic.model.dto.UniProtInfo;

public interface UniProtInfoDAO {

    void insertUniProtInfo(String uniId, String sequence, String firstTaxon, String lastTaxon) throws DaoException;

    /**
     * Retrieve the UniProt info object or null if no such uniId found.
     * @param uniId the UniProt or UniParc id
     * @return
     * @throws DaoException
     */
    UniProtInfo getUniProtInfo(String uniId) throws DaoException;
}
