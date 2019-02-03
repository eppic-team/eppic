package eppic.commons.blast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A list of blast hits corresponding to a given query.
 *
 * @author Jose Duarte
 */
public class BlastHitList implements Iterable<BlastHit> {
	
	private ArrayList<BlastHit> hits;
	private int queryLength;				// needed by print() and printSome()
	private String queryId;
	private HashMap<String,BlastHit> lookup; // a lookup table to be able to have an efficient getHit(subjectId)
	private String db;						// the db used for blasting (a path to a fasta file with corresponding index files in same dir)
	
	public BlastHitList() {
		this.hits = new ArrayList<BlastHit>();
		this.lookup = new HashMap<String, BlastHit>();
		this.queryLength = 0;
		this.queryId = null;
	}
	
	/**
	 * Adds a blast hit to this list
	 * @param hit
	 */
	public void add(BlastHit hit) {
		this.hits.add(hit);
		this.lookup.put(hit.getSubjectId(), hit);
	}

	/**
	 * Set the query length needed by the print() and printSome() methods.
	 * @param l
	 */
	public void setQueryLength(int l) {
		this.queryLength = l;
	}
	
	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}
	
	public String getQueryId() {
		return this.queryId;
	}
	
	/**
	 * Returns the query length for this blast hit list.
	 * @return
	 */
	public int getQueryLength() {
		return this.queryLength;
	}
	
	/**
	 * Gets the blast db used (a path to a fasta file with index files in same dir) 
	 * @return
	 */
	public String getDb() {
		return db;
	}
	
	/**
	 * Sets the blast db (a path to a fasta file with index files in same dir)
	 * @param db
	 */
	public void setDb(String db) {
		this.db = db;
	}
	
	/**
	 * Applies an e-value cutoff trimming out of this list all hits with e-value 
	 * higher than cutoff given
	 * @param eValueCutoff
	 */
	public void applyCutoff(double eValueCutoff) {
		Iterator<BlastHit> it = hits.iterator();
		while (it.hasNext()) {
			BlastHit hit = it.next();
			if (hit.getEvalueMaxScoringHsp()>=eValueCutoff) {
				it.remove();
				lookup.remove(hit.getSubjectId());
			}
		}
	}
	
	/**
	 * Filters out hits that rank below given rank.
	 * @param rank
	 */
	public void filterByMaxRank(int rank) {
		Iterator<BlastHit> it = this.iterator();
		int i=1;
		while (it.hasNext()) {
			BlastHit hit = it.next();
			if (i>rank) {
				it.remove();
				lookup.remove(hit.getSubjectId());
			}
			i++;
		}
	}
	
	/**
	 * Prints this BlastHitList in tabular format, one HSP per line, replicating 
	 * blast's own tabular output (blast's command line option -m 8)
	 */	
	public void printTabular() {
		for (BlastHit hit:this) {
			hit.printTabular();
		}
	}
	
	/**
	 * Prints a tabular overview of the hits in this list.
	 * Currently, if the query length is set, (i.e. > 0) a simple ascii-art
	 * overview of the matches is printed for each hit.
	 */
	public void print() {
		printSome(this.size());
	}
	
	/**
	 * Prints a tabular overview of the first numHits hits in this list.
	 * Currently, if the query length is set, (i.e. > 0) a simple ascii-art
	 * overview of the matches is printed for each hit.
	 */
	public void printSome(int numHits) {
		
		int outputLength = 80;		// length of graphical output in screen columns
		
		if(queryLength > 0) {
			double scaleFactor = 1.0 * outputLength / queryLength;
			int queryIDlength = this.getQueryId().length();
			BlastHit.printHeaderWithOverview(queryLength, scaleFactor, queryIDlength);
			for (int i = 0; i < Math.min(hits.size(), numHits); i++) {
				BlastHit hit = hits.get(i);
				hit.printWithOverview(scaleFactor);
			}			
		} else {
			// print without graphical overview
			for (int i = 0; i < Math.min(hits.size(), numHits); i++) {
				BlastHit hit = hits.get(i);
				hit.print();
			}			
		}
	}
	
	/**
	 * Returns the number of blast hits contained in this list
	 * @return
	 */
	public int size() {
		return hits.size();
	}

	/**
	 * Return an array of hits contained in this hit list
	 * @return
	 */
	public BlastHit[] getHits() {
		return (BlastHit[]) this.hits.toArray();
	}
	
	/**
	 * Returns a blast hit given its subjectId
	 * @param subjectId 
	 * @return
	 * @throws IllegalArgumentException if given subjectId not present in this BlastHitList
	 */
	public BlastHit getHit(String subjectId) {
		if (this.lookup.containsKey(subjectId))	{
			return this.lookup.get(subjectId);
		} else {
			throw new IllegalArgumentException("Given subjectId "+subjectId+" doesn't exist in this blast hit list");
		}
	}
	
	/**
	 * Returns the hit with the best e-value or null if this hit list is empty.
	 * @return
	 */
	public BlastHit getBestHit() {
		if(this.size() == 0) return null;
		BlastHit bestHit = this.hits.get(0);
		for(BlastHit hit:hits) {
			if(hit.getEvalueMaxScoringHsp() < bestHit.getEvalueMaxScoringHsp()) {
				bestHit = hit;
			}
		}
		return bestHit;
	}
	
	/**
	 * Sorts the BlastHitList by e-values of max scoring hsps
	 */
	public void sort() {
		Collections.sort(hits);
	}
	
	public boolean isEmpty() {
		return hits.isEmpty();
	}
	
	public BlastHit get(int i) {
		return hits.get(i);
	}
	
	/**
	 * Returns a list of all subject ids.
	 * @return
	 */
	public Collection<String> getSubjectIds() {
		ArrayList<String> sids = new ArrayList<String>();
		for (BlastHit hit:hits) {
			sids.add(hit.getSubjectId());
		}
		return sids;
	}
	
	/**
	 * Returns and iterator over this blast hits list
	 * @return
	 */
	@Override
	public Iterator<BlastHit> iterator() {
		return this.hits.iterator();
	}
	
	/**
	 * Returns true if this BlastHitList contains a hit with the given subjectId
	 * @param subjectId
	 * @return
	 */
	public boolean contains(String subjectId) {
		return this.lookup.containsKey(subjectId);
	}
}
