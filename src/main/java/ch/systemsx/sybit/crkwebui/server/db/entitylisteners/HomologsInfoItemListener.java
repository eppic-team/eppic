package ch.systemsx.sybit.crkwebui.server.db.entitylisteners;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import model.HomologsInfoItemDB;

/**
 * Entity listener for HomologsInfoItem used to properly handle NaN and null values.
 * @author AS
 *
 */
public class HomologsInfoItemListener
{
	@PrePersist
	public void prePersist(HomologsInfoItemDB homologsInfoItemDB)
	{
		if(homologsInfoItemDB.getIdCutoffUsed() != null)
		{
			if(Double.isNaN(homologsInfoItemDB.getIdCutoffUsed()))
			{
				homologsInfoItemDB.setIdCutoffUsed(null);
			}
		}
		
	}
	
	@PostLoad
	public void postLoad(HomologsInfoItemDB homologsInfoItemDB)
	{
		if(homologsInfoItemDB.getIdCutoffUsed() == null)
		{
			homologsInfoItemDB.setIdCutoffUsed(Double.NaN);
		}
		
	}
}
