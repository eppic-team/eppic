package ch.systemsx.sybit.crkwebui.server.db.entitylisteners;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import eppic.model.ChainClusterDB;

/**
 * Entity listener for HomologsInfoItem used to properly handle NaN and null values.
 * @author AS
 *
 */
public class HomologsInfoItemListener
{
	@PrePersist
	public void prePersist(ChainClusterDB homologsInfoItemDB)
	{
		if(homologsInfoItemDB.getSeqIdCutoff() != null)
		{
			if(Double.isNaN(homologsInfoItemDB.getSeqIdCutoff()))
			{
				homologsInfoItemDB.setSeqIdCutoff(null);
			}
		}
		
	}
	
	@PostLoad
	public void postLoad(ChainClusterDB homologsInfoItemDB)
	{
		if(homologsInfoItemDB.getSeqIdCutoff() == null)
		{
			homologsInfoItemDB.setSeqIdCutoff(Double.NaN);
		}
		
	}
}
