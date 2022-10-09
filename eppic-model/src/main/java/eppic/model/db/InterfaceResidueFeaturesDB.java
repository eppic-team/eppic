package eppic.model.db;

import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;

@Table(name="InterfaceResidueFeatures",
        indexes = @Index(name = "interfId_idx", columnList = "jobId,interfaceId", unique = false))
public class InterfaceResidueFeaturesDB implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * A unique across db identifier for the entry: for precomputed PDB entries it will be the PDB id, for
     * user jobs from files it will be a randomly generated alphanumerical string.
     */
    private String entryId;
    private int interfaceId;

    private List<ResidueBurialDB> resBurials1;
    private List<ResidueBurialDB> resBurials2;

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public int getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(int interfaceId) {
        this.interfaceId = interfaceId;
    }

    public List<ResidueBurialDB> getResBurials1() {
        return resBurials1;
    }

    public void setResBurials1(List<ResidueBurialDB> resBurials1) {
        this.resBurials1 = resBurials1;
    }

    public List<ResidueBurialDB> getResBurials2() {
        return resBurials2;
    }

    public void setResBurials2(List<ResidueBurialDB> resBurials2) {
        this.resBurials2 = resBurials2;
    }
}
