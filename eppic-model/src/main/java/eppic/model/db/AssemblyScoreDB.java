package eppic.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;

import java.io.Serializable;

public class AssemblyScoreDB implements Serializable {

	private static final long serialVersionUID = 1L;

	private String method;
	private double score;
	private double confidence;
	
	private String callName;
	private String callReason;

	@JsonBackReference
	private AssemblyDB assembly;

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

	public AssemblyDB getAssembly() {
		return assembly;
	}

	public void setAssembly(AssemblyDB assembly) {
		this.assembly = assembly;
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
}
