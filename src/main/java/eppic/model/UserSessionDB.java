package eppic.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class UserSessionDB implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int uid;
	private String sessionId;
	
	private Set<JobDB> jobs;
	
	public UserSessionDB()
	{
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
	
}
