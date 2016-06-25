package eppic.assembly.gui;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.PDBCrystallographicInfo;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.contact.Pair;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.gui.BiojavaJmol;
import org.biojava.nbio.structure.io.MMCIFFileReader;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.biojava.nbio.structure.io.util.FileDownloadUtils;
import org.biojava.nbio.structure.xtal.CrystalBuilder;
import org.biojava.nbio.structure.xtal.CrystalCell;
import org.biojava.nbio.structure.xtal.CrystalTransform;
import org.jcolorbrewer.ColorBrewer;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.MaskFunctor;
import org.jgrapht.graph.UndirectedMaskSubgraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;

import eppic.assembly.ChainVertex;
import eppic.assembly.InterfaceEdge;
import eppic.assembly.LatticeGraph;

public class LatticeGUI {
	private static Logger logger = LoggerFactory.getLogger(LatticeGUI.class);


	public enum WrappingPolicy {
		DONT_WRAP, // Don't wrap edges. Draw directly between vertices
		WRAP_TO_CELL, // Wrap lines exactly to cell boundary
		DUPLICATE, // Duplicate edges on each side of the cell boundary
	}


	//	private static interface VertexPositioner {
	//		public Point3d getUnitCellPos(Chain chain, int au);
	//	}
	//	private static class GlobalCentroidPositioner implements VertexPositioner{
	//		private Matrix4d[] unitCellOps;
	//		private Point3d refPoint;
	//		
	//		public GlobalCentroidPositioner(Structure s, CrystalCell cell, Matrix4d[] spaceOps) {
	//			// Compute reference point to locate in unit cell
	//			// This could also be done for each chain separately, to match Jmol positions
	//			Atom[] ca = StructureTools.getRepresentativeAtomArray(s);
	//			Atom centroidAtom = Calc.getCentroid(ca);
	//			refPoint = new Point3d(centroidAtom.getCoords());
	//			
	//			unitCellOps = cell.transfToOriginCell(spaceOps, refPoint);
	//		}
	//		
	//		public Point3d getUnitCellPos(Chain chain, int au) {
	//			// Get chain centroid
	//			Atom[] ca = StructureTools.getRepresentativeAtomArray(chain);
	//			Atom centroidAtom = Calc.getCentroid(ca);
	//			Point3d auPos = new Point3d(centroidAtom.getCoords());
	//
	//			// Transform via spaceOp
	//			unitCellOps[au].transform(auPos);
	//			return auPos;
	//		}
	//	}

	private Structure structure;
	private CrystalCell cell;

	private LatticeGraph<ChainVertex, InterfaceEdge> graph;

	// Position of each chain in the asymmetric unit
	private Map<String,Point3d> chainCentroid;

	private WrappingPolicy policy;
	
	private Set<Integer> debugInterfaceIds; // interface ids to display (for debugging, read from command line args), if null all displayed

	// colors
	private Map<ChainVertex,Color> vertexColors = null;
	private Map<InterfaceEdge,Color> edgeColors = null;

	public LatticeGUI(Structure struc) throws StructureException {
		this(struc,null);
	}
	public LatticeGUI(Structure struc, StructureInterfaceList interfaces) throws StructureException {
		this.structure = struc;
		this.policy = WrappingPolicy.DUPLICATE;

		// calculate interfaces
		if(interfaces == null) {
			interfaces = calculateInterfaces(struc);
		}

		this.graph = new LatticeGraph<ChainVertex, InterfaceEdge>(struc, interfaces,ChainVertex.class, InterfaceEdge.class);

		// Cell and space group info
		PDBCrystallographicInfo crystalInfo = structure.getCrystallographicInfo();
		if(crystalInfo == null) {
			logger.error("No crystallographic info set for this structure.");
			// leads to NullPointer
		}
		cell = crystalInfo.getCrystalCell();

		// Compute AU positions for each vertex
		chainCentroid = new HashMap<String,Point3d>();
		for(Chain c: structure.getChains() ) {
			chainCentroid.put(c.getChainID(), getCentroid(c));
		}

		//assignColorsById();
		assignColorsByEntity();
	}

