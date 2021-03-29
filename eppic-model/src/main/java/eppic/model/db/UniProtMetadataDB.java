package eppic.model.db;

import javax.persistence.Table;
import java.io.Serializable;

@Table(name="UniProtMetadata")
public class UniProtMetadataDB implements Serializable {

    private static final long serialVersionUID = 1L;

    private String uniRefType;
    private String version;

    public String getUniRefType() {
        return uniRefType;
    }

    public void setUniRefType(String uniRefType) {
        this.uniRefType = uniRefType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
