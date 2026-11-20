package eppic.analysis.compare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.contact.GroupContact;
import org.biojava.nbio.structure.contact.InterfaceFinder;
import org.biojava.nbio.structure.contact.StructureInterface;
import org.biojava.nbio.structure.contact.StructureInterfaceList;
import org.biojava.nbio.structure.quaternary.BioAssemblyInfo;
import org.biojava.nbio.structure.quaternary.BiologicalAssemblyBuilder;
import org.biojava.nbio.structure.quaternary.BiologicalAssemblyTransformation;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.isomorphism.VF2GraphIsomorphismInspector;
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.EppicParams;
import eppic.assembly.Assembly;
import eppic.assembly.ChainVertex;
import eppic.assembly.CrystalAssemblies;
import eppic.assembly.GraphUtils;
import eppic.assembly.InterfaceEdge;
import eppic.assembly.SubAssembly;
import eppic.analysis.compare.BiounitMatchResult.MatchType;

/**
 * Matches PDB-annotated biological assemblies to EPPIC's valid assemblies by comparing labelled graphs.
 * <p>
 * The algorithm:
 * <ol>
 * <li>The biounit is built from the asymmetric unit with BioJava's {@link BiologicalAssemblyBuilder} and its
 * actual chain-chain contacts are found with BioJava's {@link InterfaceFinder}. Graph: nodes are chains labelled
 * with their entity id, edges are contacts.</li>
 * <li>Each contact is labelled with the EPPIC interface cluster it corresponds to, using the BioJava contact overlap
 * score ({@link StructureInterface#getContactOverlapScore(StructureInterface, boolean)}) against all
 * members of each EPPIC interface cluster, with the same cutoff EPPIC uses for clustering interfaces. Contacts
 * matching no cluster are kept as unlabelled edges if their buried area is above EPPIC's minimum interface area,
 * otherwise they are ignored (as EPPIC ignores them).</li>
 * <li>For every valid assembly A, the biounit edges not engaged in A are removed (they must be redundant, i.e.
 * removing them must not change the biounit's connected components). Each remaining connected component must
 * then be isomorphic (VF2, preserving entity and cluster labels) to one of A's sub-assemblies, or else be
 * isomorphic to an induced subgraph of one of them (partial assembly).</li>
 * <li>Among the matching candidates, the best one is chosen by: fewest removed edges, full isomorphism before
 * partial, fewest engaged clusters not used by the biounit, lowest assembly id.</li>
 * </ol>
 *
 * @author Jose Duarte
 */
public class BiounitGraphMatcher {

	private static final Logger logger = LoggerFactory.getLogger(BiounitGraphMatcher.class);

	/**
	 * Biounits with more polymer chains than this are not matched (building them and finding contacts gets expensive).
	 */
	public static final int DEFAULT_MAX_CHAINS = 1000;

	/**
	 * Number of sphere points for ASA calculation of the (few) unlabelled contacts, to decide if they are
	 * above the minimum interface area. Precision is not important here.
	 */
	private static final int ASA_SPHERE_POINTS = 100;

	private final CrystalAssemblies validAssemblies;
	private final Structure asymUnit;
	private final int numThreads;
	private final int minCofactorSize;
	private int maxChains = DEFAULT_MAX_CHAINS;

	/** EPPIC interfaces grouped by interface cluster id */
	private final Map<Integer, List<StructureInterface>> eppicClusters;

	/** cache of the candidate sub-assembly graphs per assembly */
	private final Map<Assembly, List<LabelledGraph>> subAssemblyGraphs;

	/**
	 * @param validAssemblies the EPPIC assemblies; its structure is used as the asymmetric unit to build biounits from
	 * @param numThreads threads for ASA calculation
	 * @param minCofactorSize the min cofactor size for ASA calculation, as in {@link EppicParams#getMinSizeCofactorForAsa()}
	 */
	public BiounitGraphMatcher(CrystalAssemblies validAssemblies, int numThreads, int minCofactorSize) {
		this.validAssemblies = validAssemblies;
		this.asymUnit = validAssemblies.getStructure();
		this.numThreads = numThreads;
		this.minCofactorSize = minCofactorSize;
		this.eppicClusters = new TreeMap<>();
		this.subAssemblyGraphs = new HashMap<>();

		Set<Integer> seenInterfaceIds = new HashSet<>();
		for (InterfaceEdge e : validAssemblies.getLatticeGraph().getGraph().edgeSet()) {
			if (e.getInterface() == null || !seenInterfaceIds.add(e.getInterfaceId())) continue;
			eppicClusters.computeIfAbsent(e.getClusterId(), k -> new ArrayList<>()).add(e.getInterface());
		}
	}

