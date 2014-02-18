package ch.systemsx.sybit.crkwebui.server.db.entitylisteners;

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
		//TODO 
		// Check the other way of doing this !!
//		if(interfaceScoreItemDB.getScore1() != null)
//		{
//			if(Double.isNaN(interfaceScoreItemDB.getScore1()))
//			{
//				interfaceScoreItemDB.setScore1(null);
//			}
//		}
//		
//		if(interfaceScoreItemDB.getScore2() != null)
//		{
//			if(Double.isNaN(interfaceScoreItemDB.getScore2()))
//			{
//				interfaceScoreItemDB.setScore2(null);
//			}
//		}
//		
//		if(interfaceScoreItemDB.getScore() != null)
//		{
//			if(Double.isNaN(interfaceScoreItemDB.getScore()))
//			{
//				interfaceScoreItemDB.setScore(null);
//			}
//		}
	}
	
	@PostLoad
	public void postLoad(InterfaceScoreDB interfaceScoreItemDB)
	{
//		if(interfaceScoreItemDB.getScore1() == null)
//		{
//			interfaceScoreItemDB.setScore1(Double.NaN);
//		}
//		
//		if(interfaceScoreItemDB.getScore2() == null)
//		{
//			interfaceScoreItemDB.setScore2(Double.NaN);
//		}
//		
//		if(interfaceScoreItemDB.getScore() == null)
//		{
//			interfaceScoreItemDB.setScore(Double.NaN);
//		}
	}
}
