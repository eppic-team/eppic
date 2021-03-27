package eppic.db.dao.mongo;

import eppic.db.dao.ContactDAO;
import eppic.db.dao.DaoException;
import eppic.model.db.ContactDB;

import java.util.List;

public class ContactDAOMongo implements ContactDAO {
    @Override
    public List<ContactDB> getContactsForInterface(int pdbInfoUid, int interfaceId) throws DaoException {
        return null;
    }
}
