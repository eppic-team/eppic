package ch.systemsx.sybit.crkwebui.shared.exceptions;


/**
 * Exception thrown when job can not be submitted or stopped.
 * @author AS
 */
public class JobHandlerException extends Exception implements InternalException
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates empty instance of job handler exception.
	 */
	public JobHandlerException()
	{

	}

	/**
	 * Creates instance of job handler exception with set exception message.
	 * @param message text of the exception
	 */
	public JobHandlerException(String message)
	{
		super(message);
	}

	/**
	 * Creates instance of job handler exception with specified cause.
	 * @param cause cause of the exception
	 */
	public JobHandlerException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Constructs instance of job handler exception with the specified detail message and cause.
	 * @param message text of the exception
	 * @param cause cause of the exception
	 */
	public JobHandlerException(String message,
							   Throwable cause)
	{
		super(message, cause);
	}
}
