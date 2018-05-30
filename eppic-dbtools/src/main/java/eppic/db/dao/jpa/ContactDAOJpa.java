package eppic.db.dao.jpa;

import java.util.List;
import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import eppic.model.dto.Contact;
import eppic.model.dto.ContactsList;
import eppic.db.EntityManagerHandler;
import eppic.db.dao.ContactDAO;
import eppic.db.dao.DaoException;
import eppic.model.db.ContactDB;
import eppic.model.db.ContactDB_;
import eppic.model.db.InterfaceClusterDB;
import eppic.model.db.InterfaceClusterDB_;
import eppic.model.db.InterfaceDB_;
import eppic.model.db.JobDB_;
import eppic.model.db.PdbInfoDB_;
import eppic.model.db.InterfaceDB;
import eppic.model.db.JobDB;
import eppic.model.db.PdbInfoDB;

public class ContactDAOJpa implements ContactDAO {

	@Override
	public List<Contact> getContactsForInterface(int pdbInfoUid, int interfaceId)
			throws DaoException {
		
		EntityManager entityManager = null;

		List<Contact> result = new ArrayList<Contact>();

		try	{
			
			entityManager = EntityManagerHandler.getEntityManager();

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<InterfaceClusterDB> criteriaQuery = criteriaBuilder.createQuery(InterfaceClusterDB.class);

			Root<InterfaceClusterDB> interfaceClusterRoot = criteriaQuery.from(InterfaceClusterDB.class);
			Path<PdbInfoDB> pdbInfoDB = interfaceClusterRoot.get(InterfaceClusterDB_.pdbInfo);
			criteriaQuery.where(criteriaBuilder.equal(pdbInfoDB.get(PdbInfoDB_.uid), pdbInfoUid));

			TypedQuery<InterfaceClusterDB> query = entityManager.createQuery(criteriaQuery);

			List<InterfaceClusterDB> interfaceClusterDBs = query.getResultList();

			int interfUid = -1;

			outer:
			for(InterfaceClusterDB interfaceClusterDB : interfaceClusterDBs) {

				for (InterfaceDB interfaceDB : interfaceClusterDB.getInterfaces()) {
					if (interfaceDB.getInterfaceId() == interfaceId) {
						interfUid = interfaceDB.getUid();
						break outer;
					}
				}
			}

			if (interfUid == -1) {
				return null;
			}

			CriteriaQuery<ContactDB> contactDBCriteriaQuery = criteriaBuilder.createQuery(ContactDB.class);

			Root<ContactDB> contactRoot = contactDBCriteriaQuery.from(ContactDB.class);
			Path<InterfaceDB> interfaceItem = contactRoot.get(ContactDB_.interfaceItem);
			contactDBCriteriaQuery.where(criteriaBuilder.equal(interfaceItem.get(InterfaceDB_.uid), interfUid));

			TypedQuery<ContactDB> contactDBTypedQuery = entityManager.createQuery(contactDBCriteriaQuery);

			List<ContactDB> contactDBs = contactDBTypedQuery.getResultList();

			for(ContactDB contactDB : contactDBs)	{
				
				result.add(Contact.create(contactDB));
			}
		}
		catch(Throwable e) {

			throw new DaoException(e);
		}
		finally	{

			if (entityManager!=null)
				entityManager.close();
		}

		return result;

	}

	@Override
	public ContactsList getContactsForAllInterfaces(String jobId)
			throws DaoException {
		EntityManager entityManager = null;

		ContactsList contactsForInterfaces = new ContactsList();

		try	{
			entityManager = EntityManagerHandler.getEntityManager();

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<ContactDB> criteriaQuery = criteriaBuilder.createQuery(ContactDB.class);

			Root<ContactDB> contactRoot = criteriaQuery.from(ContactDB.class);
			Path<InterfaceDB> interfaceItem = contactRoot.get(ContactDB_.interfaceItem);
			Path<InterfaceClusterDB> interfaceClusterItem = interfaceItem.get(InterfaceDB_.interfaceCluster);
			Path<PdbInfoDB> pdbScoreItem = interfaceClusterItem.get(InterfaceClusterDB_.pdbInfo);
			Path<JobDB> jobItem = pdbScoreItem.get(PdbInfoDB_.job);
			criteriaQuery.where(criteriaBuilder.equal(jobItem.get(JobDB_.jobId), jobId));

			TypedQuery<ContactDB> query = entityManager.createQuery(criteriaQuery);

			List<ContactDB> contactDBs = query.getResultList();

			for(ContactDB contactDB : contactDBs) {
				
				if(contactsForInterfaces.get(contactDB.getInterfaceItem().getInterfaceId()) == null) {
					
					List<Contact> contacts = new ArrayList<Contact>();

					contactsForInterfaces.put(contactDB.getInterfaceItem().getInterfaceId(), contacts);


				}

				Contact contact = Contact.create(contactDB);

				contactsForInterfaces.get(contactDB.getInterfaceItem().getInterfaceId()).add(contact);
				

			}
		}
		catch(Throwable e) {
			throw new DaoException(e);
		}
		finally	{
			if (entityManager!=null)
				entityManager.close();
		}

		return contactsForInterfaces;
	}



}