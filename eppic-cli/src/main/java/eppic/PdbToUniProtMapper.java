package eppic;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.biojava.nbio.alignment.NeedlemanWunsch;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.alignment.template.GapPenalty;
import org.biojava.nbio.core.alignment.matrices.SubstitutionMatrixHelper;
import org.biojava.nbio.core.alignment.template.SequencePair;
import org.biojava.nbio.core.alignment.template.SubstitutionMatrix;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.biojava.nbio.structure.AminoAcid;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Compound;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.GroupType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eppic.commons.sequence.UnirefEntry;
import eppic.commons.util.Interval;

public class PdbToUniProtMapper {

	private static final Logger LOGGER = LoggerFactory.getLogger(PdbToUniProtMapper.class);
	
	private static final double MAX_QUERY_TO_UNIPROT_DISAGREEMENT = 0.1;
	

	
	/**
	 * The alignments between the PDB sequence and the UniProt reference sequence.
	 * In SequencePair the first sequence is called "query" (the PDB sequence here)
	 * and the second sequence is called "target" (the UniProt sequence here).
	 * If the sequence comes from SEQRES only 1 alignment for the representative sequence is stored,
	 * if the sequence comes from ATOM, then an alignment for every chain is stored.
	 */
	private Map<String, SequencePair<ProteinSequence,AminoAcidCompound>> alignments;
	
	private Compound compound;
	private Map<String, String> sequences;
	private UnirefEntry uniProtReference;
	
	/**
	 * The interval that matches the PDB sequence (the first and last non-gaps in the alignment)
	 */
	private Interval matchingIntervalUniProtCoords;
	
	/**
	 * The PDB-mapped interval corresponding to {@link #matchingIntervalUniProtCoords}
	 */
	private Interval matchingIntervalPdbCoords;
	
	/**
	 * Flag to sign that the sequence was taken from ATOM groups instead of SEQRES groups
	 */
	private boolean sequenceFromAtom;
	
	
	public PdbToUniProtMapper(Compound compound) {
		
		this.compound = compound;
		
		initSequences();

	}
	
	public void setUniProtReference(UnirefEntry uniref) throws CompoundNotFoundException {
		
		this.uniProtReference = uniref;
		
		initAlignments();
		
		initMatchingInterval();

	}
	
	/**
	 * Initialises the sequences, normally just the one sequence corresponding to the SEQRES of the Compound.
	 * But if no SEQRES available, we need to store all sequences of every chain member of the Compound, so 
	 * that we can have alignments for them all (each of the ATOM sequences can differ from each other).
	 */
	private void initSequences() {
		
		sequences = new TreeMap<String, String>();
			
		Chain chain = compound.getRepresentative();
		// it looks like biojava interprets MSEs as METs, at least for the sequence, so no issues here
		String repSequenceSeqRes = chain.getSeqResSequence();
			
		
		// for files without a SEQRES, it will be empty, we get it from atom groups
		if (repSequenceSeqRes.isEmpty()) {
			LOGGER.warn("Could not get a sequence from SEQRES for entity {} (chains {}). Getting it from ATOM instead",
					compound.getMolId(), compound.getChainIds().toString());
			
			for (Chain c:compound.getChains()) {
				
				String seq = getAtomSequence(c);
				
				sequences.put(c.getChainID(), seq);
				if (seq.isEmpty()) {
					LOGGER.warn("Sequence from ATOM records for chain {} has length 0",chain.getChainID());
				}

			}
			
			this.sequenceFromAtom = true;
			
		} else {
			
			// we add just the 1 representative sequence
			sequences.put(chain.getChainID() ,repSequenceSeqRes);
			this.sequenceFromAtom = false;
		}
	}
	
