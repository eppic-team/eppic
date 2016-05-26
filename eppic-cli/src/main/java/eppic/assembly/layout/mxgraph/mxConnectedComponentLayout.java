package eppic.assembly.layout.mxgraph;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.jgrapht.ext.JGraphXAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;

import eppic.assembly.BinaryBinPacker;

/**
 * Rearranges each connected component of the graph so that no components
 * overlap. Vertices within each component are left fixed relative to one
 * another, so running another layout algorithm first is desireable.
 * <p>
 * The placement of each component is determined using a {@link BinaryBinPacker}.
 * @author Spencer Bliven
 *
 * @param <V> Vertex type
 * @param <E> Edge type
 */
public class mxConnectedComponentLayout<V,E> extends mxGraphLayout {
	private static final Logger logger = LoggerFactory.getLogger(mxConnectedComponentLayout.class);

	private int padding; // units to leave around each component
	public mxConnectedComponentLayout(JGraphXAdapter<V, E> graph) {
		super(graph);

		padding = 10;
	}

	@Override
	public void execute(Object parent) {
		mxIGraphModel model = graph.getModel();

		model.beginUpdate();
		try {

			// Get bounding boxes
			Map<List<Object>,Rectangle> bounds = getConnectedComponentBounds(parent, model);

			if(logger.isDebugEnabled()) {
				for( List<Object> cc : bounds.keySet()) {
					logger.debug("Rectangle {} contains the following cells:",bounds.get(cc));
					for(Object cell : cc) {
						Object val = model.getValue(cell);
						logger.debug("    ({}) {}",val.getClass().getSimpleName(),val);
					}
				}
			}

			// Pack it
			List<Entry<Dimension2D, List<Object>>> boxes = new ArrayList<>(bounds.size());
			for( Entry<List<Object>, Rectangle> entry:bounds.entrySet()) {
				Rectangle r = entry.getValue();
				Dimension d = new Dimension(r.width + padding, r.height + padding);
				boxes.add(new SimpleEntry<Dimension2D,List<Object>>(d,entry.getKey()));
			}
			BinaryBinPacker<List<Object>> packer = new BinaryBinPacker<>(boxes);
			Rectangle2D fullBounds = packer.getBounds();
			List<Entry<List<Object>, Rectangle2D>> placements = packer.getPlacements();

			// Transform each connected component
			for( Entry<List<Object>, Rectangle2D> entry : placements) {
				List<Object> cc = entry.getKey();
				Rectangle originalPlace = bounds.get(cc);
				Rectangle2D finalPlace = entry.getValue();
				double dx = finalPlace.getX() - fullBounds.getX() + padding - originalPlace.getX();
				double dy = finalPlace.getY() - fullBounds.getY() + padding - originalPlace.getY();
				for(Object cell : cc) {

					if(!isVertexIgnored(cell) && isVertexMovable(cell) ) {
						mxGeometry geom = model.getGeometry(cell);
						setVertexLocation(cell, geom.getX()+dx, geom.getY()+dy);
					} else if(!isEdgeIgnored(cell)) {
						graph.resetEdge(cell);
					}
				}
			}
		} finally {
			model.endUpdate();
		}

	}

	/**
	 * Identify connected components within the graph and a bounding box for each
	 * @param parent
	 * @param model
	 * @param bounds
	 */
	private static Map<List<Object>, Rectangle> getConnectedComponentBounds(Object parent,
			mxIGraphModel model) {
		Map<List<Object>, Rectangle> bounds = new HashMap<>();

		// Values are a list of all connected components
		// Each element of the list should be a key to the list
		Map<Object,List<Object>> connected = new HashMap<>();
		for(int i=0,childcount=model.getChildCount(parent);i<childcount;i++) {
			Object cell = model.getChildAt(parent, i);

			//				logger.info("Cell ({}): {}",cell.getClass(),cell);
			//				logger.info("Type: {}",model.isVertex(cell)?"vertex":(model.isEdge(cell)?"edge":"other"));
			//				logger.info("Children: {}",model.getChildCount(cell));
			//				logger.info("Edges: {}",model.getEdgeCount(cell));
			//				logger.info("Value ({}): {}",model.getValue(cell).getClass(),model.getValue(cell));

			if(connected.containsKey(cell)) {
				// cell was already processed as a connected component
				continue;
			}

			// New connected component
			List<Object> cc = new LinkedList<Object>();
			Queue<Object> children = new LinkedList<Object>();
			children.add(cell);

			//				Rectangle bound = geom.getRectangle();
			Rectangle bound = new Rectangle(-1,-1);
			if(model.isVertex(cell)||model.isEdge(cell)) {
				mxGeometry geom = model.getGeometry(cell);
				bound = geom.getRectangle();
			}

			while(!children.isEmpty()) {
				// pop next child
				Object child = children.remove();
				if(connected.containsKey(child)) {
					continue;
				}
				// Add to connected component
				cc.add(child);
				connected.put(child,cc);
				// TODO figure out edge geometry too
				if(model.isVertex(child)) {
					mxGeometry geom = model.getGeometry(child);
					bound = bound.union(geom.getRectangle());
				}

				// push all children
				for(int j=0;j<model.getChildCount(child);j++) {
					children.add(model.getChildAt(child, j));
				}
				// push all connected edges & vertices
				if(model.isVertex(child)) {
					for(int edge=0;edge < model.getEdgeCount(child); edge++) {
						children.add(model.getEdgeAt(child, edge));
					}
				} else if( model.isEdge(child)) {
					children.add(model.getTerminal(child, true));
					children.add(model.getTerminal(child, false));
				} else {
					// Maybe happens for nested/collapsed graphs?
					logger.warn("Cell {} not an edge or vertex.",child);
				}
			}
			bounds.put(cc, bound);
		}
		return bounds;
	}

}
