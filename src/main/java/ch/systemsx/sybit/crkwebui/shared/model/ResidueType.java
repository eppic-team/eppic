package ch.systemsx.sybit.crkwebui.shared.model;

public enum ResidueType {

	SURFACE(0),
	RIM(1),
	CORE_EVOLUTIONARY(2),
	CORE_GEOMETRY(3);
	
	private int region;
	
	ResidueType(int region)
	{
		this.region = region;
	}
	
	public int getRegion()
	{
		return region;
	}
}