	private void initAlignments() throws CompoundNotFoundException {
		
		this.alignments = new TreeMap<String, SequencePair<ProteinSequence,AminoAcidCompound>>();
		
		if (sequenceFromAtom) {
			LOGGER.info("PDB sequences are from ATOM, will have one alignment per member chain of entity {}",compound.getMolId());
		}		
		
		
		for (String chainId: sequences.keySet()) {
			
			SequencePair<ProteinSequence,AminoAcidCompound> alignment = getAlignment(chainId, uniProtReference.getSequence());
			
			alignments.put(chainId, alignment);
			
			
			LOGGER.info("Chain "+chainId+" PDB "+(sequenceFromAtom?"ATOM":"SEQRES")+" to UniProt alignmnent:\n"+getAlignmentString(alignment));
			LOGGER.info("Query (chain "+chainId+") length: "+sequences.get(chainId).length());
			LOGGER.info("UniProt ("+uniProtReference.getUniId()+") length: "+uniProtReference.getLength());
			LOGGER.info("Alignment length: "+alignment.getLength());
		}
		
	}

	private SequencePair<ProteinSequence,AminoAcidCompound> getAlignment(String chainId, String upSequence) throws CompoundNotFoundException {

		String pdbSequence = sequences.get(chainId);
		
		// and finally we align the 2 sequences (in case of mapping from SIFTS we rather do this than trusting the SIFTS alignment info)
		SubstitutionMatrix<AminoAcidCompound> matrix = SubstitutionMatrixHelper.getBlosum50();
		// setting (20,1) to have a large enough difference so that behaviour is like eppic 2  
		GapPenalty penalty = new SimpleGapPenalty(20, 1);

		SequencePair<ProteinSequence,AminoAcidCompound> alignment = null;
		
		// before move to Biojava, we had as tags of the sequences:  "chain"+representativeChain and query.getUniId()
		ProteinSequence s1 = new ProteinSequence(pdbSequence);
		ProteinSequence s2 = new ProteinSequence(upSequence);

		NeedlemanWunsch<ProteinSequence,AminoAcidCompound> nw = 
				new NeedlemanWunsch<ProteinSequence,AminoAcidCompound>(s1,s2, penalty, matrix);
		
		alignment = nw.getPair();

		return alignment;
	}
	
	/**
	 * Checks that alignments are within the expected thresholds and returns a warning String if not.
	 * @return warning String or null if all alignments are within thresholds
	 */
	public String checkAlignments() {
		
		
		for (String chainId : alignments.keySet()) {

			SequencePair<ProteinSequence,AminoAcidCompound> alignment = alignments.get(chainId);

			int shortestSeqLength = Math.min(uniProtReference.getLength(), sequences.get(chainId).length());
			double id = (double)alignment.getNumIdenticals()/(double)shortestSeqLength;
			LOGGER.info("Query (chain "+chainId+") to reference UniProt percent identity: "+String.format("%6.2f%%",id*100.0));
			LOGGER.info("UniProt reference coverage of query's (chain "+chainId+") sequence: "+
					String.format("%6.2f%%",100.0*sequences.get(chainId).length()/uniProtReference.getLength()));


			// in strange cases like 3try (a racemic mixture with a chain composed of L and D aminoacids) the SIFTS-mapped
			// UniProt entry does not align at all to the PDB SEQRES (mostly 'X'), we have to check for this
			if (id < MAX_QUERY_TO_UNIPROT_DISAGREEMENT) {
				String msg = "Identity of PDB to UniProt reference alignment is below maximum allowed threshold ("+
						String.format("%2.0f%%", 100.0*MAX_QUERY_TO_UNIPROT_DISAGREEMENT)+"). " +
						"Will not use the UniProt reference "+uniProtReference.getUniId();
				LOGGER.warn(msg);
								
				return msg;
			} 
		}
		
		return null;
	}
	
