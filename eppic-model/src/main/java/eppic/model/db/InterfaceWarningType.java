package eppic.model.db;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

/**
 * The type of an {@link InterfaceWarningDB}.
 * <p>
 * Stored by name. New constants can be added at any time: readers that don't know a constant
 * will read it as {@link #UNKNOWN}. Do not rename or remove constants, since they are persisted.
 *
 * @since 3.5.7
 */
public enum InterfaceWarningType {

	/** Atoms of the two sides closer than the clash distance. Details in atomPairs */
	CLASHES,
	/** Atoms of the two sides closely interacting (but not clashing). Details in atomPairs */
	CLOSE_INTERACTIONS,
	/** Disulfide bridges across the interface between residues matching the UniProt reference. Details in atomPairs */
	DISULFIDE_WILD_TYPE,
	/** Disulfide bridges across the interface involving residues not matching the UniProt reference. Details in atomPairs */
	DISULFIDE_ENGINEERED,
	/** Disulfide bridges across the interface, without a UniProt reference to tell whether wild-type or engineered. Details in atomPairs */
	DISULFIDE_UNDETERMINED,
	/** One side of the interface is a peptide. The side is in molecId */
	PEPTIDE,
	/** Core residues not matching the UniProt reference. Side in molecId, details in residues */
	REFERENCE_MISMATCH_CORE,
	/** Rim residues not matching the UniProt reference. Side in molecId, details in residues */
	REFERENCE_MISMATCH_RIM,
	/** Surface residues not matching the UniProt reference. Side in molecId, details in residues */
	REFERENCE_MISMATCH_SURFACE,
	/** A type not known to this version of the model */
	@JsonEnumDefaultValue
	UNKNOWN
}
