package eppic.assembly.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import org.jgrapht.ext.JGraphXAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.model.mxICell;
import com.mxgraph.model.mxIGraphModel;

import eppic.assembly.gui.StereographicLayout.VertexPositioner;

/**
 * A jGraphX layout for LatticeGraph3D networks.
 * Layout vertices based on a stereographic projection of their 3D positions.
 * 
 * Positions are specified for each vertex by creating a VertexPositioner instance.
 * These 3D points are first spherically projected onto a unit sphere defined by
 * the center and zenith points. Then, points are projected from the zenith onto
 * the equatorial plane. Points too close to the zenith (as defined by maxRadius)
 * will be shuttled to one of the corners of the layout box.
 * @author Spencer Bliven
 *
 * @param <V> Vertex type
 * @param <E> Edge type
 */
public class mxStereographicLayout<V,E> extends mxGraphLayout {
	
	
	
	private static final Logger logger = LoggerFactory.getLogger(mxStereographicLayout.class);
	
	private final JGraphXAdapter<V, E> jgraph;
	private Point3d center;
	private Point3d zenith;
	
	private VertexPositioner<V> vertexPositioner;
	
	private int maxWidth = 500;
	private int maxHeight = 500;
	private int maxRadius = 4;
	public mxStereographicLayout(JGraphXAdapter<V, E> graph, VertexPositioner<V> positioner,
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
							Point3d angles = StereographicLayout.sphericalCoord(pos, center, zenith);
							Point2d stereo = StereographicLayout.stereographicProjection(angles, maxRadius);
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
