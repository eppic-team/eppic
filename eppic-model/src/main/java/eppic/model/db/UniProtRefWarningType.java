package eppic.model.db;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

/**
 * The type of a {@link UniProtRefWarningDB}, i.e. the reason why a chain could not be (fully) analysed
 * evolutionarily.
 * <p>
 * Stored by name. New constants can be added at any time: readers that don't know a constant
 * will read it as {@link #UNKNOWN}. Do not rename or remove constants, since they are persisted.
 *
 * @since 3.5.7
 */
public enum UniProtRefWarningType {

	/** The chain is not a protein */
	NOT_PROTEIN,
	/** The chain is a peptide (below the peptide length cutoff) */
	PEPTIDE,
	/** The sequence is composed only of unknown or non-standard amino acids */
	ALL_UNKNOWN_RESIDUES,
	/** The identity of the PDB sequence to the UniProt reference is below the threshold */
	LOW_REFERENCE_IDENTITY,
	/** The homologs search interval is below the peptide length cutoff */
	SEARCH_INTERVAL_TOO_SHORT,
	/** The SIFTS mapping contains negative length intervals */
	SIFTS_NEGATIVE_INTERVALS,
	/** The SIFTS mapping contains 0 or negative coordinates */
	SIFTS_NON_POSITIVE_COORDINATES,
	/** More than one UniProt id maps to the chain. The ids are in uniProtIds */
	CHIMERIC,
	/** More than one segment of a single UniProt id maps to the chain (insertions/deletions). The id is in uniProtIds */
	MULTIPLE_UNIPROT_SEGMENTS,
	/** A type not known to this version of the model */
	@JsonEnumDefaultValue
	UNKNOWN
}
