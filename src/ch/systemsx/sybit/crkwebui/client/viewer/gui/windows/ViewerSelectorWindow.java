package ch.systemsx.sybit.crkwebui.client.viewer.gui.windows;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.form.ComboBox;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.windows.ResizableWindow;
import ch.systemsx.sybit.crkwebui.client.commons.util.EscapedStringGenerator;
import ch.systemsx.sybit.crkwebui.shared.model.WindowData;

public class ViewerSelectorWindow extends ResizableWindow{

	private static int WINDOW_DEFAULT_WIDTH = 350;
	private static int WINDOW_DEFAULT_HEIGHT = 200;
	
	
	
	public ViewerSelectorWindow(WindowData windowData) {
		super(WINDOW_DEFAULT_WIDTH, WINDOW_DEFAULT_HEIGHT, windowData);
		
		this.setHeadingHtml(AppPropertiesManager.CONSTANTS.viewer_window_title());
		this.setHideOnButtonClick(true);
		this.setModal(true);
		this.setBlinkModal(true);
		
		ComboBox<String> selectorBox = createViewerTypeCombobox();
		
		VerticalLayoutContainer mainContainer = new VerticalLayoutContainer();
		this.setWidget(mainContainer);
		mainContainer.addStyleName("eppic-default-font");
		
		mainContainer.add(new SimpleContainer(), new VerticalLayoutData(0.5,-1));
		mainContainer.add(
				new HTMLPanel(EscapedStringGenerator.generateSafeHtml(generateViewerTypeComboBoxTooltipTemplate())),
				new VerticalLayoutData(1,-1, new Margins(0,0,20,0)));
		mainContainer.add(selectorBox, new VerticalLayoutData(1,-1));

	}
	
	/**
	 * Creates combobox used to select molecular viewer.
	 * @return viewer selector
	 */
	private ComboBox<String> createViewerTypeCombobox()
	{
		ListStore<String> store = new ListStore<String>(new ModelKeyProvider<String>() {
			@Override
			public String getKey(String item) {
				return item;
			}
		});

		store.add(AppPropertiesManager.CONSTANTS.viewer_local());
		store.add(AppPropertiesManager.CONSTANTS.viewer_jmol());
		store.add(AppPropertiesManager.CONSTANTS.viewer_pse());

		final ComboBox<String> viewerTypeComboBox = new ComboBox<String>(store, new LabelProvider<String>() {
			@Override
			public String getLabel(String item) {
				return item;
			}
		});
		
		viewerTypeComboBox.setId("viewercombo");
		viewerTypeComboBox.setTriggerAction(TriggerAction.ALL);
		viewerTypeComboBox.setEditable(false);
		viewerTypeComboBox.setWidth(200);
		
		String viewerCookie = Cookies.getCookie("crkviewer");
		if (viewerCookie != null) {
			viewerTypeComboBox.setValue(viewerCookie);
		} else {
			viewerTypeComboBox.setValue(AppPropertiesManager.CONSTANTS.viewer_jmol());
		}

		ApplicationContext.setSelectedViewer(viewerTypeComboBox.getValue());

		viewerTypeComboBox.addSelectionHandler(new SelectionHandler<String>() {	
			@Override
			public void onSelection(SelectionEvent<String> event) {
				Cookies.setCookie("crkviewer", event.getSelectedItem());
				ApplicationContext.setSelectedViewer(event.getSelectedItem());
				
			}
		});
		
		return viewerTypeComboBox;
	}
	
	/**
	 * Generates content of viewer type tooltip.
	 * @return content of viewer type tooltip
	 */
	private String generateViewerTypeComboBoxTooltipTemplate()
	{
		String viewerTypeDescription = "To run selected 3D viewer please click one of the thumbnails on the list below. The following options are provided: " +
									   "<div><ul class=\"eppic-tooltip-list\">" +
									   "<li>PDB file downloadable to a local molecular viewer</li>" +
									   "<li>Browser embedded Jmol viewer (no need for local viewer)</li>" +
									   "<li>PyMol session file (.pse) to be opened in local PyMol</li>" +
									   "</ul></div>";
		return viewerTypeDescription;
	}

}
