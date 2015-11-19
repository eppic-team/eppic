package eppic.assembly.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.jgrapht.ext.JGraphXAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;

import eppic.commons.util.GeomTools;

/**
 * Layout vertices based on a steriographic projection of their 3D positions.
 * 
 * Positions are specified for each vertex by creating a VertexPositioner instance.
 * These 3D points are first spherically projected onto a unit sphere defined by
 * the center and zenith points. Then, points are projected from the zenith onto
 * the equitorial plane. Points too close to the zenith (as defined by maxRadius)
 * will be shuttled to one of the corners of the layout box.
 * @author Spencer Bliven
 *
 * @param <V> Vertex type
 * @param <E> Edge type
 */
public class StereographicLayout<V,E> extends mxGraphLayout {
	
	public static interface VertexPositioner<V> {
		Point3d getPosition(V vertex);
	}
	
	
	private static final Logger logger = LoggerFactory.getLogger(StereographicLayout.class);
	
	private final JGraphXAdapter<V, E> jgraph;
	private Point3d center;
	private Point3d zenith;
	
	private VertexPositioner<V> vertexPositioner;
	
	private int maxWidth = 500;
	private int maxHeight = 500;
	private int maxRadius = 4;
	public StereographicLayout(JGraphXAdapter<V, E> graph, VertexPositioner<V> positioner,
			Point3d center, Point3d zenith ) {
		super(graph);
		this.jgraph = graph;
		this.center = center;
		this.zenith = zenith;
		this.vertexPositioner = positioner;
	}

	@Override
	public void execute(Object parent) {
		
		mxIGraphModel model = graph.getModel();

		// Moves the vertices to build a circle. Makes sure the
		// radius is large enough for the vertices to not
		// overlap
		model.beginUpdate();
		try
		{
			// Non-normalized coordinates for vertices
			Map<Object,Point2d> centeredCoords = new HashMap<Object, Point2d>();
			
			int childCount = model.getChildCount(parent);

			HashMap<mxICell, V> cell2Vert = jgraph.getCellToVertexMap();
			
			for (int i = 0; i < childCount; i++) {
				Object cell = model.getChildAt(parent, i);

				if (!isVertexIgnored(cell)) {
					if( isVertexMovable(cell) ) {
						V vert = cell2Vert.get(cell);
						if( vert != null ) {
							Point3d pos = vertexPositioner.getPosition(vert);
							// Project onto the sphere
							Point3d angles = sphericalCoord(pos, center, zenith);
							Point2d stereo = stereographicProjection(angles, maxRadius);
							centeredCoords.put(cell, stereo);
						} else {
							logger.error("Vertex not associated with a ChainVertex3D.");
							// keep previous location
						}
					}
				} else if (!isEdgeIgnored(cell)) {
					graph.resetEdge(cell);
				}
			}
			
			// Calculate bounds, leaving the origin centered
			double maxX = 0;
			double maxY = 0;
			for(Point2d p : centeredCoords.values()) {
				if( maxX < Math.abs(p.x) && Math.abs(p.x) < maxRadius ) {
					maxX = Math.abs(p.x);
				}
				if( maxY < Math.abs(p.y) && Math.abs(p.y) < maxRadius ) {
					maxY = Math.abs(p.y);
				}
			}
			//maxX = maxRadius;
			//maxY = maxRadius;
			double scale = Math.min( maxWidth/maxX/2, maxHeight/maxY/2 ); //pixels per unit
			// Normalize the coordinates & move the vertices
			for(Entry<Object, Point2d> entry: centeredCoords.entrySet()) {
				Point2d pos = entry.getValue();
				int x,y;
				// Place (NaN,NaN) at top right corner
				if(Double.isNaN(pos.x) && Double.isNaN(pos.y)) {
					x = maxWidth;
					y = maxHeight;
				} else {
					if( Double.isNaN(pos.x) ) {
						x = maxWidth/2;
					} else {
						x = (int)(scale*pos.x + maxWidth/2.);
						x = Math.min(Math.max(0,x), maxWidth);
					}
					if( Double.isNaN(pos.y) ) {
						y = maxHeight/2;
					} else {
						// invert y axis
						y = (int)(-scale*pos.y + maxHeight/2.);
						y = Math.min(Math.max(0,y), maxHeight);
					}
				}

				setVertexLocation(entry.getKey(), x, y);
			}
			
		}
		finally
		{
			model.endUpdate();
		}
	}

	/**
	 * Finds the stereographic projection of a point on the unit sphere to the XY plane
	 * 
	 * Input should be in spherical coordinates. The radial component will be ignored.
	 * 
	 * Points near the zenith map to (NaN,NaN) to reflect their projection to infinity.
	 * @param angles Point in spherical coordinates (radius, azimuth, zenith)
	 * @param maxRadius Don't consider points that would project more than maxRadius from the origin
	 * @return The 2D projection
	 */
	public static Point2d stereographicProjection(Point3d angles, final double maxRadius) {
		// ignore radius
		double azimuth = angles.y;
		double zenith = angles.z;

		// Ignore points too close to the zenith
		if( Math.abs(zenith) < Math.asin(1/maxRadius) ) {
			double x = Double.NaN;
			double y = Double.NaN;
			if( Math.cos(azimuth) > 0 ) {
				x = Double.POSITIVE_INFINITY;
			} else if( Math.cos(azimuth) < 0) {
				x = Double.NEGATIVE_INFINITY;
			}
			if( Math.sin(azimuth) > 0 ) {
				y = Double.POSITIVE_INFINITY;
			} else if( Math.sin(azimuth) < 0) {
				y = Double.NEGATIVE_INFINITY;
			}
			return new Point2d(x,y);
		}

		//Project to polar 2D coordinate
		double radius2D = Math.sin(zenith)/(1-Math.cos(zenith));

		// Convert back to 2D Cartesian coords
		double x = radius2D*Math.cos(azimuth);
		double y = radius2D*Math.sin(azimuth);
		return new Point2d(x,y);
	}

	/**
	 * Convert a 3D point into euler angles (radius, azimuth angle, zenith angle)
	 * @param pos
	 * @return
	 */
	public static Point3d sphericalCoord(Point3d pos, Point3d center, Point3d zenith) {
		Vector3d z = new Vector3d();
		z.sub(zenith, center);
		
		// Originally converts sphere coordinates to Cartesian, so invert it
		Matrix4d orientation = GeomTools.matrixFromPlane(center, z, null);
		orientation.invert();
		
		// Convert point to the sphere's coordinate system
		Point3d newPos = new Point3d(pos);
		orientation.transform(newPos);
		
		// Convert to spherical coordinates
		double r = newPos.distance(new Point3d());
		double zenithAng = Math.acos(newPos.z/r);
		double azimuthAng = Math.atan2(newPos.y, newPos.x);
		
		return new Point3d(r,azimuthAng,zenithAng);
	}

	public int getMaxWidth() {
		return maxWidth;
	}

	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}

	public int getMaxHeight() {
		return maxHeight;
	}

	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}

	public int getMaxRadius() {
		return maxRadius;
	}

	public void setMaxRadius(int maxRadius) {
		this.maxRadius = maxRadius;
	}

	public Point3d getCenter() {
		return center;
	}

	public Point3d getZenith() {
		return zenith;
	}

	public VertexPositioner<V> getVertexPositioner() {
		return vertexPositioner;
	}
	
}
