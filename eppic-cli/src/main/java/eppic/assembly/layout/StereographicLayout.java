package eppic.assembly.layout;

import java.util.HashSet;
import java.util.Set;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.jgrapht.UndirectedGraph;

import eppic.commons.util.GeomTools;

/**
 * Class for laying out vertices based on a stereographic projection of
 * their 3D positions.
 * 
 * 3D points are first spherically projected onto a unit sphere defined by
 * the center and zenith points. Then, points are projected from the zenith onto
 * the equatorial plane. Points too close to the zenith (as defined by maxRadius)
 * will be shuttled to one of the corners of the layout box.
 * @author Spencer Bliven
 *
 * @param <V> Vertex type
 * @param <E> Edge type
 */
public class StereographicLayout<V,E> implements GraphLayout<V,E> {

	//private static final Logger logger = LoggerFactory.getLogger(StereographicLayout.class);
	
	private Point3d center;
	private Point3d zenith;
		
	private int maxWidth = 500;
	private int maxHeight = 500;
	private int maxRadius = 4;
	
	private final VertexPositioner<V> vertexPositioner;

	public StereographicLayout(VertexPositioner<V> positioner,Point3d center, Point3d zenith ) {
		this.vertexPositioner = positioner;
		this.center = center;
		this.zenith = zenith;
	}

	/**
	 * Updates the coordinates of all vertices by stereographic projection. All
	 * coordinates will have z=0 component after projection, so they can
	 * safely be considered as 2D points.
	 * @param graph
	 * @return
	 */
	@Override
	public void projectLatticeGraph(UndirectedGraph<V,E> graph) {

		// Non-normalized coordinates for vertices
		Set<Point2d> centeredCoords = new HashSet<>();

		// Project vertices (unnormalized, centered at 0,0)
		for(V vert : graph.vertexSet()) {
			Point3d pos = vertexPositioner.getPosition(vert);
			// Project onto the sphere
			Point3d angles = sphericalCoord(pos, center, zenith);
			Point2d stereo = stereographicProjection(angles, maxRadius);
			centeredCoords.add( stereo );

			vertexPositioner.setPosition(vert,new Point3d(stereo.x, stereo.y, 0));
		}
		// Project edges (unnormalized, centered at 0,0)
//		for(E edge :oldGraph.edgeSet()) {
//			for( OrientedCircle circ : newEdge.getCircles()) {
//				Point3d pos = circ.getCenter();
//				// Project onto the sphere
//				Point3d angles = sphericalCoord(pos, center, zenith);
//				Point2d stereo = stereographicProjection(angles, maxRadius);
//				//centeredCoords.add( stereo ); // Include edge centers in bounds calculation
//				circ.setCenter(new Point3d(stereo.x, stereo.y, 0));
//			}
//			List<ParametricCircularArc> newSegs = new ArrayList<>(newEdge.getSegments().size());
//			for( ParametricCircularArc seg : newEdge.getSegments()) {
//				// Get control points
//				Point3d start = seg.getStart();
//				Point3d end = seg.getEnd();
//				Point3d pinnacle = seg.getMid();
//				Point3d mid = new Point3d();
//				mid.sub(end,start);
//
//				// Project each control
//				Point3d angles = sphericalCoord(start, center,zenith);
//				Point2d stereoStart = stereographicProjection(angles, maxRadius);
//				angles = sphericalCoord(end, center,zenith);
//				Point2d stereoEnd = stereographicProjection(angles, maxRadius);
//				angles = sphericalCoord(pinnacle, center,zenith);
//				Point2d stereoPinn = stereographicProjection(angles, maxRadius);
//				angles = sphericalCoord(mid, center,zenith);
//				Point2d stereoMid = stereographicProjection(angles, maxRadius);
//
//				Vector2d pinnDir = new Vector2d();
//				pinnDir.sub(stereoPinn, stereoMid);
//
//				ParametricCircularArc newSeg = new ParametricCircularArc(
//						new Point3d(stereoStart.x,stereoStart.y,0),
//						new Point3d(stereoEnd.x,stereoEnd.y,0),
//						new Vector3d(pinnDir.x,pinnDir.y,0) );
//
//				newSegs.add(newSeg);
//			}
//			newEdge.setSegments(newSegs);
//			newEdges.put(edge, newEdge);
//		}

		// Calculate bounds, leaving the origin centered
		double maxX = 0;
		double maxY = 0;
		for(Point2d p : centeredCoords) {
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
		for(V vert : graph.vertexSet()) {
			Point3d center = normalizePos(vertexPositioner.getPosition(vert), scale);
			vertexPositioner.setPosition(vert,center);
		}
		// Normalize edges
//		for(InterfaceEdge3D edge :newEdges.values()) {
//			for( OrientedCircle circ : edge.getCircles()) {
//				Point3d center = normalizePos(circ.getCenter(), scale);
//				circ.setCenter(center);
//			}
//			List<ParametricCircularArc> newSegs = new ArrayList<>(edge.getSegments().size());
//			for( ParametricCircularArc seg : edge.getSegments()) {
//				// Get control points
//				Point3d start    = normalizePos(seg.getStart(), scale);
//				Point3d end      = normalizePos(seg.getEnd(), scale);
//				Point3d pinnacle = normalizePos(seg.getMid(), scale);
//				Point3d mid = new Point3d();
//				mid.sub(end,start);
//
//				Vector3d pinnDir = new Vector3d();
//				pinnDir.sub(pinnacle, mid);
//
//				ParametricCircularArc newSeg = new ParametricCircularArc(
//						start,end,pinnDir);
//
//				newSegs.add(newSeg);
//			}
//			edge.setSegments(newSegs);
//		}
	}

	private Point3d normalizePos(Point3d pos, double scale) {
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
		Point3d newPos = new Point3d(x, y, 0);
		return newPos;
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
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public VertexPositioner<V> getVertexPositioner() {
		return vertexPositioner;
	}
}
