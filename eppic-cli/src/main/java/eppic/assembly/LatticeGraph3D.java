package eppic.assembly;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.PDBCrystallographicInfo;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.contact.Pair;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.io.FileConvert;
import org.biojava.nbio.structure.io.mmcif.MMCIFFileTools;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifParser;
import org.biojava.nbio.structure.io.mmcif.model.AtomSite;
import org.biojava.nbio.structure.xtal.CrystalCell;
import org.biojava.nbio.structure.xtal.CrystalTransform;
import org.jcolorbrewer.ColorBrewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.commons.util.GeomTools;

public class LatticeGraph3D extends LatticeGraph<ChainVertex3D,InterfaceEdge3D> {

	private static final Logger logger = LoggerFactory.getLogger(LatticeGraph3D.class);

	public static final double defaultInterfaceRadius = 2.5;
	public static final double defaultArcHeight = 4;
	//private final double defaultArrowOffset = 6;

	public enum WrappingPolicy {
		DONT_WRAP, // Don't wrap edges. Draw directly between vertices
		WRAP_TO_CELL, // Wrap lines exactly to cell boundary
		DUPLICATE, // Duplicate edges on each side of the cell boundary
	}

	private final Map<String,Point3d> chainCentroid;
	
	private final WrappingPolicy policy;
		

	/**
	 * Create the graph after calculating the interfaces
	 * @param struc
	 */
	public LatticeGraph3D(Structure struc) {
		this(struc,null);
	}
	/**
	 * 
	 * @param struc Structure, which must include crystallographic info
	 * @param interfaces (Optional) list of interfaces from struct. If null, will
	 *  be calculated from the structure (slow).
	 */
	public LatticeGraph3D(Structure struc, List<StructureInterface> interfaces) {
		super(struc,interfaces,ChainVertex3D.class, InterfaceEdge3D.class);
		
		this.policy = WrappingPolicy.DUPLICATE;

		// Compute centroids in AU
		chainCentroid = new HashMap<>();
		for(Chain c: structure.getPolyChains() ) {
			chainCentroid.put(c.getId(), GeomTools.getCentroid(c));
		}
		
		// Compute 3D layout
		positionVertices();
		positionEdges();
		
		// Assign colors
		//assignColorsById();
		assignColorsByEntity();
	}

//TODO figure out how to do this while preserving the vertex mapping from old to new
//     For now, just recalculate all 3D-specific properties
//	public LatticeGraph3D(LatticeGraph3D other) {
//		// Clone graph & basic properties
//		super(other, ChainVertex3D.class, InterfaceEdge3D.class);
//		
//		this.policy = other.policy;
//		this.chainCentroid = other.chainCentroid;
//		
//		otherVert = 
//	}
	/**
	 * Copy the other LatticeGraph. Recalculates all 3D position information
	 * @param other
	 */
	public LatticeGraph3D(LatticeGraph<? extends ChainVertex, ? extends InterfaceEdge> other) {
		// Clone graph & basic properties
		super(other,ChainVertex3D.class,InterfaceEdge3D.class);
		
		
		this.policy = WrappingPolicy.DUPLICATE;

		// Compute centroids in AU
		chainCentroid = new HashMap<String,Point3d>();
		for(Chain c: structure.getPolyChains() ) {
			chainCentroid.put(c.getName(), GeomTools.getCentroid(c));
		}
		
		// Compute 3D layout
		positionVertices();
		positionEdges();
		
		// Assign colors
		//assignColorsById();
		assignColorsByEntity();
	}

	/**
	 * Calculate vertex positions and colors
	 */
	private void positionVertices() {

		Set<ChainVertex3D> vertices = graph.vertexSet();
		for(ChainVertex3D v : vertices) {
			// Unit cell pos
			Point3d pos = getPosition(v);
			v.setCenter(pos);
		}
	}


