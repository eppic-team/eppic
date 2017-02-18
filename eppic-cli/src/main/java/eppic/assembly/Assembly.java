package eppic.assembly;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.io.FileConvert;
import org.biojava.nbio.structure.io.mmcif.MMCIFFileTools;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifParser;
import org.biojava.nbio.structure.io.mmcif.model.AtomSite;
import org.biojava.nbio.structure.symmetry.core.AxisAligner;
import org.biojava.nbio.structure.symmetry.core.QuatSymmetryDetector;
import org.biojava.nbio.structure.symmetry.core.QuatSymmetryParameters;
import org.biojava.nbio.structure.symmetry.core.QuatSymmetryResults;
import org.biojava.nbio.structure.symmetry.core.Rotation;
import org.biojava.nbio.structure.symmetry.core.RotationGroup;
import org.biojava.nbio.structure.symmetry.core.Subunits;
import org.biojava.nbio.structure.xtal.CrystalCell;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.CallType;
import eppic.InterfaceEvolContextList;
import eppic.commons.util.GeomTools;
import eppic.predictors.InterfaceTypePredictor;

/**
 * An Assembly of molecules within a crystal, represented by a set of engaged interface clusters.
 * 
 * Each Assembly contains 1 (and only 1) AssemblyGraph which represents the subgraph of the lattice graph
 * corresponding to the set of engaged interface clusters.
 * 
 * The data structure hierarchy is:
 * <pre>
 *   Assembly - AssemblyGraph -< SubAssembly
 * </pre>
 * 
 * @author Jose Duarte
 * @author Aleix Lafita
 *
 */
public class Assembly {
	
	private static final Logger logger = LoggerFactory.getLogger(Assembly.class);
	
	/**
	 * Maximum number of engaged interfaces to consider in the scoring calculation.
	 * If more interfaces are engaged, then a warning is thrown and the smaller
	 * (lowest probability of biological) interfaces are removed from the 
	 * calculation, in order to loose as little probability density as possible.
	 */
	private static final int MAX_NUM_ENGAGED_IFACES_SCORING = 10;
	
	/**
	 * A numerical identifier for the assembly, from 1 to n
	 */
	private int id;
	
	/**
	 * The set of engaged interface clusters, represented as a boolean vector.
	 */
	private PowerSet engagedSet;
	
	/**
	 * The parent object containing references to all other assemblies and to the original structure
	 */
	private CrystalAssemblies crystalAssemblies;
	
	/**
	 * The AssemblyGraph object containing the subgraph and its connected components
	 */
	private AssemblyGraph assemblyGraph;
	
	/** 
	 * Probability of this assembly being the biologically relevant one.
	 */
	private double probability;
	
	/**
	 * Confidence of the assembly call, as a value between 0 and 1.
	 */
	private double confidence;
	
	private String callReason;
	private CallType call;
	
	
	public Assembly(CrystalAssemblies crystalAssemblies, PowerSet engagedSet) {
		this.crystalAssemblies = crystalAssemblies;
		this.engagedSet = engagedSet;
				
		assemblyGraph = new AssemblyGraph(this);
		
	}
	
	public PowerSet getEngagedSet() {
		return engagedSet;
	}
	
	public AssemblyGraph getAssemblyGraph() {
		return assemblyGraph;
	}
	
	public int getNumEngagedInterfaceClusters() {
		int count=0;
		for (int i=0;i<engagedSet.size();i++) {
			if (engagedSet.isOn(i)) count++;
		}
		return count;
	}
	
	/**
	 * Get the parent CrystalAssemblies object containing references to all other Assemblies and to the original Structure
	 * @return
	 */
	public CrystalAssemblies getCrystalAssemblies() {
		return crystalAssemblies;
	}
	
	/**
	 * Returns true if this assembly is a child of any of the given parents, false otherwise
	 * @param parents
	 * @return
	 */
	public boolean isChild(List<Assembly> parents) {

		for (Assembly invalidGroup:parents) {
			if (this.isChild(invalidGroup)) return true;
		}
		return false;
	}
	
	/**
	 * Returns true if this assembly is child of the given potentialParent
	 * 
	 * @param potentialParent
	 * @return true if is a child false if not
	 */
	public boolean isChild(Assembly potentialParent) {
		
		return this.engagedSet.isChild(potentialParent.engagedSet);
		
	}

	/**
	 * Gets all children in assembly tree of this assembly, not adding those that are also
	 * children of other invalidParents
	 * @param invalidParents
	 * @return
	 */
	public List<Assembly> getChildren(List<Assembly> invalidParents) {

		List<PowerSet> powersetInvalidParents = new ArrayList<PowerSet>();
		for (Assembly ia:invalidParents) {
			powersetInvalidParents.add(ia.engagedSet);
		}
		
		List<PowerSet> powersetChildren = this.engagedSet.getChildren(powersetInvalidParents);
		
		List<Assembly> children = new ArrayList<Assembly>();		
		
		for (PowerSet c:powersetChildren) {
			children.add(new Assembly(crystalAssemblies, c));
		}
		
		return children;
	}



