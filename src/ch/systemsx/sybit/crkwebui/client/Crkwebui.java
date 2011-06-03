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
		
		Window.addResizeHandler(new ResizeHandler() {
			
			@Override
			public void onResize(ResizeEvent event)
			{
				mainController.setWindowHeight(event.getHeight());
				mainController.setWindowWidth(event.getWidth());
				
				mainController.resizeResultsGrid();
				mainController.resizeScoresGrid();
				mainController.setResizeInterfacesWindow(true);
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
