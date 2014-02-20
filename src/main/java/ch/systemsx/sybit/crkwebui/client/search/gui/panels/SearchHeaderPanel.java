package ch.systemsx.sybit.crkwebui.client.search.gui.panels;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;

public class SearchHeaderPanel extends VerticalLayoutContainer{

	private HTML title;
	
	private FlexTable subtitlePanel;
	private HTML subtitleLabel;
	private HTML subtitleText;
	
	public SearchHeaderPanel(){
		this.setBorders(false);
		this.setScrollMode(ScrollMode.NONE);
		this.addStyleName("eppic-default-font");
		
		title = new HTML("Search Results");
		
		subtitleLabel = new HTML("Query");
		subtitleText = new HTML();
		subtitlePanel = createSubtitlePanel(subtitleLabel, subtitleText);
		
		this.add(title);
		this.add(subtitlePanel);
	}
	
	private FlexTable createSubtitlePanel(HTML subtitleLabel, HTML subtitleText){
		FlexTable table = new FlexTable();
		table.setCellPadding(0);
		table.setCellSpacing(0);
		
		table.setWidget(0, 0, subtitleLabel);
		table.setWidget(0, 1, subtitleText);
		
		return table;
		
	}
	
	/**
	 * Updates the content of the panel
	 * @param subtitleLabel
	 * @param subtitleText
	 */
	public void updateContent(String subtitleLabel, String subtitleText){
		this.subtitleLabel.setHTML(subtitleLabel+":&nbsp;");
		this.subtitleText.setHTML(subtitleText);
		
	}
}