	public void setDebugInterfaceIds(String commaSepList) {
		
		if (commaSepList==null) return;
		
		debugInterfaceIds = new HashSet<Integer>();
		String[] items = commaSepList.split(",");
		for (String item:items) {
			debugInterfaceIds.add(Integer.parseInt(item));
		}
	}
	
	private static Point3d getCentroid(Chain c) {
		Atom[] ca = StructureTools.getRepresentativeAtomArray(c);
		Atom centroidAtom = Calc.getCentroid(ca);
		return new Point3d(centroidAtom.getCoords());
	}

	/**
	 * @param struc
	 * @return
	 */
	private StructureInterfaceList calculateInterfaces(Structure struc) {
		CrystalBuilder builder = new CrystalBuilder(struc);
		StructureInterfaceList interfaces = builder.getUniqueInterfaces();
		logger.info("Calculating ASA for "+interfaces.size()+" potential interfaces");
		interfaces.calcAsas(StructureInterfaceList.DEFAULT_ASA_SPHERE_POINTS/3, //fewer for performance
				Runtime.getRuntime().availableProcessors(),
				StructureInterfaceList.DEFAULT_MIN_COFACTOR_SIZE);
		interfaces.removeInterfacesBelowArea();
		interfaces.getClusters();
		logger.info("Found "+interfaces.size()+" interfaces");
		return interfaces;
	}

	public BiojavaJmol display(String filename) throws StructureException {
		BiojavaJmol jmol = new BiojavaJmol();
		jmol.setTitle(filename);
		//jmol.setStructure(struc);
		jmol.evalString(String.format("load \"%s\" {1 1 1};",filename));
		jmol.evalString("set unitcell {0 0 0};");
		//jmol.evalString("");
		jmol.evalString(drawVertices());
		jmol.evalString(drawEdges());
		// cartoon
		//jmol.evalString("hide null; select all;  spacefill off; wireframe off; backbone off; cartoon on;  select ligand; wireframe 0.16;spacefill 0.5; color cpk;  select *.FE; spacefill 0.7; color cpk ;  select *.CU; spacefill 0.7; color cpk ;  select *.ZN; spacefill 0.7; color cpk ;  select alls ON;");
		jmol.evalString("select all; spacefill off; wireframe off; backbone off; cartoon off; restrict not water; select none;");
		jmol.evalString("set axes molecular;");

		jmol.getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		return jmol;
	}
	
	public void display2D() {
		MaskFunctor<ChainVertex, InterfaceEdge> mask = new MaskFunctor<ChainVertex, InterfaceEdge>() {
			@Override
			public boolean isEdgeMasked(InterfaceEdge edge) {
				if(debugInterfaceIds!=null && !debugInterfaceIds.contains(edge.getInterfaceId())) {
					return true;
				}
				ChainVertex source = graph.getGraph().getEdgeSource(edge);
				if(debugAUNum != null && source.getOpId() != debugAUNum) {
					return true;
				}
				return false;
			}

			@Override
			public boolean isVertexMasked(ChainVertex vertex) {
				return false;
			}
		};
		UndirectedGraph<ChainVertex,InterfaceEdge> subgraph = new UndirectedMaskSubgraph<ChainVertex,InterfaceEdge>(this.graph.getGraph(), mask);
		JGraphXAdapter<ChainVertex, InterfaceEdge> jgraph = new JGraphXAdapter<ChainVertex,InterfaceEdge>(subgraph);

		mxGraphComponent graphComponent = new mxGraphComponent(jgraph);
		graphComponent.setSize(700, 700);

		
		//Layout
		final mxFastOrganicLayout layout = new mxFastOrganicLayout(jgraph);
		//default 50
//		layout.setForceConstant(100);
//		layout.setInitialTemp(500);
//		layout.setMaxIterations(1000);
		layout.setMaxDistanceLimit(300);
		System.out.format("Force=%f\tTemp=%f\tIter=%f\tlimits=%f-%f%n",layout.getForceConstant(),layout.getInitialTemp(),layout.getMaxIterations(),layout.getMinDistanceLimit(),layout.getMaxDistanceLimit());
		layout.execute(jgraph.getDefaultParent());

		//Colors
		for(Entry<ChainVertex,Color> entry: vertexColors.entrySet()) {
			mxICell cell = jgraph.getVertexToCellMap().get(entry.getKey());
			Color color = entry.getValue();
			String hexColor = String.format("#%02x%02x%02x", color.getRed(),color.getGreen(),color.getBlue());
			jgraph.setCellStyles(mxConstants.STYLE_FILLCOLOR, hexColor, new Object[] {cell});
			jgraph.setCellStyles(mxConstants.STYLE_FONTCOLOR, "#FFFFFF", new Object[] {cell});
		}
		for(Entry<InterfaceEdge,Color> entry: edgeColors.entrySet()) {
			mxICell cell = jgraph.getEdgeToCellMap().get(entry.getKey());
			Color color = entry.getValue();
			String hexColor = String.format("#%02x%02x%02x", color.getRed(),color.getGreen(),color.getBlue());
			jgraph.setCellStyles(mxConstants.STYLE_STROKECOLOR, hexColor, new Object[] {cell});
			jgraph.setCellStyles(mxConstants.STYLE_FONTCOLOR, hexColor, new Object[] {cell});
			
		}
		
		graphComponent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JFrame frame = new JFrame("JGraph");
		frame.getContentPane().add(graphComponent);
		frame.pack();
		frame.setVisible(true);
	}

