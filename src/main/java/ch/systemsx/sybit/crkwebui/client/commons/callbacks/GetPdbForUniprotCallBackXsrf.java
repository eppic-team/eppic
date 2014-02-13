package ch.systemsx.sybit.crkwebui.client.commons.callbacks;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.events.UpdateStatusLabelEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.StatusMessageType;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.services.eppic.CrkWebServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;

/**
 * call back after getting pdb list for a uniport
 * @author biyani_n
 *
 */
public class GetPdbForUniprotCallBackXsrf implements AsyncCallback<XsrfToken>{

	private CrkWebServiceAsync crkWebService;
	private FilterPagingLoadConfig config;
	private String uniProtId;
	
	public GetPdbForUniprotCallBackXsrf(CrkWebServiceAsync crkWebService,
			FilterPagingLoadConfig config, String uniProtId) {
		this.crkWebService = crkWebService;
		this.config = config;
		this.uniProtId = uniProtId;
	}

	@Override
	public void onFailure(Throwable caught) 
	{
		EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_get_xsrf_token_error(), caught));
	}

	@Override
	public void onSuccess(XsrfToken token) {
		if (token != null)
		{
			((HasRpcToken)crkWebService).setRpcToken(token);
			crkWebService.getListOfPDBsHavingAUniProt(config, uniProtId, new GetPdbForUniprotCallBack());
		}
		else 
		{
			EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_get_xsrf_token_error() + " - incorrect type", 
																		   StatusMessageType.SYSTEM_ERROR));
		}
	}

}
