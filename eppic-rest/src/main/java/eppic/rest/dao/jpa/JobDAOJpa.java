package eppic.rest.dao.jpa;

import eppic.model.*;
import eppic.rest.dao.DaoException;
import eppic.rest.dao.JobDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.util.*;

/**
 * Implementation of JobDAO.
 * @author AS
 *
 */
public class JobDAOJpa implements JobDAO
{
	private static final Logger logger = LoggerFactory.getLogger(JobDAOJpa.class);



	@Override
	public JobDB getJob(String jobId) throws DaoException {
		// TODO implement
		//return getJob(EntityManagerHandler.getEntityManager(),jobId);
		return null;
	}
}
