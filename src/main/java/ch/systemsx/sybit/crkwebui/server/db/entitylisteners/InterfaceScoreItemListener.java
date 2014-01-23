package ch.systemsx.sybit.crkwebui.server.db.entitylisteners;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import model.InterfaceScoreItemDB;

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
		if(interfaceScoreItemDB.getUnweightedRatio1Scores() != null)
		{
			if(Double.isNaN(interfaceScoreItemDB.getUnweightedRatio1Scores()))
			{
				interfaceScoreItemDB.setUnweightedRatio1Scores(null);
			}
		}
		
		if(interfaceScoreItemDB.getUnweightedRatio2Scores() != null)
		{
			if(Double.isNaN(interfaceScoreItemDB.getUnweightedRatio2Scores()))
			{
				interfaceScoreItemDB.setUnweightedRatio2Scores(null);
			}
		}
		
		if(interfaceScoreItemDB.getUnweightedCore1Scores() != null)
		{
			if(Double.isNaN(interfaceScoreItemDB.getUnweightedCore1Scores()))
			{
				interfaceScoreItemDB.setUnweightedCore1Scores(null);
			}
		}
		
		if(interfaceScoreItemDB.getUnweightedCore2Scores() != null)
		{
			if(Double.isNaN(interfaceScoreItemDB.getUnweightedCore2Scores()))
			{
				interfaceScoreItemDB.setUnweightedCore2Scores(null);
			}
		}
		
		if(interfaceScoreItemDB.getUnweightedRim1Scores() != null)
		{
			if(Double.isNaN(interfaceScoreItemDB.getUnweightedRim1Scores()))
			{
				interfaceScoreItemDB.setUnweightedRim1Scores(null);
			}
		}
		
		if(interfaceScoreItemDB.getUnweightedRim2Scores() != null)
		{
			if(Double.isNaN(interfaceScoreItemDB.getUnweightedRim2Scores()))
			{
				interfaceScoreItemDB.setUnweightedRim2Scores(null);
			}
		}
		
		if(interfaceScoreItemDB.getUnweightedFinalScores() != null)
		{
			if(Double.isNaN(interfaceScoreItemDB.getUnweightedFinalScores()))
			{
				interfaceScoreItemDB.setUnweightedFinalScores(null);
			}
		}
	}
	
	@PostLoad
	public void postLoad(InterfaceScoreItemDB interfaceScoreItemDB)
	{
		if(interfaceScoreItemDB.getUnweightedRatio1Scores() == null)
		{
			interfaceScoreItemDB.setUnweightedRatio1Scores(Double.NaN);
		}
		
		if(interfaceScoreItemDB.getUnweightedRatio2Scores() == null)
		{
			interfaceScoreItemDB.setUnweightedRatio2Scores(Double.NaN);
		}
		
		if(interfaceScoreItemDB.getUnweightedCore1Scores() == null)
		{
			interfaceScoreItemDB.setUnweightedCore1Scores(Double.NaN);
		}
		
		if(interfaceScoreItemDB.getUnweightedCore2Scores() == null)
		{
			interfaceScoreItemDB.setUnweightedCore2Scores(Double.NaN);
		}
		
		if(interfaceScoreItemDB.getUnweightedRim1Scores() == null)
		{
			interfaceScoreItemDB.setUnweightedRim1Scores(Double.NaN);
		}
		
		if(interfaceScoreItemDB.getUnweightedRim2Scores() == null)
		{
			interfaceScoreItemDB.setUnweightedRim2Scores(Double.NaN);
		}
		
		if(interfaceScoreItemDB.getUnweightedFinalScores() == null)
		{
			interfaceScoreItemDB.setUnweightedFinalScores(Double.NaN);
		}
	}
}
