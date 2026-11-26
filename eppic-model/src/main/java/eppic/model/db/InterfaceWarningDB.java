package eppic.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;

import java.io.Serializable;
import java.util.List;

/**
 * A warning about an interface.
 * <p>
 * The text is always present and is the human-readable form of the warning. Since 3.5.7 there is
 * also structured data (type, side, count, atom pairs, residues). The structured fields are absent
 * in data produced by earlier versions, so consumers must treat all of them as optional and
 * fall back to the text.
 */
public class InterfaceWarningDB implements Serializable {

	private static final long serialVersionUID = 1L;

	private String text;

	/**
	 * The type of warning, null in data produced before 3.5.7
	 * @since 3.5.7
	 */
	private InterfaceWarningType type;

	/**
	 * The side of the interface the warning refers to (1 or 2), null if it refers to the whole interface
	 * @since 3.5.7
	 */
	private Integer molecId;

	/**
	 * The total count of items (atom pairs or residues) the warning is about. Can be larger than
	 * the size of {@link #atomPairs} or {@link #residues}, which can be capped
	 * @since 3.5.7
	 */
	private Integer count;

	/**
	 * The atom pairs the warning is about (for clashes, close interactions, disulfides)
	 * @since 3.5.7
	 */
	private List<AtomPairDB> atomPairs;

	/**
	 * The residues the warning is about (for reference mismatches)
	 * @since 3.5.7
	 */
	private List<ResidueRefDB> residues;

	@JsonBackReference(value = "interfaceWarnings-ref")
	private InterfaceDB interfaceItem;

	public InterfaceWarningDB() {

	}

	public InterfaceWarningDB(InterfaceWarningType type, String text) {
		this.type = type;
		this.text = text;
	}

	public void setInterfaceItem(InterfaceDB interfaceItem) {
		this.interfaceItem = interfaceItem;
	}

	public InterfaceDB getInterfaceItem() {
		return interfaceItem;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public InterfaceWarningType getType() {
		return type;
	}

	public void setType(InterfaceWarningType type) {
		this.type = type;
	}

	public Integer getMolecId() {
		return molecId;
	}

	public void setMolecId(Integer molecId) {
		this.molecId = molecId;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public List<AtomPairDB> getAtomPairs() {
		return atomPairs;
	}

	public void setAtomPairs(List<AtomPairDB> atomPairs) {
		this.atomPairs = atomPairs;
	}

	public List<ResidueRefDB> getResidues() {
		return residues;
	}

	public void setResidues(List<ResidueRefDB> residues) {
		this.residues = residues;
	}

}
