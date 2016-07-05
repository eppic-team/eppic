/**
 * 
 */
package eppic.assembly.layout;

import java.util.Map;
import java.util.Map.Entry;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;

import org.biojava.nbio.structure.xtal.CrystalCell;
import org.jgrapht.UndirectedGraph;

import eppic.assembly.Assembly;
import eppic.assembly.ChainVertex3D;
import eppic.assembly.InterfaceEdge3D;

/**
 * Centers each connected component at the origin and aligns it along the main
 * symmetry axis, then performs a stereographic projection into the XY plane.
 * @author Spencer Bliven
 *
 */
public class UnitCellLayout<V extends ChainVertex3D, E extends InterfaceEdge3D>
extends AbstractGraphLayout<V, E> {
	//private static final Logger logger = LoggerFactory.getLogger(UnitCellLayout.class);
	private final CrystalCell cell;
	public UnitCellLayout(VertexPositioner<V> vertexPositioner,CrystalCell cell) {
		super(vertexPositioner);
		this.cell = cell;
	}

	@Override
	public void projectLatticeGraph(UndirectedGraph<V, E> graph) {
		Map<V, Point3i> placements = Assembly.positionVertices(graph);
		for(Entry<V, Point3i> entry : placements.entrySet()) {
			V v = entry.getKey();

			// transformation to 0,0,0 cell
			// add translation
			Point3i placement = entry.getValue();
			Vector3d trans = new Vector3d(placement.x,placement.y,placement.z);
			cell.transfToOrthonormal(trans);
			Matrix4d transmat = new Matrix4d();
			transmat.set(1., trans);

			Point3d pos = vertexPositioner.getPosition(v);
			transmat.transform(pos);
			vertexPositioner.setPosition(v, pos);
		}
	}
}
