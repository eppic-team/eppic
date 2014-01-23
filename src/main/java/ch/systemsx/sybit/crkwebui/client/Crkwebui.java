package ch.systemsx.sybit.crkwebui.client;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.ApplicationWindowResizeEvent;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.services.eppic.CrkWebServiceProvider;
import ch.systemsx.sybit.crkwebui.client.main.controllers.MainController;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.sencha.gxt.widget.core.client.container.Viewport;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 *
 * @author srebniak_a
 */
public class Crkwebui implements EntryPoint, ValueChangeHandler<String>
{
	private MainController mainController;

	private Viewport viewport;

//	private boolean isClosing = false;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad()
	{
		mainController = new MainController(viewport);

		History.addValueChangeHandler(this);

		Window.addResizeHandler(new ResizeHandler() {

			@Override
			public void onResize(ResizeEvent event)
			{
				ApplicationContext.getWindowData().setWindowWidth(event.getWidth());
				ApplicationContext.getWindowData().setWindowHeight(event.getHeight());
				
				ApplicationContext.adjustWindowData(event.getWidth(), event.getHeight());

				EventBusManager.EVENT_BUS.fireEvent(new ApplicationWindowResizeEvent());
			}
		});

		CrkWebServiceProvider.getServiceController().loadSettings();
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event)
	{
		String historyToken = event.getValue();
		mainController.displayView(historyToken);
	}
}
