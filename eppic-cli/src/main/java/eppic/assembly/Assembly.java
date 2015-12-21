package eppic.assembly;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Compound;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.contact.Pair;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceCluster;
import org.biojava.nbio.structure.io.FileConvert;
import org.biojava.nbio.structure.io.mmcif.MMCIFFileTools;
import org.biojava.nbio.structure.io.mmcif.SimpleMMcifParser;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.CallType;


/**
 * An Assembly of molecules within a crystal, represented by a set of engaged interface clusters.
 * 
 * Each Assembly contains 1 (and only 1) AssemblyGraph which represents the subgraph of the lattice graph
 * corresponding to the set of engaged interface clusters.
 * 
 * @author Jose Duarte
 *
 */
public class Assembly {
	
	private static final Logger logger = LoggerFactory.getLogger(Assembly.class);

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
	
	public List<StructureInterfaceCluster> getEngagedInterfaceClusters() {
		List<StructureInterfaceCluster> engagedInterfaceClusters = new ArrayList<StructureInterfaceCluster>();
		
		for (StructureInterfaceCluster cluster:crystalAssemblies.getInterfaceClusters()) {
			for (int i=0;i<engagedSet.size();i++) {
				if (engagedSet.isOn(i) && cluster.getId() == i+1) {
					engagedInterfaceClusters.add(cluster);
				}
			}
		}
		return engagedInterfaceClusters;
	}
	
	public List<StructureInterfaceCluster> getEngagedInterfaceClusters(Stoichiometry sto) {
		
		Set<Integer> entityIdsInSto = sto.getEntityIds();
		
		List<StructureInterfaceCluster> engagedInterfaceClusters = new ArrayList<StructureInterfaceCluster>();
		
		for (StructureInterfaceCluster cluster:getEngagedInterfaceClusters()) {
			StructureInterface interf = cluster.getMembers().get(0);
			if (isInterfaceInEntityIds(interf, entityIdsInSto)) {				
				engagedInterfaceClusters.add(cluster);
			}
		}
		return engagedInterfaceClusters;
	}
	
	public List<StructureInterfaceCluster> getHomoEngagedInterfaceClusters() {
		List<StructureInterfaceCluster> engagedInterfaceClusters = new ArrayList<StructureInterfaceCluster>();
		
		for (StructureInterfaceCluster cluster: getEngagedInterfaceClusters()) {
			if (cluster.getMembers().get(0).isHomomeric()) {
				engagedInterfaceClusters.add(cluster);
			}
		}
		return engagedInterfaceClusters;
	}
	
	public List<StructureInterfaceCluster> getHeteroEngagedInterfaceClusters() {
		List<StructureInterfaceCluster> engagedInterfaceClusters = new ArrayList<StructureInterfaceCluster>();
		
		for (StructureInterfaceCluster cluster: getEngagedInterfaceClusters()) {
			if (!cluster.getMembers().get(0).isHomomeric()) {
				engagedInterfaceClusters.add(cluster);
			}
		}
		return engagedInterfaceClusters;
	}
	
	public List<StructureInterfaceCluster> getHeteroEngagedInterfaceClusters(Stoichiometry sto) {
		
		Set<Integer> entityIdsInSto = sto.getEntityIds();
		
		List<StructureInterfaceCluster> engagedInterfaceClusters = new ArrayList<StructureInterfaceCluster>();
		
		for (StructureInterfaceCluster cluster: getEngagedInterfaceClusters()) {
			StructureInterface interf = cluster.getMembers().get(0);
			if (!interf.isHomomeric() && isInterfaceInEntityIds(interf, entityIdsInSto)) {
				engagedInterfaceClusters.add(cluster);
			}
		}
		return engagedInterfaceClusters;
	}
	
