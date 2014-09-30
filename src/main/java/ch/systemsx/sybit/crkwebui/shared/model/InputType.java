package ch.systemsx.sybit.crkwebui.shared.model;

/**
 * Input type used to start the job.
 * @author AS
 *
 */
public enum InputType
{
	NONE (-1),
	PDBCODE (0),
	FILE (1);
	
	private int index;
	
	InputType(int index)
	{
		this.index = index;
	}
	
	public int getIndex()
	{
		return index;
	}
}
