package eppic;

import java.io.Serializable;

public enum CallType implements Serializable{

	BIO           (0, "bio",    "Biological interface"),
	CRYSTAL       (1, "xtal",   "Crystal interface"), 
	//GRAY          (2, "gray",   "Can not confidently call biological or crystal interface"),
	NO_PREDICTION (3, "nopred", "Can not predict, not enough data available");
	
	private int index;
	private String name;
	private String description;
	
	private CallType(int index, String name, String description){
		this.index = index;
		this.name = name;
		this.description = description;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public int getIndex(){
		return index;
	}
	
	public static CallType getByName(String name) {
		for (CallType call:CallType.values()) {
			if (call.getName().equals(name)) {
				return call;
			}
		}
		return null;
	}
}
