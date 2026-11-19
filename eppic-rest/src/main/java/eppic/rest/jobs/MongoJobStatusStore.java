package eppic.rest.jobs;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import eppic.model.shared.StatusOfJob;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * A {@link JobStatusStore} backed by a collection in the user jobs Mongo db. One document per job, keyed by
 * submission id. Documents expire through a TTL index: they are only needed while a job is processing and
 * shortly after.
 */
public class MongoJobStatusStore implements JobStatusStore {

    private static final Logger logger = LoggerFactory.getLogger(MongoJobStatusStore.class);

    public static final String COLLECTION_NAME = "job_status";

    private static final String STATUS_FIELD = "status";
    private static final String HOST_FIELD = "host";
    private static final String SUBMITTED_AT_FIELD = "submittedAt";
    private static final String UPDATED_AT_FIELD = "updatedAt";
    private static final String HEARTBEAT_AT_FIELD = "heartbeatAt";

    private static final long EXPIRE_AFTER_DAYS = 30;

    private final MongoCollection<Document> collection;
    private final String host;

    public MongoJobStatusStore(MongoDatabase mongoDb) {
        this.collection = mongoDb.getCollection(COLLECTION_NAME);
        this.host = getHostName();
        try {
            // a no-op if the index exists already
            collection.createIndex(Indexes.ascending(SUBMITTED_AT_FIELD),
                    new IndexOptions().expireAfter(EXPIRE_AFTER_DAYS, TimeUnit.DAYS));
        } catch (Exception e) {
            logger.warn("Could not create TTL index on collection '{}'. Error: {}", COLLECTION_NAME, e.getMessage());
        }
    }

    @Override
    public void setStatus(String submissionId, StatusOfJob status) throws JobHandlerException {
        Date now = new Date();
        try {
            collection.updateOne(Filters.eq("_id", submissionId),
                    Updates.combine(
                            Updates.set(STATUS_FIELD, status.name()),
                            Updates.set(HOST_FIELD, host),
                            Updates.set(UPDATED_AT_FIELD, now),
                            Updates.set(HEARTBEAT_AT_FIELD, now),
                            Updates.setOnInsert(SUBMITTED_AT_FIELD, now)),
                    new UpdateOptions().upsert(true));
        } catch (Exception e) {
            throw new JobHandlerException("Could not write status " + status + " for job " + submissionId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void heartbeat(Collection<String> submissionIds) throws JobHandlerException {
        if (submissionIds.isEmpty()) {
            return;
        }
        try {
            collection.updateMany(Filters.in("_id", submissionIds), Updates.set(HEARTBEAT_AT_FIELD, new Date()));
        } catch (Exception e) {
            throw new JobHandlerException("Could not write heartbeat for jobs " + submissionIds + ": " + e.getMessage(), e);
        }
    }

    @Override
    public JobStatusRecord get(String submissionId) throws JobHandlerException {
        Document doc;
        try {
            doc = collection.find(Filters.eq("_id", submissionId)).first();
        } catch (Exception e) {
            throw new JobHandlerException("Could not read status for job " + submissionId + ": " + e.getMessage(), e);
        }
        if (doc == null) {
            return null;
        }
        return new JobStatusRecord(StatusOfJob.valueOf(doc.getString(STATUS_FIELD)),
                doc.getString(HOST_FIELD),
                doc.getDate(HEARTBEAT_AT_FIELD));
    }

    private static String getHostName() {
        // in k8s this is the pod name
        String hostname = System.getenv("HOSTNAME");
        if (hostname != null && !hostname.isBlank()) {
            return hostname;
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
