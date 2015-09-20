package eppic.assembly;

import javax.vecmath.Point3i;

import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;

/**
 * Edge between two ChainVertices, representing an interface between the two chains.
 * 
 * 
 * @author spencer
 *
 */
public class InterfaceEdge {

	private StructureInterface interf;
	
	private Point3i xtalTrans;

	/**
	 * Default constructor for factory methods. After construction, at a minimum
	 * call {@link #setInterface(StructureInterface)} and {@link #setXtalTrans(Point3i)}.
	 * Use {@link #InterfaceEdge(StructureInterface, Point3i)} whenever possible.
	 */
	public InterfaceEdge() {
		this(null,null);
	}

	/**
	 * Main constructor
	 * @param interf
	 * @param xtalTrans
	 */
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
	public void setInterface(StructureInterface i) {
		this.interf = i;
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
	public void setXtalTrans(Point3i xtalTrans) {
		this.xtalTrans = xtalTrans;
	}
}