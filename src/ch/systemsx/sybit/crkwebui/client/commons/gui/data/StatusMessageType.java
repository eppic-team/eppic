package ch.systemsx.sybit.crkwebui.client.commons.gui.data;

import ch.systemsx.sybit.crkwebui.shared.exceptions.InternalException;

/**
 * Type of status message.
 * @author adam
 */
public enum StatusMessageType 
{
	NO_ERROR,
	INTERNAL_ERROR,
	SYSTEM_ERROR;

	/**
	 * Retrieves type of the message based on caught exception (if null then NO_ERROR is returned).
	 * @param throwable caught throwable
	 * @return message type
	 */
	public static StatusMessageType getTypeForThrowable(Throwable throwable)
	{
		StatusMessageType statusMessageType;
		
		if(throwable == null)
		{
			statusMessageType = NO_ERROR;
		}
		else if(throwable instanceof InternalException)
		{
			statusMessageType = INTERNAL_ERROR;
		}
		else
		{
			statusMessageType = SYSTEM_ERROR;
		}
		
		return statusMessageType;
	}
}
