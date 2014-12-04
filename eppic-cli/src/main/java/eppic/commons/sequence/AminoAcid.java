package eppic.commons.sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Class: 		AminoAcid
 * Author:		Henning Stehr, stehr@molgen.mpg.de
 * 
 * An amino acid in a protein sequence. Each of the twenty naturally occuring 
 * amino acids is an instance of this class. Additionally, a placeholder 'X' for
 * an unknown amino acid and a stop codon are defined. This class also provides
 * static methods for obtaining amino acid objects by their number, one letter
 * code or three letter code and to convert between these representations. 
 * 
 * Note: Currently, most classes in the package still use a simple string representation of
 * amino acids. Helper functions for these are provided in the class AAinfo.
 * 
 */
public enum AminoAcid {
		
	/*---------------------- member variables --------------------------*/
	
    //                                                                                                                  reduced alphabets
	//                                              hydro  hydro  arom   aliph  polar  charg  pos    neg    small  tiny   15  10   8   6   4   2  
	 ALA ( 1, "Alanine",       'A', "ALA",  1, 113, -0.20, true,  false, false, false, false, false, false, true,  true ,  3,  3,  2,  1,  2,  1), 
	 ARG ( 2, "Arginine",      'R', "ARG",  7, 241,  1.43, false, false, false, true,  true,  true,  false, false, false, 14,  9,  7,  6,  4,  2), 
	 ASN ( 3, "Asparagine",    'N', "ASN",  4, 158,  0.69, false, false, false, true,  false, false, false, true,  false, 12,  8,  6,  4,  4,  2),
	 ASP ( 4, "Aspartic Acid", 'D', "ASP",  4, 151,  0.72, false, false, false, true,  true,  false, true,  true,  false, 11,  8,  6,  5,  4,  2),
	 CYS ( 5, "Cysteine",      'C', "CYS",  2, 140, -0.67, true,  false, false, true,  false, false, false, true,  false,  2,  2,  1,  1,  1,  1),
	 GLN ( 6, "Glutamine",     'Q', "GLN",  5, 189,  0.74, false, false, false, true,  false, false, false, false, false, 13,  8,  6,  4,  4,  2),
	 GLU ( 7, "Glutamic Acid", 'E', "GLU",  5, 183,  1.09, false, false, false, true,  true,  false, true,  false, false, 10,  8,  6,  5,  4,  2),
	 GLY ( 8, "Glycine",       'G', "GLY",  0,  85, -0.06, true,  false, false, false, false, false, false, true,  true ,  4,  4,  2,  2,  2,  1),
	 HIS ( 9, "Histidine",     'H', "HIS",  6, 194, -0.04, true,  true,  false, true,  true,  true,  false, false, false, 15, 10,  8,  3,  4,  2),
	 ILE (10, "Isoleucine",    'I', "ILE",  4, 182, -0.74, true,  false, true,  false, false, false, false, false, false,  1,  1,  1,  1,  1,  1),
	 LEU (11, "Leucine",       'L', "LEU",  4, 180, -0.65, true,  false, true,  false, false, false, false, false, false,  1,  1,  1,  1,  1,  1),
	 LYS (12, "Lysine",        'K', "LYS",  5, 211,  2.00, true,  false, false, true,  true,  true,  false, false, false, 14,  9,  7,  6,  4,  2),
	 MET (13, "Methionine",    'M', "MET",  4, 204, -0.71, true,  false, false, false, false, false, false, false, false,  1,  1,  1,  1,  1,  1),
	 PHE (14, "Phenylalanine", 'F', "PHE",  7, 218, -0.67, true,  true,  false, false, false, false, false, false, false,  8,  7,  5,  3,  3,  1),
	 PRO (15, "Proline",       'P', "PRO",  3, 143, -0.44, false, false, false, false, false, false, false, true , false,  7,  6,  4,  2,  2,  1),
	 SER (16, "Serine",        'S', "SER",  2, 122,  0.34, false, false, false, true,  false, false, false, true,  true ,  5,  5,  3,  4,  2,  1),
	 THR (17, "Threonine",     'T', "THR",  3, 146,  0.26, false, false, false, true,  false, false, false, true,  false,  6,  5,  3,  4,  2,  1),
	 TRP (18, "Tryptophan",    'W', "TRP", 10, 259, -0.45, true,  true,  false, true,  false, false, false, false, false,  9,  7,  5,  3,  3,  1),
	 TYR (19, "Tyrosine",      'Y', "TYR",  8, 229,  0.22, true,  true,  false, true,  false, false, false, false, false,  8,  7,  5,  3,  3,  1),
	 VAL (20, "Valine",        'V', "VAL",  3, 160, -0.61, true,  false, true,  false, false, false, false, true , false,  1,  1,  1,  1,  1,  1),
	 XXX ( 0, "Unknown",       'X', "XXX", -1, Double.NaN, Double.NaN, false, false, false, false, false, false, false, false, false, -1, -1, -1, -1, -1, -1),
	 STP (-1, "Stop codon",    '*', "STP", -1, Double.NaN, Double.NaN, false, false, false, false, false, false, false, false, false, -1, -1, -1, -1, -1, -1);	 
		
