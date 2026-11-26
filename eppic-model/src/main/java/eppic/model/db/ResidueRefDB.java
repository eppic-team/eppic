package eppic.model.db;

import java.io.Serializable;

/**
 * A reference to a residue, used in structured warnings.
 * The numbering conventions are the same as in {@link ResidueInfoDB}.
 *
 * @since 3.5.7
 */
public class ResidueRefDB implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The chain identifier (as in the interface chain1/chain2)
	 */
	private String chainId;

	/**
	 * The residue index in the SEQRES (same as {@link ResidueInfoDB#getResidueNumber()}
	 * and {@link ContactDB#getFirstResNumber()}), or null if not available
	 */
	private Integer residueNumber;

	/**
	 * The author residue number, including insertion code, e.g. "52A"
	 * (same as {@link ResidueInfoDB#getPdbResidueNumber()})
	 */
	private String pdbResidueNumber;

	/**
	 * The 3-letter residue type
	 */
	private String residueType;

	public ResidueRefDB() {

	}

	public ResidueRefDB(String chainId, Integer residueNumber, String pdbResidueNumber, String residueType) {
		this.chainId = chainId;
		this.residueNumber = residueNumber;
		this.pdbResidueNumber = pdbResidueNumber;
		this.residueType = residueType;
	}

	public String getChainId() {
		return chainId;
	}

	public void setChainId(String chainId) {
		this.chainId = chainId;
	}

	public Integer getResidueNumber() {
		return residueNumber;
	}

	public void setResidueNumber(Integer residueNumber) {
		this.residueNumber = residueNumber;
	}

	public String getPdbResidueNumber() {
		return pdbResidueNumber;
	}

	public void setPdbResidueNumber(String pdbResidueNumber) {
		this.pdbResidueNumber = pdbResidueNumber;
	}

	public String getResidueType() {
		return residueType;
	}

	public void setResidueType(String residueType) {
		this.residueType = residueType;
	}
}
