package eppic.model.db;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "UserSession")
public class UserSessionDB implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int uid;
	private String sessionId;
	@Temporal(TemporalType.TIMESTAMP)
	private Date timeStamp;
	private String ip;

	@ManyToMany
	@JoinTable(name = "UserSessionJob", joinColumns = @JoinColumn(name = "userSession_uid", referencedColumnName = "uid"),
			inverseJoinColumns = @JoinColumn(name = "job_uid", referencedColumnName = "uid"))
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
