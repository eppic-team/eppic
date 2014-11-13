package ch.systemsx.sybit.server.db.entitylisteners;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import eppic.model.ResidueDB;

/**
 * Entity listener for Residue used to properly handle NaN and null values.
 * @author AS
 *
 */
public class ResidueListener
{
	@PrePersist
	public void prePersist(ResidueDB interfaceResidueItemDB)
	{
		if(Double.isNaN(interfaceResidueItemDB.getBsa()))
		{
			interfaceResidueItemDB.setBsa(-1);
		}


		if(Double.isNaN(interfaceResidueItemDB.getAsa()))
		{
			interfaceResidueItemDB.setAsa(-1);
		}

		if(Double.isNaN(interfaceResidueItemDB.getEntropyScore()))
		{
			interfaceResidueItemDB.setEntropyScore(-1);
		}

	}

	@PostLoad
	public void postLoad(ResidueDB interfaceResidueItemDB)
	{
		if(interfaceResidueItemDB.getBsa() < 0)
		{
			interfaceResidueItemDB.setBsa(Double.NaN);
		}

		if(interfaceResidueItemDB.getAsa() < 0)
		{
			interfaceResidueItemDB.setAsa(Double.NaN);
		}

		if(interfaceResidueItemDB.getEntropyScore() < 0)
		{
			interfaceResidueItemDB.setEntropyScore(Double.NaN);
		}
	}
}
