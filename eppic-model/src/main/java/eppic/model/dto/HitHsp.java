package eppic.model.dto;

import eppic.model.db.HitHspDB;

import java.io.Serializable;

public class HitHsp implements Serializable {

    private static final long serialVersionUID = 1L;

    private int uid;
    private String db;
    // qseqid sseqid pident length mismatch gapopen qstart qend sstart send evalue bitscore
    private String queryId;
    private String subjectId;
    private double percentIdentity;
    private int aliLength;
    private int numMismatches;
    private int numGapOpenings;
    private int queryStart;
    private int queryEnd;
    private int subjectStart;
    private int subjectEnd;
    private double eValue;
    private int bitScore;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public double getPercentIdentity() {
        return percentIdentity;
    }

    public void setPercentIdentity(double percentIdentity) {
        this.percentIdentity = percentIdentity;
    }

    public int getAliLength() {
        return aliLength;
    }

    public void setAliLength(int aliLength) {
        this.aliLength = aliLength;
    }

    public int getNumMismatches() {
        return numMismatches;
    }

    public void setNumMismatches(int numMismatches) {
        this.numMismatches = numMismatches;
    }

    public int getNumGapOpenings() {
        return numGapOpenings;
    }

    public void setNumGapOpenings(int numGapOpenings) {
        this.numGapOpenings = numGapOpenings;
    }

    public int getQueryStart() {
        return queryStart;
    }

    public void setQueryStart(int queryStart) {
        this.queryStart = queryStart;
    }

    public int getQueryEnd() {
        return queryEnd;
    }

    public void setQueryEnd(int queryEnd) {
        this.queryEnd = queryEnd;
    }

    public int getSubjectStart() {
        return subjectStart;
    }

    public void setSubjectStart(int subjectStart) {
        this.subjectStart = subjectStart;
    }

    public int getSubjectEnd() {
        return subjectEnd;
    }

    public void setSubjectEnd(int subjectEnd) {
        this.subjectEnd = subjectEnd;
    }

    public double geteValue() {
        return eValue;
    }

    public void seteValue(double eValue) {
        this.eValue = eValue;
    }

    public int getBitScore() {
        return bitScore;
    }

    public void setBitScore(int bitScore) {
        this.bitScore = bitScore;
    }

    public static HitHsp create(HitHspDB hitHspDB) {
        HitHsp hitHsp = new HitHsp();
        hitHsp.setUid(hitHspDB.getUid());
        hitHsp.setDb(hitHspDB.getDb());
        hitHsp.setQueryId(hitHspDB.getQueryId());
        hitHsp.setSubjectId(hitHspDB.getSubjectId());
        hitHsp.setPercentIdentity(hitHspDB.getPercentIdentity());
        hitHsp.setAliLength(hitHspDB.getAliLength());
        hitHsp.setNumMismatches(hitHspDB.getNumMismatches());
        hitHsp.setNumGapOpenings(hitHspDB.getNumGapOpenings());
        hitHsp.setQueryStart(hitHspDB.getQueryStart());
        hitHsp.setQueryEnd(hitHspDB.getQueryEnd());
        hitHsp.setSubjectStart(hitHspDB.getSubjectStart());
        hitHsp.setSubjectEnd(hitHspDB.getSubjectEnd());
        hitHsp.seteValue(hitHspDB.geteValue());
        hitHsp.setBitScore(hitHspDB.getBitScore());
        return hitHsp;
    }
}
