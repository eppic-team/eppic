package eppic.assembly;

import java.awt.Color;

import javax.vecmath.Point3d;

import org.biojava.nbio.structure.Chain;

import eppic.assembly.gui.LatticeGUI3Dmol;
import eppic.assembly.gui.LatticeGUIJmol;

/**
 * ChainVertex, extended with properties for 3D display.
 * Some properties are specific to the {@link LatticeGUIJmol} or
 * {@link LatticeGUI3Dmol}.
 * 
 * @author blivens
 *
 */
public class ChainVertex3D extends ChainVertex {
	
	private Point3d center;
	private String uniqueName;
	private Color color;
	private String colorStr;

	public ChainVertex3D() {
		super();
	}

	public ChainVertex3D(Chain c, int opId) {
		super(c, opId);
	}
	
	public ChainVertex3D(Chain c, int opId, Point3d center) {
		super(c, opId);
		setCenter(center);
	}

	public Point3d getCenter() {
		return center;
	}

	public void setCenter(Point3d center) {
		this.center = center;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public String getColorStr() {
		return colorStr;
	}

	public void setColorStr(String colorStr) {
		this.colorStr = colorStr;
	}
	
	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	

}
