package ch.systemsx.sybit.crkwebui.shared.exceptions;


/**
 * Exception thrown when deserialization of the input data fails.
 * @author AS
 */
public class DeserializationException extends Exception implements InternalException
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates empty instance of deserialization exception.
	 */
	public DeserializationException()
	{

	}

	/**
	 * Creates instance of deserialization exception with set exception message.
	 * @param message text of the exception
	 */
	public DeserializationException(String message)
	{
		super(message);
	}

	/**
	 * Creates instance of deserialization exception with specified cause.
	 * @param cause cause of the exception
	 */
	public DeserializationException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Constructs instance of deserialization exception with the specified detail message and cause.
	 * @param message text of the exception
	 * @param cause cause of the exception
	 */
	public DeserializationException(String message,
							   Throwable cause)
	{
		super(message, cause);
	}
}
