package ch.systemsx.sybit.server.db.entitylisteners;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import eppic.model.InterfaceDB;

/**
 * Entity listener for Interface used to properly handle NaN and null values.
 * @author AS
 *
 */
public class InterfaceListener
{
	@PrePersist
	public void prePersist(InterfaceDB interfaceItemDB)
	{
		if(Double.isNaN(interfaceItemDB.getArea()))
		{
				interfaceItemDB.setArea(-1);
		}
	}
	
	@PostLoad
	public void postLoad(InterfaceDB interfaceItemDB)
	{
		if(interfaceItemDB.getArea() < 0)
		{
			interfaceItemDB.setArea(Double.NaN);
		}
	}
}