	/**
	 * Returns true if this Assembly (i.e. this set of engaged interface clusters)
	 * constitutes a valid assembly topologically and isomorphically (rules iii and iv)
	 * @return
	 */
	public boolean isValid() {
		
		if (!isIsomorphic()) {
			logger.debug("Assembly {} contains non-isomorphic subgraphs, discarding it",this.toString());
			return false;
		}
		
		return isClosedSymmetry();
	}
	
	
	/**
	 * Checks whether this Assembly has closed symmetry, i.e. that all cycles in graph are closed.
	 * The implementation goes through a few shortcuts:
	 * <li>
	 * if Assembly contains infinite interfaces return false
	 * </li>
	 * <li>
	 * if Assembly contains just 1 isologous interface returns true
	 * </li>
	 * <li>
	 * if Assembly is heteromeric and stoichiometry is uneven returns false
	 * </li>
	 * <li>
	 * if Assembly is not automorphic (i.e. all vertices of a kind must have the same kind and number of interface clusters) returns false
	 * </li>
	 * <li>
	 * finally checks that all cycles are closed by enumerating cycles and checking the translations add up to 0
	 * </li>
	 * @return
	 */
	public boolean isClosedSymmetry() {
		
		// first we check for infinites, like that we save to compute the graph cycles for infinite cases
		if (assemblyGraph.containsInfinites()) {
			logger.debug("Discarding assembly {} because it contains infinite interfaces", toString());
			return false;
		}

		// pre-check for assemblies with 1 engaged interface that is isologous: the cycle detection doesn't work for isologous
		if (GraphUtils.getNumDistinctInterfaces(assemblyGraph.getSubgraph())==1) { 
			if (assemblyGraph.containsIsologous()) {
				logger.debug("Assembly {} contains just 1 isologous interface cluster: closed symmetry, won't check cycles",toString());
				return true;
			}
			
		}
		
		// for heteromeric assemblies, uneven stoichiometries implies non-closed. We can discard uneven ones straight away
		if (!assemblyGraph.isStoichiometryEven()) {
			logger.debug("Uneven stoichiometry for assembly {}, can't be a closed symmetry. Discarding",toString());
			return false;
		}
		
		// graph automorphism is a necessary (but not sufficient) condition: all vertices of a certain entity must have the same kind and number of interfaces (interface cluster ids)
		if (!assemblyGraph.isAutomorphic()) {
			return false;
		}
	
		return assemblyGraph.areAllCyclesClosed();
	}	
	
	/**
	 * Checks whether this Assembly is isomorphic, that is if all the connected components of its subgraph
	 * are isomorphic graphs in terms of vertex labels (i.e. they have the same stoichiometries) and edge labels
	 * @return
	 */
	public boolean isIsomorphic() {
		
		
		// 1) Isomorphism of entities: they have to be all equals or if different then they must be orthogonal 
		
		if (!assemblyGraph.isEntityIsomorphic()) {
			logger.debug("Some stoichiometries of assembly {} are overlapping, assembly can't be isomorphic",this.toString());
			return false;
		}
		
		// 2) Isomorphic in edge types: the count of edges per interface cluster type should be the same for all connected components
		
		// TODO implement
		
		//List<int[]> edgeStoichiometries = new ArrayList<int[]>();
		//for (Set<ChainVertex> connectedComponents) {
		//	int[] edgeStoichiometry = new int[engagedSet.length];
		//	edgeStoichiometries.add(edgeStoichiometry);
		//	for (InterfaceEdge edge:cc.edgeSet()) {
		//		edgeStoichiometry[edge.getClusterId()-1]++;
		//	}
		//}
		
		
		// 3) Isomorphic in connectivity
		// TODO implement isomorphism check of graph connectivity
		
		return true;
	}
	
	/**
	 * Returns the description corresponding to this Assembly as a list 
	 * of AssemblyDescriptions per disjoint set,
	 * e.g. in a crystal with 2 entities A,B and no engaged interfaces, 
	 * this would return a List of size 2:
	 * - AssemblyDescription 1: size 1, stoichiometry A, symmetry C1
	 * - AssemblyDescription 2: size 1, stoichiometry B, symmetry C1
	 * The same crystal where both A and B form a dimer would return a List of size 1
	 * with an AssemblyDescription: size 2, stoichiometry AB, symmetry C1
	 * @return
	 */
	public List<AssemblyDescription> getDescription() {
		List<AssemblyDescription> list = assemblyGraph.getDescription();
		StringBuilder sb = new StringBuilder();
		int i = -1;
		for (AssemblyDescription ad:list) {
			i++;
			sb.append(ad.getSize()+"/"+ad.getStoichiometry()+"/"+ad.getSymmetry());
			if (i!=list.size()-1) sb.append(",");
		}
		logger.info("Assembly {} size/stoichometry/symmetry: {}",toString(),sb.toString()); 
		return list;
	}
	
