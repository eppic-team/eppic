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
}


