package eppic.model.db;

import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;


@Table(name = "Job",
		indexes = {
		// Whenever a job is invoked, the table has to be searched for jobIds, this index is very important!
		@Index(name = "jobId_idx", columnList = "jobId", unique = true),
		// The JobStatusUpdater polls jobs with certain status every few seconds to add any finished jobs
		@Index(name = "status_idx", columnList = "status", unique = false)
})
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
	
	public JobDB() {
//		this.userSessions = new HashSet<UserSessionDB>();
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
