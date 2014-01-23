package ch.systemsx.sybit.crkwebui.shared.exceptions;


/**
 * Exception thrown when parsing of the input data fails.
 * @author AS
 */
public class ParsingException extends Exception implements InternalException
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates empty instance of parsing exception.
	 */
	public ParsingException()
	{

	}

	/**
	 * Creates instance of parsing exception with set exception message.
	 * @param message text of the exception
	 */
	public ParsingException(String message)
	{
		super(message);
	}

	/**
	 * Creates instance of parsing exception with specified cause.
	 * @param cause cause of the exception
	 */
	public ParsingException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Constructs instance of parsing exception with the specified detail message and cause.
	 * @param message text of the exception
	 * @param cause cause of the exception
	 */
	public ParsingException(String message,
							   Throwable cause)
	{
		super(message, cause);
	}
}