	private int number;				// we use this instead of ordinal() to define our own values, e.g. for STP
	private String name;			
	private char oneLetterCode;
	private String threeLetterCode; 
	private int numberOfAtoms;		// number of heavy (non-Hydrogen) side chain atoms
	private double asaInExtTripept; // ASA in extended tripeptide conformation (GLY-X-GLY) from Miller et al JMB 1987 (for calculation of relative ASAs)
	private double hydrophobicity;	// empirical hydrophibicity scale by Miller in kcal/mol
	private boolean hydrophobic;
	private boolean aromatic;	
	private boolean aliphatic;		
	private boolean polar;
	private boolean charged;	
	private boolean positive; 		// = basic
	private boolean negative; 		// = acidic	
	private boolean small;	
	private boolean tiny;
	
	// reduced alphabets as defined by  Murphy L.R. et al. 2000 Protein Engineering (especially Fig.1)
	private int reduced15;
	private int reduced10;
	private int reduced8;
	private int reduced6;
	private int reduced4;
	private int reduced2;
	
	/*------------------------- constants ------------------------------*/
	public static final char 	INVALID_ONE_LETTER_CODE 	= '?';
	public static final String 	INVALID_THREE_LETTER_CODE 	= null;
	public static final int 	INVALID_AA_NUMBER 			= -2;
	
	/*---------------------- static variables --------------------------*/
	
	// These variables are being initialized once, when they are first
	// accessed. 
	
	// Retrieval maps: This allows retrieval of amino acids from different
	// representations in O(1) time.
	private static HashMap<Character, AminoAcid> one2aa = initOne2aa();
	private static HashMap<String, AminoAcid> three2aa = initThree2aa();
	private static HashMap<Integer, AminoAcid> num2aa = initNum2aa();
	private static HashMap<String, AminoAcid> full2aa = initFull2aa();
	private static HashMap<Integer, List<AminoAcid>> red15toaa = initRedAlphIdx2aalist(15);
	private static HashMap<Integer, List<AminoAcid>> red10toaa = initRedAlphIdx2aalist(10);
	private static HashMap<Integer, List<AminoAcid>> red8toaa = initRedAlphIdx2aalist(8);
	private static HashMap<Integer, List<AminoAcid>> red6toaa = initRedAlphIdx2aalist(6);
	private static HashMap<Integer, List<AminoAcid>> red4toaa = initRedAlphIdx2aalist(4);
	private static HashMap<Integer, List<AminoAcid>> red2toaa = initRedAlphIdx2aalist(2);
	
	/* ---------------------- constructors -----------------------------*/
	
	/**
	 * Main constructor. Only used internally to create enumeration instances.
	 */
	private AminoAcid(int number, String name, char one, String three, int atoms,
			  double asaInExtTripept,
			  double hydrophobicity,
			  boolean hydrophobic, boolean aromatic, boolean aliphatic,
			  boolean polar,       boolean charged,  boolean positive,
			  boolean negative,    boolean small,    boolean tiny,
			  int reduced15, int reduced10, int reduced8, int reduced6, int reduced4, int reduced2) {
		
		this.number = number;
		this.name = name;
		this.oneLetterCode = one;
		this.threeLetterCode = three;
		this.numberOfAtoms = atoms;
		this.asaInExtTripept = asaInExtTripept;
		this.hydrophobicity = hydrophobicity;
		this.aromatic = aromatic;
		this.hydrophobic = hydrophobic;
		this.aliphatic = aliphatic;
		this.small = small;
		this.tiny = tiny;
		this.positive = positive;
		this.polar = polar;
		this.charged = charged;
		this.negative = negative;
		this.reduced15 = reduced15;
		this.reduced10 = reduced10;
		this.reduced8 = reduced8;
		this.reduced6 = reduced6;
		this.reduced4 = reduced4;
		this.reduced2 = reduced2;
	}
	
