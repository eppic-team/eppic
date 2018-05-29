package ch.systemsx.sybit.crkwebui.client.alignment.data;

import java.io.Serializable;

import eppic.model.dto.PairwiseAlignmentData;
import eppic.model.dto.PairwiseAlignmentInfo;

/**
 * Class used to store the alignment data
 * @author biyani_n
 *
 */
public class AlignmentDataModel implements Serializable {

	private static final long serialVersionUID = 1L;

	private int uid;
	private PairwiseAlignmentInfo rowHeader;
	private PairwiseAlignmentInfo startIndex;
	private PairwiseAlignmentInfo endIndex;
	private PairwiseAlignmentData alignment;
	
	private static int COUNTER = 0;
	
	public AlignmentDataModel(String firstChainHeader,
							  String secondChainHeader,
							  int firstStartIndex,
							  int secondStartIndex,
							  int firstEndIndex,
							  int secondEndIndex,
							  String firstAlignment,
							  String markup,
							  String secondAlignment){
		
		COUNTER++;
		uid = COUNTER;
		
		rowHeader = new PairwiseAlignmentInfo();
		
		rowHeader.setFirstValue(firstChainHeader);
		rowHeader.setSecondValue(secondChainHeader);
		
		startIndex = new PairwiseAlignmentInfo();
		startIndex.setFirstValue(Integer.toString(firstStartIndex));
		startIndex.setSecondValue(Integer.toString(secondStartIndex));
		
		endIndex = new PairwiseAlignmentInfo();
		endIndex.setFirstValue(Integer.toString(firstEndIndex));
		endIndex.setSecondValue(Integer.toString(secondEndIndex));
		
		alignment = new PairwiseAlignmentData();
		alignment.setFirstSequence(firstAlignment);
		alignment.setSecondSequence(secondAlignment);
		alignment.setMarkupLine(markup);

	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public PairwiseAlignmentInfo getRowHeader() {
		return rowHeader;
	}

	public void setRowHeader(PairwiseAlignmentInfo rowHeader) {
		this.rowHeader = rowHeader;
	}

	public PairwiseAlignmentInfo getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(PairwiseAlignmentInfo startIndex) {
		this.startIndex = startIndex;
	}

	public PairwiseAlignmentInfo getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(PairwiseAlignmentInfo endIndex) {
		this.endIndex = endIndex;
	}

	public PairwiseAlignmentData getAlignment() {
		return alignment;
	}

	public void setAlignment(PairwiseAlignmentData alignment) {
		this.alignment = alignment;
	}
	
}