	/**
	 * Gets the coordinates corresponding to this Assembly.
	 * The Assembly must be a valid assembly, checked with {@link #isValid()}
	 * The output assembly will have chain identifiers like the original chains
	 * before applying symmetry operators. Thus in general a PDB file written straight from 
	 * the output will not be read correctly by other software since it will contain
	 * duplicate chain identifiers.
	 * @return
	 * @throws StructureException 
	 */
	public List<ChainVertex> getStructure() throws StructureException {
		// Place chains within unit cell
		List<ChainVertex> chains = new ArrayList<ChainVertex>();

		LatticeGraph<ChainVertex, InterfaceEdge> latticeGraph = crystalAssemblies.getLatticeGraph();
		CrystalCell cell = LatticeGraph.getCrystalCell(crystalAssemblies.getStructure());

		for(List<SubAssembly> subgroup : assemblyGraph.getSubAssembliesGroupedByStoichiometries()) {
			UndirectedGraph<ChainVertex, InterfaceEdge> cc = subgroup.get(0).getConnectedGraph();

			Map<ChainVertex, Point3i> placements = positionVertices(cc);
			transformChains(placements, crystalAssemblies.getStructure(),latticeGraph, cell, chains);
		}
		return chains;
	}

	/**
	 * For each complex in the assembly, transform it so that the complex is
	 * centered at the origin and aligned towards the z axis.
	 * 
	 * Note that disjoint complexes can overlap in 3D space. Use {@link #getStructurePacked()}
	 * to avoid this.
	 * @return For each connected component in the assembly, give a list of
	 *  vertices with transformed chains.
	 * @throws StructureException
	 */
	public List<List<ChainVertex>> getStructureCentered() throws StructureException {

		LatticeGraph<ChainVertex, InterfaceEdge> latticeGraph = crystalAssemblies.getLatticeGraph();
		CrystalCell cell = LatticeGraph.getCrystalCell(crystalAssemblies.getStructure());

		List<List<ChainVertex>> components = new ArrayList<List<ChainVertex>>(assemblyGraph.getSubAssembliesGroupedByStoichiometries().size());

		for(List<SubAssembly> subgroup : assemblyGraph.getSubAssembliesGroupedByStoichiometries()) {
			UndirectedGraph<ChainVertex, InterfaceEdge> cc = subgroup.get(0).getConnectedGraph();

			// Position connected component to avoid wrapping
			Map<ChainVertex, Point3i> placements = positionVertices(cc);
			List<ChainVertex> chains = transformChains(placements, crystalAssemblies.getStructure(),latticeGraph, cell, null);

			centerSymmetrically(cc, chains);

			components.add(chains);
		}

		return components;
	}

	/**
	 * Generate the 2D-packed structure for this assembly.
	 * 
	 * For each complex in the assembly, gather the subunits into a closed conformation,
	 * align them in the XY plane with the major axis along Z, and pack multiple
	 * complexes to avoid overlaps
	 * @return A list of all vertices, with their Chain objects transformed to
	 *  the correct 3D positions
	 * @throws StructureException
	 */
	public List<ChainVertex> getStructurePacked() throws StructureException {


		LatticeGraph<ChainVertex, InterfaceEdge> latticeGraph = crystalAssemblies.getLatticeGraph();
		CrystalCell cell = LatticeGraph.getCrystalCell(crystalAssemblies.getStructure());

		List<Entry<Dimension2D, List<ChainVertex>>> boxes = new ArrayList<Map.Entry<Dimension2D,List<ChainVertex>>>();

		for(List<SubAssembly> subgroup : assemblyGraph.getSubAssembliesGroupedByStoichiometries()) {
			UndirectedGraph<ChainVertex, InterfaceEdge> cc = subgroup.get(0).getConnectedGraph();

			// Position connected component to avoid wrapping
			Map<ChainVertex, Point3i> placements = positionVertices(cc);
			List<ChainVertex> chains = transformChains(placements, crystalAssemblies.getStructure(),latticeGraph, cell, null);

			// Center at origin
			Vector3d dim = centerSymmetrically(cc, chains);

			// pad space around the protein and make it an even multiple
			final double padding = 10;
			int x = (int)(Math.ceil( dim.x * 2./padding + 1 )*padding);
			int y = (int)(Math.ceil( dim.y * 2./padding + 1 )*padding);
			Dimension2D dim2 = new Dimension(x,y);
			boxes.add(new SimpleEntry<Dimension2D,List<ChainVertex>>(dim2,chains));
		}

		// Pack complexes in XY plane
		BinaryBinPacker<List<ChainVertex>> packer = new BinaryBinPacker<List<ChainVertex>>(boxes);
		List<Entry<List<ChainVertex>, Rectangle2D>> placements = packer.getPlacements();
		Rectangle2D container = packer.getBounds();

		List<ChainVertex> allchains = new ArrayList<ChainVertex>();
		for(Entry<List<ChainVertex>, Rectangle2D> entry : placements) {
			List<ChainVertex> chains = entry.getKey();

			// Center proteins in each box; center container at origin
			Rectangle2D place = entry.getValue();
			double x = place.getX() + place.getWidth()/2. - container.getWidth()/2.;
			double y = place.getY() + place.getHeight()/2. - container.getHeight()/2.;
			Vector3d center = new Vector3d(x,y,0);

			// Transform to XY location
			for(ChainVertex chain : chains) {
				Calc.translate(chain.getChain(), center);
			}
			allchains.addAll(chains);
		}
		return allchains;
	}

