package eppic.model.db;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Job",
		indexes = {
		// Whenever a job is invoked, the table has to be searched for jobIds, this index is very important!
		@Index(name = "jobId_idx", columnList = "jobId", unique = true),
		// The JobStatusUpdater polls jobs with certain status every few seconds to add any finished jobs
		@Index(name = "status_idx", columnList = "status", unique = false)
})
public class JobDB implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long uid;
	
	private String jobId;
	@Column(length = 15)
	private String status;
	private String email;
	@Column(length = 20)
	private String ip;
	private String inputName;
	private int inputType;
	@Temporal(TemporalType.TIMESTAMP)
	private Date submissionDate;
	private String submissionId;

	@OneToOne(mappedBy = "job", cascade = CascadeType.ALL)
	private PdbInfoDB pdbInfo;

	@ManyToMany(mappedBy = "jobs", cascade = CascadeType.ALL)
	private Set<UserSessionDB> userSessions;
	
	public JobDB() {
		this.userSessions = new HashSet<UserSessionDB>();
	}
	
	public JobDB(String inputName, int inputType) {
		this.inputName = inputName;
		this.inputType = inputType;
	}
	
	public JobDB(String inputName, int inputType, String status) {
		this.inputName = inputName;
		this.inputType = inputType;
		this.status = status;
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