	/**
	 * Initialize edge positions. Should be called during initialization after
	 * {@link #positionVertices()}.
	 */
	private void positionEdges() {
		Set<InterfaceEdge3D> edges = graph.edgeSet();
		for(InterfaceEdge3D edge : edges) {
			ChainVertex3D source = graph.getEdgeSource(edge);
			ChainVertex3D target = graph.getEdgeTarget(edge);

			logger.info("Edge {}{} -{}- {}{} translation: {}",
					source.getChainId(),source.getOpId(),
					edge.getInterfaceId(),
					target.getChainId(),target.getOpId(),
					edge.getXtalTrans() );

			switch(this.policy) {
			case DONT_WRAP: {
				// Straight lines between endpoints, no wrapping
				Point3d sourcePos = getPosition(source);
				Point3d targetPos = getPosition(target);

				//TODO set height? interface circle?
				ParametricCircularArc arrow = new ParametricCircularArc(sourcePos,targetPos,0.);
				edge.setSegments(Arrays.asList(arrow));

				break;
			}
			case DUPLICATE: {

				// Transform matrix for the interface
				Pair<CrystalTransform> transforms = edge.getInterface().getTransforms();
				Matrix4d transA = transforms.getFirst().getMatTransform();
				Matrix4d transB = transforms.getSecond().getMatTransform();

				if(! transA.epsilonEquals(CrystalTransform.IDENTITY, 1e-4) ) {
					// TODO Implement dunbrack-style transforms with non-identity left side
					logger.error("Non-identity source transformation:\n{}",transA);
				}

				CrystalCell cell = LatticeGraph.getCrystalCell(structure);


				// Vertex positions within the unit cell
				Point3d sourcePos = getPosition(source);

				// Calculate target position before wrapping
				Point3d unwrappedTarget = new Point3d(chainCentroid.get(target.getChainId()));
				Matrix4d transBOrtho = cell.transfToOrthonormal(transB);
				transBOrtho.transform(unwrappedTarget);
				getUnitCellTransformationOrthonormal(source.getChainId(), source.getOpId()).transform(unwrappedTarget);

				// Location of interface circle
				Point3d midPoint = new Point3d();
				midPoint.add(sourcePos,unwrappedTarget);
				midPoint.scale(.5);

				// wrap to source
				Point3d sourceReference = new Point3d(getReferenceCoordinate(source.getChainId()));
				getUnitCellTransformationOrthonormal(source.getChainId(), source.getOpId()).transform(sourceReference);
				Point3d[] sourceEdgePos = new Point3d[] {new Point3d(sourcePos),new Point3d(midPoint), new Point3d(unwrappedTarget)};
				cell.transfToOriginCell(sourceEdgePos,sourceReference);

				List<OrientedCircle> circles = new ArrayList<OrientedCircle>(2);
				circles.add(new OrientedCircle(sourceEdgePos[1], sourceEdgePos[0], defaultInterfaceRadius));
				List<ParametricCircularArc> segments = new ArrayList<ParametricCircularArc>(2);
				segments.add(new ParametricCircularArc(sourceEdgePos[0], sourceEdgePos[2], defaultArcHeight));

				// wrap to target
				Point3d targetReference = new Point3d(getReferenceCoordinate(target.getChainId()));
				transBOrtho.transform(targetReference);
				getUnitCellTransformationOrthonormal(source.getChainId(), source.getOpId()).transform(targetReference);
				Point3d[] targetEdgePos = new Point3d[] {new Point3d(sourcePos),new Point3d(midPoint), new Point3d(unwrappedTarget)};
				cell.transfToOriginCell(targetEdgePos,targetReference);


				if( ! sourceEdgePos[2].epsilonEquals(targetEdgePos[2], 1e-4)) {
					circles.add(new OrientedCircle(targetEdgePos[1], targetEdgePos[0], defaultInterfaceRadius));
					segments.add(new ParametricCircularArc(targetEdgePos[0], targetEdgePos[2], defaultArcHeight));

				} else {
					logger.debug("Source and target for {} within unit cell",edge);
				}

				edge.setSegments(segments);
				edge.setCircles(circles);

				break;
			}
			default:
				throw new UnsupportedOperationException();
			}
		}
	}

	
	private Point3d getPosition(ChainVertex3D v) {
		String chainId = v.getChainId();
		int au = v.getOpId();

		// AU pos
		Point3d pos = new Point3d( chainCentroid.get(chainId) );
		// Unit cell pos
		getUnitCellTransformationOrthonormal(chainId, au).transform(pos);
		return pos;
	}