	/**
	 * Takes a graph representing a single complex. The vertex's chains should
	 * be pre-transformed so that no edges wrap around the unit cell (i.e. with
	 * {@link #transformChains(Map, Structure, LatticeGraph, CrystalCell, List)}).
	 * 
	 * This method further transforms the chains of the vertices so that the
	 * complex is centered at the origin and aligned the major symmetry axis.
	 * @param cc
	 * @param chains
	 * @return The extent of the bounding box for the complex (i.e. half the
	 *  dimensions of the bounding polyhedron).
	 */
	private Vector3d centerSymmetrically(
			UndirectedGraph<ChainVertex, InterfaceEdge> cc,
			List<ChainVertex> chains) {
		// Transform to be centered with the major axis vertically
		QuatSymmetryResults symm = getQuatSymm(chains);
		RotationGroup pointgroup = symm.getRotationGroup();
		AxisAligner aligner = AxisAligner.getInstance(symm);

		Matrix4d transformation;
		if(pointgroup.getOrder() < 1) {
			// Failed?
			logger.warn("Error finding point group for complex containing {}",cc.vertexSet().iterator().next());
			
			// Find centroid
			List<Chain> chainList = new ArrayList<Chain>(cc.vertexSet().size());
			for(ChainVertex vert : cc.vertexSet()) {
				chainList.add(vert.getChain());
			}
			Vector3d centroid = new Vector3d(GeomTools.getCentroid(chainList));

			// Translate centroid to origin
			centroid.negate();
			transformation = GeomTools.getIdentityMatrix();
			transformation.setTranslation(centroid);
		} else {
			// Find major symmetry axes
			SortedMap<Integer, Vector3d> sortedAxes = new TreeMap<Integer, Vector3d>();

			//TODO select axes more intelligently
			for(int i=0; i<pointgroup.getOrder(); i++) {
				Rotation rot = pointgroup.getRotation( i );
				int fold = rot.getFold();
				if(fold < 2)
					continue; //skip identity
				AxisAngle4d axis = rot.getAxisAngle();
				Vector3d curr = new Vector3d(axis.x,axis.y,axis.z);
				sortedAxes.put(-fold, curr);
			}
			Vector3d normal, otheraxis;
			if(sortedAxes.isEmpty()) {
				normal = new Vector3d(0,0,1);
				otheraxis = null;
			} else {
				Integer largest = sortedAxes.firstKey();
				normal = sortedAxes.get(largest);
				normal.normalize();
				otheraxis = null;
				for(Vector3d o : sortedAxes.values()) {
					o.normalize();
					if( Math.abs(o.dot(normal)) < 1 - 1e-6 ) {
						otheraxis = o;
						break;
					}
				}
			}

//				// align z-axis to highest-order group
//				int highOrderAxis = pointgroup.getHigherOrderRotationAxis();
//				
//				Vector3d normal = n
//				
//				// find another axis
//				int principalAxis = pointgroup.getPrincipalAxisIndex();
//				if(principalAxis != highOrderAxis) {
//					Rotation principalRot = pointgroup.getRotation(principalAxis);
//					AxisAngle4d pAxis = principalRot.getAxisAngle();
//					otheraxis = new Vector3d(pAxis.x,pAxis.y,pAxis.z);
//				}
//				if( otheraxis == null && pointgroup.getOrder() > 0) {
//					// Choose any other axis
//					Rotation principalRot = pointgroup.getRotation(highOrderAxis!=1 ? 1:2);
//					AxisAngle4d pAxis = principalRot.getAxisAngle();
//					otheraxis = new Vector3d(pAxis.x,pAxis.y,pAxis.z);
//				}


			Point3d center = aligner.getGeometricCenter();
			transformation = GeomTools.matrixFromPlane(center, normal, otheraxis);
			transformation.invert();
		}

		// Transform chains to the origin
		for(ChainVertex vert:chains) {
			Calc.transform(vert.getChain(), transformation);
		}
		//TODO is this really half the bounding box?
		return aligner.getDimension();
	}