	/*---------------------- standard methods --------------------------*/
	/**
	 * Returns the integer id, e.g. 1 for Alanine, 0 for unknown, -1 for stop codon
	 * @return the integer id
	 */
	public int getNumber() { 
		return this.number; 
	}
	
	/**
	 * Returns the full name, e.g. Phenylalanine
	 * @return the full amino acid name
	 */
	public String getName() { 
		return this.name; 
	}
	
	/**
	 * Returns the IUPAC one letter code, e.g. 'A' for Alanine
	 * @return the amino acid one letter code
	 */	
	public char getOneLetterCode() { 
		return this.oneLetterCode; 
	}
	
	/**
	 * Returns the IUPAC three letter code, e.g. "ALA" for Alanine
	 * @return the amino acid three letter code
	 */		
	public String getThreeLetterCode() { 
		return this.threeLetterCode; 
	}
	
	/**
	 * Returns the number of side chain heavy (non-Hydrogen) atoms for this
	 * AminoAcid.
	 * @return number of side chain heavy atoms
	 */
	public int getNumberOfAtoms() {return this.numberOfAtoms; }
	
	/**
	 * Returns the ASA of the aminoacid in an ideal extended tri-peptide conformation 
	 * (GLY-X-GLY, X being this aminoacid) as calculated in Miller et al. 1987 JMB (Table 2)
	 * For calculations of relative ASAs  
	 * @return
	 */
	public double getAsaInExtTripept() {
		return asaInExtTripept;
	}
	
	/**
	 * Returns the empirical hydrophibicity by Miller in kcal/mol
	 * @return the empirical hydrophobicity value
	 */
	public double getHydrophobicity() {return this.hydrophobicity; }
	
	/**
	 * Returns true for aromatic amino acids, false otherwise
	 * @return true iff this amino acid is aromatic
	 */	
	public boolean isAromatic() { return this.aromatic; }
	
	/**
	 * Returns true for hydrophobic amino acids, false otherwise
	 * @return true iff this amino acid is hydrophobic
	 */	
	public boolean isHydrophobic() { return this.hydrophobic; }
	
	/**
	 * Returns true for aliphatic amino acids, false otherwise
	 * @return true iff this amino acid is aliphatic
	 */	
	public boolean isAliphatic() { return this.aliphatic; }
	
	/**
	 * Returns true for small amino acids, false otherwise
	 * @return true iff this amino acid is small
	 */	
	public boolean isSmall() { return this.small; }
	
	/**
	 * Returns true for tiny amino acids, false otherwise
	 * @return true iff this amino acid is tiny
	 */	
	public boolean isTiny() { return this.tiny; }
	
	/**
	 * Returns true for positively charger amino acids, false otherwise
	 * @return true iff this amino acid is positively charged
	 */	
	public boolean isPositive() { return this.positive; }
	
	/**
	 * Returns true for polar amino acids, false otherwise
	 * @return true iff this amino acid is polar
	 */	
	public boolean isPolar() { return this.polar; }
	
	/**
	 * Returns true for charger amino acids, false otherwise
	 * @return true iff this amino acid is charged
	 */	
	public boolean isCharged() { return this.charged; }
	
	/**
	 * Returns true for negatively charged amino acids, false otherwise
	 * @return true iff this amino acid is negatively charged
	 */	
	public boolean isNegative() { return this.negative; }
	
	/**
	 * Returns the index corresponding to grouping the amino-acids into a 15 groups 
	 * reduced alphabet.
	 * See Murphy L.R. et al. 2000 Protein Engineering (especially Fig.1)
	 * Indices start at 1 and are assigned in same order as Fig.1 of Murphy et al.
	 * @return the reduced15 index
	 */
	public int getReduced15() {
		return reduced15;
	}

	/**
	 * Returns the index corresponding to grouping the amino-acids into a 10 groups 
	 * reduced alphabet.
	 * See Murphy L.R. et al. 2000 Protein Engineering (especially Fig.1)
	 * Indices start at 1 and are assigned in same order as Fig.1 of Murphy et al.  
	 * @return the reduced10 index
	 */
	public int getReduced10() {
		return reduced10;
	}

