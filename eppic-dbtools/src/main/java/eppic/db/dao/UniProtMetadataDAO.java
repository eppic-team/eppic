package eppic.db.dao;

import eppic.model.db.UniProtMetadataDB;

public interface UniProtMetadataDAO {

    void insertUniProtMetadata(String uniRefType, String version) throws DaoException;

    UniProtMetadataDB getUniProtMetadata() throws DaoException;
}