	/**
	 * Calculate point group symmetry for a complex
	 * @param vertices List of vertices. Constituent chains should be pre-transformed into an orientation with closed symmetry
	 * @return
	 */
	private static QuatSymmetryResults getQuatSymm( Collection<ChainVertex> vertices) {
		// hack subunits
		List<Point3d[]> caCoords = new ArrayList<Point3d[]>();
		List<Integer> folds = new ArrayList<Integer>();
		List<Boolean> pseudo = new ArrayList<Boolean>();
		List<String> chainIds = new ArrayList<String>();
		List<Integer> models = new ArrayList<Integer>();
		List<Double> seqIDmin = new ArrayList<Double>();
		List<Double> seqIDmax = new ArrayList<Double>();
		List<Integer> clusterIDs = new ArrayList<Integer>();
		int fold = 1;
		Character chain = 'A';

		for (ChainVertex vert : vertices ){
			Point3d[] coords = getDummyCoordinates(vert.getChain());
			if (coords.length==0) {
				logger.warn("0-length coordinate array. Can't calculate quaternary symmetry!");
			}
			caCoords.add(coords);

			if (vertices.size() % fold == 0){
				folds.add(fold); //the folds are the common denominators
			}
			fold++;
			pseudo.add(false);
			chainIds.add(chain+"");
			chain++;
			models.add(0);
			seqIDmax.add(1.0);
			seqIDmin.add(1.0);
			clusterIDs.add(0);
		}

		//Create directly the subunits, because we know the aligned CA
		Subunits globalSubunits = new Subunits(caCoords, clusterIDs, 
				pseudo, seqIDmin, seqIDmax, 
				folds, chainIds, models);

		//Quaternary Symmetry Detection
		QuatSymmetryParameters param = new QuatSymmetryParameters();

		QuatSymmetryResults gSymmetry = 
				QuatSymmetryDetector.calcQuatSymmetry(globalSubunits, param);

		return gSymmetry;
	}
	
	/**
	 * Get some dummy coordinates for a chain. For computational efficiency,
	 * we represent the subunit by just a couple points. The points returned
	 * should be robust to minor subunit differences.
	 * @param c
	 * @return
	 */
	private static Point3d[] getDummyCoordinates(Chain c) {
		// Using the centroid gave poor quality since it doesn't establish the orientation.

		// Use the centroids of each third of the protein
		Atom[] ca = StructureTools.getRepresentativeAtomArray(c);
		if (ca.length==0) {
			// in some cases we find no CAs or Ps, let's use all atoms then, see issue #167
			ca = StructureTools.getAllAtomArray(c);
		}
		
		if(ca.length<3) {
			return Calc.atomsToPoints(ca);
		}
		Atom[] ca1 = Arrays.copyOfRange(ca, 0,ca.length/3);
		Atom[] ca2 = Arrays.copyOfRange(ca, ca.length/3,2*ca.length/3);
		Atom[] ca3 = Arrays.copyOfRange(ca, 2*ca.length/3,ca.length);
		Atom[] dummy = new Atom[3];
		dummy[0] = Calc.getCentroid(ca1);
		dummy[1] = Calc.getCentroid(ca2);
		dummy[2] = Calc.getCentroid(ca3);
		return Calc.atomsToPoints(dummy);
	}

	/**
	 * Takes a map of chain position from {@link #positionVertices(UndirectedGraph)}
	 * and create a new set of ChainVertex objects with transformed chains.
	 * The original ChainVertexes are not modified, but rather cloned.
	 * 
	 * @param placements Placement of each chain relative to the 0,0,0 unit cell
	 * @param latticeGraph Root graph, for calculation of the starting chain positions
	 * @param cell Unit cell
	 * @param chains (Optional) Output list to insert transformed chains into
	 * @return chains with appended elements, or a new list of transformed Chain objects if chains was null
	 * @throws StructureException
	 */
	private static List<ChainVertex> transformChains(Map<ChainVertex, Point3i> placements,
			Structure structure, LatticeGraph<ChainVertex, InterfaceEdge> latticeGraph,
			CrystalCell cell, List<ChainVertex> chains)
			throws StructureException
	{
		if( chains == null) {
			chains = new ArrayList<ChainVertex>();
		}
		for(Entry<ChainVertex, Point3i> entry : placements.entrySet()) {
			ChainVertex v = entry.getKey();

			// transformation to 0,0,0 cell
			Matrix4d m = latticeGraph.getUnitCellTransformationOrthonormal(v.getChainId(), v.getOpId());

			// add translation
			Point3i placement = entry.getValue();
			Vector3d trans = new Vector3d(placement.x,placement.y,placement.z);
			cell.transfToOrthonormal(trans);
			Matrix4d transmat = new Matrix4d();
			transmat.set(1., trans);
			transmat.mul(m);

			Chain chain = (Chain) structure.getChainByPDB(v.getChainId()).clone();
			Calc.transform(chain, transmat);
			chains.add(new ChainVertex(chain,v.getOpId()));
		}
		return chains;
	}
	
