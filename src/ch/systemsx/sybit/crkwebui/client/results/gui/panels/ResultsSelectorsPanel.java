package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.LinkWithTooltip;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Cookies;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer.VBoxLayoutAlign;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.FormPanel;
import com.sencha.gxt.widget.core.client.tips.ToolTipConfig;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.Padding;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;

/**
 * Panel containing results panel selectors (thumbnails, viewer).
 * @author adam
 *
 */
public class ResultsSelectorsPanel extends HorizontalLayoutContainer
{	
	private VBoxLayoutContainer showDownloadResultsPanel;
	
	public ResultsSelectorsPanel(PDBScoreItem pdbScoreItem)
	{
		init(pdbScoreItem);
	}
	
	private void init(PDBScoreItem pdbScoreItem)
	{
		this.addStyleName("eppic-default-top-padding");

		VBoxLayoutContainer viewerTypePanelLocation = createViewerTypePanelLocation();
		this.add(viewerTypePanelLocation, new HorizontalLayoutData(0.5, 1, new Margins(0)));
		
		setDownloadResultsLink(pdbScoreItem.getJobId());
		
		this.add(showDownloadResultsPanel, new HorizontalLayoutData(0.5, 1, new Margins(0)));
	}
	
	/**
	 * sets the link in the Download Results 
	 * @param PDBScoreItem
	 */
	public void setDownloadResultsLink(String jobId){
		showDownloadResultsPanel = new VBoxLayoutContainer();
		showDownloadResultsPanel.setBorders(false);
		
		showDownloadResultsPanel.setVBoxLayoutAlign(VBoxLayoutAlign.RIGHT);
		
		LinkWithTooltip downloadResultsLink = new LinkWithTooltip(AppPropertiesManager.CONSTANTS.info_panel_download_results_link(), 
				AppPropertiesManager.CONSTANTS.info_panel_download_results_link_hint(),
				GWT.getModuleBaseURL() + "fileDownload?type=zip&id=" + jobId);
			downloadResultsLink.addStyleName("eppic-download-link");
		
		showDownloadResultsPanel.add(downloadResultsLink);
	}
	
	/**
	 * Creates panel storing selector used to select type of the molecular viewer.
	 * @return panel with viewer selector
	 */
	private VBoxLayoutContainer createViewerTypePanelLocation()
	{
		VBoxLayoutContainer viewerTypePanelLocation = new VBoxLayoutContainer();
		viewerTypePanelLocation.setBorders(false);

		VBoxLayoutContainer vBoxLayout = new VBoxLayoutContainer();
		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);

		FormPanel viewerTypePanel = new FormPanel();
		viewerTypePanel.setBorders(false);
		viewerTypePanel.setWidth(100);
		viewerTypePanel.getElement().setPadding(new Padding(0));

		ComboBox<String> viewerTypeComboBox = createViewerTypeCombobox();
		FieldLabel viewerTypeComboBoxLabel = new FieldLabel(viewerTypeComboBox, AppPropertiesManager.CONSTANTS.results_grid_viewer_combo_label());
		viewerTypeComboBox.setStyleName("eppic-default-label");
		viewerTypePanel.add(viewerTypeComboBoxLabel);
		
		viewerTypePanelLocation.add(viewerTypePanel);
		
		return viewerTypePanelLocation;
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
		viewerTypeComboBox.setWidth(100);

		viewerTypeComboBox.setToolTipConfig(createViewerTypeComboBoxToolTipConfig());
		
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
	 * Creates configuration of the tooltip displayed over viewer type selector.
	 * @return configuration of tooltip displayed over viewer type selector
	 */
	private ToolTipConfig createViewerTypeComboBoxToolTipConfig()
	{
		ToolTipConfig viewerTypeComboBoxToolTipConfig = new ToolTipConfig();  
		viewerTypeComboBoxToolTipConfig.setTitleHtml("3D viewer selector");
		viewerTypeComboBoxToolTipConfig.setBodyHtml(generateViewerTypeComboBoxTooltipTemplate());  
		viewerTypeComboBoxToolTipConfig.setShowDelay(0);
		viewerTypeComboBoxToolTipConfig.setDismissDelay(0);
		return viewerTypeComboBoxToolTipConfig;
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