	private String drawEdges() throws StructureException {
		StringBuilder jmol = new StringBuilder();

		jmol.append( "set defaultDrawArrowScale 8\n");
//		jmol.append( drawSphere("Centroid",Color.RED,chainCentroid.get("A"),"Centroid"));
//		Point3d a0 = new Point3d(chainCentroid.get("A"));
//		structure.getCrystallographicInfo().getTransformationsOrthonormal()[7].transform(a0);
//		jmol.append( drawSphere("Centroid0",Color.PINK,a0,"Centroid0"));
//		Point3d b = new Point3d(chainCentroid.get("A"));
//		Matrix4d bXtal = interfaces.get(1).getTransforms().getSecond().getMatTransform();
//		cell.transfToOrthonormal(bXtal).transform(b);
//		jmol.append( drawSphere("B",Color.WHITE,b,"B"));

		UndirectedGraph<ChainVertex, InterfaceEdge> g = graph.getGraph();
		Set<InterfaceEdge> edges = g.edgeSet();
		for(InterfaceEdge edge : edges) {
			ChainVertex source = g.getEdgeSource(edge);
			ChainVertex target = g.getEdgeTarget(edge);
			
			if(debugInterfaceIds!=null && !debugInterfaceIds.contains(edge.getInterfaceId())) {
				continue;
			}
			if(debugAUNum != null && source.getOpId() != debugAUNum) {
				continue;
			}

			logger.info("Edge {}{} -{}- {}{} translation: {}",
					source.getChainId(),source.getOpId(),
					edge.getInterfaceId(),
					target.getChainId(),target.getOpId(),
					edge.getXtalTrans() );
			
			switch(this.policy) {
			case DONT_WRAP: {
				Point3d sourcePos = getPosition(source);
				Point3d targetPos = getPosition(target);
				Point3d mid = new Point3d();
				mid.interpolate(sourcePos, targetPos, .5);
				
				jmol.append( drawEdge(edge, sourcePos, targetPos));
				jmol.append( drawLabel("edge"+edge,getEdgeColor(edge),mid,getEdgeLabel(edge)));
				
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

				// Vertex positions within the unit cell
				Point3d sourcePos = getPosition(source);
//				Point3d targetPos = getPosition(target);
//				jmol.append( drawSphere("TargetPos",Color.DARK_GRAY,targetPos,"TargetPos"));

				// Calculate target position before wrapping
				Point3d unwrappedTarget = new Point3d(chainCentroid.get(target.getChainId()));
				Matrix4d transBOrtho = cell.transfToOrthonormal(transB);
				transBOrtho.transform(unwrappedTarget);
//				jmol.append( drawSphere("Target0",Color.PINK,chainCentroid.get(target.getChainId()),"Target0"));
//				jmol.append( drawSphere("Unwrapped",Color.DARK_GRAY,unwrappedTarget,"Unwrapped"));
				graph.getUnitCellTransformationOrthonormal(source.getChainId(), source.getOpId()).transform(unwrappedTarget);

				// Location of interface circle
				Point3d midPoint = new Point3d();
				midPoint.add(sourcePos,unwrappedTarget);
				midPoint.scale(.5);

				// wrap to source
				Point3d sourceReference = new Point3d(graph.getReferenceCoordinate(source.getChainId()));
				graph.getUnitCellTransformationOrthonormal(source.getChainId(), source.getOpId()).transform(sourceReference);
//				jmol.append( drawSphere("sourceAU"+source.getOpId(), Color.ORANGE, sourceReference, "sourceAU"+source.getOpId()));
				Point3d[] sourceEdgePos = new Point3d[] {new Point3d(sourcePos),new Point3d(midPoint), new Point3d(unwrappedTarget)};
				cell.transfToOriginCell(sourceEdgePos,sourceReference);
//				jmol.append( drawSphere("Source"+source.getOpId(),Color.CYAN,sourceEdgePos[0],"Source"+source.getOpId()));
//				jmol.append( drawSphere("Mid"+source.getOpId(),Color.MAGENTA,sourceEdgePos[1],"Mid"+source.getOpId()));
//				jmol.append( drawSphere("Target"+source.getOpId(),Color.GREEN,sourceEdgePos[2],"Target"+source.getOpId()));

				//jmol.append( drawLabel("edge"+edge,getEdgeColor(edge),sourceEdgePos[1],getEdgeLabel(edge)));
				jmol.append( drawInterfaceCircle(edge, sourceEdgePos[1], sourceEdgePos[0] ) );
				jmol.append( drawEdge(edge, sourceEdgePos[0], sourceEdgePos[2]) );

				// wrap to target
				Point3d targetReference = new Point3d(graph.getReferenceCoordinate(target.getChainId()));
				transBOrtho.transform(targetReference);
				graph.getUnitCellTransformationOrthonormal(source.getChainId(), source.getOpId()).transform(targetReference);
//				jmol.append( drawSphere("targetAU"+target.getOpId(), Color	.RED, targetReference, "targetAU"+target.getOpId()));
				Point3d[] targetEdgePos = new Point3d[] {new Point3d(sourcePos),new Point3d(midPoint), new Point3d(unwrappedTarget)};
				cell.transfToOriginCell(targetEdgePos,targetReference);
//				jmol.append( drawSphere("SourceT"+target.getOpId(),Color.CYAN.darker(),targetEdgePos[0],"SourceT"+target.getOpId()));
//				jmol.append( drawSphere("MidT"+target.getOpId(),Color.MAGENTA.darker(),targetEdgePos[1],"MidT"+target.getOpId()));
//				jmol.append( drawSphere("TargetT"+target.getOpId(),Color.GREEN.darker(),targetEdgePos[2],"TargetT"+target.getOpId()));


				if( ! sourceEdgePos[2].epsilonEquals(targetEdgePos[2], 1e-4)) {
					//jmol.append( drawLabel("edge"+edge,getEdgeColor(edge),targetEdgePos[1],getEdgeLabel(edge)));
					jmol.append( drawInterfaceCircle(edge, targetEdgePos[1], targetEdgePos[0] ) );
					jmol.append( drawEdge(edge, targetEdgePos[0], targetEdgePos[2]) );
				} else {
					logger.debug("Source and target for {} within unit cell",edge);
				}

				break;
			}
			default:
				throw new UnsupportedOperationException();
			}
		}
		logger.debug("Edge Jmol commands:\n{}",jmol.toString());
		return jmol.toString();
	}
	
