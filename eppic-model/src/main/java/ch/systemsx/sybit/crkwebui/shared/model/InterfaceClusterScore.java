package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import ch.systemsx.sybit.server.db.entitylisteners.DoubleNaNXmlAdapter;
import eppic.model.InterfaceClusterScoreDB;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class InterfaceClusterScore implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int uid;
	private String callName;
	private String callReason;
	@XmlJavaTypeAdapter(type=Double.class, value=DoubleNaNXmlAdapter.class)
	private double score;
	@XmlJavaTypeAdapter(type=Double.class, value=DoubleNaNXmlAdapter.class)
	private double score1;
	@XmlJavaTypeAdapter(type=Double.class, value=DoubleNaNXmlAdapter.class)
	private double score2;
	private double confidence;
	private String method;

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getCallName() {
		return callName;
	}

	public void setCallName(String callName) {
		this.callName = callName;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public double getScore1() {
		return score1;
	}

	public void setScore1(double score1) {
		this.score1 = score1;
	}

	public double getScore2() {
		return score2;
	}

	public void setScore2(double score2) {
		this.score2 = score2;
	}
	
	public String getCallReason() {
	    return callReason;
	}

	public void setCallReason(String callReason) {
	    this.callReason = callReason;
	}

	/**
	 * Converts DB model item into DTO one
	 * @param clusterScoreDB model item to convert
	 * @return DTO representation of model item
	 */
	public static InterfaceClusterScore create(InterfaceClusterScoreDB clusterScoreDB)
	{
		InterfaceClusterScore clusterScore = new InterfaceClusterScore();
		clusterScore.setCallName(clusterScoreDB.getCallName());
		clusterScore.setCallReason(clusterScoreDB.getCallReason());
		clusterScore.setMethod(clusterScoreDB.getMethod());
		clusterScore.setUid(clusterScoreDB.getUid());
		clusterScore.setScore(clusterScoreDB.getScore());
		clusterScore.setScore1(clusterScoreDB.getScore1());
		clusterScore.setScore2(clusterScoreDB.getScore2());
		clusterScore.setConfidence(clusterScoreDB.getConfidence());
		return clusterScore;
	}
}
