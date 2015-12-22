package eppic.assembly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.PDBCrystallographicInfo;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.xtal.CrystalCell;
import org.biojava.nbio.structure.xtal.SpaceGroup;
import org.jgrapht.EdgeFactory;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.ClassBasedVertexFactory;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;



/**
 * Graph representation of the interfaces in a unit cell.
 * 
 * Nodes are {@link ChainVertex} objects, which are identified by a chain
 * (in the asymmetric unit) and a crystallographic operator giving the
 * transform to the unit cell.
 * 
 * Edges are {@link InterfaceEdge} objects, identified by an EPPIC interface
 * and associated with a vector indicating whether the edge connects to
 * adjacent cells.
 * 
 * The class is generic, allowing other properties to be associated with
 * nodes and edges through subclasses. For instance, {@link LatticeGraph3D}
 * includes information for visualizing the graph.
 * 
 * @author Spencer Bliven, Jose Duarte
 *
 * @param <V>
 * @param <E>
 */
public class LatticeGraph<V extends ChainVertex,E extends InterfaceEdge> {

	private static final Logger logger = LoggerFactory.getLogger(LatticeGraph.class);


	private final Structure struct;
	private List<StructureInterface> interfaces;

	private UndirectedGraph<V,E> graph;
	
	private VertexFactory<V> vertexFactory;
	private EdgeFactory<V,E> edgeFactory;

	private boolean globalReferencePoint;
	private Map<String,Matrix4d[]> unitCellOperators = new HashMap<>(); // In crystal coordinates
	private Map<String,Point3d> referencePoints = new HashMap<>(); // Chain ID -> centroid coordinate

	/**
	 * Create the graph, initializing given the input structure and interfaces.
	 * @param struct
	 * @param interfaces
	 * @param vertexClass
	 * @param edgeClass
	 * @throws StructureException
	 */
	public LatticeGraph(Structure struct, List<StructureInterface> interfaces, Class<? extends V> vertexClass,Class<? extends E> edgeClass) throws StructureException {

		this.struct = struct;
		this.interfaces = interfaces;

		vertexFactory = new ClassBasedVertexFactory<V>(vertexClass);
		edgeFactory = new ClassBasedEdgeFactory<V, E>(edgeClass);
		this.graph = new Pseudograph<V,E>(edgeFactory);

		globalReferencePoint = true;


		initLatticeGraphTopologically();
		logGraph();

	}

	public LatticeGraph(Structure structure,
			StructureInterfaceList interfaces, Class<? extends V> vertexClass,
			Class<? extends E> edgeClass) throws StructureException {
		this(structure, Lists.newArrayList(interfaces),vertexClass,edgeClass);
	}

	/**
	 * Get the reference coordinate for a particular chain.
	 * 
	 * This is either the chain centroid or the asymmetric unit centroid,
	 * depending on the value of {@link #isGlobalReferencePoint()}.
	 * @param chainId
	 * @return
	 * @throws StructureException
	 */
	public Point3d getReferenceCoordinate(String chainId) throws StructureException {
		if( globalReferencePoint ) {
			// null is AU centroid
			if( ! referencePoints.containsKey(null) ) {
				Point3d globalCentroid = getCentroid(struct);
				referencePoints.put(null, globalCentroid);
				return globalCentroid;
			}
			return referencePoints.get(null);
		} else {
			if( ! referencePoints.containsKey(chainId)) {
				Point3d centroid = getCentroid(struct.getChainByPDB(chainId));
				referencePoints.put(chainId,centroid);
				return centroid;
			}
			return referencePoints.get(chainId);
		}
	}

