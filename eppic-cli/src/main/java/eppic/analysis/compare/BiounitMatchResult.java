package eppic.analysis.compare;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import eppic.assembly.Assembly;

/**
 * The result of matching one PDB-annotated biological assembly to the valid assemblies
 * enumerated by EPPIC.
 *
 * @author Jose Duarte
 */
public class BiounitMatchResult {

	/**
	 * The kind of match found.
	 */
	public enum MatchType {
		/** The biounit graph is isomorphic (entity- and interface-cluster-labelled) to the assembly */
		EXACT,
		/**
		 * The biounit is isomorphic to the assembly once some redundant biounit edges are removed
		 * (contacts whose interface cluster is not engaged in the assembly, or significant contacts that match no
		 * EPPIC interface cluster). Removing them does not change the connectivity of the biounit.
		 */
		EXTRA_EDGES,
		/** The biounit is a proper (induced) part of the assembly, e.g. the PDB annotates a partial assembly */
		SUBASSEMBLY,
		/** Only produced by the operator-based matcher: the "parent" fallback match (see issue #47) */
		PARENT,
		/** No valid assembly matches */
		NONE,
		/** Matching not attempted, e.g. the biounit is too large */
		SKIPPED;

		public boolean isMatch() {
			return this == EXACT || this == EXTRA_EDGES || this == SUBASSEMBLY || this == PARENT;
		}
	}

	private MatchType type;
	private Assembly assembly;
	private Set<Integer> clusterIds;
	private String reason;

	// stats about the biounit graph (graph matcher only, -1 if not available)
	private int numNodes = -1;
	private int numLabelledEdges = -1;
	private int numUnlabelledEdges = -1;
	private int numMinorContacts = -1;
	private int numRemovedEdges = -1;
	private String biounitStoichiometry;
	private long timeMs;

	public BiounitMatchResult(MatchType type, Assembly assembly, Set<Integer> clusterIds) {
		this.type = type;
		this.assembly = assembly;
		this.clusterIds = clusterIds == null ? Collections.emptySet() : new TreeSet<>(clusterIds);
	}

	public static BiounitMatchResult skipped(String reason) {
		BiounitMatchResult r = new BiounitMatchResult(MatchType.SKIPPED, null, null);
		r.setReason(reason);
		return r;
	}

	public MatchType getType() {
		return type;
	}

	/**
	 * @return the matching valid assembly, or null if no match
	 */
	public Assembly getAssembly() {
		return assembly;
	}

	/**
	 * @return the EPPIC interface cluster ids that the biounit's contacts map to
	 */
	public Set<Integer> getClusterIds() {
		return clusterIds;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public int getNumNodes() {
		return numNodes;
	}

	public void setNumNodes(int numNodes) {
		this.numNodes = numNodes;
	}

	public int getNumLabelledEdges() {
		return numLabelledEdges;
	}

	public void setNumLabelledEdges(int numLabelledEdges) {
		this.numLabelledEdges = numLabelledEdges;
	}

	/**
	 * @return number of biounit contacts above the area cutoff that don't match any EPPIC interface cluster
	 */
	public int getNumUnlabelledEdges() {
		return numUnlabelledEdges;
	}

	public void setNumUnlabelledEdges(int numUnlabelledEdges) {
		this.numUnlabelledEdges = numUnlabelledEdges;
	}

	/**
	 * @return number of biounit contacts below the area cutoff that don't match any EPPIC interface cluster (ignored)
	 */
	public int getNumMinorContacts() {
		return numMinorContacts;
	}

	public void setNumMinorContacts(int numMinorContacts) {
		this.numMinorContacts = numMinorContacts;
	}

	/**
	 * @return number of biounit edges removed to obtain the match (see {@link MatchType#EXTRA_EDGES})
	 */
	public int getNumRemovedEdges() {
		return numRemovedEdges;
	}

	public void setNumRemovedEdges(int numRemovedEdges) {
		this.numRemovedEdges = numRemovedEdges;
	}

	/**
	 * @return the biounit stoichiometry in terms of entity ids, e.g. "1:2,2:2"
	 */
	public String getBiounitStoichiometry() {
		return biounitStoichiometry;
	}

	public void setBiounitStoichiometry(String biounitStoichiometry) {
		this.biounitStoichiometry = biounitStoichiometry;
	}

	public long getTimeMs() {
		return timeMs;
	}

	public void setTimeMs(long timeMs) {
		this.timeMs = timeMs;
	}

	@Override
	public String toString() {
		return type + (assembly == null ? "" : " assembly " + assembly.getId() + " " + assembly) +
				" clusters " + clusterIds + (reason == null ? "" : " (" + reason + ")");
	}
}
