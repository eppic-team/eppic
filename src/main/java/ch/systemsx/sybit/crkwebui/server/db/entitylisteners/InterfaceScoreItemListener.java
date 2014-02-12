package ch.systemsx.sybit.crkwebui.server.db.entitylisteners;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import eppic.model.InterfaceScoreItemDB;

/**
 * Entity listener for InterfaceScoreItem used to properly handle NaN and null values.
 * @author AS
 *
 */
public class InterfaceScoreItemListener
{
	@PrePersist
	public void prePersist(InterfaceScoreItemDB interfaceScoreItemDB)
	{
		if(interfaceScoreItemDB.getScore1() != null)
		{
			if(Double.isNaN(interfaceScoreItemDB.getScore1()))
			{
				interfaceScoreItemDB.setScore1(null);
			}
		}
		
		if(interfaceScoreItemDB.getScore2() != null)
		{
			if(Double.isNaN(interfaceScoreItemDB.getScore2()))
			{
				interfaceScoreItemDB.setScore2(null);
			}
		}
		
		if(interfaceScoreItemDB.getScore() != null)
		{
			if(Double.isNaN(interfaceScoreItemDB.getScore()))
			{
				interfaceScoreItemDB.setScore(null);
			}
		}
	}
	
	@PostLoad
	public void postLoad(InterfaceScoreItemDB interfaceScoreItemDB)
	{
		if(interfaceScoreItemDB.getScore1() == null)
		{
			interfaceScoreItemDB.setScore1(Double.NaN);
		}
		
		if(interfaceScoreItemDB.getScore2() == null)
		{
			interfaceScoreItemDB.setScore2(Double.NaN);
		}
		
		if(interfaceScoreItemDB.getScore() == null)
		{
			interfaceScoreItemDB.setScore(Double.NaN);
		}
	}
}
