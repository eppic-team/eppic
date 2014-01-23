package ch.systemsx.sybit.crkwebui.server.db.entitylisteners;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import model.HomologItemDB;

/**
 * Entity listener for HomologItem used to properly handle NaN and null values.
 * @author AS
 *
 */
public class HomologItemListener
{
	@PrePersist
	public void prePersist(HomologItemDB homologItemDB)
	{
		if(homologItemDB.getSeqIdToQuery() != null)
		{
			if(Double.isNaN(homologItemDB.getSeqIdToQuery()))
			{
				homologItemDB.setSeqIdToQuery(null);
			}
		}
		
		if(homologItemDB.getQueryCov() != null)
		{
			if(Double.isNaN(homologItemDB.getQueryCov()))
			{
				homologItemDB.setQueryCov(null);
			}
		}		
		
	}
	
	@PostLoad
	public void postLoad(HomologItemDB homologItemDB)
	{
		if(homologItemDB.getSeqIdToQuery() == null)
		{
			homologItemDB.setSeqIdToQuery(Double.NaN);
		}
		
		if(homologItemDB.getQueryCov() == null)
		{
			homologItemDB.setQueryCov(Double.NaN);
		}
		
		
	}
}
