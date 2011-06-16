package crk;

import java.io.Serializable;

public enum ScoringType implements Serializable
{
	GEOMETRY("geometry"), ENTROPY("entropy"), KAKS("KaKs ratio");
	
	private String name;
	private ScoringType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static ScoringType getByName(String name) {
		if (name.equals("entropy")) {
			return ScoringType.ENTROPY;
		} else if (name.equals("KaKs ratio")) {
			return ScoringType.KAKS;
		} else if (name.equals("geometry")) {
			return ScoringType.GEOMETRY;
		} else {
			return null;
		}
	}
}