	/**
	 * Get the set of transformations (in orthonormal coordinates) needed
	 * to transform the specified chain to the specified position in the unit cell
	 * @param chainId Chain ID
	 * @param opId operator number, relative to the space group transformations
	 * @return An array of transformation matrices
	 * @throws StructureException if an error occurs calculating the centroid
	 */
	public Matrix4d getUnitCellTransformationOrthonormal(String chainId, int opId) throws StructureException {
		return getUnitCellTransformationsOrthonormal(chainId)[opId];
	}
	/**
	 * Get the set of transformations (in orthonormal coordinates) needed
	 * to transform the specified chain to all locations in the unit cell
	 * @param chainId Chain ID
	 * @return An array of transformation matrices
	 * @throws StructureException if an error occurs calculating the centroid
	 */
	public Matrix4d[] getUnitCellTransformationsOrthonormal(String chainId) throws StructureException {
		PDBCrystallographicInfo crystalInfo = struct.getCrystallographicInfo();
		CrystalCell cell = crystalInfo.getCrystalCell();

		Matrix4d[] crystalTrnsf = getUnitCellTransformationsCrystal(chainId);

		Matrix4d[] orthoTrnsf = new Matrix4d[crystalTrnsf.length];
		for(int i=0;i<crystalTrnsf.length;i++) {
			orthoTrnsf[i] = cell.transfToOrthonormal(crystalTrnsf[i]);
		}

		return orthoTrnsf;
	}
	/**
	 * Get the set of transformations (in crystal coordinates) needed
	 * to transform the specified chain to the specified position in the unit cell
	 * @param chainId Chain ID
	 * @param opId operator number, relative to the space group transformations
	 * @return An array of transformation matrices
	 * @throws StructureException if an error occurs calculating the centroid
	 */
	public Matrix4d getUnitCellTransformationCrystal(String chainId, int opId) throws StructureException {
		return getUnitCellTransformationsCrystal(chainId)[opId];
	}
	/**
	 * Get the set of transformations (in crystal coordinates) needed
	 * to transform the specified chain to all locations in the unit cell
	 * @param chainId Chain ID
	 * @return An array of transformation matrices
	 * @throws StructureException if an error occurs calculating the centroid
	 */
	public Matrix4d[] getUnitCellTransformationsCrystal(String chainId) throws StructureException {
		Matrix4d[] chainTransformations;
		synchronized(unitCellOperators) {
			if( ! unitCellOperators.containsKey(chainId) ) {
				PDBCrystallographicInfo crystalInfo = struct.getCrystallographicInfo();
				SpaceGroup sg = crystalInfo.getSpaceGroup();
				CrystalCell cell = crystalInfo.getCrystalCell();

				// Transformations in crystal coords
				Matrix4d[] transfs = new Matrix4d[sg.getNumOperators()];
				for (int i=0;i<sg.getNumOperators();i++) {
					transfs[i] = sg.getTransformation(i);
				}


				// Reference in crystal coords
				Point3d reference = new Point3d(getReferenceCoordinate(chainId));
				cell.transfToCrystal(reference);

				chainTransformations = cell.transfToOriginCellCrystal(transfs, reference);
				unitCellOperators.put(chainId, chainTransformations);
			} else {
				chainTransformations = unitCellOperators.get(chainId);
			}
		}
		return chainTransformations;
	}

	/**
	 * Calculate the centroid for a chain
	 * @param c
	 * @return
	 */
	private static Point3d getCentroid(Chain c) {
		Atom[] ca = StructureTools.getRepresentativeAtomArray(c);
		Atom centroidAtom = Calc.getCentroid(ca);
		return new Point3d(centroidAtom.getCoords());
	}
	/**
	 * Calculate the centroid for a whole structure
	 * @param c
	 * @return
	 */
	private static Point3d getCentroid(Structure c) {
		Atom[] ca = StructureTools.getRepresentativeAtomArray(c);
		Atom centroidAtom = Calc.getCentroid(ca);
		return new Point3d(centroidAtom.getCoords());
	}


	public UndirectedGraph<V, E> getGraph() {
		return graph;
	}

