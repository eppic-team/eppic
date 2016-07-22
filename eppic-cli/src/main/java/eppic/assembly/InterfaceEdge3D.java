package eppic.assembly;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3i;

import org.biojava.nbio.structure.contact.StructureInterface;

import eppic.assembly.gui.LatticeGUIMustache3D;
import eppic.assembly.gui.LatticeGUIJmol;

/**
 * InterfaceEdge, extended with properties for 3D display.
 * Some properties are specific to the {@link LatticeGUIJmol} or
 * {@link LatticeGUIMustache3D}.
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

	/**
	 * Perform a deep copy of the edge
	 * @param e
	 */
	public InterfaceEdge3D(InterfaceEdge3D e) {
		super(e);
		this.segments = new ArrayList<>(e.segments.size());
		for(ParametricCircularArc s :e.segments) {
			this.segments.add(new ParametricCircularArc(s));
		}
		this.uniqueName = e.uniqueName;
		this.color = e.color;
		this.colorStr = e.colorStr;
		this.circles = new ArrayList<>(e.circles.size());
		for(OrientedCircle cir : e.circles) {
			this.circles.add(new OrientedCircle(cir));
		}
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