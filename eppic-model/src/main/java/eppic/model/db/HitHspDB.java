package eppic.model.db;

import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name="HitHsp",
        indexes = @Index(name = "queryId_idx", columnList = "queryId", unique = false))
public class HitHspDB implements Serializable {

    private static final long serialVersionUID = 1L;

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
}
