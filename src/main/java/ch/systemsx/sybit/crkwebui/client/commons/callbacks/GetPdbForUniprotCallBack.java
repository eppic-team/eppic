package ch.systemsx.sybit.crkwebui.client.commons.callbacks;

import java.util.List;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.events.SearchResultsDataRetrievedEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowErrorEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.ShowMessageEvent;
import ch.systemsx.sybit.crkwebui.client.commons.events.UnmaskMainViewEvent;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.shared.model.PDBSearchResult;

import com.google.gwt.user.client.History;
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
			History.newItem("");
			String msg = "Id=" + uniProtId + " not found on the server";
			
			EventBusManager.EVENT_BUS.fireEvent(new ShowMessageEvent(AppPropertiesManager.CONSTANTS.callback_job_not_found_error(), msg));
		}
		
		EventBusManager.EVENT_BUS.fireEvent(new UnmaskMainViewEvent());
		
	}

}
