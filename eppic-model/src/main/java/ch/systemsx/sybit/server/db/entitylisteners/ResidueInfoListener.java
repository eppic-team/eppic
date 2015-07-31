package ch.systemsx.sybit.server.db.entitylisteners;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;


import eppic.model.ResidueInfoDB;

/**
 * Entity listener for ResidueInfo used to properly handle NaN and null values.
 * 
 * @author duarte_j
 */
public class ResidueInfoListener {
	@PrePersist
	public void prePersist(ResidueInfoDB residueInfoDB)
	{
		
		if(Double.isNaN(residueInfoDB.getEntropyScore()))
		{
			residueInfoDB.setEntropyScore(-1);
		}

	}

	@PostLoad
	public void postLoad(ResidueInfoDB residueInfoDB)
	{

		if(residueInfoDB.getEntropyScore() < 0)
		{
			residueInfoDB.setEntropyScore(Double.NaN);
		}
	}
}
