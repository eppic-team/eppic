package ch.systemsx.sybit.crkwebui.server.db.entitylisteners;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import model.InterfaceResidueItemDB;

/**
 * Entity listener for InterfaceResidueItem used to properly handle NaN and null values.
 * @author AS
 *
 */
public class InterfaceResidueItemListener
{
	@PrePersist
	public void prePersist(InterfaceResidueItemDB interfaceResidueItemDB)
	{
		if(interfaceResidueItemDB.getBsa() != null)
		{
			if(Float.isNaN(interfaceResidueItemDB.getBsa()))
			{
				interfaceResidueItemDB.setBsa(null);
			}
		}
		
		if(interfaceResidueItemDB.getAsa() != null)
		{
			if(Float.isNaN(interfaceResidueItemDB.getAsa()))
			{
				interfaceResidueItemDB.setAsa(null);
			}
		}
		
		if(interfaceResidueItemDB.getEntropyScore() != null)
		{
			if(Float.isNaN(interfaceResidueItemDB.getEntropyScore()))
			{
				interfaceResidueItemDB.setEntropyScore(null);
			}
		}
	}
	
	@PostLoad
	public void postLoad(InterfaceResidueItemDB interfaceResidueItemDB)
	{
		if(interfaceResidueItemDB.getBsa() == null)
		{
			interfaceResidueItemDB.setBsa(Float.NaN);
		}
		
		if(interfaceResidueItemDB.getAsa() == null)
		{
			interfaceResidueItemDB.setAsa(Float.NaN);
		}
		
		if(interfaceResidueItemDB.getEntropyScore() == null)
		{
			interfaceResidueItemDB.setEntropyScore(Float.NaN);
		}
	}
}
