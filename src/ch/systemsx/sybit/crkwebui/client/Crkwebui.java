package ch.systemsx.sybit.crkwebui.client;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

import com.extjs.gxt.ui.client.widget.Viewport;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Crkwebui implements EntryPoint, ValueChangeHandler<String> 
{
	private MainController mainController;

	private Viewport viewport;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() 
	{
		mainController = new MainController(viewport);

		History.addValueChangeHandler(this);

//		// TODO: CHECK in IE
//		Window.addWindowClosingHandler(new Window.ClosingHandler() 
//		{
//			public void onWindowClosing(ClosingEvent event) 
//			{
//				 mainController.untieJobsFromSession();
//			}
//		});

		mainController.loadSettings();
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) 
	{
		String historyToken = event.getValue();
		mainController.displayView(historyToken);
	}
}
