package eppic.model.db;

public class BlobIdentifierDB {

    private String jobId;
    private FileTypeEnum type;
    private String id;

    public BlobIdentifierDB() {

    }

    public BlobIdentifierDB(String jobId, FileTypeEnum type, String id) {
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

    public FileTypeEnum getType() {
        return type;
    }

    public void setType(FileTypeEnum type) {
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