	/**
	 * Color vertices according to the chain ID and edges by interface ID
	 * @see #assignColorsByEntity()
	 */
	public void assignColorsById() {
		// Generate list of equivalent chains
		Map<String,List<ChainVertex3D>> vClusters = new HashMap<String, List<ChainVertex3D>>();
		for( ChainVertex3D vert : graph.vertexSet()) {
			String id = vert.getChainId();
			List<ChainVertex3D> lst = vClusters.get(id);
			if(lst == null) {
				lst = new LinkedList<ChainVertex3D>();
				vClusters.put(id, lst);
			}
			lst.add(vert);
		}

		// Assign colors for vertices
		Map<ChainVertex3D, Color> vertexColors = assignColors(vClusters.values(),ColorBrewer.Dark2);
		for(Entry<ChainVertex3D, Color> entry: vertexColors.entrySet()) {
			entry.getKey().setColor(entry.getValue());
		}

		// Generate list of equivalent edges
		Map<Integer,List<InterfaceEdge3D>> eClusters = new HashMap<Integer, List<InterfaceEdge3D>>();
		for( InterfaceEdge3D edge : graph.edgeSet()) {
			Integer id = edge.getInterfaceId();
			List<InterfaceEdge3D> lst = eClusters.get(id);
			if(lst == null) {
				lst = new LinkedList<InterfaceEdge3D>();
				eClusters.put(id, lst);
			}
			lst.add(edge);
		}
		// Assign colors for edges
		Map<InterfaceEdge3D, Color> edgeColors = assignColors(eClusters.values(),ColorBrewer.Set2);
		for(Entry<InterfaceEdge3D, Color> entry: edgeColors.entrySet()) {
			entry.getKey().setColor(entry.getValue());
		}
	}
	/**
	 * Color vertices by entity and edges by interface cluster (default)
	 * @see #assignColorsById()
	 */
	public void assignColorsByEntity() {
		// Generate list of equivalent chains
		Map<Integer,List<ChainVertex3D>> vClusters = new HashMap<Integer, List<ChainVertex3D>>();
		for( ChainVertex3D vert : graph.vertexSet()) {
			Integer id = vert.getEntityId();
			List<ChainVertex3D> lst = vClusters.get(id);
			if(lst == null) {
				lst = new LinkedList<ChainVertex3D>();
				vClusters.put(id, lst);
			}
			lst.add(vert);
		}

		// Assign colors for vertices
		Map<ChainVertex3D, Color> vertexColors = assignColors(vClusters.values(),ColorBrewer.Dark2);
		for(Entry<ChainVertex3D, Color> entry: vertexColors.entrySet()) {
			entry.getKey().setColor(entry.getValue());
		}

		// Generate list of equivalent edges
		Map<Integer,List<InterfaceEdge3D>> eClusters = new HashMap<Integer, List<InterfaceEdge3D>>();
		for( InterfaceEdge3D edge : graph.edgeSet()) {
			Integer id = edge.getClusterId();
			List<InterfaceEdge3D> lst = eClusters.get(id);
			if(lst == null) {
				lst = new LinkedList<InterfaceEdge3D>();
				eClusters.put(id, lst);
			}
			lst.add(edge);
		}
		// Assign colors for edges
		Map<InterfaceEdge3D, Color> edgeColors = assignColors(eClusters.values(),ColorBrewer.Set2);
		for(Entry<InterfaceEdge3D, Color> entry: edgeColors.entrySet()) {
			entry.getKey().setColor(entry.getValue());
		}
	}

