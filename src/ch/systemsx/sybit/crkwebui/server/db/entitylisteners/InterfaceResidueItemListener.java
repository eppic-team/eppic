package ch.systemsx.sybit.crkwebui.server.db.entitylisteners;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import model.InterfaceResidueItemDB;

public class InterfaceResidueItemListener
{
	@PrePersist
	public void prePersist(InterfaceResidueItemDB interfaceResidueItemDB)
	{
		if(interfaceResidueItemDB.getBsaPercentage() != null)
		{
			if(Float.isNaN(interfaceResidueItemDB.getBsaPercentage()))
			{
				interfaceResidueItemDB.setBsaPercentage(null);
			}
		}
		
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
	}
	
	@PostLoad
	public void postLoad(InterfaceResidueItemDB interfaceResidueItemDB)
	{
		if(interfaceResidueItemDB.getBsaPercentage() == null)
		{
			interfaceResidueItemDB.setBsaPercentage(Float.NaN);
		}
		
		if(interfaceResidueItemDB.getBsa() == null)
		{
			interfaceResidueItemDB.setBsa(Float.NaN);
		}
		
		if(interfaceResidueItemDB.getAsa() == null)
		{
			interfaceResidueItemDB.setAsa(Float.NaN);
		}
	}
}
