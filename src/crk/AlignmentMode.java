package crk;

public enum AlignmentMode {

	// 3 alignment modes 
	// full length: the tcoffee alignment will be done with the full uniprot sequences of the blast hits
	// hsps only  : the tcoffee alignment will be done with only the HSPs matching regions if the blast hits
	// auto       : full or hsps will be decided automatically based on HomologsSearchMode (if LOCAL --> HSPSONLY, GLOBAL --> FULLLENGTH)
	FULLLENGTH("full"), HSPSONLY("hsp"), AUTO("auto");
		
	private String name;

	private AlignmentMode(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static AlignmentMode getByName(String name) {
		if (name.equals("full")) return FULLLENGTH;
		if (name.equals("hsp"))  return HSPSONLY;
		if (name.equals("auto")) return AUTO;
		return null;
	}
}