	/**
	 * Takes a list of lists. For each row, assigns a color to all list members.
	 * Returns a map from the list members to the color
	 * @param clusters Collection of clustered objects which should be colored alike
	 * @param palette Defaults to Dark2 if null
	 * @return
	 */
	private static <V> Map<V,Color> assignColors(Collection<? extends Collection<V>> clusters,ColorBrewer palette) {

		// Get color palette
		if(palette == null)
			palette = ColorBrewer.Dark2;
		int numColors = clusters.size();
		Color[] colors = palette.getColorPalette(Math.min(numColors,palette.getMaximumColorCount()));

		// Assign colors for vertices
		Map<V,Color> colorMap = new HashMap<V, Color>();
		int col = 0;
		for( Collection<V> clust : clusters) {
			for(V vert : clust) {
				colorMap.put(vert, colors[col]);
			}
			col = (col+1) % colors.length; // reuse colors if needed
		}
		return colorMap;
	}

	/**
	 * For each vertex and edge, set the colorStr property to the hex value of the color.
	 * 
	 */
	public void setHexColors() {
		for(ChainVertex3D v : graph.vertexSet()) {
			v.setColorStr(toHexColor(v.getColor()));
		}

		for(InterfaceEdge3D e : graph.edgeSet()) {
			String colorStr = toHexColor(e.getColor());
			e.setColorStr(colorStr);
		}
	}

	/**
	 * hex version of the color (e.g. 'FF00CC')
	 * @param color
	 * @return
	 */
	public static String toHexColor(Color color) {
		if(color == null) return null;
		return String.format("%02x%02x%02x", color.getRed(),color.getGreen(),color.getBlue());
	}

