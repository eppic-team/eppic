package eppic.assembly;

import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;

/**
 * Edge between two ChainVertices
 * 
 * 
 * @author spencer
 *
 */
public class InterfaceEdge {

	private StructureInterface interf;
		
	public InterfaceEdge(StructureInterface interf) {
		this.interf = interf;
		
	}
	
	
	
	@Override
	public String toString() {
		return String.format("-%d-",interf.getId());
	}

	public StructureInterface getInterface() {
		return interf;
	}
	
	public StructureInterfaceCluster getInterfaceCluster() {
		return interf.getCluster();
	}
	
	public int getInterfaceId() {
		return interf.getId();
	}

	public int getClusterId() {
		return interf.getCluster().getId();
	}

	public boolean isIsologous() {
		return interf.isIsologous();
	}

	public boolean isInfinite() {
		return interf.isInfinite();
	}

	
}