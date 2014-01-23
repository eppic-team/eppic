package ch.systemsx.sybit.crkwebui.client.input.gui.panels;

import ch.systemsx.sybit.crkwebui.client.commons.appdata.AppPropertiesManager;

import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.core.client.util.Margins;

public class NewsPanel extends HorizontalLayoutContainer
{
	public NewsPanel(String message)
	{
		init(message);
	}
	
	private void init(String message)
	{
		this.add(new SimpleContainer(), new HorizontalLayoutData(0.5, -1, new Margins(0)));
		
		SimpleContainer newsHeaderContainer = createNewsHeaderContainer();
		this.add(newsHeaderContainer, new HorizontalLayoutData(-1, -1, new Margins(0)));
		
		SimpleContainer newsMessageContainer = createNewsMessageContainer(message);
		this.add(newsMessageContainer, new HorizontalLayoutData(-1, -1, new Margins(0)));
	}

	/**
	 * Creates panel containing header of news.
	 * @return news header panel
	 */
	private SimpleContainer createNewsHeaderContainer() 
	{
		SimpleContainer newsHeaderContainer = new SimpleContainer();
		newsHeaderContainer.addStyleName("eppic-default-right-padding");
		
		HTML newsHeader = new HTML(AppPropertiesManager.CONSTANTS.input_news_header());
		newsHeader.addStyleName("eppic-input-news-header");
		newsHeaderContainer.add(newsHeader);
		
		return newsHeaderContainer;
	}
	
	/**
	 * Creates panel containing news message.
	 * @return news message panel
	 */
	private SimpleContainer createNewsMessageContainer(String message) 
	{
		SimpleContainer newsMessageContainer = new SimpleContainer();
		//newsMessageContainer.setWidth(true);
		//newsMessageContainer.setHeight(true);
		
		HTML newsMessage = new HTML(message);
		newsMessageContainer.add(newsMessage);
		
		return newsMessageContainer;
	}
}
