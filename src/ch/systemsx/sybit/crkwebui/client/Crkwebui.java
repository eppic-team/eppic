package ch.systemsx.sybit.crkwebui.client;

import ch.systemsx.sybit.crkwebui.client.controllers.MainController;

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
	
	private boolean isClosing = false;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() 
	{
		mainController = new MainController(viewport);

		History.addValueChangeHandler(this);

//		// TODO: CHECK in IE
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
				mainController.setWindowHeight(event.getHeight());
				mainController.setWindowWidth(event.getWidth());
				
				mainController.resizeResultsGrid();
				mainController.resizeScoresGrid();
				mainController.setResizeWindows(true);
			}
		});

		mainController.loadSettings();
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) 
	{
		String historyToken = event.getValue();
		mainController.displayView(historyToken);
	}
}