	public List<StructureInterfaceCluster> getHomoEngagedInterfaceClusters(Stoichiometry sto) {
		
		Set<Integer> entityIdsInSto = sto.getEntityIds();
		
		List<StructureInterfaceCluster> engagedInterfaceClusters = new ArrayList<StructureInterfaceCluster>();
		
		for (StructureInterfaceCluster cluster: getEngagedInterfaceClusters()) {
			StructureInterface interf = cluster.getMembers().get(0);
			if (interf.isHomomeric() && 
				isInterfaceInEntityIds(interf, entityIdsInSto) )  {
				
				engagedInterfaceClusters.add(cluster);
			}
		}
		return engagedInterfaceClusters;
	}
	
	private boolean isInterfaceInEntityIds(StructureInterface interf, Set<Integer> entityIds) {
		Pair<Compound> comps = interf.getParentCompounds();
		
		// in some rare cases the compounds are missing, we can't do much: return false
		if (comps.getFirst() == null || comps.getSecond() == null) return false;
		
		if (entityIds.contains(comps.getFirst().getMolId()) && 
			entityIds.contains(comps.getSecond().getMolId()) ) {
			return true;
		}
		return false;
	}
		
	public int getNumEngagedInterfaceClusters() {
		int count=0;
		for (int i=0;i<engagedSet.size();i++) {
			if (engagedSet.isOn(i)) count++;
		}
		return count;
	}
	
	public int getNumHomoEngagedInterfaceClusters() {
		int count=0;
		for (StructureInterfaceCluster cluster: getEngagedInterfaceClusters()) {
			if (cluster.getMembers().get(0).isHomomeric()) {
				count++;
			}
		}
		return count;
	}
	
