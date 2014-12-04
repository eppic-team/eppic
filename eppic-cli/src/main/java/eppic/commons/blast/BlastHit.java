package eppic.commons.blast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A blast hit.
 * 
 * Each hit is composed of one or more BlastHsp (HSP=high scoring pairs) which are high scoring local
 * alignments of the subject to the query.
 * 
 */
public class BlastHit implements Iterable<BlastHsp>, Serializable, Comparable<BlastHit> {
	
	private static final long serialVersionUID = 1L;

	public static final int OUTPUT_LENGTH = 80;
	private static final String ID_REGEX = "pdb\\|(\\d\\w\\w\\w)\\|(\\w)";
	
	private String queryId; // queryId and queryLength are redundant here (they belong in BlastHitList) but anyway useful to have copies here
	private int queryLength;
	private String subjectId;
	private String subjectDef;
	private int subjectLength;

	private List<BlastHsp> hsps;
	
	public BlastHit() {
		this.hsps = new ArrayList<BlastHsp>();
	}
	
	public void addHsp(BlastHsp hsp) {
		this.hsps.add(hsp);
	}
	
	public String getQueryId() {
		return queryId;
	}

	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}
	
	public int getNumHsps() {
		return hsps.size();
	}

	public int getQueryLength() {
		return this.queryLength;
	}
	
	public void setQueryLength(int queryLength) {
		this.queryLength = queryLength;
	}
	
	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}
	
	public String getSubjectDef() {
		return subjectDef;
	}
	
	public void setSubjectDef(String subjectDef) {
		this.subjectDef = subjectDef;
	}

	public void setSubjectLength(int subjectLength) {
		this.subjectLength = subjectLength;
	}

	public int getSubjectLength() {
		return subjectLength;
	}
	
	/**
	 * Returns the query coverage for this hit's hsps, i.e. 
	 * the ratio of aligned residues of the query compared to its length
	 * @return
	 */
	public double getQueryCoverage() {
		int countAlignedResidues = 0;
		for (BlastHsp hsp:this) {
			countAlignedResidues+=(hsp.getQueryEnd()-hsp.getQueryStart()+1);
		}
		return  ((double)countAlignedResidues/this.getQueryLength());
	}
	
	/**
	 * Returns the hsp with the maximum score for this hit.
	 * @return
	 */
	public BlastHsp getMaxScoringHsp() {
		return Collections.max(hsps, new Comparator<BlastHsp>() {
			@Override
			public int compare(BlastHsp o1, BlastHsp o2) {
				return Double.compare(o1.getScore(),o2.getScore());
			}
		});
	}
	
	/**
	 * Returns the score of the hsp with maximum score for this hit.
	 * @return
	 */
	public double getMaxScore() {
		return getMaxScoringHsp().getScore();
	}
	
	/**
	 * Returns the sum of scores of all hsps of this hit.
	 * @return
	 */
	public double getTotalScore() {
		double totalScore = 0;
		for (BlastHsp hsp:this) {
			totalScore+=hsp.getScore();
		}
		return totalScore;
	}
	
	/**
	 * Returns the total percent identity (identities over length of alignment) 
	 * by considering the identities across all HSPs of this hit. 
	 * @return
	 */
	public double getTotalPercentIdentity() {
		int totalIds = 0;
		int totalAlignedLength = 0;
		for (BlastHsp hsp:this) {
			totalIds += hsp.getIdentities();
			totalAlignedLength+= hsp.getAliLength();
		}
		return 100.0*((double)totalIds/(double)totalAlignedLength);
	}
	
	/**
	 * Returns the evalue of the maximum scoring hsp of this hit.
	 * @return
	 */
	public double getEvalueMaxScoringHsp() {
		return getMaxScoringHsp().getEValue();
	}
	
	/**
	 * Returns the template id as concatenated pdb code + chain code e.g. 1abcA
	 * @return the template id or null if queryId is not in the right format
	 */
	public String getTemplateId() {
		Pattern p = Pattern.compile(ID_REGEX);
		Matcher m = p.matcher(subjectId);
		if (m.matches()) {
			return m.group(1).toLowerCase()+m.group(2).toUpperCase();
		}
		return null;
	}
	
	// statics
	
	/**
	 * From a template id in the form pdbCode+pdbChainCode (e.g. 1abcA) returns a subjectId
	 * as it is in the blastable pdb sequences file: pdb|1ABC|A
	 * This is exactly the reverse of {@link #getTemplateId()}
	 * See also {@link #ID_REGEX} 
	 * @param templateId
	 * @return 
	 */
	public static String templateIdToSubjectId(String templateId) {
		Pattern p = Pattern.compile("(\\d\\w\\w\\w)(\\w)");
		Matcher m = p.matcher(templateId);
		if (m.matches()) {
			return "pdb|"+m.group(1).toUpperCase()+"|"+m.group(2);
		} else {
			throw new IllegalArgumentException("Given templateId must be of the form pdbCode+pdbChainCode, e.g. 1abcA");
		}
	}

	@Override
	public Iterator<BlastHsp> iterator() {
		return hsps.iterator();
	}

	/**
	 * Prints this BlastHit in tabular format, one HSP per line, replicating 
	 * blast's own tabular output (blast's command line option -m 8)
	 */
	public void printTabular() {
		for (BlastHsp hsp:this) {
			hsp.printTabular();
		}
	}
	
	/**
	 * Prints a few selected fields for this blast hit 
	 */
	public void print() {
		System.out.println(getQueryId()+"\t"+getSubjectId()+"\t"+getTotalPercentIdentity()+"\t"+getEvalueMaxScoringHsp()+"\t"+getMaxScore());
	}
	
	/**
	 * Prints a few selected fields for this blast hit plus a graphical representation of the match.
	 * The match is scaled by the given scale factor and rounded to screen columns. 
	 * @param scaleFactor
	 */
	public void printWithOverview(double scaleFactor) {
		System.out.printf("%"+getQueryId().length()+"s\t%10s\t%5.1f\t%8.1e\t%4.0f ", getQueryId(), getSubjectId(), getTotalPercentIdentity(), getEvalueMaxScoringHsp(), getMaxScore());
		int[] beg = new int[hsps.size()];
		int[] end = new int[hsps.size()];
		for (int i=0;i<hsps.size();i++){
			beg[i] = (int) Math.floor(scaleFactor * hsps.get(i).getQueryStart());
			end[i] = (int) Math.ceil(scaleFactor * hsps.get(i).getQueryEnd());
		}
		printOverviewLine(beg, end);
	}	
	
	/**
	 * Print the column headers corresponding to the printWithOverview() method.
	 * Additionally prints a graphical overview of the query (queryLength scaled by scaleFactor).
	 * @param queryLength
	 * @param scaleFactor
	 */
	public static void printHeaderWithOverview(int queryLength, double scaleFactor, int queryIDlength) {
		System.out.printf("%"+queryIDlength+"s\t%10s\t%5s\t%8s\t%4s ", "query", "subject", "id%", "e-val", "sc");
		int beg = 1;
		int end = (int) Math.ceil(scaleFactor * queryLength);
		printOverviewLine(beg, end);
	}
	
	/**
	 * Print one line of the match overview.
	 * @param beg the beginnings of the hsps in screen columns
	 * @param end the ends of the hsps in screen columns
	 */
	private static void printOverviewLine(int[] beg, int[] end) {
		int col = 1;
		int i = 0;
		while (true) {
			if (col>=beg[i] && col<=end[i]) {
				System.out.print("-");
			} else {
				System.out.print(" ");
			}
			col++;
			if (col>end[i]) {
				if (i==end.length-1) {
					break;
				}
				i++;
			}
		}
		System.out.println();
	}
	
	private static void printOverviewLine(int beg, int end) {
		for (int i = 1; i < beg; i++) {
			System.out.print(" ");
		}
		for (int i = beg; i <= end; i++) {
			System.out.print("-");
		}
		System.out.println();
	}

	@Override
	public int compareTo(BlastHit o) {
		return Double.compare(this.getEvalueMaxScoringHsp(), o.getEvalueMaxScoringHsp());
	}

}
