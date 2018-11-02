package eppic.model.db;

import java.io.Serializable;

public class UniProtMetadataDB implements Serializable {

    private static final long serialVersionUID = 1L;

    private int uid;
    private String uniRefType;
    private String version;


    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

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
