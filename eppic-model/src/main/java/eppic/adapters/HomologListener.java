package eppic.adapters;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import eppic.model.HomologDB;

/**
 * Entity listener for Homolog used to properly handle NaN and null values.
 * @author AS
 *
 */
public class HomologListener
{
	@PrePersist
	public void prePersist(HomologDB homologItemDB)
	{
		if(Double.isNaN(homologItemDB.getSeqId()))
		{
			homologItemDB.setSeqId(-1);
		}

		if(Double.isNaN(homologItemDB.getQueryCoverage()))
		{
			homologItemDB.setQueryCoverage(-1);
		}		

	}
	
	@PostLoad
	public void postLoad(HomologDB homologItemDB)
	{
		if(homologItemDB.getSeqId() < 0)
		{
			homologItemDB.setSeqId(Double.NaN);
		}
		
		if(homologItemDB.getQueryCoverage() < 0)
		{
			homologItemDB.setQueryCoverage(Double.NaN);
		}
		
		
	}
}
