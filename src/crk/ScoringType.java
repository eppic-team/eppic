package crk;

import java.io.Serializable;

public enum ScoringType implements Serializable
{
	GEOMETRY("geometry"), ENTROPY("entropy rim-core"), KAKS("Ka/Ks rim-core"), ZSCORE("entropy z-scores");
	
	private String name;
	private ScoringType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static ScoringType getByName(String name) {
		if (name.equals("entropy rim-core")) {
			return ScoringType.ENTROPY;
		} else if (name.equals("Ka/Ks rim-core")) {
			return ScoringType.KAKS;
		} else if (name.equals("geometry")) {
			return ScoringType.GEOMETRY;
		} else if (name.equals("entropy z-scores")) {
			return ScoringType.ZSCORE;
		} else {
			return null;
		}
	}
}


