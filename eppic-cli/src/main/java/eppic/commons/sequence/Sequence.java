package eppic.commons.sequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eppic.commons.util.Interval;

/**
 * Presenting our sequence object(!). Provides method for reading and writing
 * sequences in Fasta format.
 */
public class Sequence implements Serializable {

	private static final long serialVersionUID = 1L;

	/*------------------------------ constants ------------------------------*/
	
	// see also the REGEX in Alignment class
	// TODO this is ignoring spaces, do we want to get them too and then  
	// parse other info from the whole FASTA header? (see getPrimaryAccession() and getSecondaryAccession)
	private static final Pattern FASTAHEADER_REGEX = Pattern.compile("^>\\s*([a-zA-Z0-9_|\\-.]+)");
	
	// in principle this works for emblcds and uniprot fasta headers (not tested anywhere else!) 
	// it also removes the version (the .1 suffix at the end of the identifier that sometimes is used for instance in emblcds)
	public static final Pattern DEFLINE_PRIM_ACCESSION_REGEX = Pattern.compile("^.*\\|([^.]+)(?:\\.\\d+)?\\|.*$");
	// for uniref entries we need a slightly different regex, BTW this one will also capture isoforms of uniprot entries (like P12345-2)
	// not that makeblastdb of blast+ drops the old "lcl|" prefix, thus the regex has been adapted 
	public static final Pattern DEFLINE_PRIM_ACCESSION_UNIREF_REGEX = Pattern.compile("^UniRef\\d+_([0-9A-Z\\-]+)$");
	public static final Pattern DEFLINE_SEC_ACCESSION_REGEX = Pattern.compile("^.*\\|.*\\|([^. ]+)(?:\\.\\d+)?.*$");
	
	public static final boolean PROT_SEQUENCE = true;
	public static final boolean NUC_SEQUENCE = false;
	
	private static final Pattern NUCSEQ_REGEX = Pattern.compile("^[ACGTUX]+$");
	private static final Pattern NOTNUCSEQ_REGEX = Pattern.compile("^X+$"); // if it is only Xs we call it protein (just a guess, but is the most usual case)
	
	/*--------------------------- member variables --------------------------*/
	
	private String name;
	private String seq;
	private boolean seqType; //use constants above: PROT_SEQUENCE and NUC_SEQUENCE
	
	/*----------------------------- constructors ----------------------------*/
	
	/** 
	 * Creates a new empty sequence object (sequence type is set to protein)
	 */
	public Sequence() {
		this.name = "";
		this.seq = "";
		this.seqType = PROT_SEQUENCE;
	}
	
	/**
	 * Creates a new sequence object with the given name and the given sequence string.
	 * The sequence type (nucleotide/protein) is guessed from the sequence.
	 * The guessing will only work properly if the nucleotide sequence is composed 
	 * exclusively of A, U, T, C, G and X. Otherwise it will be called a protein. 
	 * If sequence is composed of X only (all unknown/non-standard residues) the type is 
	 * set to protein.
	 * @param name
	 * @param seq
	 */
	public Sequence(String name, String seq) {
		this.name = name;
		this.seq = seq;
		Matcher m = NUCSEQ_REGEX.matcher(seq);
		Matcher m2 = NOTNUCSEQ_REGEX.matcher(seq);
		if (m.matches()) {
			if (!m2.matches()) {
				this.seqType = NUC_SEQUENCE;
			} else {
				this.seqType = PROT_SEQUENCE;
			}
		} else {
			this.seqType = PROT_SEQUENCE;
		}
	}
	
	/**
	 * Creates a new sequence object with the given name and the given sequence string.
	 * The sequence type is passed as a boolean parameter, if true protein, false nucleotide.
	 * @param name
	 * @param seq
	 * @param protein
	 */
	public Sequence(String name, String seq, boolean protein) {
		this.name = name;
		this.seq = seq;
		this.seqType = protein;

	}

	/*-------------------------- getters and setters ------------------------*/
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the sequence's primary accession id (the one after the first pipe) extracted 
	 * from the name (FASTA header) of this Sequence if the FASTA header is from a Uniprot 
	 * or EMBL entry complying the FASTA Defline format. 
	 * See http://en.wikipedia.org/wiki/FASTA_format for Defline format 
	 * @return the accession code or null if none could be found
	 */
	public String getPrimaryAccession() {
		Matcher m = DEFLINE_PRIM_ACCESSION_REGEX.matcher(name);
		String acc = null;
		if (m.matches()) {
			acc = m.group(1);
		}
		return acc;
	}