	/**
	 * Get the PDB-to-UniProt mapping in SIFTS tabular format
	 * TODO this doesn't work well yet, because SEQRES groups don't have a residue number
	 * @param chainId
	 * @return
	 */
	public String getMappingSiftsFormat(String chainId) {

		// TODO this can't be done properly with BioJava:
		// the issue is that PDB serials are only defined for AtomGroups and not for something 
		// that is only in the SEQRES, e.g. it fails for 1smtA residue 1.

		// our mapping in SIFTS tab format,
		// e.g.: 1dan	L	P08709	1	152	1	152	61	212
		
		int uniprotBeg = matchingIntervalUniProtCoords.beg;
		int uniprotEnd = matchingIntervalUniProtCoords.end;

		Group groupBeg = getPdbGroupFromUniProtIndex(uniprotBeg, chainId);
		Group groupEnd = getPdbGroupFromUniProtIndex(uniprotEnd, chainId);

		String pdbId = "xxxx";
		if (groupBeg!=null && groupBeg.getChain()!=null && groupBeg.getChain().getStructure()!=null &&
				groupBeg.getChain().getStructure().getPDBCode()!=null && 
				!groupBeg.getChain().getStructure().getPDBCode().isEmpty()) {
			
			pdbId = groupBeg.getChain().getStructure().getPDBCode();
		}
		
		int seqresBeg = 0;
		int seqresEnd = 0;
		String pdbBeg = "0";
		String pdbEnd = "0";
		
		if (groupBeg!=null) {
			seqresBeg = getSeqresSerial(groupBeg);
			if (groupBeg.getResidueNumber()==null) LOGGER.warn("No residue number for group '{}'",groupBeg.toString()); 
			else pdbBeg = groupBeg.getResidueNumber().toString();
		}
		if (groupEnd!=null) {
			seqresEnd = getSeqresSerial(groupEnd);
			if (groupEnd.getResidueNumber()==null) LOGGER.warn("No residue number for group '{}'",groupEnd.toString()); 
			else pdbEnd = groupEnd.getResidueNumber().toString();
		}

		
		return 	pdbId+"\t"+
				chainId+"\t"+
				uniProtReference.getUniId()+"\t"+
				seqresBeg+"\t"+seqresEnd+"\t"+
				pdbBeg+"\t"+pdbEnd+"\t"+
				uniprotBeg+"\t"+uniprotEnd;
		
	}

	/**
	 * Get the PDB-to-UniProt mapping in DBREF format
	 * TODO this doesn't work well yet, because SEQRES groups don't have a residue number
	 * @param chainId
	 * @return
	 */
	public String getMappingDbrefFormat(String chainId) {
		// TODO this can't be done properly with BioJava:
		// the issue is that PDB serials are only defined for AtomGroups and not for something 
		// that is only in the SEQRES, e.g. it fails for 1smtA residue 1.

		// our mapping in DEBREF format,
		// e.g.: 1DAN L    1   152  UNP    P08709   FA7_HUMAN       61    212

		int uniprotBeg = matchingIntervalUniProtCoords.beg;
		int uniprotEnd = matchingIntervalUniProtCoords.end;

		Group groupBeg = getPdbGroupFromUniProtIndex(uniprotBeg, chainId);
		Group groupEnd = getPdbGroupFromUniProtIndex(uniprotEnd, chainId);

		String pdbId = "xxxx";
		if (groupBeg!=null && groupBeg.getChain()!=null && groupBeg.getChain().getStructure()!=null &&
				groupBeg.getChain().getStructure().getPDBCode()!=null && 
				!groupBeg.getChain().getStructure().getPDBCode().isEmpty()) {

			pdbId = groupBeg.getChain().getStructure().getPDBCode();
		}
		
		int pdbSeqNumBeg = 0;
		int pdbSeqNumEnd = 0;
		char pdbInsBeg = ' ';
		char pdbInsEnd = ' ';
		
		if (groupBeg!=null) {
			if (groupBeg.getResidueNumber()==null) LOGGER.warn("No residue number for group '{}'",groupBeg.toString()); 
			else {
				pdbSeqNumBeg = groupBeg.getResidueNumber().getSeqNum();
				pdbInsBeg = groupBeg.getResidueNumber().getInsCode();
			}
		}
		if (groupEnd!=null) {
			if (groupEnd.getResidueNumber()==null) LOGGER.warn("No residue number for group '{}'",groupEnd.toString()); 
			else {
				pdbSeqNumEnd = groupEnd.getResidueNumber().getSeqNum();
				pdbInsEnd = groupEnd.getResidueNumber().getInsCode();
			}
		}


		return 	pdbId + " " + chainId + " " + 
				String.format("%4d",pdbSeqNumBeg)+pdbInsBeg+" "+
				String.format("%4d",pdbSeqNumEnd)+pdbInsEnd+" "+
				"UNP   "+" "+
				String.format("%-8s", uniProtReference.getUniId())+" "+
				String.format("%12s", "") + " "+
				String.format("%5d", uniprotBeg)+"  "+
				String.format("%5d", uniprotEnd)+ " ";
	}

