package eppic.model.db;

import com.fasterxml.jackson.annotation.JsonBackReference;
import eppic.model.adapters.ClusterScoreListener;

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
@Table(name = "InterfaceClusterScore")
@EntityListeners(ClusterScoreListener.class)
public class InterfaceClusterScoreDB implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int uid;

	@Column(length = 4)
	private String pdbCode;
	private int clusterId;

	@Column(length = 6)
	private String callName;

	@Column(length = 10000)
	private String callReason;

	@Column(nullable = true)
	private double score;	
	private double confidence;
	@Column(nullable = true)
	private double score1;
	@Column(nullable = true)
	private double score2;
	@Column(length = 15)
	private String method;

	@ManyToOne
	@JsonBackReference
	private InterfaceClusterDB interfaceCluster;
	
	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getPdbCode() {
		return pdbCode;
	}

	public void setPdbCode(String pdbCode) {
		this.pdbCode = pdbCode;
	}

	public int getClusterId() {
		return clusterId;
	}

	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
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

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public InterfaceClusterDB getInterfaceCluster() {
		return interfaceCluster;
	}

	public void setInterfaceCluster(InterfaceClusterDB interfaceCluster) {
		this.interfaceCluster = interfaceCluster;
	}

}
