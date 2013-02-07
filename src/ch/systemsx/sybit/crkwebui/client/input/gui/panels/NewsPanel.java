package ch.systemsx.sybit.crkwebui.client.input.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

public class NewsPanel extends LayoutContainer
{
	public NewsPanel(String message)
	{
		init(message);
	}
	
	private void init(String message)
	{
		this.setLayout(new RowLayout(Orientation.HORIZONTAL));
		this.add(new LayoutContainer(), new RowData(0.5, -1, new Margins(0)));
		
		LayoutContainer newsHeaderContainer = createNewsHeaderContainer();
		this.add(newsHeaderContainer, new RowData(-1, -1, new Margins(0)));
		
		LayoutContainer newsMessageContainer = createNewsMessageContainer(message);
		this.add(newsMessageContainer, new RowData(-1, -1, new Margins(0)));
	}

	/**
	 * Creates panel containing header of news.
	 * @return news header panel
	 */
	private LayoutContainer createNewsHeaderContainer() 
	{
		LayoutContainer newsHeaderContainer = new LayoutContainer();
		newsHeaderContainer.addStyleName("eppic-default-right-padding");
		
		Label newsHeader = new Label(AppPropertiesManager.CONSTANTS.input_news_header());
		newsHeader.addStyleName("eppic-input-news-header");
		newsHeaderContainer.add(newsHeader);
		
		return newsHeaderContainer;
	}
	
	/**
	 * Creates panel containing news message.
	 * @return news message panel
	 */
	private LayoutContainer createNewsMessageContainer(String message) 
	{
		LayoutContainer newsMessageContainer = new LayoutContainer();
		newsMessageContainer.setAutoWidth(true);
		newsMessageContainer.setAutoHeight(true);
		
		Label newsMessage = new Label(message);
		newsMessageContainer.add(newsMessage);
		
		return newsMessageContainer;
	}
}