	private String getXtalTransString(InterfaceEdge edge) {
		int[] coords = new int[3];
		edge.getXtalTrans().get(coords);
		final String[] letters = new String[] {"a","b","c"};

		StringBuilder str = new StringBuilder();
		for(int i=0;i<3;i++) {
			if( coords[i] != 0) {
				if( Math.abs(coords[i]) == 1 ) {
					if(str.length()>0)
						str.append(',');
					String sign = coords[i] > 0 ? "+" : "-";
					str.append(sign+letters[i]);
				} else {
					if(str.length()>0)
						str.append(',');
					str.append(String.format("%d%s",coords[i],letters[i]));
				}
			}
		}
		return str.toString();
	}
	private String getEdgeLabel(InterfaceEdge edge) {
		String xtal = getXtalTransString(edge);
		if(xtal != null && !xtal.isEmpty()) {
			return String.format("%d: %s", edge.getInterfaceId(),xtal );
		} else {
			return String.valueOf(edge.getInterfaceId());
		}
	}

	private String drawInterfaceCircle(InterfaceEdge edge, Point3d pos, Point3d perpendicular) {
		// draw interface circle
		String color = toJmolColor(getEdgeColor(edge));
		if( color == null ) {
			color = "white";
		}
		String name = toUniqueJmolID(String.format("interface%s_%d",
				edge.getInterfaceId(), graph.getGraph().getEdgeSource(edge).getOpId()));
		String jmol = String.format("draw ID \"%s\" \"%s\" CIRCLE {%f,%f,%f} {%f,%f,%f} DIAMETER 5.0 COLOR %s;%n",
				name,getEdgeLabel(edge),
				pos.x,pos.y,pos.z,
				perpendicular.x,perpendicular.y,perpendicular.z,
				color );
		//		jmol += String.format("set echo ID echoInterface%s {%f,%f,%f}; color echo %s;  echo %s;%n",
		//				edge.getInterfaceId(), pos.x,pos.y,pos.z,
		//				color, edge.getInterfaceId());
		return jmol;
	}
	private String drawEdge(InterfaceEdge edge, Point3d posA, Point3d posB) {
		Vector3d ab = new Vector3d();
		ab.sub(posB, posA);

		// Start and end a fixed distance from the end
		final double gapDist = 5.5; // Angstroms; slightly more than drawSphere radius
		Point3d start = new Point3d();
		start.scaleAdd(gapDist/ab.length(), ab, posA);
		Point3d end = new Point3d();
		end.scaleAdd(1 - gapDist/ab.length(), ab, posA);
		
		// Midpoint is offset by a small amount in a random direction
		final double midOffset = 4.;
		// Random vector orthogonal to the line between the two points
		Vector3d orthogonal = randomOrthogonalVector(ab);
		
		Point3d mid = new Point3d();
		mid.scaleAdd(.5, ab, posA);
		mid.scaleAdd(midOffset,orthogonal,mid);//orthogonal is normalized
		

		String color;
		color = toJmolColor(getEdgeColor(edge));
		if( color == null ) {
			color = "white";
		}

		ChainVertex source = graph.getGraph().getEdgeSource(edge);
		ChainVertex target = graph.getGraph().getEdgeTarget(edge);
		String name = toUniqueJmolID(String.format("edge_%s_%s", source,target ));
		String jmol = String.format("draw ID \"%s\" ARROW {%f,%f,%f} {%f,%f,%f} {%f,%f,%f} COLOR %s;\n",
				name,
				start.x,start.y,start.z, mid.x,mid.y,mid.z, end.x,end.y,end.z,
				color );
		
		return jmol;
	}
	
