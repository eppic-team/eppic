package ch.systemsx.sybit.crkwebui.server.db.dao.jpa;

import java.util.List;
import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import ch.systemsx.sybit.crkwebui.server.db.dao.ContactDAO;
import ch.systemsx.sybit.crkwebui.shared.exceptions.DaoException;
import ch.systemsx.sybit.crkwebui.shared.model.Contact;
import ch.systemsx.sybit.crkwebui.shared.model.ContactsList;
import ch.systemsx.sybit.crkwebui.server.db.EntityManagerHandler;
import eppic.model.ContactDB;
import eppic.model.ContactDB_;
import eppic.model.InterfaceClusterDB;
import eppic.model.InterfaceClusterDB_;
import eppic.model.InterfaceDB_;
import eppic.model.JobDB_;
import eppic.model.PdbInfoDB_;
import eppic.model.InterfaceDB;
import eppic.model.JobDB;
import eppic.model.PdbInfoDB;

public class ContactDAOJpa implements ContactDAO {

	@Override
	public List<Contact> getContactsForInterface(int interfaceUid)
			throws DaoException {
		
		EntityManager entityManager = null;

		List<Contact> result = new ArrayList<Contact>();

		try	{
			
			entityManager = EntityManagerHandler.getEntityManager();

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<ContactDB> criteriaQuery = criteriaBuilder.createQuery(ContactDB.class);

			Root<ContactDB> contactRoot = criteriaQuery.from(ContactDB.class);
			Path<InterfaceDB> interfaceItem = contactRoot.get(ContactDB_.interfaceItem);
			criteriaQuery.where(criteriaBuilder.equal(interfaceItem.get(InterfaceDB_.uid), interfaceUid));

			Query query = entityManager.createQuery(criteriaQuery);

			@SuppressWarnings("unchecked")
			List<ContactDB> contactDBs = query.getResultList();

			for(ContactDB contactDB : contactDBs)	{
				
				result.add(Contact.create(contactDB));
			}
		}
		catch(Throwable e) {
			
			e.printStackTrace();
			throw new DaoException(e);
		}
		finally	{
			
			try	{
				entityManager.close();
			}
			catch(Throwable t) {
				t.printStackTrace();
			}
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

			Query query = entityManager.createQuery(criteriaQuery);

			@SuppressWarnings("unchecked")
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
			e.printStackTrace();
			throw new DaoException(e);
		}
		finally	{
			try	{
				entityManager.close();
			}
			catch(Throwable t) {
				t.printStackTrace();
			}
		}

		return contactsForInterfaces;
	}



}