package eppic;

public enum HomologsSearchMode {

	// 2 homologs search modes at the moment
	// local: for searching with uniprot fragment that matches the PDB sequence
	// global: for searching with full uniprot sequence

	LOCAL("local"), GLOBAL("global");
	
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
		return null;
	}
}
