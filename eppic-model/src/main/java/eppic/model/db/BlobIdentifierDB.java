package eppic.model.db;

public class BlobIdentifierDB {

    public static final String BLOBS_COLLECTION_NAME = "blobs";
    public static final String BLOBS_ID_KEY = "blobId";
    public static final String BLOB_KEY = "blob";

    private String jobId;
    private String type;
    private String id;

    public BlobIdentifierDB(String jobId, String type, String id) {
        this.jobId = jobId;
        this.type = type;
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "BlobIdentifierDB{" +
                "jobId='" + jobId + '\'' +
                ", type='" + type + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