	/**
 	 * Returns the index corresponding to grouping the amino-acids into a 8 groups 
	 * reduced alphabet.
	 * See Murphy L.R. et al. 2000 Protein Engineering (especially Fig.1)
	 * Indices start at 1 and are assigned in same order as Fig.1 of Murphy et al. 
	 * @return the reduced8 index 
	 */
	public int getReduced8() {
		return reduced8;
	}

	/**
	 * Returns the index corresponding to grouping the amino-acids into a 6 groups 
	 * reduced alphabet.
	 * See Mirny and Shakhnovich 1999 JMB.
	 * Indices start at 1 and are assigned approximately matching the order of the 
	 * Murphy et al ones.
	 * @return the reduced6 index
	 */
	public int getReduced6() {
		return reduced6;
	}

	/**
	 * Returns the index corresponding to grouping the amino-acids into a 4 groups 
	 * reduced alphabet.
	 * See Murphy L.R. et al. 2000 Protein Engineering (especially Fig.1)
	 * Indices start at 1 and are assigned in same order as Fig.1 of Murphy et al.  
	 * @return the reduced4 index
	 */
	public int getReduced4() {
		return reduced4;
	}

	/**
	 * Returns the index corresponding to grouping the amino-acids into a 2 groups 
	 * reduced alphabet.
	 * See Murphy L.R. et al. 2000 Protein Engineering (especially Fig.1)
	 * Indices start at 1 and are assigned in same order as Fig.1 of Murphy et al.  
	 * @return the reduced2 index
	 */
	public int getReduced2() {
		return reduced2;
	}

	/**
	 * Returns true for the 20 standard amino acids, false otherwise
	 * @return true iff this is one of the 20 standard amino acids
	 */
	public boolean isStandardAA() {
		return (this.getNumber()<=20 && this.getNumber()>=1);
	}
	
	/*----------------------- static methods ---------------------------*/
	
	/**
	 * Get amino acid object by number
	 * @param num amino acid number (between 0 and 20) (e.g. 0 for Unknown, 1 for Alanine)
	 * @return An amino acid object of the given type or null, if num is invalid
	 */
	public static AminoAcid getByNumber(int num) {
		return num2aa.get(num);
	}	
	
	/**
	 * Get amino acid object by IUPAC one letter code
	 * @param one amino acid one letter code (e.g. 'A' for Alanine)
	 * @return An amino acid object of the given type 
	 * or null, if one letter code is invalid
	 */
	public static AminoAcid getByOneLetterCode(char one) {
		return one2aa.get(Character.toUpperCase(one));
	}
	
	/**
	 * Get amino acid object by IUPAC three letter code
	 * @param three amino acid three letter code (e.g. "ALA" for Alanine)
	 * @return An amino acid object of the given type 
	 * or null, if three letter code is invalid
	 */
	public static AminoAcid getByThreeLetterCode(String three) {
		return three2aa.get(three.toUpperCase());
	}
	
	/**
	 * Get amino acid object by full aminoacid name
	 * @param fullName the full name of an amino acid with first letter capitalised (e.g. "Alanine" or "Aspartic Acid") 
	 * @return an amino acid object of the given type or null if full name is invalid
	 */
	public static AminoAcid getByFullName(String fullName) {
		return full2aa.get(fullName);
	}
	
	/**
	 * Get AminoAcid object by index of the given reduced alphabet.
	 * Valid alphabets are 20, 15, 10, 8, 6, 4, 2.
	 * @param index the index in the specified reduced alphabet
	 * @param reducedAlphabet one of 20, 15, 10, 8, 6, 4, 2
	 * @return an amino acid object corresponding to the given index or null 
	 * if index is invalid or reducedAlphabet is invalid
	 */
	public static List<AminoAcid> getByReducedAlphabetIndex(int index, int reducedAlphabet) {
		List<AminoAcid> list = new ArrayList<AminoAcid>();
		switch(reducedAlphabet) {
		case 20:
			// for completeness we also put 20 here
			list.add(getByNumber(index));
		case 15:
			return red15toaa.get(index);
		case 10:
			return red10toaa.get(index);
		case 8:
			return red8toaa.get(index);
		case 6:
			return red6toaa.get(index);
		case 4:
			return red4toaa.get(index);
		case 2:
			return red2toaa.get(index);
		}
		return list;
	}
	
