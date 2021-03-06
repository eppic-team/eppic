package eppic;

import java.io.Serializable;

public enum ScoringType implements Serializable
{
	GEOMETRY("geometry"), 
	CORERIM("entropy core-rim"), 
	KAKS("Ka/Ks core-rim"), 
	CORESURFACE("entropy core-surface"), 
	COMBINED("combined"), 
	PISA("pisa");
	
	private String name;
	private ScoringType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static ScoringType getByName(String name) {
		if (name.equals("entropy core-rim")) {
			return ScoringType.CORERIM;
		} else if (name.equals("Ka/Ks core-rim")) {
			return ScoringType.KAKS;
		} else if (name.equals("geometry")) {
			return ScoringType.GEOMETRY;
		} else if (name.equals("entropy core-surface")) {
			return ScoringType.CORESURFACE;
		} else if (name.equals("combined")) {
			return ScoringType.COMBINED;
		}  else if (name.equals("pisa")) {
			return ScoringType.PISA;
		} else {
			return null;
		}
	}
}


