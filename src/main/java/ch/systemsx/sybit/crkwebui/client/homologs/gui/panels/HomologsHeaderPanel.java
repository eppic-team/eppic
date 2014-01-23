/**
 * 
 */
package ch.systemsx.sybit.crkwebui.client.homologs.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.gui.links.ImageLinkWithTooltip;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;

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
	private ImageLinkWithTooltip downloadImage;
	private ColorPalettePanel colorPanel;
	
	public HomologsHeaderPanel(HomologsInfoItem infoItem, String jobId){
		this.setBorders(false);
		
		this.addStyleName("eppic-default-font");
		
		infoContainer = createInfoContainer();
		this.add(infoContainer, new HorizontalLayoutData(1,-1));
		
		downloadContainer = createDownloadContainer();
		this.add(downloadContainer, new HorizontalLayoutData(60,-1));
		
		updateContent(infoItem, jobId);
	}

	/**
	 * Updates the content of the panel
	 * @param infoItem
	 * @param jobId
	 */
	public void updateContent(HomologsInfoItem infoItem, String jobId) {
		String subInterval = infoItem.getRefUniProtStart()+"-"+infoItem.getRefUniProtEnd();
		fillQuery(infoItem.getUniprotId(), subInterval);
		fillSubtitle(infoItem.getIdCutoffUsed(), infoItem.getClusteringPercentIdUsed());
		fillDownloadsLink(infoItem, jobId);
	}

	/**
	 * Fills in the subtitle panel
	 * @param idCutoff
	 * @param clusteringPercent
	 */
	private void fillSubtitle(double idCutoff, int clusteringPercent) {
		String identity = String.valueOf(Math.round(idCutoff*100));
		String clusterPer = String.valueOf(Math.round(clusteringPercent));
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
	private void fillDownloadsLink(HomologsInfoItem infoItem, String jobId) {
		String source = "resources/icons/download.png";
		String alignmentId = infoItem.getChains().substring(0, 1);
    	String downloadLink = GWT.getModuleBaseURL() + "fileDownload?type=fasta&id=" + jobId + "&alignment=" + alignmentId;
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
