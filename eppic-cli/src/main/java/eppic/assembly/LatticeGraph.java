package eppic.assembly;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;

import org.apache.commons.collections15.Transformer;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.PDBCrystallographicInfo;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.contact.Pair;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.io.MMCIFFileReader;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.biojava.nbio.structure.io.LocalPDBDirectory.FetchBehavior;
import org.biojava.nbio.structure.xtal.CrystalBuilder;
import org.biojava.nbio.structure.xtal.CrystalCell;
import org.biojava.nbio.structure.xtal.CrystalTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;



public class LatticeGraph {
	private static final Logger logger = LoggerFactory.getLogger(LatticeGraph.class);


	protected static class ChainVertexKey {
		public int opId;
		public String chainId;
		public ChainVertexKey(String chainId,int opId) {
			this.opId = opId;
			this.chainId = chainId;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((chainId == null) ? 0 : chainId.hashCode());
			result = prime * result + opId;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ChainVertexKey other = (ChainVertexKey) obj;
			if (chainId == null) {
				if (other.chainId != null)
					return false;
			} else if (!chainId.equals(other.chainId))
				return false;
			if (opId != other.opId)
				return false;
			return true;
		}

	}

	private Graph<LatticeGraphVertex,InterfaceEdge> graph;
	// Maps chainId and unit cell operator id to a vertex
	private Map<ChainVertexKey, ChainVertex> chainNodes;
	
	private boolean extendedEdges = true;

	public LatticeGraph(Structure struct, StructureInterfaceList interfaces) {
		graph = new DirectedOrderedSparseMultigraph<LatticeGraphVertex, InterfaceEdge>();
		chainNodes = new HashMap<ChainVertexKey,ChainVertex>();

		// Begin SciFi comments
		// SPACE OPS! Transform!
		Matrix4d[] spaceOps = struct.getCrystallographicInfo().getTransformationsOrthonormal();

		// GROUP OF SPACE OPS! Symmetry operations
		//SpaceGroup spaceGroup = crystalInfo.getSpaceGroup();

		// CRYSTAL POWER CELL! Cell dimensions and angles
		CrystalCell cell = struct.getCrystallographicInfo().getCrystalCell();



		// Generate vertices for unit cell
		initChainVertices(struct, spaceOps, cell);
		logger.info("Found "+graph.getVertexCount()+" chains in unit cell");


		// For each interface, add edges for each asymm unit
		for(StructureInterface face : interfaces) {
			//if(face.getId() != 4) continue;

			Pair<CrystalTransform> transforms = face.getTransforms();
			CrystalTransform transformA = transforms.getFirst();
			CrystalTransform transformB = transforms.getSecond();
			Matrix4d crystalTransformA = transformA.getMatTransform();
			Matrix4d crystalTransformB = transformB.getMatTransform();
			Matrix4d faceTransformA = cell.transfToOrthonormal(crystalTransformA);
			Matrix4d faceTransformB = cell.transfToOrthonormal(crystalTransformB);

			Pair<String> chainIds = face.getMoleculeIds();
			String chainA = chainIds.getFirst();
			String chainB = chainIds.getSecond();

			ChainVertex auA = chainNodes.get(new ChainVertexKey(chainA,0));
			ChainVertex auB = chainNodes.get(new ChainVertexKey(chainB,0));

			Point3d startPosA = auA.getAUPosition();
			Point3d startPosB = auB.getAUPosition();

			// transform according to the interface
			Point3d endPosA = new Point3d(startPosA);
			Point3d endPosB = new Point3d(startPosB);
			faceTransformA.transform(endPosA);
			faceTransformB.transform(endPosB);


			// Add edge for each asymmetric unit
			for(int opId = 0; opId < spaceOps.length; opId++) {
				//if(opId != 0) continue;

				// transform according to the spaceop
				Point3d posA = new Point3d(endPosA);
				Point3d posB = new Point3d(endPosB);
				spaceOps[opId].transform(posA);
				spaceOps[opId].transform(posB);

				// Return to the Unit cell
				Point3d ucPosA = new Point3d(posA);
				Point3d ucPosB = new Point3d(posB);
				cell.transfToOriginCell(ucPosA);
				cell.transfToOriginCell(ucPosB);
				logger.info(String.format("Interface %d AU %d: %s;\t%s\t->\t%s;\t%s",face.getId(),opId,
						round(posA,2),round(posB,2),round(ucPosA,2),round(ucPosB,2) ));


				// Determine which AU the partners belong to
				ChainVertex vertA = findVertex(ucPosA);
				ChainVertex vertB = findVertex(ucPosB);

				// Create interface vertex at the midpoint
				InterfaceVertex ivert = new InterfaceVertex(opId,face.getId());
				Point3d mid = new Point3d();
				mid.add(posA, posB);
				mid.scale(0.5);
				Point3d ucMid = new Point3d(mid);
				ivert.setPosition(ucMid);

				// Orient towards posA
				Point3d perpPointUC = new Point3d(posA);
				ivert.setPerpendicularPoint(perpPointUC);

				// transform both points together to UC
				cell.transfToOriginCell(new Point3d[] {ucMid,perpPointUC},ucMid);

				// Set properties
				graph.addVertex(ivert);
				
				InterfaceEdge edgeA = new InterfaceEdge(face.getId());
				InterfaceEdge edgeB = new InterfaceEdge(face.getId());
				

				//Set segments for wrapped edges
				if(extendedEdges) {
					extendEdge(edgeA, posA, mid, ucPosA, ucMid, cell);
					extendEdge(edgeB, mid, posB, ucMid, ucPosB, cell);
				} else {
					wrapEdge(edgeA, posA, mid, ucPosA, ucMid, cell);
					wrapEdge(edgeB, mid, posB, ucMid, ucPosB, cell);
				}

				graph.addEdge(edgeA, vertA, ivert, EdgeType.DIRECTED);
				graph.addEdge(edgeB, vertB, ivert, EdgeType.DIRECTED);
			}
		}
	}

