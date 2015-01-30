package eppic.commons.sequence;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eppic.commons.util.Interval;
import eppic.commons.util.IntervalSet;


/**
 * A multiple protein sequence alignment. This class represents a set of
 * protein sequences which are globally aligned and provides functions
 * to map between the original and the aligned sequences and for reading
 * and writing alignment files (PIR, FASTA, CLUSTAL). 
 * 
 * @author		Henning Stehr, Jose Duarte, Lars Petzold
 */
public class MultipleSequenceAlignment implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/*------------------------------ constants ------------------------------*/	

	public static final String PIRFORMAT = "PIR";
	public static final String FASTAFORMAT = "FASTA";
	public static final String CLUSTALFORMAT = "CLUSTAL";
	public static final char GAPCHARACTER = '-';
	private static final String FASTAHEADER_REGEX = "^>\\s*([a-zA-Z0-9_|\\-.]+)";
	public static final String FASTAHEADER_CHAR = ">";
	private static final String PIRHEADER_REGEX = "^>[A-Z0-9][A-Z0-9];([a-zA-Z0-9_|\\-.]+)";
	public static final String PIRHEADER_CHAR = ">";
	public static final String PIRSEQEND = "*";
	/*--------------------------- member variables --------------------------*/		
	
	private String[] sequences;
	
	private TreeMap<String, Integer> tags2indices; 	// sequence tag to index in the sequences array (starting at 0)
	private TreeMap<Integer, String> indices2tags;	// sequence index to sequence tag

	private TreeMap<Integer,int[]> mapAlign2Seq; // map of seq index to arrays mapping alignment serials to sequence serials 
	private TreeMap<Integer,int[]> mapSeq2Align; // map of seq index to arrays mapping sequence serials to alignment serials
	
	/*----------------------------- constructors ----------------------------*/
	
	/**
	 * Creates an Alignment from a file in either FASTA, PIR or DALI format
	 * @param fileName
	 * @param format, one of {@link #PIRFORMAT}, {@link #FASTAFORMAT},  {@link #DALIFORMAT} or {@link #CLUSTALFORMAT}
	 * @throws IOException
	 * @throws FileFormatException
	 * @throws AlignmentConstructionException 
	 */
	public MultipleSequenceAlignment(String fileName, String format) throws IOException, FileFormatException, AlignmentConstructionException {
		if (format.equals(PIRFORMAT)){
			readFilePIRFormat(fileName);
		} else if (format.equals(FASTAFORMAT)){
			readFileFastaFormat(fileName);
		} else if (format.equals(CLUSTALFORMAT)) {
			readFileClustalFormat(fileName);
		} else {
			throw new IllegalArgumentException("Format "+format+" not supported by Alignment class");
		}
		
		// checking lengths, i.e. checking we read correctly from file
		checkLengths();
		// map sequence serials (starting at 1, no gaps) to alignment serials (starting at 1, possibly gaps)
		doMapping();
		
		// if indices2tags/tags2indices length don't match sequences length then there were duplicate tags in the file
		if (indices2tags.size()!=sequences.length || tags2indices.size()!=sequences.length) {
			throw new AlignmentConstructionException("There are duplicate tags in the file "+fileName);
		}

	}
		
	/**
	 * Creates a trivial alignment given a Map of tags to sequences
	 * The sequences must have the same lengths. 
	 * @param sequences a Map of sequence tags to sequences, the sequences must have the same length
	 * @throws AlignmentConstructionException if sequences lengths differ or if 
	 * size of given map is 0
	 */
	public MultipleSequenceAlignment(TreeMap<String, String> sequences) throws AlignmentConstructionException {

		if (sequences.size() == 0) {
			throw new AlignmentConstructionException("No sequences were passed for constructing the alignment.");
		}
		// check that sequences have the same length
		int length = sequences.get(sequences.firstKey()).length();
		for(String seqTag: sequences.keySet()) {
			if(sequences.get(seqTag).length() != length) {
				throw new AlignmentConstructionException("Cannot create trivial alignment. Sequence lenghts are not the same.");
			}
		}
		
		this.sequences = new String[sequences.size()];
		this.indices2tags = new TreeMap<Integer, String>();
		this.tags2indices = new TreeMap<String, Integer>();
		
		int i=0;
		for (String seqTag: sequences.keySet()) {
			this.sequences[i]=sequences.get(seqTag);
			this.indices2tags.put(i,seqTag);
			this.tags2indices.put(seqTag, i);
			i++;
		}
		doMapping();
		
	}
	
	/**
	 * Creates a trivial alignment given an array of tags and an array of sequences
	 * @param seqTags
	 * @param sequences an array of sequences, they must have the same length
	 * @throws AlignmentConstructionException if different number of sequences and 
	 * tags given, or if size of given array is 0
	 */
	public MultipleSequenceAlignment(String[] seqTags, String[] sequences) throws AlignmentConstructionException {
		if (seqTags.length!=sequences.length) {
			throw new AlignmentConstructionException("Different number of sequences and tags given.");
		}
		if (sequences.length == 0) {
			throw new AlignmentConstructionException("No sequences were passed for constructing the alignment.");
		}
		// check that sequences have the same length
		int length = sequences[0].length();
		for(String sequence: sequences) {
			if(sequence.length() != length) {
				throw new AlignmentConstructionException("Cannot create trivial alignment. Sequence lenghts are not the same.");
			}
		}
		
		this.sequences = new String[sequences.length];
		this.indices2tags = new TreeMap<Integer, String>();
		this.tags2indices = new TreeMap<String, Integer>();
		
		for (int i=0;i<sequences.length;i++) {
			this.sequences[i]=sequences[i];
			this.indices2tags.put(i,seqTags[i]);
			this.tags2indices.put(seqTags[i], i);
		}
		doMapping();

	}
	
	/**
	 * Creates a trivial alignment given a list of Sequences
	 * @param sequences a List of Sequence objects, the sequences must have the same length
	 * @throws AlignmentConstructionException
	 */
	public MultipleSequenceAlignment(List<Sequence> sequences) throws AlignmentConstructionException {
		if (sequences.size() == 0) {
			throw new AlignmentConstructionException("No sequences were passed for constructing the alignment.");
		}
		// check that sequences have the same length
		int length = -1;
		for(Sequence sequence: sequences) {
			if (length==-1) {
				length = sequence.getLength();
				continue;
			}
			if(sequence.getLength() != length) {
				throw new AlignmentConstructionException("Cannot create trivial alignment. Sequence lenghts are not the same.");
			}
		}
		
		this.sequences = new String[sequences.size()];
		this.indices2tags = new TreeMap<Integer, String>();
		this.tags2indices = new TreeMap<String, Integer>();
		
		for (int i=0;i<sequences.size();i++) {
			this.sequences[i]=sequences.get(i).getSeq();
			this.indices2tags.put(i,sequences.get(i).getName());
			this.tags2indices.put(sequences.get(i).getName(), i);
		}
		doMapping();		
	}
	
	/*---------------------------- private methods --------------------------*/
	
	/**
	 * Initializes the maps to map from sequence indices to alignment indices and vice versa.
	 * Both sequence and alignment indices start at 1
	 */
	private void doMapping() {
		this.mapAlign2Seq = new TreeMap<Integer, int[]>();
		this.mapSeq2Align = new TreeMap<Integer, int[]>();
				
		for (int i=0;i<sequences.length;i++){
			String seq = sequences[i];
			
			int[] mapAl2Seq = new int[seq.length()+1];
			int[] mapSeq2Al = new int[getSequenceNoGaps(indices2tags.get(i)).length()+1];
			int seqIndex = 1;
			for (int alignIndex=1;alignIndex<=seq.length();alignIndex++){
				if (seq.charAt(alignIndex-1)!=GAPCHARACTER) {
					mapAl2Seq[alignIndex] = seqIndex;
					mapSeq2Al[seqIndex] = alignIndex;
					seqIndex++;
				} else { // for gaps we assign a -1
					mapAl2Seq[alignIndex] = -1;
				}
			}
			mapAlign2Seq.put(i, mapAl2Seq);
			mapSeq2Align.put(i , mapSeq2Al);
		}
	}
	
	private void checkLengths() throws AlignmentConstructionException {
		if (sequences.length==0) return;
		
		int firstLength = 0;
		for (int i=0;i<sequences.length;i++) {
			if (i==0) {
				firstLength = sequences[i].length();
			} else {
				if (sequences[i].length()!=firstLength) {
					throw new AlignmentConstructionException("Error: Some sequences in alignment have different lengths.");
				}
			}
		}
	}
	
	private void readFilePIRFormat(String fileName) throws IOException, FileFormatException {
		String 	nextLine = "",
				currentSeq = "",
				currentSeqTag = "";
		boolean foundPirHeader = false;
		int lineNum = 0;
		int seqIndex = 0;
		int nonEmptyLine = 0;

		// open file

		BufferedReader fileIn = new BufferedReader(new FileReader(fileName));

		// otherwise initialize TreeMap of sequences and rewind file
		ArrayList<String> seqsAL = new ArrayList<String>();
		indices2tags = new TreeMap<Integer, String>();
		tags2indices = new TreeMap<String, Integer>();
		//fileIn.reset();

		// read sequences
		while((nextLine = fileIn.readLine()) != null) {
		    ++lineNum;
			nextLine = nextLine.trim();					    // remove whitespace
			if(nextLine.length() > 0) {						// ignore empty lines
				nonEmptyLine++;
				if (nonEmptyLine==1 && !nextLine.startsWith(PIRHEADER_CHAR)) { // quick check for PIR format
					fileIn.close();
					throw new FileFormatException("First non-empty line of file "+fileName+" does not seem to be a PIR header.");
				}
				if(nextLine.endsWith(PIRSEQEND)) {				// end of sequence
					currentSeq += nextLine.substring(0, nextLine.length() - 1);
					seqsAL.add(currentSeq);
					indices2tags.put(seqIndex,currentSeqTag);
					tags2indices.put(currentSeqTag,seqIndex);
					seqIndex++;
				} else {
					Pattern p = Pattern.compile(PIRHEADER_REGEX);
					Matcher m = p.matcher(nextLine);
					if (m.find()){				// start new sequence
						fileIn.readLine();      // skip description line
						currentSeq = "";						
						currentSeqTag=m.group(1);
						foundPirHeader = true;
					} else {
						currentSeq = currentSeq + nextLine;     // read sequence
					}
				}
			}
		} // end while

		sequences = new String[seqsAL.size()];
		seqsAL.toArray(sequences);
		
		fileIn.close();
		
		// if no pir headers found, file format is wrong
		if(!foundPirHeader) {
		    throw new FileFormatException("File does not conform with Pir file format (could not detect any Pir header in the file).",fileName,(long)lineNum);
		}
		
	}

	private void readFileFastaFormat(String fileName) throws IOException, FileFormatException {
		String 	nextLine = "",
				currentSeq = "",
				lastSeqTag = "";
		boolean foundFastaHeader = false;
		long lineNum = 0;
		int seqIndex = 0;
		int nonEmptyLine = 0;
		
		// open file

		BufferedReader fileIn = new BufferedReader(new FileReader(fileName));

		// read file  	

		// initialize TreeMap of sequences 
		ArrayList<String> seqsAL = new ArrayList<String>();
		tags2indices = new TreeMap<String, Integer>();
		indices2tags = new TreeMap<Integer, String>();

		// read sequences
		while((nextLine = fileIn.readLine()) != null) {
		    ++lineNum;
			nextLine = nextLine.trim();					    // remove whitespace
			if(nextLine.length() > 0) {						// ignore empty lines
				nonEmptyLine++;
				if (nonEmptyLine==1 && !nextLine.startsWith(">")) { // quick check for FASTA format
					fileIn.close();
					throw new FileFormatException("First non-empty line of FASTA file "+fileName+" does not seem to be a FASTA header.");
				}
				Pattern p = Pattern.compile(FASTAHEADER_REGEX);
				Matcher m = p.matcher(nextLine);
				if (m.find()){
					if (!lastSeqTag.equals("")) {
						seqsAL.add(currentSeq);
						indices2tags.put(seqIndex, lastSeqTag);
						tags2indices.put(lastSeqTag, seqIndex);
						currentSeq = "";
						seqIndex++;
					}
					lastSeqTag=m.group(1);
					foundFastaHeader = true;
				} else {
					currentSeq += nextLine;
				}
			}
		} // end while
		// inserting last sequence
		seqsAL.add(currentSeq);
		indices2tags.put(seqIndex,lastSeqTag);
		tags2indices.put(lastSeqTag,seqIndex);

		sequences = new String[seqsAL.size()];
		seqsAL.toArray(sequences);
		
		fileIn.close();
		
		// if no fasta headers found, file format is wrong
		if(!foundFastaHeader) {
		    throw new FileFormatException("File does not conform with FASTA file format (could not find any FASTA header in the file).",fileName,lineNum);
		}
		
	}
    
	private void readFileClustalFormat(String fileName) throws IOException, FileFormatException {
		// open file
		BufferedReader fileIn = new BufferedReader(new FileReader(fileName));

		// initialize TreeMap of sequences 
		TreeMap<String,StringBuffer> tags2seqs = new TreeMap<String, StringBuffer>();
		tags2indices = new TreeMap<String, Integer>();
		indices2tags = new TreeMap<Integer, String>();

		// read sequences
		String line;
		int lineNum=0;
		int seqIdx = 0;
		Pattern p = Pattern.compile("^(\\S+)\\s+([a-zA-Z\\-]+).*"); // regex for the sequence lines
		while((line = fileIn.readLine()) != null) {
		    ++lineNum;
			if (lineNum == 1) {
				if (!line.startsWith("CLUSTAL")) {
					fileIn.close();
					throw new FileFormatException("File "+fileName+" does not conform with CLUSTAL format (first line does not start with CLUSTAL)");
				}
				continue;
			}
			line = line.trim();
			if(line.length()==0) {
				continue;
			}
			
			Matcher m = p.matcher(line);
			if (m.matches()) {
				if (!tags2seqs.containsKey(m.group(1))) {
					tags2seqs.put(m.group(1),new StringBuffer(m.group(2)));
					tags2indices.put(m.group(1), seqIdx);
					indices2tags.put(seqIdx, m.group(1));
					seqIdx++;
				} else {
					tags2seqs.get(m.group(1)).append(m.group(2));
				}
			}
		} 

		
		sequences = new String[tags2seqs.size()];

		for (seqIdx=0;seqIdx<sequences.length;seqIdx++) {
			sequences[seqIdx]=tags2seqs.get(indices2tags.get(seqIdx)).toString().toUpperCase();
		}
		
		fileIn.close();
				
	}
	
	/*---------------------------- public methods ---------------------------*/
	
	/**
	 * @return a deep copy of this alignment
	 */
	public MultipleSequenceAlignment copy() throws AlignmentConstructionException {
		String[] newSeqs = sequences.clone();
		String[] newTags = new String[newSeqs.length];
		for (int i = 0; i < newTags.length; i++) {
			newTags[i] = indices2tags.get(i);
		}
		return new MultipleSequenceAlignment(newTags, newSeqs);
	}
	
	/**
	 * Adds a sequence to the alignment. The sequence has to have the same length as the alignment
	 * (possibly containing gap characters) otherwise an AlignmentConstructionError is thrown.
	 * The internal mappings are regenerated for the new alignment.
	 * @param newTag the sequence name
	 * @param newSeq the new sequence
	 * @throws AlignmentConstructionException if given newSeq differs in length from this alignment or if 
	 * given newTag already exists in this alignment
	 */
	public void addSequence(String newTag, String newSeq) throws AlignmentConstructionException {
		int l = this.getAlignmentLength();
		// check length of new sequence
		if(newSeq.length() != l) {
			throw new AlignmentConstructionException("Cannot add sequence of length " + newSeq.length() + " to alignment of length " + l);
		}
		// make sure that tag does not exist yet
		if(tags2indices.containsKey(newTag)) {
			throw new AlignmentConstructionException("Cannot add sequence. Tag " + newTag + " exists in alignment.");
		}
		
		int oldNumSeqs = sequences.length;
		String[] newSequences = new String[oldNumSeqs+1];
		for (int i = 0; i < oldNumSeqs; i++) {
			newSequences[i] = this.sequences[i];
		}
		newSequences[oldNumSeqs] = newSeq;				// add to sequence array
		this.sequences = newSequences;
		
		this.indices2tags.put(oldNumSeqs, newTag);
		this.tags2indices.put(newTag, oldNumSeqs);
		
		// regenerate position mapppings
		doMapping();
		
	}
	
	/**
	 * Returns a deep copy of this alignment where the given sequence has been added.
	 * @param newTag the sequence name
	 * @param newSeq the new sequence (possibly containing gaps)
	 * @return a deep copy of this alignment where the given sequence has been added.
	 * @throws AlignmentConstructionException if given newSeq differs in length from this alignment or if 
	 * given newTag already exists in this alignment 
	 */
	public MultipleSequenceAlignment copyAndAdd(String newTag, String newSeq) throws AlignmentConstructionException {
		MultipleSequenceAlignment newAl = this.copy();
		newAl.addSequence(newTag, newSeq);
		return newAl;
	}
	
	/**
	 * Returns the gap character
	 * @return The gap character
	 */
    public static char getGapCharacter() { 
    	return GAPCHARACTER; 
    }
	
	/**
	 * Returns the sequence (with gaps) given a sequence tag
	 * @param seqTag
	 * @return
	 */
    public String getAlignedSequence(String seqTag) { 
    	return sequences[tags2indices.get(seqTag)]; 
    }
    
    /**
     * Returns the length of the alignment (including gaps) 
     * @return
     */
    public int getAlignmentLength() { 
    	return sequences[0].length(); 
    }
    
    /**
     * Returns the total number of sequences in the alignment
     * @return
     */
    public int getNumberOfSequences() { 
    	return sequences.length; 
    }
	
    /**
     * Gets the sequence tag from the sequence index
     * @param i
     * @return
     */
    public String getTagFromIndex(int i) {
    	return indices2tags.get(i);
    }
    
    /**
     * Gets the sequence index from the sequence tag
     * @param seqTag
     * @return
     */
    public int getIndexFromTag(int seqTag) {
    	return tags2indices.get(seqTag);
    }
    
    /**
     * Returns true if alignment contains the sequence identified by seqTag
     * @param seqTag
     * @return
     */
    public boolean hasTag(String seqTag){
    	return tags2indices.containsKey(seqTag);
    }
    
    /**
     * Reset the tags to the given set of tags
     * @param newTags
     * @throws IllegalArgumentException if length of array of tags is different
     *  from number of sequences in this Alignment
     */
   	public void resetTags(String[] newTags) {
   		if (newTags.length!=this.getNumberOfSequences()) {
   			throw new IllegalArgumentException("Number of tags given for resetting of tags differ from number of sequences in the alignment");
   		}
   		tags2indices = new TreeMap<String, Integer>();
   		indices2tags = new TreeMap<Integer, String>();
   		for (int i=0; i<newTags.length; i++) {
   			tags2indices.put(newTags[i],i);
   			indices2tags.put(i, newTags[i]);
   		}
    }
    
   	/**
   	 * Resets given existingTag to newTag
   	 * @param existingTag
   	 * @param newTag
   	 * @throws IllegalArgumentException if existingTag not present in this Alignment
   	 */
   	public void resetTag(String existingTag, String newTag) {
   		if (!tags2indices.containsKey(existingTag)) {
   			throw new IllegalArgumentException("Given tag "+existingTag+" doesn't exist in this Alignment");
   		} 
   		int i = tags2indices.get(existingTag);
   		indices2tags.put(i, newTag);
   		tags2indices.remove(existingTag);
   		tags2indices.put(newTag, i);
   		
   	}
   	
   	/**
   	 * Removes the sequence corresponding to the given tag.
   	 * @param tag
   	 */
   	public void removeSequence(String tag) {
   		if (!tags2indices.containsKey(tag)) {
   			throw new IllegalArgumentException("Given tag "+tag+" doesn't exist in this Alignment");
   		} 
 
   		int idxToRemove = tags2indices.get(tag);

   		String[] newSequences = new String[this.sequences.length-1];
		TreeMap<Integer,String> newIndices2tags = new TreeMap<Integer, String>();
		TreeMap<String,Integer> newTags2indices = new TreeMap<String, Integer>();
		
		int j = 0; // new indexing
		for (int i=0;i<this.sequences.length;i++) {
			if (i!=idxToRemove) {
				newSequences[j] = this.sequences[i];
				newIndices2tags.put(j,this.indices2tags.get(i));
				newTags2indices.put(this.indices2tags.get(i), j);
				j++;
			}
		}
		
		this.sequences = newSequences;
		this.indices2tags = newIndices2tags;
		this.tags2indices = newTags2indices;
		
		doMapping();

   	}
   	
    /**
     * Returns all sequence tags in a Collection<String>
     * Conserves the order of the sequences as they were added to the Alignment
     * @return
     */
    public Collection<String> getTags(){
    	return indices2tags.values();
    }
    
    /**
     * Returns sequence seqTag with no gaps
     * @param seqNumber
     * @return
     */
    public String getSequenceNoGaps(String seqTag){
    	String seq = "";
    	for (int i=0;i<getAlignmentLength();i++){
    		char letter = getAlignedSequence(seqTag).charAt(i);
    		if (letter!=GAPCHARACTER){
    			seq+=letter;
    		}
    	}
    	return seq;
    }
    
    /**
     * Given the alignment index (starting at 1, possibly gaps),
     * returns the sequence index (starting at 1, no gaps) of sequence seqTag
     * @param seqTag
     * @param alignIndex
     * @throws IndexOutOfBoundsException if 0 given as alignIndex or else if 
     * alignIndex is bigger than maximum stored index 
     * @return the sequence index, -1 if sequence is a gap at that position
     */
    public int al2seq(String seqTag, int alignIndex){
    	if (alignIndex==0) throw new IndexOutOfBoundsException("Disallowed alignment index (0) given");
    	return mapAlign2Seq.get(tags2indices.get(seqTag))[alignIndex];
    }
    
    /**
     * Given sequence index (starting at 1, no gaps) of sequence seqTag,
     * returns the alignment index (starting at 1, possibly gaps)
     * @param seqTag
     * @param seqIndex
     * @throws IndexOutOfBoundsException if 0 given as seqIndex or else if 
     * seqIndex is bigger than maximum stored index
     * @return the alignment index
     */
    public int seq2al(String seqTag, int seqIndex) {
    	if (seqIndex==0) throw new IndexOutOfBoundsException("Disallowed sequence index (0) given");
    	return mapSeq2Align.get(tags2indices.get(seqTag))[seqIndex];
    }
    
    /**
     * Gets column alignIndex of the alignment as a String
     * @param alignIndex
     * @return
     */
    public String getColumn(int alignIndex){
    	String col="";
    	for (String seq:sequences){
    		col+=seq.charAt(alignIndex-1);
    	}
    	return col;
    }
    
    /**
     * Calculate the entropy of column alignIndex of the alignment, assuming that this is
     * a multiple protein sequence alignment (i.e. not a nucleotide alignment).
     * Gaps are not considered in the entry summation, but the calculation of amino acid 
     * column probabilities do take gaps into account.
     * @param alignIndex the column of the alignment
     * @param numGroupsAlphabet the number of groups in the alphabet to be used, valid 
     * values are 20, 15, 10, 8, 6, 4, 2 see {@link AminoAcid} enum  
     * @return
     */
    public double getColumnEntropy(int alignIndex, int numGroupsAlphabet) {
    	int[] counts = getColumnCounts(alignIndex, numGroupsAlphabet);
    	
    	// important: we are considering also gaps when calculating probabilities
    	
    	double sumplogp = 0.0;
    	double log2 = Math.log(2);
    	
		for (int i=1;i<=numGroupsAlphabet;i++){
			double prob = (double)counts[i]/(double)this.getNumberOfSequences(); // i.e. we consider gaps!
			if (prob!=0){ // plogp is defined to be 0 when p=0 (because of limit). If we let java calculate it, it gives NaN (-infinite) because it tries to compute log(0) 
				sumplogp += prob*(Math.log(prob)/log2);
			}
		}
		return (-1.0)*sumplogp;
    }   
    
    /**
     * Gets the counts of groups of aminoacids for the column alignIndex
     * @param alignIndex the column of the alignment
     * @param numGroupsAlphabet the number of groups in the alphabet to be used, valid 
     * values are 20, 15, 10, 8, 6, 4, 2 see {@link AminoAcid} enum 
     * @return an array of size numGroupsAlphabet+1 with indices containing the 
     * counts of groups, the indices correspond to those of the {@link AminoAcid} enum, 
     * the 0 index contains the count of gaps in the column
     * @throws IllegalArgumentException if the numGroupsAlphabet given is not one of the
     * valid ones
     */
    public int[] getColumnCounts(int alignIndex, int numGroupsAlphabet) {
    	String column = getColumn(alignIndex);
    	
    	// we use 0 for the gap counts, the rest for the AminoAcid classes counts (see AminoAcid enum)
    	int[] counts = new int[numGroupsAlphabet+1];  
    	 
    	for (int i=0;i<column.length();i++) {
    		char letter = column.charAt(i);
    		
    		if (letter==GAPCHARACTER) {
    			counts[0]++;
    		}     			
    		else if (AminoAcid.isStandardAA(letter)) {
    			AminoAcid aa = AminoAcid.getByOneLetterCode(letter);
    			int index;
    			switch(numGroupsAlphabet) {
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
    			default:
    				throw new IllegalArgumentException("A "+numGroupsAlphabet+ " groups alphabet is an invalid alphabet");	
    			}
    			counts[index]++;    		
    		}
    		// notice that non-standard aas are not counted neither as gap or as class, that should not be a big problem in most cases
    	}
    	return counts;
    }

    /**
     * Prints to given PrintStream profile information for the given tag's sequence, assuming 
     * it is a protein sequence (only aminoacids): aminoacid column counts and entropies
     * @param ps
     * @param tag the sequence tag for which the column counts will be computed and printed
     * @param numGroupsAlphabet the number of groups in the alphabet to be used, valid 
     * values are 20, 15, 10, 8, 6, 4, 2 see {@link AminoAcid} enum
     */
    public void printProfile(PrintStream ps, String tag, int numGroupsAlphabet) {
    	String sequence = this.getSequenceNoGaps(tag);
		ps.print("\t");
		for (int j=1;j<=20;j++) {
			ps.print("\t"+AminoAcid.getByNumber(j).getOneLetterCode());
		}
		ps.println();
		for (int i=1;i<=sequence.length();i++){
			// this is not very efficient, we are counting twice: when calling getColumnEntropy and getColumnCounts
			// TODO rewrite if this becomes a bottleneck
			double entropy = this.getColumnEntropy(this.seq2al(tag, i), numGroupsAlphabet);
			int[] counts = this.getColumnCounts(this.seq2al(tag, i), numGroupsAlphabet);
			ps.print(i+"\t"+sequence.charAt(i-1));
			int sum = 0;
			for (int j=1;j<=numGroupsAlphabet;j++) {
				ps.printf("\t%d",counts[j]);
				sum+=counts[j];
			}
			ps.printf("\t%d\t%5.2f\n",sum,entropy);
		}
    }
    
    /**
     * Prints alignment by columns in tab delimited format,
     * useful to import to MySQL
     */
    public void printTabDelimited(){
    	for (int alignIndex=1;alignIndex<getAlignmentLength();alignIndex++){
    		for (String seq:sequences){
    			System.out.print(seq.charAt(alignIndex-1)+"\t");
    		}
    		System.out.print(alignIndex+"\t");
    		for (int i=0; i<sequences.length;i++){
    			int seqIndex = al2seq(indices2tags.get(i), alignIndex); 
    			if (seqIndex!=-1){ // everything not gaps
    				System.out.print(seqIndex+"\t");
    			} else {  // gaps
    				System.out.print("\\N\t");
    			}
    		}
    		System.out.println();
    	}
    }
    
    /**
     * Prints the alignment in simple text format (without sequence tags) to stdout
     */
    public void printSimple() {
    	for(String sequence: sequences) {
    		System.out.println(sequence);
    	}
    }
    
    /**
     * Prints the alignment in fasta format to stdout
     */
    public void printFasta() {
   		writeFasta(System.out,80,true);
    }

	/**
	 * Writes alignment to the given output stream. The output format 
	 * conforms to the FASTA format.
	 * @param out  the PrintStream to print to
	 * @param lineLength  the maximal line length, setting this to null 
	 *  always results in 80 characters per line
	 * @param alignedSeqs  toggles the output of the aligned or ungapped 
	 *  sequences 
	 */
	public void writeFasta(PrintStream out, Integer lineLength, boolean alignedSeqs) {
		out.println(getFastaString(lineLength, alignedSeqs));
	}
	
	/**
	 * Returns a string with the alignment in FASTA format.
	 * @param lineLength  the maximal line length, setting this to null 
	 *  always results in 80 characters per line
	 * @param alignedSeqs  toggles the output of the aligned or ungapped 
	 *  sequences 
	 * @return
	 */
	public String getFastaString(Integer lineLength, boolean alignedSeqs) {
		int len = 80;
		String seq = "";

		if( lineLength != null ) {
			len = lineLength;
		}
		String alnString = "";
		for( String name : getTags() ) {
			seq = alignedSeqs ? getAlignedSequence(name) : getSequenceNoGaps(name);
			alnString+=FASTAHEADER_CHAR + name +"\n";
			for(int i=0; i<seq.length(); i+=len) {
				alnString+=seq.substring(i, Math.min(i+len,seq.length()))+"\n";
			}
		}
		return alnString;
	}
	
    /**
     * Gets list of consecutive non-gapped sub-sequences (by means of an interval set).
     * Example (X denotes any valid amino acid):
     * <p>
     * 
     * The data:<br>
     * <code>s1: XXX---XXX-X--X</code><br>
     * <code>s2: XXX---XXXX-XXX</code><br>
     * <code>s3: --XXXX--XX-XXX</code><br>
     * <p>
     * 
     * The function calls:<br>
     * <code>TreeMap m = new TreeMap();</code><br>
     * <code>m.put("s1","XXX---XXX-X--X");</code><br>
     * <code>m.put("s2","XXX---XXXX-XXX");</code><br>
     * <code>m.put("s3","--XXXX--XX-XXX");</code><br>
     * <code>Alignment ali = new Alignment(m);</code><br>
     * <code>String[] tagSet1 = new String[1];</code><br>
     * <code>String[] tagSet2 = new String[2];</code><br>
     * <code>tagSet1[0] = "s1";</code><br>
     * <code>tagSet2[0] = "s2";</code><br>
     * <code>tagSet2[1] = "s3";</code><br>
     * <code>System.out.println(ali.getMatchingBlocks("s2",tagSet1));</code><br>
     * <code>System.out.println(ali.getMatchingBlocks("s1",tagSet2));</code><br>
     * <p>
     * The output:<br>
     * <code>[0 6, 7 9]</code><br>
     * <code>[0 2, 3 5, 6 6, 7 7]</code><br>
     *
     * @param tag  tag of the sequence of which the chunk list is to be determined
     * @param projectionTags  list of tags of sequences in the alignment whose 
     *  projection along with sequence named tag is to be used as projection 
     *  from the whole alignment. Note, that invoking this function with 
     *  {@link #getTags()} set to this parameter, considers the whole alignment 
     *  matrix. 
     * @param tag
     * @param projectionTags
     * @param positions alignment columns for which we want to get the 2 matching interval sets
     * @param degOfConservation
     * @return interval set representing the sequence of non-gapped sequence 
     * chunks. 
     * @throws IndexOutOfBoundsException 
     */
    public IntervalSet getMatchingBlocks(String tag, Collection<String> projectionTags, TreeSet<Integer> positions, int degOfConservation) 
    throws IndexOutOfBoundsException {

    	/*
    	 * col        - current alignment column
    	 * prevCol    - previous alignment column
    	 * start      - start column for the next chunk to be added
    	 * foundStart - flag set whenever a start position for the next chunk 
    	 *               to be added has been encountered
    	 * c          - observed character in sequence 'tag' in column 'col'
    	 * limit      - maximal number of tolerated gap characters at a certain 
    	 *               alignment column with respect to the sequences 
    	 *               referencened in 'projectionTags'
    	 * chunks     - the list of consecutive chunks to be returned
    	 */
    	IntervalSet chunks = new IntervalSet();
    	int col;
    	int prevCol = 1;
    	int start = 1;
    	boolean foundStart = false;
    	char c = '-';
    	int limit =  Math.max(projectionTags.size() - degOfConservation,0);

    	if(positions.isEmpty()) return chunks;
    	col = positions.iterator().next();
    	
    	for(Iterator<Integer> it = positions.iterator(); it.hasNext(); ) {
    		prevCol = col;
    		col = it.next();
    		c = getAlignedSequence(tag).charAt(col-1);

    		if( c == getGapCharacter() ) {
    			if( foundStart ) {
    				// complete chunk
    				chunks.add(new Interval(al2seq(tag,start),al2seq(tag,prevCol)));
    				foundStart = false;
    			}
    		} else if ( limit >= count(projectionTags,col,getGapCharacter()) ) {
    			if( foundStart ) {
    				if( col - prevCol > 1 ) {
    					// we allow the in between positions only to consist 
    					// of gap characters. otherwise we have to complete the 
    					// current chunk as the in-between non-gap positions
    					// are not contained in 'positions'
    					if( isBlockOf(tag,prevCol,col,getGapCharacter()) ) {
    						for( String t : projectionTags) {
    							if( !isBlockOf(t,prevCol,col,getGapCharacter()) ) {
    								foundStart = false;
    								break;
    							}
    						}
    					} else {
    						foundStart = false;
    					}

    					// please note that the 'foundStart' variable is 
    					// abused in the preceding if-clause to avoid the 
    					// allocation of an additional boolean
    					if( !foundStart ) {
    						// complete chunk
    						chunks.add(new Interval(al2seq(tag,start),al2seq(tag,prevCol)));
    						foundStart = true;
    						start = col;
    					}
    				} // else: current chunk can easily be extended
    			} else {
    				foundStart = true;
    				start = col;
    			}
    		} else {
    			if( foundStart ) {
    				foundStart = false;
    				chunks.add(new Interval(al2seq(tag,start),al2seq(tag,prevCol)));
    			}
    		}
    	}

    	if( foundStart ) {
    		// complete last chunk
    		chunks.add(new Interval(al2seq(tag,start),al2seq(tag,col)));
    	}

    	return chunks;
    }

    /**
     * Extracts from the set of given alignment position those without gaps.
     * @param projectionTags  tags of the sequences to be considered 
     * @param positions  alignment positions, i.e. indices of some columns
     * @param extractInPlace  enable this flag to directly delete all nodes 
     *  pointing to "non-gapless" columns positions, set this parameter to 
     *  false to return a new node set, i.e., 'positions' remains unchanged!
     * @return a set of indices of alignment columns out of the set of 
     * considered columns ('positions'). Please note, that parameter 
     * 'extractInPlace' has an immense impact on the output generated.     
     */
    public TreeSet<Integer> getGaplessColumns(Collection<String> projectionTags, TreeSet<Integer> positions, boolean extractInPlace) {

    	// this node set will be filled and returned if the in place editing of 
    	// parameter 'positions' is disabled
    	TreeSet<Integer> output = null;
    	if( !extractInPlace ) {
    		output = new TreeSet<Integer>();
    	}

    	int col;

    	for( Iterator<Integer> it = positions.iterator(); it.hasNext(); ) {
    		col = it.next();
    		if(count(projectionTags, col, getGapCharacter()) > 0 ) {
    			// this column contains at least one gap
    			if( extractInPlace ) {
    				// remove corresponding item in 'positions'
    				it.remove();
    			}
    		} else if( !extractInPlace ) {
    			// gapless column found -> record this event in 'output' (as 
    			// 'positions' is not editable)
    			output.add(col);
    		}
    	}

    	// return the correct node set
    	if( extractInPlace ) {
    		return positions;
    	} else {
    		return output;
    	}
    }
    
    /**
     * Returns the set of gapless columns.
     * @return the set of gapless columns
     */
    public TreeSet<Integer> getGaplessColumns() {
    	TreeSet<Integer> cols = new TreeSet<Integer>();
    	Collection<String> tags = this.getTags();
    	for (int i = 1; i < this.getAlignmentLength(); i++) {
    		if(count(tags, i, getGapCharacter()) == 0) {
    			cols.add(i);
    		}
		}
    	return cols;
    }
    
    /**
     * Returns the set of gapless columns between start and end.
     * @return the set of gapless columns between start and end
     */
    public TreeSet<Integer> getGaplessColumns(int start, int end) {
    	TreeSet<Integer> cols = new TreeSet<Integer>();
    	Collection<String> tags = this.getTags();
    	for (int i = 1; i < this.getAlignmentLength(); i++) {
    		if(count(tags, i, getGapCharacter()) == 0) {
    			if(i >= start && i <= end ) {
    				cols.add(i);
    			}
    		}
		}
    	return cols;
    }
    
    /**
     * Returns the longest non-gapped region of columns in the alignments.
     * @return the interval with the longest non-gapped region
     */
    public TreeSet<Integer> getLongestNonGappedRegion() {
    	TreeSet<Integer> longestRegion = new TreeSet<Integer>();
    	int beg = 0; 
    	int bestBeg = 0;
    	int bestEnd = 0;
    	int l = getAlignmentLength();
    	Collection<String> tags = this.getTags(); 
    	
    	// find first matching column
    	int col = 1;
    	while(col <= l) {
    		if(count(tags, col, getGapCharacter()) == 0) {
    			beg = col;
    			bestBeg = col;
    			bestEnd = col;
    			break;
    		}
    		col++;
		}
    	if(col > l) return longestRegion; // no column without gaps found
    	
    	while(col < l) {
    		col++;
    		if(count(tags, col, getGapCharacter()) > 0) {
    			// end previous interval
    			if((col-1)-beg > bestEnd-bestBeg) {
    				bestBeg = beg;
    				bestEnd = col-1;
    			}
    			beg = col + 1;
    		}
    	}

    	for (int i = bestBeg; i <= bestEnd; i++) {
			longestRegion.add(i);
		}
    	return longestRegion;
    }

    /**
     * Counts the number of occurrences of the given character at the given 
     * alignment column. The sequences to be considered is limited to the 
     * given collection of alignment tags.
     * @param tags  tags of the sequences to be considered
     * @param col  
     * @param c  
     * @return
     */
    public int count(Collection<String> tags, int col, char c) throws IndexOutOfBoundsException {
    	int i=0;
    	for( String t : tags ) {
    		if( getAlignedSequence(t).charAt(col-1) == c ) {
    			++i;
    		}
    	}
    	return i;
    }

    /**
     * 
     * @param tag
     * @param begin
     * @param end
     * @param c
     * @return
     * @throws IndexOutOfBoundsException
     */
    public boolean isBlockOf( String tag, int begin, int end, char c ) throws IndexOutOfBoundsException {
    	for(int i=begin; i<end; ++i) {
    		if( getAlignedSequence(tag).charAt(i-1) != c ) {
    			return false;
    		}
    	}
    	return true;
    }

    /** 
     * to test the class 
     * @throws IOException 
     * @throws FileFormatException 
     * @throws AlignmentConstructionException */
    public static void main(String[] args) throws IOException, FileFormatException, AlignmentConstructionException {
    	if (args.length<1){
    		System.err.println("Must provide FASTA file name as argument");
    		System.exit(1);
    	}
    	String fileName=args[0];


    	MultipleSequenceAlignment al = new MultipleSequenceAlignment(fileName,MultipleSequenceAlignment.FASTAFORMAT);


    	// print columns
    	for (int i=1;i<=al.getAlignmentLength();i++){
    		System.out.println(al.getColumn(i));
    	}
    	// print all sequences tags and sequences
    	for (String seqTag:al.getTags()){
    		System.out.println(seqTag);
    		System.out.println(al.getAlignedSequence(seqTag));
    	}
    	// test of seq indices
    	for (int index:al.indices2tags.keySet()) {
    		System.out.println("index "+index+", tag: "+al.indices2tags.get(index));
    	}
    	// test of al2seq
    	for (int i=1;i<=al.getAlignmentLength();i++) {
    		System.out.println("alignment serial: "+i+", seq serial: "+al.al2seq(al.getTagFromIndex(0),i));
    	}
    	// test of seq2al 
    	for (int serial=1;serial<=al.getSequenceNoGaps(al.getTagFromIndex(0)).length();serial++){
    		System.out.println("seq serial: "+serial+", alignment serial: "+al.seq2al(al.getTagFromIndex(0), serial));
    	}
    	// print alignment by columns tab delimited
    	//al.printTabDelimited();
    }

}
