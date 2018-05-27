package eppic.dtomodel;


/**
 * Input with type.
 * @author AS
 */
public class InputWithType
{
	/**
	 * Job inputName.
	 */
	private String inputName;
	
	/**
	 * Job input type.
	 */
	private int inputType;

	/**
	 * Creates empty instance of inputName with type.
	 */
	public InputWithType()
	{

	}

	public InputWithType(String inputName,
						 int inputType)
	{
		this.inputName = inputName;
		this.inputType = inputType;
	}

	public String getInputName() {
		return inputName;
	}

	public void setInputName(String inputName) {
		this.inputName = inputName;
	}

	public void setInputType(int inputType) {
		this.inputType = inputType;
	}

	public int getInputType() {
		return inputType;
	}

}
