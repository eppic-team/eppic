package eppic.db.dao;

import eppic.model.db.BlobIdentifierDB;

public interface BlobsDao {

    void insert(BlobIdentifierDB blobId, byte[] blob) throws DaoException;

    byte[] get(BlobIdentifierDB blobId) throws DaoException;

    long remove(String entryId) throws DaoException;

}
