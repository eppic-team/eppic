package ch.systemsx.sybit.crkwebui.shared.exceptions;


/**
 * Exception thrown when can nto generate native specification for queuing system.
 * @author AS
 */
public class NativeSpecificationException extends Exception implements InternalException
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates empty instance of native specification exception.
	 */
	public NativeSpecificationException()
	{

	}

	/**
	 * Creates instance of native specification exception with set exception message.
	 * @param message text of the exception
	 */
	public NativeSpecificationException(String message)
	{
		super(message);
	}

	/**
	 * Creates instance of native specification exception with specified cause.
	 * @param cause cause of the exception
	 */
	public NativeSpecificationException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Constructs instance of native specification exception with the specified detail message and cause.
	 * @param message text of the exception
	 * @param cause cause of the exception
	 */
	public NativeSpecificationException(String message,
							   Throwable cause)
	{
		super(message, cause);
	}
}
