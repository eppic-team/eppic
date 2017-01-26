package ch.systemsx.sybit.server.db.entitylisteners;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import eppic.model.ResidueBurialDB;

/**
 * Entity listener for ResidueBurial used to properly handle NaN and null values.
 * @author AS
 *
 */
public class ResidueBurialListener
{
	@PrePersist
	public void prePersist(ResidueBurialDB residueBurialDB)
	{
		if(Double.isNaN(residueBurialDB.getBsa()))
		{
			residueBurialDB.setBsa(-1);
		}


		if(Double.isNaN(residueBurialDB.getAsa()))
		{
			residueBurialDB.setAsa(-1);
		}


	}

	@PostLoad
	public void postLoad(ResidueBurialDB residueBurialDB)
	{
		if(residueBurialDB.getBsa() < 0)
		{
			residueBurialDB.setBsa(Double.NaN);
		}

		if(residueBurialDB.getAsa() < 0)
		{
			residueBurialDB.setAsa(Double.NaN);
		}

	}
}
