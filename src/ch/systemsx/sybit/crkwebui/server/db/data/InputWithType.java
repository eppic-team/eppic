package ch.systemsx.sybit.crkwebui.server.db.data;


/**
 * Input with type.
 * @author AS
 */
public class InputWithType
{
	/**
	 * Job input.
	 */
	private String input;
	
	/**
	 * Job input type.
	 */
	private int inputType;

	/**
	 * Creates empty instance of input with type.
	 */
	public InputWithType()
	{

	}

	public InputWithType(String input,
						 int inputType)
	{
		this.input = input;
		this.inputType = inputType;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public void setInputType(int inputType) {
		this.inputType = inputType;
	}

	public int getInputType() {
		return inputType;
	}

}