	public void setMaxChains(int maxChains) {
		this.maxChains = maxChains;
	}

	/**
	 * Match the given biounit to the valid assemblies.
	 * @param bioUnit the PDB biounit annotation, with transformations referring to asym ids of the asymmetric unit
	 * @return the result, never null
	 */
	public BiounitMatchResult match(BioAssemblyInfo bioUnit) {
		long start = System.currentTimeMillis();
		BiounitMatchResult result = doMatch(bioUnit);
		result.setTimeMs(System.currentTimeMillis() - start);
		logger.info("Graph matching of PDB biounit {}: {}", bioUnit.getId(), result);
		return result;
	}

	private BiounitMatchResult doMatch(BioAssemblyInfo bioUnit) {

		if (bioUnit.getTransforms() == null || bioUnit.getTransforms().isEmpty()) {
			return BiounitMatchResult.skipped("no transformations");
		}

		// 0. only the polymer chains (the transforms list is copied, the builder sorts it in place)
		List<BiologicalAssemblyTransformation> transforms = new ArrayList<>();
		for (BiologicalAssemblyTransformation t : bioUnit.getTransforms()) {
			if (asymUnit.getPolyChain(t.getChainId()) != null) {
				transforms.add(t);
			}
		}
		if (transforms.isEmpty()) {
			return BiounitMatchResult.skipped("no polymer chains");
		}
		if (transforms.size() > maxChains) {
			return BiounitMatchResult.skipped("too many chains: " + transforms.size() + " > " + maxChains);
		}

		// 1. build biounit and find its contacts
		Structure bu = new BiologicalAssemblyBuilder().rebuildQuaternaryStructure(asymUnit, transforms, true, false);

		LabelledGraph buGraph = new LabelledGraph();
		for (Chain c : bu.getPolyChains()) {
			buGraph.addVertex(new Node(c.getId(), getEntityId(c)));
		}

		InterfaceFinder finder = new InterfaceFinder(bu);
		finder.setCutoff(EppicParams.INTERFACE_DIST_CUTOFF);
		StructureInterfaceList contacts = finder.getAllInterfaces();

		// 2. label contacts with EPPIC interface clusters.
		// Symmetry-equivalent contacts have identical residue contact sets: we label (and calculate areas for)
		// only one contact per distinct residue contact set
		Map<String, Integer> labelCache = new HashMap<>();
		Map<String, StructureInterface> unlabelledReps = new LinkedHashMap<>();
		List<StructureInterface> unlabelled = new ArrayList<>();
		List<String> unlabelledKeys = new ArrayList<>();
		int numLabelled = 0;
		Set<Integer> clusterIds = new TreeSet<>();
		for (StructureInterface contact : contacts) {
			String key = getContactKey(contact);
			int clusterId = labelCache.computeIfAbsent(key, k -> findBestCluster(contact));
			if (clusterId < 0) {
				unlabelled.add(contact);
				unlabelledKeys.add(key);
				unlabelledReps.putIfAbsent(key, contact);
				continue;
			}
			if (addEdge(buGraph, contact, clusterId)) {
				numLabelled++;
				clusterIds.add(clusterId);
			}
		}

		int numUnlabelled = 0;
		if (!unlabelled.isEmpty()) {
			StructureInterfaceList reps = new StructureInterfaceList();
			unlabelledReps.values().forEach(reps::add);
			reps.calcAsas(ASA_SPHERE_POINTS, numThreads, minCofactorSize);
			for (int i = 0; i < unlabelled.size(); i++) {
				StructureInterface contact = unlabelled.get(i);
				double area = unlabelledReps.get(unlabelledKeys.get(i)).getTotalArea();
				if (area >= EppicParams.MIN_INTERFACE_AREA_TO_KEEP) {
					logger.info("Biounit {} contact {}-{} (area {}) does not match any EPPIC interface cluster",
							bioUnit.getId(), contact.getMoleculeIds().getFirst(), contact.getMoleculeIds().getSecond(),
							String.format("%.0f", area));
					if (addEdge(buGraph, contact, Link.UNLABELLED)) numUnlabelled++;
				}
			}
		}

		// 3. compare with every valid assembly
		int numComponents = new ConnectivityInspector<>(buGraph).connectedSets().size();

		Candidate best = null;
		for (Assembly a : validAssemblies) {
			Candidate c = evaluate(a, buGraph, numComponents, clusterIds);
			if (c != null && (best == null || c.compareTo(best) < 0)) {
				best = c;
			}
		}

		BiounitMatchResult result;
		if (best == null) {
			result = new BiounitMatchResult(MatchType.NONE, null, clusterIds);
		} else {
			MatchType type;
			if (best.partial) type = MatchType.SUBASSEMBLY;
			else if (best.numRemoved > 0) type = MatchType.EXTRA_EDGES;
			else type = MatchType.EXACT;
			result = new BiounitMatchResult(type, best.assembly, clusterIds);
			result.setNumRemovedEdges(best.numRemoved);
		}
		result.setNumNodes(buGraph.vertexSet().size());
		result.setNumLabelledEdges(numLabelled);
		result.setNumUnlabelledEdges(numUnlabelled);
		result.setNumMinorContacts(unlabelled.size() - numUnlabelled);
		result.setBiounitStoichiometry(buGraph.getStoichiometryString());
		return result;
	}

