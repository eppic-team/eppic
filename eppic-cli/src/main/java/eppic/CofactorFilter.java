package eppic;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Element;
import org.biojava.nbio.structure.Group;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Decides which non-polymer groups are cofactors, i.e. groups whose burial of
 * protein residues should be taken into account (see {@link CofactorBurial}).
 * <p>
 * A non-water group with more than one non-Hydrogen atom is a cofactor if any of these rules applies:
 * <ol>
 * <li>{@link Rule#CURATED}: its chemical component is in the PDBe cofactor list, of any size.
 * The list is the classification of organic cofactors by PDBe, derived from the CoFactor database
 * of the Thornton lab. It is bundled in {@value #PDBE_COFACTORS_RESOURCE}, downloaded from
 * https://www.ebi.ac.uk/pdbe/api/pdb/compound/cofactors on 2026-11-26.</li>
 * <li>{@link Rule#METAL}: it contains metals, all of them metals used by living organisms
 * ({@link #BIOLOGICAL_METALS}) and it has at least the minimum number of atoms for a metal cofactor.
 * This excludes the heavy atom compounds used for phasing (Hg, Pt, Au, Gd...) and the metal ions.</li>
 * <li>{@link Rule#SIZE}: it has at least the minimum number of atoms for a cofactor.</li>
 * </ol>
 * Polyoxometalates, used as crystallization additives, are never cofactors
 * (see {@link #isPolyoxometalateLike(Group)}).
 */
public class CofactorFilter {

	public enum Rule { CURATED, METAL, SIZE }

	public static final String PDBE_COFACTORS_RESOURCE = "/eppic/pdbe_cofactors.json";

	/**
	 * Metals that are part of biological cofactors. Besides the common ones: W (archaeal and bacterial
	 * oxidoreductases), V (vanadium nitrogenase, haloperoxidases), Cd (diatom carbonic anhydrase) and
	 * the light lanthanides (XoxF methanol dehydrogenases, lanmodulin).
	 */
	public static final Set<Element> BIOLOGICAL_METALS = Collections.unmodifiableSet(EnumSet.of(
			Element.Na, Element.K, Element.Mg, Element.Ca,
			Element.V, Element.Mn, Element.Fe, Element.Co, Element.Ni, Element.Cu, Element.Zn,
			Element.Mo, Element.Cd, Element.W,
			Element.La, Element.Ce, Element.Pr, Element.Nd));

	/**
	 * The addenda metals of polyoxometalates
	 */
	private static final Set<Element> POLYOXOMETALATE_METALS = EnumSet.of(Element.V, Element.Mo, Element.W);

	private static Set<String> pdbeCofactors;

	private final int minSizeCofactor;
	private final int minSizeMetalCofactor;

	/**
	 * @param minSizeCofactor minimum number of non-Hydrogen atoms for any group to be a cofactor ({@link Rule#SIZE}).
	 * If negative, no group is a cofactor
	 * @param minSizeMetalCofactor minimum number of non-Hydrogen atoms for a group with biological metals
	 * to be a cofactor ({@link Rule#METAL})
	 */
	public CofactorFilter(int minSizeCofactor, int minSizeMetalCofactor) {
		this.minSizeCofactor = minSizeCofactor;
		this.minSizeMetalCofactor = minSizeMetalCofactor;
	}

	/**
	 * Whether no group can be a cofactor with this filter.
	 * @return
	 */
	public boolean isDisabled() {
		return minSizeCofactor < 0;
	}

	/**
	 * Returns the rule by which the group is a cofactor, or null if it is not one.
	 * @param g a non-polymer group
	 * @return
	 */
	public Rule getCofactorRule(Group g) {
		if (isDisabled() || g.isWater()) return null;

		int size = 0;
		for (Atom a : g.getAtoms()) {
			if (a.getElement() != Element.H) size++;
		}
		if (size <= 1) return null;

		if (getPdbeCofactors().contains(g.getPDBName())) return Rule.CURATED;

		if (isPolyoxometalateLike(g)) return null;

		if (size >= minSizeMetalCofactor && hasOnlyBiologicalMetals(g)) return Rule.METAL;

		if (size >= minSizeCofactor) return Rule.SIZE;

		return null;
	}

	/**
	 * Whether the group has at least one metal and all its metals are in {@link #BIOLOGICAL_METALS}.
	 * @param g
	 * @return
	 */
	static boolean hasOnlyBiologicalMetals(Group g) {
		boolean hasMetal = false;
		for (Atom a : g.getAtoms()) {
			Element e = a.getElement();
			if (!e.isMetal()) continue;
			if (!BIOLOGICAL_METALS.contains(e)) return false;
			hasMetal = true;
		}
		return hasMetal;
	}

	/**
	 * Whether the group looks like a polyoxometalate (a crystallization additive): it has V, Mo or W,
	 * and no Carbon or Sulfur. The biological V/Mo/W cofactors are organic (pterins) or, like FeMoco,
	 * contain Sulfur and Carbon. The Mn4CaO5 cluster of photosystem II is not affected since it has none of those metals.
	 * @param g
	 * @return
	 */
	static boolean isPolyoxometalateLike(Group g) {
		boolean hasAddendaMetal = false;
		for (Atom a : g.getAtoms()) {
			Element e = a.getElement();
			if (e == Element.C || e == Element.S) return false;
			if (POLYOXOMETALATE_METALS.contains(e)) hasAddendaMetal = true;
		}
		return hasAddendaMetal;
	}

	/**
	 * Returns the chemical component ids of the PDBe cofactor list, read from the bundled resource.
	 * @return
	 */
	public static synchronized Set<String> getPdbeCofactors() {
		if (pdbeCofactors == null) {
			pdbeCofactors = Collections.unmodifiableSet(readPdbeCofactors());
		}
		return pdbeCofactors;
	}

	private static Set<String> readPdbeCofactors() {
		Set<String> codes = new TreeSet<>();
		try (InputStream is = CofactorFilter.class.getResourceAsStream(PDBE_COFACTORS_RESOURCE)) {
			if (is == null) throw new IOException("Could not find resource " + PDBE_COFACTORS_RESOURCE);
			// format: {"class name": [{"cofactors": ["CCD id", ...], "EC": [...]}, ...], ...}
			JsonNode root = new ObjectMapper().readTree(is);
			Iterator<Map.Entry<String, JsonNode>> classes = root.fields();
			while (classes.hasNext()) {
				for (JsonNode entry : classes.next().getValue()) {
					for (JsonNode code : entry.path("cofactors")) {
						codes.add(code.asText());
					}
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Could not read the PDBe cofactor list", e);
		}
		return codes;
	}
}
