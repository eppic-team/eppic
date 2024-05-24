package eppic.model.db;

import javax.persistence.Index;
import javax.persistence.Table;

@Table(name = "Blob",
        indexes = {@Index(name = "blobId_idx", columnList = "blobId.jobdId,blobId.type,blobId.id", unique = true), @Index(name = "blobId-jobId_idx", columnList = "blobId.jobId")})
public class BlobDB {

    public static final String BLOBS_ID_KEY = "blobId";
    public static final String BLOB_KEY = "blob";

    private BlobIdentifierDB blobId;
    private byte[] blob;

    public BlobDB() {

    }

    public BlobDB(BlobIdentifierDB blobId, byte[] blob) {
        this.blobId = blobId;
        this.blob = blob;
    }

    public BlobIdentifierDB getBlobId() {
        return blobId;
    }

    public void setBlobId(BlobIdentifierDB blobId) {
        this.blobId = blobId;
    }

    public byte[] getBlob() {
        return blob;
    }

    public void setBlob(byte[] blob) {
        this.blob = blob;
    }
}
