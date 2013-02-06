package ch.systemsx.sybit.crkwebui.client;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.events.ApplicationWindowResizeEvent;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.client.commons.services.eppic.CrkWebServiceProvider;
import ch.systemsx.sybit.crkwebui.client.main.controllers.MainController;

import com.extjs.gxt.themes.client.Access;
import com.extjs.gxt.themes.client.Slate;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.util.Theme;
import com.extjs.gxt.ui.client.util.ThemeManager;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;

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
		ThemeManager.register(Slate.SLATE);
		ThemeManager.register(Access.ACCESS);
		ThemeManager.register(Theme.GRAY);
		Slate.SLATE.set("file","resources/themes/slate/css/xtheme-slate.css");
		Access.ACCESS.set("file","resources/themes/access/css/xtheme-access.css");
		Theme.GRAY.set("file","resources/css/gxt-gray.css");
		GXT.setDefaultTheme(Theme.GRAY, false);

		mainController = new MainController(viewport);

		History.addValueChangeHandler(this);

//		This feature has been disabled because it untied the sessions also during the refreshing of the page
//		or closing one of the tab in the browser.
//
//		Window.addWindowClosingHandler(new Window.ClosingHandler()
//		{
//			public void onWindowClosing(ClosingEvent event)
//			{
//				if(!isClosing)
//				{
//					mainController.untieJobsFromSession();
//					isClosing = true;
//				}
//			}
//		});

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
