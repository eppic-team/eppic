package ch.systemsx.sybit.crkwebui.server.db.entitylisteners;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import model.InterfaceItemDB;

/**
 * Entity listener for InterfaceItem used to properly handle NaN and null values.
 * @author AS
 *
 */
public class InterfaceItemListener
{
	@PrePersist
	public void prePersist(InterfaceItemDB interfaceItemDB)
	{
		if(interfaceItemDB.getAsaC1() != null)
		{
			if(Double.isNaN(interfaceItemDB.getAsaC1()))
			{
				interfaceItemDB.setAsaC1(null);
			}
		}
		
		if(interfaceItemDB.getAsaC2() != null)
		{
			if(Double.isNaN(interfaceItemDB.getAsaC2()))
			{
				interfaceItemDB.setAsaC2(null);
			}
		}
		
		if(interfaceItemDB.getAsaR1() != null)
		{
			if(Double.isNaN(interfaceItemDB.getAsaR1()))
			{
				interfaceItemDB.setAsaR1(null);
			}
		}
		
		if(interfaceItemDB.getAsaR2() != null)
		{
			if(Double.isNaN(interfaceItemDB.getAsaR2()))
			{
				interfaceItemDB.setAsaR2(null);
			}
		}
		
		if(interfaceItemDB.getBsaC1() != null)
		{
			if(Double.isNaN(interfaceItemDB.getBsaC1()))
			{
				interfaceItemDB.setBsaC1(null);
			}
		}
		
		if(interfaceItemDB.getBsaC2() != null)
		{
			if(Double.isNaN(interfaceItemDB.getBsaC2()))
			{
				interfaceItemDB.setBsaC2(null);
			}
		}
		
		if(interfaceItemDB.getBsaR1() != null)
		{
			if(Double.isNaN(interfaceItemDB.getBsaR1()))
			{
				interfaceItemDB.setBsaR1(null);
			}
		}
		
		if(interfaceItemDB.getBsaR2() != null)
		{
			if(Double.isNaN(interfaceItemDB.getBsaR2()))
			{
				interfaceItemDB.setBsaR2(null);
			}
		}
		
		if(interfaceItemDB.getArea() != null)
		{
			if(Double.isNaN(interfaceItemDB.getArea()))
			{
				interfaceItemDB.setArea(null);
			}
		}
	}
	
	@PostLoad
	public void postLoad(InterfaceItemDB interfaceItemDB)
	{
		
		if(interfaceItemDB.getAsaC1() == null)
		{
			interfaceItemDB.setAsaC1(Double.NaN);
		}
		
		if(interfaceItemDB.getAsaC2() == null)
		{
			interfaceItemDB.setAsaC2(Double.NaN);
		}
		
		if(interfaceItemDB.getAsaR1() == null)
		{
			interfaceItemDB.setAsaR1(Double.NaN);
		}
		
		if(interfaceItemDB.getAsaR2() == null)
		{
			interfaceItemDB.setAsaR2(Double.NaN);
		}
		
		if(interfaceItemDB.getBsaC1() == null)
		{
			interfaceItemDB.setBsaC1(Double.NaN);
		}
		
		if(interfaceItemDB.getBsaC2() == null)
		{
			interfaceItemDB.setBsaC2(Double.NaN);
		}
		
		if(interfaceItemDB.getBsaR1() == null)
		{
			interfaceItemDB.setBsaR1(Double.NaN);
		}
		
		if(interfaceItemDB.getBsaR2() == null)
		{
			interfaceItemDB.setBsaR2(Double.NaN);
		}
		
		if(interfaceItemDB.getArea() == null)
		{
			interfaceItemDB.setArea(Double.NaN);
		}
	}
}
