package eppic.assembly.layout;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.vecmath.Point3d;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.assembly.BinaryBinPacker;

/**
 * Rearranges each connected component of the graph so that no components
 * overlap in the XY plane. Vertices within each component are left fixed relative to one
 * another, so running another layout algorithm first is desirable.
 * <p>
 * The placement of each component is determined using a {@link BinaryBinPacker}.
 * @author Spencer Bliven
 *
 * @param <V> Vertex type
 * @param <E> Edge type
 */
public class ConnectedComponentLayout<V,E> extends AbstractGraphLayout<V, E> {
	private static final Logger logger = LoggerFactory.getLogger(ConnectedComponentLayout.class);

	private int padding; // units to leave around each component
	public ConnectedComponentLayout(VertexPositioner<V> vertexPositioner) {
		super(vertexPositioner);
		padding = 10;
	}

	@Override
	public void projectLatticeGraph(UndirectedGraph<V, E> graph) {

			// Get bounding boxes
			Map<Set<V>,Rectangle> bounds = getConnectedComponentBounds(graph);

			// Pack it
			List<Entry<Dimension2D, Set<V>>> boxes = new ArrayList<>(bounds.size());
			for( Entry<Set<V>, Rectangle> entry:bounds.entrySet()) {
				Rectangle r = entry.getValue();
				Dimension d = new Dimension(r.width + padding, r.height + padding);
				boxes.add(new SimpleEntry<Dimension2D,Set<V>>(d,entry.getKey()));
			}
			BinaryBinPacker<Set<V>> packer = new BinaryBinPacker<>(boxes);
			Rectangle2D fullBounds = packer.getBounds();
			List<Entry<Set<V>, Rectangle2D>> placements = packer.getPlacements();

			// Transform each connected component
			for( Entry<Set<V>, Rectangle2D> entry : placements) {
				Set<V> cc = entry.getKey();
				Rectangle originalPlace = bounds.get(cc);
				Rectangle2D finalPlace = entry.getValue();
				logger.debug("Placing {} component within {}",cc.iterator().next(), finalPlace);
				double dx = finalPlace.getX() - fullBounds.getX() + padding/2. - originalPlace.getX();
				double dy = finalPlace.getY() - fullBounds.getY() + padding/2. - originalPlace.getY();
				Point3d dpos = new Point3d(dx,dy,0.);
				for(V vert : cc) {
					Point3d pos = vertexPositioner.getPosition(vert);
					pos.add(dpos);
					vertexPositioner.setPosition(vert, pos);
				}
			}

	}

	/**
	 * Identify connected components within the graph and a bounding box for each
	 * @param parent
	 * @param model
	 * @param bounds
	 */
	private Map<Set<V>, Rectangle> getConnectedComponentBounds(UndirectedGraph<V, E> graph) {
		ConnectivityInspector<V, E> connectivity = new ConnectivityInspector<>(graph);
		
		Map<Set<V>, Rectangle> bounds = new HashMap<>();

		for( Set<V> cc : connectivity.connectedSets()) {
			Rectangle bound = new Rectangle(-1,-1);
			for(V vert : cc) {
				Point3d pos = vertexPositioner.getPosition(vert);
				bound.add((int)pos.x,(int)pos.y);
			}
			bounds.put(cc,bound);
		}
		return bounds;
	}

	/**
	 * @return the padding
	 */
	public int getPadding() {
		return padding;
	}

	/**
	 * @param padding the padding to set
	 */
	public void setPadding(int padding) {
		this.padding = padding;
	}

}
