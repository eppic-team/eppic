package ch.systemsx.sybit.crkwebui.shared.exceptions;


/**
 * General exception used in the communication between client and server
 * @author srebniak_a
 *
 */
public class CrkWebException extends Exception implements InternalException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates empty instance of crk web exception.
	 */
	public CrkWebException()
	{
		
	}

	/**
	 * Creates instance of crk web exception with specified cause.
	 * @param cause cause of the exception
	 */
	public CrkWebException(Throwable e)
	{
		super(e);
	}
	
	/**
	 * Creates instance of crk web exception with set exception message.
	 * @param message text of the exception
	 */
	public CrkWebException(String message)
	{
		super(message);
	}
}
