/**
 * 
 */
package eppic.assembly.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point3d;

import org.biojava.nbio.structure.AminoAcidImpl;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.AtomImpl;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.cluster.Subunit;
import org.biojava.nbio.structure.cluster.SubunitCluster;
import org.biojava.nbio.structure.cluster.SubunitClusterer;
import org.biojava.nbio.structure.cluster.SubunitClustererMethod;
import org.biojava.nbio.structure.cluster.SubunitClustererParameters;
import org.biojava.nbio.structure.symmetry.axis.AxisAligner;
import org.biojava.nbio.structure.symmetry.core.QuatSymmetryDetector;
import org.biojava.nbio.structure.symmetry.core.QuatSymmetryParameters;
import org.biojava.nbio.structure.symmetry.core.QuatSymmetryResults;
import org.biojava.nbio.structure.symmetry.core.Rotation;
import org.biojava.nbio.structure.symmetry.core.RotationGroup;
import org.biojava.nbio.structure.symmetry.core.Stoichiometry;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.MaskFunctor;
import org.jgrapht.graph.UndirectedMaskSubgraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centers each connected component at the origin and aligns it along the main
 * symmetry axis, then performs a stereographic projection into the XY plane.
 * @author Spencer Bliven
 *
 */
public class QuaternaryOrientationLayout<V, E> extends AbstractGraphLayout<V, E> {
	private static final Logger logger = LoggerFactory.getLogger(QuaternaryOrientationLayout.class);

	public QuaternaryOrientationLayout(VertexPositioner<V> vertexPositioner) {
		super(vertexPositioner);
	}

	@Override
	public void projectLatticeGraph(UndirectedGraph<V, E> graph) {
		ConnectivityInspector<V,E> connectivity = new ConnectivityInspector<>(graph);
		for( Set<V> connected : connectivity.connectedSets()) {
			// Focus on one complex
			UndirectedGraph<V,E> subgraph = QuaternaryOrientationLayout.getVertexSubgraph(graph, connected);
			// Orient
			QuatSymmetryResults gSymmetry = QuaternaryOrientationLayout.getQuatSymm(subgraph,vertexPositioner);
			RotationGroup pointgroup = gSymmetry.getRotationGroup();
			AxisAligner aligner = AxisAligner.getInstance(gSymmetry);
			Point3d center = aligner.getGeometricCenter();

			AxisAngle4d axis = null;
			if (pointgroup==null) {
				// pointgroup is null for 1y4m 
				// that's because Helical symmetry is detected, let's check
				if (gSymmetry.getSymmetry().equals("H")) {
					// I've no clue which helical symmetry to get, I'll go for lowest angle - JD 2017-10-02
					logger.info("Connected Component containing {} has helical symmetry", connected.iterator().next());
					axis = gSymmetry.getHelixLayers().getByLowestAngle().getAxisAngle();
				} else {
					logger.warn("Point group is null and Connected Component does not have helical symmetry. "
							+ "Setting axis to 0,0,0 "
							+ "for Connected Component containing {}", connected.iterator().next());
					axis = new AxisAngle4d();
				}
			} else {
				Rotation rotation = pointgroup.getRotation(pointgroup.getHigherOrderRotationAxis());
				axis = rotation.getAxisAngle();
			}
			Point3d zenith = new Point3d(axis.x,axis.y,axis.z);
			zenith.add(center);

			logger.info("Connected Component containing {} has center {} and zenith {} angle {}",
					connected.iterator().next(), center, zenith, axis.angle);

			StereographicLayout<V, E> stereo = new StereographicLayout<>(vertexPositioner, center, zenith);
			stereo.projectLatticeGraph(subgraph);
		}
	}

	public static <V,E> QuatSymmetryResults getQuatSymm(
			UndirectedGraph<V, E> subgraph, VertexPositioner<V> vertexPositioner) {

		List<Subunit> subunits = new ArrayList<>();
		
		for (V vert : subgraph.vertexSet() ){
			Point3d centroid = vertexPositioner.getPosition(vert);
			Atom atom = new AtomImpl();
			atom.setCoords(new double[] {centroid.x, centroid.y, centroid.z});
			// setting a dummy group so that subunit clusterer doesn't break
			Group g = new AminoAcidImpl();
			((AminoAcidImpl) g).setAminoType('A');
			g.setPDBName("ALA");
			atom.setGroup(g);
			Atom[] caCoords = new Atom[] {atom};
			
			Subunit subunit = new Subunit(caCoords, null, null, null);
			
			subunits.add(subunit);			
		}

		SubunitClustererParameters clusterParams = new SubunitClustererParameters(true);
		clusterParams.setSequenceIdentityThreshold(1.0);
		clusterParams.setClustererMethod(SubunitClustererMethod.SEQUENCE);
				
		Stoichiometry globalSubunits = SubunitClusterer.cluster(subunits, clusterParams);
		
		//Quaternary Symmetry Detection
		QuatSymmetryParameters param = new QuatSymmetryParameters();

		QuatSymmetryResults gSymmetry = 
				QuatSymmetryDetector.calcGlobalSymmetry(globalSubunits, param);

		return gSymmetry;
	}
	
	public static <V,E> UndirectedMaskSubgraph<V,E> getVertexSubgraph(
			final UndirectedGraph<V,E> graph,
			final Set<V> connected) {
		MaskFunctor<V,E> mask = new MaskFunctor<V,E>() {
			@Override
			public boolean isVertexMasked(V vertex) {
				return !connected.contains(vertex);
			}

			@Override
			public boolean isEdgeMasked(E edge) {
				V s = graph.getEdgeSource(edge);
				V t = graph.getEdgeTarget(edge);
				return !(connected.contains(s) && connected.contains(t));
			}
		};
		return new UndirectedMaskSubgraph<V,E>(graph, mask);
	}

}
