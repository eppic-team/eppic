package eppic.assembly;

import javax.vecmath.Point3d;

public class InterfaceVertex implements LatticeGraphVertex {
	private int opId; //assymetric unit
	private int interfaceId; // interface
	private Point3d perpendicularPoint;
	
	public InterfaceVertex(int opId, int interfaceId) {
		super();
		this.opId = opId;
		this.interfaceId = interfaceId;
		this.setPerpendicularPoint(null);
	}

	@Override
	public String toString() {
		return String.format("i%dx%d",opId,interfaceId);
	}

	public int getOpId() {
		return opId;
	}

	public int getInterfaceId() {
		return interfaceId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + interfaceId;
		result = prime * result + opId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InterfaceVertex other = (InterfaceVertex) obj;
		if (interfaceId != other.interfaceId)
			return false;
		if (opId != other.opId)
			return false;
		return true;
	}

	public Point3d getPerpendicularPoint() {
		return perpendicularPoint;
	}

	public void setPerpendicularPoint(Point3d perpendicularPoint) {
		this.perpendicularPoint = perpendicularPoint;
	}
}