	private Point3d round(Point3d p, int places) {
		double placeMult = Math.pow(10, places);
		double x = Math.round(p.x*placeMult)/placeMult;
		double y = Math.round(p.y*placeMult)/placeMult;
		double z = Math.round(p.z*placeMult)/placeMult;
		return new Point3d( x,y,z );
	}

	/**
	 *
	 * @param edge The edge to add segments to
	 * @param posA start position, unwrapped
	 * @param posB end position, unwrapped
	 * @param ucPosA start position, wrapped to unit cell
	 * @param ucPosB end position, wrapped to unit cell
	 * @param cell Unit cell parameters
	 */
	private void wrapEdge(InterfaceEdge edge, Point3d posA, Point3d posB,
			Point3d ucPosA, Point3d ucPosB, CrystalCell cell) {
		Point3i cellA = cell.getCellIndices(posA);
		Point3i cellB = cell.getCellIndices(posB);

		// no wrapping within cell
		if( cellA.equals(cellB)) {
			edge.addSegment(ucPosA, ucPosB);
			return;
		}

		int[] indicesA = new int[3];
		int[] indicesB = new int[3];
		cellA.get(indicesA);
		cellB.get(indicesB);

		// For each side, identify cell boundaries which could be crossed
		List<Point3d> intersectionsA = new ArrayList<Point3d>(3);
		List<Point3d> intersectionsB = new ArrayList<Point3d>(3);

		for(int coord=0;coord<3;coord++) {
			// if they differ in this coordinate
			if( indicesA[coord] != indicesB[coord] ) {
				// Normal runs perpendicular to the other two coords
				// Calculate cross product of two other crystal axes
				double[] axis1Cryst = new double[3];
				double[] axis2Cryst = new double[3];
				axis1Cryst[(coord+1)%3] = 1.0;
				axis2Cryst[(coord+2)%3] = 1.0;
				Vector3d axis1 = new Vector3d(axis1Cryst);
				Vector3d axis2 = new Vector3d(axis2Cryst);
				cell.transfToOrthonormal(axis1);
				cell.transfToOrthonormal(axis2);
				Vector3d normal = new Vector3d();
				normal.cross(axis1,axis2);

				// Determine a point on the plane
				// For the free coordinates, pick some relatively close to the cells
				int[] pointACryst = Arrays.copyOf(indicesA, 3); // in crystal coords
				int[] pointBCryst = Arrays.copyOf(indicesB, 3);
				if( indicesA[coord] < indicesB[coord] ) {
					// A is left of B
					pointACryst[coord] = indicesA[coord]+1;
					pointBCryst[coord] = indicesB[coord];
				} else {
					pointACryst[coord] = indicesA[coord];
					pointBCryst[coord] = indicesB[coord]+1;
				}
				Point3d pointA = new Point3d(pointACryst[0],pointACryst[1],pointACryst[2]);
				Point3d pointB = new Point3d(pointBCryst[0],pointBCryst[1],pointBCryst[2]);
				cell.transfToOrthonormal(pointA);
				cell.transfToOrthonormal(pointB);

				// Determine intersection
				Point3d intersectionA = planeIntersection(posA, posB, pointA, normal);
				Point3d intersectionB = planeIntersection(posA, posB, pointB, normal);

				// Only well defined if A->B is not parallel to the normal
				if( intersectionA != null && intersectionB != null) {
					intersectionsA.add(intersectionA);
					intersectionsB.add(intersectionB);
				}
			}
		}

		// Draw a segment to the nearest intersection for each point
		Point3d intersectionA = nearestNeighbor(intersectionsA, posA);
		Point3d intersectionB = nearestNeighbor(intersectionsB, posB);

		// convert to unit cell
		// intersections are on the edge of the origin cell, so need to reference posA & posB
		cell.transfToOriginCell(new Point3d[] {intersectionA}, posA);
		cell.transfToOriginCell(new Point3d[] {intersectionB}, posB);

		edge.addSegment(ucPosA, intersectionA);
		edge.addSegment(intersectionB, ucPosB);
	}