	/**
	 * Get amino acids in ascending order of hydrophobicity.
	 * @return an array containing the amino acids in order of hydrophobicity
	 */
	public static AminoAcid[] valuesInOrderOfHydrophobicity() {
		AminoAcid[] values = AminoAcid.values();
		AminoAcid[] newValues = new AminoAcid[values.length];
		System.arraycopy(values, 0, newValues, 0, values.length);
		java.util.Arrays.sort(newValues, new Comparator<AminoAcid>() {
			public int compare(AminoAcid a, AminoAcid b) {
				return new Double(a.hydrophobicity).compareTo(new Double(b.hydrophobicity));
			}
		});		
		return newValues;
	}
	
	/**
	 * Returns a list of all 20 standard amino acids as a Collection of AminoAcid objects.
	 * @return a collection of the 20 standard amino acids
	 */
	public static Collection<AminoAcid> getAllStandardAAs() {
		Collection<AminoAcid> list = new ArrayList<AminoAcid>();
		for (AminoAcid aa:AminoAcid.values()) {
			if (aa.getNumber()<21 && aa.getNumber()>0) {
				list.add(aa);
			}
		}
		return list;
	}
	
	// conversion methods
	
	/**
	 * Converts amino acid one letter code to three letter code
	 * @param one amino acid one letter code (e.g. 'A' for Alanine)
	 * @return amino acid three letter code or INVALID_THREE_LETTER_CODE if input is invalid
	 */
	public static String one2three(char one) {
		AminoAcid aa = getByOneLetterCode(one);
		return aa==null?INVALID_THREE_LETTER_CODE:aa.getThreeLetterCode();
	}
	
	/**
	 * Converts amino acid three letter code to one letter code
	 * @param three amino acid three letter code (e.g. "ALA" for Alanine)
	 * @return amino acid one letter code or INVALID_ONE_LETTER_CODE if input is invalid
	 */
	public static char three2one(String three) {
		AminoAcid aa = getByThreeLetterCode(three);
		return aa==null?INVALID_ONE_LETTER_CODE:aa.getOneLetterCode();
	}
	
	/**
	 * Converts amino acid three letter code to number
	 * @param three amino acid three letter code (e.g. "ALA" for Alanine)
	 * @return amino acid number (between 1 and 20) or INVALID_AA_NUMBER if input is invalid
	 */
	public static int three2num(String three) {
		AminoAcid aa = getByThreeLetterCode(three);
		return aa==null?INVALID_AA_NUMBER:aa.getNumber();		
	}
	
	/**
	 * Converts amino acid number to three letter code 
	 * @param num amino acid number (between -1 and 20) (e.g. 1 for Alanine, -1 for stop codon)
	 * @return amino acid three letter code or null if input is invalid  
	 */
	public static String num2three(int num) {
		AminoAcid aa = getByNumber(num);
		return aa==null?INVALID_THREE_LETTER_CODE:aa.getThreeLetterCode();
	}

	/**
	 * Converts amino acid one letter code to number
	 * @param one amino acid one letter code (e.g. 'A' for Alanine)
	 * @return amino acid number (between 1 and 20 or 0 for unknown type, -1 for stop codon) 
	 * or -2 if input is invalid
	 */
	public static int one2num(char one) {
		AminoAcid aa = getByOneLetterCode(one);
		return aa==null?INVALID_AA_NUMBER:aa.getNumber();
	}
	
	/**
	 * Converts amino acid number to one letter code 
	 * @param num amino acid number (between -1 and 20) (e.g. 1 for Alanine, -1 for stop codon, 0 for unknown)
	 * @return amino acid one letter code or INVALID_ONE_LETTER_CODE if input is invalid  
	 */
	public static char num2one(int num) {
		AminoAcid aa = getByNumber(num);
		return aa==null?INVALID_ONE_LETTER_CODE:aa.getOneLetterCode();		
	}	
	
	// information methods
	
	/**
	 * Returns true if given string is the three-letter code of a standard aminoacid,
	 * false otherwise
	 * @param three string to test
	 * @return true if given string is the three-letter code of a standard aminoacid, 
	 * false otherwise
	 */
	public static boolean isStandardAA(String three) {
		AminoAcid aa = getByThreeLetterCode(three);
		return aa==null?false:aa.isStandardAA();
	}

