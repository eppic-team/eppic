package eppic.adapters;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

import eppic.model.InterfaceClusterScoreDB;

/**
 * Entity listener for InterfaceScore used to properly handle NaN and null values.
 * @author AS
 *
 */
public class ClusterScoreListener
{
	@PrePersist
	public void prePersist(InterfaceClusterScoreDB interfaceClusterScoreDB)
	{
		//TODO 
		// Check the other way of doing this !!

		if(Double.isNaN(interfaceClusterScoreDB.getScore()))
		{
			interfaceClusterScoreDB.setScore(-1000000);
		}
		if(Double.isNaN(interfaceClusterScoreDB.getScore1()))
		{
			interfaceClusterScoreDB.setScore1(-1000000);
		}
		if(Double.isNaN(interfaceClusterScoreDB.getScore2()))
		{
			interfaceClusterScoreDB.setScore2(-1000000);
		}

	}

	@PostLoad
	public void postLoad(InterfaceClusterScoreDB interfaceClusterScoreDB)
	{

		if(interfaceClusterScoreDB.getScore() == -1000000)
		{
			interfaceClusterScoreDB.setScore(Double.NaN);
		}
		if(interfaceClusterScoreDB.getScore1() == -1000000)
		{
			interfaceClusterScoreDB.setScore1(Double.NaN);
		}
		if(interfaceClusterScoreDB.getScore2() == -1000000)
		{
			interfaceClusterScoreDB.setScore2(Double.NaN);
		}
	}
}