	/**
	 *
	 * @param edge The edge to add segments to
	 * @param posA start position, unwrapped
	 * @param posB end position, unwrapped
	 * @param ucPosA start position, wrapped to unit cell
	 * @param ucPosB end position, wrapped to unit cell
	 * @param cell Unit cell parameters
	 */
	private void extendEdge(InterfaceEdge edge, Point3d posA, Point3d posB,
			Point3d ucPosA, Point3d ucPosB, CrystalCell cell) {
		Point3i cellA = cell.getCellIndices(posA);
		Point3i cellB = cell.getCellIndices(posB);

		// no wrapping within cell
		if( cellA.equals(cellB)) {
			edge.addSegment(ucPosA, ucPosB);
			return;
		}

		// create segment with A in UC
		Point3d p1 = new Point3d(posA);
		Point3d p2 = new Point3d(posB);
		// convert to unit cell
		// intersections are on the edge of the origin cell, so need to reference posA
		cell.transfToOriginCell(new Point3d[] {p1,p2}, posA);
		edge.addSegment(p1,p2);

		// create segment with B in UC
		p1 = new Point3d(posA);
		p2 = new Point3d(posB);
		// convert to unit cell
		cell.transfToOriginCell(new Point3d[] {p1,p2}, posB);
		edge.addSegment(p1,p2);
	}

	private Point3d nearestNeighbor(List<Point3d> points, Point3d query) {
		if(points.isEmpty()) {
			throw new IllegalArgumentException("No search points provided");
		}
		Iterator<Point3d> it = points.iterator();
		Point3d nearest = it.next();
		if(!it.hasNext()) {
			return nearest; // length 1 list
		}
		double minDist = query.distanceSquared(nearest);
		while( it.hasNext() ) {
			Point3d pt = it.next();
			double dist = query.distanceSquared(pt);
			if(dist < minDist) {
				minDist = dist;
				nearest = pt;
			}
		}
		return nearest;
	}

	/**
	 * Calculate the intersection of a line (given by two points) with a plane
	 * (given by a point and a normal vector).
	 * @param segA
	 * @param segB
	 * @param origin
	 * @param normal
	 */
	private Point3d planeIntersection(Point3d segA, Point3d segB, Point3d origin, Vector3d normal) {
		double tol = 1e-6; //tolerance for equality

		Vector3d line = new Vector3d(); // vector B-A
		line.sub(segB, segA);

		// Check if parallel
		if(Math.abs(line.dot(normal)) < tol) {
			return null; // no intersection
		}

		// intersect at distance d along line, where
		// d = dot(normal, origin - A)/dot(normal, B - A)
		Vector3d planeToLine = new Vector3d();
		planeToLine.sub(origin,segA);

		double d = normal.dot(planeToLine) / normal.dot(line);

		// Calculate final intersection point
		Point3d intersection = new Point3d(line);
		intersection.scaleAdd(d,segA);

		return intersection;
	}

	/**
	 * Initialize the ChainVertex nodes of the graph
	 * @param struc
	 * @param spaceOps
	 * @param cell
	 */
	private void initChainVertices(Structure struc, Matrix4d[] spaceOps,
			CrystalCell cell) {
		for(Chain c : struc.getChains()) {
			// Calculate centroid position for this chain in the AU
			String chainId = c.getChainID();
			Atom[] ca = StructureTools.getAtomCAArray(c);
			Point3d centroidAU = new Point3d(Calc.getCentroid(ca).getCoords());

			for(int opId = 0; opId < spaceOps.length; opId++) {
				// Apply operator to centroid
				Point3d centroid = new Point3d(centroidAU);
				spaceOps[opId].transform(centroid);

				//				if(!cell.getCellIndices(centroid).equals(new Point3i(0,0,0)))
				//					logger.info("moving chain "+chainId+" op "+opId);
				// Make sure it is inside the cell
				cell.transfToOriginCell(centroid);

				// Create new vertex & add to the graph
				ChainVertex vert = new ChainVertex(chainId,opId);
				vert.setPosition(centroid);
				vert.setAUPosition(centroidAU);

				chainNodes.put(new ChainVertexKey(chainId,opId), vert);
				graph.addVertex(vert);
			}

		}
	}

	/**
	 * Finds a Vertex that corresponds to the specified atom.
	 *
	 * Returns null if no vertex is found within a small margin of error
	 * @param vertices
	 * @param atom
	 * @return
	 */
	private ChainVertex findVertex(Point3d atom) {
		final double tol = 1e-12;

		for(ChainVertex vert : chainNodes.values()) {
			if(vert.getPosition() == null) {
				continue;
			}
			double distSq = vert.getPosition().distanceSquared(atom);
			if(distSq < tol) {
				return vert;
			}
		}
		return null;
	}


	
	public Graph<LatticeGraphVertex, InterfaceEdge> getGraph() {
		return graph;
	}

	
}