	public int getNumHeteroEngagedInterfaceClusters() {
		int count=0;
		for (StructureInterfaceCluster cluster: getEngagedInterfaceClusters()) {
			if (!cluster.getMembers().get(0).isHomomeric()) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Get the number of engaged interface clusters that involve entities present in the given stoichiometry
	 * @param sto
	 * @return
	 */
	public int getNumEngagedInterfaceClusters(Stoichiometry sto) {
		int count = 0;
		Set<Integer> entityIdsInSto = sto.getEntityIds();
		for (StructureInterfaceCluster cluster: getEngagedInterfaceClusters()) {
			StructureInterface interf = cluster.getMembers().get(0);
			if (isInterfaceInEntityIds(interf, entityIdsInSto)  ) {
				// both sides are part of chains present in stoichiometry
				count++;
			}			
		}
		return count;
	}
	
	/**
	 * Get the number of homomeric engaged interface clusters that involve entities present in the given stoichiometry
	 * @param sto
	 * @return
	 */
	public int getNumHomoEngagedInterfaceClusters(Stoichiometry sto) {
		int count = 0;
		Set<Integer> entityIdsInSto = sto.getEntityIds();
		for (StructureInterfaceCluster cluster: getEngagedInterfaceClusters()) {
			StructureInterface interf = cluster.getMembers().get(0);				
			if (interf.isHomomeric() && 
				isInterfaceInEntityIds(interf, entityIdsInSto)   ) {
				// both sides are part of chains present in stoichiometry
				count++;
			}
			
		}
		return count;
	}
	
	/**
	 * Get the number of heteromeric engaged interface clusters that involve entities present in the given stoichiometry
	 * @param sto
	 * @return
	 */
	public int getNumHeteroEngagedInterfaceClusters(Stoichiometry sto) {
		int count = 0;
		Set<Integer> entityIdsInSto = sto.getEntityIds();
		for (StructureInterfaceCluster cluster: getEngagedInterfaceClusters()) {
			StructureInterface interf = cluster.getMembers().get(0);				
			if (!interf.isHomomeric() && 
				isInterfaceInEntityIds(interf, entityIdsInSto)   ) {
				// both sides are part of chains present in stoichiometry
				count++;
			}
			
		}
		return count;
	}
	
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
	
	private boolean containsInfinites() {
		for (StructureInterfaceCluster cluster: getEngagedInterfaceClusters()) {
			
			for (StructureInterface interf:cluster.getMembers()) {
				// if a single member of cluster is infinite we consider the cluster infinite
				if (interf.isInfinite()) return true;
			}
		}
		return false;
	}
	
	public boolean isClosedSymmetry() {
		
		// first we check for infinites, like that we save to compute the graph cycles for infinite cases
		if (containsInfinites()) {
			logger.debug("Discarding assembly {} because it contains infinite interfaces", toString());
			return false;
		}

		// pre-check for assemblies with 1 engaged interface that is isologous: the cycle detection doesn't work for isologous
		if (getNumEngagedInterfaceClusters()==1) {
			for (StructureInterface interf : getEngagedInterfaceClusters().get(0).getMembers()) {
				// with a single interface in cluster isologous, we call the whole isologous
				if (interf.isIsologous()) {
					logger.debug("Assembly {} contains just 1 isologous interface cluster: closed symmetry, won't check cycles",toString());
					return true;
				}
			}
			
		}
		
		// for heteromeric assemblies, uneven stoichiometries implies non-closed. We can discard uneven ones straight away
		if (!assemblyGraph.isStoichiometryEven()) {
			logger.debug("Uneven stoichiometry for assembly {}, can't be a closed symmetry. Discarding",toString());
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

		List<ChainVertex> chains = new ArrayList<ChainVertex>();
		
		// we assume this is a valid assembly
		// we get any of the isomorphic connected components, let's say the first one

		UndirectedGraph<ChainVertex, InterfaceEdge> firstCc = assemblyGraph.getFirstConnectedComponent();
		
		
		Iterator<ChainVertex> it = firstCc.vertexSet().iterator();
		
		ChainVertex refVertex = it.next();
		
		// transform refVertex, no translations for it
		Matrix4d m = crystalAssemblies.getLatticeGraph().getUnitCellTransformationOrthonormal(refVertex.getChainId(), refVertex.getOpId());
		Chain chain = (Chain) crystalAssemblies.getStructure().getChainByPDB(refVertex.getChainId()).clone();
		Calc.transform(chain, m);
		chains.add(new ChainVertex(chain, refVertex.getOpId()));
		
		while (it.hasNext()) {
			ChainVertex v = it.next();
			m = crystalAssemblies.getLatticeGraph().getUnitCellTransformationOrthonormal(v.getChainId(), v.getOpId());
			// transform the chain
			chain = (Chain) crystalAssemblies.getStructure().getChainByPDB(v.getChainId()).clone();
			Calc.transform(chain, m);
			chains.add(new ChainVertex(chain,v.getOpId()));
			

			// we still need to get the xtal translation from the edges in the path from refVertex to current vertex
			Point3d trans = new Point3d(0,0,0);
			DijkstraShortestPath<ChainVertex, InterfaceEdge> dsp = 
					new DijkstraShortestPath<ChainVertex, InterfaceEdge>(firstCc, refVertex, v);
			GraphPath<ChainVertex,InterfaceEdge> gp = dsp.getPath();
			
			List<ChainVertex> visitedVertices = Graphs.getPathVertexList(gp);
			List<InterfaceEdge> path = gp.getEdgeList();
			
			
			//List<InterfaceEdge> path = DijkstraShortestPath.findPathBetween(firstCc, refVertex, v);
			
			for (int i=0;i<path.size();i++) {
				InterfaceEdge e = path.get(i);
				ChainVertex s = firstCc.getEdgeSource(e);
				Point3d currentTrans = new Point3d(e.getXtalTrans().x,e.getXtalTrans().y,e.getXtalTrans().z);
				
				// making sure we get the direction correctly
				if (!s.equals(visitedVertices.get(i))) {
					currentTrans.negate();
				}
				trans.add(currentTrans);
			}
			crystalAssemblies.getStructure().getCrystallographicInfo().getCrystalCell().transfToOrthonormal(trans);
			
			Calc.translate(chain, new Vector3d(trans.x,trans.y,trans.z)); 
		}
		
		return chains;
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
		for (ChainVertex cv:getStructure()) {
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
		int numChains = getStructure().size();
		Set<String> uniqueChains = new HashSet<String>();
		for (ChainVertex cv:getStructure()) {
			uniqueChains.add(cv.getChain().getChainID());
		}
		if (numChains != uniqueChains.size()) symRelatedChainsExist = true;
		
		
		PrintStream ps = new PrintStream(new GZIPOutputStream(new FileOutputStream(file)));

		ps.println(SimpleMMcifParser.MMCIF_TOP_HEADER+"eppic_assembly_"+getId());
		
		ps.print(FileConvert.getAtomSiteHeader());
		
		List<Object> atomSites = new ArrayList<Object>();
		
		int atomId = 1;
		for (ChainVertex cv:getStructure()) {
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
				
		ps.print(MMCIFFileTools.toMMCIF(atomSites));
		
		
		ps.close();
	}
	
	
	
	/**
	 * Return the first interface cluster (present in given stoichiometry) that has the given 
	 * multiplicity.
	 * @param multiplicity
	 * @param sto
	 * @return the first interface cluster with the desired multiplicity or null if no interface cluster has the 
	 * desired multiplicity
	 */
	public StructureInterfaceCluster getInterfClusterWithMultiplicity(int multiplicity, Stoichiometry sto) {
		
		for (StructureInterfaceCluster interfCluster:getHomoEngagedInterfaceClusters(sto)) {
			
			if (multiplicity == GraphUtils.getEdgeMultiplicity(assemblyGraph.getFirstRelevantConnectedComponent(sto), interfCluster.getId())) {
				return interfCluster;
			}
			
		}
		
		return null;
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
		int numClusters = getNumEngagedInterfaceClusters();
		sb.append("{");
		int e = 0;
		for (int i=0;i<engagedSet.size();i++) {
			if (engagedSet.isOn(i)) {
				sb.append(i+1);
				e++;
				if (e!=numClusters) sb.append(",");
			}
		}
		sb.append("}");
		return sb.toString();
	}

	public void score() {

		// note: the code here goes along the same lines of Stoichiometry.getSymmetry(), now SubAssembly.getSymmetry
		// we should try to unify them a bit and to reuse the common parts

		// we won't support non-fully-covering stoichiometries for the moment
		// TODO support them: most likely requires a more complex data model where we can have score/calls for each of the subcomponents of the assembly
		if (!assemblyGraph.isFullyCovering()) {
			logger.warn("Assembly {} does not cover all entities, assembly scoring will be done for first stoichiometry only", toString());
		}
		
		SubAssembly firstSubAssembly = assemblyGraph.getSubAssemblies().get(0);
		
		Stoichiometry sto = firstSubAssembly.getStoichiometry();
				
		int n = sto.getFirstNonZero();
		
		if (n==-1) {
			logger.warn("All counts are 0 for first stoichiometry of assembly {}. Something is wrong: can't score assembly!",toString());
			return;
		}
		
		
		int numEntities = sto.getNumPresentEntities();
		
		boolean heteromer = false;
		if (numEntities>1) heteromer = true;

		String sym = firstSubAssembly.getSymmetry();
		

		setCall(CallType.CRYSTAL); // set crystal as default call, only if found to be bio it will be overridden below

		if (n==1) {
			
			if (!heteromer) {
				// a C1 assembly (i.e. monomeric if homomeric):
				// no scoring at this stage, later we look at all assemblies and if no larger assembly is bio, we assign bios to C1 assemblies
				
			} else {
				// C1 heteromeric: 1:1:1 stoichiometry
				List<StructureInterfaceCluster> list = getEngagedInterfaceClusters(sto);
				int countBio = 0;
				for (StructureInterfaceCluster interfCluster:list) {
					if (getCrystalAssemblies().getInterfaceEvolContextList().
							getCombinedClusterPredictor(interfCluster.getId()).getCall() == CallType.BIO ) {
						countBio++;
					}
				}
				if (countBio>= (numEntities-1) ) setCall(CallType.BIO);
			}
			return;
		}
		

		UndirectedGraph<ChainVertex, InterfaceEdge> g = assemblyGraph.getFirstRelevantConnectedComponent(sto);
		GraphContractor gctr = new GraphContractor(g);
		
		
		if (heteromer) {

			g = gctr.contract();
			
			// TODO we should check the call of contracted interfaces and score properly based on 
			// them and the relevant interfaces below
		}

		
		TreeMap<Integer,Integer> clusterIdsToMult = GraphUtils.getCycleMultiplicities(g);
		
		
		if (sym.startsWith("C")) {

			StructureInterfaceCluster interfCluster = null;

			if (sym.startsWith("C2")) {				
				// we've got to treat the C2 case especially because multiplicity=2 won't be detected in graph
				
				if (clusterIdsToMult.isEmpty()) {
					logger.error("Empty list of engaged interface clusters for a homomeric C2 symmetry. Something is wrong!");
					
				} else {
					int clusterId = clusterIdsToMult.firstKey(); // the largest interface present (interface cluster ids are sorted on areas)
					interfCluster = crystalAssemblies.getInterfaceClusters().get(clusterId-1);
				}

			} else {

				int clusterId = -1;
				for (int cId: clusterIdsToMult.keySet()) {
					if (clusterId==-1 && clusterIdsToMult.get(cId) == n) 
						clusterId = cId;
					else if (clusterIdsToMult.get(cId)==n) 
						logger.info("Assembly {} has more than 1 interface cluster with cycle multiplicity {}. Taking assembly call from first one.", 
								toString(), n);
				}
				
				if (clusterId == -1) {
					logger.warn("Could not find the C{} interface for assembly {}. Something is wrong!",
							n,toString());
				} else {				
					interfCluster = crystalAssemblies.getInterfaceClusters().get(clusterId-1);
				
				}
			}

			if (interfCluster!=null) {
				// the call for the Cn interface will be the call for the assembly
				CallType call = getCrystalAssemblies().getInterfaceEvolContextList().getCombinedClusterPredictor(interfCluster.getId()).getCall();
				setCall(call);
				// TODO in heteromeric cases we should check that the edges that we have contracted have also the same call

			} else {
				logger.warn("Could not find the relevant C{} interface, assembly {} will be NOPRED",n,toString());
				setCall(CallType.NO_PREDICTION);
			}



		} else if (sym.startsWith("D") || sym.equals("T") || sym.equals("O") || sym.equals("I")) {
			
			// In all other point group symmetries there's always 2 essential interfaces out of all of
			// the engaged ones that are needed to form the symmetry. 
			// Then a third one follows necessarily (though it might be too small and not show up in our 
			// list) because the other 2 are at the right angles to produce a third one. 
			
			// A possible strategy for scoring is simply taking the 2 largest interfaces and checking that both 
			// have a BIO call (that would be a sufficient condition), otherwise is XTAL. 
			// Another possibility would be to take any 2 interfaces out of the list and check if at 
			// least 2 are BIO.
						
			// In a D assembly most usually the 2 largest interfaces are the 2 isologous, 
			// the 3rd one being the heterologous. But that's not a general rule at all! there are counter-examples


			// the keys of the map should be sorted from first cluster id to last cluster id (which are sorted by areas)

			Iterator<Integer> it = clusterIdsToMult.keySet().iterator();
			int clusterId1 = it.next(); // the largest
			int clusterId2 = it.next(); // the second largest
			
			StructureInterfaceCluster first = crystalAssemblies.getInterfaceClusters().get(clusterId1-1); // the largest
			StructureInterfaceCluster second = crystalAssemblies.getInterfaceClusters().get(clusterId2-1); // the second largest
			
			CallType firstCall = getCrystalAssemblies().getInterfaceEvolContextList().getCombinedClusterPredictor(first.getId()).getCall();
			CallType secondCall = getCrystalAssemblies().getInterfaceEvolContextList().getCombinedClusterPredictor(second.getId()).getCall();
			
			if (firstCall == CallType.BIO && secondCall == CallType.BIO) {
				setCall(CallType.BIO);
			}
			
		} else {
			logger.warn("Assembly scoring for symmetry {} not supported yet", sym);
			return;
		}



	}
	
	/**
	 * Sorts given list of interface clusters by descending descending areas
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
