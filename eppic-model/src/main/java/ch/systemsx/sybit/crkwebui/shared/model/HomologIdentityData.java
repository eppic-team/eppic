package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

public class HomologIdentityData implements Serializable, Comparable<HomologIdentityData>{
	
	private static final long serialVersionUID = 1L;
	
	private double seqIdToQuery;
	private int queryStart;
	private int queryEnd;
	private int queryLength;
	
	public HomologIdentityData(double seqIdToQuery,
							   int queryStart,
	                           int queryEnd,
	                           int queryLength){
		this.seqIdToQuery = seqIdToQuery;
		this.queryStart = queryStart;
		this.queryEnd = queryEnd;
		this.queryLength = queryLength;
	}

	public double getSeqIdToQuery() {
		return seqIdToQuery;
	}

	public void setSeqIdToQuery(double seqIdToQuery) {
		this.seqIdToQuery = seqIdToQuery;
	}

	public int getQueryStart() {
		return queryStart;
	}

	public void setQueryStart(int queryStart) {
		this.queryStart = queryStart;
	}

	public int getQueryEnd() {
		return queryEnd;
	}

	public void setQueryEnd(int queryEnd) {
		this.queryEnd = queryEnd;
	}

	public int getQueryLength() {
		return queryLength;
	}

	public void setQueryLength(int queryLength) {
		this.queryLength = queryLength;
	}

	/**
	 * Compares the sequence identity of one item to the other
	 */
	@Override
	public int compareTo(HomologIdentityData o) {
		return new Double(this.getSeqIdToQuery()).compareTo(o.getSeqIdToQuery());
	}

}
