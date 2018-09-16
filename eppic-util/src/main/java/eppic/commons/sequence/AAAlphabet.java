package eppic.commons.sequence;

import java.io.Serializable;

public class AAAlphabet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String STANDARD_20 = "A:C:D:E:F:G:H:I:K:L:M:N:P:Q:R:S:T:V:W:Y";
	/**
	 * See Murphy L.R. et al. 2000 Protein Engineering (especially Fig.1)
	 */
	public static final String MURPHY_15   = "A:C:D:E:FY:G:H:ILMV:KR:N:P:Q:S:T:W";
	/**
	 * See Murphy L.R. et al. 2000 Protein Engineering (especially Fig.1)
	 */
	public static final String MURPHY_10   = "A:C:DENQ:FWY:G:H:ILMV:KR:P:ST";
	/**
	 * See Murphy L.R. et al. 2000 Protein Engineering (especially Fig.1)
	 */
	public static final String MURPHY_8    = "AG:CILMV:DENQ:FWY:H:KR:P:ST";
	/**
	 * See Mirny and Shakhnovich 1999 JMB.
	 */
	public static final String MIRNY_6     = "ACILMV:DE:FHWY:GP:KR:NQST";
	/**
	 * See Murphy L.R. et al. 2000 Protein Engineering (especially Fig.1)
	 */
	public static final String MURPHY_4    = "AGPST:CILMV:DEHKNQR:FWY";
	/**
	 * See Murphy L.R. et al. 2000 Protein Engineering (especially Fig.1)
	 */
	public static final String MURPHY_2    = "ACFGILMPSTVWY:DEHKNQR";
	/**
	 * Wang and Wang 1999, Nat Structural Biology 
	 */
	public static final String WANG_2      = "ADEGHKNPQRST:CFILMVWY";
	
	private int ala;
	private int arg;
	private int asn;
	private int asp;
	private int cys;
	private int gln;
	private int glu;
	private int gly;
	private int his;
	private int ile;
	private int leu;
	private int lys;
	private int met;
	private int phe;
	private int pro;
	private int ser;
	private int thr;
	private int trp;
	private int tyr;
	private int val;
	
	private String[] groups;
	
	private int numLetters;
	
	public AAAlphabet(String alphabetString) {
		groups = alphabetString.split(":");
		numLetters = groups.length;
		updateGroups();
	}
	
	public void updateGroups() {
		for (int i = 0; i < groups.length; i++) {
			for (int j = 0; j < groups[i].length(); j++) {
				switch (groups[i].charAt(j)) {
				case 'A':
					ala = (i + 1);
					break;
				case 'R':
					arg = (i + 1);
					break;
				case 'N':
					asn = (i + 1);
					break;
				case 'D':
					asp = (i + 1);
					break;
				case 'C':
					cys = (i + 1);
					break;
				case 'Q':
					gln = (i + 1);
					break;
				case 'E':
					glu = (i + 1);
					break;
				case 'G':
					gly = (i + 1);
					break;
				case 'H':
					his = (i + 1);
					break;
				case 'I':
					ile = (i + 1);
					break;
				case 'L':
					leu = (i + 1);
					break;
				case 'K':
					lys = (i + 1);
					break;
				case 'M':
					met = (i + 1);
					break;
				case 'F':
					phe = (i + 1);
					break;
				case 'P':
					pro = (i + 1);
					break;
				case 'S':
					ser = (i + 1);
					break;
				case 'T':
					thr = (i + 1);
					break;
				case 'W':
					trp = (i + 1);
					break;
				case 'Y':
					tyr = (i + 1);
					break;
				case 'V':
					val = (i + 1);
					break;	
				}
			}
		}
	}
	
	public int getGroupByOneLetterCode(char oneLetterCode) {
		switch (oneLetterCode) {
		case 'A':
			return ala;
		case 'R':
			return arg;
		case 'N':
			return asn;
		case 'D':
			return asp;
		case 'C':
			return cys;
		case 'Q':
			return gln;
		case 'E':
			return glu;
		case 'G':
			return gly;
		case 'H':
			return his;
		case 'I':
			return ile;
		case 'L':
			return leu;
		case 'K':
			return lys;
		case 'M':
			return met;
		case 'F':
			return phe;
		case 'P':
			return pro;
		case 'S':
			return ser;
		case 'T':
			return thr;
		case 'W':
			return trp;
		case 'Y':
			return tyr;
		case 'V':
			return val;
		default:
			return -1;
		}
	}

	public int getNumLetters() {
		return numLetters;
	}
	
	public String[] getGroups() {
		return groups;
	}
	
	public void setGroups(String[] newGroups) {
		groups = newGroups;
		numLetters = groups.length;
		updateGroups();
	}
	
	@Override
	public String toString() {
		String toReturn = "";
		for (String group : groups) {
			toReturn += group;
			toReturn += ":";
		}
		return toReturn.substring(0, toReturn.length() - 1);
	}

	public static boolean isValidAlphabetIdentifier(int identifier) {
		return (identifier == 0  || identifier == 2  || identifier == 4  || identifier == 6  || identifier == 8 || 
				identifier == 10 || identifier == 15 || identifier == 20);
	}
}
