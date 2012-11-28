package ch.systemsx.sybit.crkwebui.shared.model;

public enum InterfaceResidueType {

	SURFACE(0),
	RIM(1),
	CORE_EVOLUTIONARY(2),
	CORE_GEOMETRY(3);
	
	private int assignment;
	
	InterfaceResidueType(int assignment)
	{
		this.assignment = assignment;
	}
	
	public int getAssignment()
	{
		return assignment;
	}
}
