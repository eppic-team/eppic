package eppic.commons.blast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The sequence search cache with a list of hits per query (for all queries).
 *
 * @author Jose Duarte
 * @since 3.2.0
 */
public class SeqSearchCache {

    private static final Logger logger = LoggerFactory.getLogger(SeqSearchCache.class);

    private Map<String, BlastHitList> queriesToHitList;
    private String db;

    public SeqSearchCache() {
        queriesToHitList = new HashMap<>();

    }

    /**
     * Reads a file in blast tabular format (option -m 8 in legacy blast, -outfmt 6 in blast+,
     * or output of mmseqs convertalis).
     * @param blastTabFormatFile a file in blast tabular format
     * @throws IOException if something goes wrong when reading file
     */
    public void initCache(File blastTabFormatFile) throws IOException {

        // TODO set db
        db = null;
        queriesToHitList = new HashMap<>();

        try (
        BufferedReader br = new BufferedReader(new FileReader(blastTabFormatFile));) {

            String line;
            int lineNum = 0;
            while ((line = br.readLine()) != null) {
                lineNum++;
                if (line.isEmpty()) continue;
                String[] tokens = line.split("\t");
                if (tokens.length != 12) {
                    logger.warn("Line {} of file {} does not have 12 fields. Ignoring line.", lineNum, blastTabFormatFile);
                    continue;
                }
                // qseqid sseqid pident length mismatch gapopen qstart qend sstart send evalue bitscore

                String queryId = tokens[0];
                String subjectId = tokens[1];
                try {
                    double identity = Double.parseDouble(tokens[2]);
                    int length = Integer.parseInt(tokens[3]);
                    //int mismatches = Integer.parseInt(tokens[4]);
                    //int gapOpenings = Integer.parseInt(tokens[5]);
                    int qStart = Integer.parseInt(tokens[6]);
                    int qEnd = Integer.parseInt(tokens[7]);
                    int sStart = Integer.parseInt(tokens[8]);
                    int sEnd = Integer.parseInt(tokens[9]);
                    double eValue = Double.parseDouble(tokens[10]);
                    int bitScore = Integer.parseInt(tokens[11]);

                    BlastHitList queriesHitList;
                    if (queriesToHitList.containsKey(queryId)) {
                        queriesHitList = queriesToHitList.get(queryId);
                    } else {
                        queriesHitList = new BlastHitList();
                        queriesHitList.setQueryId(queryId);
                        queriesHitList.setDb(this.db);
                        // TODO query length is not available for tabular format, is it needed for something?
                        queriesToHitList.put(queryId, queriesHitList);
                    }

                    BlastHit hit;
                    if (queriesHitList.contains(subjectId)) {
                        hit = queriesHitList.getHit(subjectId);
                    } else {
                        hit = new BlastHit();
                        hit.setQueryId(queryId);
                        hit.setSubjectId(subjectId);
                        // TODO review if query/subject length needed, also subject def

                        queriesHitList.add(hit);
                    }

                    BlastHsp hsp = new BlastHsp(hit);
                    hsp.setAliLength(length);
                    hsp.setEValue(eValue);
                    hsp.setQueryStart(qStart);
                    hsp.setQueryEnd(qEnd);
                    hsp.setSubjectStart(sStart);
                    hsp.setSubjectEnd(sEnd);
                    hsp.setScore(bitScore);
                    hsp.setIdentities((int)(identity*length)); // TODO check if this is right

                    hit.addHsp(hsp);

                } catch (NumberFormatException e) {
                    logger.warn("Wrong number format for line {} of file {}. Query id is {}. Will ignore line. Error: {}", lineNum, blastTabFormatFile, queryId, e.getMessage());
                }
            }
        }

    }

    public void setDb(String db) {
        this.db = db;
    }

    /**
     * Tells whether given queryId is present in the cache.
     * @param queryId the query identifier
     * @return true if queryId present in cache, false otherwise
     */
    public boolean hasQueryId(String queryId) {
        return queriesToHitList.containsKey(queryId);
    }

    /**
     * Gets the hit list for given queryId
     * @param queryId the query identifier
     * @return hit list for queryId or null if not present
     */
    public BlastHitList getHitsForQuery(String queryId) {
        return queriesToHitList.get(queryId);
    }

    public Collection<BlastHitList> getAllHitLists() {
        return queriesToHitList.values();
    }
}
