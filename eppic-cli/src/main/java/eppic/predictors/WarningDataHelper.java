package eppic.predictors;

import java.util.ArrayList;
import java.util.List;

import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.contact.AtomContact;

import eppic.InterfaceEvolContext;
import eppic.model.db.AtomPairDB;
import eppic.model.db.InterfaceWarningDB;
import eppic.model.db.InterfaceWarningType;
import eppic.model.db.ResidueRefDB;

/**
 * Static helpers to convert BioJava objects into the structured data of warnings.
 *
 * @since 3.5.7
 */
public class WarningDataHelper {

	/**
	 * Maximum number of atom pairs or residues stored in a single warning. The full count is always stored.
	 */
	public static final int MAX_ITEMS_PER_WARNING = 100;

	private WarningDataHelper() {

	}

	/**
	 * Converts an internal molecule id ({@link InterfaceEvolContext#FIRST} or {@link InterfaceEvolContext#SECOND})
	 * to the interface side as stored in warnings: 1 or 2, matching the interface's chain1 and chain2
	 * @param molecId the internal molecule id
	 * @return 1 or 2
	 */
	public static int getSide(int molecId) {
		return molecId == InterfaceEvolContext.FIRST ? 1 : 2;
	}

	/**
	 * Creates a warning about atom pairs, with the full count and the (capped) atom pairs
	 * @param type the warning type
	 * @param text the human-readable warning
	 * @param pairs the atom contacts
	 * @return the warning
	 */
	public static InterfaceWarningDB createAtomPairsWarning(InterfaceWarningType type, String text, List<AtomContact> pairs) {
		InterfaceWarningDB warning = new InterfaceWarningDB(type, text);
		warning.setCount(pairs.size());
		warning.setAtomPairs(getAtomPairs(pairs));
		return warning;
	}

	/**
	 * Creates a warning about residues of one side of the interface, with the full count and the (capped) residues
	 * @param type the warning type
	 * @param text the human-readable warning
	 * @param molecId the internal molecule id ({@link InterfaceEvolContext#FIRST} or {@link InterfaceEvolContext#SECOND})
	 * @param residues the residues
	 * @return the warning
	 */
	public static InterfaceWarningDB createResiduesWarning(InterfaceWarningType type, String text, int molecId, List<Group> residues) {
		InterfaceWarningDB warning = new InterfaceWarningDB(type, text);
		warning.setMolecId(getSide(molecId));
		warning.setCount(residues.size());
		warning.setResidues(getResidueRefs(residues));
		return warning;
	}

	/**
	 * Gets a residue reference for the given group. The chain id is the chain name, as in the interface chain ids.
	 * @param g the group
	 * @return the residue reference
	 */
	public static ResidueRefDB getResidueRef(Group g) {
		Chain chain = g.getChain();
		String chainId = null;
		Integer residueNumber = null;
		if (chain != null) {
			chainId = chain.getName();
			if (chain.getEntityInfo() != null) {
				residueNumber = chain.getEntityInfo().getAlignedResIndex(g, chain);
			}
		}
		String pdbResidueNumber = g.getResidueNumber() == null ? null : g.getResidueNumber().toString();
		return new ResidueRefDB(chainId, residueNumber, pdbResidueNumber, g.getPDBName());
	}

	/**
	 * Gets the atom pair data for the given contact. The first atom is expected to be in the first side of the interface.
	 * @param contact the contact
	 * @return the atom pair
	 */
	public static AtomPairDB getAtomPair(AtomContact contact) {
		Atom first = contact.getPair().getFirst();
		Atom second = contact.getPair().getSecond();
		AtomPairDB pair = new AtomPairDB();
		pair.setFirstResidue(getResidueRef(first.getGroup()));
		pair.setSecondResidue(getResidueRef(second.getGroup()));
		pair.setFirstAtom(first.getName());
		pair.setSecondAtom(second.getName());
		pair.setDistance(contact.getDistance());
		return pair;
	}

	/**
	 * Gets the atom pairs data for the given contacts, capped at {@link #MAX_ITEMS_PER_WARNING}
	 * @param contacts the contacts
	 * @return the atom pairs
	 */
	public static List<AtomPairDB> getAtomPairs(List<AtomContact> contacts) {
		List<AtomPairDB> pairs = new ArrayList<>();
		for (AtomContact contact : contacts) {
			if (pairs.size() >= MAX_ITEMS_PER_WARNING) break;
			pairs.add(getAtomPair(contact));
		}
		return pairs;
	}

	/**
	 * Gets the residue references for the given groups, capped at {@link #MAX_ITEMS_PER_WARNING}
	 * @param groups the groups
	 * @return the residue references
	 */
	public static List<ResidueRefDB> getResidueRefs(List<Group> groups) {
		List<ResidueRefDB> refs = new ArrayList<>();
		for (Group g : groups) {
			if (refs.size() >= MAX_ITEMS_PER_WARNING) break;
			refs.add(getResidueRef(g));
		}
		return refs;
	}
}
