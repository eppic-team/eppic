package eppic.db.dao;

import eppic.model.db.HitHspDB;

import java.util.List;

public interface HitHspDAO {

    void insertHitHsp(HitHspDB hitHspDB) throws DaoException;

    List<HitHspDB> getHitHspsForQueryId(String queryId) throws DaoException;
}