	/**
	 * Return the PDB-to-UniProt reference alignment. 
	 * If the entity is composed of several chains and the sequences come from ATOM groups, then
	 * only the first alignment of the group of alignments is returned.
	 * @return
	 */
	public SequencePair<ProteinSequence,AminoAcidCompound> getAlignment() {
		return alignments.values().iterator().next();
	}
	
	/**
	 * Returns true if the sequences have been extracted from ATOM groups rather than from 
	 * SEQRES groups. They will be from ATOM groups if Chain.getSeqResSequence returned an empty string.
	 * @return
	 */
	public boolean isSequenceFromAtom() {
		return sequenceFromAtom;
	}
	
	/**
	 * Returns the PDB sequence. If the entity is composed of several chains without a SEQRES,
	 * the ATOM sequence with the max length is returned. 
	 * @return
	 */
	public String getPdbSequence() {
		
		String maxSeq = null;
		int maxLength = 0;
		
		for (String seq:sequences.values()) {
			if (seq.length()>maxLength) {
				maxSeq = seq;
				maxLength = seq.length();
			}
		}
		return maxSeq;
	}
	
	/**
	 * Get the interval of the UniProt reference sequence that matches the 
	 * PDB sequence (the first and last non-gaps in the alignment)
	 * @return
	 */
	public Interval getMatchingIntervalUniProtCoords() {
		return matchingIntervalUniProtCoords;
	}

	/**
	 * The PDB-mapped interval corresponding to {@link #getMatchingIntervalUniProtCoords()}
	 * @return
	 */
	public Interval getMatchingIntervalPdbCoords() {
		return matchingIntervalPdbCoords;
	}

	/**
	 * Returns the interval that needs to be used for homolog search, depending on given searchMode
	 * @param searchMode
	 * @return
	 */
	public Interval getHomologsSearchInterval(HomologsSearchMode searchMode) {
		
		if (searchMode==HomologsSearchMode.GLOBAL) {

			return new Interval(1, uniProtReference.getLength());

		} else if (searchMode==HomologsSearchMode.LOCAL) {

			return getMatchingIntervalUniProtCoords();

		} else {
			LOGGER.error("Search mode {} not supported!", searchMode.getName());
			return null;
		}
	}
	
