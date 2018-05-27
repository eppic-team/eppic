package eppic.adapters;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import eppic.model.InterfaceScoreDB;

/**
 * Entity listener for InterfaceScore used to properly handle NaN and null values.
 * @author AS
 *
 */
public class InterfaceScoreListener
{
	@PrePersist
	public void prePersist(InterfaceScoreDB interfaceScoreItemDB)
	{
//		 TODO 
//		 Check the other way of doing this !!
		
			if(Double.isNaN(interfaceScoreItemDB.getScore1()))
			{
				interfaceScoreItemDB.setScore1(-1000000);
			}
		
		
		
			if(Double.isNaN(interfaceScoreItemDB.getScore2()))
			{
				interfaceScoreItemDB.setScore2(-1000000);
			}
		
		
		
			if(Double.isNaN(interfaceScoreItemDB.getScore()))
			{
				interfaceScoreItemDB.setScore(-1000000);
			}
		
	}
	
	@PostLoad
	public void postLoad(InterfaceScoreDB interfaceScoreItemDB)
	{
		if(interfaceScoreItemDB.getScore1() == -1000000)
		{
			interfaceScoreItemDB.setScore1(Double.NaN);
		}
		
		if(interfaceScoreItemDB.getScore2() == -1000000)
		{
			interfaceScoreItemDB.setScore2(Double.NaN);
		}
		
		if(interfaceScoreItemDB.getScore() == -1000000)
		{
			interfaceScoreItemDB.setScore(Double.NaN);
		}
	}
}
