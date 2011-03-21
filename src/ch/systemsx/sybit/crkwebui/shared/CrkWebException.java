package ch.systemsx.sybit.crkwebui.shared;

import java.io.Serializable;

/**
 * General exception used in the communication between client and server
 * @author srebniak_a
 *
 */
public class CrkWebException extends Exception implements Serializable
{
	public CrkWebException()
	{
		
	}

	public CrkWebException(Throwable e)
	{
		super(e);
	}
	
	public CrkWebException(String message)
	{
		super(message);
	}
}