	/**
	 * 
	 * @param n The plane normal
	 * @return A random normalized vector orthogonal to n
	 */
	private Vector3d randomOrthogonalVector(Vector3d n) {
		Vector3d random;
		do {
			// Sample uniformly at random from unit sphere
			random = new Vector3d( Math.random()*2-1.0, Math.random()*2-1.0, Math.random()*2-1.0 );
		} while( random.lengthSquared() > 1);
		
		// Project random onto the plane defined by n and the origin
		Vector3d vecToPlane = new Vector3d(n);
		vecToPlane.normalize();
		vecToPlane.scale( random.dot(vecToPlane) );
		random.sub(vecToPlane);

		// Normalize
		random.normalize();
		return random;
	}
	
	private String drawSphere(String name, Color color, Point3d pos, String label) {
		if(color == null) color = Color.WHITE;
		name = toUniqueJmolID(name);
		String echoName = toUniqueJmolID("echo"+name);
		String colorStr = toJmolColor(color);
		String jmol = String.format("isosurface ID \"%s\" COLOR %s CENTER {%f,%f,%f} SPHERE 5.0;\n",
				name, colorStr, pos.x,pos.y,pos.z );
		jmol += drawLabel(echoName,color,pos,label);
		return jmol;
	}
	
	private String drawLabel(String name, Color color, Point3d pos, String label) {
		if(color == null) color = Color.WHITE;
		String colorStr = toJmolColor(color);
		name = toUniqueJmolID(name);
		return String.format("set echo ID \"%s\" {%f,%f,%f}; color echo %s; echo \"  %s\";\n",
				name, pos.x,pos.y,pos.z,colorStr, label);
	}
	