	/**
	 * Calculate the intersection of a line (given by two points) with a plane
	 * (given by a point and a normal vector).
	 * @param segA
	 * @param segB
	 * @param origin
	 * @param normal
	 */
	@SuppressWarnings("unused")
	private static Point3d planeIntersection(Point3d segA, Point3d segB, Point3d origin, Vector3d normal) {
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
	 * Writes to given PrintWriter the whole unit cell in mmCIF format, with chain ids as follows:
	 *  <li> author_ids: chainId_operatorId</li>
	 *  <li> asym_ids: chainId_operatorId</li>
	 * The atom ids are renumbered, so that symmetry partners don't repeat them.
	 * Otherwise some molecular viewers (e.g. 3Dmol.js) won't be able to read the atoms
	 * as distinct.
	 * <p>
	 * Note that PyMOL supports multi-letter chain ids only from 1.7.4
	 * @param out the writer to write the mmCIF data to
	 * @throws IOException
	 */
	public void writeCellToMmCifFile(PrintWriter out) throws IOException {
		
		// Some molecular viewers like 3Dmol.js need globally unique atom identifiers (across chains)
		// With the approach below we add an offset to atom ids of sym-related molecules to avoid repeating atom ids
		
		// we only do renumbering in the case that there are sym-related chains in the assembly
		// that way we stay as close to the original as possible
		boolean symRelatedChainsExist = false;
		int numChains = structure.size();
		Set<String> uniqueChains = new HashSet<String>();
		for (ChainVertex3D cv:getGraph().vertexSet()) {
			uniqueChains.add(cv.getChain().getName());
		}
		if (numChains != uniqueChains.size()) symRelatedChainsExist = true;

		out.println(SimpleMMcifParser.MMCIF_TOP_HEADER+"eppic_unit_cell");

		// Cell and space group info
		PDBCrystallographicInfo crystalInfo = structure.getCrystallographicInfo();
		if(crystalInfo == null) {
			logger.error("No crystallographic info set for this structure.");
			// leads to NullPointer
		} else {
			out.print(MMCIFFileTools.toMMCIF("_cell", MMCIFFileTools.convertCrystalCellToCell(crystalInfo.getCrystalCell())));
			out.print(MMCIFFileTools.toMMCIF("_symmetry", MMCIFFileTools.convertSpaceGroupToSymmetry(crystalInfo.getSpaceGroup())));
		}

		out.print(FileConvert.getAtomSiteHeader());

		List<AtomSite> atomSites = new ArrayList<>();

		int atomId = 1;
		for (ChainVertex3D cv:getGraph().vertexSet()) {
			String chainId = cv.getChain().getName()+"_"+cv.getOpId();
			//TODO maybe need to clone and transform here?
			Matrix4d m = getUnitCellTransformationOrthonormal(cv.getChain().getName(), cv.getOpId());
			//Point3d refCoord = graph.getReferenceCoordinate(cv.getChainId());

			Chain newChain = (Chain) cv.getChain().clone();
			Calc.transform(newChain, m);

			for (Group g: newChain.getAtomGroups()) {
				for (Atom a: g.getAtoms()) {
					if (symRelatedChainsExist) 
						atomSites.add(MMCIFFileTools.convertAtomToAtomSite(a, 1, chainId, chainId, atomId));
					else 
						atomSites.add(MMCIFFileTools.convertAtomToAtomSite(a, 1, chainId, chainId));

					atomId++;
				}
				for (Group altG:g.getAltLocs()) {
					for (Atom a: altG.getAtoms()) {

						if (symRelatedChainsExist)
							atomSites.add(MMCIFFileTools.convertAtomToAtomSite(a, 1, chainId, chainId, atomId));
						else
							atomSites.add(MMCIFFileTools.convertAtomToAtomSite(a, 1, chainId, chainId));

						atomId++;
					}
				}
			}
		}

		out.print(MMCIFFileTools.toMMCIF(atomSites, AtomSite.class));


		out.close();
	}
	
	/**
	 * Returns a set of all unique transformations needed to create the unit cell (one per vertex in lattice graph).
	 * @return
	 */
	public Set<Matrix4d> getUnitCellTransforms() {
		Set<Matrix4d> transforms = new HashSet<>();
		for (ChainVertex3D cv:getGraph().vertexSet()) {
			Matrix4d m = getUnitCellTransformationOrthonormal(cv.getChain().getName(), cv.getOpId());
			
			transforms.add(m);
		}
		
		return transforms;
	}
	
	/**
	 *
	 * @param edge The edge to add segments to
	 * @param posA start position, unwrapped
	 * @param posB end position, unwrapped
	 * @param ucPosA start position, wrapped to unit cell
	 * @param ucPosB end position, wrapped to unit cell
	 * @param cell Unit cell parameters
	 *
		private List<Pair<Point3d>> wrapEdge(InterfaceEdge3D edge, Point3d posA, Point3d posB,
				Point3d ucPosA, Point3d ucPosB, CrystalCell cell) {
			List<Pair<Point3d>> segments = new ArrayList<Pair<Point3d>>(2);

			Point3i cellA = cell.getCellIndices(posA);
			Point3i cellB = cell.getCellIndices(posB);

			// no wrapping within cell
			if( cellA.equals(cellB)) {
				segments.add(new Pair(ucPosA, ucPosB));
				return segments;
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

			segments.add(new Pair(ucPosA, intersectionA));
			segments.add(new Pair(intersectionB, ucPosB));
			return segments;
		}
	 */
	
	public Point3d cellCenter() {
		Point3d center = new Point3d(.5,.5,.5);
		CrystalCell cell = structure.getCrystallographicInfo().getCrystalCell();
		cell.transfToOrthonormal(center);
		cell.transfToOriginCell(center);
		return center;
	}
	

}