	/**
	 * Returns the sequence's secondary accession id (the one after the second pipe) extracted 
	 * from the name (FASTA header) of this Sequence if the FASTA header is from a Uniprot 
	 * or EMBL entry complying the FASTA Defline format.
	 * See http://en.wikipedia.org/wiki/FASTA_format for Defline format 
	 * @return the accession code or null if none could be found
	 */
	public String getSecondaryAccession() {
		Matcher m = DEFLINE_SEC_ACCESSION_REGEX.matcher(name);
		String acc = null;
		if (m.matches()) {
			acc = m.group(1);
		}
		return acc;
	}
	
	/**
	 * @return the sequence
	 */
	public String getSeq() {
		return seq;
	}
	
	/**
	 * Returns a new Sequence object that is a copy of this one but with only with the
	 * subsequence given in interval. The sequence numbering used is from 1 to length-1
	 * @param interv
	 * @return
	 */
	public Sequence getInterval(Interval interval) {
		return new Sequence(this.name,this.seq.substring(interval.beg-1,interval.end),this.isProtein());
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param seq the sequence to set
	 */
	public void setSeq(String seq) {
		this.seq = seq;
	}
	
	/**
	 * Returns the length of this sequence.
	 * @return
	 */
	public int getLength() {
		return this.seq.length();
	}
	
	public boolean isProtein() {
		if (seqType==PROT_SEQUENCE) return true;
		else return false;
	}
	
	public boolean isNucleotide() {
		if (seqType==NUC_SEQUENCE) return true;
		else return false;
	}
	
	/**
	 * Sets type of this sequence
	 * @param protein true if type is protein, false if type is nucleotide
	 */
	public void setType(boolean protein) {
		this.seqType = protein;
	}
	
	/*---------------------------- public methods ---------------------------*/
	
	/**
	 * @return the string representation of this sequence (without header)
	 */
	@Override
	public String toString() {
		return this.seq.toString();
	}
	
	/**
	 * Creates a new sequence object by parsing the given Fasta file.
	 * @param fastaFile
	 * @throws IOException
	 * @throws FileFormatException
	 */
	public void readFromFastaFile(File fastaFile) throws IOException, FileFormatException {
		BufferedReader fileIn = new BufferedReader(new FileReader(fastaFile));
		String nextLine;
		// read sequences
		while((nextLine = fileIn.readLine()) != null) {
			Matcher m = FASTAHEADER_REGEX.matcher(nextLine);
			if (m.find()) {
				name = m.group(1).trim();
			} else {
				seq += nextLine.trim();
			}
		}
		fileIn.close();
		if (seq.contains(" ")) {
			throw new FileFormatException("The sequence in FASTA file "+fastaFile+" contains spaces.");
		}
	}
	
	/**
	 * Writes this sequence to given file in FASTA format
	 * Output line length is fixed at 80
	 * @param fastaFile
	 * @throws IOException
	 */
	public void writeToFastaFile(File fastaFile) throws IOException {
		String[] seqs = {this.getSeq()};
		String[] tags = {this.getName()};
		writeSeqs(fastaFile, seqs, tags);
	}
	
	/**
	 * Writes this sequence to the given PrintStream in FASTA format.
	 * Output line length is fixed at 80
	 * @param fastaFile
	 * @throws IOException
	 */
	public void writeToPrintStream(PrintStream out) throws IOException {
		String[] seqs = {this.getSeq()};
		String[] tags = {this.getName()};
		writeSeqs(out, seqs, tags);
	}
	
	/*---------------------------- static methods ---------------------------*/
	
	/**
	 * Writes given sequences and tags to given sequence file in FASTA format
	 * Output line length is fixed at 80
	 * @param seqFile
	 * @param seqs
	 * @param tags
	 * @throws FileNotFoundException 
	 */
	public static void writeSeqs(File seqFile, String[] seqs, String[] tags) throws FileNotFoundException {
		PrintStream Out = new PrintStream(new FileOutputStream(seqFile));
		writeSeqs(Out, seqs, tags);
		Out.close();
	} 
	
	/**
	 * Writes given sequences and tags to given PrintStream in FASTA format
	 * Output line length is fixed at 80
	 * @param seqFile
	 * @param seqs
	 * @param tags
	 * @throws FileNotFoundException 
	 */
	public static void writeSeqs(PrintStream Out, String[] seqs, String[] tags) {
		int len = 80;
		for (int seqIdx=0;seqIdx<seqs.length;seqIdx++) { 
			Out.println(">"+tags[seqIdx]);
			for(int i=0; i<seqs[seqIdx].length(); i+=len) {
				Out.println(seqs[seqIdx].substring(i, Math.min(i+len,seqs[seqIdx].length())));
			}		
		}
	} 
	
	/**
	 * Write given list of Sequence objects to given PrintStream in FASTA format
	 * Output line length is fixed at 80 
	 * @param ps
	 * @param sequences
	 */
	public static void writeSeqs(PrintStream ps, List<Sequence> sequences) {
		int len = 80;
		for (Sequence sequence:sequences) { 
			ps.println(">"+sequence.getName());
			for(int i=0; i<sequence.getSeq().length(); i+=len) {
				ps.println(sequence.getSeq().substring(i, Math.min(i+len,sequence.getSeq().length())));
			}		
		}		
	}
	
	/**
	 * Prints a ruler with column numbers in steps of 10 up to the given length.
	 * @param length
	 */
	public static void printSeqRuler(int length) {
		StringBuilder st = new StringBuilder(length);
		for (int i = 10; i <= length; i+=10) {
			st.append(String.format("%10d", i));
		}
		System.out.println(st);
		
		st = new StringBuilder(length);
		for (int i = 10; i <= length; i+=10) {
			st.append(String.format("%10s", "|"));
		}
		System.out.println(st);

	}
	
	/**
	 * Reads a fasta file containing multiple sequences returning a 
	 * list of Sequence objects
	 * @param seqsFile
	 * @param fastaHeaderRegex a regex that specifies as its first capture group what
	 * will be read from the FASTA header as a sequence tag. If null then {@link #FASTAHEADER_REGEX}
	 * will be used
	 * @return
	 * @throws IOException
	 */
	public static List<Sequence> readSeqs(File seqsFile, Pattern fastaHeaderRegex) throws IOException, FileFormatException {
		if (fastaHeaderRegex==null) fastaHeaderRegex = FASTAHEADER_REGEX;
		List<Sequence> list = new ArrayList<Sequence>();
		BufferedReader br = new BufferedReader(new FileReader(seqsFile));
		String line;
		String lastTag = null;
		String currentTag = null;
		StringBuffer seq = null;
		while ((line=br.readLine())!=null){
			if (line.isEmpty()) continue;
			if (line.startsWith(">")) {
				Matcher m = fastaHeaderRegex.matcher(line);
				if (m.find()) {
					currentTag = m.group(1);
					if (lastTag!=null) {
						list.add(new Sequence(lastTag, seq.toString()));
					}
					seq = new StringBuffer();
					lastTag = currentTag;
				} else {
					br.close();
					throw new FileFormatException("FASTA file "+seqsFile+" does not seem to have proper FASTA headers");
				}
			} else {
				seq.append(line.trim());
			}
		}
		br.close();
		list.add(new Sequence(lastTag, seq.toString())); // adding the last sequence
		return list;
	}
	
	public static List<Sequence> readSeqs(InputStream is, Pattern fastaHeaderRegex) throws IOException, FileFormatException {
		if (fastaHeaderRegex==null) fastaHeaderRegex = FASTAHEADER_REGEX;
		List<Sequence> list = new ArrayList<Sequence>();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		String lastTag = null;
		String currentTag = null;
		StringBuffer seq = null;
		while ((line=br.readLine())!=null){
			if (line.isEmpty()) continue;
			if (line.startsWith(">")) {
				Matcher m = fastaHeaderRegex.matcher(line);
				if (m.find()) {
					currentTag = m.group(1);
					if (lastTag!=null) {
						list.add(new Sequence(lastTag, seq.toString()));
					}
					seq = new StringBuffer();
					lastTag = currentTag;
				} else {
					br.close();
					throw new FileFormatException("Could not find FASTA headers in input stream");
				}
			} else {
				seq.append(line.trim());
			}
		}
		br.close();
		list.add(new Sequence(lastTag, seq.toString())); // adding the last sequence
		return list;
	}
}
