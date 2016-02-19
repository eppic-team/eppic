package ch.systemsx.sybit.crkwebui.client.commons.callbacks;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.events.SearchResultsDataRetrievedEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowErrorEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.UnmaskMainViewEvent;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.shared.helpers.PDBSearchResult;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class GetPdbForUniprotCallBack implements AsyncCallback<List<PDBSearchResult>>{
	
	private String uniProtId;
	
	public GetPdbForUniprotCallBack(String uniProtId){
		this.uniProtId = uniProtId;
	}

	@Override
	public void onFailure(Throwable caught) {
		EventBusManager.EVENT_BUS.fireEvent(new ShowErrorEvent("Error during searching the results: " + caught.getMessage()));
		
	}

	@Override
	public void onSuccess(List<PDBSearchResult> result) {
		
		if (result != null)
		{
				EventBusManager.EVENT_BUS.fireEvent(new SearchResultsDataRetrievedEvent(uniProtId, result));
				
		}
		else
		{
			result = new ArrayList<PDBSearchResult>();
		}
		
		EventBusManager.EVENT_BUS.fireEvent(new UnmaskMainViewEvent());
		
	}

}
