package ch.systemsx.sybit.crkwebui.server.util;

import model.InterfaceItem;
import model.InterfaceScoreItem;
import model.InterfaceScoreItemKey;
import model.PDBScoreItem;
import crk.PdbScore;

public class PDBModelConverter 
{
	public static PDBScoreItem createPDBScoreItem(PdbScore[] pdbScore)
	{
		PDBScoreItem pdbScoreItem = null;
		
		if((pdbScore != null) && (pdbScore.length > 0))
		{
			pdbScoreItem = new PDBScoreItem();
			pdbScoreItem.setBioCutoff(pdbScore[0].getBioCutoff());
			pdbScoreItem.setBsaToAsaCutoffs(pdbScore[0].getBsaToAsaCutoffs());
			pdbScoreItem.setBsaToAsaRelaxStep(pdbScore[0].getBsaToAsaRelaxStep());
			pdbScoreItem.setBsaToAsaSoftCutoff(pdbScore[0].getBsaToAsaSoftCutoff());
			pdbScoreItem.setHomologsCutoff(pdbScore[0].getHomologsCutoff());
			pdbScoreItem.setIdCutoff(pdbScore[0].getIdCutoff());
			pdbScoreItem.setMaxNumSeqsCutoff(pdbScore[0].getMaxNumSeqsCutoff());
			pdbScoreItem.setMinCoreSize(pdbScore[0].getMinCoreSize());
			pdbScoreItem.setMinMemberCoreSize(pdbScore[0].getMinMemberCoreSize());
			pdbScoreItem.setPdbName(pdbScore[0].getPdbName());
			pdbScoreItem.setQueryCovCutoff(pdbScore[0].getQueryCovCutoff());
			pdbScoreItem.setScoreWeighted(pdbScore[0].isScoreWeighted());
			pdbScoreItem.setXtalCutoff(pdbScore[0].getXtalCutoff());
			pdbScoreItem.setZoomUsed(pdbScore[0].isZoomUsed());
			
			int nrOfInterface = pdbScore[0].getInterfaceScoreMap().size();
			
			int i = 1;
			
				while(i <= nrOfInterface)
				{
					InterfaceItem interfaceItem = new InterfaceItem();
					interfaceItem.setId(i);
					interfaceItem.setArea(pdbScore[0].getInterfaceScoreMap().get(i).getInterfArea());
					interfaceItem.setName(pdbScore[0].getInterfaceScoreMap().get(i).getFirstChainId() + "+" + 
										  pdbScore[0].getInterfaceScoreMap().get(i).getSecondChainId());
					interfaceItem.setSize1(pdbScore[0].getInterfaceScoreMap().get(i).getCoreSize1()[0]);
					interfaceItem.setSize2(pdbScore[0].getInterfaceScoreMap().get(i).getCoreSize2()[0]);
					interfaceItem.setNumHomologs1(pdbScore[0].getInterfaceScoreMap().get(i).getNumHomologs1());
					interfaceItem.setNumHomologs2(pdbScore[0].getInterfaceScoreMap().get(i).getNumHomologs2());
					pdbScoreItem.addInterfaceItem(interfaceItem);
					
					int j=0;
					
					while(j < pdbScore.length)
					{
						InterfaceScoreItem interfaceScoreItem = new InterfaceScoreItem(pdbScoreItem);
						interfaceScoreItem.setId(i);
						
						//TODO
						String method = pdbScore[j].getScoType().getName();
						if (method.equals("entropy")) {
							method = "Entropy";
						} else if (method.equals("KaKs ratio")) {
							method = "Kaks";
						}
						
						interfaceScoreItem.setMethod(method);
//						interfaceScoreItem.setPdbScoreItem(pdbScoreItem);
						
						interfaceScoreItem.setUnweightedRim1Scores(pdbScore[j].getInterfaceScoreMap().get(i).getRim1Scores()[0]);
						interfaceScoreItem.setWeightedRim1Scores(pdbScore[j + 1].getInterfaceScoreMap().get(i).getRim1Scores()[0]);
						
						interfaceScoreItem.setUnweightedCore1Scores(pdbScore[j].getInterfaceScoreMap().get(i).getCore1Scores()[0]);
						interfaceScoreItem.setWeightedCore1Scores(pdbScore[j + 1].getInterfaceScoreMap().get(i).getCore1Scores()[0]);
						
						interfaceScoreItem.setUnweightedRim2Scores(pdbScore[j].getInterfaceScoreMap().get(i).getRim2Scores()[0]);
						interfaceScoreItem.setWeightedRim2Scores(pdbScore[j + 1].getInterfaceScoreMap().get(i).getRim2Scores()[0]);
						
						interfaceScoreItem.setUnweightedRim1Scores(pdbScore[j].getInterfaceScoreMap().get(i).getRim1Scores()[0]);
						interfaceScoreItem.setWeightedRim1Scores(pdbScore[j + 1].getInterfaceScoreMap().get(i).getRim1Scores()[0]);
						
						interfaceScoreItem.setUnweightedCore2Scores(pdbScore[j].getInterfaceScoreMap().get(i).getCore2Scores()[0]);
						interfaceScoreItem.setWeightedCore2Scores(pdbScore[j + 1].getInterfaceScoreMap().get(i).getCore2Scores()[0]);
						
						interfaceScoreItem.setUnweightedRatio1Scores(pdbScore[j].getInterfaceScoreMap().get(i).getRatio1Scores()[0]);
						interfaceScoreItem.setWeightedRatio1Scores(pdbScore[j + 1].getInterfaceScoreMap().get(i).getRatio1Scores()[0]);
						
						interfaceScoreItem.setUnweightedRatio2Scores(pdbScore[j].getInterfaceScoreMap().get(i).getRatio2Scores()[0]);
						interfaceScoreItem.setWeightedRatio2Scores(pdbScore[j + 1].getInterfaceScoreMap().get(i).getRatio2Scores()[0]);
						
						interfaceScoreItem.setUnweightedFinalScores(pdbScore[j].getInterfaceScoreMap().get(i).getFinalScores()[0]);
						interfaceScoreItem.setWeightedFinalScores(pdbScore[j + 1].getInterfaceScoreMap().get(i).getFinalScores()[0]);
						
						interfaceScoreItem.setCall(pdbScore[j].getInterfaceScoreMap().get(i).getCalls()[0].getName());
						
						InterfaceScoreItemKey interfaceScoreItemKey = new InterfaceScoreItemKey();
						interfaceScoreItemKey.setInterfaceId(i);
						interfaceScoreItemKey.setMethod(method);
						
						pdbScoreItem.addInterfaceScoreItem(interfaceScoreItemKey, interfaceScoreItem);
						
						j+=2;
					}
					
					i++;
				}
			
		}
		
		return pdbScoreItem;
	}
}