	/**
	 * Initialises the matching-to-PDB interval in the UniProt reference sequence: matching either
	 * the SEQRES PDB sequence or maximally matching all the chains' sequences if no SEQRES is available.
	 * E.g. this alignment:
	 * <pre>  
	 *                12345678
	 *  UniProt ref:  ABCDEFGH
	 *  SEQRES  seq:  --CDMFG-
	 * </pre>
	 * would give interval 3,7.
	 * <br>
	 * E.g. when using ATOM sequences, this alignment:
	 * <pre>
	 *                12345678  
	 *  UniProt ref:  ABCDEFGH
	 *  ATOM  seq A:  --CDMFG-
	 *  ATOM  seq B:  --C-MFG-
	 *  ATOM  seq A:  --CDMF--
	 * </pre> 
	 * would give interval 3,7 because it is the interval that maximally matches all the member chains' sequences
	 *   
	 */
	private void initMatchingInterval() {

		// either we have one alignment for the representative or multiple for each member chain
		// we go through all of them and find the largest matching interval
		
		int minBeg = Integer.MAX_VALUE;
		int maxEnd = 0;
		
		int pdbBeg = -1;
		int pdbEnd = -1;
		
		for (SequencePair<ProteinSequence,AminoAcidCompound> pair:this.alignments.values()) {
			int upBeg = getFirstMatchingPos(pair, false);
			int upEnd = getLastMatchingPos(pair, false);
			
			if (upBeg!=-1 && upBeg<=minBeg) {				
				minBeg = upBeg;
				pdbBeg = getFirstMatchingPos(pair, true);
			}
			
			if (upEnd!=-1 && upEnd>=maxEnd) {
				maxEnd = upEnd;
				pdbEnd = getLastMatchingPos(pair, true);
			}
		}
		
		if (minBeg == Integer.MAX_VALUE || maxEnd == 0) {
			
			LOGGER.warn("Could not find a matching interval for entity {}", compound.getMolId());
			
			matchingIntervalUniProtCoords = null;
			matchingIntervalPdbCoords = null;
		}
		
		matchingIntervalUniProtCoords = new Interval(minBeg, maxEnd);
		matchingIntervalPdbCoords = new Interval(pdbBeg, pdbEnd);
		
	}
	
	/**
	 * Tells whether a given residue from the PDB structure
	 * is matching to the same residue in the UniProt reference (mismatches occur 
	 * mostly because of engineered residues in PDB).
	 * @param g the group
	 * @return
	 * @see #getUniProtIndexForPdbGroup(Group, boolean)
	 */
	public boolean isPdbGroupMatchingUniProt(Group g) {
		
		Chain c = g.getChain();
		
		SequencePair<ProteinSequence,AminoAcidCompound>  alignment = null;
		if (sequenceFromAtom) {
			// we get the corresponding alignment for the chain
			alignment = alignments.get(c.getChainID());
		} else {
			// we should have just the one alignment for the SEQRES sequence
			alignment = alignments.values().iterator().next();
			if (compound.getChains().size()>1 && alignments.size()>1) 
				LOGGER.warn("More than 1 alignment for entity {} contained in pdb-to-uniprot mapper, expected only 1: something is wrong!",
						compound.getMolId());
		}
		
		int resser = getSeqresSerial(g);
		
		if (resser==-1) {
			LOGGER.info("The group '{}' wasn't found in ATOM or SEQRES, will mark it as mismatch to UniProt reference.", 
					g.toString());
			return false;
		}
		
		int alnIdx = alignment.getQuery().getAlignmentIndexAt(resser);
		AminoAcidCompound cmpnd1 = alignment.getCompoundInQueryAt(alnIdx);
		AminoAcidCompound cmpnd2 = alignment.getCompoundInTargetAt(alnIdx);
		// a gap-to-gap matching is in principle impossible
		if (cmpnd1.getShortName().equals("-") && cmpnd2.getShortName().equals("-")) {
			LOGGER.error("Alignment position {} maps to gaps in both PDB and UniProt reference sequences, this is most likely a bug!",
					alnIdx);
		}
		return cmpnd1.equals(cmpnd2);
	}
	
