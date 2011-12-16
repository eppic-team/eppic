package ch.systemsx.sybit.crkwebui.server.db.entitylisteners;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import model.InterfaceResidueMethodItemDB;

public class InterfaceResidueMethodItemListener
{
	@PrePersist
	public void prePersist(InterfaceResidueMethodItemDB interfaceResidueMethodItemDB)
	{
		if(interfaceResidueMethodItemDB.getScore() != null)
		{
			if(Float.isNaN(interfaceResidueMethodItemDB.getScore()))
			{
				interfaceResidueMethodItemDB.setScore(null);
			}
		}
	}
	
	@PostLoad
	public void postLoad(InterfaceResidueMethodItemDB interfaceResidueMethodItemDB)
	{
		if(interfaceResidueMethodItemDB.getScore() == null)
		{
			interfaceResidueMethodItemDB.setScore(Float.NaN);
		}
	}
}
