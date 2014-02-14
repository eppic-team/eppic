package ch.systemsx.sybit.crkwebui.server.db.entitylisteners;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import eppic.model.HomologDB;

/**
 * Entity listener for HomologItem used to properly handle NaN and null values.
 * @author AS
 *
 */
public class HomologItemListener
{
	@PrePersist
	public void prePersist(HomologDB homologItemDB)
	{
		if(homologItemDB.getSeqId() != null)
		{
			if(Double.isNaN(homologItemDB.getSeqId()))
			{
				homologItemDB.setSeqId(null);
			}
		}
		
		if(homologItemDB.getQueryCoverage() != null)
		{
			if(Double.isNaN(homologItemDB.getQueryCoverage()))
			{
				homologItemDB.setQueryCoverage(null);
			}
		}		
		
	}
	
	@PostLoad
	public void postLoad(HomologDB homologItemDB)
	{
		if(homologItemDB.getSeqId() == null)
		{
			homologItemDB.setSeqId(Double.NaN);
		}
		
		if(homologItemDB.getQueryCoverage() == null)
		{
			homologItemDB.setQueryCoverage(Double.NaN);
		}
		
		
	}
}
