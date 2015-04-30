package ch.systemsx.sybit.crkwebui.shared.model;

import java.io.Serializable;

import eppic.model.AssemblyScoreDB;

public class AssemblyScore implements Serializable {

	private static final long serialVersionUID = 1L;

	private String method;
	private double score;
	private double confidence;
	
	private Assembly assembly;
	
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

	public Assembly getAssembly() {
		return assembly;
	}

	public void setAssembly(Assembly assembly) {
		this.assembly = assembly;
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
		return assemblyScore;
	}
}
