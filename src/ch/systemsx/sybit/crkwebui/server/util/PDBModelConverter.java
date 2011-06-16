package ch.systemsx.sybit.crkwebui.server.util;

import ch.systemsx.sybit.crkwebui.shared.CrkWebException;
import model.InterfaceItem;
import model.InterfaceScoreItem;
import model.InterfaceScoreItemKey;
import model.PDBScoreItem;
import crk.PdbScore;

/**
 * This class is used to convert crk pdb item into crkweb
 * @author srebniak_a
 *
 */
public class PDBModelConverter 
{
	public static PDBScoreItem createPDBScoreItem(PdbScore[] pdbScore) throws CrkWebException
	{
		PDBScoreItem pdbScoreItem = null;
		
		try
		{
			if((pdbScore != null) && (pdbScore.length > 0))
			{
				pdbScoreItem = new PDBScoreItem();
				pdbScoreItem.setBioCutoff(pdbScore[0].getBioCutoff());
				pdbScoreItem.setBsaToAsaCutoff(pdbScore[0].getBsaToAsaCutoff());
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
				pdbScoreItem.setTitle(pdbScore[0].getPdbTitle());
				pdbScoreItem.setNumHomologsStrings(pdbScore[0].getNumHomologsStrings());
				
				int nrOfInterface = pdbScore[0].getInterfaceScoreMap().size();
				
				int i = 1;
				
					while(i <= nrOfInterface)
					{
						InterfaceItem interfaceItem = new InterfaceItem();
						interfaceItem.setId(i);
						
						interfaceItem.setArea(pdbScore[0].getInterfaceScoreMap().get(i).getInterfArea());
						interfaceItem.setName(pdbScore[0].getInterfaceScoreMap().get(i).getFirstChainId() + "+" + 
											  pdbScore[0].getInterfaceScoreMap().get(i).getSecondChainId());
						interfaceItem.setSize1(pdbScore[0].getInterfaceScoreMap().get(i).getCoreSize1());
						interfaceItem.setSize2(pdbScore[0].getInterfaceScoreMap().get(i).getCoreSize2());
						interfaceItem.setNumHomologs1(pdbScore[0].getInterfaceScoreMap().get(i).getNumHomologs1());
						interfaceItem.setNumHomologs2(pdbScore[0].getInterfaceScoreMap().get(i).getNumHomologs2());
						interfaceItem.setOperator(pdbScore[0].getInterfaceScoreMap().get(i).getOperator());
						interfaceItem.setWarnings(pdbScore[0].getInterfaceScoreMap().get(i).getWarnings());
						interfaceItem.setCallReason(pdbScore[0].getInterfaceScoreMap().get(i).getCallReason());
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
//							interfaceScoreItem.setPdbScoreItem(pdbScoreItem);
							
							interfaceScoreItem.setUnweightedRim1Scores(pdbScore[j].getInterfaceScoreMap().get(i).getRim1Score());
							interfaceScoreItem.setWeightedRim1Scores(pdbScore[j + 1].getInterfaceScoreMap().get(i).getRim1Score());
							
							interfaceScoreItem.setUnweightedCore1Scores(pdbScore[j].getInterfaceScoreMap().get(i).getCore1Score());
							interfaceScoreItem.setWeightedCore1Scores(pdbScore[j + 1].getInterfaceScoreMap().get(i).getCore1Score());
							
							interfaceScoreItem.setUnweightedRim2Scores(pdbScore[j].getInterfaceScoreMap().get(i).getRim2Score());
							interfaceScoreItem.setWeightedRim2Scores(pdbScore[j + 1].getInterfaceScoreMap().get(i).getRim2Score());
							
							interfaceScoreItem.setUnweightedRim1Scores(pdbScore[j].getInterfaceScoreMap().get(i).getRim1Score());
							interfaceScoreItem.setWeightedRim1Scores(pdbScore[j + 1].getInterfaceScoreMap().get(i).getRim1Score());
							
							interfaceScoreItem.setUnweightedCore2Scores(pdbScore[j].getInterfaceScoreMap().get(i).getCore2Score());
							interfaceScoreItem.setWeightedCore2Scores(pdbScore[j + 1].getInterfaceScoreMap().get(i).getCore2Score());
							
							interfaceScoreItem.setUnweightedRatio1Scores(pdbScore[j].getInterfaceScoreMap().get(i).getRatio1Score());
							interfaceScoreItem.setWeightedRatio1Scores(pdbScore[j + 1].getInterfaceScoreMap().get(i).getRatio1Score());
							
							interfaceScoreItem.setUnweightedRatio2Scores(pdbScore[j].getInterfaceScoreMap().get(i).getRatio2Score());
							interfaceScoreItem.setWeightedRatio2Scores(pdbScore[j + 1].getInterfaceScoreMap().get(i).getRatio2Score());
							
							interfaceScoreItem.setUnweightedFinalScores(pdbScore[j].getInterfaceScoreMap().get(i).getFinalScore());
							interfaceScoreItem.setWeightedFinalScores(pdbScore[j + 1].getInterfaceScoreMap().get(i).getFinalScore());
							
							interfaceScoreItem.setCall(pdbScore[j].getInterfaceScoreMap().get(i).getCall().getName());
							
							InterfaceScoreItemKey interfaceScoreItemKey = new InterfaceScoreItemKey();
							interfaceScoreItemKey.setInterfaceId(i);
							interfaceScoreItemKey.setMethod(method);
							
							pdbScoreItem.addInterfaceScoreItem(interfaceScoreItemKey, interfaceScoreItem);
							
							j+=2;
						}
						
						i++;
					}
				
			}
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			throw new CrkWebException("Error during parsing pdb item: " + t.getMessage());
		}
		
		return pdbScoreItem;
	}
}
