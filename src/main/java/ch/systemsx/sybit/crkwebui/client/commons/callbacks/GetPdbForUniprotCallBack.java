package ch.systemsx.sybit.crkwebui.client.commons.callbacks;

import ch.systemsx.sybit.crkwebui.client.commons.events.ShowErrorEvent;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.shared.model.PDBSearchResult;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

public class GetPdbForUniprotCallBack implements AsyncCallback<PagingLoadResult<PDBSearchResult>>{

	@Override
	public void onFailure(Throwable caught) {
		EventBusManager.EVENT_BUS.fireEvent(new ShowErrorEvent("Error during searching the results: " + caught.getMessage()));
		
	}

	@Override
	public void onSuccess(PagingLoadResult<PDBSearchResult> result) {
		// TODO Auto-generated method stub
		
	}

}
