package eppic.commons.blast;

import java.io.Serializable;

import eppic.commons.sequence.AlignmentConstructionException;
import eppic.commons.sequence.MultipleSequenceAlignment;
import eppic.model.db.HitHspDB;


public class BlastHsp implements Serializable {

	private static final long serialVersionUID = 1L;

	private BlastHit parent;
	private int aliLength;
	private int queryStart;
	private int queryEnd;
	private int subjectStart;
	private int subjectEnd;
	private double eValue;
	private double score;
	private int identities;

	private MultipleSequenceAlignment al; 

	/**
	 * Constructor to be used when parsing from XML output, use the setters to 
	 * fill the values
	 */
	public BlastHsp(BlastHit hit) {
		this.parent = hit;
	}
	
	/**
	 * Returns the BlastHit parent of this HSP.
	 * @return
	 */
	public BlastHit getParent() {
		return parent;
	}
	
	/**
	 * Sets the BlastHit parent of this HSP.
	 * @param parent
	 */
	public void setParent(BlastHit parent) {
		this.parent = parent;
	}
	
	/**
	 * Returns the score for this HSP.
	 * @return
	 */
	public double getScore() {
		return score;
	}
	
	/**
	 * Sets the score for this HSP.
	 * @param score
	 */
	public void setScore(double score) {
		this.score = score;
	}
	
	/** 
	 * Returns the e-value for this HSP.
	 * @return
	 */
	public double getEValue() {
		return this.eValue;
	}
	
	/**
	 * Returns the alignment length of this HSP.
	 * @return
	 */
	public int getAliLength() {
		return aliLength;
	}

	/**
	 * Returns the subject start position of this HSP.
	 * @return
	 */
	public int getSubjectStart() {
		return subjectStart;
	}

	/**
	 * Returns the subject's end position of this hsp.
	 * @return
	 */
	public int getSubjectEnd() {
		return subjectEnd;
	}

	/**
	 * Returns the query's start position of this HSP.
	 * @return
	 */
	public int getQueryStart() {
		return queryStart;
	}

	/**
	 * Returns the query's end position of this HSP.
	 * @return
	 */
	public int getQueryEnd() {
		return queryEnd;
	}

	public void setAliLength(int aliLength) {
		this.aliLength = aliLength;
	}

	public void setQueryStart(int queryStart) {
		this.queryStart = queryStart;
	}

	public void setQueryEnd(int queryEnd) {
		this.queryEnd = queryEnd;
	}

	public void setSubjectStart(int subjectStart) {
		this.subjectStart = subjectStart;
	}

	public void setSubjectEnd(int subjectEnd) {
		this.subjectEnd = subjectEnd;
	}

	public void setEValue(double value) {
		eValue = value;
	}

	/**
	 * Returns the percent identity for this HSP, defined as number of identities over length of alignment.
	 * @return
	 */
	public double getPercentIdentity() {
		return (double)(100*identities)/((double)aliLength);
	}
	
	/**
	 * Returns the query percent identity for this HSP, defined as number of identities over aligned query length.
	 * @return
	 */
	public double getQueryPercentIdentity() {
		return (double)(100*identities)/((double)(queryEnd-queryStart+1));
	}
	
	public void setIdentities(int identitities) {
		this.identities = identitities;
	}
	
	public int getIdentities() {
		return identities;		
	}
	
	public MultipleSequenceAlignment getAlignment() {
		return al;
	}
	
	/**
	 * Sets the alignment of this HSP given the 2 aligned sequences of query and subject
	 * @param querySeq
	 * @param subjectSeq
	 */
	public void setAlignment(String querySeq, String subjectSeq) {
		String[] tags = {parent.getQueryId(), parent.getSubjectId()};
		String[] seqs = {querySeq, subjectSeq};
		try {
			this.al = new MultipleSequenceAlignment(tags, seqs);
		} catch (AlignmentConstructionException e) {
			System.err.println("Error while constructing alignment from parsed blast output: "+e.getMessage());
		}
	}

