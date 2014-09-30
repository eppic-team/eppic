package ch.systemsx.sybit.crkwebui.shared.exceptions;


/**
 * Exception thrown during retrieving the data.
 * @author srebniak_a
 *
 */
public class DaoException extends Exception implements InternalException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates empty instance of dao exception.
	 */
	public DaoException()
	{
		
	}

	/**
	 * Creates instance of dao exception with specified cause.
	 * @param cause cause of the exception
	 */
	public DaoException(Throwable e)
	{
		super(e);
	}
	
	/**
	 * Creates instance of dao exception with set exception message.
	 * @param message text of the exception
	 */
	public DaoException(String message)
	{
		super(message);
	}
}
