package ch.systemsx.sybit.crkwebui.shared.exceptions;


/**
 * Exception thrown when validation of the input data fails.
 * @author AS
 */
public class ValidationException extends Exception implements InternalException
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates empty instance of validation exception.
	 */
	public ValidationException()
	{

	}

	/**
	 * Creates instance of validation exception with set exception message.
	 * @param message text of the exception
	 */
	public ValidationException(String message)
	{
		super(message);
	}

	/**
	 * Creates instance of validation exception with specified cause.
	 * @param cause cause of the exception
	 */
	public ValidationException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Constructs instance of validation exception with the specified detail message and cause.
	 * @param message text of the exception
	 * @param cause cause of the exception
	 */
	public ValidationException(String message,
							   Throwable cause)
	{
		super(message, cause);
	}
}
