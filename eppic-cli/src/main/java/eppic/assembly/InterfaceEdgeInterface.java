package eppic.assembly;

import javax.vecmath.Point3i;

/**
 * An interface for edges representing protein-protein interfaces in lattice graphs.
 * 
 * @author Jose Duarte
 */
public interface InterfaceEdgeInterface {
	
	/**
	 * Return the interface id uniquely identifying the protein-protein interface
	 * @return the interface identifier
	 */
	int getInterfaceId();
	
	/**
	 * Return the interface cluster id uniquely identifying a group of equivalent protein-protein interfaces
	 * @return the interface cluster identifier
	 */
	int getClusterId();
	
	/**
	 * Return the crystal translation (in crystal coordinates) that this edge represents
	 * @return
	 */
	Point3i getXtalTrans();

}
