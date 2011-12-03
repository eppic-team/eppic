package crk;

public enum HomologsSearchMode {

	// 3 homologues search modes at the moment
	// local: for searching with uniprot fragment that matches the PDB sequence
	// global: for searching with full uniprot sequence
	// auto: local or global is decided automatically
	LOCAL("local"), GLOBAL("global"), AUTO("auto");
	
	private String name;
	
	private HomologsSearchMode(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static HomologsSearchMode getByName(String name) {
		if (name.equals("local")) return LOCAL;
		if (name.equals("global")) return GLOBAL;
		if (name.equals("auto")) return AUTO;
		return null;
	}
}
