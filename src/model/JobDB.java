package model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class JobDB implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long uid;
	private String jobId;
	private String status;
	private String email;
	private String ip;
	private String input;
	private Date submissionDate;
	
	private PDBScoreItemDB pdbScoreItem;
	
	private Set<UserSessionDB> userSessions;
	
	public JobDB()
	{
		this.userSessions = new HashSet<UserSessionDB>();
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

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public void setSubmissionDate(Date submissionDate) {
		this.submissionDate = submissionDate;
	}

	public Date getSubmissionDate() {
		return submissionDate;
	}

	public void setPdbScoreItem(PDBScoreItemDB pdbScoreItem) {
		this.pdbScoreItem = pdbScoreItem;
	}

	public PDBScoreItemDB getPdbScoreItem() {
		return pdbScoreItem;
	}

	public void setUserSessions(Set<UserSessionDB> userSessions) {
		this.userSessions = userSessions;
	}

	public Set<UserSessionDB> getUserSessions() {
		return userSessions;
	}
}
