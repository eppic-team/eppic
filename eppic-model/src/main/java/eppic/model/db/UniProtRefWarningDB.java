package eppic.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;
import java.io.Serializable;
import java.util.List;

/**
 * A warning about the UniProt reference / evolutionary analysis of a chain cluster.
 * <p>
 * The text is always present and is the human-readable form of the warning. Since 3.5.7 there is
 * also a type and optional details. The structured fields are absent in data produced by earlier
 * versions, so consumers must treat them as optional and fall back to the text.
 */
public class UniProtRefWarningDB implements Serializable {

	private static final long serialVersionUID = 1L;

	private String text;

	/**
	 * The type of warning, null in data produced before 3.5.7
	 * @since 3.5.7
	 */
	private UniProtRefWarningType type;

	/**
	 * The UniProt ids involved, for {@link UniProtRefWarningType#CHIMERIC},
	 * {@link UniProtRefWarningType#MULTIPLE_UNIPROT_SEGMENTS} and {@link UniProtRefWarningType#LOW_REFERENCE_IDENTITY}
	 * @since 3.5.7
	 */
	private List<String> uniProtIds;

	@JsonBackReference
	private ChainClusterDB chainCluster;

	public UniProtRefWarningDB() {

	}

	public UniProtRefWarningDB(UniProtRefWarningType type, String text) {
		this.type = type;
		this.text = text;
	}

	public void setChainCluster(ChainClusterDB chainCluster) {
		this.chainCluster = chainCluster;
	}

	public ChainClusterDB getChainCluster() {
		return chainCluster;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public UniProtRefWarningType getType() {
		return type;
	}

	public void setType(UniProtRefWarningType type) {
		this.type = type;
	}

	public List<String> getUniProtIds() {
		return uniProtIds;
	}

	public void setUniProtIds(List<String> uniProtIds) {
		this.uniProtIds = uniProtIds;
	}

}