	/**
	 * Evaluates whether the biounit graph matches the given assembly.
	 * @return the candidate or null if it doesn't match
	 */
	private Candidate evaluate(Assembly a, LabelledGraph buGraph, int numComponents, Set<Integer> buClusterIds) {

		SortedSet<Integer> engaged = GraphUtils.getDistinctInterfaceClusters(a.getAssemblyGraph().getSubgraph());

		// remove the edges not engaged in this assembly
		LabelledGraph filtered = buGraph.copyWithEdges(l -> engaged.contains(l.label));
		int numRemoved = buGraph.edgeSet().size() - filtered.edgeSet().size();

		List<Set<Node>> components = new ConnectivityInspector<>(filtered).connectedSets();
		if (components.size() != numComponents) {
			// removed edges were not redundant: they changed connectivity
			return null;
		}

		List<LabelledGraph> subs = getSubAssemblyGraphs(a);

		boolean partial = false;
		for (Set<Node> comp : components) {
			LabelledGraph compGraph = filtered.inducedSubgraph(comp);
			Signature sig = compGraph.getSignature();

			boolean full = false;
			for (LabelledGraph sub : subs) {
				if (sig.equals(sub.getSignature()) && isIsomorphic(compGraph, sub)) {
					full = true;
					break;
				}
			}
			if (full) continue;

			boolean part = false;
			for (LabelledGraph sub : subs) {
				if (sig.fitsIn(sub.getSignature()) && compGraph.vertexSet().size() < sub.vertexSet().size()
						&& isInducedSubgraph(compGraph, sub)) {
					part = true;
					break;
				}
			}
			if (!part) return null;
			partial = true;
		}

		Set<Integer> extraEngaged = new TreeSet<>(engaged);
		extraEngaged.removeAll(buClusterIds);

		return new Candidate(a, numRemoved, partial, extraEngaged.size());
	}

	private List<LabelledGraph> getSubAssemblyGraphs(Assembly a) {
		return subAssemblyGraphs.computeIfAbsent(a, k -> {
			// sub-assemblies with identical signatures are assumed equivalent: keep one per signature
			Map<Signature, LabelledGraph> unique = new LinkedHashMap<>();
			for (SubAssembly sub : k.getAssemblyGraph().getSubAssemblies()) {
				LabelledGraph g = LabelledGraph.fromSubAssembly(sub.getConnectedGraph());
				unique.putIfAbsent(g.getSignature(), g);
			}
			return new ArrayList<>(unique.values());
		});
	}

	/**
	 * Finds the EPPIC interface cluster that best matches the given contact by contact overlap.
	 * @return the cluster id or -1 if none is above {@link EppicParams#CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF}
	 */
	private int findBestCluster(StructureInterface contact) {
		int bestId = -1;
		double bestScore = EppicParams.CLUSTERING_CONTACT_OVERLAP_SCORE_CUTOFF;
		for (Map.Entry<Integer, List<StructureInterface>> entry : eppicClusters.entrySet()) {
			for (StructureInterface eppicInterf : entry.getValue()) {
				double score = Math.max(
						contact.getContactOverlapScore(eppicInterf, false),
						contact.getContactOverlapScore(eppicInterf, true));
				if (score >= bestScore && (bestId == -1 || score > bestScore)) {
					bestScore = score;
					bestId = entry.getKey();
				}
			}
		}
		return bestId;
	}

