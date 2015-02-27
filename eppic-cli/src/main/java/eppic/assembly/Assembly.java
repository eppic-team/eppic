package eppic.assembly;

import java.util.ArrayList;
import java.util.List;

import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.contact.StructureInterfaceList;


public class Assembly {

	private List<StructureInterfaceCluster> interfaceClusters;
	
	private int size;
	private String symmetry;
	private String stoichiometry;
	
	public Assembly(StructureInterfaceList interfaces, boolean[] engagedSet) {
		interfaceClusters = new ArrayList<StructureInterfaceCluster>();
		
		for (StructureInterfaceCluster cluster:interfaces.getClusters()) {
			for (int i=0;i<engagedSet.length;i++) {
				if (engagedSet[i] && cluster.getId() == i+1) {
					interfaceClusters.add(cluster);
				}
			}
		}
	}
	
	public List<StructureInterfaceCluster> getInterfaceClusters() {
		return interfaceClusters;
	}
	
	public void setInterfaces(List<StructureInterfaceCluster> interfaceClusters) {
		this.interfaceClusters = interfaceClusters;
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
