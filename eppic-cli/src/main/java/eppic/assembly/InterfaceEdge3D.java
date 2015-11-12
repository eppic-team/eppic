package eppic.assembly;

import java.awt.Color;
import java.util.List;

import javax.vecmath.Point3i;

import org.biojava.nbio.structure.contact.StructureInterface;

import eppic.assembly.gui.LatticeGUI3Dmol;
import eppic.assembly.gui.LatticeGUIJmol;

/**
 * InterfaceEdge, extended with properties for 3D display.
 * Some properties are specific to the {@link LatticeGUIJmol} or
 * {@link LatticeGUI3Dmol}.
 * 
 * @author blivens
 *
 */

public class InterfaceEdge3D extends InterfaceEdge {
	private List<ParametricCircularArc> segments;
	private String uniqueName;
	private Color color;
	private String colorStr;
	private List<OrientedCircle> circles; // positions to draw the interface circle, if any
	
	public InterfaceEdge3D() {
		super();
	}

	public InterfaceEdge3D(StructureInterface interf, Point3i xtalTrans) {
		super(interf, xtalTrans);
	}

	public List<ParametricCircularArc> getSegments() {
		return segments;
	}

	public void setSegments(List<ParametricCircularArc> segments) {
		this.segments = segments;
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

	public List<OrientedCircle> getCircles() {
		return circles;
	}

	public void setCircles(List<OrientedCircle> circles) {
		this.circles = circles;
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