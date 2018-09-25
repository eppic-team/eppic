package eppic;

import eppic.commons.util.Interval;
import org.biojava.nbio.core.alignment.template.SequencePair;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to store a BioJava alignment object (SequencePair) plus extra unaligned flanking regions
 * left and right.
 * <p>
 * Needed in case we use the SIFTS alignments directly (to work around problems with BioJava alignment,
 * see https://github.com/biojava/biojava/issues/243)
 *
 * @since 3.2.0
 * @author Jose Duarte
 */
public class FullAlignment {

    private static final Logger logger = LoggerFactory.getLogger(FullAlignment.class);

    /**
     * The alignment contains only the aligned region without the flanks
     */
    private SequencePair<ProteinSequence,AminoAcidCompound> ali;

    private String firstSeq;
    private String secondSeq;
    private Interval firstInterv;
    private Interval secondInterv;
    private String firstSeqLeftFlank;
    private String firstSeqRightFlank;

    private String secondSeqLeftFlank;
    private String secondSeqRightFlank;

    /**
     * Construct an alignment based on the 2 full sequences, the BioJava SequencePair (alignment)
     * and the aligned regions firstInterv and secondInterv
     * @param ali the BioJava alignment
     * @param firstSequence the first full sequence
     * @param secondSequence the second full sequence
     * @param firstInterv the region of first full sequence that is contained in ali, if null then ali is assumed
     *                    to contain the full sequence
     * @param secondInterv the region of second full sequence that is contained in ali, if null then ali is assumed
     *                     to contain the full sequence
     */
    public FullAlignment(SequencePair<ProteinSequence,AminoAcidCompound> ali,
                         String firstSequence, String secondSequence,
                         Interval firstInterv, Interval secondInterv) {

        this.ali = ali;

        this.firstSeq = firstSequence;
        this.secondSeq = secondSequence;

        firstSeqLeftFlank = "";
        firstSeqRightFlank = "";

        secondSeqLeftFlank = "";
        secondSeqRightFlank = "";

        if (firstInterv != null) {
            firstSeqLeftFlank = firstSequence.substring(0, firstInterv.beg - 1);
            firstSeqRightFlank = firstSequence.substring(firstInterv.end);
            this.firstInterv = firstInterv;
        } else {
            this.firstInterv = new Interval(1, firstSequence.length());
        }

        if (secondInterv != null) {
            secondSeqLeftFlank = secondSequence.substring(0, secondInterv.beg - 1);
            secondSeqRightFlank = secondSequence.substring(secondInterv.end);
            this.secondInterv = secondInterv;
        } else {
            this.secondInterv = new Interval(1, secondSequence.length());
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
    public String getAlignmentString() {
        // TODO integrate better
        return
                getAlignedSequence(true) + "\n" +
                getAlignedSequence(false)+ "\n" +
                        "Aligned region (" +
                        firstInterv.beg + "-" + firstInterv.end + ", "+
                        secondInterv.beg + "-" + secondInterv.end + "): \n" +
                        ali.toString(100);
    }

    /**
     * Get the length of the alignment (the flanks are considered to be fully unaligned, i.e.
     * aligned purely against gaps)
     * @return
     */
    public int getLength() {
        return ali.getLength() +
                firstSeqLeftFlank.length() + secondSeqLeftFlank.length() +
                firstSeqRightFlank.length() + secondSeqRightFlank.length();
    }

    /**
     * Get the number of identities in the aligned region (the flanks are considered to be fully unaligned, i.e.
     * aligned purely against gaps)
     * @return
     */
    public int getNumIdenticals() {
        return ali.getNumIdenticals();
    }

    /**
     * Return the position (in sequence 1 or 2 depending of parameter) of the first occurrence of a match (identity)
     *
     * @param first true if we want the position in sequence 1, false if we want the position in sequence 2
     * @return the position or -1 if there are no matches
     */
    public int getFirstMatchingPos(boolean first) {
        int pos1 = 1;
        int pos2 = 1;
        for (int i=1;i<=ali.getLength();i++) {
            AminoAcidCompound current1 = ali.getCompoundAt(1, i);
            AminoAcidCompound current2 = ali.getCompoundAt(2, i);
            if (current1.equals(current2)) {
                if (first)
                    return pos1 + firstSeqLeftFlank.length();
                else
                    return pos2 + secondSeqLeftFlank.length();
            }
            if (!current1.getShortName().equals("-")) pos1++;
            if (!current2.getShortName().equals("-")) pos2++;
        }
        return -1;
    }

    /**
     * Return the position (in sequence 1 or 2 depending of parameter) of the last occurrence of a match (identity)
     *
     * @param first true if we want the position in sequence 1, false if we want the position in sequence 2
     * @return the position or -1 if there are no matches
     */
    public int getLastMatchingPos(boolean first) {
        int pos1 = ali.getQuery().getOriginalSequence().getLength();
        int pos2 = ali.getTarget().getOriginalSequence().getLength();
        for (int i=ali.getLength();i>0;i--) {
            AminoAcidCompound current1 = ali.getCompoundAt(1, i);
            AminoAcidCompound current2 = ali.getCompoundAt(2, i);
            if (current1.equals(current2)) {
                if (first)
                    return pos1 + firstSeqLeftFlank.length();
                else
                    return pos2 + secondSeqLeftFlank.length();
            }
            if (!current1.getShortName().equals("-")) pos1--;
            if (!current2.getShortName().equals("-")) pos2--;
        }
        return -1;
    }

    /**
     * Get the alignment index (1-based) for a given sequence index of either first or second sequence
     * @param seqPos the sequence index (1-based) for first or second sequence
     * @param first true if the input is a position in sequence 1, false if the input is a position in sequence 2
     * @return the alignment index or -1 if in flank regions
     */
    private int getAlignmentIndexAt(int seqPos, boolean first) {
        int aliPos;
        if (first) {
            if (seqPos < firstInterv.beg || seqPos > firstInterv.end) {
                // in flanks outside of aligned region
                return -1;
            }
            aliPos = ali.getQuery().getAlignmentIndexAt(seqPos - firstSeqLeftFlank.length());
        } else {
            if (seqPos < secondInterv.beg || seqPos > secondInterv.end) {
                // in flanks outside of aligned region
                return -1;
            }
            aliPos = ali.getTarget().getAlignmentIndexAt(seqPos - secondSeqLeftFlank.length());
        }
        return aliPos;
    }

    /**
     * Gets the corresponding index in other sequence, given a position in first or second sequence
     * @param seqPos the sequence position
     * @param first if true seqPos refers to first sequence, if false seqPos refers to second sequence
     * @return the sequence position in the other sequence, or -1 if it is a gap
     */
    public int getSeqPosOtherSeq(int seqPos, boolean first) {
        int alnIndex = getAlignmentIndexAt(seqPos, first);
        if (alnIndex == -1) {
            // in flanks
            // in principle it's possible to return a position in this case, but I don't think we want to.
            // We treat the flank regions as fully unaligned (fully aligned to gaps)
            return -1;
        }
        if (first) {
            // we need the other sequence here, i.e. target
            return ali.getIndexInTargetAt(alnIndex) + secondSeqLeftFlank.length();
        } else {
            return ali.getIndexInQueryAt(alnIndex) + firstSeqLeftFlank.length();
        }

    }

    /**
     * Tells whether given sequence position maps to a gap (any position in flanking region is
     * considered to map to a gap)
     * @param seqPos the sequence position in first or second sequence
     * @param first whether seqPos refers to first or second sequence
     * @return
     */
    public boolean hasGap(int seqPos, boolean first) {

        int alnIdx = getAlignmentIndexAt(seqPos, first);
        if (alnIdx == -1) {
            // in flanks
            return true;
        }

        return ali.hasGap(alnIdx);

    }

    /**
     * Get the first or second sequence with their gaps.
     * The first sequence is padded with gaps on both ends, the second sequence is padded with gaps
     * in the region between the flanks and the aligned region, i.e. (U=unaligned, A=aligned):
     * <pre>
     *     ---UUUAAAAAA-AAAUU----
     *     UUU---AA-AAAAAAA--UUUU
     * </pre>
     * @param first if true first sequence, if false second
     * @return
     */
    public String getAlignedSequence(boolean first) {

        if (first) {
            return getNgap(secondSeqLeftFlank.length()) + firstSeqLeftFlank +
                    ali.getAlignedSequence(1).getSequenceAsString() +
                    firstSeqRightFlank + getNgap(secondSeqRightFlank.length());
        } else {
            return secondSeqLeftFlank + getNgap(firstSeqLeftFlank.length()) +
                    ali.getAlignedSequence(2).getSequenceAsString() +
                    getNgap(firstSeqRightFlank.length()) + secondSeqRightFlank;
        }
    }

    /**
     * Get a string with n gap chars ("-")
     * @param n the number of gap chars desired
     * @return
     */
    private String getNgap(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<n; i++) {
            sb.append("-");
        }
        return sb.toString();
    }

    /**
     * Tells whether the given sequence position in either first or second sequence
     * matches (identical) to other sequence in alignment.
     * @param seqPos
     * @param first true if input seqPos refers to first sequence, false if input seqPos refers to second sequence
     * @return
     */
    public boolean isMatchingPos(int seqPos, boolean first) {

        int alnIdx = getAlignmentIndexAt(seqPos, first);
        if (alnIdx == -1) {
            // in flanks
            return false;
        }
        AminoAcidCompound cmpnd1 = ali.getCompoundInQueryAt(alnIdx);
        AminoAcidCompound cmpnd2 = ali.getCompoundInTargetAt(alnIdx);
        // a gap-to-gap matching is in principle impossible
        if (cmpnd1.getShortName().equals("-") && cmpnd2.getShortName().equals("-")) {
            logger.error("Alignment position {} maps to gaps in both sequences, this is most likely a bug!",
                    alnIdx);
        }
        return cmpnd1.equals(cmpnd2);
    }
}