	/**
	 * Given a residue, returns its corresponding UniProt's sequence index. 
	 * Depending on parameter positionWithinSubinterval,
	 * the returned position will be with respect to the matching subinterval or 
	 * with respect to the full sequence (with 1-based indices for either case).
	 * @param g the group
	 * @param positionWithinSubinterval if true the returned position will be with respect to the UniProt reference
	 * subinterval that matches the PDB, if false the returned position will be with respect to the full
	 * UniProt reference sequence 
	 * @return the mapped UniProt sequence position or -1 if it maps to a gap
	 * @see #isPdbGroupMatchingUniProt(Group)
	 */
	public int getUniProtIndexForPdbGroup(Group g, boolean positionWithinSubinterval) {

		Chain c = g.getChain();

		SequencePair<ProteinSequence,AminoAcidCompound>  alignment = null;
		if (sequenceFromAtom) {
			// we get the corresponding alignment for the chain
			alignment = alignments.get(c.getChainID());
		} else {
			// we should have just the one alignment for the SEQRES sequence
			alignment = alignments.values().iterator().next();
			if (compound.getChains().size()>1 && alignments.size()>1) 
				LOGGER.warn("More than 1 alignment for entity {} contained in pdb-to-uniprot mapper, expected only 1: something is wrong!",
						compound.getMolId());
		}

		int resser = getSeqresSerial(g);

		if (resser==-1) {
			LOGGER.info("The group '{}' wasn't found in ATOM or SEQRES, so we can't map to UniProt reference.", 
					g.toString());

			return -1;
		}
		
		int alnIdx = alignment.getQuery().getAlignmentIndexAt(resser);
		if (alignment.hasGap(alnIdx)) {
			// maps to gap in target (UniProt)
			return -1;
		}
		// this gets the position in uniprot sequence with indices starting at 1
		int uniprotPos = alignment.getIndexInTargetAt(alnIdx);

		if (!positionWithinSubinterval) {
			return uniprotPos;
		}
		else {

			// the position in the subsequence that was used for blasting
			int pos = uniprotPos - (matchingIntervalUniProtCoords.beg -1); 
			// check if it is out of the subsequence: can happen when his tags or other engineered residues at termini 
			// in n-terminal (pos<0), e.g. 3n1e
			// or c-terminal (pos>=subsequence length) e.g. 2eyi
			if (pos<=0 || pos>matchingIntervalUniProtCoords.getLength()) return -1; 

			return pos;
		}
	}
	
	/**
	 * Given a sequence index of the query UniProt sequence (1-based), returns its
	 * corresponding group in the structure (from chain as given in chainId).
	 * TODO beware this does not work well yet when sequence is from ATOM 
	 * @param uniProtIndex the 1-based index in the full UniProt sequence
	 * @param chainId the chainId of the chain where we want to extract the group from
	 * @return the mapped PDB group or null if it maps to a gap
	 */
	public Group getPdbGroupFromUniProtIndex(int uniProtIndex, String chainId) {
		
		SequencePair<ProteinSequence,AminoAcidCompound>  alignment = null;
		if (sequenceFromAtom) {
			// we get the corresponding alignment for the chain
			alignment = alignments.get(chainId);
		} else {
			// we should have just the one alignment for the SEQRES sequence
			alignment = alignments.values().iterator().next();
			if (compound.getChains().size()>1 && alignments.size()>1) 
				LOGGER.warn("More than 1 alignment for entity {} contained in pdb-to-uniprot mapper, expected only 1: something is wrong!",
						compound.getMolId());
		}
		
		int alnIdx = alignment.getTarget().getAlignmentIndexAt(uniProtIndex);		
		if (alignment.hasGap(alnIdx)) {
			return null;
		}
		
		int resser = alignment.getIndexInQueryAt(alnIdx);
		
		// getting the relevant chain
		Chain chain = null;
		for (Chain c:compound.getChains()) {
			if (c.getChainID().equals(chainId)) {
				chain = c;
			}
		}
		
		if (sequenceFromAtom){
			// this should revert what chain.getAtomSequence() does but!
			// TODO there is an issue: in chain.getAtomSequence only amino groups are added, if there are
			// het groups in between this will fail!
			return chain.getAtomGroups().get(resser-1);
		} else {
			// this reverts what chain.getSeqResSequence() does
			return chain.getSeqResGroups().get(resser-1);
		}
	}
	
	private static String getAtomSequence(Chain c) {
		// TODO we could do a better job in identifying non-standard aminoacids if we used chem comps
		StringBuilder seqBuilder = new StringBuilder();
		
		List<Group> groups = c.getAtomGroups(GroupType.AMINOACID);
		for (Group g:groups) {
			if (g instanceof AminoAcid) {
				AminoAcid a = (AminoAcid) g;
				seqBuilder.append(a.getAminoType());
			}
		}
		return seqBuilder.toString();
	}
	
