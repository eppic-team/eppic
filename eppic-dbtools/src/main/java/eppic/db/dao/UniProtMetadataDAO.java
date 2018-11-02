package eppic.db.dao;

import eppic.model.dto.UniProtMetadata;

public interface UniProtMetadataDAO {

    void insertUniProtMetadata(String uniRefType, String version) throws DaoException;

    UniProtMetadata getUniProtMetadata() throws DaoException;
}