	/**
	 * Returns true if given char is the one-letter code of a standard aminoacid,
	 * false otherwise
	 * @param one char to test
	 * @return true if given char is the one-letter code of a standard aminoacid,
	 * false otherwise
	 */
	public static boolean isStandardAA(char one) {
		AminoAcid aa = getByOneLetterCode(one);
		return aa==null?false:aa.isStandardAA();		
	}

	/**
	 * Checks whether a reduced alphabet of the given size exists
	 * @param num number to test
	 * @return true if given num is a valid number of groups of a reduced alphabet, false otherwise
	 */
	public static boolean isValidNumGroupsReducedAlphabet(int num) {
		if (num==20 || num==15 || num==10 || num==8 || num==6 || num==4 || num==2) {
			return true;
		}
		return false;
	}
	
	/**
	 * Prints information about the reduced alphabets.
	 */
	public static void printReducedAlphabetInfo() {
		int[] alphabets = {15, 10, 8, 6, 4, 2};
		for (int numGroups:alphabets) {
			System.out.println("Alphabet of "+numGroups+" groups");
			for (int i=1;i<=numGroups;i++){
				List<AminoAcid> list = AminoAcid.getByReducedAlphabetIndex(i, numGroups);
				System.out.print(i+": ");
				for (AminoAcid aa:list) {
					System.out.print(aa.getOneLetterCode()+" ");
				}
				System.out.print("  ");
			}
			System.out.println();
		}
	}
	

	/*----------------------- private methods --------------------------*/
	/**
	 * initialize static map to get amino acid by its ordinal number
	 */
	private static HashMap<Integer, AminoAcid> initNum2aa() {
		HashMap<Integer, AminoAcid> num2aa = new HashMap<Integer, AminoAcid>();
		for(AminoAcid aa:AminoAcid.values()) {
			num2aa.put(aa.getNumber(), aa);
		}
		return num2aa;
	}
	
	/**
	 * initialize static map to get amino acid list by its reduced alphabet index
	 */
	private static HashMap<Integer, List<AminoAcid>> initRedAlphIdx2aalist(int reducedAlphabet) {
		HashMap<Integer, List<AminoAcid>> num2list = new HashMap<Integer, List<AminoAcid>>();

		for(AminoAcid aa:AminoAcid.values()) {
			int index = -1;
			switch(reducedAlphabet) {
			case 20:
				index = aa.getNumber();
				break;
			case 15:
				index = aa.getReduced15();
				break;
			case 10:
				index = aa.getReduced10();
				break;
			case 8:
				index = aa.getReduced8();
				break;
			case 6:
				index = aa.getReduced6();
				break;
			case 4:
				index = aa.getReduced4();
				break;
			case 2:
				index = aa.getReduced2();
				break;
			}
			if (num2list.containsKey(index)) {
				num2list.get(index).add(aa);
			} else {
				List<AminoAcid> list = new ArrayList<AminoAcid>();
				list.add(aa);
				num2list.put(index,list);
			}
		}
		return num2list;
	}

	/**
	 * initialize static map to get amino acid by its one letter code
	 */
	private static HashMap<Character, AminoAcid> initOne2aa() {
		HashMap<Character, AminoAcid> one2aa = new HashMap<Character, AminoAcid>();
		for(AminoAcid aa:AminoAcid.values()) {
			one2aa.put(aa.getOneLetterCode(), aa);
		}
		return one2aa;
	}
	
	/**
	 * initialize static map to get amino acid by its three letter code
	 */
	private static HashMap<String, AminoAcid> initThree2aa() {
		HashMap<String, AminoAcid> three2aa = new HashMap<String, AminoAcid>();
		for(AminoAcid aa:AminoAcid.values()) {
			three2aa.put(aa.getThreeLetterCode(), aa);
		}
		return three2aa;
	}	

	/**
	 * initialize static map to get amino acid by its full name
	 */
	private static HashMap<String, AminoAcid> initFull2aa()	{
		HashMap<String, AminoAcid> full2aa = new HashMap<String, AminoAcid>();
		for (AminoAcid aa:AminoAcid.values()) {
			full2aa.put(aa.getName(),aa);
		}
		return full2aa;
	}
	
	
}

