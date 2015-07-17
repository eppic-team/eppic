package eppic.assembly;

import javax.vecmath.Point3i;

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
	
	private Point3i xtalTrans;


	public InterfaceEdge(StructureInterface interf, Point3i xtalTrans) {
		this.interf = interf;
		this.xtalTrans = xtalTrans;
	}
	
	
	
	@Override
	public String toString() {
		return String.format("-%d(%d)-",interf.getId(),interf.getCluster().getId());
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
		if(interf.getCluster() == null)
			return -1;
		return interf.getCluster().getId();
	}

	public boolean isIsologous() {
		return interf.isIsologous();
	}

	public boolean isInfinite() {
		return interf.isInfinite();
	}

	public Point3i getXtalTrans() {
		return xtalTrans;
	}
	
}