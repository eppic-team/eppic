package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import eppic.model.AssemblyScoreDB;

@XmlAccessorType(XmlAccessType.FIELD)
public class AssemblyScore implements Serializable {

	private static final long serialVersionUID = 1L;

	private String method;
	private double score;
	private double confidence;
	
	private String callName;
	private String callReason;
	
	
	public AssemblyScore() {
		
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
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

	public String getCallName() {
		return callName;
	}

	public void setCallName(String callName) {
		this.callName = callName;
	}

	public String getCallReason() {
		return callReason;
	}

	public void setCallReason(String callReason) {
		this.callReason = callReason;
	}

	/**
	 * Converts DB model item into DTO one.
	 * @param assemblyScoreDB model item to convert
	 * @return DTO representation of model item
	 */
	public static AssemblyScore create(AssemblyScoreDB assemblyScoreDB) {
		AssemblyScore assemblyScore = new AssemblyScore();
		assemblyScore.setConfidence(assemblyScoreDB.getConfidence());
		assemblyScore.setMethod(assemblyScoreDB.getMethod());
		assemblyScore.setScore(assemblyScoreDB.getScore());
		assemblyScore.setCallName(assemblyScoreDB.getCallName());
		assemblyScore.setCallReason(assemblyScoreDB.getCallReason());
		return assemblyScore;
	}
}