	private Set<String> jmolIDs = new HashSet<String>();
	/**
	 * Insures that a Jmol identifier is unique. If an identifier has been used
	 * previously, appends an integer sequentially until it is unique
	 * @param name Seed identifier
	 * @return a unique string starting with name
	 */
	private String toUniqueJmolID(String name) {
		if(name == null) return null;
		String uid = name;
		int i = 0;
		synchronized(jmolIDs) {
			while(jmolIDs.contains(uid)) {
				uid = String.format("%s #%d",name,i);
				i++;
			}
			jmolIDs.add(uid);
		}
		return uid;
	}
	private String drawVertices() throws StructureException {
		StringBuilder jmol = new StringBuilder();

		Set<ChainVertex> vertices = graph.getGraph().vertexSet();
		for(ChainVertex v : vertices) {
			Color color = getColor(v);
			if( color == null ) {
				color = Color.BLUE;
			}
			// Unit cell pos
			Point3d pos = getPosition(v);

			// Create Jmol string for this vertex
//			jmol.append(String.format("isosurface ID chain%s COLOR %s CENTER {%f,%f,%f} SPHERE 5.0;\n",
//					v, color, pos.x,pos.y,pos.z ));
//			jmol.append(String.format("set echo ID echoChain%s {%f,%f,%f}; color echo %s; echo \"  %s\";\n",
//					v, pos.x,pos.y,pos.z,color,v));
			jmol.append(drawSphere("chain"+v.toString(), color, pos,v.toString()));

		}
		logger.debug("Vertex Jmol commands:\n{}",jmol.toString());
		return jmol.toString();
	}


	private Point3d getPosition(ChainVertex v) throws StructureException {
		String chainId = v.getChainId();
		int au = v.getOpId();

		// AU pos
		Point3d pos = new Point3d( chainCentroid.get(chainId) );
		// Unit cell pos
		graph.getUnitCellTransformationOrthonormal(chainId, au).transform(pos);
		return pos;
	}


	/**
	 * Color vertices according to the chain ID and edges by interface ID
	 * @see #assignColorsByEntity()
	 */
	public void assignColorsById() {
		// Generate list of equivalent chains
		Map<String,List<ChainVertex>> vClusters = new HashMap<String, List<ChainVertex>>();
		for( ChainVertex vert : graph.getGraph().vertexSet()) {
			String id = vert.getChainId();
			List<ChainVertex> lst = vClusters.get(id);
			if(lst == null) {
				lst = new LinkedList<ChainVertex>();
				vClusters.put(id, lst);
			}
			lst.add(vert);
		}

		// Assign colors for vertices
		vertexColors = assignColors(vClusters.values(),ColorBrewer.Dark2);

		// Generate list of equivalent edges
		Map<Integer,List<InterfaceEdge>> eClusters = new HashMap<Integer, List<InterfaceEdge>>();
		for( InterfaceEdge edge : graph.getGraph().edgeSet()) {
			Integer id = edge.getInterfaceId();
			List<InterfaceEdge> lst = eClusters.get(id);
			if(lst == null) {
				lst = new LinkedList<InterfaceEdge>();
				eClusters.put(id, lst);
			}
			lst.add(edge);
		}
		// Assign colors for edges
		edgeColors = assignColors(eClusters.values(),ColorBrewer.Set2);
	}
	/**
	 * Color vertices by entity and edges by interface cluster (default)
	 * @see #assignColorsById()
	 */
	public void assignColorsByEntity() {
		// Generate list of equivalent chains
		Map<Integer,List<ChainVertex>> vClusters = new HashMap<Integer, List<ChainVertex>>();
		for( ChainVertex vert : graph.getGraph().vertexSet()) {
			Integer id = vert.getEntityId();
			List<ChainVertex> lst = vClusters.get(id);
			if(lst == null) {
				lst = new LinkedList<ChainVertex>();
				vClusters.put(id, lst);
			}
			lst.add(vert);
		}

		// Assign colors for vertices
		vertexColors = assignColors(vClusters.values(),ColorBrewer.Dark2);

		// Generate list of equivalent edges
		Map<Integer,List<InterfaceEdge>> eClusters = new HashMap<Integer, List<InterfaceEdge>>();
		for( InterfaceEdge edge : graph.getGraph().edgeSet()) {
			Integer id = edge.getClusterId();
			List<InterfaceEdge> lst = eClusters.get(id);
			if(lst == null) {
				lst = new LinkedList<InterfaceEdge>();
				eClusters.put(id, lst);
			}
			lst.add(edge);
		}
		// Assign colors for edges
		edgeColors = assignColors(eClusters.values(),ColorBrewer.Set2);
	}
	
