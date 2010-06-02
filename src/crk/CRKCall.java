package crk;

public enum CRKCall {

	BIO        ("bio",     "Biological interface"),
	CRYSTAL    ("xtal",    "Crystal interface"),
	SMALL_CORE ("xtal-sc", "Core too small, likely to be a crystal interface"), 
	GRAY       ("unsure",  "Can not confidently call biological or crystal interface");
	
	
	private String name;
	private String description;
	
	private CRKCall(String name, String description){
		this.name = name;
		this.description = description;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
}
