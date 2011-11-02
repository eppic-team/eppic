package ch.systemsx.sybit.crkwebui.server.db.entitylisteners;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import model.InterfaceScoreItemDB;

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
		
		if(interfaceScoreItemDB.getWeightedRatio1Scores() != null)
		{
			if(Double.isNaN(interfaceScoreItemDB.getWeightedRatio1Scores()))
			{
				interfaceScoreItemDB.setWeightedRatio1Scores(null);
			}
		}
		
		if(interfaceScoreItemDB.getWeightedRatio2Scores() != null)
		{
			if(Double.isNaN(interfaceScoreItemDB.getWeightedRatio2Scores()))
			{
				interfaceScoreItemDB.setWeightedRatio2Scores(null);
			}
		}
		
		if(interfaceScoreItemDB.getWeightedCore1Scores() != null)
		{
			if(Double.isNaN(interfaceScoreItemDB.getWeightedCore1Scores()))
			{
				interfaceScoreItemDB.setWeightedCore1Scores(null);
			}
		}
		
		if(interfaceScoreItemDB.getWeightedCore2Scores() != null)
		{
			if(Double.isNaN(interfaceScoreItemDB.getWeightedCore2Scores()))
			{
				interfaceScoreItemDB.setWeightedCore2Scores(null);
			}
		}
			
		if(interfaceScoreItemDB.getWeightedRim1Scores() != null)
		{
			if(Double.isNaN(interfaceScoreItemDB.getWeightedRim1Scores()))
			{
				interfaceScoreItemDB.setWeightedRim1Scores(null);
			}
		}
		
		if(interfaceScoreItemDB.getWeightedRim2Scores() != null)
		{
			if(Double.isNaN(interfaceScoreItemDB.getWeightedRim2Scores()))
			{
				interfaceScoreItemDB.setWeightedRim2Scores(null);
			}
		}
		
		if(interfaceScoreItemDB.getWeightedFinalScores() != null)
		{
			if(Double.isNaN(interfaceScoreItemDB.getWeightedFinalScores()))
			{
				interfaceScoreItemDB.setWeightedFinalScores(null);
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
		
		if(interfaceScoreItemDB.getWeightedRatio1Scores() == null)
		{
			interfaceScoreItemDB.setWeightedRatio1Scores(Double.NaN);
		}
		
		if(interfaceScoreItemDB.getWeightedRatio2Scores() == null)
		{
			interfaceScoreItemDB.setWeightedRatio2Scores(Double.NaN);
		}
		
		if(interfaceScoreItemDB.getWeightedCore1Scores() == null)
		{
			interfaceScoreItemDB.setWeightedCore1Scores(Double.NaN);
		}
		
		if(interfaceScoreItemDB.getWeightedCore2Scores() == null)
		{
			interfaceScoreItemDB.setWeightedCore2Scores(Double.NaN);
		}
		
		if(interfaceScoreItemDB.getWeightedRim1Scores() == null)
		{
			interfaceScoreItemDB.setWeightedRim1Scores(Double.NaN);
		}
		
		if(interfaceScoreItemDB.getWeightedRim2Scores() == null)
		{
			interfaceScoreItemDB.setWeightedRim2Scores(Double.NaN);
		}
		
		if(interfaceScoreItemDB.getWeightedFinalScores() == null)
		{
			interfaceScoreItemDB.setWeightedFinalScores(Double.NaN);
		}
	}
}