	private void logGraph() {
		logger.info("Found {} vertices and {} edges in unit cell", graph.vertexSet().size(), graph.edgeSet().size());

		List<E> sortedEdges = new ArrayList<E>();
		sortedEdges.addAll(graph.edgeSet());
		Collections.sort(sortedEdges, new Comparator<E>() {
			@Override
			public int compare(E o1, E o2) {
				return new Integer(o1.getInterfaceId()).compareTo(new Integer(o2.getInterfaceId()));
			}			
		});

		for (E edge:sortedEdges) {
			V first = graph.getEdgeSource(edge);
			V second = graph.getEdgeTarget(edge);
			Point3i xtalT = edge.getXtalTrans();
			logger.info("Edge {} ({}) between {} ({}) - {} ({})"+
					String.format(" [%2d,%2d,%2d]", xtalT.x,xtalT.y,xtalT.z), 
					edge.getInterfaceId(),
					edge.getClusterId(),
					first.getChainId()+first.getOpId(), 
					first.getEntity(),
					second.getChainId()+second.getOpId(),
					second.getEntity());

		}

	}

	private void initLatticeGraphTopologically() throws StructureException {		

		final int numOps = struct.getCrystallographicInfo().getSpaceGroup().getNumOperators();

		for (Chain c:struct.getChains()) {
			
			if (c.getCompound()==null) {
				logger.warn("Chain {} will not be added to the graph because it does not have an entity associated to it.", c.getChainID());
				continue;
			}
			
			for (int i=0;i<numOps;i++) {
				V vertex = vertexFactory.createVertex();
				vertex.setChain(c);
				vertex.setOpId(i);
				graph.addVertex(vertex);
			}
		}

		for (StructureInterface interf:interfaces) {
			Matrix4d Ci = interf.getTransforms().getSecond().getMatTransform(); // crystal operator

			String sourceChainId = interf.getMoleculeIds().getFirst();
			String targetChainId = interf.getMoleculeIds().getSecond();
			for (int j=0;j<numOps;j++) {
				Matrix4d Tj = getUnitCellTransformationCrystal(sourceChainId, j);

				// Cij = Tj * Ci
				Matrix4d Cij = new Matrix4d(Tj);
				Cij.mul(Ci);

				// with Cij we obtain the end operator id
				int k = getEndAuCell(Cij,targetChainId);
				if( k < 0) {
					logger.error("No matching operator found for:\n{}",Cij);
					continue;
				}

				// translation is given by: X =  Tj * Ci * Tkinv
				Matrix4d Tk = getUnitCellTransformationCrystal(targetChainId, k);
				Matrix4d Tkinv = new Matrix4d(Tk);
				Tkinv.invert(); // Tkinv
				Matrix4d X = new Matrix4d(Cij);
				X.mul(Tkinv);

				Point3i xtalTrans = new Point3i(
						(int) Math.round(X.m03), (int) Math.round(X.m13), (int) Math.round(X.m23));


				V sVertex = vertexFactory.createVertex();
				sVertex.setChain(struct.getChainByPDB(sourceChainId));
				sVertex.setOpId(j);
				V tVertex = vertexFactory.createVertex();
				tVertex.setChain(struct.getChainByPDB(targetChainId));
				tVertex.setOpId(k);

				E edge = edgeFactory.createEdge(sVertex, tVertex);
				edge.setInterface(interf);
				edge.setXtalTrans(xtalTrans);
				
				graph.addEdge(sVertex, tVertex, edge);

			}
		}

	}

	/**
	 * Given an operator, returns the operator id of the matching generator
	 * @param m
	 * @return
	 * @throws StructureException 
	 */
	private int getEndAuCell(Matrix4d m, String chainId) throws StructureException {
		Matrix4d[] ops = getUnitCellTransformationsCrystal(chainId);
		for (int j=0;j<ops.length;j++) {
			Matrix4d Tj = ops[j];

			if (epsilonEqualsModulusXtal(Tj, m)) {
				return j;
			}
		}

		logger.warn("No matching generator found for operator:\n {}", m.toString());
		return -1;
	}

