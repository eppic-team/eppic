package eppic.model.db;

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

@Entity
@Table(name = "InterfaceScore")
@EntityListeners({InterfaceScoreListener.class})
public class InterfaceScoreDB implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int uid;

	@Column(length = 4)
	private String pdbCode;

	@Column(length = 15)
	private String method;
	private int interfaceId;

	@Column(nullable = true)
	private double score1;
	@Column(nullable = true)
	private double score2;
	@Column(nullable = true)
	private double score; 
	
	private double confidence;

	@Column(length = 6)
	private String callName;
	@Column(length = 10000)
	private String callReason;

	@ManyToOne
	private InterfaceDB interfaceItem;
	
	public InterfaceScoreDB() {
	}
	
	public InterfaceScoreDB(PdbInfoDB pdbScoreItem) {
	}
	
	public String getPdbCode() {
		return pdbCode;
	}

	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
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
	
	public void setUid(int uid) {
		this.uid = uid;
	}

	public int getUid() {
		return uid;
	}

}
