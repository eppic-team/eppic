package eppic.model.db;

import java.io.Serializable;

public class UniProtInfoDB implements Serializable {

    private static final long serialVersionUID = 1L;

    private int uid;

    private String uniId;
    private String sequence;

    private String firstTaxon;
    private String lastTaxon;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

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
