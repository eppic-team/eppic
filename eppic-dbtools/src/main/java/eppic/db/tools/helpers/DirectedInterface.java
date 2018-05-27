package eppic.db.tools.helpers;

import static eppic.db.tools.helpers.Interface.*;

public class DirectedInterface {
	private Interface iface;
	private int direction;// Interface.FIRST or Interface.SECOND
	public DirectedInterface(Interface iface, int dir) {
		if( dir != FIRST && dir != SECOND) {
			throw new IllegalArgumentException("Illegal direction");
		}
		this.iface = iface;
		this.direction = dir;
	}
	/**
	 * @return the iface
	 */
	public Interface getInterface() {
		return iface;
	}
	/**
	 * @return the direction
	 */
	public int getDirection() {
		return direction;
	}
	
	@Override
	public String toString() {
		return iface.toString() + (direction==FIRST ? ">" : "<");
	}
}