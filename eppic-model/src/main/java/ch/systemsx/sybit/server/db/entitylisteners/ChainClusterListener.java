package ch.systemsx.sybit.server.db.entitylisteners;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import eppic.model.ChainClusterDB;

/**
 * Entity listener for ChainCluster used to properly handle NaN and null values.
 * @author AS
 *
 */
public class ChainClusterListener
{
	@PrePersist
	public void prePersist(ChainClusterDB homologsInfoItemDB)
	{

		if(Double.isNaN(homologsInfoItemDB.getSeqIdCutoff()))
		{
			homologsInfoItemDB.setSeqIdCutoff(-1);
		}


	}
	
	@PostLoad
	public void postLoad(ChainClusterDB homologsInfoItemDB)
	{
		if(homologsInfoItemDB.getSeqIdCutoff() < 0)
		{
			homologsInfoItemDB.setSeqIdCutoff(Double.NaN);
		}
		
	}
}
