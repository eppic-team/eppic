package eppic.model.db;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class UserSessionDB implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int uid;
	private String sessionId;
	private Date timeStamp;
	private String ip;
	
	private Set<JobDB> jobs;
	
	public UserSessionDB() {
		jobs = new HashSet<JobDB>();
	}
	
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setJobs(Set<JobDB> jobs) {
		this.jobs = jobs;
	}

	public Set<JobDB> getJobs() {
		return jobs;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
}
