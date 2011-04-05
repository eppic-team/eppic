package ch.systemsx.sybit.crkwebui.shared;

import java.io.Serializable;

/**
 * General exception used in the communication between client and server
 * @author srebniak_a
 *
 */
public class CrkWebException extends Exception implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
