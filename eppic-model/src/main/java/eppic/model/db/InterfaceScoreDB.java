package eppic.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;
import eppic.model.adapters.InterfaceScoreListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

public class InterfaceScoreDB implements Serializable {

	private static final long serialVersionUID = 1L;

	private String method;
	private int interfaceId;

	private double score1;
	private double score2;
	private double score;
	
	private double confidence;

	private String callName;
	private String callReason;

	@JsonBackReference
	private InterfaceDB interfaceItem;
	
	public InterfaceScoreDB() {
	}
	
	public InterfaceScoreDB(PdbInfoDB pdbScoreItem) {
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

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
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

	public void setInterfaceItem(InterfaceDB interfaceItem) {
		this.interfaceItem = interfaceItem;
	}

	public InterfaceDB getInterfaceItem() {
		return interfaceItem;
	}
	
}
