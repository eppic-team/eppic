package eppic.assembly;

import java.util.List;

import org.biojava.nbio.structure.contact.StructureInterface;

public class Assembly {

	private List<StructureInterface> interfaces;
	
	private int size;
	private String symmetry;
	private String stoichiometry;
	
	public List<StructureInterface> getInterfaces() {
		return interfaces;
	}
	
	public void setInterfaces(List<StructureInterface> interfaces) {
		this.interfaces = interfaces;
	}
	
	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public String getSymmetry() {
		return symmetry;
	}
	
	public void setSymmetry(String symmetry) {
		this.symmetry = symmetry;
	}
	
	public String getStoichiometry() {
		return stoichiometry;
	}
	
	public void setStoichiometry(String stoichiometry) {
		this.stoichiometry = stoichiometry;
	}
	
	
}
