package ch.systemsx.sybit.crkwebui.shared.exceptions;


/**
 * Exception thrown when can not properly instantiate job manager.
 * @author AS
 */
public class JobManagerException extends Exception implements InternalException
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates empty instance of job manager exception.
	 */
	public JobManagerException()
	{

	}

	/**
	 * Creates instance of job manager exception with set exception message.
	 * @param message text of the exception
	 */
	public JobManagerException(String message)
	{
		super(message);
	}

	/**
	 * Creates instance of job manager exception with specified cause.
	 * @param cause cause of the exception
	 */
	public JobManagerException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Constructs instance of job manager exception with the specified detail message and cause.
	 * @param message text of the exception
	 * @param cause cause of the exception
	 */
	public JobManagerException(String message,
							   Throwable cause)
	{
		super(message, cause);
	}
}
