package ch.systemsx.sybit.crkwebui.client.results.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.LinkWithTooltip;
import ch.systemsx.sybit.crkwebui.shared.model.PDBScoreItem;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;

/**
 * Panel containing results panel selectors (thumbnails, viewer).
 * @author adam
 *
 */
public class ResultsSelectorsPanel extends LayoutContainer
{	
	private LayoutContainer showDownloadResultsPanel;
	
	public ResultsSelectorsPanel(PDBScoreItem pdbScoreItem)
	{
		init(pdbScoreItem);
	}
	
	private void init(PDBScoreItem pdbScoreItem)
	{
		this.setLayout(new RowLayout(Orientation.HORIZONTAL));
		this.addStyleName("eppic-default-top-padding");

		LayoutContainer viewerTypePanelLocation = createViewerTypePanelLocation();
		this.add(viewerTypePanelLocation, new RowData(0.5, 1, new Margins(0)));
		
		showDownloadResultsPanel = new LayoutContainer();
		showDownloadResultsPanel.setBorders(false);
		
		VBoxLayout vBoxLayout = new VBoxLayout();
		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.RIGHT);
		
		showDownloadResultsPanel.setLayout(vBoxLayout);
		
		setDownloadResultsLink(pdbScoreItem.getJobId());
		
		this.add(showDownloadResultsPanel, new RowData(0.5, 1, new Margins(0)));
	}
	
	/**
	 * sets the link in the Download Results 
	 * @param PDBScoreItem
	 */
	public void setDownloadResultsLink(String jobId){
		showDownloadResultsPanel.removeAll();
		
		LinkWithTooltip downloadResultsLink = new LinkWithTooltip(AppPropertiesManager.CONSTANTS.info_panel_download_results_link(), 
				AppPropertiesManager.CONSTANTS.info_panel_download_results_link_hint(), 
				ApplicationContext.getWindowData(), 
				0, 
				GWT.getModuleBaseURL() + "fileDownload?type=zip&id=" + jobId);
			downloadResultsLink.addStyleName("eppic-download-link");
			
		showDownloadResultsPanel.add(downloadResultsLink, new RowData(1, 1, new Margins(0)));
	}
	
	/**
	 * Creates panel storing selector used to select type of the molecular viewer.
	 * @return panel with viewer selector
	 */
	private LayoutContainer createViewerTypePanelLocation()
	{
		LayoutContainer viewerTypePanelLocation = new LayoutContainer();
		viewerTypePanelLocation.setBorders(false);

		VBoxLayout vBoxLayout = new VBoxLayout();
		vBoxLayout.setVBoxLayoutAlign(VBoxLayoutAlign.LEFT);

		viewerTypePanelLocation.setLayout(vBoxLayout);

		FormPanel viewerTypePanel = new FormPanel();
		viewerTypePanel.getHeader().setVisible(false);
		viewerTypePanel.setBorders(false);
		viewerTypePanel.setBodyBorder(false);
		viewerTypePanel.setFieldWidth(100);
		viewerTypePanel.setPadding(0);

		SimpleComboBox<String> viewerTypeComboBox = createViewerTypeCombobox();
		viewerTypePanel.add(viewerTypeComboBox);
		viewerTypePanelLocation.add(viewerTypePanel);
		
		return viewerTypePanelLocation;
	}
	
	/**
	 * Creates combobox used to select molecular viewer.
	 * @return viewer selector
	 */
	private SimpleComboBox<String> createViewerTypeCombobox()
	{
		final SimpleComboBox<String> viewerTypeComboBox = new SimpleComboBox<String>();
		viewerTypeComboBox.setId("viewercombo");
		viewerTypeComboBox.setTriggerAction(TriggerAction.ALL);
		viewerTypeComboBox.setEditable(false);
		viewerTypeComboBox.setFireChangeEventOnSetValue(true);
		viewerTypeComboBox.setWidth(100);
		viewerTypeComboBox.add(AppPropertiesManager.CONSTANTS.viewer_local());
		viewerTypeComboBox.add(AppPropertiesManager.CONSTANTS.viewer_jmol());
		viewerTypeComboBox.add(AppPropertiesManager.CONSTANTS.viewer_pse());
		viewerTypeComboBox.setToolTip(createViewerTypeComboBoxToolTipConfig());
		
		String viewerCookie = Cookies.getCookie("crkviewer");
		if (viewerCookie != null) {
			viewerTypeComboBox.setSimpleValue(viewerCookie);
		} else {
			viewerTypeComboBox.setSimpleValue(AppPropertiesManager.CONSTANTS.viewer_jmol());
		}

		ApplicationContext.setSelectedViewer(viewerTypeComboBox.getValue()
				.getValue());

		viewerTypeComboBox.setFieldLabel(AppPropertiesManager.CONSTANTS.results_grid_viewer_combo_label());
		viewerTypeComboBox.setLabelStyle("eppic-default-label");
		viewerTypeComboBox.addListener(Events.Change,
				new Listener<FieldEvent>() {
					public void handleEvent(FieldEvent be) {
						Cookies.setCookie("crkviewer", viewerTypeComboBox
								.getValue().getValue());
						ApplicationContext.setSelectedViewer(viewerTypeComboBox
								.getValue().getValue());
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
		viewerTypeComboBoxToolTipConfig.setTitle("3D viewer selector");
		viewerTypeComboBoxToolTipConfig.setTemplate(new Template(generateViewerTypeComboBoxTooltipTemplate()));  
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
