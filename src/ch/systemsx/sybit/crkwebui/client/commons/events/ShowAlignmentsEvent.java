package ch.systemsx.sybit.crkwebui.client.commons.events;

import ch.systemsx.sybit.crkwebui.client.commons.handlers.ShowAlignmentsHandler;
import ch.systemsx.sybit.crkwebui.shared.model.HomologsInfoItem;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when alignments window is to be displayed.
 * @author AS
 */
public class ShowAlignmentsEvent extends GwtEvent<ShowAlignmentsHandler> 
{
	public static Type<ShowAlignmentsHandler> TYPE = new Type<ShowAlignmentsHandler>();
	
	/**
	 * Homologs info.
	 */
	private final HomologsInfoItem homologsInfoItem;
	
	/**
	 * Name of the pdb.
	 */
	private final String pdbName;
	
	/**
	 * X position of the window to display.
	 */
	private final int xPosition;
	
	/**
	 * Y position of the window to display.
	 */
	private final int yPostiton;
	
	public ShowAlignmentsEvent(HomologsInfoItem homologsInfoItem,
							   String pdbName,
							   int xPosition,
							   int yPosition)
	{
		this.homologsInfoItem = homologsInfoItem;
		this.pdbName = pdbName;
		this.xPosition = xPosition;
		this.yPostiton = yPosition;
	}
	
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<ShowAlignmentsHandler> getAssociatedType() 
	{
		return TYPE;
	}

	@Override
	protected void dispatch(ShowAlignmentsHandler handler) 
	{
		handler.onShowAlignments(this);
	}

	/**
	 * Retrieves homologs info to display.
	 * @return homologs info
	 */
	public HomologsInfoItem getHomologsInfoItem() {
		return homologsInfoItem;
	}

	/**
	 * Retrieves name of the pdb.
	 * @return name of the pdb
	 */
	public String getPdbName() {
		return pdbName;
	}
	
	/**
	 * Retrieves x position of the window to display.
	 * @return x position of the window to display
	 */
	public int getxPosition() {
		return xPosition;
	}

	/**
	 * Retrieves y position of the window to display.
	 * @return y position of the window to display
	 */
	public int getyPostiton() {
		return yPostiton;
	}

}