	/**
	 * A key identifying the residue contact set of a contact, independent of chain names and of the order of the 2 chains:
	 * entity ids and sorted residue-pair list, in the canonical of the 2 possible orders.
	 */
	private static String getContactKey(StructureInterface contact) {
		int e1 = getEntityId(contact.getParentChains().getFirst());
		int e2 = getEntityId(contact.getParentChains().getSecond());
		List<String> direct = new ArrayList<>();
		List<String> inverse = new ArrayList<>();
		for (GroupContact gc : contact.getGroupContacts()) {
			String r1 = gc.getPair().getFirst().getResidueNumber().toString();
			String r2 = gc.getPair().getSecond().getResidueNumber().toString();
			direct.add(r1 + "-" + r2);
			inverse.add(r2 + "-" + r1);
		}
		Collections.sort(direct);
		Collections.sort(inverse);
		String d = e1 + "|" + e2 + "|" + String.join(",", direct);
		String i = e2 + "|" + e1 + "|" + String.join(",", inverse);
		return d.compareTo(i) <= 0 ? d : i;
	}

	private static boolean addEdge(LabelledGraph g, StructureInterface contact, int label) {
		Node n1 = g.getNode(contact.getParentChains().getFirst().getId());
		Node n2 = g.getNode(contact.getParentChains().getSecond().getId());
		if (n1 == null || n2 == null || n1.equals(n2)) {
			logger.warn("Could not find nodes for contact {}-{}", contact.getMoleculeIds().getFirst(), contact.getMoleculeIds().getSecond());
			return false;
		}
		if (g.containsEdge(n1, n2)) return false;
		g.addEdge(n1, n2, new Link(label));
		return true;
	}

	private static int getEntityId(Chain c) {
		return c.getEntityInfo() == null ? -1 : c.getEntityInfo().getMolId();
	}

	private static boolean isIsomorphic(LabelledGraph g1, LabelledGraph g2) {
		return new VF2GraphIsomorphismInspector<>(g1, g2, NODE_COMPARATOR, LINK_COMPARATOR).isomorphismExists();
	}

	/**
	 * @return true if small is isomorphic to an induced subgraph of big
	 */
	private static boolean isInducedSubgraph(LabelledGraph small, LabelledGraph big) {
		return new VF2SubgraphIsomorphismInspector<>(big, small, NODE_COMPARATOR, LINK_COMPARATOR).isomorphismExists();
	}

	private static final Comparator<Node> NODE_COMPARATOR = Comparator.comparingInt(n -> n.entityId);
	private static final Comparator<Link> LINK_COMPARATOR = Comparator.comparingInt(l -> l.label);

	private static class Candidate implements Comparable<Candidate> {
		final Assembly assembly;
		final int numRemoved;
		final boolean partial;
		final int numExtraEngaged;

		Candidate(Assembly assembly, int numRemoved, boolean partial, int numExtraEngaged) {
			this.assembly = assembly;
			this.numRemoved = numRemoved;
			this.partial = partial;
			this.numExtraEngaged = numExtraEngaged;
		}

		@Override
		public int compareTo(Candidate o) {
			if (numRemoved != o.numRemoved) return Integer.compare(numRemoved, o.numRemoved);
			if (partial != o.partial) return partial ? 1 : -1;
			if (numExtraEngaged != o.numExtraEngaged) return Integer.compare(numExtraEngaged, o.numExtraEngaged);
			return Integer.compare(assembly.getId(), o.assembly.getId());
		}
	}

	/**
	 * A chain in a graph, labelled by entity id
	 */
	static class Node {
		final String id;
		final int entityId;

		Node(String id, int entityId) {
			this.id = id;
			this.entityId = entityId;
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof Node && ((Node) o).id.equals(id);
		}

		@Override
		public int hashCode() {
			return id.hashCode();
		}

		@Override
		public String toString() {
			return id + "(" + entityId + ")";
		}
	}

	/**
	 * A contact in a graph, labelled by EPPIC interface cluster id. Compared by identity.
	 */
	static class Link {
		static final int UNLABELLED = -1;
		final int label;

		Link(int label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return String.valueOf(label);
		}
	}

