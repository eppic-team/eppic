package crk;

public enum ScoringType {
	ENTROPY("entropy"), KAKS("KaKs ratio");
	
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
		} else {
			return null;
		}
	}
}


