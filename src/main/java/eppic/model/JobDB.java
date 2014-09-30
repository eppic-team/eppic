package eppic.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class JobDB implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long uid;
	
	private String jobId;
	private String status;
	private String email;
	private String ip;
	private String inputName;
	private int inputType;
	private Date submissionDate;
	private String submissionId;
	
	private PdbInfoDB pdbInfo;
	
	private Set<UserSessionDB> userSessions;
	
	public JobDB() {
		this.userSessions = new HashSet<UserSessionDB>();
	}
	
	public JobDB(String inputName, int inputType) {
		this.inputName = inputName;
		this.inputType = inputType;
	}
	
	public Long getUid() {
		return uid;
	}
	
	public void setUid(Long uid) {
		this.uid = uid;
	}
	
	public String getJobId() {
		return jobId;
	}
	
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getInputName() {
		return inputName;
	}

	public void setInputName(String inputName) {
		this.inputName = inputName;
	}

	public void setSubmissionDate(Date submissionDate) {
		this.submissionDate = submissionDate;
	}

	public Date getSubmissionDate() {
		return submissionDate;
	}

	public void setPdbInfo(PdbInfoDB pdbInfo) {
		this.pdbInfo = pdbInfo;
	}

	public PdbInfoDB getPdbInfo() {
		return pdbInfo;
	}

	public void setUserSessions(Set<UserSessionDB> userSessions) {
		this.userSessions = userSessions;
	}

	public Set<UserSessionDB> getUserSessions() {
		return userSessions;
	}

	public void setInputType(int inputType) {
		this.inputType = inputType;
	}

	public int getInputType() {
		return inputType;
	}

	public void setSubmissionId(String submissionId) {
		this.submissionId = submissionId;
	}

	public String getSubmissionId() {
		return submissionId;
	}
}
