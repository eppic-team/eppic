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
	 * Sets the interface id uniquely identifying the protein-protein interface
	 * @param interfaceId the interface identifier
	 */
	void setInterfaceId(int interfaceId);
	
	/**
	 * Return the interface cluster id uniquely identifying a group of equivalent protein-protein interfaces
	 * @return the interface cluster identifier
	 */
	int getClusterId();
	
	/**
	 * Sets the interface cluster id uniquely identifying a group of equivalent protein-protein interfaces
	 * @param clusterId the interface cluster identifier
	 */
	void setClusterId(int clusterId);
	
	/**
	 * Return the crystal translation (in crystal coordinates) that this edge represents
	 * @return
	 */
	Point3i getXtalTrans();
	
	/**
	 * Sets the crystal translation (in crystal coordinates) of this edge
	 * @param xtalTrans
	 */
	void setXtalTrans(Point3i xtalTrans);
	
	/**
	 * Tells whether this interface is isologous
	 * @return
	 */
	boolean isIsologous();
	
	/**
	 * Sets the isIsologous property
	 * @param isIsologous
	 */
	void setIsIsologous(boolean isIsologous);	
	
	/**
	 * Tells whether this interface is infinite, i.e. its engagement produces an infinite subgraph in the lattice
	 * @return
	 */
	boolean isInfinite();
	
	/**
	 * Sets the isInfinite property
	 * @param isInfinite
	 */
	void setIsInfinite(boolean isInfinite);

	String getXtalTransString();
	
	/**
	 * converts the crystal translation of an edge into a crystallographic string, e.g. "+a,-b,+2c"
	 * @param edge
	 * @return
	 */
	static String getXtalTransString(Point3i xtalTrans) {
		int[] coords = new int[3];
		xtalTrans.get(coords);
		final String[] letters = new String[] {"a","b","c"};

		StringBuilder str = new StringBuilder();
		for(int i=0;i<3;i++) {
			if( coords[i] != 0) {
				if( Math.abs(coords[i]) == 1 ) {
					if(str.length()>0)
						str.append(',');
					String sign = coords[i] > 0 ? "+" : "-";
					str.append(sign+letters[i]);
				} else {
					if(str.length()>0)
						str.append(',');
					str.append(String.format("%d%s",coords[i],letters[i]));
				}
			}
		}
		return str.toString();
	}

}