	/**
	 * Takes a list of lists. For each row, assigns a color to all list members.
	 * Returns a map from the list members to the color
	 * @param inputs Collection of clustered objects which should be colored alike
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
	
	
	private Color getColor(ChainVertex v) {
		if(vertexColors != null && vertexColors.containsKey(v)) {
			return vertexColors.get(v);
		}
		return Color.yellow;
	}
	private Color getEdgeColor(InterfaceEdge e) {
		if(edgeColors != null && edgeColors.containsKey(e)) {
			return edgeColors.get(e);
		}
		return Color.GRAY;
	}
	private static String toJmolColor(Color color) {
		if(color == null) return null;
		return String.format("[%f,%f,%f]", color.getRed()/256f,color.getGreen()/256f,color.getBlue()/256f);
	}

	private static final Integer debugAUNum = null; // single AU to display or null for all

	public static void main(String[] args) throws IOException, StructureException {
		
		if (args.length<1) {
			logger.error("No PDB code or file name given.");
			logger.error("Usage: LatticeGUI <PDB code or file> [comma separated list of interfaces to display]");
			System.exit(1);
		}
		
		
		String input = args[0];
		String interfaceIdsCommaSep = null;
		if (args.length==2) {
			interfaceIdsCommaSep = args[1];
		}

		String filename;
		Structure struc;
		
		if (new File(FileDownloadUtils.expandUserHome(input)).exists()) {
			filename = FileDownloadUtils.expandUserHome(input);
			struc = StructureTools.getStructure(filename);
		} else if (input.matches("\\d\\w\\w\\w")){
			AtomCache cache = new AtomCache();
			cache.setUseMmCif(true);
			struc = cache.getStructure(input);
			File file = getFile(cache,input);
			if(!file.exists() ) {
				logger.error(String.format("Error loading %s from %s",input,file.getAbsolutePath()));
				System.exit(1);
			}
			filename = file.getAbsolutePath();
		} else {
			filename = null;
			struc = null;
			logger.error("Input doesn't seem to be a file or a PDB code. Exiting");
			System.exit(1);
		}
		
		LatticeGUI gui = new LatticeGUI(struc);
		gui.setDebugInterfaceIds(interfaceIdsCommaSep);
		gui.display(filename);
		gui.display2D();
	}

	/**
	 * Tries to guess the file location from an AtomCache
	 * @param cache
	 * @param name
	 * @return
	 */
	public static File getFile(AtomCache cache, String name) {
		if(cache.isUseMmCif()) {
			MMCIFFileReader reader = new MMCIFFileReader(cache.getPath());
			reader.setFetchBehavior(cache.getFetchBehavior());
			reader.setObsoleteBehavior(cache.getObsoleteBehavior());

			File file = reader.getLocalFile(name);
			return file;
		} else {
			PDBFileReader reader = new PDBFileReader(cache.getPath());
			reader.setFetchBehavior(cache.getFetchBehavior());
			reader.setObsoleteBehavior(cache.getObsoleteBehavior());

			reader.setFileParsingParameters(cache.getFileParsingParams());

			File file = reader.getLocalFile(name); 

			return file;
		}
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
	 *
	 * @param edge The edge to add segments to
	 * @param posA start position, unwrapped
	 * @param posB end position, unwrapped
	 * @param ucPosA start position, wrapped to unit cell
	 * @param ucPosB end position, wrapped to unit cell
	 * @param cell Unit cell parameters
	 *
	private List<Pair<Point3d>> wrapEdge(InterfaceEdge edge, Point3d posA, Point3d posB,
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

}
