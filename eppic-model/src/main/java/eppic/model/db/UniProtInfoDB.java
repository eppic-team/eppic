package eppic.model.db;

import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name="UniProtInfo",
        indexes = @Index(name = "uniId_idx", columnList = "uniId", unique = true)) // All requests will be via uniId, so this is important
public class UniProtInfoDB implements Serializable {

    private static final long serialVersionUID = 1L;

    private String uniId;

    // As of Sept 2018, info from uniprot.org: the longest sequence is A0A1V4K6M4 at 36,991 AA
    private String sequence;

    private String firstTaxon;
    private String lastTaxon;

    public String getUniId() {
        return uniId;
    }

    public void setUniId(String uniId) {
        this.uniId = uniId;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getFirstTaxon() {
        return firstTaxon;
    }

    public void setFirstTaxon(String firstTaxon) {
        this.firstTaxon = firstTaxon;
    }

    public String getLastTaxon() {
        return lastTaxon;
    }

    public void setLastTaxon(String lastTaxon) {
        this.lastTaxon = lastTaxon;
    }
}
