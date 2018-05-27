package ch.systemsx.sybit.crkwebui.client.commons.callbacks;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.events.SearchResultsDataRetrievedEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowErrorEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.UnmaskMainViewEvent;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import eppic.dtomodel.PDBSearchResult;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class GetPdbForPdbChainCallBack implements AsyncCallback<List<PDBSearchResult>>{
	
	private String pdbCode;
	private String chain;
	
	public GetPdbForPdbChainCallBack(String pdbCode, String chain){
		this.pdbCode = pdbCode;
		this.chain = chain;
	}

	@Override
	public void onFailure(Throwable caught) {
		EventBusManager.EVENT_BUS.fireEvent(new ShowErrorEvent("Error during searching the results: " + caught.getMessage()));
		
	}

	@Override
	public void onSuccess(List<PDBSearchResult> result) {
		
		if (result != null)
		{
			EventBusManager.EVENT_BUS.fireEvent(new SearchResultsDataRetrievedEvent(pdbCode, chain, result));
				
		}
		else
		{
			result = new ArrayList<PDBSearchResult>();
		}
		
		EventBusManager.EVENT_BUS.fireEvent(new UnmaskMainViewEvent());
		
	}

}