	/**
	 * Cheap graph invariants: node counts per entity, edge counts per label and the sorted multiset of
	 * (entity, sorted incident labels) per node.
	 */
	static class Signature {
		final TreeMap<Integer, Integer> entityCounts = new TreeMap<>();
		final TreeMap<Integer, Integer> labelCounts = new TreeMap<>();
		final List<String> nodeTypes = new ArrayList<>();

		boolean fitsIn(Signature o) {
			for (Map.Entry<Integer, Integer> e : entityCounts.entrySet()) {
				if (o.entityCounts.getOrDefault(e.getKey(), 0) < e.getValue()) return false;
			}
			for (Map.Entry<Integer, Integer> e : labelCounts.entrySet()) {
				if (o.labelCounts.getOrDefault(e.getKey(), 0) < e.getValue()) return false;
			}
			return true;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Signature)) return false;
			Signature s = (Signature) o;
			return entityCounts.equals(s.entityCounts) && labelCounts.equals(s.labelCounts) && nodeTypes.equals(s.nodeTypes);
		}

		@Override
		public int hashCode() {
			return Objects.hash(entityCounts, labelCounts, nodeTypes);
		}
	}

	/**
	 * Simple undirected graph of chains (labelled by entity) and contacts (labelled by interface cluster)
	 */
	static class LabelledGraph extends SimpleGraph<Node, Link> {
		private static final long serialVersionUID = 1L;

		private final Map<String, Node> nodesById = new HashMap<>();
		private Signature signature;

		LabelledGraph() {
			super(Link.class);
		}

		static LabelledGraph fromSubAssembly(UndirectedGraph<ChainVertex, InterfaceEdge> g) {
			LabelledGraph lg = new LabelledGraph();
			for (ChainVertex v : g.vertexSet()) {
				lg.addVertex(new Node(v.getChainId() + "_" + v.getOpId(), v.getEntityId()));
			}
			for (InterfaceEdge e : g.edgeSet()) {
				Node n1 = lg.getNode(g.getEdgeSource(e).getChainId() + "_" + g.getEdgeSource(e).getOpId());
				Node n2 = lg.getNode(g.getEdgeTarget(e).getChainId() + "_" + g.getEdgeTarget(e).getOpId());
				if (n1.equals(n2) || lg.containsEdge(n1, n2)) {
					logger.debug("Skipping loop or multiple edge {} in sub-assembly graph", e);
					continue;
				}
				lg.addEdge(n1, n2, new Link(e.getClusterId()));
			}
			return lg;
		}

		@Override
		public boolean addVertex(Node n) {
			nodesById.put(n.id, n);
			signature = null;
			return super.addVertex(n);
		}

		@Override
		public boolean addEdge(Node n1, Node n2, Link l) {
			signature = null;
			return super.addEdge(n1, n2, l);
		}

		Node getNode(String id) {
			return nodesById.get(id);
		}

		LabelledGraph copyWithEdges(java.util.function.Predicate<Link> keep) {
			LabelledGraph g = new LabelledGraph();
			for (Node n : vertexSet()) g.addVertex(n);
			for (Link l : edgeSet()) {
				if (keep.test(l)) g.addEdge(getEdgeSource(l), getEdgeTarget(l), l);
			}
			return g;
		}

		LabelledGraph inducedSubgraph(Set<Node> nodes) {
			LabelledGraph g = new LabelledGraph();
			for (Node n : nodes) g.addVertex(n);
			for (Link l : edgeSet()) {
				Node s = getEdgeSource(l);
				Node t = getEdgeTarget(l);
				if (nodes.contains(s) && nodes.contains(t)) g.addEdge(s, t, l);
			}
			return g;
		}

		Signature getSignature() {
			if (signature != null) return signature;
			Signature s = new Signature();
			for (Node n : vertexSet()) {
				s.entityCounts.merge(n.entityId, 1, Integer::sum);
				List<Integer> incident = new ArrayList<>();
				for (Link l : edgesOf(n)) incident.add(l.label);
				Collections.sort(incident);
				s.nodeTypes.add(n.entityId + ":" + incident);
			}
			Collections.sort(s.nodeTypes);
			for (Link l : edgeSet()) {
				s.labelCounts.merge(l.label, 1, Integer::sum);
			}
			signature = s;
			return s;
		}

		String getStoichiometryString() {
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<Integer, Integer> e : getSignature().entityCounts.entrySet()) {
				if (sb.length() > 0) sb.append(',');
				sb.append(e.getKey()).append(':').append(e.getValue());
			}
			return sb.toString();
		}
	}
}
