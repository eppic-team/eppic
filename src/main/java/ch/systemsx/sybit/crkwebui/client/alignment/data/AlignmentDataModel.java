package ch.systemsx.sybit.crkwebui.client.alignment.data;

import java.io.Serializable;

/**
 * Class used to store the alignment data
 * @author biyani_n
 *
 */
public class AlignmentDataModel implements Serializable {

	private static final long serialVersionUID = 1L;

	private int uid;
	private String[] rowHeader;
	private Integer[] startIndex;
	private Integer[] endIndex;
	private String[] alignment;
	
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
		
		rowHeader = new String[2];
		rowHeader[0] = firstChainHeader;
		rowHeader[1] = secondChainHeader;
		
		startIndex = new Integer[2];
		startIndex[0] = firstStartIndex;
		startIndex[1] = secondStartIndex;
		
		endIndex = new Integer[2];
		endIndex[0] = firstEndIndex;
		endIndex[1] = secondEndIndex;
		
		alignment = new String[3];
		alignment[0] = firstAlignment;
		alignment[1] = markup;
		alignment[2] = secondAlignment;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String[] getRowHeader() {
		return rowHeader;
	}

	public void setRowHeader(String[] rowHeader) {
		this.rowHeader = rowHeader;
	}

	public Integer[] getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(Integer[] startIndex) {
		this.startIndex = startIndex;
	}

	public Integer[] getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(Integer[] endIndex) {
		this.endIndex = endIndex;
	}

	public String[] getAlignment() {
		return alignment;
	}

	public void setAlignment(String[] alignment) {
		this.alignment = alignment;
	}
	
}
