package ch.systemsx.sybit.crkwebui.client.alignment.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;
import ch.systemsx.sybit.crkwebui.client.commons.appdata.ApplicationContext;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;

public class AlignmentHeaderPanel extends VerticalLayoutContainer{

	private FlexTable refUniProtHeader;
	private HTML uniProtLink;
	private HTML taxonomy;
	
	private FlexTable chainsPanel;
	private HTML chains;
	
	public AlignmentHeaderPanel(){
		this.setBorders(false);
		this.addStyleName("eppic-default-font");
		
		uniProtLink = new HTML();
		taxonomy = new HTML();
		uniProtLink.addStyleName("eppic-alignments-window-uniprot");
		
		refUniProtHeader = createUniProtHeaderPanel();
		this.add(refUniProtHeader, new VerticalLayoutData(-1,-1));
		
		chains = new HTML();
		
		chainsPanel = createChainsPanel();
		this.add(chainsPanel, new VerticalLayoutData(-1, -1));
		
	}
	
	private FlexTable createUniProtHeaderPanel(){
		FlexTable table = new FlexTable();
		table.setCellSpacing(0);
		table.setCellPadding(0);
		
		HTML label = new HTML(AppPropertiesManager.CONSTANTS.alignment_window_uniprot_label()+":&nbsp;");
		label.addStyleName("eppic-alignments-window-uniprot");
		
		table.setWidget(0, 0, label);
		table.setWidget(0, 1, uniProtLink);
		table.setWidget(0, 2, taxonomy);
		
		return table;
		
	}
	
	private FlexTable createChainsPanel(){
		FlexTable table = new FlexTable();
		table.setCellSpacing(0);
		table.setCellPadding(0);
		
		HTML label = new HTML(AppPropertiesManager.CONSTANTS.alignment_window_chains_label()+":&nbsp;");
		
		table.setWidget(0, 0, label);
		table.setWidget(0, 1, chains);
		
		return table;
		
	}
	
	/**
	 * Updates the content of the header panel
	 */
	public void updateContent(String uniProtId, String firstTaxon, String lastTaxon, String repChain, String memberChains)
	{
		String baseUrl = ApplicationContext.getSettings().getUniprotLinkUrl();
		uniProtLink.setHTML("<a href='"+ baseUrl + uniProtId +"' target='_blank'>"+ uniProtId + "</a>");
		taxonomy.setHTML(createTaxonomyString(firstTaxon, lastTaxon));
		chains.setHTML(createChainsString(repChain, memberChains));
	}
	
	private String createTaxonomyString(String firstTaxon, String lastTaxon){
		if(firstTaxon  != null && lastTaxon != null){
			return "&nbsp;(<i>" + firstTaxon + "</i>,&nbsp;<i>" + lastTaxon + "</i>)";
		} else{
			return "";
		}
	}
	
	private String createChainsString(String repChain, String memberChains){
		if(memberChains != null){
			return repChain + "&nbsp;(" + memberChains +")";
		} else {
			return repChain;
		}
	}
	
}
