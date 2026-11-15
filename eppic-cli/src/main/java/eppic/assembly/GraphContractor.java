package eppic.assembly;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.vecmath.Point3i;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contraction of heteromeric edges of a lattice graph, in order to obtain a pseudo-homomeric graph
 * where every vertex represents a heteromeric "protomer".
 * <p>
 * The contraction is done as a quotient of the periodic graph: given an interface cluster to contract,
 * the connected components of the subgraph containing only edges of that cluster are found and each component
 * is replaced by a single representative vertex. Every vertex in a component gets assigned
 * an offset (in unit cells) with respect to its representative, found by adding up the edge translations
 * along a path in the component. All remaining edges are then re-attached to the representatives with their
 * translations corrected by the offsets of their 2 end vertices:
 * <pre>
 *   t(rep(s) -> rep(t)) = offset(s) + t(s -> t) - offset(t)
 * </pre>
 * With that correction the net translation of any cycle is identical in the full and in the contracted graph,
 * which is what makes the closed-symmetry check equivalent in the 2 graphs.
 * <p>
 * After every contraction round:
 * <li>edges internal to a contracted vertex (loops with 0 translation) disappear: they are implicitly engaged</li>
 * <li>loops with non-zero translation are kept and flagged as infinite: the contracted vertex is in contact with a translated copy of itself</li>
 * <li>edges joining the same 2 vertices with the same translation (i.e. the same 2 protomers in the lattice)
 * are merged into a single edge. If they belong to different interface clusters then those clusters are equivalent
 * in the contracted graph and are merged into a single one, see {@link #getMergedInterfClusterIds(int)}</li>
 * <li>edges joining the same 2 vertices with different translations are kept: they join different pairs of protomers in the lattice</li>
 * <p>
 * The original graph is never modified.
 *
 * @param <V>
 * @param <E>
 */
public class GraphContractor<V extends ChainVertexInterface, E extends InterfaceEdgeInterface> {

	private static final Logger logger = LoggerFactory.getLogger(GraphContractor.class);

	private static final Point3i ZERO = new Point3i(0, 0, 0);

	/**
	 * Deterministic ordering of vertices, used to choose representatives and to orient edges.
	 */
	private final Comparator<V> vertexComparator = Comparator
			.comparingInt((V v) -> v.getEntityId())
			.thenComparing(v -> v.getChainId())
			.thenComparingInt(v -> v.getOpId());

	/**
	 * The original graph
	 */
	private UndirectedGraph<V, E> g;

	/**
	 * The contracted graph
	 */
	private UndirectedGraph<V, E> cg;

	/**
	 * Encodes a tree with which nodes were merged.
	 * Access with {@link #getContractedVertex(ChainVertexInterface)}
	 */
	private Map<V,V> contractedVertices;

	/**
	 * The set of contracted interface cluster ids
	 */
	private Set<Integer> contractedInterfClusterIds;

	/**
	 * For every interface cluster id surviving in the contracted graph, the interface cluster ids
	 * of the original graph that it represents (including itself).
	 */
	private Map<Integer, SortedSet<Integer>> mergedInterfClusterIds;

	public GraphContractor(UndirectedGraph<V, E> g) {
		this.g = g;
		this.contractedVertices = new HashMap<V, V>();
		this.contractedInterfClusterIds = new TreeSet<>();
		this.mergedInterfClusterIds = new TreeMap<>();
	}

	/**
	 * Contract the graph iteratively using the largest contractible heteromeric interface in each iteration,
	 * in order to create a pseudo-homomeric graph.
	 * A heteromeric interface cluster is contractible if it is present in all vertices of the 2 entities
	 * that it joins and if its edges alone form a finite (closed) assembly.
	 * If at some point there are heteromeric interfaces left but none of them is contractible the
	 * procedure stops, and the returned graph will not be pseudo-homomeric.
	 * Contracted vertices can be obtained subsequently with {@link #getContractedVertices()} and their
	 * replacements by {@link #getContractedVertex(V)}
	 * @return the contracted graph, also accessible via {@link #getContractedGraph()}
	 * @param edgeClass
	 */
	public UndirectedGraph<V, E> contract(Class<? extends E> edgeClass) {

		cg = g;

		// heteromeric interface clusters that were tried and found not to be contractible
		Set<Integer> rejected = new HashSet<>();

		int i = 0;
		while (true) {

			// TODO select the best scoring interface instead of largest (in most cases it will coincide with the largest anyway)
			int interfClusterId = GraphUtils.getLargestHeteroInterfaceCluster(cg, rejected);

			// if no more contractible heteromeric interfaces we break: end of iteration
			if (interfClusterId == -1) break;

			i++;
			logger.info("Round {} of contraction: contracting interface cluster {}",i,interfClusterId);
			logger.debug("Starting graph before contraction has {} vertices and {} edges. Counts per entity: {}",
					cg.vertexSet().size(),cg.edgeSet().size(), getEntityCounts(cg));

			// the interface clusters that were merged to this one in previous rounds: must get them before contracting
			SortedSet<Integer> members = getMergedInterfClusterIds(interfClusterId);

			UndirectedGraph<V, E> contGraph = contractInterfaceCluster(cg, interfClusterId, edgeClass);

			if (contGraph == null) {
				logger.info("Interface cluster {} forms an infinite assembly on its own. Won't contract it", interfClusterId);
				rejected.add(interfClusterId);
				continue;
			}

			cg = contGraph;

			// we keep a list of every interf cluster id we contract (and those that had been merged to it)
			contractedInterfClusterIds.addAll(members);
		}

		if (!GraphUtils.getDistinctHeteromericInterfaceClusterIds(cg).isEmpty()) {
			logger.warn("Heteromeric interface clusters {} could not be contracted. The contracted graph is not pseudo-homomeric",
					GraphUtils.getDistinctHeteromericInterfaceClusterIds(cg));
		}

		logger.debug("Final contracted graph ({} vertices, {} edges):\n{}", cg.vertexSet().size(), cg.edgeSet().size(),
				GraphUtils.asString(cg));

		return cg;
	}

	/**
	 * Contract all edges with given interfClusterId, see the class docs for an explanation of the procedure.
	 * @param inputGraph
	 * @param interfClusterId
	 * @param edgeClass
	 * @return the contracted graph or null if the edges of the given interface cluster don't form a
	 * finite assembly, in which case the contraction is not possible
	 */
	private UndirectedGraph<V, E> contractInterfaceCluster(
			UndirectedGraph<V, E> inputGraph, int interfClusterId, Class<? extends E> edgeClass) {

		// 1. The connected components of the graph with only edges of the given interface cluster.
		// Every vertex gets a representative and an offset to it

		Map<V, V> reps = new HashMap<>();
		Map<V, Point3i> offsets = new HashMap<>();

		if (!findComponents(inputGraph, interfClusterId, reps, offsets)) {
			return null;
		}

		// 2. The quotient graph: every edge is re-attached to the representatives

		UndirectedGraph<V, E> contGraph = new Pseudograph<>(edgeClass);

		for (V v : inputGraph.vertexSet()) {
			if (reps.get(v).equals(v)) {
				contGraph.addVertex(v);
			} else {
				contractedVertices.put(v, reps.get(v));
			}
		}

		// edges grouped by the pair of vertices that they join in the lattice (vertices + translation)
		Map<EdgeKey, List<E>> groups = new LinkedHashMap<>();

		for (E e : inputGraph.edgeSet()) {

			V s = inputGraph.getEdgeSource(e);
			V t = inputGraph.getEdgeTarget(e);
			V sRep = reps.get(s);
			V tRep = reps.get(t);

			// t(rep(s) -> rep(t)) = offset(s) + t(s -> t) - offset(t)
			Point3i trans = new Point3i(offsets.get(s));
			trans.add(e.getXtalTrans());
			trans.sub(offsets.get(t));

			boolean isLoop = sRep.equals(tRep);

			if (isLoop && trans.equals(ZERO)) {
				// internal to the contracted vertex: the contracted edges themselves and edges induced by them
				continue;
			}

			// a canonical direction for the edge, so that equivalent edges can be recognised
			if ( (!isLoop && vertexComparator.compare(sRep, tRep) > 0) || (isLoop && !isCanonical(trans)) ) {
				V tmp = sRep;
				sRep = tRep;
				tRep = tmp;
				trans.negate();
			}

			E newEdge = copyEdge(e, trans);
			if (isLoop) {
				// a contracted vertex in contact with a translated copy of itself
				newEdge.setIsInfinite(true);
			}

			groups.computeIfAbsent(new EdgeKey(sRep, tRep, trans), k -> new ArrayList<>()).add(newEdge);
		}

		// 3. Different interface clusters joining the same 2 vertices with same translation are
		// equivalent in the contracted graph: we merge them into the one with lowest id

		Map<Integer, Integer> parents = new HashMap<>();
		for (Entry<EdgeKey, List<E>> entry : groups.entrySet()) {
			if (entry.getKey().isLoop()) continue;
			int first = entry.getValue().get(0).getClusterId();
			for (E e : entry.getValue()) {
				union(parents, first, e.getClusterId());
			}
		}

		for (List<E> group : groups.values()) {
			for (E e : group) {
				int clusterId = e.getClusterId();
				int root = find(parents, clusterId);
				if (root != clusterId) {
					e.setClusterId(root);
				}
				// we keep track of what's merged: note this also handles clusters that had been merged in previous rounds
				SortedSet<Integer> members = mergedInterfClusterIds.computeIfAbsent(root, k -> new TreeSet<>());
				members.add(root);
				members.add(clusterId);
				if (root != clusterId && mergedInterfClusterIds.containsKey(clusterId)) {
					members.addAll(mergedInterfClusterIds.get(clusterId));
				}
			}
		}
		// the ones that aren't roots anymore, or that have disappeared from the graph, are removed
		Set<Integer> roots = new HashSet<>();
		for (List<E> group : groups.values()) {
			for (E e : group) roots.add(e.getClusterId());
		}
		mergedInterfClusterIds.keySet().retainAll(roots);

		// 4. Finally we add 1 edge per vertex pair, translation and interface cluster

		for (Entry<EdgeKey, List<E>> entry : groups.entrySet()) {
			EdgeKey key = entry.getKey();
			Map<Integer, E> uniques = new TreeMap<>();
			for (E e : entry.getValue()) {
				E unique = uniques.get(e.getClusterId());
				if (unique == null) {
					uniques.put(e.getClusterId(), e);
				} else {
					unique.setIsInfinite(unique.isInfinite() || e.isInfinite());
					unique.setIsIsologous(unique.isIsologous() || e.isIsologous());
					// for consistency we keep the lowest interface id as the representative
					if (e.getInterfaceId() < unique.getInterfaceId()) {
						unique.setInterfaceId(e.getInterfaceId());
					}
				}
			}
			for (E e : uniques.values()) {
				contGraph.addEdge(key.s, key.t, e);
			}
		}

		setIsologousFlags(contGraph);

		logger.debug("Post-contraction graph has {} vertices and {} edges. Counts per entity: {}",
				contGraph.vertexSet().size(), contGraph.edgeSet().size(), getEntityCounts(contGraph));

		return contGraph;
	}

	/**
	 * Finds the connected components of the subgraph of given graph that has only edges of the given interface cluster,
	 * filling the reps map with the representative of each vertex (the vertex itself if not touched by any edge
	 * of the given interface cluster) and the offsets map with the translation of each vertex with respect to its representative.
	 * @param graph
	 * @param interfClusterId
	 * @param reps
	 * @param offsets
	 * @return true if all components are finite, false if any of the components is infinite, i.e. it contains
	 * a cycle with non-zero net translation
	 */
	private boolean findComponents(UndirectedGraph<V, E> graph, int interfClusterId, Map<V, V> reps, Map<V, Point3i> offsets) {

		for (V start : graph.vertexSet()) {

			if (offsets.containsKey(start)) continue;

			// breadth first traversal through edges of the given cluster, adding up translations
			List<V> component = new ArrayList<>();
			Deque<V> queue = new ArrayDeque<>();
			offsets.put(start, new Point3i(0, 0, 0));
			queue.add(start);

			while (!queue.isEmpty()) {
				V u = queue.poll();
				component.add(u);

				for (E e : graph.edgesOf(u)) {
					if (e.getClusterId() != interfClusterId) continue;

					V s = graph.getEdgeSource(e);
					V t = graph.getEdgeTarget(e);

					Point3i offset = new Point3i(offsets.get(u));
					V w;
					if (s.equals(t)) {
						if (!e.getXtalTrans().equals(ZERO)) return false;
						continue;
					} else if (u.equals(s)) {
						w = t;
						offset.add(e.getXtalTrans());
					} else {
						w = s;
						offset.sub(e.getXtalTrans());
					}

					Point3i existing = offsets.get(w);
					if (existing == null) {
						offsets.put(w, offset);
						queue.add(w);
					} else if (!existing.equals(offset)) {
						// 2 paths to the same vertex with different translations: a non-closed cycle
						return false;
					}
				}
			}

			// the representative is chosen deterministically. Offsets must then be expressed with respect to it
			V rep = component.get(0);
			for (V v : component) {
				if (vertexComparator.compare(v, rep) < 0) rep = v;
			}
			Point3i repOffset = new Point3i(offsets.get(rep));
			for (V v : component) {
				reps.put(v, rep);
				offsets.get(v).sub(repOffset);
			}
		}

		return true;
	}

	/**
	 * Sets the isologous flag of all edges in the contracted graph: an edge of a contracted graph is isologous
	 * if any of the edges it comes from was isologous, or if it is the only edge of its interface cluster
	 * in its 2 vertices (i.e. the 2 vertices are related by a 2-fold, or it does not propagate at all).
	 * @param contGraph
	 */
	private void setIsologousFlags(UndirectedGraph<V, E> contGraph) {
		for (E e : contGraph.edgeSet()) {
			V s = contGraph.getEdgeSource(e);
			V t = contGraph.getEdgeTarget(e);
			if (s.equals(t) || e.isIsologous()) continue;

			if (countEdges(contGraph, s, e.getClusterId()) == 1 && countEdges(contGraph, t, e.getClusterId()) == 1) {
				e.setIsIsologous(true);
			}
		}
	}

	private int countEdges(UndirectedGraph<V, E> graph, V v, int interfClusterId) {
		int count = 0;
		for (E e : graph.edgesOf(v)) {
			if (e.getClusterId() == interfClusterId) count++;
		}
		return count;
	}

	/**
	 * Creates a new edge with the properties of given one but with the given translation.
	 * The given edge stays untouched.
	 * @param e
	 * @param trans
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private E copyEdge(E e, Point3i trans) {
		// we create a new edge so that we can keep the original edges from the original graph intact
		InterfaceEdge newEdge;
		if (e instanceof InterfaceEdge) {
			newEdge = new InterfaceEdge((InterfaceEdge) e);
		} else {
			newEdge = new InterfaceEdge();
			newEdge.setInterfaceId(e.getInterfaceId());
			newEdge.setClusterId(e.getClusterId());
			newEdge.setIsInfinite(e.isInfinite());
			newEdge.setIsIsologous(e.isIsologous());
		}
		newEdge.setXtalTrans(trans);
		// the casting should be safe
		return (E) newEdge;
	}

	/**
	 * A canonical sign for translations of loop edges: first non-zero coordinate must be positive
	 * @param trans
	 * @return
	 */
	private static boolean isCanonical(Point3i trans) {
		if (trans.x != 0) return trans.x > 0;
		if (trans.y != 0) return trans.y > 0;
		return trans.z >= 0;
	}

	private static int find(Map<Integer, Integer> parents, int id) {
		Integer parent = parents.get(id);
		if (parent == null || parent == id) return id;
		int root = find(parents, parent);
		parents.put(id, root);
		return root;
	}

	private static void union(Map<Integer, Integer> parents, int id1, int id2) {
		int root1 = find(parents, id1);
		int root2 = find(parents, id2);
		if (root1 == root2) return;
		// the lowest id (largest interface) is the representative
		parents.put(Math.max(root1, root2), Math.min(root1, root2));
	}

	public UndirectedGraph<V, E> getOriginalGraph() {
		return g;
	}

	public UndirectedGraph<V, E> getContractedGraph() {
		return cg;
	}

	/**
	 * Returns the replacement vertex given a contracted vertex
	 * @param v
	 * @return the replacement for v, or v if it was not contracted
	 */
	public V getContractedVertex(V v) {
		if(!contractedVertices.containsKey(v)) {
			return v; //replaced with itself
		}
		V replacement = contractedVertices.get(v);
		// Check for additional replacements recursively
		if( replacement != v) {
			V replacement2 = getContractedVertex(replacement);
			if( replacement != replacement2) {
				// shorten search path for future searches
				replacement = replacement2;
				contractedVertices.put(v,replacement);
			}
		}
		return replacement;
	}

	/**
	 * Returns the set of removed vertices
	 * @return
	 */
	public Set<V> getContractedVertices() {
		return contractedVertices.keySet();
	}

	public Set<Integer> getContractedEntityIds() {
		Set<Integer> set = new TreeSet<Integer>();

		for (V v: contractedVertices.keySet()) {
			set.add(v.getEntityId());
		}
		return set;
	}

	/**
	 * Returns the interface cluster ids that were contracted, i.e. those that are engaged
	 * in every vertex of the contracted graph.
	 * @return
	 */
	public Set<Integer> getContractedInterfClusterIds() {
		return contractedInterfClusterIds;
	}

	/**
	 * Given an interface cluster id present in the contracted graph, returns all the interface cluster ids
	 * of the original graph that it represents (including itself): they all join the same pairs of contracted vertices.
	 * @param interfClusterId
	 * @return
	 */
	public SortedSet<Integer> getMergedInterfClusterIds(int interfClusterId) {
		SortedSet<Integer> members = mergedInterfClusterIds.get(interfClusterId);
		if (members == null) {
			members = new TreeSet<>();
			members.add(interfClusterId);
		}
		return members;
	}

	/**
	 * Returns the interface cluster ids of the original graph that are neither contracted nor
	 * represented in the contracted graph: they join vertices that ended up in the same contracted vertex and thus
	 * are engaged whenever the contracted interface clusters are.
	 * @return
	 */
	public SortedSet<Integer> getInducedInterfClusterIds() {
		SortedSet<Integer> induced = new TreeSet<>(GraphUtils.getDistinctInterfaceClusters(g));
		induced.removeAll(contractedInterfClusterIds);
		if (cg != null) {
			for (int interfClusterId : GraphUtils.getDistinctInterfaceClusters(cg)) {
				induced.removeAll(getMergedInterfClusterIds(interfClusterId));
			}
		}
		return induced;
	}

	/**
	 * Converts a set of engaged interface cluster ids of the contracted graph into the corresponding set of
	 * engaged interface cluster ids of the original graph: the contracted ones, plus those induced by the contraction,
	 * plus the given ones together with all others that were merged to them.
	 * @param engagedInContracted
	 * @return
	 */
	public SortedSet<Integer> getOriginalInterfClusterIds(Collection<Integer> engagedInContracted) {
		SortedSet<Integer> ids = new TreeSet<>(contractedInterfClusterIds);
		ids.addAll(getInducedInterfClusterIds());
		for (int interfClusterId : engagedInContracted) {
			ids.addAll(getMergedInterfClusterIds(interfClusterId));
		}
		return ids;
	}

	/**
	 * Gets a map of entity ids to counts of vertices of that entity for the given graph
	 * @param g
	 * @return
	 */
	private static <V extends ChainVertexInterface, E extends InterfaceEdgeInterface> Map<Integer,Integer> getEntityCounts(UndirectedGraph<V, E> g) {
		Map<Integer, Integer> counts = new TreeMap<Integer,Integer>();
		for (V v:g.vertexSet()) {
			counts.merge(v.getEntityId(), 1, Integer::sum);
		}
		return counts;
	}

	/**
	 * The identity of an edge in the lattice: the 2 vertices it joins and the translation between them.
	 */
	private class EdgeKey {
		final V s;
		final V t;
		final Point3i trans;

		EdgeKey(V s, V t, Point3i trans) {
			this.s = s;
			this.t = t;
			this.trans = trans;
		}

		boolean isLoop() {
			return s.equals(t);
		}

		@Override
		public int hashCode() {
			return 31 * (31 * s.hashCode() + t.hashCode()) + trans.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof GraphContractor.EdgeKey)) return false;
			@SuppressWarnings("unchecked")
			EdgeKey other = (EdgeKey) o;
			return s.equals(other.s) && t.equals(other.t) && trans.equals(other.trans);
		}
	}
}
