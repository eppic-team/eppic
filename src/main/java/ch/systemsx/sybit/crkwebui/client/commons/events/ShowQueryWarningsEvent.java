package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowQueryWarningsHandler;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when query warnings tooltip is to be displayed.
 * @author AS
 */
public class ShowQueryWarningsEvent extends GwtEvent<ShowQueryWarningsHandler> 
{
	public static Type<ShowQueryWarningsHandler> TYPE = new Type<ShowQueryWarningsHandler>();
	
	/**
	 * Template of the query warnings tootlip.
	 */
	private final String tooltipTemplate;
	
	/**
	 * X position of the tooltip to display.
	 */
	private final int xCoordinate;
	
	/**
	 * Y position of the tooltip to display.
	 */
	private final int yCoordinate;
	
	public ShowQueryWarningsEvent(String tooltipTemplate,
								  int xCoordinate,
								  int yCoordinate)
	{
		this.tooltipTemplate = tooltipTemplate;
		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
	}
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ShowQueryWarningsHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(ShowQueryWarningsHandler handler) 
	{
		handler.onShowQueryWarnings(this);
	}

	/**
	 * Retrieves template of the query warnings tooltip.
	 * @return content of the query warnings tooltip
	 */
	public String getTooltipTemplate() {
		return tooltipTemplate;
	}
	
	/**
	 * Retrieves x position of the tooltip to display.
	 * @return x position of the tooltip to display
	 */
	public int getxCoordinate() {
		return xCoordinate;
	}

	/**
	 * Retrieves y position of the tooltip to display.
	 * @return y position of the tooltip to display
	 */
	public int getyCoordinate() {
		return yCoordinate;
	}
}
