package eppic.db.dao;

import eppic.model.db.BlobDB;
import eppic.model.db.BlobIdentifierDB;

import java.util.List;

public interface BlobsDao {

    void insert(BlobIdentifierDB blobId, byte[] blob) throws DaoException;

    byte[] get(BlobIdentifierDB blobId) throws DaoException;

    List<BlobDB> getAll() throws DaoException;

    long remove(String entryId) throws DaoException;

    long countAll() throws DaoException;

}
