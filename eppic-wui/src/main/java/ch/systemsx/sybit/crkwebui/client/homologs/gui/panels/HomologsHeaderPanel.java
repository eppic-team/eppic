/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.homologs.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.ImageLinkWithTooltip;
import ch.systemsx.sybit.crkwebui.server.files.downloader.servlets.FileDownloadServlet;
import ch.systemsx.sybit.crkwebui.shared.model.ChainCluster;
import ch.systemsx.sybit.crkwebui.shared.model.PdbInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;

import eppic.EppicParams;

/**
 * Panel containing information on the homogs info item
 * @author biyani_n
 *
 */
public class HomologsHeaderPanel extends HorizontalLayoutContainer{
	
	private VerticalLayoutContainer infoContainer;
	private HTML queryLabel;
	private HTML subtitleLabel;
	
	private VerticalLayoutContainer downloadContainer;
	private SimpleContainer potatoContainer;
	private ImageLinkWithTooltip potatoImage;
	private ImageLinkWithTooltip downloadImage;
	private ColorPalettePanel colorPanel;
	
	public HomologsHeaderPanel(ChainCluster chainCluster, String jobId, PdbInfo pdbInfo){
		this.setBorders(false);
		
		this.addStyleName("eppic-default-font");
		
		infoContainer = createInfoContainer();
		this.add(infoContainer, new HorizontalLayoutData(1,-1));
		
		potatoContainer = createPotatoContainer(chainCluster, jobId, pdbInfo);
		this.add(potatoContainer, new HorizontalLayoutData(60,-1));
		
		downloadContainer = createDownloadContainer();
		this.add(downloadContainer, new HorizontalLayoutData(60,-1));
		
		updateContent(chainCluster, jobId);
	}

	/**
	 * Updates the content of the panel
	 * @param infoItem
	 * @param jobId
	 */
	public void updateContent(ChainCluster infoItem, String jobId) {
		String subInterval = infoItem.getRefUniProtStart()+"-"+infoItem.getRefUniProtEnd();
		fillQuery(infoItem.getRefUniProtId(), subInterval);
		fillSubtitle(infoItem.getSeqIdCutoff(), infoItem.getClusteringSeqId());
		fillDownloadsLink(infoItem, jobId);
	}

	/**
	 * Fills in the subtitle panel
	 * @param idCutoff
	 * @param clusteringId
	 */
	private void fillSubtitle(double idCutoff, double clusteringId) {
		String identity = String.valueOf(Math.round(idCutoff*100));
		String clusterPer = String.valueOf(Math.round(clusteringId*100));
		String text = AppPropertiesManager.CONSTANTS.homologs_window_subtitle_text();
		text = text.replaceFirst("%s", identity);
		text = text.replaceFirst("%s", clusterPer);
		subtitleLabel.setHTML(text);		
	}

	/**
	 * Fills in the downloads link
	 * @param infoItem
	 * @param jobId
	 */
	private void fillDownloadsLink(ChainCluster chainCluster, String jobId) {
		String source = "resources/icons/download.png";
		String repChainId = chainCluster.getRepChain();
    	String downloadLink = GWT.getModuleBaseURL() + 
    			FileDownloadServlet.SERVLET_NAME + "?" +
    			FileDownloadServlet.PARAM_TYPE+"=" + FileDownloadServlet.TYPE_VALUE_MSA + 
    			"&"+FileDownloadServlet.PARAM_ID+"=" + jobId + 
    			"&"+FileDownloadServlet.PARAM_REP_CHAIN_ID+"=" + repChainId;
    	
		downloadImage.setData(source, 
						20, 20, 
						AppPropertiesManager.CONSTANTS.homologs_window_downloads_tooltip(), 
						downloadLink);
	}

	/**
	 * Fills in the query header
	 * @param uniprotId
	 * @param subInterval
	 */
	private void fillQuery(String uniprotId, String subInterval) {
		queryLabel.setHTML(AppPropertiesManager.CONSTANTS.homologs_window_query_text()+": "+uniprotId+" ("+subInterval+")");
	}

	/**
	 * Creates the download panel
	 * @return container
	 */
	private VerticalLayoutContainer createDownloadContainer() {
		VerticalLayoutContainer vlc = new VerticalLayoutContainer();
		
		vlc.add(new SimpleContainer(), new VerticalLayoutData(0.5, -1, new Margins(0)));
		
		vlc.add(new HTML(AppPropertiesManager.CONSTANTS.homologs_window_downloads_text()));
		
		String source = "resources/icons/download.png";
		downloadImage = new ImageLinkWithTooltip(source, 
						20, 20, 
						AppPropertiesManager.CONSTANTS.homologs_window_downloads_tooltip(), 
						"");
		
		vlc.add(downloadImage);
		
		return vlc;
		
	}
	
	/**
	 * Creates the potato container
	 * @return container
	 */
	private SimpleContainer createPotatoContainer(ChainCluster chainCluster, String jobId, PdbInfo pdbInfo) {
		SimpleContainer vlc = new SimpleContainer();
		
		// from SequenceInfoPanel
		String repChainId = chainCluster.getRepChain();
		String pdbName = pdbInfo.getTruncatedInputName();
		String downloadPseLink = GWT.getModuleBaseURL() + 
				FileDownloadServlet.SERVLET_NAME + "?" +
				FileDownloadServlet.PARAM_TYPE+"=" + FileDownloadServlet.TYPE_VALUE_ENTROPIESPSE+
				"&"+FileDownloadServlet.PARAM_ID+"=" + jobId + 
				"&"+FileDownloadServlet.PARAM_REP_CHAIN_ID+"=" + repChainId;
		
		String colorPseIconImgSrc = 
				ApplicationContext.getSettings().getResultsLocationForJob(jobId) + "/" +
				pdbName +"."+repChainId+EppicParams.ENTROPIES_FILE_SUFFIX+".png";

		potatoImage = new ImageLinkWithTooltip(colorPseIconImgSrc, 
						40, 40, 
						AppPropertiesManager.CONSTANTS.homologs_panel_entropiespse_hint(), 
						downloadPseLink);
		
		vlc.add(potatoImage);
		
		return vlc;
		
	}

	/**
	 * Create the information container
	 * @return the container
	 */
	private VerticalLayoutContainer createInfoContainer() {
		VerticalLayoutContainer vlc = new VerticalLayoutContainer();
		
		queryLabel = new HTML();
		queryLabel.addStyleName("eppic-homologs-window-query");
		vlc.add(queryLabel, new VerticalLayoutData(1,-1));
		
		subtitleLabel = new HTML();
		vlc.add(subtitleLabel, new VerticalLayoutData(1, -1));
		
		colorPanel = new ColorPalettePanel(AppPropertiesManager.CONSTANTS.homologs_window_color_code_text());
		vlc.add(colorPanel, new VerticalLayoutData(1,-1));
		
		return vlc;
		
	}

}