	@SuppressWarnings("unused")
	private static void transformChainsInPlace(Map<ChainVertex, Point3i> placements,
			Structure structure, LatticeGraph<ChainVertex, InterfaceEdge> latticeGraph,
			CrystalCell cell)
					throws StructureException
	{
		for(Entry<ChainVertex, Point3i> entry : placements.entrySet()) {
			ChainVertex v = entry.getKey();

			// transformation to 0,0,0 cell
			Matrix4d m = latticeGraph.getUnitCellTransformationOrthonormal(v.getChainId(), v.getOpId());

			// add translation
			Point3i placement = entry.getValue();
			Vector3d trans = new Vector3d(placement.x,placement.y,placement.z);
			cell.transfToOrthonormal(trans);
			Matrix4d transmat = new Matrix4d();
			transmat.set(1., trans);
			transmat.mul(m);

			Calc.transform(v.getChain(), transmat);
		}
	}

	/**
	 * Given a valid lattice graph (i.e. one where there are no infinite assemblies), 
	 * this method chooses a unit cell for each vertex such that no edges wrap
	 * around to the other side.
	 * 
	 * <p>To specify the positions of particular vertices manually, use
	 * {@link #positionVertices(UndirectedGraph, List, List)}
	 * @param graph
	 * @return
	 * @see #positionVertices(UndirectedGraph, List, List)
	 */
	public static <V extends ChainVertex, E extends InterfaceEdge>
	Map<V, Point3i> positionVertices(final UndirectedGraph<V,E> graph) {
		return positionVertices(graph,null,null);
	}
	/**
	 * Given a valid lattice graph (i.e. one where there are no infinite assemblies), 
	 * this method chooses a unit cell for each vertex such that no edges wrap
	 * around to the other side.
	 * 
	 * <p>If desired, the unit cell for one vertex in each connected component
	 * can be specified. The position for this vertex will be fixed, with connected
	 * vertices positioned around it to prevent wrapping.
	 * 
	 * <p>For graphs which do contain an infinite assembly, some edges must always
	 * wrap but this function will generally assign positions to reduce the
	 * number. Specifically, the algorithm positions the reference (if any), then
	 * the neighbors, then vertices at distance 2, and so forth.
	 * 
	 * 
	 * @param graph An undirected graph of one or more components
	 * @param refVertexes (Optional) A list of ChainVertexes to use as reference
	 *  positions, or null to choose arbitrary references in the unit cell.
	 * @param refCells (Optional) A list giving the unit cell positions for each
	 *  reference vertex, or null if all reference vertices should be in cell
	 *  (0,0,0). Ignored if refVertexes is omitted.
	 * @return A map between vertices and unit cell positions
	 */
	public static <V extends ChainVertex, E extends InterfaceEdge>
	Map<V, Point3i> positionVertices(final UndirectedGraph<V,E> graph, final List<V> refVertexes, final List<Point3i> refCells) {
		// Mark unplaced vertices
		final Set<V> unprocessed = new HashSet<V>(graph.vertexSet());
		// Location of placed vertices
		final Map<V,Point3i> placements = new HashMap<V, Point3i>(graph.vertexSet().size());

		// Set up iterators for the reference cells
		if( refVertexes != null ) {
			if( refCells != null && refVertexes.size() != refCells.size() ) {
				throw new IllegalArgumentException("reference arguments must have the same length");
			}
		}
		// refVertexes defaults to empty list
		Iterator<V> refVertexIt;
		if(refVertexes == null) {
			refVertexIt = Collections.emptyListIterator();
		} else {
			refVertexIt = refVertexes.iterator();
		}
		// refCells defaults to (0,0,0) point
		Iterator<Point3i> refCellsIt;
		if(refCells == null) {
			refCellsIt = new Iterator<Point3i>() {
				@Override
				public boolean hasNext() {
					return true;
				}
				@Override
				public Point3i next() {
					return new Point3i();
				}
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		} else {
			refCellsIt = refCells.iterator();
		}

		while(!unprocessed.isEmpty()) {
			// Find a valid reference vertex
			V root = null;
			Point3i rootCell = null;
			// First get references from input
			while(root == null && refVertexIt.hasNext()) {
				V vert = refVertexIt.next();
				Point3i cell = refCellsIt.next();
				if( unprocessed.contains(root)) {
					root = vert;
					rootCell = cell;
				}
			}
			// Fall back to any arbitrary vertex
			if(root == null) {
				root = unprocessed.iterator().next();
				rootCell = new Point3i();
			}

			// Traverse connected component
			placements.put(root, rootCell);
			unprocessed.remove(root);
			logger.debug("Placing {} at {}",root,rootCell);
			BreadthFirstIterator<V,E> it = new BreadthFirstIterator<V,E>(graph,root);
			it.addTraversalListener(new TraversalListener<V, E>() {
				@Override
				public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {}
				@Override
				public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {}

				@Override
				public void edgeTraversed(EdgeTraversalEvent<E> event) {
					// TODO Auto-generated method stub
					E edge = event.getEdge();
					// Undirected edge, so source and target may be swapped
					V s = graph.getEdgeSource(edge);
					V t = graph.getEdgeTarget(edge);

					if( placements.containsKey(s) ) {
						// forward edge
						if( placements.containsKey(t) ) {
							logger.trace("Revisiting {} via edge {} from {}",t,edge,s);
							return;
						}
						Point3i sPlacement = placements.get(s);
						Point3i xtalTrans = edge.getXtalTrans();
						Point3i tPlacement = new Point3i(sPlacement);
						tPlacement.add(xtalTrans);
						placements.put(t,tPlacement);
						unprocessed.remove(t);
						logger.debug("Placing {} at {}",t,tPlacement);
					} else if( placements.containsKey(t)){
						Point3i tPlacement = placements.get(t);
						Point3i xtalTrans = edge.getXtalTrans();
						Point3i sPlacement = new Point3i(tPlacement);
						sPlacement.sub(xtalTrans);
						placements.put(s, sPlacement);
						unprocessed.remove(s);
						logger.debug("Placing {} at {}",s,sPlacement);
					} else {
						// Forbidden by BFS contract
						logger.error("Traversed {} from {} to {}, but neither is positioned yet.",edge,s,t);
						return;
					}
				}
				@Override
				public void vertexTraversed(VertexTraversalEvent<V> e) {}
				@Override
				public void vertexFinished(VertexTraversalEvent<V> e) {}
			});
			while(it.hasNext()) {
				// Traversal Listener populates placements
				it.next();
			}
		}

		assert(placements.size() == graph.vertexSet().size());

		return placements;
	}
	
	/**
	 * Writes this Assembly to PDB file (gzipped) with a model per chain.
	 * @param file
	 * @throws StructureException
	 * @throws IOException
	 */
	public void writeToPdbFile(File file) throws StructureException, IOException {
		PrintStream ps = new PrintStream(new GZIPOutputStream(new FileOutputStream(file)));
		int modelId = 1;
		for (ChainVertex cv:getStructurePacked()) {
			ps.println("MODEL"+String.format("%9d",modelId));
			ps.print(cv.getChain().toPDB());
			ps.println("TER");
			ps.println("ENDMDL");
			modelId++;
		}
		ps.println("END");
		ps.close();
	}
	
	/**
	 * Writes this Assembly to mmCIF file (gzipped) with chain ids as follows:
	 *  <li> author_ids: chainId_operatorId</li>
	 *  <li> asym_ids: chainId_operatorId</li>
	 * The atom ids will be renumbered if the Assembly contains symmetry-related molecules,
	 * otherwise some molecular viewers (e.g. 3Dmol.js) won't be able to read the atoms
	 * as distinct.
	 * Note that PyMOL supports multi-letter chain ids only from 1.7.4
	 * @param file
	 * @throws IOException
	 * @throws StructureException
	 */
	public void writeToMmCifFile(File file) throws IOException, StructureException {

		// Some molecular viewers like 3Dmol.js need globally unique atom identifiers (across chains)
		// With the approach below we add an offset to atom ids of sym-related molecules to avoid repeating atom ids

		// we only do renumbering in the case that there are sym-related chains in the assembly
		// that way we stay as close to the original as possible
		boolean symRelatedChainsExist = false;
		List<ChainVertex> structure = getStructurePacked();
		int numChains = structure.size();
		Set<String> uniqueChains = new HashSet<String>();
		for (ChainVertex cv:structure) {
			uniqueChains.add(cv.getChain().getChainID());
		}
		if (numChains != uniqueChains.size()) symRelatedChainsExist = true;


		PrintStream ps = new PrintStream(new GZIPOutputStream(new FileOutputStream(file)));

		ps.println(SimpleMMcifParser.MMCIF_TOP_HEADER+"eppic_assembly_"+getId());

		ps.print(FileConvert.getAtomSiteHeader());

		List<AtomSite> atomSites = new ArrayList<>();

		int atomId = 1;
		for (ChainVertex cv:structure) {
			String chainId = cv.getChain().getChainID()+"_"+cv.getOpId();

			for (Group g: cv.getChain().getAtomGroups()) {
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

		ps.print(MMCIFFileTools.toMMCIF(atomSites, AtomSite.class));


		ps.close();
	}
	
	/**
	 * Get this Assembly's identifier
	 * @return
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Set this Assembly's identifier
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	public void setCall(CallType call) {
		this.call = call;
	}
	
	public CallType getCall() {
		return call;
	}
	
	public void setCallReason(String callReason) {
		this.callReason = callReason;
	}
	
	public String getCallReason() {
		return callReason;
	}
	
	public double getScore() {
		return probability;
	}
	
	public double getConfidence() {
		return confidence;
	}
	
	@Override
	public boolean equals(Object other) {
		if (! (other instanceof Assembly)) return false;
		
		Assembly o = (Assembly) other;
		
		return this.engagedSet.equals(o.engagedSet);
		
	}
	
	@Override
	public int hashCode() {
		return this.engagedSet.hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		SortedSet<Integer> interfClusterIds = GraphUtils.getDistinctInterfaceClusters(assemblyGraph.getSubgraph());
		int numClusters = interfClusterIds.size();
		
		sb.append("{");

		int i = 0;
		for (int interfClusterId : interfClusterIds) {

			sb.append(interfClusterId);
			
			if (i!=numClusters-1) sb.append(",");

			i++;
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Compute a probabilistic score from the individual interface
	 * probabilities.
	 */
	public void calcScore() {
		
		// Calculate the probabilities for each possible set of interface clusters
		probability = 0;
		InterfaceEvolContextList iecl = getCrystalAssemblies().getInterfaceEvolContextList();
		
		// Construct a list of all the smaller powersets of this assembly that are equivalent
		List<PowerSet> pss = new ArrayList<PowerSet>();
		
		PowerSet reducedSet = new PowerSet(engagedSet);
		
		// If the number of engaged interfaces is high, warn and disengage
		if (reducedSet.sizeOn() > MAX_NUM_ENGAGED_IFACES_SCORING) {
			logger.warn("There are {} engaged interface clusters in assembly {}. "
					+ "They will be reduced to compute assembly scoring.", 
					reducedSet.sizeOn(), id);
			
			while (reducedSet.sizeOn() > MAX_NUM_ENGAGED_IFACES_SCORING) {
				
				// Find the lowest probability cluster
				int index = 0;
				double probability = 1;
				for (int i = 1; i < reducedSet.size() + 1; i++) {
					double p = iecl.getCombinedClusterPredictor(i).getScore();
					if (p <= probability) {
						index = i - 1;
						probability = p;
					}
				}
				logger.info("Disengaging interface cluster {} for assembly {} scoring",
						index + 1, id);
				if (probability > 0.1) {
					logger.warn("Disengaging interface cluster {} of assembly {} "
							+ "scoring, with probability {} of being biologically "
							+ "relevant. Significant probability density might be "
							+ "missing for the score of this assembly.", index + 1,
							id, String.format("%.2f", probability));
				}
				
				reducedSet.switchOff(index);
			}
		}
		
		for (PowerSet ps : reducedSet.getOnPowerSet(1)) {
			// Equivalent means that they have the same number of subassemblies
			Assembly pa = new Assembly(crystalAssemblies, ps);
			if (pa.getAssemblyGraph().getSubAssemblies().size() == 
					this.getAssemblyGraph().getSubAssemblies().size())
				pss.add(ps);
		}
		pss.add(reducedSet);
					
		for (PowerSet ps : pss) {
			double prob = 1;
			for (int i = 1; i < ps.size()+1; i++) {
				double p = iecl.getCombinedClusterPredictor(i).getScore();
				if (ps.isOff(i-1))
					p = (1-p);
				prob *= p;
			}
			probability += prob;
		}
	}
	
	/**
	 * Normalize the probabilistic score by the total sum of assembly
	 * probabilities. This is needed to account for the 0 probability
	 * of impossible interface combinations (assemblies).
	 * @param sumProbs total sum of assembly probabilities in the crystal.
	 */
	public void normalizeScore(double sumProbs) {
		probability = probability / sumProbs;
	}
	
	/**
	 * Calculate the confidence of the call.
	 */
	public void calcConfidence() {
		switch(call){
		case BIO:
			confidence = probability;
			callReason = "Highest probability assembly of being biologically relevant found in the crystal.";
			break;
		case CRYSTAL:
			confidence = 1 - probability;
			callReason = "Another assembly in the crystal has a higher probability of being biologically relevant.";
			break;
		case NO_PREDICTION:
			// should that be a global variable in eppic params?
			confidence = InterfaceTypePredictor.CONFIDENCE_UNASSIGNED;
			callReason = "There is not enough data to classify this assembly.";
		}
	}
	
	/**
	 * Sorts given list of interface clusters by descending areas
	 * @param list
	 */
	@SuppressWarnings("unused")
	private static void sortStructureInterfaceClusterList(List<StructureInterfaceCluster> list) {
		
		Collections.sort(list, new Comparator<StructureInterfaceCluster>() {

			@Override
			public int compare(StructureInterfaceCluster o1, StructureInterfaceCluster o2) {
				return Double.compare(o2.getTotalArea(), o1.getTotalArea());
			}
		});
	}
	

}
