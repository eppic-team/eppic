package eppic.db.dao;

import eppic.model.db.InterfaceResidueFeaturesDB;

import java.util.List;

public interface InterfaceResidueFeaturesDAO {

    void insertInterfResFeatures(List<InterfaceResidueFeaturesDB> list) throws DaoException;

    InterfaceResidueFeaturesDB getInterfResFeatures(String jobId, int interfId) throws DaoException;
}
