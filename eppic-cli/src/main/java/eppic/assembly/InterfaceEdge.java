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
public class InterfaceEdge implements InterfaceEdgeInterface {

	private int interfaceId;
	private int clusterId;
	private boolean isInfinite;
	private boolean isIsologous;
	
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
		
		if (interf!=null) {
			this.interfaceId = interf.getId();
			this.isInfinite = interf.isInfinite();
			this.isIsologous = interf.isIsologous();
		}
		else { 
			this.interfaceId = -1;
			this.isInfinite = false;
			this.isIsologous = false;
		}
		
		if (interf!=null && interf.getCluster()!=null)
			this.clusterId = interf.getCluster().getId();
		else 
			this.clusterId = -1;
	}
	
	/** Copy constructor */
	public InterfaceEdge(InterfaceEdge o) {
		this.interf = o.interf;
		this.xtalTrans = o.xtalTrans;
		this.interfaceId = o.interfaceId;
		this.clusterId = o.clusterId;
		this.isInfinite = o.isInfinite;
		this.isIsologous = o.isIsologous;
	}
	
	@Override
	public String toString() {
		return String.format("-%d(%d)-", getInterfaceId(), getClusterId());
	}

	public StructureInterface getInterface() {
		return interf;
	}
	
	public void setInterface(StructureInterface i) {
		this.interf = i;
		this.interfaceId = interf.getId();
		this.isInfinite = interf.isInfinite();
		this.isIsologous = interf.isIsologous();

		if (interf.getCluster()!=null) {
			this.clusterId = interf.getCluster().getId();
		}
		else {
			this.clusterId = -1;
		}
	}
	
	public StructureInterfaceCluster getInterfaceCluster() {
		return interf.getCluster();
	}
	
	@Override
	public int getInterfaceId() {
		return interfaceId;
	}
	
	@Override
	public void setInterfaceId(int interfaceId) {
		this.interfaceId = interfaceId;
	}

	@Override
	public int getClusterId() {
		return clusterId;
	}
	
	@Override
	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}

	@Override
	public boolean isIsologous() {
		return isIsologous;
	}
	
	@Override
	public void setIsIsologous(boolean isIsologous) {
		this.isIsologous = isIsologous;
	}
	
	@Override
	public boolean isInfinite() {
		return isInfinite;
	}
	
	@Override
	public void setIsInfinite(boolean isInfinite) {
		this.isInfinite = isInfinite;
	}

	@Override
	public Point3i getXtalTrans() {
		return xtalTrans;
	}
	
	@Override
	public void setXtalTrans(Point3i xtalTrans) {
		this.xtalTrans = xtalTrans;
	}
	
	public String getXtalTransString() {
		return InterfaceEdge.getXtalTransString(getXtalTrans());
	}
	
	public static String getXtalTransString(Point3i xtalTrans) {
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