	/**
	 * Test whether two matrices have the same rotational component and an
	 * integer translational component.
	 * @param T First matrix, in crystal coordinates
	 * @param m Second matrix, in crystal coordinates
	 * @return
	 */
	private static boolean epsilonEqualsModulusXtal(Matrix4d T, Matrix4d m) {

		// T == m  <=>  T - m = 0
		Matrix4d sub = new Matrix4d(T);
		sub.sub(m);

		for (int i=0;i<3;i++) {
			for (int j=0;j<3;j++) {
				if (Math.abs(sub.getElement(i, j))>0.0001) {
					return false;
				}
			}
		}

		for (int i=0;i<3;i++) {

			if (!isInteger(sub.getElement(i,3))) {
				return false;
			}
		}

		return true;
	}

	public static boolean isInteger(double x) {
		// note that (x%1)==0 would not work, see test TestModuloIssues
		return Math.abs(Math.round(x)-x) < 0.0001;
	}

	/**
	 * For any 2 vertices in the graph that contain 2 or more edges with the same 
	 * interface id, remove all but first edges. 
	 */
	public void removeDuplicateEdges() {

		Set<E> toRemove = new HashSet<E>();

		int i = -1;
		for (V iVertex:graph.vertexSet()) {
			i++;
			int j = -1;
			for (V jVertex:graph.vertexSet()) {
				j++;
				if (j<i) continue; // i.e. we include i==j (to remove loop edges)

				Set<E> edges = graph.getAllEdges(iVertex, jVertex);
				Map<Integer,Set<E>> groups = groupIntoTypes(edges);

				for (int interfaceId:groups.keySet()){
					Set<E> group = groups.get(interfaceId);

					if (group.size()==0) {
						continue;
					} else if (group.size()==1) {
						continue;
					} else if (group.size()>2) {
						// we warn for more than 2 edges, that should not occur
						logger.warn("More than 2 edges with interface id {} between vertices {},{}",
								interfaceId,iVertex.toString(),jVertex.toString()); 
					}
					// now we are in case 2 or more edges 
					// we keep first and remove the rest
					Iterator<E> it = group.iterator();
					it.next(); // first edge: we keep it
					while (it.hasNext()) {						
						E edge = it.next();
						toRemove.add(edge);
						logger.info("Removed edge with interface id {} between vertices {},{} ", 
								interfaceId,iVertex.toString(),jVertex.toString());
					}

				}


			}

		}
		// now we do the removal
		for (E edge:toRemove) {
			graph.removeEdge(edge);
		}

	}

	/**
	 * Given a set of edges groups them into interface id groups
	 * @param edges
	 * @return a map of interface ids to sets of edges with the corresponding interface id
	 */
	private Map<Integer,Set<E>> groupIntoTypes(Set<E> edges) {
		Map<Integer,Set<E>> map = new HashMap<Integer,Set<E>>();

		for (E edge:edges) {
			Set<E> set = null;
			if (!map.containsKey(edge.getInterfaceId())) {
				set = new HashSet<E>();
				map.put(edge.getInterfaceId(), set);
			} else {
				set = map.get(edge.getInterfaceId());
			}
			set.add(edge);

		}
		return map;
	}

	/**
	 * @return True if the centroid of the whole structure will be used,
	 *  or false if the chain centroids should be used individually
	 */
	public boolean isGlobalReferencePoint() {
		return globalReferencePoint;
	}
	/**
	 * Set the behavior for choosing a reference point which will stay in the unit
	 * cell upon any of the transformations returned by
	 * {@link #getUnitCellTransformationsOrthonormal(String)}
	 * @param globalReferencePoint True if the centroid of the whole structure will be used,
	 *  or false if the chain centroids should be used individually
	 */
	public void setGlobalReferencePoint(boolean globalReferencePoint) {
		if(this.globalReferencePoint != globalReferencePoint) {
			referencePoints = new HashMap<String, Point3d>();
			unitCellOperators = new HashMap<String, Matrix4d[]>();
			this.globalReferencePoint = globalReferencePoint;
		}
	}

}