	/**
	 * Returns an alignment result of transforming the hsp local alignment of this BlastHit 
	 * into one that contains the full sequences of query and subject given (padded 
	 * with gaps on the opposite sides). 
	 * e.g. If blast alignment is:
	 *  q:   ABC--DE--  (full q: bbABCDEeee) 
	 *  s:   -ABCD--EF  (full s: bbbABCDEFee)
	 * the new alignment will be:
	 *  q:  ---bbABC--DE--eee--
	 *  s:  bbb---ABCD--EF---ee
	 *      ^^^^^         ^^^^^
	 * The alignment of this BlastHit is unaffected. The returned alignment is a new object.
	 * @param fullQuerySeq
	 * @param fullSubjectSeq
	 * @return
	 */
	public MultipleSequenceAlignment getAlignmentFullSeqs(String fullQuerySeq, String fullSubjectSeq) {
		
		String querySeqNoGaps = this.al.getSequenceNoGaps(parent.getQueryId());
		String subjectSeqNoGaps = this.al.getSequenceNoGaps(parent.getSubjectId());

		if (!fullQuerySeq.contains(querySeqNoGaps)){
			throw new IllegalArgumentException("Given full query sequence is not a superstring of this BlastHit's alignment query sequence");
		}
		if (!fullSubjectSeq.contains(subjectSeqNoGaps)){
			throw new IllegalArgumentException("Given full subject sequence is not a superstring of this BlastHit's alignment subject sequence");
		}
		
		if (fullQuerySeq.length()==querySeqNoGaps.length() && fullSubjectSeq.length()==subjectSeqNoGaps.length()) {
			// the condition is equivalent to following, as a sanity check we also try it
			if (parent.getQueryLength()==fullQuerySeq.length() && parent.getSubjectLength()==fullSubjectSeq.length()) {
				// nothing to do, blast alignment is already spanning both full sequences of query and subject
				return this.al;				
			} else {
				System.err.println("Unexpected error: inconsistency between queryStart/End, subjectStart/End and sequences in stored alignment. Please report bug!");
				System.exit(1);
			}
		}
		
		String querySeq = this.al.getAlignedSequence(parent.getQueryId());
		String subjectSeq = this.al.getAlignedSequence(parent.getSubjectId());
		
		String newQuerySeq = getNGaps(this.subjectStart)+
							fullQuerySeq.substring(0, this.queryStart-1)+
							querySeq+
							fullQuerySeq.substring(this.queryEnd)+
							getNGaps(parent.getSubjectLength()-this.subjectEnd);
		String newSubjectSeq = fullSubjectSeq.substring(0, this.subjectStart-1)+
							getNGaps(this.queryStart)+
							subjectSeq+
							getNGaps(parent.getQueryLength()-this.queryEnd)+
							fullSubjectSeq.substring(this.subjectEnd);
		
		String[] tags = {parent.getQueryId(), parent.getSubjectId()};
		String[] seqs = {newQuerySeq, newSubjectSeq};
		MultipleSequenceAlignment newAln = null;
		try {
			newAln = new MultipleSequenceAlignment(tags, seqs);
		} catch (AlignmentConstructionException e) {
			System.err.println("Unexpected error: new alignment with full sequences from blast alignment couldn't be created. Please report the bug! Error: "+e.getMessage());
			System.exit(1);
		}
		return newAln;
	}
	
	/**
	 * Produces a string of n gap characters
	 * @param n
	 * @return
	 */
	private String getNGaps(int n) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<n; i++)
		      buf.append (MultipleSequenceAlignment.GAPCHARACTER);
		return buf.toString();	
	}
	
	/**
	 * Return the alignment of this HSP with the subjectId tag replaced by templateId 
	 * (pdbCode+pdbChaincode). If subjectId doesn't match regex {@value #ID_REGEX} then 
	 * alignment with normal tags is returned. 
	 * is returned.
	 * @see {@link #getTemplateId()}
	 * @param fullQuerySeq
	 * @param fullSubjectSeq
	 * @return
	 */
	public MultipleSequenceAlignment getAlignmentFullSeqsWithPDBTag(String fullQuerySeq, String fullSubjectSeq) {
		if (parent.getTemplateId()!=null) {
			MultipleSequenceAlignment aln = this.getAlignmentFullSeqs(fullQuerySeq, fullSubjectSeq);
			aln.resetTag(parent.getSubjectId(), parent.getTemplateId());
			return aln;
		} else {
			return getAlignmentFullSeqs(fullQuerySeq, fullSubjectSeq);
		}
	}

	/**
	 * Returns the query coverage for this HSP, i.e. 
	 * the ratio of aligned residues of the query compared to the query's full length
	 * @return
	 */
	public double getQueryCoverage() {
		return  ((double)(getQueryEnd()-getQueryStart()+1)/parent.getQueryLength());
	}
	
	/**
	 * Prints this BlastHsp in tabular format replicating blast's own 
	 * tabular output (blast's command line option -m 8). 
	 * The only field that we can't reproduce (because we don't parse it) is 
	 * the gap openings (column 6) where we print always a 0 instead.
	 */
	public void printTabular() {
		String scoreFormat = "%4.0f";
		if (score<100) {
			scoreFormat = "%4.1f";
		}
		System.out.printf("%s\t%s\t%.2f\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%.0e\t"+scoreFormat+"\n",
				parent.getQueryId(),parent.getSubjectId(),
				getPercentIdentity(),aliLength,aliLength-getIdentities(),0,queryStart,queryEnd,subjectStart,subjectEnd,eValue,score);
	}

	public HitHspDB toDbModel() {
		HitHspDB hitHspDB = new HitHspDB();

		hitHspDB.setDb(null); // TODO where do we get the db from?
		hitHspDB.setQueryId(this.parent.getQueryId());
		hitHspDB.setSubjectId(this.parent.getSubjectId());
		hitHspDB.setPercentIdentity(getPercentIdentity()/100.0);
		hitHspDB.setAliLength(this.aliLength);
		// missing for now: numMismatches, numGapOpenings
		hitHspDB.setQueryStart(this.queryStart);
		hitHspDB.setQueryEnd(this.queryEnd);
		hitHspDB.setSubjectStart(this.subjectStart);
		hitHspDB.setSubjectEnd(this.subjectEnd);
		hitHspDB.seteValue(this.eValue);
		hitHspDB.setBitScore((int)this.score); // TODO check if score in this class can be converted to int

		return hitHspDB;
	}

//	public static BlastHsp createFromDbModel(HitHspDB hitHspDB) {
//
//
//	}
	
}
