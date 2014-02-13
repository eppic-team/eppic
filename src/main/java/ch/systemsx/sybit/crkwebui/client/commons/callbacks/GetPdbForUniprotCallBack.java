package ch.systemsx.sybit.crkwebui.client.commons.callbacks;

import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;

public class GetPdbForUniprotCallBack implements AsyncCallback<PagingLoadResult<PDBScoreItem>>{

	@Override
	public void onFailure(Throwable caught) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSuccess(PagingLoadResult<PDBScoreItem> result) {
		// TODO Auto-generated method stub
		
	}

}
