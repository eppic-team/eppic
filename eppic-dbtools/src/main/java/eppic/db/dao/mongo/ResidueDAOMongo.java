package eppic.db.dao.mongo;

import eppic.db.dao.DaoException;
import eppic.db.dao.ResidueDAO;
import eppic.model.db.ResidueInfoDB;

import java.util.List;

public class ResidueDAOMongo implements ResidueDAO {
    @Override
    public List<ResidueInfoDB> getResiduesForInterface(int interfaceUid) throws DaoException {
        return null;
    }
}