	/**
	 * Return the SEQRES serial of the Group g in the Chain c.
	 * If no SEQRES serials are available, then the index in the atom groups
	 * of the corresponding chain is returned (1-based).
	 * 
	 * NOTE: in current implementation, this does not work for groups that are
	 * only in SEQRES and not in ATOM
	 * @param g
	 * @return the SEQRES serial (1 to n) or -1 if not found
	 */
	private int getSeqresSerial(Group g) {
		if (sequenceFromAtom) {	
			// this numbering would correspond to each of the strings in sequences
			// thus this has to behave exactly in the same way as how the sequences were initialised 
			// with getAtomSequence(Chain)

			// TODO we could do a better job in identifying non-standard aminoacids if we used chem comps 

			List<Group> groups = g.getChain().getAtomGroups(GroupType.AMINOACID);
			
			for (int i=0;i<groups.size();i++) {
				
				Group group = groups.get(i);
				
				if (g == group) 
					return i+1; 
			}
			return -1;
		} 
		
		else { 
			// IMPORTANT NOTE: this won't work for groups that are not in ATOM groups, due to
			// seqres groups not having residue numbers in BioJava
			return compound.getAlignedResIndex(g, g.getChain());
		}
	}
	
	/**
	 * Returns the the given alignment as a nicely formatted
	 * alignment string in several lines with a middle line of matching characters,
	 * e.g. 
	 * chainA  AAAA--BCDEFGICCC
	 *         ||.|  ||.|||:|||
	 * QABCD1  AABALCBCJEFGLCCC
	 * @return
	 */
	private static String getAlignmentString(SequencePair<ProteinSequence,AminoAcidCompound> alignment) {
		return alignment.toString(100);
	}
	
	/**
	 * Return the position (in sequence 1 or 2 depending of parameter) of the first occurrence of a match (identity)
	 * 
	 * @param pair the pair of aligned sequences
	 * @param first true if we want the position in sequence 1, false if we want the position in sequence 2  
	 * @return the position or -1 if there are no matches
	 */
	private static int getFirstMatchingPos(SequencePair<ProteinSequence,AminoAcidCompound> pair, boolean first) {
		int pos1 = 1;
		int pos2 = 1;
		for (int i=1;i<=pair.getLength();i++) {
			AminoAcidCompound current1 = pair.getCompoundAt(1, i);
			AminoAcidCompound current2 = pair.getCompoundAt(2, i);
			if (current1.equals(current2)) {
				if (first) return pos1;
				else return pos2;
			}
			if (!current1.getShortName().equals("-")) pos1++;
			if (!current2.getShortName().equals("-")) pos2++;
		}
		return -1;
	}
	
	/**
	 * Return the position (in sequence 1 or 2 depending of parameter) of the last occurrence of a match (identity)
	 * 
	 * @param pair the pair of aligned sequences 
	 * @param first true if we want the position in sequence 1, false if we want the position in sequence 2  
	 * @return the position or -1 if there are no matches
	 */
	private static int getLastMatchingPos(SequencePair<ProteinSequence,AminoAcidCompound> pair, boolean first) {
		int pos1 = pair.getQuery().getOriginalSequence().getLength();
		int pos2 = pair.getTarget().getOriginalSequence().getLength();
		for (int i=pair.getLength();i>0;i--) {
			AminoAcidCompound current1 = pair.getCompoundAt(1, i);
			AminoAcidCompound current2 = pair.getCompoundAt(2, i);
			if (current1.equals(current2)) {
				if (first) return pos1;
				else return pos2;
			}
			if (!current1.getShortName().equals("-")) pos1--;
			if (!current2.getShortName().equals("-")) pos2--;
		}
		return -1;		
	}
}
