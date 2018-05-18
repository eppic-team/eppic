package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import ch.systemsx.sybit.server.db.entitylisteners.DoubleNaNXmlAdapter;
import eppic.model.InterfaceScoreDB;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * DTO class for InterfaceScore.
 * @author AS
 */
public class InterfaceScore implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	
	private String method;
	private int interfaceId;

	@XmlJavaTypeAdapter(type=Double.class, value=DoubleNaNXmlAdapter.class)
	private double score1;
	@XmlJavaTypeAdapter(type=Double.class, value=DoubleNaNXmlAdapter.class)
	private double score2;
	@XmlJavaTypeAdapter(type=Double.class, value=DoubleNaNXmlAdapter.class)
	private double score; 
	
	private double confidence;
	
	private String callName;
	private String callReason;
	
	public InterfaceScore()
	{
		
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
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getMethod() {
		return method;
	}

	public void setInterfaceId(int interfaceId) 
	{
		this.interfaceId = interfaceId;
	}
	
	public int getInterfaceId()
	{
		return interfaceId;
	}

	public void setCallName(String callName) {
		this.callName = callName;
	}

	public String getCallName() {
		return callName;
	}
	
	public String getCallReason() {
		return callReason;
	}
	
	public void setCallReason(String callReason) {
		this.callReason = callReason;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}
	
	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	/**
	 * Converts DB model item into DTO one
	 * @param interfaceScoreDB model item to convert
	 * @return DTO representation of model item
	 */
	public static InterfaceScore create(InterfaceScoreDB interfaceScoreDB)
	{
		InterfaceScore interfaceScore = new InterfaceScore();
		interfaceScore.setCallName(interfaceScoreDB.getCallName());
		interfaceScore.setCallReason(interfaceScoreDB.getCallReason());
		interfaceScore.setInterfaceId(interfaceScoreDB.getInterfaceId());
		interfaceScore.setMethod(interfaceScoreDB.getMethod());
		interfaceScore.setUid(interfaceScoreDB.getUid());
		interfaceScore.setScore1(interfaceScoreDB.getScore1());
		interfaceScore.setScore2(interfaceScoreDB.getScore2());
		interfaceScore.setScore(interfaceScoreDB.getScore());
		interfaceScore.setConfidence(interfaceScoreDB.getConfidence());
		return interfaceScore;
	}

}
