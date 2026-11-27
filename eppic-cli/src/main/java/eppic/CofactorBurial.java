package eppic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Element;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.ResidueNumber;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.asa.AsaCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The residues of each polymer chain that are buried by the chain's own cofactors
 * (large non-polymer groups such as heme).
 * <p>
 * The interface ASAs from BioJava are calculated with polymer atoms only: since the
 * BioJava 5 data model, non-polymer groups live in separate chains and are not part
 * of the interface molecules, so a residue that lines a heme pocket is counted as
 * surface. This class recalculates, per chain, the ASA with and without the cofactors
 * and treats the residues that the cofactors bury in the same way that a protein-protein
 * interface core would be treated: a residue is buried by cofactors if, in the chain
 * without cofactors, it is surface (ASA above the minimum ASA for surface) and either
 * <ul>
 * <li>it is not surface any more once the cofactors are present, or</li>
 * <li>the fraction of its ASA buried by the cofactors is at least the burial cutoff
 * (same bsa/asa criterion as for interface core residues).</li>
 * </ul>
 * Those residues are then excluded from the surface used for the background distribution
 * of the core-surface score.
 */
public class CofactorBurial implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(CofactorBurial.class);

	/**
	 * Original chain name to keys (see {@link #key(ResidueNumber)}) of residues buried by cofactors
	 */
	private final Map<String, Set<String>> buriedResidues;

	/**
	 * Chain name (possibly an NCS-expanded one) to original chain name, can be null if no NCS expansion
	 */
	private final Map<String, String> chainOrigNames;

	private CofactorBurial(Map<String, Set<String>> buriedResidues, Map<String, String> chainOrigNames) {
		this.buriedResidues = buriedResidues;
		this.chainOrigNames = chainOrigNames;
	}

	/**
	 * Returns an instance where no residue is buried by cofactors.
	 * @return
	 */
	public static CofactorBurial none() {
		return new CofactorBurial(new HashMap<>(), null);
	}

	/**
	 * Calculates the residues buried by cofactors for the given polymer chains of the structure.
	 * @param structure the structure, with its non-polymer chains
	 * @param chainNames the author names of the polymer chains to calculate (possibly NCS-expanded names)
	 * @param chainOrigNames map of NCS-expanded chain names to original chain names, or null if no NCS expansion.
	 * NCS copies have the same cofactors as their original chain, so only the original chains are calculated
	 * @param cofactorFilter decides which non-polymer groups are cofactors
	 * @param minAsaForSurface the minimum ASA for a residue to be considered surface
	 * @param burialCutoff the minimum fraction of a residue's ASA buried by cofactors for the residue to be considered buried
	 * @param nSpherePoints number of sphere points for the ASA calculation
	 * @param nThreads number of threads for the ASA calculation
	 * @return
	 */
	public static CofactorBurial calculate(Structure structure, Collection<String> chainNames, Map<String, String> chainOrigNames,
										   CofactorFilter cofactorFilter, double minAsaForSurface, double burialCutoff,
										   int nSpherePoints, int nThreads) {

		Map<String, Set<String>> buriedResidues = new HashMap<>();

		if (cofactorFilter.isDisabled()) {
			LOGGER.info("Cofactors are ignored for ASA calculations");
			return new CofactorBurial(buriedResidues, chainOrigNames);
		}

		Set<String> origChainNames = new TreeSet<>();
		for (String chainName : chainNames) {
			origChainNames.add(getOrigName(chainName, chainOrigNames));
		}

		for (String chainName : origChainNames) {
			Chain polyChain = structure.getPolyChainByPDB(chainName);
			if (polyChain == null) {
				LOGGER.warn("Could not find polymer chain {} to calculate burial by cofactors", chainName);
				continue;
			}

			Map<Group, CofactorFilter.Rule> cofactorRules = new LinkedHashMap<>();
			for (Chain nonPolyChain : structure.getNonPolyChainsByPDB(chainName)) {
				for (Group g : nonPolyChain.getAtomGroups()) {
					CofactorFilter.Rule rule = cofactorFilter.getCofactorRule(g);
					if (rule != null) cofactorRules.put(g, rule);
				}
			}
			List<Group> cofactors = new ArrayList<>(cofactorRules.keySet());

			if (cofactors.isEmpty()) continue;

			Set<String> buried = calcBuriedResidues(polyChain, cofactors, minAsaForSurface, burialCutoff, nSpherePoints, nThreads);
			buriedResidues.put(chainName, buried);

			LOGGER.info("Chain {}: {} cofactor(s) ({}) bury {} residue(s) that will not be considered surface",
					chainName, cofactors.size(), getCofactorsString(cofactorRules), buried.size());
			if (!buried.isEmpty())
				LOGGER.info("Chain {}: residues buried by cofactors: {}", chainName, String.join(" ", buried));
		}

		return new CofactorBurial(buriedResidues, chainOrigNames);
	}

	private static Set<String> calcBuriedResidues(Chain polyChain, List<Group> cofactors, double minAsaForSurface, double burialCutoff,
												  int nSpherePoints, int nThreads) {

		// the residue atoms come first, in the same order in both calculations, so that the atom ASAs can be compared
		Map<Group, List<Atom>> residueAtoms = new LinkedHashMap<>();
		List<Atom> atomsNoCofactors = new ArrayList<>();
		for (Group g : polyChain.getAtomGroups()) {
			List<Atom> atoms = getNonHAtoms(g);
			if (atoms.isEmpty()) continue;
			residueAtoms.put(g, atoms);
			atomsNoCofactors.addAll(atoms);
		}

		List<Atom> atomsWithCofactors = new ArrayList<>(atomsNoCofactors);
		for (Group cofactor : cofactors) {
			atomsWithCofactors.addAll(getNonHAtoms(cofactor));
		}

		double[] asasNoCofactors = calcAsas(atomsNoCofactors, nSpherePoints, nThreads);
		double[] asasWithCofactors = calcAsas(atomsWithCofactors, nSpherePoints, nThreads);

		// a set ordered by residue position (insertion order of the poly chain groups)
		Set<String> buried = new LinkedHashSet<>();
		int i = 0;
		for (Map.Entry<Group, List<Atom>> entry : residueAtoms.entrySet()) {
			double asaNoCofactors = 0;
			double asaWithCofactors = 0;
			for (int j = 0; j < entry.getValue().size(); j++) {
				asaNoCofactors += asasNoCofactors[i];
				asaWithCofactors += asasWithCofactors[i];
				i++;
			}

			if (isBuriedByCofactors(asaNoCofactors, asaWithCofactors, minAsaForSurface, burialCutoff)) {
				buried.add(key(entry.getKey().getResidueNumber()));
			}
		}
		return buried;
	}

	/**
	 * Whether a residue counts as buried by cofactors, with the same criteria as for a protein-protein interface:
	 * it must be surface without the cofactors and then either stop being surface with them, or have at least
	 * burialCutoff of its ASA buried by them.
	 * @param asaNoCofactors the residue ASA without cofactors
	 * @param asaWithCofactors the residue ASA with cofactors
	 * @param minAsaForSurface the minimum ASA for a residue to be considered surface
	 * @param burialCutoff the minimum bsa/asa ratio for a residue to be considered buried
	 * @return
	 */
	static boolean isBuriedByCofactors(double asaNoCofactors, double asaWithCofactors, double minAsaForSurface, double burialCutoff) {
		if (asaNoCofactors <= minAsaForSurface) return false;
		if (asaWithCofactors <= minAsaForSurface) return true;
		double bsa = asaNoCofactors - asaWithCofactors;
		return bsa / asaNoCofactors >= burialCutoff;
	}

	private static double[] calcAsas(List<Atom> atoms, int nSpherePoints, int nThreads) {
		AsaCalculator asaCalc = new AsaCalculator(atoms.toArray(new Atom[0]),
				AsaCalculator.DEFAULT_PROBE_SIZE, nSpherePoints, nThreads);
		return asaCalc.calculateAsas();
	}

	private static List<Atom> getNonHAtoms(Group g) {
		List<Atom> atoms = new ArrayList<>();
		for (Atom a : g.getAtoms()) {
			if (a.getElement() == Element.H) continue;
			atoms.add(a);
		}
		return atoms;
	}

	private static String getCofactorsString(Map<Group, CofactorFilter.Rule> cofactorRules) {
		List<String> names = new ArrayList<>();
		for (Map.Entry<Group, CofactorFilter.Rule> entry : cofactorRules.entrySet()) {
			Group g = entry.getKey();
			names.add(g.getPDBName() + "-" + g.getResidueNumber().toString() + " by " + entry.getValue().name().toLowerCase());
		}
		return String.join(", ", names);
	}

	private static String getOrigName(String chainName, Map<String, String> chainOrigNames) {
		if (chainOrigNames == null) return chainName;
		String origName = chainOrigNames.get(chainName);
		return origName == null ? chainName : origName;
	}

	/**
	 * A key for the residue that does not depend on the chain name, since NCS-expanded
	 * chains have their residue numbers renamed to the expanded chain name.
	 */
	private static String key(ResidueNumber rn) {
		return rn.getSeqNum() + (rn.getInsCode() == null ? "" : rn.getInsCode().toString());
	}

	/**
	 * Whether the given residue of the given chain is buried by the chain's cofactors.
	 * @param chainName the chain name, possibly an NCS-expanded one
	 * @param rn the residue number
	 * @return
	 */
	public boolean isBuried(String chainName, ResidueNumber rn) {
		Set<String> buried = buriedResidues.get(getOrigName(chainName, chainOrigNames));
		return buried != null && buried.contains(key(rn));
	}

	/**
	 * Returns the given residues of the given chain without those buried by the chain's cofactors.
	 * @param chainName the chain name, possibly an NCS-expanded one
	 * @param residues the residues
	 * @return
	 */
	public List<Group> removeBuried(String chainName, List<Group> residues) {
		Set<String> buried = buriedResidues.get(getOrigName(chainName, chainOrigNames));
		if (buried == null || buried.isEmpty()) return residues;
		List<Group> filtered = new ArrayList<>(residues.size());
		for (Group g : residues) {
			if (!buried.contains(key(g.getResidueNumber()))) filtered.add(g);
		}
		return filtered;
	}

	/**
	 * Returns the number of residues buried by cofactors in the given chain.
	 * @param chainName the chain name, possibly an NCS-expanded one
	 * @return
	 */
	public int getNumBuried(String chainName) {
		Set<String> buried = buriedResidues.get(getOrigName(chainName, chainOrigNames));
		return buried == null ? 0 : buried.size();
	}
}
