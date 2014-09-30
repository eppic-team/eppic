package ch.systemsx.sybit.crkwebui.client.commons.callbacks;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.events.UpdateStatusLabelEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.data.StatusMessageType;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.services.eppic.CrkWebServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.XsrfToken;

/**
 * call back after getting pdb list for a uniport
 * @author biyani_n
 *
 */
public class GetPdbForPdbChainCallBackXsrf implements AsyncCallback<XsrfToken>{

	private CrkWebServiceAsync crkWebService;
	private String pdbCode;
	private String chain;

	public GetPdbForPdbChainCallBackXsrf(CrkWebServiceAsync crkWebService, String pdbCode, String chain) {
		this.crkWebService = crkWebService;
		this.pdbCode = pdbCode;
		this.chain = chain;
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
			crkWebService.getListOfPDBs(pdbCode, chain, new GetPdbForPdbChainCallBack(pdbCode, chain));
		}
		else 
		{
			EventBusManager.EVENT_BUS.fireEvent(new UpdateStatusLabelEvent(AppPropertiesManager.CONSTANTS.callback_get_xsrf_token_error() + " - incorrect type", 
																		   StatusMessageType.SYSTEM_ERROR));
		}
	}

}
