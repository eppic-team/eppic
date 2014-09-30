package ch.systemsx.sybit.crkwebui.client.about.gui.windows;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.events.WindowHideEvent;
import ch.systemsx.sybit.crkwebui.client.commons.gui.windows.ResizableWindow;
import ch.systemsx.sybit.crkwebui.client.commons.managers.EventBusManager;
import ch.systemsx.sybit.crkwebui.shared.model.WindowData;

import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;

/**
 * Window containing general information about the application.
 * @author AS
 */
public class AboutWindow extends ResizableWindow 
{
	private static int ABOUT_WINDOW_DEFAULT_WIDTH = 500;
	private static int ABOUT_WINDOW_DEFAULT_HEIGHT = 400;
	
	public AboutWindow(WindowData windowData) 
	{
		super(ABOUT_WINDOW_DEFAULT_WIDTH,
			  ABOUT_WINDOW_DEFAULT_HEIGHT,
			  windowData);
		
		this.setHeadingHtml(AppPropertiesManager.CONSTANTS.about_window_title());
		this.setHideOnButtonClick(true);
		this.setModal(true);
		this.setBlinkModal(true);
		this.addStyleName("eppic-default-font");
		
		this.setWidget(new HTML(
				"<table cellspacing=\"10\">" +
				"<tr>" +
				"<td>" +
				"<b>EPPIC</b>" +
				"</td>" +
				"<td>" +
				"An Evolutionary Protein-Protein Interface Classifier." +
				"</td>" +
				"</tr>" +
				"<tr>" +
				"<td></td>" +
				"<td>" +
				"Crystallography and Structural Bioinformatics Group<br>" +
				"Laboratory of Biomolecular Research<br>" +
				"Paul Scherrer Institut<br>" +
				"Villigen PSI<br>" +
				"Switzerland" +
				"</td>" +
				"</tr>" +
				"<tr>" +
				"<td></td>" +
				"<td>This server is intended for research purposes only. " +
				"It is a non-profit service to the scientific community, " +
				"provided on an \"as is\" basis without any warranty, expressed or implied. " +
				"The authors can not be held liable in any way for the service provided here. </td>" +
				"<td>" +
				"</tr>" +
				"<tr>" +
				"<td></td>" +
				"<td>eppic@systemsx.ch</td>" +
				"<td>" +
				"</tr>" +
				"</table>"));
		
		this.addHideHandler(new HideHandler()
		{
			@Override
			public void onHide(HideEvent event) {
				EventBusManager.EVENT_BUS.fireEvent(new WindowHideEvent());	

			}
		});
	}
}
